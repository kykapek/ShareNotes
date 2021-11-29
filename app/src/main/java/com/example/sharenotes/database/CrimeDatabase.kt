package com.example.sharenotes.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sharenotes.Crime

@Database(entities = [Crime::class ], version = 1)
@TypeConverters(CrimeTypeConverts::class)
abstract class CrimeDatabase : RoomDatabase() {

    abstract fun crimeDAO(): CrimeDAO

}