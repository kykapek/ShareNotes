package com.example.sharenotes.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.sharenotes.Crime
import com.example.sharenotes.database.CrimeDatabase
import java.lang.IllegalStateException
import java.util.*

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context) {

    private val database : CrimeDatabase = Room.databaseBuilder(  //реализация класса CrimeDatabase
            context.applicationContext,
            CrimeDatabase::class.java, //класс бд
            DATABASE_NAME  //имя файла бд
    ).build()

    private val crimeDAO = database.crimeDAO()

    fun getCrimes(): LiveData<List<Crime>> = crimeDAO.getCrimes()
    fun getCrime(id: UUID): LiveData<Crime?> = crimeDAO.getCrime(id)

    companion object{
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context) {
            if(INSTANCE == null){
                INSTANCE = CrimeRepository(context)
            }
        }

        fun get(): CrimeRepository {
            return INSTANCE ?:
            throw IllegalStateException("Repository must be initialized")
        }
    }

}