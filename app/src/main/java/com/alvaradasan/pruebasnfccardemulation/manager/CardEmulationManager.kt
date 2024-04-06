package com.alvaradasan.pruebasnfccardemulation.manager

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import android.os.Bundle
import com.alvaradasan.pruebasnfccardemulation.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class CardEmulationManager(activityFlow : Flow<Activity>, adapter : NfcAdapter, requestedRead: AtomicBoolean) {

    companion object {
        //Utilizado para saber cuándo una operación ha dado problemas o ha funcionado bien
        val ERROR = "ERROR"
    }

    /** Adaptador de antena que es capaz de leer etiquetas NFC */
    private val adapter : NfcAdapter
    /** Flujo para poder utilizar la actividad de esta aplicación */
    private val activityFlow : Flow<Activity>
    /** Booleano utilizado para no interferir en la gestión de lectura hecha por otros subprocesos */
    val requestedRead : AtomicBoolean

    /** Utilizado para gestión de subprocesos en segundo plano */
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    /** Utilizado para enviar resultados de acciones a otros subprocesos */
    private var resultStream: Channel<String>
    /** Utilizado apra enviar resultados de lectura a otros subprocesos */
    private var writeDataStream: Channel<String>

    init {
        this.adapter = adapter
        this.activityFlow = activityFlow
        this.resultStream = Channel(capacity = 10, onBufferOverflow = BufferOverflow.SUSPEND)
        this.writeDataStream = Channel(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        this.requestedRead = requestedRead
    }

    /**
     * Callback utilizado para gestionar el envío de un comando simple en formato APDU y obtener la respuesta que devuelve la etiqueta
     * NFC que soporta APDU
     */
    private val establishConnectionCallback = NfcAdapter.ReaderCallback { tag ->
        coroutineScope.launch {
            runCatching {
                if(tag.techList.contains("android.nfc.tech.IsoDep")) {
                    val isoDep = IsoDep.get(tag)
                    if(!isoDep.isConnected) isoDep.connect()

                    runCatching {
                        val response = isoDep.transceive(Utils.hexStringToByteArray("00A4040007A0000002471001"))
                        resultStream.send(String(response))
                    }.onFailure {
                        it.printStackTrace()
                        resultStream.send(NfcManager.ERROR)
                    }

                    if(isoDep.isConnected) isoDep.close()
                } else resultStream.send(NfcManager.ERROR)
            }.onFailure {
                it.printStackTrace()
                resultStream.send(NfcManager.ERROR)
            }
        }
    }

    /**
     * Inicia la búsqueda de una etiqueta NFC que soporte APDU, y en caso de encontrarlo, llama a un callback que se encarga de enviar un
     * simple comando en formato APDU, y devuelve la respuesta como cadena de texto
     *
     * @return [String]
     */
    suspend fun startCommunication() : String {
        manageStartRead(establishConnectionCallback)
        return resultStream.receive()
    }

    /**
     * Inicia la búsqueda de una etiqueta NFC que soporte APDU, y en caso de encontrarla, llama a un callback pasado como argumento de tipo [NfcAdapter.ReaderCallback]
     *
     * @param callback Callback llamado en el caso de encontrar una etiqueta NFC que soporte APDU
     */
    private suspend fun manageStartRead( callback: NfcAdapter.ReaderCallback ) {
        runCatching {
            activityFlow.collect {
                val options = Bundle()
                options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
                //adapter.enableForegroundDispatch()
                adapter.enableReaderMode(
                    it,
                    callback,
                    NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS or
                        NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    options
                )
            }
        }.onFailure {
            System.err.println("Error al iniciar lectura")
            it.printStackTrace()
        }.onSuccess {
            println("Lectura bien hecha")
        }
    }

    /**
     * Detiene la búsqueda de una etiqueta NFC que soporte APDU
     */
    suspend fun manageStopRead() {
        activityFlow.collect {
            println("Stopped reading (NFC antenna)")
            adapter.disableReaderMode(it)
        }
    }

}