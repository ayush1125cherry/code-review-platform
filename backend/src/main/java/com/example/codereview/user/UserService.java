package com.example.codereview.user;

import com.example.codereview.auth.dto.ChangePasswordRequest;
import com.example.codereview.auth.dto.UpdateProfileRequest;
import com.example.codereview.auth.dto.UserDto;
import com.example.codereview.exception.BadRequestException;
import com.example.codereview.exception.ResourceNotFoundException;
import com.example.codereview.github.GitHubAccount;
import com.example.codereview.github.GitHubAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GitHubAccountRepository gitHubAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserDto getUserDto(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToDto(user);
    }

    @Transactional
    public UserDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setName(request.getName());
        // Email is permanent once created and cannot be edited
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            if (request.getEmail() != null && !request.getEmail().isBlank()) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    throw new BadRequestException("Email is already taken");
                }
                user.setEmail(request.getEmail().trim());
            }
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getGeminiApiKey() != null) {
            user.setGeminiApiKey(request.getGeminiApiKey().trim().isEmpty() ? null : request.getGeminiApiKey().trim());
        }

        User updated = userRepository.save(user);
        return mapToDto(updated);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Incorrect current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public UserDto mapToDto(User user) {
        Optional<GitHubAccount> ghAccount = gitHubAccountRepository.findFirstByUserOrderByIdDesc(user);
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl() != null ? user.getAvatarUrl() : (ghAccount.map(GitHubAccount::getAvatarUrl).orElse(null)))
                .githubConnected(ghAccount.isPresent())
                .githubUsername(ghAccount.map(GitHubAccount::getGithubUsername).orElse(null))
                .hasCustomGeminiKey(user.getGeminiApiKey() != null && !user.getGeminiApiKey().isBlank())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
