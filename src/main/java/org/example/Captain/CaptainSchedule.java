package org.example.Captain;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
public class CaptainSchedule {
    private int scheduleId;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int captainId;
    private boolean isAvailable;
    private String dayOfWeek;
    private String captainName;

    // Post-construct method to set dayOfWeek
    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
        this.dayOfWeek = scheduleDate.getDayOfWeek().toString();
    }
}
