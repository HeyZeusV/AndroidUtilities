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
import com.heyzeusv.androidutilities.room.creators.CsvInterfacesCreator
import com.heyzeusv.androidutilities.room.creators.EntityFilesCreator
import com.heyzeusv.androidutilities.room.creators.RoomBackupRestoreCreator
import com.heyzeusv.androidutilities.room.creators.RoomDataCreator
import com.heyzeusv.androidutilities.room.creators.RoomUtilBaseCreator
import com.heyzeusv.androidutilities.room.creators.RoomUtilStatusCreator
import com.heyzeusv.androidutilities.room.util.Constants.FALSE
import com.heyzeusv.androidutilities.room.util.Constants.OPTION_CSV
import com.heyzeusv.androidutilities.room.util.Constants.OPTION_DB
import com.heyzeusv.androidutilities.room.util.Constants.OPTION_HILT
import com.heyzeusv.androidutilities.room.util.Constants.PACKAGE_DATABASE
import com.heyzeusv.androidutilities.room.util.Constants.PACKAGE_ENTITY
import com.heyzeusv.androidutilities.room.util.Constants.PACKAGE_TYPE_CONVERTER
import com.heyzeusv.androidutilities.room.util.TypeConverterInfo

class RoomProcessor(
    private val codeGenerator: CodeGenerator,
    private val options: Map<String, String>,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val csvOption = options[OPTION_CSV]
        val dbOption = options[OPTION_DB]
        val hiltOption = options[OPTION_HILT]

        // get all symbols
        val tcSymbols = resolver.getSymbolsWithAnnotation(PACKAGE_TYPE_CONVERTER)
        val eSymbols = resolver.getSymbolsWithAnnotation(PACKAGE_ENTITY)
        val dbSymbols = resolver.getSymbolsWithAnnotation(PACKAGE_DATABASE)

        dbSymbols.filterIsInstance<KSClassDeclaration>().forEach { symbol ->
            (symbol as? KSClassDeclaration)?.let { dbClass ->
                if (csvOption?.lowercase() != FALSE) {
                    val typeConverterInfoList = createTypeConverterInfoList(tcSymbols, logger)

                    CsvInterfacesCreator(
                        codeGenerator = codeGenerator,
                        dbClassDeclaration = dbClass,
                        logger = logger,
                    )

                    val entityInfoList = EntityFilesCreator(
                        codeGenerator = codeGenerator,
                        dbClassDeclaration = dbClass,
                        symbols = eSymbols,
                        typeConverterInfoList = typeConverterInfoList,
                        logger = logger
                    ).entityInfoList

                    RoomUtilBaseCreator(
                        codeGenerator = codeGenerator,
                        dbClassDeclaration = dbClass,
                        logger = logger,
                    )

                    RoomDataCreator(
                        codeGenerator = codeGenerator,
                        dbClassDeclaration = dbClass,
                        entityInfoList = entityInfoList,
                        logger = logger,
                    )

                    CsvConverterCreator(
                        codeGenerator = codeGenerator,
                        hiltOption = hiltOption,
                        dbClassDeclaration = dbClass,
                        entityInfoList = entityInfoList,
                        logger = logger,
                    )
                }
                if (dbOption?.lowercase() != FALSE) {
                    RoomBackupRestoreCreator(
                        codeGenerator = codeGenerator,
                        hiltOption = hiltOption,
                        dbClassDeclaration = dbClass,
                        logger = logger,
                    )
                }
                RoomUtilStatusCreator(
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