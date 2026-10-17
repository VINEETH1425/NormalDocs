package com.storeconnect.user.service;

import com.storeconnect.user.config.JwtUtil;
import com.storeconnect.user.dto.AuthResponse;
import com.storeconnect.user.dto.CustomerDTO;
import com.storeconnect.user.dto.LoginRequest;
import com.storeconnect.user.dto.RegisterRequest;
import com.storeconnect.user.entity.Customer;
import com.storeconnect.user.exception.DuplicateEmailException;
import com.storeconnect.user.exception.InvalidCredentialsException;
import com.storeconnect.user.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(CustomerRepository customerRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (customerRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException(email);
        }

        String id = "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String hashedPassword = passwordEncoder.encode(request.password());

        Customer customer = Customer.builder()
                .id(id)
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(email)
                .phone(request.phone() != null ? request.phone().trim() : null)
                .address(request.address() != null ? request.address().trim() : null)
                .passwordHash(hashedPassword)
                .role("CUSTOMER")
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Registered new customer: {} ({})", saved.getEmail(), saved.getId());

        CustomerDTO dto = toDTO(saved);
        String token = jwtUtil.generateToken(saved.getId(), saved.getEmail(), saved.getRole());

        return AuthResponse.of(token, jwtUtil.getExpirationMs(), dto);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        Customer customer = customerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), customer.getPasswordHash())) {
            log.warn("Failed login attempt for email: {}", email);
            throw new InvalidCredentialsException();
        }

        log.info("Customer logged in successfully: {} ({})", customer.getEmail(), customer.getId());

        CustomerDTO dto = toDTO(customer);
        String token = jwtUtil.generateToken(customer.getId(), customer.getEmail(), customer.getRole());

        return AuthResponse.of(token, jwtUtil.getExpirationMs(), dto);
    }

    public CustomerDTO toDTO(Customer customer) {
        return new CustomerDTO(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getRole(),
                customer.getCreatedAt()
        );
    }
}
