package io.github.suriyadi15.qrishook.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppSettingsTest {
    private val a = WebhookEndpoint("a", "Server A", "https://a.test/hook", "secret-a")
    private val b = WebhookEndpoint("b", "Server B", "https://b.test/hook")

    @Test
    fun webhookForPrefersMappingOverDefault() {
        val settings = AppSettings(
            webhooks = listOf(a, b),
            defaultWebhookId = "a",
            merchantWebhookIds = mapOf("bri" to "b"),
        )

        assertEquals(b, settings.webhookFor("bri"))
        assertEquals(a, settings.webhookFor("gopay"))
    }

    @Test
    fun webhookForFallsBackToDefaultWhenMappingPointsToDeletedWebhook() {
        val settings = AppSettings(
            webhooks = listOf(a),
            defaultWebhookId = "a",
            merchantWebhookIds = mapOf("bri" to "gone"),
        )

        assertEquals(a, settings.webhookFor("bri"))
    }

    @Test
    fun webhookForIsNullWithoutMappingOrDefault() {
        val settings = AppSettings(webhooks = listOf(a), defaultWebhookId = null)

        assertNull(settings.webhookFor("bri"))
    }

    @Test
    fun twoMerchantsCanShareOneWebhook() {
        val settings = AppSettings(
            webhooks = listOf(a, b),
            merchantWebhookIds = mapOf("bri" to "b", "gopay" to "b", "interactive" to "a"),
        )

        assertEquals(b, settings.webhookFor("bri"))
        assertEquals(b, settings.webhookFor("gopay"))
        assertEquals(a, settings.webhookFor("interactive"))
        assertEquals(setOf("bri", "gopay"), settings.merchantsMappedTo("b"))
    }

    @Test
    fun upsertFirstWebhookBecomesDefaultAndEnablesMerchants() {
        val result = AppSettings().withWebhookUpserted(a, setOf("bri"), makeDefault = false)

        assertEquals(listOf(a), result.webhooks)
        assertEquals("a", result.defaultWebhookId)
        assertEquals(mapOf("bri" to "a"), result.merchantWebhookIds)
        assertEquals(setOf("bri"), result.selectedMerchantIds)
    }

    @Test
    fun upsertMovesMerchantFromAnotherWebhook() {
        val start = AppSettings(
            webhooks = listOf(a, b),
            defaultWebhookId = "a",
            merchantWebhookIds = mapOf("bri" to "a", "gopay" to "a"),
            selectedMerchantIds = setOf("bri", "gopay"),
        )

        val result = start.withWebhookUpserted(b, setOf("bri"), makeDefault = false)

        assertEquals(mapOf("bri" to "b", "gopay" to "a"), result.merchantWebhookIds)
        assertEquals("a", result.defaultWebhookId)
    }

    @Test
    fun upsertDropsMappingsThatWereUncheckedAndRenamesInPlace() {
        val start = AppSettings(
            webhooks = listOf(a),
            defaultWebhookId = "a",
            merchantWebhookIds = mapOf("bri" to "a", "gopay" to "a"),
            selectedMerchantIds = setOf("bri", "gopay"),
        )

        val renamed = a.copy(name = "Renamed")
        val result = start.withWebhookUpserted(renamed, setOf("bri"), makeDefault = true)

        assertEquals(listOf(renamed), result.webhooks)
        assertEquals(mapOf("bri" to "a"), result.merchantWebhookIds)
        assertEquals("a", result.defaultWebhookId)
    }

    @Test
    fun upsertClearsDefaultWhenDefaultSwitchIsTurnedOff() {
        val start = AppSettings(webhooks = listOf(a, b), defaultWebhookId = "a")

        val result = start.withWebhookUpserted(a, emptySet(), makeDefault = false)

        assertNull(result.defaultWebhookId)
    }

    @Test
    fun removeCleansMappingsAndMovesDefault() {
        val start = AppSettings(
            webhooks = listOf(a, b),
            defaultWebhookId = "a",
            merchantWebhookIds = mapOf("bri" to "a", "gopay" to "b"),
            selectedMerchantIds = setOf("bri", "gopay"),
        )

        val result = start.withWebhookRemoved("a")

        assertEquals(listOf(b), result.webhooks)
        assertEquals("b", result.defaultWebhookId)
        assertEquals(mapOf("gopay" to "b"), result.merchantWebhookIds)
    }

    @Test
    fun removeLastWebhookClearsDefault() {
        val result = AppSettings(webhooks = listOf(a), defaultWebhookId = "a").withWebhookRemoved("a")

        assertNull(result.defaultWebhookId)
        assertEquals(emptyList<WebhookEndpoint>(), result.webhooks)
    }

    @Test
    fun unroutedMerchantsAreSelectedOnesWithoutDestination() {
        val settings = AppSettings(
            webhooks = listOf(a),
            defaultWebhookId = null,
            merchantWebhookIds = mapOf("bri" to "a"),
            selectedMerchantIds = setOf("bri", "gopay"),
        )

        assertEquals(setOf("gopay"), settings.unroutedMerchantIds())
        assertEquals(emptySet<String>(), settings.copy(defaultWebhookId = "a").unroutedMerchantIds())
    }
}
