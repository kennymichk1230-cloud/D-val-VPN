package com.example

import com.wireguard.config.WireGuardConfigParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WireGuardConfigParserTest {

    @Test
    fun testParseValidConfig() {
        val configText = """
            [Interface]
            PrivateKey = private_key_value
            Address = 10.0.0.2/24, fd86:ea04:1111::2/64
            DNS = 1.1.1.1, 8.8.8.8
            # Some random comment here

            [Peer]
            PublicKey = public_key_value
            AllowedIPs = 0.0.0.0/0, ::/0
            Endpoint = 192.168.1.100:51820
            PersistentKeepalive = 25
        """.trimIndent()

        val config = WireGuardConfigParser.parse(configText)

        assertNotNull(config)
        
        // Assert Interface values
        assertEquals("private_key_value", config.`interface`.privateKey)
        assertEquals(2, config.`interface`.addresses.size)
        assertEquals("10.0.0.2/24", config.`interface`.addresses[0])
        assertEquals("fd86:ea04:1111::2/64", config.`interface`.addresses[1])
        assertEquals(2, config.`interface`.dns.size)
        assertEquals("1.1.1.1", config.`interface`.dns[0])
        assertEquals("8.8.8.8", config.`interface`.dns[1])

        // Assert Peer values
        assertEquals(1, config.peers.size)
        val peer = config.peers[0]
        assertEquals("public_key_value", peer.publicKey)
        assertEquals("192.168.1.100:51820", peer.endpoint)
        assertEquals(2, peer.allowedIps.size)
        assertEquals("0.0.0.0/0", peer.allowedIps[0])
        assertEquals("::/0", peer.allowedIps[1])

        // Assert WgConfig helper properties
        assertEquals("public_key_value", config.publicKey)
        assertEquals("192.168.1.100:51820", config.endpoint)
        assertEquals(2, config.allowedIps.size)
    }

    @Test
    fun testParseMultiplePeers() {
        val configText = """
            [Interface]
            PrivateKey = interface_priv_key

            [Peer]
            PublicKey = peer_1_pub_key
            Endpoint = 10.0.0.1:51820
            AllowedIPs = 10.0.0.1/32

            [Peer]
            PublicKey = peer_2_pub_key
            Endpoint = 10.0.0.2:51820
            AllowedIPs = 10.0.0.2/32
        """.trimIndent()

        val config = WireGuardConfigParser.parse(configText)
        assertNotNull(config)
        assertEquals(2, config.peers.size)

        assertEquals("peer_1_pub_key", config.peers[0].publicKey)
        assertEquals("10.0.0.1:51820", config.peers[0].endpoint)
        assertEquals("10.0.0.1/32", config.peers[0].allowedIps[0])

        assertEquals("peer_2_pub_key", config.peers[1].publicKey)
        assertEquals("10.0.0.2:51820", config.peers[1].endpoint)
        assertEquals("10.0.0.2/32", config.peers[1].allowedIps[0])
    }

    @Test
    fun testParseEmptyConfig() {
        val configText = ""
        val config = WireGuardConfigParser.parse(configText)
        assertNotNull(config)
        assertEquals("", config.`interface`.privateKey)
        assertTrue(config.peers.isEmpty())
        assertEquals("", config.publicKey)
        assertEquals("", config.endpoint)
        assertTrue(config.allowedIps.isEmpty())
    }
}
