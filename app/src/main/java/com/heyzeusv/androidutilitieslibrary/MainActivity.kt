package com.heyzeusv.androidutilitieslibrary

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.heyzeusv.androidutilitieslibrary.database.Database
import com.heyzeusv.androidutilitieslibrary.feature.AnnotatedStringScreen
import com.heyzeusv.androidutilitieslibrary.feature.AppAboutScreen
import com.heyzeusv.androidutilitieslibrary.feature.AppAboutScreenNoBackOrIcon
import com.heyzeusv.androidutilitieslibrary.feature.AppAboutScreenNoIcon
import com.heyzeusv.androidutilitieslibrary.feature.ComposableResources
import com.heyzeusv.androidutilitieslibrary.feature.RoomCsvScreen
import com.heyzeusv.androidutilitieslibrary.ui.theme.AndroidUtilitiesLibraryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            Database::class.java,
            "Database.db"
        )
//            .createFromAsset("InitDatabase.db")
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL("INSERT INTO DefaultItemFts(DefaultItemFts) VALUES ('rebuild')")
                }
            })
            .fallbackToDestructiveMigration()
            .build()

        setContent {
            AndroidUtilitiesLibraryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(db)
                }
            }
        }
    }
}

@Composable
fun AppNavHost(db: Database) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screens.Overview,
    ) {
        composable<Screens.Overview> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(onClick = { navController.navigate(Screens.About) }) {
                    Text(text = "About Screen")
                }
                Button(onClick = { navController.navigate(Screens.AboutNoIcon) }) {
                    Text(text = "About Screen Without Icon")
                }
                Button(onClick = { navController.navigate(Screens.AboutNoBackOrIcon) }) {
                    Text(text = "About Screen Without Icon or Back")
                }
                Button(onClick = { navController.navigate(Screens.AnnotatedString) }) {
                    Text(text = "Annotated String")
                }
                Button(onClick = { navController.navigate(Screens.ComposableResources) }) {
                    Text(text = "Composable Resources")
                }
                Button(onClick = { navController.navigate(Screens.RoomCsv) }) {
                    Text(text = "Room Csv")
                }
            }
        }
        composable<Screens.About> {
            AppAboutScreen { navController.navigateUp() }
        }
        composable<Screens.AboutNoIcon> {
            AppAboutScreenNoIcon { navController.navigateUp() }
        }
        composable<Screens.AboutNoBackOrIcon> {
            AppAboutScreenNoBackOrIcon()
        }
        composable<Screens.AnnotatedString> {
            AnnotatedStringScreen()
        }
        composable<Screens.ComposableResources> {
            ComposableResources()
        }
        composable<Screens.RoomCsv> {
            RoomCsvScreen(db)
        }
    }
}