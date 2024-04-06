package com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.nfc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alvaradasan.pruebasnfccardemulation.manager.NfcManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Nfc Rw View Model Factory. Utilizado para poder crear un ViewModel con NfcMainViewModel pasando parámetros
 *
 * @property nfcManager Instancia de un gestor de lectura y comuncación con etiquetas NFC
 * @constructor Create [NfcRWViewModelFactory]
 */
class NfcRWViewModelFactory(private val nfcManager: NfcManager?): ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = NfcRWViewModel(nfcManager) as T
}

/**
 * ViewModel de la UI NfcMainView que maneja métodos del NfcManager que tiene como argumento, para controlar cuando
 * iniciar una búsqueda, sobrescribir la información (si soporta NDef) que contiene, y cuándo detener la conexión
 *
 * @property nfcManager Instancia de un gestor de lectura y comuncación con etiquetas NFC
 * @constructor Create [NfcRWViewModel]
 */
class NfcRWViewModel (
    private val nfcManager: NfcManager?
) : ViewModel() {
    /**
     * Se encarga de detener la búsqueda de una etiqueta NFC
     */
    fun stopRead() {
        viewModelScope.launch { nfcManager?.manageStopRead() }
    }

    /**
     * Se encarga de iniciar la búsqueda de una etiqueta NFC que soporte el formato NDef y de modificar el valor del primer registro de texto que
     * se encuentre, por el valor de data pasado
     *
     * @param data Cadena de texto con el contenido que se quiere escribir en la etiqueta NFC detectada
     * @param onDone Función lambda llamada cuando la etiqueta NFC (que soporte NDef) sobreescribe el valor del registro, pasando como argumento
     * una cadena de texto que representa el resultado de la acción llevada a cabo
     */
    fun writeNdefData(data: String, onDone: (String) -> Unit) {
        nfcManager?.requestedRead?.set(true)
        viewModelScope.launch {
            val res = nfcManager?.writeNdefData(data)
            res?.let { onDone(res) }
            manageStopReadAfterOperation()
        }
    }

    /**
     * Método que se encarga de mantener la lectura unos 5 segundos tras haberse producido una comunicación exitosa, para
     * que este dispositivo no reconozca la etiqueta o tarjeta actualmente reconocida y la gestione con otra aplicación al
     * momento
     */
    private suspend fun manageStopReadAfterOperation() {
        nfcManager?.requestedRead?.set(false)
        delay(5000)
        nfcManager?.let { if(!it.requestedRead.get()) it.manageStopRead() }
    }
}