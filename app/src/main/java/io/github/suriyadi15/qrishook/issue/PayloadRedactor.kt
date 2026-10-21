package io.github.suriyadi15.qrishook.issue

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object PayloadRedactor {
    const val REDACTED = "[REDACTED]"
    const val REDACTED_NUMBER = "[REDACTED_NUMBER]"
    const val REDACTED_EMAIL = "[REDACTED_EMAIL]"

    private val sensitiveKeys = setOf(
        "android.people",
        "android.people.list",
        "android.messagingUser",
        "android.messagingStyleUser",
        "android.messages",
        "android.selfDisplayName",
    )

    private val emailRegex = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    private val phoneRegex = Regex("""(?<!\d)(?:\+62|62|0)8\d{1,3}[\s-]?\d{3,4}[\s-]?\d{3,5}(?!\d)""")
    private val longNumberRegex = Regex("""(?<!\d)\d{8,}(?!\d)""")

    fun redact(json: String): String {
        return try {
            redactObject(JSONObject(json)).toString(2)
        } catch (error: JSONException) {
            redactText(json)
        }
    }

    fun redactText(value: String): String {
        return value
            .replace(emailRegex, REDACTED_EMAIL)
            .replace(phoneRegex, REDACTED_NUMBER)
            .replace(longNumberRegex, REDACTED_NUMBER)
    }

    private fun redactObject(source: JSONObject): JSONObject {
        val result = JSONObject()
        source.keys().forEach { key ->
            val value = source.get(key)
            result.put(key, if (key in sensitiveKeys && value != JSONObject.NULL) REDACTED else redactValue(value))
        }
        return result
    }

    private fun redactArray(source: JSONArray): JSONArray {
        val result = JSONArray()
        for (index in 0 until source.length()) {
            result.put(redactValue(source.get(index)))
        }
        return result
    }

    private fun redactValue(value: Any): Any {
        return when (value) {
            is JSONObject -> redactObject(value)
            is JSONArray -> redactArray(value)
            is String -> redactText(value)
            else -> value
        }
    }
}
