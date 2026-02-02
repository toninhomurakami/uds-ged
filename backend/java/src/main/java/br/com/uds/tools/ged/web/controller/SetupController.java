package br.com.uds.tools.ged.web.controller;

import br.com.uds.tools.ged.dto.InitialSetupRequest;
import br.com.uds.tools.ged.dto.UserResponse;
import br.com.uds.tools.ged.facade.UserFacade;
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

    private final UserFacade userFacade;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> status() {
        boolean needsSetup = userFacade.count() == 0;
        return ResponseEntity.ok(Map.of("needsSetup", needsSetup));
    }

    @PostMapping("/initial-admin")
    public ResponseEntity<UserResponse> createInitialAdmin(@Valid @RequestBody InitialSetupRequest request) {
        UserResponse created = userFacade.createInitialAdmin(
                request.getName(),
                request.getUsername(),
                request.getPassword()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
