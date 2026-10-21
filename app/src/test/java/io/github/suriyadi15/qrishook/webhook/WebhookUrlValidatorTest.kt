package io.github.suriyadi15.qrishook.webhook

import org.junit.Assert.assertEquals
import org.junit.Test

class WebhookUrlValidatorTest {
    @Test
    fun httpsIsValid() {
        assertEquals(WebhookUrlValidation.Valid, WebhookUrlValidator.validate("https://example.com/hook"))
    }

    @Test
    fun httpIsInsecure() {
        assertEquals(WebhookUrlValidation.Insecure, WebhookUrlValidator.validate("http://192.168.1.5:3000/hook"))
    }

    @Test
    fun blankIsEmpty() {
        assertEquals(WebhookUrlValidation.Empty, WebhookUrlValidator.validate("   "))
    }

    @Test
    fun missingSchemeOrBadHostIsInvalid() {
        assertEquals(WebhookUrlValidation.Invalid, WebhookUrlValidator.validate("example.com/hook"))
        assertEquals(WebhookUrlValidation.Invalid, WebhookUrlValidator.validate("ftp://example.com"))
        assertEquals(WebhookUrlValidation.Invalid, WebhookUrlValidator.validate("https://"))
    }

    @Test
    fun displayHostReturnsHostOrRawInput() {
        assertEquals("example.com", WebhookUrlValidator.displayHost("https://example.com/a/b"))
        assertEquals("not a url", WebhookUrlValidator.displayHost("not a url"))
    }
}
