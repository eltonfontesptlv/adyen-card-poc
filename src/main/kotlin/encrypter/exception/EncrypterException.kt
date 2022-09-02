package encrypter.exception

internal class EncrypterException(message: String?, cause: Throwable?) : Exception(message, cause) {
    companion object {
        private const val serialVersionUID = 2699577096011945291L
    }
}
