package Learning.Management.System.controller;

import Learning.Management.System.dto.LoginRequestDTO;
import Learning.Management.System.dto.LoginResponseDTO;
import Learning.Management.System.dto.PasswordChangeRequestDTO;
import Learning.Management.System.dto.UserResponseDTO;
import Learning.Management.System.entity.User;
import Learning.Management.System.repository.UserRepo;
import Learning.Management.System.security.CustomerUserDetailsService;
import Learning.Management.System.security.JwtUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomerUserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        var userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        var jwtToken = jwtUtils.generateToken(userDetails);
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user was not found"));

        return ResponseEntity.ok(LoginResponseDTO.builder()
                .token(jwtToken)
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build());
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Principal principal) {
        User user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user was not found"));
        return ResponseEntity.ok(UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build());
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody PasswordChangeRequestDTO request, Principal principal) {
        User user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user was not found"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        if (request.getNewPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password is too long");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a different password");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    public ResponseEntity<String> getAuthenticatedUserMessage(@Valid Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        return
                ResponseEntity.ok("Authenticated user: " + principal.getName());
    }
}
