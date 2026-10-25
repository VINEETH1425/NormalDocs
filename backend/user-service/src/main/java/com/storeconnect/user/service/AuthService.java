package com.storeconnect.user.service;

import com.storeconnect.user.config.JwtUtil;
import com.storeconnect.user.dto.AuthResponse;
import com.storeconnect.user.dto.CustomerDTO;
import com.storeconnect.user.dto.LoginRequest;
import com.storeconnect.user.dto.RegisterRequest;
import com.storeconnect.user.entity.Customer;
import com.storeconnect.user.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (customerRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        String id = "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Customer customer = new Customer(
                id,
                request.getFirstName(),
                request.getLastName(),
                request.getEmail().toLowerCase().trim(),
                request.getPhone(),
                request.getAddress(),
                passwordEncoder.encode(request.getPassword()),
                "CUSTOMER"
        );

        Customer saved = customerRepository.save(customer);
        String token = jwtUtil.generateToken(saved.getId(), saved.getEmail(), saved.getRole());

        return new AuthResponse(token, "Bearer", jwtUtil.getExpirationMs(), toDTO(saved));
    }

    public AuthResponse login(LoginRequest request) {
        Customer customer = customerRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(customer.getId(), customer.getEmail(), customer.getRole());

        return new AuthResponse(token, "Bearer", jwtUtil.getExpirationMs(), toDTO(customer));
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
