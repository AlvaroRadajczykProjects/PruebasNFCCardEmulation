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

/**
 * Clase de datos con algunas variables utilizadas en la UI NfcMainView
 *
 * @property dialogText Cadena de texto con el contenido que se muestra cuando el diálogo flotante aparece en pantalla (normalmente cuando se va a realizar
 * alguna operación, para avisar de que se debe de activar NFC acercase una etiqueta NFC, o anunciar el resultado de una operación o su resultado)
 * @constructor Create [NfcMainViewModelUiState]
 */
data class NfcMainViewModelUiState(
    val dialogText : String = "a",
)

/**
 * Nfc Main View Model Factory. Utilizado para poder crear un ViewModel con NfcMainViewModel pasando parámetros
 *
 * @property nfcManager Instancia de un gestor de lectura y comuncación con etiquetas NFC
 * @constructor Create [NfcMainViewModelFactory]
 */
class NfcMainViewModelFactory(private val nfcManager: NfcManager?): ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = NfcMainViewModel(nfcManager) as T
}

/**
 * ViewModel de la UI NfcMainView que maneja métodos del NfcManager que tiene como argumento, para controlar cuando
 * iniciar una búsqueda, obtener información acerca de la etiqueta NFC, peraparar una etiqueta pra que pueda soportar
 * NDef, y cuándo detener la conexión
 *
 * @property nfcManager Instancia de un gestor de lectura y comuncación con etiquetas NFC
 * @constructor Create [NfcMainViewModel]
 */
class NfcMainViewModel(
    private val nfcManager: NfcManager?
) : ViewModel() {
    /** StatFlow que se utiliza para gestionar variables reflejadas en la UI */
    private val _uiState = MutableStateFlow(NfcMainViewModelUiState())
    val uiState = _uiState.asStateFlow()
    
    /**
     * Utilizado para modificar los valores de los atributos de NfcMainViewModelUiStatey que se vea reflejado en la UI
     *
     * @param updating Función lambda a cuyo parámetro se debería llamar al método copy
     */
    fun updateUiState(updating: (NfcMainViewModelUiState) -> NfcMainViewModelUiState) {
        _uiState.update(updating)
    }

    /**
     * Se encarga de detener la búsqueda de una etiqueta NFC
     */
    fun stopRead() {
        viewModelScope.launch { nfcManager?.manageStopRead() }
    }

    /**
     * Se encarga de iniciar la búsqueda de una etiqueta NFC y de obtener información acerca de la misma
     *
     * @param onReceive Función lambda llamada cuando la etiqueta NFC devuelve un resultado, con el resultado en una cadena de texto pasado como
     * argumento
     */
    fun getInfoResult(onReceive: (String) -> Unit) {
        nfcManager?.requestedRead?.set(true)
        viewModelScope.launch {
            val res = nfcManager?.getInfoResult()
            res?.let { onReceive(it) }
            manageStopReadAfterOperation()
        }
    }

    /**
     * Se encarga de iniciar la búsqueda de una etiqueta NFC que soporte el formato NDef y de obtener el valor del primer registro de texto que
     * se encuentre
     *
     * @param onReceive Función lambda llamada cuando la etiqueta NFC (que soporte NDef) devuelve el contenido del primero registro de texto que tiene,
     * que se pasa como argumento
     */
    fun readNdefData(onReceive: (String) -> Unit) {
        nfcManager?.requestedRead?.set(true)
        viewModelScope.launch {
            val res = nfcManager?.readNdefData()
            res?.let { onReceive(it) }
            manageStopReadAfterOperation()
        }
    }

    /**
     * Se encarga de iniciar la búsqueda de una etiqueta NFC que soporte el formato NdefFormatable (que pueda soportar Ndef pero necesite de un proceso
     * por el cual se adapta el contenido de la etiqueta para que lo pueda soportar) y de, en caso de soportarlo, formatearla para que pueda soportar
     * el formato Ndef. Añade un registro vinculado a esta aplicación (con NFC activado, al acercar la etiqueta a este dispositivo, se abrirá automáticamente
     * esta aplicación aunque no esté abierta) y un registro de texto, vacío
     *
     * @param onReceive Función lambda llamada cuando se realiza el formateo de la etiqueta, con el resultado del proceso en una cadena de texto pasada como argumento
     */
    fun formatToNdef(onReceive : (String) -> Unit) {
        nfcManager?.requestedRead?.set(true)
        viewModelScope.launch {
            val res = nfcManager?.formatNdef()
            res?.let { onReceive(it) }
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