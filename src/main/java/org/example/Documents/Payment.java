package org.example.Documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Payment {
    private int paymentId;         // PK
    private int requestId;         // FK
    private double amount;
    private String paymentMethod;  // Cash, GCash, etc.
    private String orNumber;
    private LocalDateTime datePaid;
    private String cashier;
    private double discount;
    private String status;
    private String remarks;
    private String referenceNo;
    private String transactionId;
    private String paymentChannel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public Payment generateData(){
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        return Payment.builder()
                .transactionId("TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .orNumber(timestamp)
                .paymentChannel("Over-the-Counter")
                .build();
    }
}