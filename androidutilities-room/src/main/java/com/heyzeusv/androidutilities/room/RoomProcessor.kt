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
import com.heyzeusv.androidutilities.room.util.addOriginalAndUtil
import com.heyzeusv.androidutilities.room.util.getPackageName
import com.heyzeusv.androidutilities.room.util.getUtilName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

class RoomProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val logger = environment.logger

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val entityDataList = mutableListOf<EntityData>()

        // get all symbols
        val tcSymbols = resolver.getSymbolsWithAnnotation("androidx.room.TypeConverter")
        val symbols = resolver.getSymbolsWithAnnotation("androidx.room.Entity")
        val dbSymbol = resolver.getSymbolsWithAnnotation("androidx.room.Database")

        logger.info("Creating list of TypeConverterInfo...")
        val typeConverterInfoList = createTypeConverterInfoList(tcSymbols)
        logger.info("List of TypeConvertInfo: $typeConverterInfoList")

        val classNameMap = mutableMapOf<ClassName, ClassName>()
        // filter out symbols that are not classes
        symbols.filterIsInstance<KSClassDeclaration>().forEach { symbol ->
            (symbol as? KSClassDeclaration)?.let { classDeclaration ->
                if (classDeclaration.annotations.any { it.shortName.getShortName() == "Fts4" }) {
                    return@forEach
                }
                val packageName = classDeclaration.containingFile?.packageName?.asString().orEmpty()
                val fileName = classDeclaration.getUtilName()
                classNameMap.addOriginalAndUtil(classDeclaration)

                logger.info("class name: $fileName")

                // use KotlinPoet for code generation
                val fileSpecBuilder = FileSpec.builder(packageName, fileName)

                val classBuilder = buildEntityClass(
                    typeConverterInfoList = typeConverterInfoList,
                    classDeclaration = classDeclaration,
                    entityDataList = entityDataList,
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
                val dbPackageName = dbClass.getPackageName()

                val roomDataFileName = "RoomData"
                val roomDataFileSpec = FileSpec.builder(dbPackageName, roomDataFileName)
                val roomDataTypeSpec = TypeSpec.classBuilder(roomDataFileName)
                    .buildRoomData(classNameMap = classNameMap)
                roomDataFileSpec.addType(roomDataTypeSpec.build())

                environment.codeGenerator.createNewFile(
                    dependencies = Dependencies(false, dbClass.containingFile!!),
                    packageName = dbPackageName,
                    fileName = roomDataFileName,
                    extensionName = "kt",
                ).bufferedWriter().use { roomDataFileSpec.build().writeTo(it) }

                val roomDataClassName = ClassName(dbPackageName, roomDataFileName)
                val csvConverterFileName = "CsvConverter"
                val csvConverterFileSpec = FileSpec.builder(dbPackageName, csvConverterFileName)
                val csvConverterTypeSpec = TypeSpec.classBuilder(csvConverterFileName)
                    .buildCsvConverter(
                        roomDataClassName = roomDataClassName,
                        entityDataList = entityDataList,
                    )
                csvConverterFileSpec.addType(csvConverterTypeSpec.build())

                environment.codeGenerator.createNewFile(
                    dependencies = Dependencies(false, dbClass.containingFile!!),
                    packageName = dbPackageName,
                    fileName = csvConverterFileName,
                    extensionName = "kt",
                ).bufferedWriter().use { csvConverterFileSpec.build().writeTo(it) }
            }
        }

        // filter out symbols that are not valid
        val ret = symbols.filterNot { it.validate() }.toList()
        return ret
    }
}

/**
 *  Returns a list of [TypeConverterInfo] created from given [symbols] which have been annotated
 *  with Room.TypeConverter.
 *
 *  @param symbols Sequence of functions annotated with Room.TypeConverter.
 *  @return List of [TypeConverterInfo].
 */
private fun createTypeConverterInfoList(symbols: Sequence<KSAnnotated>): List<TypeConverterInfo> {
    val typeConverterInfoList = mutableListOf<TypeConverterInfo>()

    symbols.filterIsInstance<KSFunctionDeclaration>().forEach { symbol ->
        (symbol as? KSFunctionDeclaration)?.let { functionDeclaration ->
            val tcInfo = TypeConverterInfo.fromFunctionDeclaration(functionDeclaration)
            typeConverterInfoList.add(tcInfo)
        }
    }

    return typeConverterInfoList
}