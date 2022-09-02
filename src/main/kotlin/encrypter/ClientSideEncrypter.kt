package encrypter

import encrypter.exception.EncrypterException
import org.bouncycastle.jce.provider.BouncyCastleProvider

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.*

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

class ClientSideEncrypter(publicKeyString: String) {
    private var pubKey: PublicKey? = null
    private var aesCipher: Cipher? = null
    private var rsaCipher: Cipher
    private val srandom: SecureRandom = SecureRandom()

    init {
        Security.addProvider(BouncyCastleProvider())
        val keyComponents = publicKeyString.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        val pubKeySpec = RSAPublicKeySpec(
            BigInteger(keyComponents[1].lowercase(Locale.getDefault()), 16),
            BigInteger(keyComponents[0].lowercase(Locale.getDefault()), 16)
        )
        pubKey = try {
            keyFactory.generatePublic(pubKeySpec)
        } catch (e: InvalidKeySpecException) {
            throw EncrypterException("Problem reading public key: $publicKeyString", e)
        }
        try {
            aesCipher = Cipher.getInstance("AES/CCM/NoPadding", "BC")
        } catch (e: NoSuchAlgorithmException) {
            throw EncrypterException("Problem instantiation AES Cipher Algorithm", e)
        } catch (e: NoSuchPaddingException) {
            throw EncrypterException("Problem instantiation AES Cipher Padding", e)
        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        }
        try {
            rsaCipher = Cipher.getInstance("RSA/None/PKCS1Padding")
            rsaCipher.init(Cipher.ENCRYPT_MODE, pubKey)
        } catch (e: NoSuchAlgorithmException) {
            throw EncrypterException("Problem instantiation RSA Cipher Algorithm", e)
        } catch (e: NoSuchPaddingException) {
            throw EncrypterException("Problem instantiation RSA Cipher Padding", e)
        } catch (e: InvalidKeyException) {
            throw EncrypterException("Invalid public key: $publicKeyString", e)
        }
    }

    @Throws(EncrypterException::class)
    fun encrypt(plainText: String): String {
        val aesKey = generateAESKey(256)
        val iv = generateIV(12)
        val encrypted: ByteArray
        try {
            aesCipher?.init(Cipher.ENCRYPT_MODE, aesKey, IvParameterSpec(iv))
            encrypted = aesCipher?.doFinal(plainText.toByteArray()) ?: throw EncrypterException("Error", null)
        } catch (e: IllegalBlockSizeException) {
            throw EncrypterException("Incorrect AES Block Size", e)
        } catch (e: BadPaddingException) {
            throw EncrypterException("Incorrect AES Padding", e)
        } catch (e: InvalidKeyException) {
            throw EncrypterException("Invalid AES Key", e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw EncrypterException("Invalid AES Parameters", e)
        }
        val result = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, result, 0, iv.size)
        System.arraycopy(encrypted, 0, result, iv.size, encrypted.size)
        val encryptedAESKey: ByteArray
        return try {
            encryptedAESKey = rsaCipher.doFinal(aesKey.encoded)
            PREFIX + VERSION + SEPARATOR + Base64.getEncoder().encode(encryptedAESKey) + SEPARATOR + Base64.getEncoder().encode(result)
        } catch (e: IllegalBlockSizeException) {
            throw EncrypterException("Incorrect RSA Block Size", e)
        } catch (e: BadPaddingException) {
            throw EncrypterException("Incorrect RSA Padding", e)
        }
    }

    @Throws(EncrypterException::class)
    private fun generateAESKey(keySize: Int): SecretKey {
        val kgen: KeyGenerator? = try {
            KeyGenerator.getInstance("AES")
        } catch (e: NoSuchAlgorithmException) {
            throw EncrypterException("Unable to get AES algorithm", e)
        }
        kgen?.init(keySize)
        if (kgen != null) {
            return kgen.generateKey()
        }

        throw EncrypterException("Unable to get AES algorithm", null)
    }

    @Synchronized
    private fun generateIV(ivSize: Int): ByteArray {
        val iv = ByteArray(ivSize)
        srandom.nextBytes(iv)
        return iv
    }

    companion object {
        private const val PREFIX = "adyenan"
        private const val VERSION = "0_1_1"
        private const val SEPARATOR = "$"
    }
}
