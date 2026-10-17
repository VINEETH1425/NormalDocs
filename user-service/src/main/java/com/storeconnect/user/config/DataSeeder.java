package com.storeconnect.user.config;

import com.storeconnect.user.entity.Customer;
import com.storeconnect.user.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUserIfNotExists("CUST-ADMIN-01", "Admin", "User", "admin@storeconnect.com", "+91 9999900001", "StoreConnect HQ, CyberCity", "Admin@123", "ADMIN");
        seedUserIfNotExists("CUST-OPR-GGN", "Gurugram", "Operator", "operator.ggn@storeconnect.com", "+91 9999900002", "DLF CyberHub Store", "Operator@123", "STORE_OPERATOR");
        seedUserIfNotExists("CUST-OPR-DEL", "Delhi", "Operator", "operator.del@storeconnect.com", "+91 9999900003", "Connaught Place Flagship", "Operator@123", "STORE_OPERATOR");
        seedUserIfNotExists("CUST-001", "Rahul", "Verma", "rahul.verma@example.com", "+91 9876543210", "Sector 56, Gurugram, Haryana", "Customer@123", "CUSTOMER");
        seedUserIfNotExists("CUST-002", "Priya", "Sharma", "priya.sharma@example.com", "+91 9811223344", "Greater Kailash 1, New Delhi", "Customer@123", "CUSTOMER");
    }

    private void seedUserIfNotExists(String id, String firstName, String lastName, String email, String phone, String address, String rawPassword, String role) {
        if (!customerRepository.existsByEmailIgnoreCase(email)) {
            Customer customer = Customer.builder()
                    .id(id)
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email.toLowerCase())
                    .phone(phone)
                    .address(address)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .role(role)
                    .build();
            customerRepository.save(customer);
            log.info("Seeded user account: {} [Role: {}]", email, role);
        }
    }
}
