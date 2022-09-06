package br.com.petlove

import br.com.petlove.exception.EncrypterException
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Card(private var publicKey: String) {
    var number: String? = null
    var expiryMonth: String? = null
    var expiryYear: String? = null
    var cardHolderName: String? = null
    var cvc: String? = null
    var generationTime: Date? = null

    fun serialize(): String? {
        val cardJson = JSONObject()
        var encryptedData: String? = null
        try {
            cardJson.put("generationtime", GENERATION_DATE_FORMAT.format(generationTime))
            cardJson.put("number", number)
            cardJson.put("holderName", cardHolderName)
            cardJson.put("cvc", cvc)
            cardJson.put("expiryMonth", expiryMonth)
            cardJson.put("expiryYear", expiryYear)
            encryptedData = encryptData(cardJson.toString(), publicKey)
        } catch (e: JSONException) {
            println(e.message)
        }
        return encryptedData
    }

    fun serializeParam(param: String, value: String): String? {
        val cardJson = JSONObject()
        var encryptedData: String? = null
        try {
            cardJson.put("generationtime", GENERATION_DATE_FORMAT.format(generationTime))
            cardJson.put(param, value)
            encryptedData = encryptData(cardJson.toString(), publicKey)
        } catch (e: JSONException) {
            println(e.message)
        }
        return encryptedData
    }

    private fun encryptData(data: String, publicKey: String): String? {
        var encryptedData: String? = null
        try {
            val encrypter = ClientSideEncrypter(publicKey)
            encryptedData = encrypter.encrypt(data)
        } catch (e: EncrypterException) {
            println("Error in encrypter " + e.message)
        }
        return encryptedData
    }

    override fun toString(): String {
        val cardJson = JSONObject()
        try {
            cardJson.put("generationtime", GENERATION_DATE_FORMAT.format(generationTime))
            if (number!!.length >= 4) {
                cardJson.put("number", number!!.substring(0, 3))
            }
            cardJson.put("holderName", cardHolderName)
        } catch (e: JSONException) {
            println(e.message)
        }
        return cardJson.toString()
    }

    companion object {
        private val GENERATION_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        init {
            GENERATION_DATE_FORMAT.timeZone = TimeZone.getTimeZone("UTC")
        }
    }
}
