package Learning.Management.System.controller;

import Learning.Management.System.dto.LoginRequestDTO;
import Learning.Management.System.dto.LoginResponseDTO;
import Learning.Management.System.model.User;
import Learning.Management.System.repository.UserRepo;
import Learning.Management.System.security.CustomerUserDetailsService;
import Learning.Management.System.security.JwtUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

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

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        var userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        var jwtToken = jwtUtils.generateToken(userDetails);
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user was not found"));

        return ResponseEntity.ok(LoginResponseDTO.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build());
    }

    @GetMapping("/users")
    public ResponseEntity<String> getCurrentUser(@Valid Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        return
                ResponseEntity.ok("Authenticated user: " + principal.getName());
    }
}
