package com.storeconnect.user.service;

import com.storeconnect.user.dto.CustomerDTO;
import com.storeconnect.user.dto.CustomerUpdateRequest;
import com.storeconnect.user.entity.Customer;
import com.storeconnect.user.exception.UserNotFoundException;
import com.storeconnect.user.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(String id) {
        return customerRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public CustomerDTO updateCustomer(String id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        customer.setFirstName(request.firstName().trim());
        customer.setLastName(request.lastName().trim());
        if (request.phone() != null) {
            customer.setPhone(request.phone().trim());
        }
        if (request.address() != null) {
            customer.setAddress(request.address().trim());
        }

        Customer saved = customerRepository.save(customer);
        log.info("Updated customer profile: {}", saved.getId());
        return toDTO(saved);
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
