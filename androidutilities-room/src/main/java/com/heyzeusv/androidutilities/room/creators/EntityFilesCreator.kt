package com.heyzeusv.androidutilities.room.creators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.heyzeusv.androidutilities.room.util.CloseClass
import com.heyzeusv.androidutilities.room.util.Constants.ANNOTATION_COLUMN_INFO
import com.heyzeusv.androidutilities.room.util.Constants.ANNOTATION_EMBEDDED
import com.heyzeusv.androidutilities.room.util.Constants.ANNOTATION_ENTITY
import com.heyzeusv.androidutilities.room.util.Constants.ANNOTATION_FTS4
import com.heyzeusv.androidutilities.room.util.Constants.ANNOTATION_IGNORE
import com.heyzeusv.androidutilities.room.util.Constants.EXTENSION_KT
import com.heyzeusv.androidutilities.room.util.EmbeddedInfo
import com.heyzeusv.androidutilities.room.util.EntityInfo
import com.heyzeusv.androidutilities.room.util.FieldInfo
import com.heyzeusv.androidutilities.room.util.PropertyInfo
import com.heyzeusv.androidutilities.room.util.TypeConverterInfo
import com.heyzeusv.androidutilities.room.util.addIndented
import com.heyzeusv.androidutilities.room.util.asListTypeName
import com.heyzeusv.androidutilities.room.util.containsNullableType
import com.heyzeusv.androidutilities.room.util.equalsNullableType
import com.heyzeusv.androidutilities.room.util.getAnnotationArgumentValue
import com.heyzeusv.androidutilities.room.util.getName
import com.heyzeusv.androidutilities.room.util.getPackageName
import com.heyzeusv.androidutilities.room.util.getUtilName
import com.heyzeusv.androidutilities.room.util.ifNotBlankAppend
import com.heyzeusv.androidutilities.room.util.removeKotlinPrefix
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import kotlin.Exception

/**
 *  Creates *RoomUtil files from classes annotated with Room.Entity.
 *
 *  @param codeGenerator Creates the files.
 *  @param symbols Classes annotated with Room.Entity.
 *  @param typeConverterInfoList Information on classes annotated with Room.TypeConverter.
 *  @param logger Used to print info to output.
 */
internal class EntityFilesCreator(
    private val codeGenerator: CodeGenerator,
    dbClassDeclaration: KSClassDeclaration,
    private val symbols: Sequence<KSAnnotated>,
    private val typeConverterInfoList: List<TypeConverterInfo>,
    private val logger: KSPLogger,
) {
    /**
     *  List of primitive types in the form of [ClassName] that Room accepts without needing a
     *  TypeConverter.
     */
    private val validRoomTypes = listOf(
        ClassName("kotlin", "Boolean"), ClassName("kotlin", "Short"),
        ClassName("kotlin", "Int"), ClassName("kotlin", "Long"),
        ClassName("kotlin", "Byte"), ClassName("kotlin", "String"),
        ClassName("kotlin", "Char"), ClassName("kotlin", "Double"),
        ClassName("kotlin", "Float"), ClassName("kotlin", "ByteArray"),
    )

    private val _entityInfoList = mutableListOf<EntityInfo>()
    val entityInfoList: List<EntityInfo> get() = _entityInfoList

    private val csvDataClassName = ClassName(dbClassDeclaration.getPackageName(), "CsvData")
    private val csvInfoClassName = ClassName(dbClassDeclaration.getPackageName(), "CsvInfo")

    /**
     *  Go through all [symbols] and create a *RoomUtil file from each using inner class
     *  [EntityBuilder].
     */
    private fun createEntityFiles() {
        symbols.filterIsInstance<KSClassDeclaration>().forEach { symbol ->
            (symbol as? KSClassDeclaration)?.let { classDeclaration ->
                // skip any classes that are annotated with Room.Fts4
                if (classDeclaration.annotations.any { it.getName() == ANNOTATION_FTS4 }) {
                    logger.warn("Entity annotated with Fts4 detected...\n" +
                            "   Make sure to rebuild Fts4 table after import using: " +
                            "   INSERT INTO table_name(table_name) VALUES('rebuild');\n" +
                            "   Will not be part of import/export...")
                    return@forEach
                }
                val entityBuilder = EntityBuilder(classDeclaration)

                // write the file
                codeGenerator.createNewFile(
                    dependencies = Dependencies(false, symbol.containingFile!!),
                    packageName = entityBuilder.packageName,
                    fileName = entityBuilder.fileName,
                    extensionName = EXTENSION_KT,
                ).bufferedWriter().use { entityBuilder.fileBuilder.build().writeTo(it) }
            }
        }
    }

    init {
        createEntityFiles()
    }

    /**
     *  Builds *RoomUtil from given [classDeclaration] using KotlinPoet's *Spec.Builders.
     */
    private inner class EntityBuilder(private val classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.getPackageName()
        val fileName = classDeclaration.getUtilName()
        val fileBuilder = FileSpec.builder(packageName, fileName)

        /**
         *  Get table name by first searching for tableName parameter of Room.Entity annotation.
         *  If it is blank, then use the name of the class annotated with Room.Entity.
         */
        private val tableName = classDeclaration
            .getAnnotationArgumentValue(ANNOTATION_ENTITY, "tableName")
            .ifBlank { classDeclaration.simpleName.getShortName() }

        val classBuilder = TypeSpec.classBuilder(fileName)
            .addModifiers(KModifier.DATA)
            .addSuperinterface(csvDataClassName)
            .addProperty(PropertySpec.builder("tableName", String::class)
                .initializer("%S", tableName)
                .build()
            )
        private val constructorBuilder = FunSpec.constructorBuilder()
        private val companionObjectBuilder = TypeSpec.companionObjectBuilder()
            .addSuperinterface(csvInfoClassName)

        private val propertyInfoList = mutableListOf<PropertyInfo>()

        /**
         *  Goes through all properties of [classDeclaration] and builds *RoomUtil's
         *  properties/parameters by adding parameters to [constructorBuilder] and adding
         *  properties to [classBuilder]. Recursively called when property is annotated with
         *  Room.Embedded in order to flatten out embedded class. Also checks if property type is
         *  accepted by Room natively, else it searches [typeConverterInfoList] to get correct
         *  type when saved to Room.
         */
        private fun buildProperties(
            classDeclaration: KSClassDeclaration,
            embeddedPrefix: String = "",
        ) {
            classDeclaration.getAllProperties().forEach { prop ->
                val name = prop.getName()
                val annotations = prop.annotations.map { it.getName() }

                // print log message and continue
                if (annotations.contains(ANNOTATION_IGNORE)) {
                    logger.info("Ignoring field $name of type ${prop.type}")
                // flatten embedded class by recursively calling this function and passing prefix (if any)
                } else if (annotations.contains(ANNOTATION_EMBEDDED)) {
                    logger.info("Flattening embedded class ${prop.type}")

                    val embeddedClass = prop.type.resolve().declaration as KSClassDeclaration
                    // get prefix by getting Room.Embedded.prefix value
                    val newPrefix = prop.getAnnotationArgumentValue(ANNOTATION_EMBEDDED, "prefix")
                    val embeddedInfo = EmbeddedInfo(name, embeddedClass)
                    propertyInfoList.add(embeddedInfo)

                    buildProperties(
                        classDeclaration = embeddedClass,
                        embeddedPrefix = "$embeddedPrefix$newPrefix",
                    )
                } else {
                    val androidType: TypeName = prop.type.toTypeName()
                    // search for TypeConverter if prop.type is not accepted by Room
                    val roomType: TypeName =
                        if (validRoomTypes.containsNullableType(prop.type.toTypeName())) {
                            prop.type.toTypeName()
                        } else {
                            val tcInfo = typeConverterInfoList.find {
                                it.returnType.equalsNullableType(prop.type.toTypeName())
                            } ?: throw Exception("No TypeConverter found with $androidType return type!!")
                            tcInfo.parameterType
                        }

                    var fieldName = "$embeddedPrefix$name"
                    // check if custom field name has been set using ColumnInfo.name
                    if (annotations.contains(ANNOTATION_COLUMN_INFO)) {
                        val columnName =
                            prop.getAnnotationArgumentValue(ANNOTATION_COLUMN_INFO, "name")
                        // checks if columnName is different from its default value
                        if (columnName != "[field-name]") fieldName = "$embeddedPrefix$columnName"
                    }

                    constructorBuilder.addParameter(fieldName, roomType)
                    classBuilder.addProperty(PropertySpec.builder(fieldName, roomType)
                        .initializer(fieldName)
                        .build()
                    )
                    val fieldInfo = FieldInfo(
                        name = name,
                        fieldName = fieldName,
                        androidType = androidType,
                        roomType = roomType
                    )
                    propertyInfoList.add(fieldInfo)
                }
            }
            propertyInfoList.add(CloseClass())
        }

        /**
         *  Builds functions that convert from original entity type to RoomUtil type and vice versa.
         */
        private fun buildToFunctions() {
            val toOriginalFunBuilder = FunSpec.builder("toOriginal")
                .returns(classDeclaration.toClassName())
                .addCode(buildCodeBlock {
                    add("return %L(\n", classDeclaration.getName())
                    addIndented {
                        propertyInfoList.forEach { info -> buildToOriginalProperties(info) }
                    }
                    add(")")
                })

            val toUtilFunBuilder = FunSpec.builder("toUtil")
                .returns(ClassName(classDeclaration.getPackageName(), classDeclaration.getUtilName()))
                .addParameter("entity", classDeclaration.toClassName())
                .addCode(buildCodeBlock {
                    add("return %L(\n", classDeclaration.getUtilName())
                    addIndented {
                        val toUtilEmbeddedPrefixList = mutableListOf<String>()
                        propertyInfoList.forEach { info ->
                            buildToUtilProperties(info, toUtilEmbeddedPrefixList)
                        }
                    }
                    add(")")
                })

            classBuilder.addFunction(toOriginalFunBuilder.build())
            companionObjectBuilder.addFunction(toUtilFunBuilder.build())
        }

        /**
         *  Adds one line to [CodeBlock] for toOriginal(), which depends on given [info] type.
         */
        private fun CodeBlock.Builder.buildToOriginalProperties(info: PropertyInfo) {
            when (info) {
                is FieldInfo -> {
                    // no TypeConverter needed if androidType = roomType
                    if (info.androidType == info.roomType) {
                        add("%L = %L,\n", info.name, info.fieldName)
                    } else {
                        // search for TypeConverter depending on info androidType and roomTyp
                        val tcInfo = typeConverterInfoList.find {
                            it.parameterType == info.roomType && it.returnType == info.androidType
                        } ?: throw Exception("No TypeConverter found with ${info.roomType} " +
                                "parameter and ${info.androidType} return type!!")
                        val tcClass = ClassName(tcInfo.packageName, tcInfo.className)
                        // use KotlinPoet format specifier to automatically import TypeConverter
                        // and call it.
                        add(
                            "%L = %T().%L(%L),\n",
                            info.name, tcClass , tcInfo.functionName, info.fieldName
                        )
                    }
                }
                // calls embedded class
                is EmbeddedInfo -> {
                    add("%L = %L(\n", info.name, info.embeddedClass.getName())
                    indent()
                }
                // closes embedded classes
                is CloseClass -> {
                    unindent()
                    add("),\n")
                }
            }
        }

        /**
         *  Adds one line to [CodeBlock] for toUtil(), which depends on given [info] type. Adds
         *  all strings of [embeddedPrefixList] as prefix to parameter if [info] is [FieldInfo].
         */
        private fun CodeBlock.Builder.buildToUtilProperties(
            info: PropertyInfo,
            embeddedPrefixList: MutableList<String>,
        ) {
            when (info) {
                is FieldInfo -> {
                    val prefix = embeddedPrefixList.joinToString(separator = ".")
                        .ifNotBlankAppend(".")
                    // no TypeConverter needed if androidType = roomType
                    if (info.androidType == info.roomType) {
                        add("%L = entity.%L%L,\n", info.fieldName, prefix, info.name)
                    } else {
                        // search for TypeConverter depending on info androidType and roomType
                        val tcInfo = typeConverterInfoList.find {
                            it.parameterType == info.androidType && it.returnType == info.roomType
                        } ?: throw Exception("No TypeConverter found with ${info.androidType} " +
                                "parameter and ${info.roomType} return type!!")
                        val tcClass = ClassName(tcInfo.packageName, tcInfo.className)
                        // use KotlinPoet format specifier to automatically import TypeConverter
                        // and call it.
                        add(
                            "%L = %T().%L(entity.%L%L),\n",
                            info.fieldName, tcClass, tcInfo.functionName, prefix, info.name
                        )
                    }
                }
                is EmbeddedInfo -> embeddedPrefixList.add(info.name)
                is CloseClass -> embeddedPrefixList.remove(info.name)
            }
        }

        /**
         *  Builds the implementations of CsvInfo and CsvData using given [fieldInfoList].
         */
        private fun buildCsvInterfaceImplementations(fieldInfoList: List<FieldInfo>) {
            val fileNamePropBuilder = PropertySpec.builder("csvFileName", String::class)
                .addModifiers(KModifier.OVERRIDE)
                .initializer("%S", "$tableName.csv")

            val stringMapClass = Map::class.asTypeName()
                .parameterizedBy(String::class.asTypeName(), String::class.asTypeName())
            val fieldToTypeMapPropBuilder = PropertySpec
                .builder("csvFieldToTypeMap", stringMapClass)
                .addModifiers(KModifier.OVERRIDE)
                .initializer(buildCodeBlock {
                    addStatement("mapOf(")
                    addIndented {
                        fieldInfoList.forEach { fieldInfo ->
                            addStatement(
                                "%S to %S,",
                                fieldInfo.fieldName, fieldInfo.roomType.removeKotlinPrefix()
                            )
                        }
                    }
                    add(")")
                })

            companionObjectBuilder.addProperty(fileNamePropBuilder.build())
                .addProperty(fieldToTypeMapPropBuilder.build())

            val anyListClass = Any::class.asListTypeName(nullable = true)
            val rowPropBuilder = PropertySpec.builder("csvRow", anyListClass)
                .addModifiers(KModifier.OVERRIDE)
                .initializer(buildCodeBlock {
                    addStatement("listOf(")
                    addIndented {
                        fieldInfoList.forEach { addStatement("%L,", it.fieldName) }
                    }
                    add(")")
                })

            classBuilder.addProperty(rowPropBuilder.build())
        }

        init {
            logger.info("Creating file $fileName...")

            // build parameters/properties
            buildProperties(classDeclaration)
            val entityInfo = EntityInfo(
                originalClassName = classDeclaration.toClassName(),
                utilClassName = ClassName(classDeclaration.getPackageName(), classDeclaration.getUtilName()),
                tableName = tableName,
                fieldInfoList = propertyInfoList.filterIsInstance<FieldInfo>()
            )
            _entityInfoList.add(entityInfo)
            // removes extra CloseClass that is added
            propertyInfoList.removeAt(propertyInfoList.lastIndex)

            // build toOriginal/toUtil functions
            buildToFunctions()
            // build CsvInfo/CsvData implementations
            buildCsvInterfaceImplementations(entityInfo.fieldInfoList)

            // add constructor to class
            classBuilder.primaryConstructor(constructorBuilder.build())
            // add companion object to class
            classBuilder.addType(companionObjectBuilder.build())

            // add class to file
            fileBuilder.addType(classBuilder.build())
        }
    }
}