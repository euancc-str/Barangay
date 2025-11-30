package org.example.Users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.UserInterface;

import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class Person extends UserInterface {
    String sex;
    String name;
    String lastName;
    String middleName;
    String suffix;
    String gender;
    String photoPath;
    String purok;
    String street;
    String contactNo;
    int age;
    LocalDate dob;
    String civilStatus;
    String address;
    String username;
    public Person(Person other) {
        this.name = name;
        this.lastName = other.lastName;
        this.middleName = other.middleName;
        this.suffix = other.suffix;
        this.age = other.age;
        this.dob = other.dob;
        this.civilStatus = other.civilStatus;
        this.address = other.address;
    }

    public String getFullName() {
        return name + " " +
                (middleName != null ? middleName + " " : "") +
                lastName;
    }
}
