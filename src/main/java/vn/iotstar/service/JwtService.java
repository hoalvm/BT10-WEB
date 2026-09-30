package vn.iotstar.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;

/**
 * JWT Service dung Nimbus JOSE + JWT (thay the JJWT theo yeu cau bai 4).
 * Thuat toan: HS256 (HMAC-SHA256).
 */
@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    // ----------------------------------------------------------------
    //  Generate
    // ----------------------------------------------------------------

    public String generateToken(UserDetails userDetails) {
        return buildToken(userDetails, jwtExpiration);
    }

    private String buildToken(UserDetails userDetails, long expiration) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(userDetails.getUsername())
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + expiration))
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(new MACSigner(getSecretBytes()));

            return signedJWT.serialize();

        } catch (JOSEException e) {
            throw new RuntimeException("Khong the tao JWT token", e);
        }
    }

    // ----------------------------------------------------------------
    //  Parse / Validate
    // ----------------------------------------------------------------

    public String extractUsername(String token) {
        return getClaimsSet(token).getSubject();
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = getClaimsSet(token).getExpirationTime();
        return expiration != null && expiration.before(new Date());
    }

    /**
     * Parse va verify chu ky JWT, tra ve JWTClaimsSet.
     * Nem RuntimeException neu token khong hop le hoac het han chu ky.
     */
    public JWTClaimsSet getClaimsSet(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            boolean verified = signedJWT.verify(new MACVerifier(getSecretBytes()));
            if (!verified) {
                throw new RuntimeException("JWT signature khong hop le");
            }
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (claims.getExpirationTime() != null && claims.getExpirationTime().before(new Date())) {
                throw new RuntimeException("JWT token da het han");
            }
            return claims;
        } catch (ParseException | JOSEException e) {
            throw new RuntimeException("JWT khong the parse hoac verify: " + e.getMessage(), e);
        }
    }

    // ----------------------------------------------------------------
    //  Key helper
    // ----------------------------------------------------------------

    private byte[] getSecretBytes() {
        // Decode hex string to raw bytes
        String key = secretKey;
        if (key.length() == 64 && key.matches("[0-9a-fA-F]+")) {
            // HEX-encoded 32-byte key
            byte[] bytes = new byte[key.length() / 2];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) Integer.parseInt(key.substring(2 * i, 2 * i + 2), 16);
            }
            return bytes;
        }
        // fallback: raw UTF-8 bytes (phai >= 32 bytes cho HS256)
        return key.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
