package com.ddogalmap.global.security.websocket;

import com.ddogalmap.global.security.jwt.JwtTokenProvider;
import com.ddogalmap.global.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;

@Slf4j 	//로그 확인차 4군데 남겨두었습니다. 작성자: 이은성, 작성일시: 260601
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
				// 1.
				log.warn("[WS-CONNECT-FAIL] sessionId={}", accessor.getSessionId());
				throw new AccessDeniedException("유효한 JWT가 필요합니다.");
			}

			Long userId = jwtTokenProvider.getUserId(token);
			//2.
			log.warn("[WS-CONNECT] userId={} sessionId={}", userId, accessor.getSessionId());

			UserPrincipal principal = new UserPrincipal(userId, jwtTokenProvider.getRole(token));
			UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
			accessor.setUser(authentication);
		}

		//3.
		if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
			log.info("[WS-SUBSCRIBE] destination={} sessionId={}", accessor.getDestination(), accessor.getSessionId());
		}
		//4.
		if (StompCommand.SEND.equals(accessor.getCommand())) {

			log.info("[WS-SEND] destination={} sessionId={}", accessor.getDestination(), accessor.getSessionId());
		}


		if ((StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand()))
				&& accessor.getUser() == null) {
			throw new AccessDeniedException("인증된 사용자만 채팅 기능을 사용할 수 있습니다.");
		}

		return message;
	}

    private String resolveToken(StompHeaderAccessor accessor) {
        // 헤더에서 먼저 읽기
        String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }

        // STOMP Authorization 헤더가 없으면 쿠키에서 토큰을 조회한다.
        List<String> cookies = accessor.getNativeHeader("cookie");
        if (cookies != null) {
            for (String cookie : cookies) {
                for (String part : cookie.split(";")) {
                    String trimmed = part.trim();
                    if (trimmed.startsWith("accessToken=")) {
                        return trimmed.substring("accessToken=".length());
                    }
                }
            }
        }

		return null;
	}
}
