package com.example.loginauthapi.dto.shiftpass;

import com.example.loginauthapi.domain.shiftpass.ShiftPass;
import com.example.loginauthapi.domain.user.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ShiftPassResponse {

    public ShiftPassResponse(ShiftPass shiftPass) {

        this.id = shiftPass.getId();
        this.createdBy = getShiftPassEssentialUserDetails(shiftPass.getCreatedBy());
        this.active = shiftPass.isActive();
        this.offeredUsers = shiftPass.getOfferedUsers().stream()
                .map(this::getShiftPassEssentialUserDetails)
                .toList();
        this.finalUser = shiftPass.getFinalUser() != null ? getShiftPassEssentialUserDetails(shiftPass.getFinalUser()) : null;
        this.originalShiftId = shiftPass.getOriginalShiftId();
        this.startTime = shiftPass.getStartTime();
        this.endTime = shiftPass.getEndTime();
        this.value = shiftPass.getValue();
        this.locationName = shiftPass.getLocationName();

    }

    private long id;
    private User createdBy;

    private boolean active;
    private List<User> offeredUsers;
    private User finalUser;

    //Original shift data tracking
    private Long originalShiftId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double value;
    private String locationName;

    public User getShiftPassEssentialUserDetails(User user) {
        User essentialUserDetails = new User();
        essentialUserDetails.setId(user.getId());
        essentialUserDetails.setEmail(user.getEmail());
        essentialUserDetails.setName(user.getName());
        return essentialUserDetails;
    }

}
