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

    suspend fun startCommunication() : String {
        manageStartRead(establishConnectionCallback)
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

    suspend fun manageStopRead() {
        activityFlow.collect {
            println("Stopped reading (NFC antenna)")
            adapter.disableReaderMode(it)
        }
    }

}