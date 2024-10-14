package com.example.loginauthapi.services;

import com.example.loginauthapi.domain.shift.Shift;
import com.example.loginauthapi.domain.shiftpass.ShiftPass;
import com.example.loginauthapi.domain.user.User;
import com.example.loginauthapi.repositories.ShiftPassRepository;
import com.example.loginauthapi.repositories.ShiftRepository;
import com.example.loginauthapi.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Service
public class ShiftPassService {

    @Autowired
    ShiftRepository shiftRepository;

    @Autowired
    ShiftPassRepository shiftPassRepository;

    @Autowired
    UserRepository userRepository;

    public Optional<ShiftPass> findById(Long id) {
        return shiftPassRepository.findById(id);
    }

    public void save(ShiftPass shiftPass) {
        shiftPassRepository.save(shiftPass);
    }

    public void delete(ShiftPass shiftPass) {
        shiftPassRepository.delete(shiftPass);
    }

    public ShiftPass createShiftPass(Long shiftId, String userEmail, List<User> offeredUsers) {

        try {
            Optional<Shift> optionalShift = shiftRepository.findById(shiftId);
            Optional<User> optionalUser = userRepository.findByEmail(userEmail);
            if (optionalUser.isEmpty() || optionalShift.isEmpty()) {
                return null;
            }

            Shift shift = optionalShift.get();
            User user = optionalUser.get();

            ShiftPass newShiftPass = new ShiftPass();

            //set passing basic info
            newShiftPass.setCreatedBy(user);
            newShiftPass.setActive(true);
            newShiftPass.setOfferedUsers(emptyList());
            newShiftPass.setFinalUser(null);

            //copy original shift info
            newShiftPass.setOriginalShiftId(shift.getId());
            newShiftPass.setStartTime(shift.getStartTime());
            newShiftPass.setEndTime(shift.getEndTime());
            newShiftPass.setValue(shift.getValue());
            newShiftPass.setLocationName(shift.getLocation().getName());

            List<User> offeredUsersCopy = new ArrayList<>(offeredUsers);
            newShiftPass.setOfferedUsers(offeredUsersCopy);

            shift.setPassing(true);
            shiftRepository.save(shift);

            return shiftPassRepository.save(newShiftPass);
        } catch (Exception e) { //TODO work on exception handling
            return null;
        }
    }

    @Transactional
    public void updateOriginalShiftAndShiftPassAndCreateNewShift(ShiftPass shiftPass, Shift newShift, Shift originalShift) {
        try {
            shiftRepository.save(newShift);
            shiftRepository.delete(originalShift);
            shiftPassRepository.save(shiftPass);
        } catch (Exception e) {
            //TODO work on exception handling
            throw e;
        }
    }

    @Transactional
    public void deleteShiftPassAndUpdateShift(ShiftPass shiftPass, Shift shift) {
        try {
            shiftPassRepository.delete(shiftPass);
            shift.setPassing(false);
            shiftRepository.save(shift);
        } catch (Exception e) {
            //TODO work on exception handling
            throw e;
        }
    }

}
