package com.heyzeusv.androidutilities.room

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
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
        val eSymbols = resolver.getSymbolsWithAnnotation("androidx.room.Entity")
        val dbSymbols = resolver.getSymbolsWithAnnotation("androidx.room.Database")

        val typeConverterInfoList = createTypeConverterInfoList(logger, tcSymbols)

        val classNameMap = mutableMapOf<ClassName, ClassName>()
        // filter out symbols that are not classes
        eSymbols.filterIsInstance<KSClassDeclaration>().forEach { symbol ->
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
        dbSymbols.filterIsInstance<KSClassDeclaration>().forEach { symbol ->
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
        val tcRet = tcSymbols.filterNot { it.validate() }.toList()
        val eRet = eSymbols.filterNot { it.validate() }.toList()
        val dbRet = dbSymbols.filterNot { it.validate() }.toList()
        return tcRet + eRet + dbRet
    }
}

/**
 *  Returns a list of [TypeConverterInfo] created from given [symbols] which have been annotated
 *  with Room.TypeConverter.
 *
 *  @param logger Used to print out messages in log.
 *  @param symbols Sequence of functions annotated with Room.TypeConverter.
 *  @return List of [TypeConverterInfo].
 */
private fun createTypeConverterInfoList(
    logger: KSPLogger,
    symbols: Sequence<KSAnnotated>
): List<TypeConverterInfo> =
    symbols.filterIsInstance<KSFunctionDeclaration>().run {
        logger.info("Creating list of TypeConverterInfo...")
        val typeConverterInfoList = mutableListOf<TypeConverterInfo>()

        forEach { symbol ->
            (symbol as? KSFunctionDeclaration)?.let { functionDeclaration ->
                val tcInfo = TypeConverterInfo.fromFunctionDeclaration(functionDeclaration)
                typeConverterInfoList.add(tcInfo)
            }
        }
        logger.info("List of TypeConvertInfo: $typeConverterInfoList")
        typeConverterInfoList
    }