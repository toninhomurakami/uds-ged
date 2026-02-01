package br.com.uds.tools.ged.web.controller;

import br.com.uds.tools.ged.service.UserService;
import br.com.uds.tools.ged.dto.InitialSetupRequest;
import br.com.uds.tools.ged.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class SetupController {

    private final UserService userService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> status() {
        boolean needsSetup = userService.count() == 0;
        return ResponseEntity.ok(Map.of("needsSetup", needsSetup));
    }

    @PostMapping("/initial-admin")
    public ResponseEntity<UserResponse> createInitialAdmin(@Valid @RequestBody InitialSetupRequest request) {
        UserResponse created = userService.createInitialAdmin(
                request.getName(),
                request.getUsername(),
                request.getPassword()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @ExceptionHandler({ IllegalStateException.class, IllegalArgumentException.class })
    public ResponseEntity<Map<String, String>> handleSetupException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "Requisição inválida"));
    }
}
