package io.github.suriyadi15.qrishook.merchant

object AmountParser {
    val DEFAULT_PATTERNS = listOf(
        """(?i)\bRp\.?\s?([0-9][0-9.,]*)""",
        """(?i)\bIDR\s?([0-9][0-9.,]*)""",
        """(?i)\bsebesar\s+([0-9][0-9.,]*)""",
    )

    fun parse(text: String, patterns: List<String>): Long? {
        for (pattern in patterns) {
            val match = Regex(pattern).find(text) ?: continue
            val raw = match.groupValues.getOrNull(1).orEmpty()
            val normalized = raw
                .replace(".", "")
                .replace(",", "")
                .filter { it.isDigit() }
            val amount = normalized.toLongOrNull()
            if (amount != null && amount > 0) return amount
        }
        return null
    }
}
