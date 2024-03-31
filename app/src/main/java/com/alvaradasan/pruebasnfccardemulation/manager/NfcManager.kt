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
        val ERROR = "ERROR"
    }

    private val adapter : NfcAdapter
    private val activityFlow : Flow<Activity>
    val requestedRead : AtomicBoolean

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var resultStream: Channel<String>
    private var writeDataStream: Channel<String>

    init {
        this.adapter = adapter
        this.activityFlow = activityFlow
        this.resultStream = Channel(capacity = 10, onBufferOverflow = BufferOverflow.SUSPEND)
        this.writeDataStream = Channel(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        this.requestedRead = requestedRead
    }

    fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

    private val getInfoCallback = NfcAdapter.ReaderCallback { tag ->
        var res = "ID -> ${tag.id.toHex()};Technologies -> ${tag.techList.contentToString()};"
        tag.techList.forEach { tech ->
            if(tech == "android.nfc.tech.NfcA") {
                val nfca = NfcA.get(tag)
                res += "NfcA -> [SAK: ${nfca.sak}, ATQA: ${nfca.atqa.toHex()}, Timeout: ${nfca.timeout}, MaxTransceiveLength: ${nfca.maxTransceiveLength}];"
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
                if(idp.historicalBytes != null) res += ", Historical bytes: ${idp.historicalBytes.toHex()}"
                if(idp.hiLayerResponse != null) res += ", HiLayerResponse: ${idp.hiLayerResponse.toHex()}"
                res += "];"
            }
        }
        coroutineScope.launch {
            resultStream.send(res)
        }
    }

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

    suspend fun getInfoResult() : String {
        manageStartRead(getInfoCallback)
        return resultStream.receive()
    }

    suspend fun readNdefData() : String {
        manageStartRead(readNdefCallback)
        return resultStream.receive()
    }

    suspend fun writeNdefData(data: String): String {
        writeDataStream.send(data)
        manageStartRead(writeNdefCallback)
        return resultStream.receive()
    }

    suspend fun formatNdef() : String {
        manageStartRead(formatToNdefCallback)
        return resultStream.receive()
    }

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

    suspend fun manageStopRead() {
        activityFlow.collect {
            println("Stopped reading (NFC antenna)")
            adapter.disableReaderMode(it)
        }
    }
}