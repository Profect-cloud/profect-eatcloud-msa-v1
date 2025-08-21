package com.eatcloud.authservice.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * 내부 시스템 간 통신에 사용하는 단명 Passport JWT 발급 및 JWKS 제공 서비스.
 */
@Service
public class PassportTokenService {

    private final Map<String, RSAKey> keyIdToRsaKey = new ConcurrentHashMap<>();
    private volatile String currentKeyId;

    public PassportTokenService() {
        rotateKey();
    }

    /**
     * 키 로테이션: 새 RSA 키를 생성하고 current kid를 교체한다.
     */
    public synchronized void rotateKey() {
        try {
            RSAKey rsaJWK = new RSAKeyGenerator(2048)
                .keyUse(com.nimbusds.jose.jwk.KeyUse.SIGNATURE)
                .keyID(UUID.randomUUID().toString())
                .generate();
            keyIdToRsaKey.put(rsaJWK.getKeyID(), rsaJWK);
            currentKeyId = rsaJWK.getKeyID();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA key for passport", e);
        }
    }

    public String issuePassport(String subjectUserId, List<String> roles, long ttlSeconds) {
        Objects.requireNonNull(subjectUserId, "subjectUserId");
        if (roles == null) roles = List.of();

        RSAKey rsaKey = keyIdToRsaKey.get(currentKeyId);
        if (rsaKey == null) throw new IllegalStateException("No active RSA key");

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlSeconds);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
            .subject(subjectUserId)
            .issuer("auth-service")
            .audience("services")
            .claim("typ", "passport")
            .claim("roles", roles)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(exp))
            .jwtID(UUID.randomUUID().toString())
            .build();

        try {
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(rsaKey.getKeyID())
                .type(JOSEObjectType.JWT)
                .build();

            SignedJWT signedJWT = new SignedJWT(header, claims);
            JWSSigner signer = new RSASSASigner(rsaKey.toPrivateKey());
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign passport JWT", e);
        }
    }

    /**
     * 공개키 세트를 JWKS로 반환한다.
     */
    public Map<String, Object> getJwks() {
        List<JWK> publicKeys = keyIdToRsaKey.values().stream()
            .map(RSAKey::toPublicJWK)
            .map(k -> (JWK) k)
            .toList();
        return new JWKSet(publicKeys).toJSONObject();
    }
}


