package com.heyzeusv.androidutilities.room

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
import com.heyzeusv.androidutilities.room.creators.CsvConverterCreator
import com.heyzeusv.androidutilities.room.creators.EntityFilesCreator
import com.heyzeusv.androidutilities.room.creators.RoomBackupRestoreCreator
import com.heyzeusv.androidutilities.room.creators.RoomDataCreator
import com.heyzeusv.androidutilities.room.creators.RoomUtilBaseCreator
import com.heyzeusv.androidutilities.room.util.TypeConverterInfo

class RoomProcessor(
    private val codeGenerator: CodeGenerator,
    private val options: Map<String, String>,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val csvOption = options["roomUtilCsv"].toBoolean()
        val dbOption = options["roomUtilDb"].toBoolean()
        val hiltOption = options["roomUtilHilt"].toBoolean()

        // get all symbols
        val tcSymbols = resolver.getSymbolsWithAnnotation("androidx.room.TypeConverter")
        val eSymbols = resolver.getSymbolsWithAnnotation("androidx.room.Entity")
        val dbSymbols = resolver.getSymbolsWithAnnotation("androidx.room.Database")

        val typeConverterInfoList = createTypeConverterInfoList(tcSymbols, logger)

        val entityFilesCreator = EntityFilesCreator(
            codeGenerator = codeGenerator,
            symbols = eSymbols,
            typeConverterInfoList = typeConverterInfoList,
            logger = logger
        )

        dbSymbols.filterIsInstance<KSClassDeclaration>().forEach { symbol ->
            (symbol as? KSClassDeclaration)?.let { dbClass ->
                RoomUtilBaseCreator(
                    codeGenerator = codeGenerator,
                    dbClassDeclaration = dbClass,
                    logger = logger,
                )
                RoomDataCreator(
                    codeGenerator = codeGenerator,
                    dbClassDeclaration = dbClass,
                    entityInfoList = entityFilesCreator.entityInfoList,
                    logger = logger,
                )
                CsvConverterCreator(
                    codeGenerator = codeGenerator,
                    dbClassDeclaration = dbClass,
                    entityInfoList = entityFilesCreator.entityInfoList,
                    logger = logger,
                )
                RoomBackupRestoreCreator(
                    codeGenerator = codeGenerator,
                    dbClassDeclaration = dbClass,
                    logger = logger,
                )
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
    symbols: Sequence<KSAnnotated>,
    logger: KSPLogger
): List<TypeConverterInfo> =
    symbols.filterIsInstance<KSFunctionDeclaration>().run {
        if (count() > 0) {
            logger.info("Creating list of TypeConverterInfo...")
            val typeConverterInfoList = mutableListOf<TypeConverterInfo>()

            forEach { symbol ->
                (symbol as? KSFunctionDeclaration)?.let { functionDeclaration ->
                    val tcInfo = TypeConverterInfo.fromFunctionDeclaration(functionDeclaration)
                    typeConverterInfoList.add(tcInfo)
                }
            }
            logger.info("List of TypeConverterInfo: $typeConverterInfoList")
            typeConverterInfoList
        } else {
            logger.info("No TypeConverters found...")
            emptyList()
        }
    }