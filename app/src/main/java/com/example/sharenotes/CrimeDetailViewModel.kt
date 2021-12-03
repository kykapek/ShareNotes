package com.example.sharenotes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.sharenotes.repository.CrimeRepository
import java.util.*

class CrimeDetailViewModel(): ViewModel() {

    private val crimeRepository = CrimeRepository.get() //связь с репозиторием
    private val crimeIdLiveData = MutableLiveData<UUID>()  //идентификатор новости

    var crimeLiveData: LiveData<Crime?> =
            Transformations.switchMap(crimeIdLiveData) { crimeId ->  //преобразование
                crimeRepository.getCrime(crimeId)
            }

    fun loadCrime(crimeId: UUID) {
        crimeIdLiveData.value = crimeId
    }

    fun saveCrime(crime: Crime) {
        crimeRepository.updateCrime(crime)
    }

}