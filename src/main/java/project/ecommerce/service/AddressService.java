package project.ecommerce.service;

import project.ecommerce.dto.AddressRequest;
import project.ecommerce.dto.AddressResponse;
import project.ecommerce.entity.Address;
import project.ecommerce.entity.User;
import project.ecommerce.repository.AddressRepository;
import project.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressResponse create(String email, AddressRequest request) {
        User user = getUser(email);

        // Se for marcado como principal, remove o principal anterior
        if (Boolean.TRUE.equals(request.getMain())) {
            addressRepository.findByUserAndMainTrue(user)
                    .ifPresent(a -> {
                        a.setMain(false);
                        addressRepository.save(a);
                    });
        }

        Address address = Address.builder()
                .user(user)
                .street(request.getStreet())
                .number(request.getNumber())
                .complement(request.getComplement())
                .neighborhood(request.getNeighborhood())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .main(Boolean.TRUE.equals(request.getMain()))
                .build();

        return toResponse(addressRepository.save(address));
    }

    public List<AddressResponse> findAll(String email) {
        User user = getUser(email);
        return addressRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AddressResponse findById(String email, Long id) {
        User user = getUser(email);
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Endereco nao encontrado."));
        return toResponse(address);
    }

    public AddressResponse update(String email, Long id, AddressRequest request) {
        User user = getUser(email);
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Endereco nao encontrado."));

        if (Boolean.TRUE.equals(request.getMain())) {
            addressRepository.findByUserAndMainTrue(user)
                    .ifPresent(a -> {
                        if (!a.getId().equals(id)) {
                            a.setMain(false);
                            addressRepository.save(a);
                        }
                    });
        }

        address.setStreet(request.getStreet());
        address.setNumber(request.getNumber());
        address.setComplement(request.getComplement());
        address.setNeighborhood(request.getNeighborhood());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());
        address.setMain(Boolean.TRUE.equals(request.getMain()));

        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public void delete(String email, Long id) {
        User user = getUser(email);
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Endereco nao encontrado."));
        addressRepository.delete(address);
    }

    @Transactional
    public AddressResponse setMain(String email, Long id) {
        User user = getUser(email);

        // Remove principal anterior
        addressRepository.findByUserAndMainTrue(user)
                .ifPresent(a -> {
                    a.setMain(false);
                    addressRepository.save(a);
                });

        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Endereco nao encontrado."));

        address.setMain(true);
        return toResponse(addressRepository.save(address));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado."));
    }

    public AddressResponse toResponse(Address address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setStreet(address.getStreet());
        response.setNumber(address.getNumber());
        response.setComplement(address.getComplement());
        response.setNeighborhood(address.getNeighborhood());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setZipCode(address.getZipCode());
        response.setMain(address.getMain());
        return response;
    }
}
