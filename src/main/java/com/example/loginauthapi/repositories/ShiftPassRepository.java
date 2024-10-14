package com.example.loginauthapi.repositories;


import com.example.loginauthapi.domain.shiftpass.ShiftPass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftPassRepository extends JpaRepository<ShiftPass, Long> {

}
