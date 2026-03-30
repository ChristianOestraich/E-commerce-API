package project.ecommerce.service;

import project.ecommerce.dto.OrderResponse;
import project.ecommerce.dto.UserResponse;
import project.ecommerce.dto.UserUpdateRequest;
import project.ecommerce.entity.Order;
import project.ecommerce.entity.User;
import project.ecommerce.entity.enums.Role;
import project.ecommerce.repository.OrderRepository;
import project.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    // Lista usuarios ativos com paginacao e busca por nome/email
    public Page<UserResponse> findAll(Pageable pageable, String search) {
        if (search != null && !search.isBlank()) {
            return userRepository
                    .findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndEmailContainingIgnoreCase(
                            search, search, pageable)
                    .map(this::toResponse);
        }
        return userRepository.findByActiveTrue(pageable)
                .map(this::toResponse);
    }

    public UserResponse findById(Long id) {
        return toResponse(userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado.")));
    }

    public List<UserResponse> findAllIncludingInactive() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado."));
        user.setName(request.getName());
        user.setRole(request.getRole());
        return toResponse(userRepository.save(user));
    }

    public void deactivate(Long id) {
        User user = userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado."));
        user.setActive(false);
        userRepository.save(user);
    }

    // Toggle ativar/inativar
    @Transactional
    public UserResponse toggleActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado."));
        user.setActive(!user.getActive());
        return toResponse(userRepository.save(user));
    }

    // Altera role do usuario
    @Transactional
    public UserResponse updateRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado."));
        user.setRole(role);
        return toResponse(userRepository.save(user));
    }

    public Page<OrderResponse> getUserOrders(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado."));

        return orderRepository.findByUser(user, pageable)
                .map(order -> {
                    OrderResponse r = new OrderResponse();
                    r.setId(order.getId());
                    r.setStatus(order.getStatus());
                    r.setTotal(order.getTotal());
                    r.setDiscount(order.getDiscount());
                    r.setCreatedAt(order.getCreatedAt());
                    r.setActive(order.getActive());
                    return r;
                });
    }

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setActive(user.getActive());
        return response;
    }
}