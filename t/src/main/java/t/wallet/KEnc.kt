package t.wallet

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.X509EncodedKeySpec
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


internal object JEnc {

    ///////////////////////////////////////////////////////////////////////////
    // AES encryption
    ///////////////////////////////////////////////////////////////////////////
    private const val transformation = "AES/ECB/PKCS5Padding"
    private const val CONFIG_KEY = "194,vusaidlvne84726*7n0jf098;n/e"


    fun decrypt(date: String): String {
        return String(
            decryptAES(
                hexString2Bytes(date),
                CONFIG_KEY.toByteArray(),
                transformation,
                null
            )!!, StandardCharsets.UTF_8
        )
    }

    /**
     * Return the bytes of hash encryption.
     *
     * @param data      The data.
     * @param algorithm The name of hash encryption.
     * @return the bytes of hash encryption
     */
    fun md5(data: ByteArray?): String? {
        return if (data == null || data.size <= 0) null else try {
            val md = MessageDigest.getInstance("MD5")
            md.update(data)
            bytes2HexString(md.digest())
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        }
    }



    /**
     * Return the bytes of AES decryption.
     *
     * @param data           The data.
     * @param key            The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv             The buffer with the IV. The contents of the
     * buffer are copied to protect against subsequent modification.
     * @return the bytes of AES decryption
     */
    private fun decryptAES(
        data: ByteArray,
        key: ByteArray,
        transformation: String,
        iv: ByteArray?
    ): ByteArray? {
        return symmetricTemplate(data, key, "AES", transformation, iv, false)
    }

    /**
     * Return the bytes of symmetric encryption or decryption.
     *
     * @param data           The data.
     * @param key            The key.
     * @param algorithm      The name of algorithm.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param isEncrypt      True to encrypt, false otherwise.
     * @return the bytes of symmetric encryption or decryption
     */
    private fun symmetricTemplate(
        data: ByteArray?,
        key: ByteArray?,
        algorithm: String,
        transformation: String,
        iv: ByteArray?,
        isEncrypt: Boolean
    ): ByteArray? {
        return if (data == null || data.size == 0 || key == null || key.size == 0) null else try {
            val secretKey: SecretKey
            secretKey = if ("DES" == algorithm) {
                val desKey = DESKeySpec(key)
                val keyFactory = SecretKeyFactory.getInstance(algorithm)
                keyFactory.generateSecret(desKey)
            } else {
                SecretKeySpec(key, algorithm)
            }
            val cipher = Cipher.getInstance(transformation)
            if (iv == null || iv.size == 0) {
                cipher.init(
                    if (isEncrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE,
                    secretKey
                )
            } else {
                val params: AlgorithmParameterSpec = IvParameterSpec(iv)
                cipher.init(
                    if (isEncrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE,
                    secretKey,
                    params
                )
            }
            cipher.doFinal(data)
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    private val HEX_DIGITS_UPPER = charArrayOf(
        '0',
        '1',
        '2',
        '3',
        '4',
        '5',
        '6',
        '7',
        '8',
        '9',
        'A',
        'B',
        'C',
        'D',
        'E',
        'F'
    )




    /**
     * Bytes to hex string.
     *
     * e.g. bytes2HexString(new byte[] { 0, (byte) 0xa8 }, true) returns "00A8"
     *
     * @param bytes       The bytes.
     * @return hex string
     */
    private fun bytes2HexString(bytes: ByteArray?): String {
        if (bytes == null) return ""
        val hexDigits = HEX_DIGITS_UPPER
        val len = bytes.size
        if (len <= 0) return ""
        val ret = CharArray(len shl 1)
        var i = 0
        var j = 0
        while (i < len) {
            ret[j++] = hexDigits[bytes[i].toInt() shr 4 and 0x0f]
            ret[j++] = hexDigits[bytes[i].toInt() and 0x0f]
            i++
        }
        return String(ret)
    }

    /**
     * Hex string to bytes.
     *
     * e.g. hexString2Bytes("00A8") returns { 0, (byte) 0xA8 }
     *
     * @param hexString The hex string.
     * @return the bytes
     */
    private fun hexString2Bytes(hexString: String): ByteArray {
        var hexString: String? = hexString
        if (hexString == null || hexString.length == 0) return ByteArray(0)
        var len = hexString.length
        if (len % 2 != 0) {
            hexString = "0$hexString"
            len = len + 1
        }
        val hexBytes = hexString.uppercase(Locale.getDefault()).toCharArray()
        val ret = ByteArray(len shr 1)
        var i = 0
        while (i < len) {
            ret[i shr 1] = (hex2Dec(hexBytes[i]) shl 4 or hex2Dec(
                hexBytes[i + 1]
            )).toByte()
            i += 2
        }
        return ret
    }

    private fun hex2Dec(hexChar: Char): Int {
        return if (hexChar >= '0' && hexChar <= '9') {
            hexChar.code - '0'.code
        } else if (hexChar >= 'A' && hexChar <= 'F') {
            hexChar.code - 'A'.code + 10
        } else {
            throw IllegalArgumentException()
        }
    }


    val ALGORITHM = "RSA"
    val PUBLIC_KEY = """
        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6SUwaFMBh/yHyp+zoLb5
        ic6FW5hTFMBOFWJWtPO0+ivjx/0Uc+dwdARi1+boZaVF2wCHIZMFf9PQrDuyByYU
        XePMtA7C23czFG0jmbXYPPfqvg7Ws2/UfuqKsYcZyPDG1I344t3io84RreQWwVIG
        dE8KGTykAotmBS40vQ8ug7Hu2xKafqFwdsVrcFaKIdyzS7mu5oXyuBapWm17UyGt
        V+HH74oVkcAa4Vo7HHVF1Y2UD2VJe5G9hNKyBPKUhzFQddfsy+uwfdeLDT9nNq1e
        EML3D3vMQfI2X3uxLydPEImRW/4tawjHRCIFp06qIZ/Ct9YrU+413CAD+0zyfEYM
        hQIDAQAB
    """.trimIndent()



    fun encryptRSA(text: String): String? {
        var cipherText: String? = null
        try {
            // generate publicKey instance
            val byteKey = stringToByteArray(PUBLIC_KEY)
            val X509publicKey = X509EncodedKeySpec(byteKey)
            val kf: KeyFactory = KeyFactory.getInstance(ALGORITHM)
            val publicKey = kf.generatePublic(X509publicKey)
            // get an RSA cipher object and print the provider
            val cipher: Cipher = Cipher.getInstance(ALGORITHM)
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            cipherText = cipher.doFinal(text.toByteArray()).let {
                byteArrayToString(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cipherText
    }


    private fun stringToByteArray(text: String): ByteArray {
//        return Base64.decode(text, Base64.DEFAULT)
        return text.decodeBase64ToByteArray()
    }

    private fun byteArrayToString(byteArray: ByteArray): String {
//        return Base64.encodeToString(byteArray, Base64.DEFAULT)
        return byteArray.encodeBase64ToString()
    }



}


fun String.encodeBase64ToString(): String = String(this.toByteArray().encodeBase64())
fun String.encodeBase64ToByteArray(): ByteArray = this.toByteArray().encodeBase64()
fun ByteArray.encodeBase64ToString(): String = String(this.encodeBase64())

fun String.decodeBase64(): String = String(this.toByteArray().decodeBase64())
fun String.decodeBase64ToByteArray(): ByteArray = this.toByteArray().decodeBase64()
fun ByteArray.decodeBase64ToString(): String = String(this.decodeBase64())

fun ByteArray.encodeBase64(): ByteArray {
    val table = (CharRange('A', 'Z') + CharRange('a', 'z') + CharRange('0', '9') + '+' + '/').toCharArray()
    val output = ByteArrayOutputStream()
    var padding = 0
    var position = 0
    while (position < this.size) {
        var b = this[position].toInt() and 0xFF shl 16 and 0xFFFFFF
        if (position + 1 < this.size) b = b or (this[position + 1].toInt() and 0xFF shl 8) else padding++
        if (position + 2 < this.size) b = b or (this[position + 2].toInt() and 0xFF) else padding++
        for (i in 0 until 4 - padding) {
            val c = b and 0xFC0000 shr 18
            output.write(table[c].toInt())
            b = b shl 6
        }
        position += 3
    }
    for (i in 0 until padding) {
        output.write('='.toInt())
    }
    return output.toByteArray()
}

fun ByteArray.decodeBase64(): ByteArray {
    val table = intArrayOf(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1,
        -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
        -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1)

    val output = ByteArrayOutputStream()
    var position = 0
    while (position < this.size) {
        var b: Int
        if (table[this[position].toInt()] != -1) {
            b = table[this[position].toInt()] and 0xFF shl 18
        } else {
            position++
            continue
        }
        var count = 0
        if (position + 1 < this.size && table[this[position + 1].toInt()] != -1) {
            b = b or (table[this[position + 1].toInt()] and 0xFF shl 12)
            count++
        }
        if (position + 2 < this.size && table[this[position + 2].toInt()] != -1) {
            b = b or (table[this[position + 2].toInt()] and 0xFF shl 6)
            count++
        }
        if (position + 3 < this.size && table[this[position + 3].toInt()] != -1) {
            b = b or (table[this[position + 3].toInt()] and 0xFF)
            count++
        }
        while (count > 0) {
            val c = b and 0xFF0000 shr 16
            output.write(c.toChar().toInt())
            b = b shl 8
            count--
        }
        position += 4
    }
    return output.toByteArray()
}