package com.mikhail_ryumkin_r.my_gps_assistant.mainViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mikhail_ryumkin_r.my_gps_assistant.dbRoom.MainDb
import com.mikhail_ryumkin_r.my_gps_assistant.dbRoom.TrackItemChet
import com.mikhail_ryumkin_r.my_gps_assistant.location.locationModel.LocationModel
import com.mikhail_ryumkin_r.my_gps_assistant.location.locationModel.LocationModelRecording
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class MainViewModel(db : MainDb) : ViewModel() {

    private val dao = db.getDao()
    val locationUpdates = MutableLiveData<LocationModel>()
    val locationUpdatesRecording = MutableLiveData<LocationModelRecording>()
    val currentTrack = MutableLiveData<TrackItemChet>()
    val tracks = dao.getAllTracks().asLiveData()

    suspend fun insertTrack(trackItem: TrackItemChet) = viewModelScope.launch {
        dao.insertTrack(trackItem)
    }

    fun deleteTrack(trackItem: TrackItemChet) = viewModelScope.launch {
        dao.deleteTrack(trackItem)
    }


    class ViewModelFactory(private val db: MainDb) : ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)){
                return MainViewModel(db) as T
            }
            throw IllegalArgumentException("Неизвестный ViewModelClass")
        }
    }
}