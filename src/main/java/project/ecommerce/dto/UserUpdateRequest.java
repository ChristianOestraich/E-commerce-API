package project.ecommerce.dto;

import project.ecommerce.entity.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotBlank
    private String name;
    @NotNull
    private Role role;
}
