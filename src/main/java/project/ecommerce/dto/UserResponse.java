package project.ecommerce.dto;

import project.ecommerce.entity.enums.Role;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private Boolean active;
}