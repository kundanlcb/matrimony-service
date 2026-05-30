package com.cm.matrimony_service.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cm.matrimony_service.biodata.Biodata;
import com.cm.matrimony_service.biodata.BiodataRepository;
import com.cm.matrimony_service.biodata.Gender;
import com.cm.matrimony_service.interaction.InteractionRepository;
import com.cm.matrimony_service.match.MatchCriteria;
import com.cm.matrimony_service.match.MatchCriteriaRepository;
import com.cm.matrimony_service.user.User;
import com.cm.matrimony_service.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MatrimonyApiIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BiodataRepository biodataRepository;

	@Autowired
	private MatchCriteriaRepository criteriaRepository;

	@Autowired
	private InteractionRepository interactionRepository;

	@BeforeEach
	void cleanDatabase() {
		interactionRepository.deleteAll();
		criteriaRepository.deleteAll();
		biodataRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void authFlowCreatesUserBiodataCriteriaAndReturnsToken() throws Exception {
		mockMvc.perform(post("/api/v1/auth/request-otp")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("mobileNumber", "+919999999999"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("success"))
			.andExpect(jsonPath("$.expiresInSeconds").value(300));

		mockMvc.perform(post("/api/v1/auth/verify-otp")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("mobileNumber", "+919999999999", "otp", "123456"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("success"))
			.andExpect(jsonPath("$.token").isNotEmpty())
			.andExpect(jsonPath("$.user.registrationStep").value("biodata"))
			.andExpect(jsonPath("$.user.preferredLanguage").value("en"));

		User user = userRepository.findByMobileNumber("+919999999999").orElseThrow();
		org.assertj.core.api.Assertions.assertThat(biodataRepository.findByUserId(user.getId())).isPresent();
		org.assertj.core.api.Assertions.assertThat(criteriaRepository.findByUserId(user.getId())).isPresent();
	}

	@Test
	void protectedEndpointsRequireBearerToken() throws Exception {
		mockMvc.perform(get("/api/v1/biodata/me"))
			.andExpect(status().isForbidden());
	}

	@Test
	void biodataCanBePatchedAndCompletedOnlyWhenRequiredFieldsExist() throws Exception {
		String token = login("+919111111111");

		mockMvc.perform(post("/api/v1/biodata/me/complete").header("Authorization", bearer(token)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("error"))
			.andExpect(jsonPath("$.missingFields").isArray());

		mockMvc.perform(patch("/api/v1/biodata/me")
				.header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of(
					"fullName", "Rahul Jha",
					"gender", "Male",
					"age", 29,
					"gotra", "Kashyap",
					"profession", "Software Engineer",
					"annualIncome", 1800000,
					"location", "Darbhanga",
					"education", "B.Tech",
					"photoUrl", "https://cdn.example.test/profile.jpg",
					"interests", List.of("Reading", "Travel")))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.fullName").value("Rahul Jha"))
			.andExpect(jsonPath("$.gender").value("Male"))
			.andExpect(jsonPath("$.interests", hasSize(2)));

		mockMvc.perform(post("/api/v1/biodata/me/complete").header("Authorization", bearer(token)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.registrationStep").value("completed"));
	}

	@Test
	void matchesRespectCriteriaSortingAndInteractionExclusions() throws Exception {
		User active = createUserWithProfile("+919222222222", "Active User", Gender.MALE, 30, "Kashyap", "Patna", "Engineer", 900000L);
		User candidateOne = createUserWithProfile("+919333333333", "Sneha Mishra", Gender.FEMALE, 26, "Vatsa", "Patna", "Doctor", 1400000L);
		createUserWithProfile("+919444444444", "Puja Jha", Gender.FEMALE, 32, "Vatsa", "Delhi", "Engineer", 2200000L);
		User passed = createUserWithProfile("+919555555555", "Ignored User", Gender.FEMALE, 25, "Vatsa", "Patna", "Doctor", 3000000L);

		String token = loginExisting(active.getMobileNumber());
		mockMvc.perform(post("/api/v1/interactions")
				.header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("toUserId", passed.getId(), "type", "passed"))))
			.andExpect(status().isOk());

		mockMvc.perform(put("/api/v1/matches/criteria")
				.header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of(
					"minAge", 24,
					"maxAge", 30,
					"minIncome", 1000000,
					"allowedGotras", List.of("Vatsa"),
					"allowedLocations", List.of("Patna")))))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/matches")
				.header("Authorization", bearer(token))
				.param("sortBy", "income"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content", hasSize(1)))
			.andExpect(jsonPath("$.content[0].id").value(candidateOne.getId().toString()))
			.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	void mutualInterestConvertsBothSidesToAcceptedMatch() throws Exception {
		User first = createUserWithProfile("+919666666666", "First", Gender.MALE, 28, "Kashyap", "Patna", "Engineer", 1000000L);
		User second = createUserWithProfile("+919777777777", "Second", Gender.FEMALE, 26, "Vatsa", "Patna", "Doctor", 1100000L);
		String firstToken = loginExisting(first.getMobileNumber());
		String secondToken = loginExisting(second.getMobileNumber());

		mockMvc.perform(post("/api/v1/interactions")
				.header("Authorization", bearer(firstToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("toUserId", second.getId(), "type", "interest_sent"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isMutualMatch").value(false));

		mockMvc.perform(get("/api/v1/interactions/received")
				.header("Authorization", bearer(secondToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].fullName").value("First"));

		mockMvc.perform(post("/api/v1/interactions")
				.header("Authorization", bearer(secondToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("toUserId", first.getId(), "type", "interest_sent"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isMutualMatch").value(true));

		org.assertj.core.api.Assertions.assertThat(interactionRepository.findAll())
			.extracting(interaction -> interaction.getType().name())
			.containsOnly("MATCH_ACCEPTED");
	}

	@Test
	void uploadEndpointReturnsUserScopedUrl() throws Exception {
		String token = login("+918888888888");

		mockMvc.perform(get("/api/v1/upload/presigned-url")
				.header("Authorization", bearer(token))
				.param("fileName", "../profile photo.jpg")
				.param("contentType", "image/jpeg"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.uploadUrl").value(org.hamcrest.Matchers.containsString("contentType=image%2Fjpeg")))
			.andExpect(jsonPath("$.fileUrl").value(org.hamcrest.Matchers.containsString("profile_photo.jpg")));
	}

	private User createUserWithProfile(String mobileNumber, String name, Gender gender, int age, String gotra,
		String location, String profession, Long income) {
		User user = userRepository.save(new User(mobileNumber));
		user.setVerified(true);
		user = userRepository.save(user);
		Biodata biodata = new Biodata(user);
		biodata.setFullName(name);
		biodata.setGender(gender);
		biodata.setAge(age);
		biodata.setGotra(gotra);
		biodata.setLocation(location);
		biodata.setProfession(profession);
		biodata.setAnnualIncome(income);
		biodata.setMaritalStatus("Never Married");
		biodata.setDiet("Vegetarian");
		biodata.setPhotoUrl("https://cdn.example.test/%s.jpg".formatted(user.getId()));
		biodataRepository.save(biodata);
		criteriaRepository.save(new MatchCriteria(user));
		return user;
	}

	private String login(String mobileNumber) throws Exception {
		return loginExisting(mobileNumber);
	}

	private String loginExisting(String mobileNumber) throws Exception {
		mockMvc.perform(post("/api/v1/auth/request-otp")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("mobileNumber", mobileNumber))))
			.andExpect(status().isOk());
		MvcResult result = mockMvc.perform(post("/api/v1/auth/verify-otp")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("mobileNumber", mobileNumber, "otp", "123456"))))
			.andExpect(status().isOk())
			.andReturn();
		JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
		return node.get("token").asText();
	}

	private String bearer(String token) {
		return "Bearer " + token;
	}

	private String json(Object value) throws Exception {
		return objectMapper.writeValueAsString(value);
	}
}
