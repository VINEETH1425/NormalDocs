package com.storeconnect.user.config;

import com.storeconnect.user.entity.Customer;
import com.storeconnect.user.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!customerRepository.existsByEmailIgnoreCase("admin@storeconnect.com")) {
            customerRepository.save(new Customer(
                    "CUST-ADMIN-01",
                    "Admin",
                    "User",
                    "admin@storeconnect.com",
                    "+91 9999900001",
                    "StoreConnect HQ, CyberCity",
                    passwordEncoder.encode("Admin@123"),
                    "ADMIN"
            ));
        }

        if (!customerRepository.existsByEmailIgnoreCase("operator.ggn@storeconnect.com")) {
            customerRepository.save(new Customer(
                    "CUST-OPR-GGN",
                    "Gurugram",
                    "Operator",
                    "operator.ggn@storeconnect.com",
                    "+91 9999900002",
                    "DLF CyberHub Store",
                    passwordEncoder.encode("Operator@123"),
                    "STORE_OPERATOR"
            ));
        }

        if (!customerRepository.existsByEmailIgnoreCase("rahul.verma@example.com")) {
            customerRepository.save(new Customer(
                    "CUST-001",
                    "Rahul",
                    "Verma",
                    "rahul.verma@example.com",
                    "+91 9876543210",
                    "Sector 56, Gurugram, Haryana",
                    passwordEncoder.encode("Customer@123"),
                    "CUSTOMER"
            ));
        }
    }
}
