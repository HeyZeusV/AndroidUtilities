package com.heyzeusv.androidutilities.room

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec

class RoomProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val logger = environment.logger

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // get all symbols with Entity
        val symbols = resolver.getSymbolsWithAnnotation("androidx.room.Entity")
            .plus(resolver.getSymbolsWithAnnotation("androidx.room.ColumnInfo"))
            .toSet()

        // filter out symbols that are not classes
        symbols.filterIsInstance<KSClassDeclaration>().forEach { symbol ->
            (symbol as? KSClassDeclaration)?.let { classDeclaration ->
                val packageName = classDeclaration.containingFile?.packageName?.asString().orEmpty()
                val className = classDeclaration.simpleName.getShortName()
                val fileName = "${className}Impl"

                logger.info("class name: $className")
                val declarations = classDeclaration.containingFile?.declarations
                declarations?.forEach { logger.info("declarations $it") }
                symbol.primaryConstructor?.parameters?.forEach { parameter ->
                    val name = parameter.name?.getShortName() ?: "empty name"
                    logger.info("parameter name: ${name}, type: ${parameter.type}")
                }
                symbol.getAllProperties().forEach { prop ->
                    val name = prop.qualifiedName?.getShortName()
                    logger.info("property name: ${name}, type: ${prop.type}")
                }

                // use KotlinPoet for code generation
                val fileSpecBuilder = FileSpec.builder(packageName, fileName)

//                // create copy of class
//                val classBuilder = TypeSpec.classBuilder(className)
//                    .addModifiers(KModifier.DATA)
//                    .primaryConstructor(classDeclaration)
//
//                fileSpecBuilder.addType(classBuilder.build())

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

        // filter out symbols that are not valid
        val ret = symbols.filterNot { it.validate() }.toList()
        return ret
    }
}

//private fun TypeSpec.Builder.primaryConstructor(
//    classDeclaration: KSClassDeclaration
//): TypeSpec.Builder {
//    val cBuilder = FunSpec.constructorBuilder()
//    classDeclaration.primaryConstructor?.parameters?.forEach { parameter ->
//
//        ParameterSpec.builder("ajd", String::class).build()
//        cBuilder.addParameter(parameter.name.toString())
//    }
//    val t = classDeclaration.primaryConstructor?.parameters?.first()
//    t?.
//    classDeclaration.getAllProperties().forEach { prop ->
//        cBuilder.addParameter(prop.simpleName.toString(),)
//    }
//    this.primaryConstructor(
//        FunSpec.constructorBuilder()
//
//            .build()
//    )
//
//    return this
//}