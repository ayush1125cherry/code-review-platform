package com.example.codereview.auth;

import com.example.codereview.auth.dto.AuthResponse;
import com.example.codereview.auth.dto.LoginRequest;
import com.example.codereview.auth.dto.SignupRequest;
import com.example.codereview.auth.dto.UserDto;
import com.example.codereview.exception.BadRequestException;
import com.example.codereview.user.User;
import com.example.codereview.user.UserRepository;
import com.example.codereview.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername().toLowerCase().trim())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .avatarUrl("https://api.dicebear.com/7.x/identicon/svg?seed=" + request.getUsername())
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtUtils.generateToken(savedUser.getUsername(), savedUser.getId());
        UserDto userDto = userService.mapToDto(savedUser);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(userDto)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLogin().trim(), request.getPassword())
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtUtils.generateToken(userPrincipal.getUsername(), userPrincipal.getId());
        UserDto userDto = userService.getUserDto(userPrincipal.getId());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(userDto)
                .build();
    }
}
