package com.example.loginauthapi.repositories;


import com.example.loginauthapi.domain.shiftpass.ShiftPass;
import com.example.loginauthapi.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShiftPassRepository extends JpaRepository<ShiftPass, Long> {

    // Query to find ShiftPasses where the given user appears in any role
    @Query("SELECT sp FROM ShiftPass sp WHERE :user MEMBER OF sp.offeredUsers AND sp.active = true")
    List<ShiftPass> findByOfferedUsersContaining(@Param("user") User user);

}
