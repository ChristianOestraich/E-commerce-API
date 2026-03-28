package project.ecommerce.service;

import project.ecommerce.dto.UserResponse;
import project.ecommerce.dto.UserUpdateRequest;
import project.ecommerce.entity.User;
import project.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> findAll() {
        return userRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse findById(Long id) {
        return toResponse(userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado.")));
    }

    public List<UserResponse> findAllIncludingInactive() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        user.setName(request.getName());
        user.setRole(request.getRole());

        return toResponse(userRepository.save(user));
    }

    public void deactivate(Long id) {
        User user = userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        user.setActive(false);
        userRepository.save(user);
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setActive(user.getActive());
        return response;
    }
}