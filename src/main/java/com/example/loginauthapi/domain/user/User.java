package com.example.loginauthapi.domain.user;

import com.example.loginauthapi.domain.location.Location;
import com.example.loginauthapi.domain.shift.Shift;
import com.example.loginauthapi.domain.shiftpass.ShiftPass;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @JsonIgnoreProperties("user")
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Shift> shifts;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Location> locations;

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ShiftPass> createdShiftPasses;

    @ManyToMany(mappedBy = "offeredUsers", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<ShiftPass> offeredShiftPasses;


}
