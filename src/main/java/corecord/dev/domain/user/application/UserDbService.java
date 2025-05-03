package corecord.dev.domain.user.application;

import corecord.dev.common.exception.GeneralException;
import corecord.dev.common.status.ErrorStatus;
import corecord.dev.domain.user.domain.entity.User;
import corecord.dev.domain.user.domain.enums.Provider;
import corecord.dev.domain.user.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDbService {
    private final UserRepository userRepository;

    @Transactional
    public void updateUserTmpChat(User user, Long chatRoomId) {
        user.updateTmpChat(chatRoomId);
    }

    @Transactional
    public void deleteUserTmpChat(User user) {
        user.deleteTmpChat();
    }

    @Transactional
    public void deleteUserByUserId(Long userId) {
        userRepository.deleteUserByUserId(userId);
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public boolean existsByProviderIdAndProvider(String providerId, Provider provider) {
        return userRepository.existsByProviderIdAndProvider(providerId, provider);
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.UNAUTHORIZED));
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.UNAUTHORIZED));
    }

}
