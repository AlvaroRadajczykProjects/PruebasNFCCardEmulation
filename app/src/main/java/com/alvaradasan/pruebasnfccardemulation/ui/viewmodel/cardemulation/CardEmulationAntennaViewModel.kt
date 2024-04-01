package com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.cardemulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alvaradasan.pruebasnfccardemulation.manager.CardEmulationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CardEmulationAntennaViewModelFactory(private val cardEmulationManager : CardEmulationManager?): ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = CardEmulationAntennaViewModel(cardEmulationManager) as T
}

class CardEmulationAntennaViewModel(
    private val cardEmulationManager: CardEmulationManager?
) : ViewModel() {
    fun stopRead() {
        viewModelScope.launch { cardEmulationManager?.manageStopRead() }
    }

    fun establishConnection(onEstablished: (String) -> Unit ) {
        viewModelScope.launch {
            val result = cardEmulationManager?.startCommunication()
            result?.let { onEstablished(it) }
            manageStopReadAfterOperation()
        }
    }

    private suspend fun manageStopReadAfterOperation() {
        cardEmulationManager?.requestedRead?.set(false)
        delay(5000)
        cardEmulationManager?.let { if(!it.requestedRead.get()) it.manageStopRead() }
    }
}