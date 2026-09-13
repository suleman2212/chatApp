package com.chat;

import com.chat.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    @Test
    void testTokenGenerationAndValidation() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");

        String token = jwtService.generateToken("testuser", 42L);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertEquals("testuser", jwtService.extractUsername(token));
        assertEquals(42L, jwtService.extractUserId(token));
    }
}
