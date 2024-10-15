package com.example.loginauthapi.domain.user;

import com.example.loginauthapi.domain.location.Location;
import com.example.loginauthapi.domain.shift.Shift;
import com.example.loginauthapi.domain.shiftpass.ShiftPass;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private String id;

    private String name;
    private String email;
    private String professionalRegister;
    private String city;
    private String state;
    private int professionalType;

    @JsonIgnore
    private String password;

    // Ignore circular reference when serializing shifts
    @JsonIgnoreProperties("user")
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Shift> shifts;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Location> locations;

    // Ignore the reference to user in ShiftPass to prevent recursion
    @JsonIgnoreProperties({"createdBy", "offeredUsers", "finalUser"})
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private List<ShiftPass> createdShiftPasses;

    // Use @JsonIgnore here to avoid recursion with offered shift passes
    @JsonIgnore
    @ManyToMany(mappedBy = "offeredUsers", fetch = FetchType.LAZY)
    private List<ShiftPass> offeredShiftPasses;
}
