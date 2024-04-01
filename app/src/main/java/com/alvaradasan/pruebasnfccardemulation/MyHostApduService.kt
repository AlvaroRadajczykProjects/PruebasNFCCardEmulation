package com.alvaradasan.pruebasnfccardemulation

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

class MyHostApduService : HostApduService() {
    companion object {
        var sendingContent : String = ""
    }

    override fun onCreate() {
        super.onCreate()
        println("Se crea el servicio MyHostApduService")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("Se destruye el servicio MyHostApduService")
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        println("LLEGA: ${Utils.toHex(commandApdu)}")

        return sendingContent.toByteArray()

        //COMANDOS ESPECÍFICOS DEL PROTOCOLO APDU, NO ES NECESARIO UTILIZARLOS PARA UNA PRUEBA SIMPLE
        /*if (commandApdu == null) return Utils.hexStringToByteArray(Utils.STATUS_FAILED)

        val hexCommandApdu = Utils.toHex(commandApdu)
        if (hexCommandApdu.length < Utils.MIN_APDU_LENGTH) return Utils.hexStringToByteArray(Utils.STATUS_FAILED)

        if (hexCommandApdu.substring(0, 2) != Utils.DEFAULT_CLA) return Utils.hexStringToByteArray(Utils.CLA_NOT_SUPPORTED)

        if (hexCommandApdu.substring(2, 4) != Utils.SELECT_INS) return Utils.hexStringToByteArray(Utils.INS_NOT_SUPPORTED)

        return if (hexCommandApdu.substring(10, 24) == Utils.AID)  Utils.hexStringToByteArray(Utils.STATUS_SUCCESS)
        else Utils.hexStringToByteArray(Utils.STATUS_FAILED)*/
    }

    override fun onDeactivated(reason: Int) {
        Log.d(Utils.TAG, "Deactivated: $reason")
    }
}