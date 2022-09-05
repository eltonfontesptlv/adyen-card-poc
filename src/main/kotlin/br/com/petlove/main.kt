package br.com.petlove // ktlint-disable filename
import br.com.petlove.exception.EncrypterException
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

fun main() {
    val publicKey = System.getenv("ADYEN_PUBLIC_KEY")

    try {
        val card = Card()
        card.number = "2222400010000008"
        card.cardHolderName = "John Doe"
        card.cvc = "737"
        card.expiryMonth = "03"
        card.expiryYear = "2030"
        card.generationTime = Date()

        val encryptedCard = card.serialize(publicKey)
        println(encryptedCard)
    } catch (ex: Exception) {
        println(ex.localizedMessage)
    }
}

class Card {
    var number: String? = null
    var expiryMonth: String? = null
    var expiryYear: String? = null
    var cardHolderName: String? = null
    var cvc: String? = null
    var generationTime: Date? = null

    fun serialize(publicKey: String): String? {
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
