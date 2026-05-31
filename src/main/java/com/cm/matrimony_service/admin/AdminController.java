package com.cm.matrimony_service.admin;

import com.cm.matrimony_service.masterdata.City;
import com.cm.matrimony_service.masterdata.CityRepository;
import com.cm.matrimony_service.masterdata.Gotra;
import com.cm.matrimony_service.masterdata.GotraRepository;
import com.cm.matrimony_service.masterdata.Religion;
import com.cm.matrimony_service.masterdata.ReligionRepository;
import com.cm.matrimony_service.masterdata.Caste;
import com.cm.matrimony_service.masterdata.CasteRepository;
import com.cm.matrimony_service.masterdata.Profession;
import com.cm.matrimony_service.masterdata.ProfessionRepository;
import com.cm.matrimony_service.user.RegistrationStep;
import com.cm.matrimony_service.user.User;
import com.cm.matrimony_service.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;
import com.cm.matrimony_service.common.exception.ApiException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CityRepository cityRepository;
    private final GotraRepository gotraRepository;
    private final ReligionRepository religionRepository;
    private final CasteRepository casteRepository;
    private final ProfessionRepository professionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public record CreateMasterDataRequest(@NotBlank String type, @NotBlank String name) {}
    public record CreateUserRequest(@NotBlank String mobileNumber, @NotBlank String password, Boolean verified) {}
    public record UpdateUserRequest(@NotBlank String mobileNumber, Boolean verified, String password) {}

    @GetMapping("/master-data/{type}")
    public List<?> getMasterData(@PathVariable String type) {
        return switch (type.toLowerCase()) {
            case "city" -> cityRepository.findAll();
            case "gotra" -> gotraRepository.findAll();
            case "religion" -> religionRepository.findAll();
            case "caste" -> casteRepository.findAll();
            case "profession" -> professionRepository.findAll();
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid master data type");
        };
    }

    @PostMapping("/master-data")
    public Object createMasterData(@Valid @RequestBody CreateMasterDataRequest request) {
        return switch (request.type().toLowerCase()) {
            case "city" -> cityRepository.save(new City(request.name()));
            case "gotra" -> gotraRepository.save(new Gotra(request.name()));
            case "religion" -> religionRepository.save(new Religion(request.name()));
            case "caste" -> casteRepository.save(new Caste(request.name()));
            case "profession" -> professionRepository.save(new Profession(request.name()));
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid master data type");
        };
    }

    @DeleteMapping("/master-data/{type}/{id}")
    public void deleteMasterData(@PathVariable String type, @PathVariable UUID id) {
        switch (type.toLowerCase()) {
            case "city" -> cityRepository.deleteById(id);
            case "gotra" -> gotraRepository.deleteById(id);
            case "religion" -> religionRepository.deleteById(id);
            case "caste" -> casteRepository.deleteById(id);
            case "profession" -> professionRepository.deleteById(id);
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid master data type");
        }
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/users")
    @Transactional
    public User createUser(@Valid @RequestBody CreateUserRequest request) {
        if (userRepository.findByMobileNumber(request.mobileNumber()).isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "User with this mobile number already exists");
        }
        User user = new User(request.mobileNumber());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setVerified(request.verified() != null ? request.verified() : false);
        user.setRegistrationStep(RegistrationStep.BIODATA);
        return userRepository.save(user);
    }

    @PutMapping("/users/{id}")
    @Transactional
    public User updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        
        if (!user.getMobileNumber().equals(request.mobileNumber()) && 
            userRepository.findByMobileNumber(request.mobileNumber()).isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Mobile number already in use");
        }

        user.setMobileNumber(request.mobileNumber());
        if (request.verified() != null) {
            user.setVerified(request.verified());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        return userRepository.save(user);
    }
}
