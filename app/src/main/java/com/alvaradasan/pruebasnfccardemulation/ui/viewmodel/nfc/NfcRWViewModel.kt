package com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.nfc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alvaradasan.pruebasnfccardemulation.manager.NfcManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class NfcRWViewModelFactory(private val nfcManager: NfcManager?): ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = NfcRWViewModel(nfcManager) as T
}

class NfcRWViewModel (
    private val nfcManager: NfcManager?
) : ViewModel() {
    fun stopRead() {
        viewModelScope.launch { nfcManager?.manageStopRead() }
    }

    fun writeNdefData(data: String, onDone: (String) -> Unit) {
        nfcManager?.requestedRead?.set(true)
        viewModelScope.launch {
            val res = nfcManager?.writeNdefData(data)
            res?.let { onDone(res) }
            manageStopReadAfterOperation()
        }
    }

    private suspend fun manageStopReadAfterOperation() {
        nfcManager?.requestedRead?.set(false)
        delay(5000)
        nfcManager?.let { if(!it.requestedRead.get()) it.manageStopRead() }
    }
}