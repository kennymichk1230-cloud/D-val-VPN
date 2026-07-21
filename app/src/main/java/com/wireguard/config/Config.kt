package com.wireguard.config

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class Config private constructor(
    val addresses: List<String>,
    val dns: List<String>,
    val privateKey: String,
    val publicKey: String,
    val endpoint: String,
    val allowedIps: List<String>
) {
    companion object {
        @JvmStatic
        fun parse(inputStream: InputStream): Config {
            val reader = BufferedReader(InputStreamReader(inputStream))
            var currentSection = ""
            val addresses = mutableListOf<String>()
            val dns = mutableListOf<String>()
            var privateKey = ""
            var publicKey = ""
            var endpoint = ""
            val allowedIps = mutableListOf<String>()

            reader.forEachLine { rawLine ->
                val line = rawLine.trim()
                if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) {
                    return@forEachLine
                }
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length - 1).trim().lowercase()
                    return@forEachLine
                }

                val parts = line.split("=", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim().lowercase()
                    val value = parts[1].trim()

                    when (currentSection) {
                        "interface" -> {
                            when (key) {
                                "privatekey" -> privateKey = value
                                "address" -> addresses.addAll(value.split(",").map { it.trim() })
                                "dns" -> dns.addAll(value.split(",").map { it.trim() })
                            }
                        }
                        "peer" -> {
                            when (key) {
                                "publickey" -> publicKey = value
                                "endpoint" -> endpoint = value
                                "allowedips" -> allowedIps.addAll(value.split(",").map { it.trim() })
                            }
                        }
                    }
                }
            }

            return Config(addresses, dns, privateKey, publicKey, endpoint, allowedIps)
        }
    }
}
