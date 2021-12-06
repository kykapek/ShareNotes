package com.example.sharenotes.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sharenotes.Crime

@Database(entities = [Crime::class ], version = 2)
@TypeConverters(CrimeTypeConverts::class)
abstract class CrimeDatabase : RoomDatabase() {

    abstract fun crimeDAO(): CrimeDAO

}

val migration_1_2 = object : Migration(1, 2) {  //из какой в какую идет миграция
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''"
        )
    }
}