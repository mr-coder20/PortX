package com.mrcoder20.portx.data.local

import app.cash.sqldelight.ColumnAdapter

val listOfIntAdapter = object : ColumnAdapter<List<Int>, String> {
    override fun decode(databaseValue: String): List<Int> {
        val cleanValue = databaseValue.trim().removePrefix("[").removeSuffix("]")
        if (cleanValue.isEmpty()) return emptyList()
        return try {
            cleanValue.split(",").map { it.trim().toInt() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun encode(value: List<Int>): String = value.joinToString(separator = ",")
}

val mapIntStringAdapter = object : ColumnAdapter<Map<Int, String>, String> {
    override fun decode(databaseValue: String): Map<Int, String> {
        val cleanValue = databaseValue.trim().removePrefix("{").removeSuffix("}")
        if (cleanValue.isEmpty()) return emptyMap()
        return try {
            cleanValue.split("|").associate { entry ->
                val parts = entry.split(":")
                if (parts.size >= 2) {
                    parts[0].trim().toInt() to parts.subList(1, parts.size).joinToString(":")
                } else {
                    // Fallback for malformed entry
                    0 to "unknown"
                }
            }.filter { it.key != 0 }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    override fun encode(value: Map<Int, String>): String =
        value.entries.joinToString(separator = "|") { "${it.key}:${it.value}" }
}

val booleanAdapter = object : ColumnAdapter<Boolean, Long> {
    override fun decode(databaseValue: Long): Boolean = databaseValue == 1L
    override fun encode(value: Boolean): Long = if (value) 1L else 0L
}
