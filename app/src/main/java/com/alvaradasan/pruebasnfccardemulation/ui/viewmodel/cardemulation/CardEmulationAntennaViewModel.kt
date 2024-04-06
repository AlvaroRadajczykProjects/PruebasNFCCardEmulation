package com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.cardemulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alvaradasan.pruebasnfccardemulation.manager.CardEmulationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Card Emulation Antenna View Model Factory. Utilizado para poder crear un ViewModel con CardEmulationAntennaViewModel pasando parámetros
 *
 * @property cardEmulationManager Instancia de un gestor de lectura y comuncación con tarjetas que soportan APDU
 * @constructor Create [CardEmulationAntennaViewModelFactory]
 */
class CardEmulationAntennaViewModelFactory(private val cardEmulationManager : CardEmulationManager?): ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = CardEmulationAntennaViewModel(cardEmulationManager) as T
}

/**
 * ViewModel de la UI CardEmulationAntennaView que maneja métodos del CardEmulationManager que tiene como argumento, para controlar cuando
 * iniciar una búsqueda, cómo gestionarla para enviar un comando en formato APDU y recibir una respuesta, y para controlar su detención
 *
 * @property cardEmulationManager Instancia de un gestor de lectura y comuncación con tarjetas que soportan APDU
 * @constructor Create [CardEmulationAntennaViewModel]
 */
class CardEmulationAntennaViewModel(
    private val cardEmulationManager: CardEmulationManager?
) : ViewModel() {
    /**
     * Se encarga de detener la búsqueda de un dispositivo que soporte APDU
     */
    fun stopRead() {
        viewModelScope.launch { cardEmulationManager?.manageStopRead() }
    }

    /**
     * Se encarga de inciar la búsqueda de un dispositivo que soporte APDU y de pasar el resultado recibido de la tarjeta tras
     * enviar un comando básico, que se puede encontrar en [CardEmulationManager]
     *
     * @param onEstablished Función lambda llamada con el resultado de la respuesta de la tarjeta, cuando esta responde a esta dispositivo
     */
    fun establishConnection(onEstablished: (String) -> Unit ) {
        viewModelScope.launch {
            val result = cardEmulationManager?.startCommunication()
            result?.let { onEstablished(it) }
            manageStopReadAfterOperation()
        }
    }

    /**
     * Método que se encarga de mantener la lectura unos 5 segundos tras haberse producido una comunicación exitosa, para
     * que este dispositivo no reconozca la etiqueta o tarjeta actualmente reconocida y la gestione con otra aplicación al
     * momento
     */
    private suspend fun manageStopReadAfterOperation() {
        cardEmulationManager?.requestedRead?.set(false)
        delay(5000)
        cardEmulationManager?.let { if(!it.requestedRead.get()) it.manageStopRead() }
    }
}