package com.heyzeusv.androidutilities.room

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName

class RoomProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val logger = environment.logger

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // get all symbols with Entity
        val symbols = resolver.getSymbolsWithAnnotation("androidx.room.Entity")
            .plus(resolver.getSymbolsWithAnnotation("androidx.room.ColumnInfo"))
            .plus(resolver.getSymbolsWithAnnotation("androidx.room.Ignore"))
            .toSet()

        // filter out symbols that are not classes
        symbols.filterIsInstance<KSClassDeclaration>().forEach { symbol ->
            (symbol as? KSClassDeclaration)?.let { classDeclaration ->
                val packageName = classDeclaration.containingFile?.packageName?.asString().orEmpty()
                val className = "${classDeclaration.simpleName.getShortName()}RoomUtil"

                logger.info("class name: $className")

                // use KotlinPoet for code generation
                val fileSpecBuilder = FileSpec.builder(packageName, className)

                // create copy of class
                val classBuilder = TypeSpec.classBuilder(className)
                    .addModifiers(KModifier.DATA)
                    .primaryConstructor(classDeclaration, logger)
                    .properties(classDeclaration, logger)

                fileSpecBuilder.addType(classBuilder.build())

                // writing the file
                environment.codeGenerator.createNewFile(
                    dependencies = Dependencies(false, symbol.containingFile!!),
                    packageName = packageName,
                    fileName = className,
                    extensionName = "kt",
                ).bufferedWriter().use {
                    fileSpecBuilder.build().writeTo(it)
                }
            }
        }

        // filter out symbols that are not valid
        val ret = symbols.filterNot { it.validate() }.toList()
        return ret
    }
}

private fun TypeSpec.Builder.primaryConstructor(
    classDeclaration: KSClassDeclaration,
    logger: KSPLogger,
): TypeSpec.Builder {
    val cBuilder = FunSpec.constructorBuilder()
    classDeclaration.primaryConstructor?.parameters?.forEach { parameter ->
        parameter.name?.getShortName()?.let { name ->
            if (!parameter.annotations.any { it.shortName.getShortName() == "Ignore" }) {
                logger.info("parameter name: ${name}, type: ${parameter.type}")

                cBuilder.addParameter(name, parameter.type.toTypeName())
            } else {
                logger.info("ignored! name: $name}")
            }
        }
    }
    this.primaryConstructor(cBuilder.build())

    return this
}

private fun TypeSpec.Builder.properties(
    classDeclaration: KSClassDeclaration,
    logger: KSPLogger,
): TypeSpec.Builder {
    classDeclaration.getAllProperties().forEach { prop ->
        prop.qualifiedName?.getShortName()?.let { name ->
            if (!prop.annotations.any { it.shortName.getShortName() == "Ignore" }) {
                logger.info("property name: ${name}, type: ${prop.type}")
                this.addProperty(PropertySpec.builder(name, prop.type.toTypeName()).initializer(name).build())
            } else {
                logger.info("ignored! name: $name")
            }
        }
    }
    return this
}