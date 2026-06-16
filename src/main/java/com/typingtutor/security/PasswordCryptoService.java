package com.typingtutor.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;

/**
 * Encrypts/decrypts login and password-change payloads so they never travel as plaintext
 * in the request body (defense-in-depth on top of TLS — guards against browser extensions,
 * intercepting proxies, or anyone capturing a screen/network log of the request).
 *
 * Keypair is generated fresh on each server start and kept in memory only; the frontend
 * fetches the current public key immediately before every encrypt call, so a restart never
 * leaves clients holding a stale key.
 */
@Component
public class PasswordCryptoService {

    private static final Logger log = LoggerFactory.getLogger(PasswordCryptoService.class);
    private static final String OAEP_HASH = "SHA-256";

    private KeyPair keyPair;

    @PostConstruct
    void generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            this.keyPair = generator.generateKeyPair();
            log.info("[CRYPTO] RSA keypair generated for password transport encryption");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA algorithm unavailable", e);
        }
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    /** Decrypts a Base64 RSA-OAEP(SHA-256) ciphertext produced by the frontend's Web Crypto call. */
    public String decrypt(String base64Ciphertext) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                    OAEP_HASH, "MGF1", new MGF1ParameterSpec(OAEP_HASH), PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate(), oaepParams);
            byte[] plain = cipher.doFinal(Base64.getDecoder().decode(base64Ciphertext));
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not decrypt password payload. Please retry.");
        }
    }
}
