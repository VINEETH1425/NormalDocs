package com.storeconnect.payment.service;

import com.storeconnect.payment.dto.PaymentRequest;
import com.storeconnect.payment.dto.PaymentResponse;
import com.storeconnect.payment.entity.Payment;
import com.storeconnect.payment.entity.PaymentStatus;
import com.storeconnect.payment.exception.PaymentAlreadyProcessedException;
import com.storeconnect.payment.exception.PaymentNotFoundException;
import com.storeconnect.payment.kafka.PaymentEventProducer;
import com.storeconnect.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final Random random = new Random();

    public PaymentService(PaymentRepository paymentRepository, PaymentEventProducer paymentEventProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentEventProducer = paymentEventProducer;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing simulated payment for order {} (amount: {})", request.orderId(), request.amount());

        if (paymentRepository.existsByOrderId(request.orderId())) {
            Payment existing = paymentRepository.findByOrderId(request.orderId()).orElseThrow();
            if (existing.getStatus() == PaymentStatus.SUCCESS) {
                throw new PaymentAlreadyProcessedException(request.orderId());
            }
        }

        // Simulate gateway latency
        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

        // 95% success rate in simulation (fail only if amount exceeds 1,000,000 or 5% random failure)
        boolean isSuccess = request.amount().compareTo(new BigDecimal("1000000")) <= 0 && random.nextInt(100) < 95;

        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String txnId = "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Payment payment = Payment.builder()
                .id(paymentId)
                .orderId(request.orderId())
                .transactionId(txnId)
                .amount(request.amount())
                .status(isSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .failureReason(isSuccess ? null : "Payment gateway declined transaction (simulated failure)")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment processed: {} [Status: {}]", saved.getTransactionId(), saved.getStatus());

        if (isSuccess) {
            paymentEventProducer.publishPaymentSuccess(saved.getId(), saved.getOrderId(), saved.getAmount());
        } else {
            paymentEventProducer.publishPaymentFailed(saved.getId(), saved.getOrderId(), saved.getAmount(), saved.getFailureReason());
        }

        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(this::toDTO)
                .orElseThrow(() -> new PaymentNotFoundException("orderId: " + orderId));
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(String id) {
        return paymentRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new PaymentNotFoundException("id: " + id));
    }

    public PaymentResponse toDTO(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getTransactionId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getFailureReason(),
                payment.getCreatedAt()
        );
    }
}
