package Learning.Management.System.service;

import Learning.Management.System.dto.UserRegistrationDTO;
import Learning.Management.System.dto.UserResponseDTO;
import Learning.Management.System.dto.LearnerOptionDTO;
import Learning.Management.System.entity.Role;
import Learning.Management.System.entity.User;
import Learning.Management.System.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepo.findAll(pageable)
                .map(this::mapUser);
    }

    public Page<LearnerOptionDTO> getLearners(Pageable pageable) {
        return userRepo.findByRole(Role.LEARNER, pageable)
                .map(user -> LearnerOptionDTO.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .build());
    }

    public UserResponseDTO registerUser(UserRegistrationDTO dto) {
        if(userRepo.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
        }

        User user = User.builder()
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .role(dto.getRole())
                .build();

        User saved = userRepo.save(user);

        return mapUser(saved);
    }

    private UserResponseDTO mapUser(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
