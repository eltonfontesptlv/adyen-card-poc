import com.adyen.Client // ktlint-disable filename
import com.adyen.enums.Environment
import com.adyen.model.Amount
import com.adyen.model.Card
import com.adyen.model.checkout.PaymentsRequest
import com.adyen.model.checkout.PaymentsResponse
import com.adyen.model.checkout.details.CardDetails
import com.adyen.service.Checkout
import com.adyen.service.exception.ApiException
import encrypter.ClientSideEncrypter

fun main() {
    val xApiKey = System.getenv("ADYEN_API_KEY")
    val publicKey = System.getenv("ADYEN_PUBLIC_KEY")
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
                        Card().details(sendCardEncrypted)
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

fun Card.details(encrypted: Boolean = false): CardDetails =
    if (encrypted) {
        val publicKey = System.getenv("ADYEN_PUBLIC_KEY")
        val card = CardEncrypted(publicKey)
        CardDetails()
            .type("scheme")
            .encryptedCardNumber(card.serialize("4111111111111111"))
            .encryptedExpiryMonth(card.serialize("03"))
            .encryptedExpiryYear(card.serialize("2030"))
            .encryptedSecurityCode(card.serialize("737"))
    } else {
        CardDetails()
            .type("scheme")
            .number("4111111111111111")
            .expiryMonth("03")
            .expiryYear("2030")
            .holderName("John Smith")
            .cvc("737")
    }

class CardEncrypted(publicKey: String) {
    private var client: ClientSideEncrypter? = null
    private var encryptedCard = ""

    init {
        client = ClientSideEncrypter(publicKey)
    }

    fun serialize(data: String): String? {
        this.encryptedCard += data
        return client?.encrypt(data)
    }

    fun builder(): String? = serialize(this.encryptedCard)
}
