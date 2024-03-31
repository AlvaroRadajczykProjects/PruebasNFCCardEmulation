package com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.cardemulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alvaradasan.pruebasnfccardemulation.manager.CardEmulationManager
import com.alvaradasan.pruebasnfccardemulation.manager.NfcManager
import com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.nfc.NfcMainViewModel
import com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.nfc.NfcMainViewModelUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CardEmulationMainViewModelUiState(
    val num: Int = 0
)

class CardEmulationMainViewModelFactory(private val cardEmulationManager : CardEmulationManager?): ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = CardEmulationMainViewModel(cardEmulationManager) as T
}

class CardEmulationMainViewModel(
    private val cardEmulationManager: CardEmulationManager?
) : ViewModel() {
    private val _uiState = MutableStateFlow(CardEmulationMainViewModelUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Update Ui State.
     *
     * @param updating Lambda function which it param may call copy() method
     */
    fun updateUiState(updating: (CardEmulationMainViewModelUiState) -> CardEmulationMainViewModelUiState) {
        _uiState.update(updating)
    }

    fun stopRead() {
        viewModelScope.launch { cardEmulationManager?.manageStopRead() }
    }

    fun establishConnection( onEstablished: (String) -> Unit ) {
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