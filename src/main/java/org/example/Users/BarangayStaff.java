package org.example.Users;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class BarangayStaff extends Person {
    private String staffId;           // PK
    private String firstName;
    private String lastName;
    private String position;
    private String contactNo;
    private String email;
    private String username;
    @ToString.Exclude
    private String password;       // Encrypted
    private String role;           // Admin, Captain, Official, Resident
    private String status;
    private String department;
    private LocalDateTime lastLogin;
    private String assignedZone;
    private LocalDateTime createdAt;
    private String citizenship;
    private LocalDateTime updatedAt;
    private String idNumber;

}
