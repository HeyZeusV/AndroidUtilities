package com.heyzeusv.androidutilities.room

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
import com.heyzeusv.androidutilities.room.csv.buildCsvConverter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.toTypeName

class RoomProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val logger = environment.logger
    private val tcInfoMap: Map<RoomTypes, MutableList<TypeConverterInfo>> = mapOf(
        RoomTypes.TO_ACCEPTED to mutableListOf(),
        RoomTypes.TO_COMPLEX to mutableListOf()
    )

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val csvFileNames = mutableListOf<String>()
        // get all symbols
        val symbols = resolver.getSymbolsWithAnnotation("androidx.room.Entity")
            .plus(resolver.getSymbolsWithAnnotation("androidx.room.ColumnInfo"))
            .plus(resolver.getSymbolsWithAnnotation("androidx.room.Ignore"))
            .plus(resolver.getSymbolsWithAnnotation("androidx.room.Embedded"))
            .plus(resolver.getSymbolsWithAnnotation("androidx.room.TypeConverter"))
            .toSet()
         val dbSymbol = resolver.getSymbolsWithAnnotation("androidx.room.Database")

        symbols.filterIsInstance<KSFunctionDeclaration>().forEach { functionDeclaration ->
            val packageName = functionDeclaration.containingFile?.packageName?.asString().orEmpty()
            val parentClass = functionDeclaration.parentDeclaration?.simpleName?.getShortName().orEmpty()
            val functionName = functionDeclaration.simpleName.getShortName()
            val parameterType = functionDeclaration.parameters.first().type.toTypeName()
            val returnType = functionDeclaration.returnType?.toTypeName()!!
            val info = TypeConverterInfo(
                packageName = packageName,
                parentClass = parentClass,
                functionName = functionName,
                parameterType = parameterType,
                returnType = returnType
            )
            if (RoomTypes.TO_ACCEPTED.types.containsNullableType(returnType)) {
                tcInfoMap[RoomTypes.TO_ACCEPTED]!!.add(info)
            } else {
                tcInfoMap[RoomTypes.TO_COMPLEX]!!.add(info)
            }
        }

        val classNameMap = mutableMapOf<ClassName, ClassName>()
        // filter out symbols that are not classes
        symbols.filterIsInstance<KSClassDeclaration>().forEach { symbol ->
            (symbol as? KSClassDeclaration)?.let { classDeclaration ->
                val packageName = classDeclaration.containingFile?.packageName?.asString().orEmpty()
                val fileName = classDeclaration.utilName()
                classNameMap.addOriginalAndUtil(classDeclaration)

                logger.info("class name: $fileName")

                // use KotlinPoet for code generation
                val fileSpecBuilder = FileSpec.builder(packageName, fileName)

                val classBuilder = recreateEntityClass(
                    tcInfoMap = tcInfoMap,
                    classDeclaration = classDeclaration,
                    csvFileNames = csvFileNames,
                    logger = logger,
                )

                fileSpecBuilder.addType(classBuilder.build())

                // writing the file
                environment.codeGenerator.createNewFile(
                    dependencies = Dependencies(false, symbol.containingFile!!),
                    packageName = packageName,
                    fileName = fileName,
                    extensionName = "kt",
                ).bufferedWriter().use {
                    fileSpecBuilder.build().writeTo(it)
                }
            }
        }
        dbSymbol.filterIsInstance<KSClassDeclaration>().forEach { symbol ->
            (symbol as? KSClassDeclaration)?.let { dbClass ->
                buildRoomData(
                    codeGenerator = environment.codeGenerator,
                    dbClass = dbClass,
                    classNameMap = classNameMap,
                    logger = logger,
                )

                buildCsvConverter(
                    codeGenerator = environment.codeGenerator,
                    dbClass = dbClass,
                    csvFileNames = csvFileNames,
                    logger = logger,
                )
            }
        }

        // filter out symbols that are not valid
        val ret = symbols.filterNot { it.validate() }.toList()
        return ret
    }
}


fun KSClassDeclaration.className(): String = simpleName.getShortName()
fun KSClassDeclaration.utilName(): String = "${className()}RoomUtil"
fun KSClassDeclaration.packageName(): String = containingFile?.packageName?.asString().orEmpty()
fun CodeBlock.Builder.addIndented(code: CodeBlock.Builder.() -> Unit): CodeBlock.Builder = apply {
    indent()
    code()
    unindent()
}

fun MutableMap<ClassName, ClassName>.addOriginalAndUtil(classDeclaration: KSClassDeclaration) {
    val packageName = classDeclaration.packageName()
    val className = classDeclaration.className()
    val utilName = classDeclaration.utilName()
    this[ClassName(packageName, className)] = ClassName(packageName, utilName)
}