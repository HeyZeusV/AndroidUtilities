package com.heyzeusv.androidutilities.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec

class RoomProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val logger = environment.logger

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // get all symbols with Entity
        val symbols = resolver.getSymbolsWithAnnotation(Entity::class.qualifiedName.orEmpty())
            .plus(resolver.getSymbolsWithAnnotation(ColumnInfo::class.qualifiedName.orEmpty()))
            .toSet()

        // filter out symbols that are not classes
        symbols.filterIsInstance<KSClassDeclaration>().forEach { symbol ->
            (symbol as? KSClassDeclaration)?.let { classDeclaration ->
                val packageName = classDeclaration.packageName.toString()
                val className = classDeclaration.simpleName.toString()
                val fileName = "${className}Impl"

                logger.info("class name: ${classDeclaration.simpleName}")
                classDeclaration.primaryConstructor?.parameters?.forEach { parameter ->
                    logger.info("parameter name: ${parameter.name}, type: ${parameter.type}")
                }
                classDeclaration.getAllProperties().forEach { prop ->
                    logger.info("property name: ${prop.simpleName}, type: ${prop.type}")
                }

//                // use KotlinPoet for code generation
//                val fileSpecBuilder = FileSpec.builder(packageName, fileName)
//
//                // create copy of class
//                val classBuilder = TypeSpec.classBuilder(className)
//                    .addModifiers(KModifier.DATA)
//                    .primaryConstructor(classDeclaration)
//
//                fileSpecBuilder.addType(classBuilder.build())

//                // writing the file
//                environment.codeGenerator.createNewFile(
//                    dependencies = Dependencies(false, symbol.containingFile!!),
//                    packageName = packageName,
//                    fileName = fileName,
//                    extensionName = "kt",
//                ).bufferedWriter().use {
//                    fileSpecBuilder.build().writeTo(it)
//                }
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