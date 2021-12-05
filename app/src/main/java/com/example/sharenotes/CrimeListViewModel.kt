package com.example.sharenotes

import androidx.lifecycle.ViewModel
import com.example.sharenotes.repository.CrimeRepository

class CrimeListViewModel : ViewModel() {

    /*
    val crimes = mutableListOf<CrimeData>()

    //macs

    init {
        for(i in 0 until 100){
            val crime = CrimeData()
            crime.title = "Crime #$i"
            crime.isSolved = i % 2 == 0
            crimes += crime
        }
    }

     */
    private val crimeRepository = CrimeRepository.get()
    val crimesListLiveData = crimeRepository.getCrimes()

    fun addCrime(crime: Crime) {
        crimeRepository.addCrime(crime)
    }

}