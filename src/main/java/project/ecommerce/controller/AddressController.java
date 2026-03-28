package project.ecommerce.controller;

import project.ecommerce.dto.AddressRequest;
import project.ecommerce.dto.AddressResponse;
import project.ecommerce.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> create(Authentication auth,
                                                  @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.create(auth.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> findAll(Authentication auth) {
        return ResponseEntity.ok(addressService.findAll(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> findById(Authentication auth,
                                                    @PathVariable Long id) {
        return ResponseEntity.ok(addressService.findById(auth.getName(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> update(Authentication auth,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.update(auth.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth,
                                       @PathVariable Long id) {
        addressService.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/main")
    public ResponseEntity<AddressResponse> setMain(Authentication auth,
                                                   @PathVariable Long id) {
        return ResponseEntity.ok(addressService.setMain(auth.getName(), id));
    }
}