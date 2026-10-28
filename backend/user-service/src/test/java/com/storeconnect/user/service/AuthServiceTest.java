package com.storeconnect.user.service;

import com.storeconnect.user.config.JwtUtil;
import com.storeconnect.user.dto.AuthResponse;
import com.storeconnect.user.dto.CustomerDTO;
import com.storeconnect.user.dto.LoginRequest;
import com.storeconnect.user.dto.RegisterRequest;
import com.storeconnect.user.entity.Customer;
import com.storeconnect.user.exception.BadRequestException;
import com.storeconnect.user.exception.UnauthorizedException;
import com.storeconnect.user.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        sampleCustomer = new Customer(
                "CUST-001",
                "Rahul",
                "Sharma",
                "rahul@example.com",
                "+91 9876543210",
                "Sector 29, Gurugram",
                "$2a$10$encodedPasswordHash",
                "CUSTOMER"
        );
        sampleCustomer.setCreatedAt(LocalDateTime.of(2026, 1, 15, 10, 30));
    }

    @Test
    @DisplayName("register: Success creates customer and returns valid AuthResponse")
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest(
                "Rahul",
                "Sharma",
                "rahul@example.com",
                "Secret@123",
                "+91 9876543210",
                "Sector 29, Gurugram"
        );

        when(customerRepository.existsByEmailIgnoreCase("rahul@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("$2a$10$encodedPasswordHash");
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken(anyString(), eq("rahul@example.com"), eq("CUSTOMER"))).thenReturn("mocked.jwt.token");
        when(jwtUtil.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresInMs()).isEqualTo(86400000L);

        CustomerDTO dto = response.getUser();
        assertThat(dto).isNotNull();
        assertThat(dto.getEmail()).isEqualTo("rahul@example.com");
        assertThat(dto.getFirstName()).isEqualTo("Rahul");
        assertThat(dto.getLastName()).isEqualTo("Sharma");
        assertThat(dto.getRole()).isEqualTo("CUSTOMER");

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository, times(1)).save(captor.capture());
        Customer saved = captor.getValue();
        assertThat(saved.getId()).startsWith("CUST-");
        assertThat(saved.getPasswordHash()).isEqualTo("$2a$10$encodedPasswordHash");
    }

    @Test
    @DisplayName("register: Duplicate email throws BadRequestException")
    void testRegister_DuplicateEmail_ThrowsBadRequestException() {
        RegisterRequest request = new RegisterRequest(
                "Rahul",
                "Sharma",
                "rahul@example.com",
                "Secret@123",
                null,
                null
        );

        when(customerRepository.existsByEmailIgnoreCase("rahul@example.com")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.register(request));
        assertThat(ex.getMessage()).contains("Email already registered: rahul@example.com");

        verify(customerRepository, never()).save(any());
        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("login: Valid credentials return AuthResponse")
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("rahul@example.com", "Secret@123");

        when(customerRepository.findByEmailIgnoreCase("rahul@example.com")).thenReturn(Optional.of(sampleCustomer));
        when(passwordEncoder.matches("Secret@123", sampleCustomer.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateToken(sampleCustomer.getId(), sampleCustomer.getEmail(), sampleCustomer.getRole()))
                .thenReturn("mocked.login.jwt.token");
        when(jwtUtil.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mocked.login.jwt.token");
        assertThat(response.getUser().getId()).isEqualTo("CUST-001");
        assertThat(response.getUser().getEmail()).isEqualTo("rahul@example.com");
    }

    @Test
    @DisplayName("login: Unknown email throws UnauthorizedException")
    void testLogin_EmailNotFound_ThrowsUnauthorizedException() {
        LoginRequest request = new LoginRequest("nonexistent@example.com", "Password@123");

        when(customerRepository.findByEmailIgnoreCase("nonexistent@example.com")).thenReturn(Optional.empty());

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.login(request));
        assertThat(ex.getMessage()).isEqualTo("Invalid email or password");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("login: Wrong password throws UnauthorizedException")
    void testLogin_WrongPassword_ThrowsUnauthorizedException() {
        LoginRequest request = new LoginRequest("rahul@example.com", "WrongSecretPassword");

        when(customerRepository.findByEmailIgnoreCase("rahul@example.com")).thenReturn(Optional.of(sampleCustomer));
        when(passwordEncoder.matches("WrongSecretPassword", sampleCustomer.getPasswordHash())).thenReturn(false);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.login(request));
        assertThat(ex.getMessage()).isEqualTo("Invalid email or password");

        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("toDTO: Maps all entity attributes to CustomerDTO faithfully")
    void testToDTO_MapsAllAttributes() {
        CustomerDTO dto = authService.toDTO(sampleCustomer);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("CUST-001");
        assertThat(dto.getFirstName()).isEqualTo("Rahul");
        assertThat(dto.getLastName()).isEqualTo("Sharma");
        assertThat(dto.getEmail()).isEqualTo("rahul@example.com");
        assertThat(dto.getPhone()).isEqualTo("+91 9876543210");
        assertThat(dto.getAddress()).isEqualTo("Sector 29, Gurugram");
        assertThat(dto.getRole()).isEqualTo("CUSTOMER");
        assertThat(dto.getCreatedAt()).isEqualTo(sampleCustomer.getCreatedAt());
    }
}

