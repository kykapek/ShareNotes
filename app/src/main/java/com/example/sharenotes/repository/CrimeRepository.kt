package com.example.sharenotes.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.sharenotes.Crime
import com.example.sharenotes.database.CrimeDatabase
import com.example.sharenotes.database.migration_1_2
import java.io.File
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context) {

    private val database : CrimeDatabase = Room.databaseBuilder(  //реализация класса CrimeDatabase
            context.applicationContext,
            CrimeDatabase::class.java, //класс бд
            DATABASE_NAME  //имя файла бд
    ).addMigrations(migration_1_2).build()

    private val crimeDAO = database.crimeDAO()
    private val executor = Executors.newSingleThreadExecutor() //исполнитель, который работает в фоновом потоке
    private val filesDir = context.applicationContext.filesDir

    fun updateCrime(crime: Crime) {
        executor.execute {  //в фоновый поток
            crimeDAO.updateCrime(crime)
        }
    }

    fun addCrime(crime: Crime) {
        executor.execute {  //в фоновый поток
            crimeDAO.addCrime(crime)
        }
    }

    fun getPhotoFile(crime: Crime): File = File(filesDir, crime.photoFileNAme)  //возвращает об=ты File, указывающие в нужные места

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