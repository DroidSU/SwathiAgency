package com.sujoy.swathiagency.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sujoy.swathiagency.data.dbModels.FileObjectModels
import com.sujoy.swathiagency.utilities.Constants

@Database(entities = [FileObjectModels::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "swathi_agency_database"
                )
                    .addMigrations(MIGRATION_1_2) // Add your migrations here
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Define your migrations here if needed
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Perform your migration here
                database.execSQL("ALTER TABLE ${Constants.TABLE_FILE_OBJECTS} ADD COLUMN ${Constants.COMPANY_NAME} TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}