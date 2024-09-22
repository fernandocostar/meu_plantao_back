package com.example.loginauthapi.repositories;

import com.example.loginauthapi.domain.location.Location;
import com.example.loginauthapi.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    @Query("SELECT l FROM Location l WHERE l.active = true AND l.user = :user")
    List<Location> findActiveByUser(@Param("user") User user);

}
