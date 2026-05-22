package io.github.suriyadi15.qrishook.update

object AppVersion {
    fun compare(left: String, right: String): Int {
        val leftParts = parse(left) ?: return 0
        val rightParts = parse(right) ?: return 0
        val maxSize = maxOf(leftParts.size, rightParts.size)

        for (index in 0 until maxSize) {
            val leftPart = leftParts.getOrElse(index) { 0 }
            val rightPart = rightParts.getOrElse(index) { 0 }
            if (leftPart != rightPart) return leftPart.compareTo(rightPart)
        }

        return 0
    }

    private fun parse(version: String): List<Int>? {
        val normalized = version
            .trim()
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore("-")
            .substringBefore("+")

        if (normalized.isBlank()) return null

        return normalized
            .split(".")
            .map { part -> part.toIntOrNull() ?: return null }
    }
}
