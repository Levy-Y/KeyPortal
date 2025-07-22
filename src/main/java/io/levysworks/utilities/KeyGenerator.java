package io.levysworks.utilities;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for generating SSH key pairs and hashing keys.
 */
public class KeyGenerator {
    /**
     * Generates an Ed25519 SSH key pair.
     *
     * @return an {@link SSHKeyPair} containing the private and public keys
     * @throws IOException if key generation fails
     * @throws InterruptedException if the process is interrupted
     */
    public static SSHKeyPair RegisterKeypair() throws IOException, InterruptedException {
        return generateEd25519KeyPair();
    }

    /**
     * Provides hashing functionality for SSH keys.
     */
    @ApplicationScoped
    public static class KeyHasher {
        /**
         * Generates a SHA-256 fingerprint from the given key string.
         *
         * @param key the input key string
         * @return hexadecimal SHA-256 hash fingerprint
         * @throws NoSuchAlgorithmException if SHA-256 algorithm is not available
         */
        public String generateFingerprint(String key) throws NoSuchAlgorithmException {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            md.update(key.getBytes());

            byte[] digest = md.digest();

            StringBuilder hexString = new StringBuilder();

            for (byte b : digest) {
                hexString.append(Integer.toHexString(0xFF & b));
            }

            return hexString.toString();
        }
    }

    /**
     * Represents a pair of SSH keys: private and public.
     */
    public static class SSHKeyPair {
        private final String privateKey;
        private final String publicKey;

        /**
         * Constructs an SSHKeyPair.
         *
         * @param privateKey the private key string
         * @param publicKey the public key string
         */
        public SSHKeyPair(String privateKey, String publicKey) {
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        public String getPrivateKey() { return privateKey; }
        public String getPublicKey() { return publicKey; }
    }

    /**
     * Generates an SSH key pair using the system's ssh-keygen command.
     *
     * @param keyType the type of key
     * @param keySize the size of the key in bits
     * @return generated {@link SSHKeyPair}
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    public static SSHKeyPair generateKeyPair(String keyType, int keySize) throws IOException, InterruptedException {
        String tempDir = System.getProperty("java.io.tmpdir");
        String keyPath = tempDir + File.separator + "temp_ssh_key_" + System.currentTimeMillis();

        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[9];
        random.nextBytes(bytes);
        String uid = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        ProcessBuilder pb = new ProcessBuilder();
        pb.command("ssh-keygen", "-t", keyType, "-b", String.valueOf(keySize), "-C", uid, "-f", keyPath, "-N", "");

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            StringBuilder errorMsg = new StringBuilder();
            while ((errorLine = errorReader.readLine()) != null) {
                errorMsg.append(errorLine).append("\n");
            }
            throw new RuntimeException("ssh-keygen failed: " + errorMsg.toString());
        }

        Path privateKeyPath = Paths.get(keyPath);
        Path publicKeyPath = Paths.get(keyPath + ".pub");

        String privateKey = Files.readString(privateKeyPath);
        String publicKey = Files.readString(publicKeyPath);

        Files.deleteIfExists(privateKeyPath);
        Files.deleteIfExists(publicKeyPath);

        return new SSHKeyPair(privateKey, publicKey);
    }

    /**
     * Generates an RSA SSH key pair with the specified size.
     *
     * @param keySize key size in bits
     * @return generated RSA {@link SSHKeyPair}
     * @throws IOException if key generation fails
     * @throws InterruptedException if process interrupted
     */
    public static SSHKeyPair generateRSAKeyPair(int keySize) throws IOException, InterruptedException {
        return generateKeyPair("rsa", keySize);
    }

    /**
     * Generates an ECDSA SSH key pair with 256 bits key size.
     *
     * @return generated ECDSA {@link SSHKeyPair}
     * @throws IOException if key generation fails
     * @throws InterruptedException if process interrupted
     */
    public static SSHKeyPair generateECDSAKeyPair() throws IOException, InterruptedException {
        return generateKeyPair("ecdsa", 256);
    }

    /**
     * Generates an Ed25519 SSH key pair.
     *
     * @return generated Ed25519 {@link SSHKeyPair}
     * @throws IOException if key generation fails
     * @throws InterruptedException if process interrupted
     */
    public static SSHKeyPair generateEd25519KeyPair() throws IOException, InterruptedException {
        return generateKeyPair("ed25519", 256);
    }
}
