package com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.nfc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alvaradasan.pruebasnfccardemulation.manager.NfcManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NfcMainViewModelUiState(
    val dialogText : String = "a",
)

class NfcMainViewModelFactory(private val nfcManager: NfcManager?): ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = NfcMainViewModel(nfcManager) as T
}

class NfcMainViewModel(
    private val nfcManager: NfcManager?
) : ViewModel() {
    private val _uiState = MutableStateFlow(NfcMainViewModelUiState())
    val uiState = _uiState.asStateFlow()
    
    /**
     * Update Ui State.
     *
     * @param updating Lambda function which it param may call copy() method
     */
    fun updateUiState(updating: (NfcMainViewModelUiState) -> NfcMainViewModelUiState) {
        _uiState.update(updating)
    }

    fun stopRead() {
        viewModelScope.launch { nfcManager?.manageStopRead() }
    }

    fun getInfoResult(onReceive: (String) -> Unit) {
        nfcManager?.requestedRead?.set(true)
        viewModelScope.launch {
            val res = nfcManager?.getInfoResult()
            res?.let { onReceive(it) }
            manageStopReadAfterOperation()
        }
    }

    fun readNdefData(onReceive: (String) -> Unit) {
        nfcManager?.requestedRead?.set(true)
        viewModelScope.launch {
            val res = nfcManager?.readNdefData()
            res?.let { onReceive(it) }
            manageStopReadAfterOperation()
        }
    }

    fun formatToNdef(onReceive : (String) -> Unit) {
        nfcManager?.requestedRead?.set(true)
        viewModelScope.launch {
            val res = nfcManager?.formatNdef()
            res?.let { onReceive(it) }
            manageStopReadAfterOperation()
        }
    }

    private suspend fun manageStopReadAfterOperation() {
        nfcManager?.requestedRead?.set(false)
        delay(5000)
        nfcManager?.let { if(!it.requestedRead.get()) it.manageStopRead() }
    }

}