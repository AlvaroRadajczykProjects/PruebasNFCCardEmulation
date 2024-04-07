package com.alvaradasan.pruebasnfccardemulation.manager

import android.app.Activity
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import android.os.Bundle
import android.util.Log
import com.alvaradasan.pruebasnfccardemulation.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class NfcManager(activityFlow : Flow<Activity>, adapter : NfcAdapter, requestedRead: AtomicBoolean) {
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
     * Callback que se encarga de gestionar la obtención de información de una etiqueta NFC
     */
    private val getInfoCallback = NfcAdapter.ReaderCallback { tag ->
        var res = "ID -> ${Utils.toHex(tag.id)};Technologies -> ${tag.techList.contentToString()};"
        tag.techList.forEach { tech ->
            if(tech == "android.nfc.tech.NfcA") {
                val nfca = NfcA.get(tag)
                res += "NfcA -> [SAK: ${nfca.sak}, ATQA: ${Utils.toHex(nfca.atqa)}, Timeout: ${nfca.timeout}, MaxTransceiveLength: ${nfca.maxTransceiveLength}];"
            }
            if(tech == "android.nfc.tech.MifareClassic") {
                val mf = MifareClassic.get(tag)
                res += "Mifare -> [Type: ${mf.type}, Size: ${mf.size}, Timeout: ${mf.timeout}, MaxTransceiveLength: ${mf.maxTransceiveLength}, " +
                        "BlockCount: ${mf.blockCount}, SectorCount: ${mf.sectorCount}];"
            }
            if(tech == "android.nfc.tech.Ndef") {
                val ndef = Ndef.get(tag)
                res += "Ndef -> [Type: ${ndef.type}, IsWritable: ${ndef.isWritable}, MaxSize: ${ndef.maxSize}];"
            }
            if(tech == "android.nfc.tech.NdefFormatable") {
                res += "NdefFormatable -> [Este formato no contiene informacion];"
            }
            if(tech == "android.nfc.tech.IsoDep") {
                val idp = IsoDep.get(tag)
                res += "IsoDep -> [Timeout: ${idp.timeout}, MaxTransceiveLength: ${idp.maxTransceiveLength}, " +
                        "IsExtendedLengthApduSupported: ${idp.isExtendedLengthApduSupported}"
                if(idp.historicalBytes != null) res += ", Historical bytes: ${Utils.toHex(idp.historicalBytes)}"
                if(idp.hiLayerResponse != null) res += ", HiLayerResponse: ${
                    com.alvaradasan.pruebasnfccardemulation.Utils.toHex(idp.hiLayerResponse)}"
                res += "];"
            }
        }
        coroutineScope.launch {
            resultStream.send(res)
        }
    }

    /**
     * Callback que se encarga de leer el contenido del primer registro de tipo texto que se encuentra en la etiqueta NFC (que soporte NDef)
     */
    private val readNdefCallback = NfcAdapter.ReaderCallback { tag ->
        coroutineScope.launch {
            runCatching {
                if(tag.techList.contains("android.nfc.tech.Ndef")) {
                    val ndef = Ndef.get(tag)
                    if (!ndef.isConnected) ndef.connect()
                    if (ndef.isConnected) {
                        runCatching {
                            ndef.cachedNdefMessage?.let { cachedMessage ->
                                val data = cachedMessage.records[1].payload
                                resultStream.send(String(data))
                            } ?: run {
                                resultStream.send(ERROR)
                            }
                        }.onFailure {
                            it.printStackTrace()
                            resultStream.send(ERROR)
                        }
                    }
                    if (ndef.isConnected) ndef.close()
                } else {
                    Log.e("NfcManager", "readNdefCallback -> No tiene Ndef")
                    resultStream.send(ERROR)
                }
            }.onFailure {
                it.printStackTrace()
                resultStream.send(ERROR)
            }
        }
    }

    /**
     * Callback que se utiliza para gestionar la sobreescritura del contenido del primer registro de la etiqueta NFC encontrada (que soporte NDef)
     */
    private val writeNdefCallback = NfcAdapter.ReaderCallback { tag ->
        coroutineScope.launch {
            runCatching {
                if(tag.techList.contains("android.nfc.tech.Ndef")) {
                    val ndef = Ndef.get(tag)
                    if (!ndef.isConnected) ndef.connect()
                    if (ndef.isConnected) {
                        runCatching {
                            ndef.cachedNdefMessage?.let { cachedMessage ->
                                val textRecord = NdefRecord(
                                    NdefRecord.TNF_WELL_KNOWN,
                                    NdefRecord.RTD_TEXT,
                                    byteArrayOf(),
                                    writeDataStream.receive().toByteArray()
                                )
                                if (cachedMessage.records.size > 1) {
                                    cachedMessage.records[1] = textRecord
                                    val ndefMessage =
                                        NdefMessage(cachedMessage.records)
                                    ndef.writeNdefMessage(ndefMessage)
                                }
                                resultStream.send("Success")
                            } ?: run {
                                resultStream.send(ERROR)
                            }
                        }.onFailure { resultStream.send(ERROR) }
                    }
                    if (ndef.isConnected) ndef.close()
                } else resultStream.send(ERROR)
            }.onFailure { resultStream.send(ERROR) }
        }
    }

    /**
     * Callback que se encarga de gestionar el proceso de formateo de una etiqueta que soporte NdefFormatable, para que pueda soprtar Ndef
     */
    private val formatToNdefCallback = NfcAdapter.ReaderCallback { tag ->
        coroutineScope.launch {
            runCatching {
                if(tag.techList.contains("android.nfc.tech.NdefFormatable")) {
                    val ndef = NdefFormatable.get(tag)
                    if (!ndef.isConnected) ndef.connect()
                    if (ndef.isConnected) {
                        runCatching {
                            val records = arrayOf(
                                NdefRecord.createApplicationRecord("com.alvaradasan.pruebasnfccardemulation"),
                                NdefRecord(
                                    NdefRecord.TNF_WELL_KNOWN,
                                    NdefRecord.RTD_TEXT,
                                    byteArrayOf(),
                                    byteArrayOf()
                                )
                            )
                            val ndefMessage = NdefMessage(records)
                            ndef.format(ndefMessage)
                            resultStream.send("Formateado realizado correctamente")
                        }.onFailure { resultStream.send(ERROR) }
                    }
                    if (ndef.isConnected) ndef.close()
                } else resultStream.send(ERROR)
            }.onFailure { resultStream.send(ERROR) }
        }
    }

    /**
     * Método que inicia la lectura de la información de una etiqueta NFC y devuelve el resultado del proceso
     *
     * @return [String] resultado del proceso, que será ERROR si falla, y cualquier otro en caso contrario
     */
    suspend fun getInfoResult() : String {
        manageStartRead(getInfoCallback)
        return resultStream.receive()
    }

    /**
     * Método que inicia la lectura del primer registro de texto de una etiqueta NFC que soporte NDef, y devuelve su valor
     *
     * @return [String] datos que devuelve la etiqueta NFC con la cual se ha comunicado
     */
    suspend fun readNdefData() : String {
        manageStartRead(readNdefCallback)
        return resultStream.receive()
    }

    /**
     * Método que inicia sobreescritura del contenido del primer regiestro de texto de una etiqueta NFC que soporte NDef,
     * y devuelve el resultado de la ejecución
     *
     * @param data
     * @return [String] resultado del proceso, que será ERROR si falla, y cualquier otro en caso contrario
     */
    suspend fun writeNdefData(data: String): String {
        writeDataStream.send(data)
        manageStartRead(writeNdefCallback)
        return resultStream.receive()
    }

    /**
     * Format Ndef.
     *
     * @return [String] resultado del proceso, que será ERROR si falla, y cualquier otro en caso contrario
     */
    suspend fun formatNdef() : String {
        manageStartRead(formatToNdefCallback)
        return resultStream.receive()
    }

    /**
     * Inicia la búsqueda de una etiqueta NFC, y en caso de encontrarla, llama a un callback pasado como argumento de tipo [NfcAdapter.ReaderCallback]
     *
     * @param callback Callback llamado en el caso de encontrar una etiqueta NFC
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
                            NfcAdapter.FLAG_READER_NFC_B or
                            NfcAdapter.FLAG_READER_NFC_F or
                            NfcAdapter.FLAG_READER_NFC_V or
                            NfcAdapter.FLAG_READER_NFC_BARCODE or
                            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
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
     * Detiene la búsqueda de una etiqueta NFC
     */
    suspend fun manageStopRead() {
        activityFlow.collect {
            println("Stopped reading (NFC antenna)")
            adapter.disableReaderMode(it)
        }
    }
}