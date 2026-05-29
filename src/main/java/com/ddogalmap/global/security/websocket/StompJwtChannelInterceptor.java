package com.ddogalmap.global.security.websocket;

import com.ddogalmap.global.security.jwt.JwtTokenProvider;
import com.ddogalmap.global.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class StompJwtChannelInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = resolveToken(accessor);
            if (!StringUtils.hasText(token) || !jwtTokenProvider.validateToken(token)) {
                throw new AccessDeniedException("유효한 JWT가 필요합니다.");
            }

            Long userId = jwtTokenProvider.getUserId(token);
            UserPrincipal principal = new UserPrincipal(userId, jwtTokenProvider.getRole(token));
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            accessor.setUser(authentication);
        }

        if ((StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand()))
                && accessor.getUser() == null) {
            throw new AccessDeniedException("인증된 사용자만 채팅 기능을 사용할 수 있습니다.");
        }

        return message;
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        return authHeader.substring(BEARER_PREFIX.length());
    }
}
