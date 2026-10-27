package Learning.Management.System.controller;


import Learning.Management.System.dto.UserRegistrationDTO;
import Learning.Management.System.dto.UserResponseDTO;
import Learning.Management.System.repository.UserRepo;
import Learning.Management.System.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRegistrationDTO dto) {
        return ResponseEntity.ok(userService.registerUser(dto));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUserLegacy(@Valid @RequestBody UserRegistrationDTO dto) {
        return registerUser(dto);
    }
}
