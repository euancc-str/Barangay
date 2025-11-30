package org.example.Users;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class Resident extends Person {
    private int residentId;        // PK
    private String firstName;
    private String gender;
    private String username;
    private String contactNo;
    private String photoPath;
    private String email;
    @ToString.Exclude
    private String password;
    private int age;
    private String phoneNumber;
    private String voterStatus;
    private String ctcNumber;
    private String householdNo;
    private String nationalId;
    private int photoID;
    private String ctcDateIssued;
    private String position;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;


}