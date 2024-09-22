package com.example.loginauthapi.services;

import com.example.loginauthapi.domain.location.Location;
import com.example.loginauthapi.domain.user.User;
import com.example.loginauthapi.repositories.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LocationService {

    @Autowired
    LocationRepository locationRepository;

    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }

    public List<Location> findByUser(User user) {
        return locationRepository.findActiveByUser(user);
    }

    public Location save(Location location) {
        return locationRepository.save(location);
    }

    public void deleteById(Long id) {
        locationRepository.deleteById(id);
    }
}
