package com.cm.matrimony_service.masterdata;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cm.matrimony_service.common.exception.ApiException;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/api/v1/master-data")
@RequiredArgsConstructor
public class MasterDataController {

    private final CityRepository cityRepository;
    private final GotraRepository gotraRepository;
    private final ReligionRepository religionRepository;
    private final CasteRepository casteRepository;
    private final ProfessionRepository professionRepository;

    @GetMapping("/{type}")
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
}
