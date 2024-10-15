package com.example.loginauthapi.dto;

import com.example.loginauthapi.domain.shiftpass.ShiftPass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OfferedShiftPassesResponse {

    private Long id;
    private Long originalShiftId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isActive;
    private String locationName;
    private double value;

    public OfferedShiftPassesResponse(ShiftPass shiftPass) {
        this.id = shiftPass.getId();
        this.originalShiftId = shiftPass.getOriginalShiftId();
        this.startTime = shiftPass.getStartTime();
        this.endTime = shiftPass.getEndTime();
        this.isActive = shiftPass.isActive();
        this.value = shiftPass.getValue();
        this.locationName = shiftPass.getLocationName();
    }

}
