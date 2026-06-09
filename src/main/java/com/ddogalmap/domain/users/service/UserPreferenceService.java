package com.ddogalmap.domain.users.service;

import com.ddogalmap.domain.users.dto.request.ChatPreferenceUpdateRequest;
import com.ddogalmap.domain.users.dto.response.ChatPreferenceResponse;
import com.ddogalmap.domain.users.entity.User;
import com.ddogalmap.domain.users.exception.UserNotFoundException;
import com.ddogalmap.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ChatPreferenceResponse getChatPreference(Long userId) {
        User user = getUser(userId);
        return new ChatPreferenceResponse(user.getChatEnabled());
    }

    @Transactional
    public ChatPreferenceResponse updateChatPreference(Long userId, ChatPreferenceUpdateRequest request) {
        if (request.chatEnabled() == null) {
            throw new IllegalArgumentException("채팅 수신 설정 값은 필수입니다.");
        }

        User user = getUser(userId);
        user.updateChatEnabled(request.chatEnabled());
        return new ChatPreferenceResponse(user.getChatEnabled());
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));
    }
}
