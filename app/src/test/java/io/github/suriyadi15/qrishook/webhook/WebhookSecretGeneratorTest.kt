package io.github.suriyadi15.qrishook.webhook

import org.junit.Assert.assertEquals
import org.junit.Test

class WebhookSecretGeneratorTest {
    @Test
    fun generatesDistinct32CharHexSecrets() {
        val first = WebhookSecretGenerator.generate()
        val second = WebhookSecretGenerator.generate()

        assertEquals(32, first.length)
        assertEquals(true, first.matches(Regex("[0-9a-f]{32}")))
        assertEquals(false, first == second)
    }
}
