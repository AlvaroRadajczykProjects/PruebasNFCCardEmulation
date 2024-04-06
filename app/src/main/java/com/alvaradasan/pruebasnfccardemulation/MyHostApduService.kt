package com.alvaradasan.pruebasnfccardemulation

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

/**
 * Servicio que hereda de [HostApduService] y que implementa los métodos necesarios para que el dispositivo que ejecute esta
 * aplicación pueda comportarse como una etiqueta NFC capaz de mantener una comunicación con el protocolo APDU
 *
 * @constructor Create empty constructor for MyHostApduService.
 */
class MyHostApduService : HostApduService() {
    companion object {
        /** Cadena de texto que se enviará como reespuesta cuando una antena NFC que soporte APDU intente comunicarse con este dispositivo */
        var sendingContent : String = ""
    }

    /**
     * Modificación de onCreate para mostrar un mensaje en consola cuando este servicio se haya creado.
     * Implementado para observar que este servicio se crea y se mantiene activo hasta que la comunicación entre
     * etiqueta y antena se finaliza
     */
    override fun onCreate() {
        super.onCreate()
        println("Se crea el servicio MyHostApduService")
    }

    /**
     * Modificación de onDestroy para mostrar un mensaje en consola cuando este servicio se haya destruido.
     * Implementado para observar que este servicio se crea y se mantiene activo hasta que la comunicación entre
     * etiqueta y antena se finaliza
     */
    override fun onDestroy() {
        super.onDestroy()
        println("Se destruye el servicio MyHostApduService")
    }

    /**
     * Process Command Apdu.
     *
     * @param commandApdu
     * @param extras
     * @return [Byte]
     */
    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        println("Mensaje en formmato APDU que llega de una antena NFC que soporta APDU: ${Utils.toHex(commandApdu)}")
        println("Se responde con este mensaje: $sendingContent")
        return sendingContent.toByteArray()

        //COMANDOS ESPECÍFICOS DEL PROTOCOLO APDU, NO UTILIZADOS EN ESTA RAMA
        /*if (commandApdu == null) return Utils.hexStringToByteArray(Utils.STATUS_FAILED)

        val hexCommandApdu = Utils.toHex(commandApdu)
        if (hexCommandApdu.length < Utils.MIN_APDU_LENGTH) return Utils.hexStringToByteArray(Utils.STATUS_FAILED)

        if (hexCommandApdu.substring(0, 2) != Utils.DEFAULT_CLA) return Utils.hexStringToByteArray(Utils.CLA_NOT_SUPPORTED)

        if (hexCommandApdu.substring(2, 4) != Utils.SELECT_INS) return Utils.hexStringToByteArray(Utils.INS_NOT_SUPPORTED)

        return if (hexCommandApdu.substring(10, 24) == Utils.AID)  Utils.hexStringToByteArray(Utils.STATUS_SUCCESS)
        else Utils.hexStringToByteArray(Utils.STATUS_FAILED)*/
    }

    override fun onDeactivated(reason: Int) {
        println("El servicio MyHostApduService se desactiva: $reason")
    }
}