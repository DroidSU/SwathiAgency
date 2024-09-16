package com.sujoy.swathiagency.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.CustomerOrderModel
import com.sujoy.swathiagency.data.datamodels.ItemsModel
import com.sujoy.swathiagency.data.datamodels.OrderFileModel
import com.sujoy.swathiagency.utilities.Constants

@Database(
    entities = [OrderFileModel::class, CustomerOrderModel::class, CustomerModel::class, ItemsModel::class],
    version = 7,
    exportSchema = false
)
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
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Perform your migration here
                database.execSQL("ALTER TABLE ${Constants.TABLE_ORDERS} ADD COLUMN ${Constants.COMPANY_NAME} TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
        CREATE TABLE IF NOT EXISTS ${Constants.TABLE_CUSTOMERS} (
                            ${Constants.CUSTOMER_ID} INTEGER PRIMARY KEY NOT NULL,
        ${Constants.CUSTOMER_NAME} TEXT NOT NULL,
        ${Constants.CUSTOMER_ROUTE} TEXT NOT NULL,
        ${Constants.CUSTOMER_AMOUNT} FLOAT NOT NULL
    )
    """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ${Constants.TABLE_ITEMS} ADD COLUMN ${Constants.COMPANY_NAME}")
            }
        }
    }
}