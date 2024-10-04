package com.heyzeusv.androidutitlities.room

import com.tschuchort.compiletesting.SourceFile

val dummyDb = SourceFile.kotlin(
    name = "TestDatabase.kt",
    contents = """
        package test

        import androidx.room.Database

        @Database
        abstract class TestDatabase
    """.trimIndent()
)