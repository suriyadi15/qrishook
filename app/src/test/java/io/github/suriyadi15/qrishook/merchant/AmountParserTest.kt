package io.github.suriyadi15.qrishook.merchant

import org.junit.Assert.assertEquals
import org.junit.Test

class AmountParserTest {
    @Test
    fun parsesRupiahFormats() {
        assertEquals(10_000L, AmountParser.parse("QRIS dibayar Rp10.000", AmountParser.DEFAULT_PATTERNS))
        assertEquals(10_000L, AmountParser.parse("Pembayaran Rp 10.000 berhasil", AmountParser.DEFAULT_PATTERNS))
        assertEquals(10_000L, AmountParser.parse("Payment received IDR 10,000", AmountParser.DEFAULT_PATTERNS))
    }
}
