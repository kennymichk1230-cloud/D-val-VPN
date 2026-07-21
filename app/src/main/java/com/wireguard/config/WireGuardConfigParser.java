package com.wireguard.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A robust utility to parse WireGuard configuration files (.conf) in Java.
 * It extracts key parameters such as Endpoint, Public Key, and Allowed IPs
 * from the configuration sections.
 */
public class WireGuardConfigParser {

    public static class PeerConfig {
        private String endpoint = "";
        private String publicKey = "";
        private final List<String> allowedIps = new ArrayList<>();

        public String getEndpoint() {
            return endpoint;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public List<String> getAllowedIps() {
            return Collections.unmodifiableList(allowedIps);
        }

        @Override
        public String toString() {
            return "PeerConfig{" +
                    "endpoint='" + endpoint + '\'' +
                    ", publicKey='" + publicKey + '\'' +
                    ", allowedIps=" + allowedIps +
                    '}';
        }
    }

    public static class InterfaceConfig {
        private String privateKey = "";
        private final List<String> addresses = new ArrayList<>();
        private final List<String> dns = new ArrayList<>();

        public String getPrivateKey() {
            return privateKey;
        }

        public List<String> getAddresses() {
            return Collections.unmodifiableList(addresses);
        }

        public List<String> getDns() {
            return Collections.unmodifiableList(dns);
        }

        @Override
        public String toString() {
            return "InterfaceConfig{" +
                    "privateKey='" + privateKey + '\'' +
                    ", addresses=" + addresses +
                    ", dns=" + dns +
                    '}';
        }
    }

    public static class WgConfig {
        private final InterfaceConfig interfaceConfig = new InterfaceConfig();
        private final List<PeerConfig> peers = new ArrayList<>();

        public InterfaceConfig getInterface() {
            return interfaceConfig;
        }

        public List<PeerConfig> getPeers() {
            return Collections.unmodifiableList(peers);
        }

        /**
         * Convenience helper to get the primary (first) peer endpoint.
         */
        public String getEndpoint() {
            return peers.isEmpty() ? "" : peers.get(0).getEndpoint();
        }

        /**
         * Convenience helper to get the primary (first) peer public key.
         */
        public String getPublicKey() {
            return peers.isEmpty() ? "" : peers.get(0).getPublicKey();
        }

        /**
         * Convenience helper to get the primary (first) peer allowed IPs.
         */
        public List<String> getAllowedIps() {
            return peers.isEmpty() ? Collections.emptyList() : peers.get(0).getAllowedIps();
        }
    }

    /**
     * Parses a WireGuard .conf file from an InputStream.
     *
     * @param inputStream The input stream containing the .conf file content.
     * @return WgConfig representing the parsed configuration.
     * @throws IOException If any reading error occurs.
     */
    public static WgConfig parse(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return parse(reader);
        }
    }

    /**
     * Parses a WireGuard .conf file from a String.
     *
     * @param configText The configuration file content as a string.
     * @return WgConfig representing the parsed configuration.
     */
    public static WgConfig parse(String configText) {
        if (configText == null) {
            throw new IllegalArgumentException("configText cannot be null");
        }
        try (BufferedReader reader = new BufferedReader(new StringReader(configText))) {
            return parse(reader);
        } catch (IOException e) {
            // StringReader doesn't throw IOExceptions, but handle it gracefully just in case
            throw new RuntimeException("Unexpected parsing failure", e);
        }
    }

    private static WgConfig parse(BufferedReader reader) throws IOException {
        WgConfig config = new WgConfig();
        String line;
        String currentSection = "";
        PeerConfig currentPeer = null;

        while ((line = reader.readLine()) != null) {
            // Strip comments and leading/trailing whitespace
            String cleanLine = line.trim();
            if (cleanLine.isEmpty() || cleanLine.startsWith("#") || cleanLine.startsWith(";")) {
                continue;
            }

            // Detect section headers
            if (cleanLine.startsWith("[") && cleanLine.endsWith("]")) {
                currentSection = cleanLine.substring(1, cleanLine.length() - 1).trim().toLowerCase();
                if ("peer".equals(currentSection)) {
                    currentPeer = new PeerConfig();
                    config.peers.add(currentPeer);
                } else {
                    currentPeer = null;
                }
                continue;
            }

            // Parse Key-Value pairs
            int equalIdx = cleanLine.indexOf('=');
            if (equalIdx != -1) {
                String key = cleanLine.substring(0, equalIdx).trim().toLowerCase();
                String value = cleanLine.substring(equalIdx + 1).trim();

                if ("interface".equals(currentSection)) {
                    switch (key) {
                        case "privatekey":
                            config.interfaceConfig.privateKey = value;
                            break;
                        case "address":
                            parseCommaSeparatedList(value, config.interfaceConfig.addresses);
                            break;
                        case "dns":
                            parseCommaSeparatedList(value, config.interfaceConfig.dns);
                            break;
                    }
                } else if ("peer".equals(currentSection) && currentPeer != null) {
                    switch (key) {
                        case "publickey":
                            currentPeer.publicKey = value;
                            break;
                        case "endpoint":
                            currentPeer.endpoint = value;
                            break;
                        case "allowedips":
                            parseCommaSeparatedList(value, currentPeer.allowedIps);
                            break;
                    }
                }
            }
        }
        return config;
    }

    private static void parseCommaSeparatedList(String value, List<String> targetList) {
        String[] parts = value.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                targetList.add(trimmed);
            }
        }
    }
}
