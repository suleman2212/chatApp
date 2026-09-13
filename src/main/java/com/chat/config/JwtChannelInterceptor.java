package com.chat.config;

import com.chat.service.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * Validates the JWT on the STOMP CONNECT frame (browsers can't attach an
 * Authorization header to the raw SockJS handshake, so it has to be checked
 * here instead of at the HTTP layer). On success, the verified username is
 * stored in the STOMP session attributes and becomes the source of truth for
 * "who is this" for the rest of the session -- the client can no longer just
 * claim to be someone else in the message payload.
 */
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public JwtChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Missing or malformed Authorization header");
            }

            String token = authHeader.substring(7);

            if (!jwtService.isTokenValid(token)) {
                throw new IllegalArgumentException("Invalid or expired token");
            }

            String username = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);

            org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            username, null, java.util.Collections.emptyList()
                    );
            accessor.setUser(authentication);

            if (accessor.getSessionAttributes() != null) {
                accessor.getSessionAttributes().put("username", username);
                if (userId != null) {
                    accessor.getSessionAttributes().put("userId", userId);
                }
            }
        }

        return message;
    }
}
