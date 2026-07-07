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
import com.cm.matrimony_service.biodata.Biodata;
import com.cm.matrimony_service.biodata.BiodataRepository;
import com.cm.matrimony_service.match.MatchCriteria;
import com.cm.matrimony_service.match.MatchCriteriaRepository;
import com.cm.matrimony_service.subscription.Subscription;
import com.cm.matrimony_service.subscription.SubscriptionRepository;
import com.cm.matrimony_service.interaction.InteractionRepository;
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
    private final BiodataRepository biodataRepository;
    private final MatchCriteriaRepository matchCriteriaRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InteractionRepository interactionRepository;

    public record CreateMasterDataRequest(@NotBlank String type, @NotBlank String name) {}
    public record CreateUserRequest(@NotBlank @jakarta.validation.constraints.Email String email, @NotBlank String password, Boolean verified) {}
    public record UpdateUserRequest(
        @NotBlank @jakarta.validation.constraints.Email String email, 
        Boolean verified, 
        String password,
        String fullName,
        String gender,
        Integer age,
        String gotra,
        String mool,
        String dateOfBirth,
        String birthTime,
        String birthPlace,
        String religion,
        String caste,
        String profession,
        Long annualIncome,
        String education,
        String location,
        String height,
        String maritalStatus,
        String diet,
        String complexion,
        String fatherName,
        String motherName,
        String siblingsDetail,
        String aboutMe,
        String phoneNumber
    ) {}

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

    public record AdminUserResponse(
        User user,
        @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"user", "addresses"})
        Biodata biodata,
        @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"user"})
        MatchCriteria matchCriteria,
        @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"user"})
        List<Subscription> subscriptions
    ) {}

    @GetMapping({"/users", "/users/"})
    public List<AdminUserResponse> getUsers() {
        List<User> users = userRepository.findAll();
        List<Biodata> biodatas = biodataRepository.findAll();
        List<MatchCriteria> matchCriterias = matchCriteriaRepository.findAll();
        List<Subscription> subscriptions = subscriptionRepository.findAll();

        return users.stream().map(user -> {
            Biodata userBiodata = biodatas.stream()
                .filter(b -> b.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElse(null);
            MatchCriteria userMatchCriteria = matchCriterias.stream()
                .filter(m -> m.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElse(null);
            List<Subscription> userSubs = subscriptions.stream()
                .filter(s -> s.getUser().getId().equals(user.getId()))
                .sorted((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()))
                .toList();
            return new AdminUserResponse(user, userBiodata, userMatchCriteria, userSubs);
        }).toList();
    }

    @PostMapping({"/users", "/users/"})
    @Transactional
    public User createUser(@Valid @RequestBody CreateUserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "User with this email already exists");
        }
        User user = new User(request.email());
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
        
        if (!user.getEmail().equals(request.email()) && 
            userRepository.findByEmail(request.email()).isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email already in use");
        }

        user.setEmail(request.email());
        if (request.verified() != null) {
            user.setVerified(request.verified());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        userRepository.save(user);

        biodataRepository.findByUserId(user.getId()).ifPresent(biodata -> {
            if (request.fullName() != null) biodata.setFullName(request.fullName());
            if (request.gender() != null && !request.gender().isBlank()) {
                try {
                    biodata.setGender(com.cm.matrimony_service.biodata.Gender.valueOf(request.gender()));
                } catch (IllegalArgumentException e) {
                    // ignore invalid
                }
            }
            if (request.age() != null) biodata.setAge(request.age());
            if (request.gotra() != null) biodata.setGotra(request.gotra());
            if (request.mool() != null) biodata.setMool(request.mool());
            if (request.dateOfBirth() != null) biodata.setDateOfBirth(request.dateOfBirth());
            if (request.birthTime() != null) biodata.setBirthTime(request.birthTime());
            if (request.birthPlace() != null) biodata.setBirthPlace(request.birthPlace());
            if (request.religion() != null) biodata.setReligion(request.religion());
            if (request.caste() != null) biodata.setCaste(request.caste());
            if (request.profession() != null) biodata.setProfession(request.profession());
            if (request.annualIncome() != null) biodata.setAnnualIncome(request.annualIncome());
            if (request.education() != null) biodata.setEducation(request.education());
            if (request.location() != null) biodata.setLocation(request.location());
            if (request.height() != null) biodata.setHeight(request.height());
            if (request.maritalStatus() != null) biodata.setMaritalStatus(request.maritalStatus());
            if (request.diet() != null) biodata.setDiet(request.diet());
            if (request.complexion() != null) biodata.setComplexion(request.complexion());
            if (request.fatherName() != null) biodata.setFatherName(request.fatherName());
            if (request.motherName() != null) biodata.setMotherName(request.motherName());
            if (request.siblingsDetail() != null) biodata.setSiblingsDetail(request.siblingsDetail());
            if (request.aboutMe() != null) biodata.setAboutMe(request.aboutMe());
            if (request.phoneNumber() != null) biodata.setPhoneNumber(request.phoneNumber());
            
            biodataRepository.save(biodata);
        });

        return user;
    }

    @DeleteMapping("/users/{id}")
    @Transactional
    public void deleteUser(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        interactionRepository.deleteAllByUserId(id);
        subscriptionRepository.findByUserIdOrderByCreatedAtDesc(id).forEach(subscriptionRepository::delete);
        matchCriteriaRepository.findByUserId(id).ifPresent(matchCriteriaRepository::delete);
        biodataRepository.findByUserId(id).ifPresent(biodataRepository::delete);

        userRepository.delete(user);
    }
}
