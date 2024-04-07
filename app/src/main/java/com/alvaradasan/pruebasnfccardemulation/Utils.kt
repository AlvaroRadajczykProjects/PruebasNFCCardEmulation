package com.alvaradasan.pruebasnfccardemulation

/**
 * Clase que tiene métodos para gestionar arrays de bytes y convertirlos en cadenas de texto en hexadecimal, y viceversa. También hay algunas
 * variables con representaciones hexadecimales en cadenas de texto, y otras variables, que se pueden utilizar para enviar una respuesta en el
 * formato del protocolo APDU, aunque no se utilizan en este proyecto, o al menos en esta rama
 *
 * @constructor Create empty constructor for Utils.
 */
class Utils {
    companion object {
        val MIN_APDU_LENGTH = 12
        val STATUS_SUCCESS = "9000"
        val STATUS_FAILED = "6F00"
        val CLA_NOT_SUPPORTED = "6E00"
        val INS_NOT_SUPPORTED = "6D00"
        val AID = "A0000002471001"
        val SELECT_INS = "A4"
        val DEFAULT_CLA = "00"

        private val HEX_CHARS = "0123456789ABCDEF"
        private val HEX_CHARS_ARRAY = HEX_CHARS.toCharArray()

        /**
         * Devuelve un array de bytes con los valores en hexadecimal que representa la cadena de texto data pasada como argumento
         *
         * @param data Cadena de caracteres que debería representar un valor en hexadecimal
         * @return [Byte] Cadena de bytes que se asocia  a la representación en hexadecinal de data
         */
        fun hexStringToByteArray(data: String) : ByteArray {
            val result = ByteArray(data.length / 2)
            for (i in 0 until data.length step 2) {
                val firstIndex = HEX_CHARS.indexOf(data[i]);
                val secondIndex = HEX_CHARS.indexOf(data[i + 1]);
                val octet = firstIndex.shl(4).or(secondIndex)
                result.set(i.shr(1), octet.toByte())
            }
            return result
        }

        /**
         * Devuelve una cadena de texto que representa un valor en hexadecimal, a partir de los bytes del argumento byteArray
         *
         * @param byteArray array de bytes con el que se quiere obtener la representación hexadecimal
         * @return [String] cadena de texto con la representación en hexadecimal que se espera obtener
         */
        fun toHex(byteArray: ByteArray) : String {
            val result = StringBuffer()
            byteArray.forEach {
                val octet = it.toInt()
                val firstIndex = (octet and 0xF0).ushr(4)
                val secondIndex = octet and 0x0F
                result.append(HEX_CHARS_ARRAY[firstIndex])
                result.append(HEX_CHARS_ARRAY[secondIndex])
            }
            return result.toString()
        }
    }
}