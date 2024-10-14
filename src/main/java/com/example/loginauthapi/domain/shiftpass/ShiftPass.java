package com.example.loginauthapi.domain.shiftpass;

import com.example.loginauthapi.domain.shift.Shift;
import com.example.loginauthapi.domain.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ShiftPass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnoreProperties("shifts")
    @JsonBackReference
    private User createdBy;

    @Column(nullable = false)
    private boolean active;

    @ManyToMany
    @JoinTable(
            name = "shift_pass_offered_users",
            joinColumns = @JoinColumn(name = "shift_pass_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonManagedReference
    private List<User> offeredUsers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "final_user")
    @JsonIgnoreProperties("shifts")
    private User finalUser;

    //Original shift data tracking
    @Column(name = "origin_shift_id", nullable = false)
    private Long originalShiftId;
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    @Column(nullable = false)
    private double value;
    @Column(name = "location_name", nullable = false)
    private String locationName;

}
