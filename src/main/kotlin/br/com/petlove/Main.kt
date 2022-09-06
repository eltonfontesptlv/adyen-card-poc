package br.com.petlove // ktlint-disable filename
import com.adyen.Client
import com.adyen.enums.Environment
import com.adyen.model.Amount
import com.adyen.model.checkout.PaymentsRequest
import com.adyen.model.checkout.PaymentsResponse
import com.adyen.model.checkout.details.CardDetails
import com.adyen.service.Checkout
import com.adyen.service.exception.ApiException
import java.util.*

fun main() {
    validateRawCardData()
    // testCardEncryption()
}

fun validateRawCardData() {
    val xApiKey = System.getenv("ADYEN_API_KEY")
    val merchantAccount = System.getenv("ADYEN_MERCHANT_ACCOUNT")
    val sendCardEncrypted = true

    try {
        val response: PaymentsResponse = Checkout(
            Client(xApiKey, Environment.TEST)
        )
            .payments(
                PaymentsRequest()
                    .merchantAccount(merchantAccount)
                    .enableRecurring(true)
                    .amount(
                        Amount()
                            .currency("BRL")
                            .value(0L)
                    )
                    .paymentMethod(
                        details(sendCardEncrypted)
                    )
                    .recurringProcessingModel(PaymentsRequest.RecurringProcessingModelEnum.SUBSCRIPTION)
                    .shopperInteraction(PaymentsRequest.ShopperInteractionEnum.CONTAUTH)
                    .reference("PAY-321654987")
            )
        println("Success: $response")
    } catch (ex: ApiException) {
        println("Error Api: ${ex.localizedMessage}")
    } catch (ex: Exception) {
        println("Error: ${ex.localizedMessage}")
    }
}

fun details(encrypted: Boolean = false): CardDetails =
    if (encrypted) {
        CardDetails()
            .type("scheme")
            .encryptedCardNumber("test_4111111111111111")
            .encryptedExpiryMonth("test_03")
            .encryptedExpiryYear("test_2030")
            .encryptedSecurityCode("test_737")
    } else {
        CardDetails()
            .type("scheme")
            .number("4111111111111111")
            .expiryMonth("03")
            .expiryYear("2030")
            .holderName("John Smith")
            .cvc("737")
    }

fun testCardEncryption() {
    val publicKey = System.getenv("ADYEN_PUBLIC_KEY")

    try {
        val card = Card(publicKey)
        card.number = "2222400010000008"
        card.cardHolderName = "John Doe"
        card.cvc = "737"
        card.expiryMonth = "03"
        card.expiryYear = "2030"
        card.generationTime = Date()
        val encryptedCard = card.serialize()

        println("Classic Version 68:")
        println(encryptedCard)
        println("---------------------------")

        val number = card.serializeParam("number", card.number!!)
        val cardHolderName = card.serializeParam("cardHolderName", card.cardHolderName!!)
        val cvc = card.serializeParam("cvc", card.cvc!!)
        val expiryMonth = card.serializeParam("expiryMonth", card.expiryMonth!!)
        val expiryYear = card.serializeParam("expiryYear", card.expiryYear!!)

        println("API Version 69:")
        println("Number: $number")
        println("Holder Name: $cardHolderName")
        println("CVC: $cvc")
        println("Expiry Month: $expiryMonth")
        println("Expiry Year: $expiryYear")
    } catch (ex: Exception) {
        println(ex.localizedMessage)
    }
}
