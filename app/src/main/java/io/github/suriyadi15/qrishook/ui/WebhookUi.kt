package io.github.suriyadi15.qrishook.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.suriyadi15.qrishook.R
import io.github.suriyadi15.qrishook.WebhookTestState
import io.github.suriyadi15.qrishook.data.AppSettings
import io.github.suriyadi15.qrishook.data.WebhookEndpoint
import io.github.suriyadi15.qrishook.merchant.MerchantRegistry
import io.github.suriyadi15.qrishook.webhook.WebhookResult
import io.github.suriyadi15.qrishook.webhook.WebhookSecretGenerator
import io.github.suriyadi15.qrishook.webhook.WebhookUrlValidation
import io.github.suriyadi15.qrishook.webhook.WebhookUrlValidator
import java.util.UUID

internal data class WebhookEditorTarget(
    val endpoint: WebhookEndpoint,
    val isNew: Boolean,
) {
    companion object {
        fun forNew() = WebhookEditorTarget(
            endpoint = WebhookEndpoint(id = UUID.randomUUID().toString(), name = "", url = ""),
            isNew = true,
        )

        /**
         * [webhookId] opens that webhook; null ("set up") opens the first webhook, or a new one
         * when none exists yet.
         */
        fun forId(settings: AppSettings, webhookId: String?): WebhookEditorTarget {
            val existing = settings.webhooks.firstOrNull { it.id == webhookId }
                ?: if (webhookId == null) settings.webhooks.firstOrNull() else null
            return existing?.let { WebhookEditorTarget(it, isNew = false) } ?: forNew()
        }
    }
}

private fun merchantName(merchantId: String): String {
    return MerchantRegistry.builtInParsers.firstOrNull { it.merchantId == merchantId }?.displayName
        ?: merchantId
}

private fun compactUrl(url: String): String {
    return url.trim().removePrefix("https://").removePrefix("http://")
}

private fun copyToClipboard(context: Context, label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
}

@Composable
internal fun WebhookSection(
    settings: AppSettings,
    tests: Map<String, WebhookTestState>,
    onAddWebhook: () -> Unit,
    onEditWebhook: (String?) -> Unit,
    onRetryPending: () -> Unit,
) {
    Section {
        Text(stringResource(R.string.webhook_title), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        if (settings.webhooks.isEmpty()) {
            Text(
                stringResource(R.string.webhook_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                settings.webhooks.forEach { endpoint ->
                    WebhookRow(
                        endpoint = endpoint,
                        settings = settings,
                        testState = tests[endpoint.id],
                        onClick = { onEditWebhook(endpoint.id) },
                    )
                }
            }
        }

        val unrouted = settings.unroutedMerchantIds()
        if (unrouted.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            UnroutedBanner(
                merchantNames = unrouted.map(::merchantName).sorted(),
                onSetup = { onEditWebhook(null) },
            )
        }

        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onAddWebhook) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.webhook_add))
            }
            if (settings.webhooks.isNotEmpty()) {
                OutlinedButton(onClick = onRetryPending) {
                    Text(stringResource(R.string.webhook_retry))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WebhookRow(
    endpoint: WebhookEndpoint,
    settings: AppSettings,
    testState: WebhookTestState?,
    onClick: () -> Unit,
) {
    val isDefault = settings.defaultWebhookId == endpoint.id
    val merchants = settings.merchantsMappedTo(endpoint.id).map(::merchantName).sorted()

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    endpoint.name,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (isDefault) {
                    Spacer(Modifier.width(8.dp))
                    Chip(stringResource(R.string.webhook_default_chip), highlighted = true)
                }
                Spacer(Modifier.weight(1f))
                if (endpoint.secret.isNotBlank()) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = stringResource(R.string.webhook_secret_set),
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Text(
                compactUrl(endpoint.url),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (merchants.isNotEmpty() || isDefault) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    merchants.forEach { Chip(it) }
                    if (isDefault) Chip(stringResource(R.string.webhook_other_merchants))
                }
            }
            TestResultLine(testState = testState, currentUrl = endpoint.url)
        }
    }
}

@Composable
private fun Chip(text: String, highlighted: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (highlighted) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

/** Shows the last "Send test" outcome, but only while it still matches the URL on screen. */
@Composable
private fun TestResultLine(testState: WebhookTestState?, currentUrl: String) {
    when (testState) {
        null -> Unit
        WebhookTestState.Sending -> Text(
            stringResource(R.string.webhook_testing),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
        )

        is WebhookTestState.Done -> {
            val result = testState.result
            val resultUrl = when (result) {
                is WebhookResult.Success -> result.webhookUrl
                is WebhookResult.Failure -> result.webhookUrl
            }
            if (resultUrl != currentUrl.trim()) return
            when (result) {
                is WebhookResult.Success -> Text(
                    "✓ " + stringResource(R.string.webhook_test_ok, result.code),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                is WebhookResult.Failure -> Text(
                    "✗ " + stringResource(R.string.webhook_test_fail, result.reason),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun UnroutedBanner(merchantNames: List<String>, onSetup: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.webhook_unrouted_banner, merchantNames.joinToString()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onSetup) {
                Text(stringResource(R.string.webhook_unrouted_action))
            }
        }
    }
}

@Composable
internal fun MerchantSection(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onEditWebhook: (String?) -> Unit,
) {
    Section {
        Text(stringResource(R.string.merchant_title), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        MerchantPicker(
            settings = settings,
            onSelectedMerchantIdsChange = {
                onSettingsChange(settings.copy(selectedMerchantIds = it))
            },
            onEditWebhook = onEditWebhook,
        )
    }
}

@Composable
private fun MerchantPicker(
    settings: AppSettings,
    onSelectedMerchantIdsChange: (Set<String>) -> Unit,
    onEditWebhook: (String?) -> Unit,
) {
    val selectedMerchantIds = settings.selectedMerchantIds

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        MerchantRegistry.builtInParsers.forEach { parser ->
            val checked = parser.merchantId in selectedMerchantIds
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(parser.displayName, fontWeight = FontWeight.Medium)
                    Text(
                        parser.merchantPackages.joinToString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    if (checked) {
                        MerchantDestination(
                            settings = settings,
                            merchantId = parser.merchantId,
                            onClick = onEditWebhook,
                        )
                    }
                }
                Checkbox(
                    checked = checked,
                    onCheckedChange = { isChecked ->
                        val next = if (isChecked) {
                            selectedMerchantIds + parser.merchantId
                        } else {
                            selectedMerchantIds - parser.merchantId
                        }
                        onSelectedMerchantIdsChange(next)
                    },
                )
            }
        }
    }
}

@Composable
private fun MerchantDestination(
    settings: AppSettings,
    merchantId: String,
    onClick: (String?) -> Unit,
) {
    val destination = settings.webhookFor(merchantId)
    val explicit = settings.merchantWebhookIds[merchantId] == destination?.id
    val (text, color) = when {
        destination == null -> stringResource(R.string.merchant_dest_none) to MaterialTheme.colorScheme.error
        explicit -> stringResource(R.string.merchant_dest, destination.name) to MaterialTheme.colorScheme.primary
        else -> stringResource(R.string.merchant_dest_default, destination.name) to MaterialTheme.colorScheme.primary
    }
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = Modifier
            .clickable { onClick(destination?.id) }
            .padding(vertical = 4.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WebhookEditorSheet(
    settings: AppSettings,
    target: WebhookEditorTarget,
    testState: WebhookTestState?,
    onSettingsChange: (AppSettings) -> Unit,
    onTest: (WebhookEndpoint) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val initial = target.endpoint
    val id = initial.id

    var name by remember(id) { mutableStateOf(initial.name) }
    var url by remember(id) { mutableStateOf(initial.url) }
    var secret by remember(id) { mutableStateOf(initial.secret) }
    var showSecret by remember(id) { mutableStateOf(false) }
    var merchantIds by remember(id) { mutableStateOf(settings.merchantsMappedTo(id)) }
    var makeDefault by remember(id) {
        mutableStateOf(settings.defaultWebhookId == id || (target.isNew && settings.webhooks.isEmpty()))
    }
    var confirmDelete by remember(id) { mutableStateOf(false) }

    val validation = WebhookUrlValidator.validate(url)
    val canSave = validation is WebhookUrlValidation.Valid || validation is WebhookUrlValidation.Insecure
    val testing = testState is WebhookTestState.Sending

    fun currentEndpoint() = WebhookEndpoint(
        id = id,
        name = name.trim().ifBlank { WebhookUrlValidator.displayHost(url) },
        url = url.trim(),
        secret = secret.trim(),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(
                    if (target.isNew) R.string.webhook_sheet_add_title else R.string.webhook_sheet_edit_title,
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.webhook_name_label)) },
                placeholder = { Text(stringResource(R.string.webhook_name_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text(stringResource(R.string.webhook_url_label)) },
                placeholder = { Text(stringResource(R.string.webhook_url_placeholder)) },
                singleLine = true,
                isError = validation is WebhookUrlValidation.Invalid,
                supportingText = when (validation) {
                    WebhookUrlValidation.Invalid -> {
                        { Text(stringResource(R.string.webhook_url_invalid)) }
                    }

                    WebhookUrlValidation.Insecure -> {
                        { Text(stringResource(R.string.webhook_url_insecure)) }
                    }

                    else -> null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = secret,
                onValueChange = { secret = it },
                label = { Text(stringResource(R.string.webhook_secret_label)) },
                supportingText = { Text(stringResource(R.string.webhook_secret_helper)) },
                singleLine = true,
                visualTransformation = if (showSecret) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showSecret = !showSecret }) {
                        Text(
                            stringResource(
                                if (showSecret) R.string.webhook_secret_hide else R.string.webhook_secret_show,
                            ),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        secret = WebhookSecretGenerator.generate()
                        showSecret = true
                    },
                ) {
                    Text(stringResource(R.string.webhook_secret_generate))
                }
                if (secret.isNotBlank()) {
                    OutlinedButton(onClick = { copyToClipboard(context, "Webhook secret", secret) }) {
                        Text(stringResource(R.string.copy))
                    }
                }
            }

            Text(
                stringResource(R.string.webhook_merchants_title),
                fontWeight = FontWeight.SemiBold,
            )
            Column {
                MerchantRegistry.builtInParsers.forEach { parser ->
                    val checked = parser.merchantId in merchantIds
                    val currentOwner = settings.merchantWebhookIds[parser.merchantId]
                        ?.takeIf { it != id }
                        ?.let { ownerId -> settings.webhooks.firstOrNull { it.id == ownerId } }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                merchantIds = if (checked) {
                                    merchantIds - parser.merchantId
                                } else {
                                    merchantIds + parser.merchantId
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = checked, onCheckedChange = null)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(parser.displayName, fontWeight = FontWeight.Medium)
                            if (currentOwner != null) {
                                Text(
                                    stringResource(R.string.webhook_merchant_currently, currentOwner.name),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(stringResource(R.string.webhook_default_switch), fontWeight = FontWeight.SemiBold)
                    Text(
                        stringResource(R.string.webhook_default_helper),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                Switch(checked = makeDefault, onCheckedChange = { makeDefault = it })
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    enabled = canSave && !testing,
                    onClick = { onTest(currentEndpoint()) },
                ) {
                    Text(
                        stringResource(if (testing) R.string.webhook_testing else R.string.webhook_send_test),
                    )
                }
                TestResultLine(testState = testState, currentUrl = url)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!target.isNew) {
                    TextButton(onClick = { confirmDelete = true }) {
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                Spacer(Modifier.width(8.dp))
                Button(
                    enabled = canSave,
                    onClick = {
                        onSettingsChange(
                            settings.withWebhookUpserted(currentEndpoint(), merchantIds, makeDefault),
                        )
                        onDismiss()
                    },
                ) {
                    Text(stringResource(R.string.save))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (confirmDelete) {
        val affected = settings.selectedMerchantIds
            .filter { settings.webhookFor(it)?.id == id }
            .map(::merchantName)
            .sorted()
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.webhook_delete_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.webhook_delete_message, initial.name.ifBlank { initial.url }))
                    if (affected.isNotEmpty()) {
                        Text(
                            stringResource(R.string.webhook_delete_affected, affected.joinToString()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onSettingsChange(settings.withWebhookRemoved(id))
                        onDismiss()
                    },
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}
