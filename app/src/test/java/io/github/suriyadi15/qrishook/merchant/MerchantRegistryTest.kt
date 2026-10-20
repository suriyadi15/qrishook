package io.github.suriyadi15.qrishook.merchant

import org.junit.Assert.assertEquals
import org.junit.Test

class MerchantRegistryTest {
    @Test
    fun selectedParsersReturnsOnlySelectedParsers() {
        val parsers = MerchantRegistry.selectedParsers(
            setOf("interactive_qris", "bri_merchant", "gopay_merchant", "unknown_merchant"),
        )

        assertEquals(listOf("interactive_qris", "bri_merchant", "gopay_merchant"), parsers.map { it.merchantId })
    }

    @Test
    fun selectedParsersReturnsEmptyListWhenSelectionIsEmpty() {
        assertEquals(emptyList<Any>(), MerchantRegistry.selectedParsers(emptySet()))
    }
}
