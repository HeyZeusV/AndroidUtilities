package com.heyzeusv.androidutilities.room.csv

import com.heyzeusv.androidutilities.room.EntityData
import com.heyzeusv.androidutilities.room.util.addIndented
import com.heyzeusv.androidutilities.room.util.getDataName
import com.heyzeusv.androidutilities.room.util.removeKotlinPrefix
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.buildCodeBlock

internal fun importCsvToRoomFunSpec(
    roomDataClassName: ClassName,
    entityDataList: List<EntityData>,
): FunSpec.Builder {
    val selectedDirectoryUri = "selectedDirectoryUri"

    val funSpec = FunSpec.builder("importCsvToRoom")
        .addAnnotation(AnnotationSpec.builder(Suppress::class)
            .addMember("%S", "UNCHECKED_CAST")
            .build()
        )
        .addParameter(selectedDirectoryUri, uriClassName)
        .returns(roomDataClassName.copy(nullable = true))
        .addCode(buildCodeBlock {
            add("""
                val selectedDirectory = DocumentFile.fromTreeUri(context, $selectedDirectoryUri)!!
                if (!selectedDirectory.exists()) {
                  // selected directory does no exist
                  return null
                }
                val csvDocumentFiles = mutableListOf<DocumentFile>()
                csvFileNames.forEach {
                  val file = selectedDirectory.findFile(it)
                  if (file == null) {
                    // file was not found
                    return null
                  } else {
                    csvDocumentFiles.add(file)
                  }
                }
                
            """.trimIndent())
            entityDataList.forEachIndexed { i, entityData ->
                val utilName = entityData.utilClassName.getDataName()
                addStatement("")
                addStatement("val %L = importCsvToRoomEntity(csvDocumentFiles[%L])", utilName, i)
                addStatement("if (%L == null) return null // error importing data", utilName)
            }
            addStatement("")
            addStatement("return RoomData(")
            entityDataList.forEach { data ->
                val utilName = data.utilClassName.getDataName()
                val dataName = utilName.replace("RoomUtil", "")
                addStatement(
                    format = "  %L = (%L as List<%L>).map { it.toOriginal() },",
                    args = arrayOf(dataName, utilName, data.utilClassName.simpleName),
                )
            }
            addStatement(")")
        })

    return funSpec
}

internal fun importCsvToRoomEntityFunSpec(
    entityDataList: List<EntityData>,
): FunSpec.Builder {
    val csvFile = "csvFile"
    val csvReaderMemberName = MemberName("com.github.doyaaaaaken.kotlincsv.dsl", "csvReader")

    val funSpec = FunSpec.builder("importCsvToRoomEntity")
        .addParameter(csvFile, documentFileClassName)
        .returns(csvDataListClassName.copy(nullable = true))
        .addCode(buildCodeBlock {
            add("""
                val inputStream = context.contentResolver.openInputStream($csvFile.uri)
                  ?: return null // corrupt file
                try {
                  
            """.trimIndent())
            addStatement("val content = %M().readAll(inputStream)", csvReaderMemberName)
            addIndented {
                add("""
                  if (content.size == 1) {
                    return emptyList()
                  }
                  
                  val header = content[0]
                  val rows = content.drop(1)
                  val entityData = mutableListOf<CsvData>()
                  when (header) {
                  
                """.trimIndent())
                addIndented {
                    entityDataList.forEach { entityData ->
                        add("""
                        %T.csvHeader -> {
                          rows.forEach {
                            val entry = %T(
                            
                        """.trimIndent(), entityData.utilClassName, entityData.utilClassName)
                        addIndented {
                            addIndented {
                                entityData.fieldInfoList.forEachIndexed { index, info ->
                                    val cast = getTypeCast(info.endType)
                                    add("  %L = it[%L]%L,\n", info.fieldName, index, cast)
                                }
                            }
                        }
                        add("""
                            )
                            entityData.add(entry)
                          }
                        }
                  
                        """.trimIndent())
                    }
                }
            }
            add("""
                  }
                  return entityData
                } catch (e: Exception) {
                  return null // invalid data, wrong type data
                }
            """.trimIndent())
        })

    return funSpec
}

private fun getTypeCast(type: TypeName): String {
    val cast = when (type.removeKotlinPrefix()) {
        "Boolean" -> ".toBoolean()"
        "Boolean?" -> ".toBoolean()"
        "Short" -> ".toShort()"
        "Short?" -> ".toShortOrNull()"
        "Int" -> ".toInt()"
        "Int?" -> ".toIntOrNull()"
        "Long" -> ".toLong()"
        "Long?" -> ".toLongOrNull()"
        "Byte" -> ".toByte()"
        "Byte?" -> ".toByteOrNull()"
        "Char" -> ".single()"
        "Char?" -> ".singleOrNull()"
        "Double" -> ".toDouble()"
        "Double?" -> ".toDoubleOrNull()"
        "Float" -> ".toFloat()"
        "Float?" -> ".toFloatOrNull()"
        "ByteArray" -> ".toByteArray()"
        "ByteArray?" -> ".toByteArray()"
        else -> ""
    }
    return cast
}