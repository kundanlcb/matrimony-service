package com.cm.matrimony_service.masterdata;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MasterDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MasterDataLoader.class);

    private final CityRepository cityRepository;
    private final GotraRepository gotraRepository;
    private final ReligionRepository religionRepository;
    private final CasteRepository casteRepository;
    private final ProfessionRepository professionRepository;

    @Override
    public void run(String... args) {
        seedCities();
        seedGotras();
        seedReligions();
        seedCastes();
        seedProfessions();
    }

    private void seedCities() {
        if (cityRepository.count() == 0) {
            log.info("Seeding default cities...");
            List<String> defaultCities = List.of(
                "Darbhanga", "Madhubani", "Patna", "Saharsa", "Samastipur", "Muzaffarpur",
                "Begusarai", "Purnia", "Bhagalpur", "Ranchi", "Delhi NCR", "Bangalore",
                "Mumbai", "Kolkata", "Pune", "Hyderabad"
            );
            defaultCities.forEach(name -> cityRepository.save(new City(name)));
        }
    }

    private void seedGotras() {
        if (gotraRepository.count() == 0) {
            log.info("Seeding default gotras...");
            List<String> defaultGotras = List.of(
                "Kashyap", "Shandilya", "Vatsa", "Bhardwaj", "Parashar", "Katyayan", "Alambayan", "Savarna"
            );
            defaultGotras.forEach(name -> gotraRepository.save(new Gotra(name)));
        }
    }

    private void seedReligions() {
        if (religionRepository.count() == 0) {
            log.info("Seeding default religions...");
            List<String> defaultReligions = List.of(
                "Hindu", "Muslim", "Christian", "Sikh", "Jain", "Buddhist"
            );
            defaultReligions.forEach(name -> religionRepository.save(new Religion(name)));
        }
    }

    private void seedCastes() {
        if (casteRepository.count() == 0) {
            log.info("Seeding default castes...");
            List<String> defaultCastes = List.of(
                "Brahmin (Maithil)", "Kayastha", "Rajput", "Baniya", "Karna Kayastha", "Yadav", "Other"
            );
            defaultCastes.forEach(name -> casteRepository.save(new Caste(name)));
        }
    }

    private void seedProfessions() {
        if (professionRepository.count() == 0) {
            log.info("Seeding default professions...");
            List<String> defaultProfessions = List.of(
                "Software Engineer", "Doctor", "Teacher", "Business", "Government Service",
                "Civil Servant", "Chartered Accountant", "Lawyer", "Professor", "Architect",
                "Defense Services", "Homemaker", "Other"
            );
            defaultProfessions.forEach(name -> professionRepository.save(new Profession(name)));
        }
    }
}
