package com.typingtutor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordCryptoServiceTest {

    private PasswordCryptoService cryptoService;

    @BeforeEach
    void setUp() {
        cryptoService = new PasswordCryptoService();
        cryptoService.generateKeyPair();
    }

    @Test
    void decryptsCiphertextEncryptedWithThePublishedPublicKey() throws Exception {
        String plaintext = "New@Password1234";
        String ciphertext = encryptWithPublishedKey(plaintext);

        assertThat(cryptoService.decrypt(ciphertext)).isEqualTo(plaintext);
    }

    @Test
    void rejectsGarbageCiphertext() {
        assertThatThrownBy(() -> cryptoService.decrypt("not-valid-base64-ciphertext"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /** Mirrors what the frontend's Web Crypto RSA-OAEP(SHA-256) call produces. */
    private String encryptWithPublishedKey(String plaintext) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(cryptoService.getPublicKeyBase64());
        PublicKey publicKey = KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                "SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), PSource.PSpecified.DEFAULT);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams);
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(ciphertext);
    }
}
