package com.example.loginauthapi.services;

import com.example.loginauthapi.domain.shift.Shift;
import com.example.loginauthapi.domain.user.User;
import com.example.loginauthapi.repositories.ShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShiftService {

    @Autowired
    ShiftRepository shiftRepository;

    public List<Shift> findAll() {
        return shiftRepository.findAll();
    }

    public Optional<Shift> findById(Long id) {
        return shiftRepository.findById(id);
    }

    public List<Shift> findByUser(User user) {
        return shiftRepository.findByUser(user);
    }

    public Shift save(Shift shift) {
        return shiftRepository.save(shift);
    }

    public void deleteById(Long id) {
        shiftRepository.deleteById(id);
    }

    public boolean setPassing(Shift shift, boolean passing) {
        try {
            shift.setPassing(passing);
            shiftRepository.save(shift);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
