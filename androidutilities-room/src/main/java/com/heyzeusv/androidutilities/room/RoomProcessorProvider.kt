package com.heyzeusv.androidutilities.room

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class RoomProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RoomProcessor(
            codeGenerator = environment.codeGenerator,
            options = environment.options,
            logger = environment.logger,
        )
    }
}