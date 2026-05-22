package io.github.suriyadi15.qrishook.merchant

object MerchantRegistry {
    val builtInParsers: List<MerchantParser> = listOf(
        InterActiveQrisMerchantParser,
    )

    fun selectedParsers(selectedMerchantIds: Set<String>): List<MerchantParser> {
        if (selectedMerchantIds.isEmpty()) return emptyList()

        return builtInParsers.filter { parser ->
            parser.merchantId in selectedMerchantIds
        }
    }
}
