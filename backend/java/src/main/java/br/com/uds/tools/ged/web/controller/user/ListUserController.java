package br.com.uds.tools.ged.web.controller.user;

import br.com.uds.tools.ged.dto.UserResponse;
import br.com.uds.tools.ged.facade.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ListUserController {

    private final UserFacade userFacade;

    @GetMapping
    public ResponseEntity<List<UserResponse>> list() {
        return ResponseEntity.ok(userFacade.findAll());
    }
}
