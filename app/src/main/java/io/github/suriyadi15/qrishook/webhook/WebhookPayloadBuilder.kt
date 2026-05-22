package io.github.suriyadi15.qrishook.webhook

import io.github.suriyadi15.qrishook.data.EventEntity

object WebhookPayloadBuilder {
    fun build(event: EventEntity): String = buildFormatted(event)

    private fun buildFormatted(event: EventEntity): String {
        return jsonObject(
            baseFields(event) + listOf(
                jsonObjectField(
                    "notification",
                    listOf(
                        jsonField("source_package", event.sourcePackage),
                        jsonField("source_app", event.sourceApp),
                        jsonField("title", event.title),
                        jsonField("text", event.text),
                        jsonField("big_text", event.bigText),
                        jsonField("received_at", event.receivedAt),
                    ),
                ),
                jsonObjectField(
                    "payment",
                    listOf(
                        jsonField("amount", event.amount),
                        jsonField("currency", event.currency),
                        jsonNullableField("sender_name", event.senderName),
                        jsonNullableField("payment_source", event.paymentSource),
                    ),
                ),
                jsonObjectField(
                    "raw",
                    listOf(
                        jsonField("source_package", event.sourcePackage),
                        jsonField("source_app", event.sourceApp),
                        jsonField("title", event.title),
                        jsonField("text", event.text),
                        jsonField("big_text", event.bigText),
                        jsonField("received_at", event.receivedAt),
                    ),
                ),
            ),
        )
    }

    private fun baseFields(event: EventEntity): List<String> {
        return listOf(
            jsonField("event_id", event.eventId),
            jsonField("type", event.type),
            jsonField("merchant_id", event.merchantId),
            jsonField("source_package", event.sourcePackage),
            jsonField("source_app", event.sourceApp),
            jsonField("received_at", event.receivedAt),
        )
    }

    private fun jsonObject(fields: List<String>): String {
        return fields.joinToString(separator = ",", prefix = "{", postfix = "}")
    }

    private fun jsonObjectField(name: String, fields: List<String>): String {
        return "${quote(name)}:${jsonObject(fields)}"
    }

    private fun jsonField(name: String, value: String): String {
        return "${quote(name)}:${quote(value)}"
    }

    private fun jsonField(name: String, value: Long): String {
        return "${quote(name)}:$value"
    }

    private fun jsonNullableField(name: String, value: String?): String {
        return if (value == null) {
            "${quote(name)}:null"
        } else {
            jsonField(name, value)
        }
    }

    private fun quote(value: String): String {
        return buildString {
            append('"')
            value.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> {
                        if (char.code < 0x20) {
                            append("\\u")
                            append(char.code.toString(16).padStart(4, '0'))
                        } else {
                            append(char)
                        }
                    }
                }
            }
            append('"')
        }
    }
}
