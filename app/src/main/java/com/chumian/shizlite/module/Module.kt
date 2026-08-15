package com.chumian.shizlite.module

import org.json.JSONObject

data class Module(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val requiredLevel: String,
    val entryScript: String,
    val installed: Boolean = true,
    val activated: Boolean = false,
    val installPath: String = ""
) {
    companion object {
        fun fromJson(json: JSONObject): Module {
            return Module(
                id = json.optString("id", "unknown"),
                name = json.optString("name", "Unknown"),
                version = json.optString("version", "1.0"),
                author = json.optString("author", "Unknown"),
                description = json.optString("description", ""),
                requiredLevel = json.optString("requiredLevel", "shizuku"),
                entryScript = json.optString("entryScript", "main.sh")
            )
        }
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("version", version)
            put("author", author)
            put("description", description)
            put("requiredLevel", requiredLevel)
            put("entryScript", entryScript)
            put("installed", installed)
            put("activated", activated)
            put("installPath", installPath)
        }
    }
}
