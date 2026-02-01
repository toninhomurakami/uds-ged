package br.com.uds.tools.ged.service;

import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.security.JwtService;
import br.com.uds.tools.ged.dto.LoginRequest;
import br.com.uds.tools.ged.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        User user = userService.getByUsername(request.getUsername());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }
        String token = jwtService.generateToken(user);
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole());
    }
}
