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

public class KeyGenerator {
    public static SSHKeyPair RegisterKeypair() throws IOException, InterruptedException {
        return generateEd25519KeyPair();
    }

    @ApplicationScoped
    public static class KeyHasher {
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

    public static class SSHKeyPair {
        private final String privateKey;
        private final String publicKey;

        public SSHKeyPair(String privateKey, String publicKey) {
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        public String getPrivateKey() { return privateKey; }
        public String getPublicKey() { return publicKey; }
    }

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

    public static SSHKeyPair generateRSAKeyPair(int keySize) throws IOException, InterruptedException {
        return generateKeyPair("rsa", keySize);
    }

    public static SSHKeyPair generateECDSAKeyPair() throws IOException, InterruptedException {
        return generateKeyPair("ecdsa", 256);
    }

    public static SSHKeyPair generateEd25519KeyPair() throws IOException, InterruptedException {
        return generateKeyPair("ed25519", 256);
    }
}
