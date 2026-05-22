package io.github.suriyadi15.qrishook.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class JsonFormatterTest {
    @Test
    fun formatsEscapedStringsAndNestedValues() {
        val payload = linkedMapOf(
            "title" to "A \"quoted\"\nline",
            "flags" to 10,
            "enabled" to true,
            "items" to listOf("one", null, "two\tthree"),
        )

        assertEquals(
            """
            {
              "title": "A \"quoted\"\nline",
              "flags": 10,
              "enabled": true,
              "items": [
                "one",
                null,
                "two\tthree"
              ]
            }
            """.trimIndent(),
            JsonFormatter.format(payload),
        )
    }
}
