package project.ecommerce.dto;

import lombok.Data;

@Data
public class AddressResponse {
    private Long id;
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;
    private String zipCode;
    private Boolean main;
}
