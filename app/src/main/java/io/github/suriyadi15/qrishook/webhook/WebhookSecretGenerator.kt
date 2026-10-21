package io.github.suriyadi15.qrishook.webhook

import java.security.SecureRandom

object WebhookSecretGenerator {
    private val random = SecureRandom()

    /** 32 hex characters (128 bits). */
    fun generate(): String {
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return bytes.joinToString(separator = "") { "%02x".format(it) }
    }
}
