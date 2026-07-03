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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.cm.matrimony_service.auth.EmailService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MatrimonyApiIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EmailService emailService;

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
				.content(json(Map.of("email", "test99@example.com"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("success"))
			.andExpect(jsonPath("$.expiresInSeconds").value(300));

		mockMvc.perform(post("/api/v1/auth/verify-otp")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("email", "test99@example.com", "otp", "123456"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("success"))
			.andExpect(jsonPath("$.token").isNotEmpty())
			.andExpect(jsonPath("$.user.registrationStep").value("biodata"))
			.andExpect(jsonPath("$.user.preferredLanguage").value("en"));

		User user = userRepository.findByEmail("test99@example.com").orElseThrow();
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
		String token = login("test11@example.com");

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
		User active = createUserWithProfile("test22@example.com", "Active User", Gender.MALE, 30, "Kashyap", "Patna", "Engineer", 900000L);
		User candidateOne = createUserWithProfile("test33@example.com", "Sneha Mishra", Gender.FEMALE, 26, "Vatsa", "Patna", "Doctor", 1400000L);
		createUserWithProfile("test44@example.com", "Puja Jha", Gender.FEMALE, 32, "Vatsa", "Delhi", "Engineer", 2200000L);
		User passed = createUserWithProfile("test55@example.com", "Ignored User", Gender.FEMALE, 25, "Vatsa", "Patna", "Doctor", 3000000L);

		String token = loginExisting(active.getEmail());
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
		User first = createUserWithProfile("test66@example.com", "First", Gender.MALE, 28, "Kashyap", "Patna", "Engineer", 1000000L);
		User second = createUserWithProfile("test77@example.com", "Second", Gender.FEMALE, 26, "Vatsa", "Patna", "Doctor", 1100000L);
		String firstToken = loginExisting(first.getEmail());
		String secondToken = loginExisting(second.getEmail());

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
	void decliningInterestRemovesFromReceivedQueueAndExposesSentAndMatches() throws Exception {
		User first = createUserWithProfile("test88@example.com", "Sender", Gender.MALE, 28, "Kashyap", "Patna", "Engineer", 1000000L);
		User second = createUserWithProfile("test99b@example.com", "Recipient", Gender.FEMALE, 26, "Vatsa", "Patna", "Doctor", 1100000L);
		String firstToken = loginExisting(first.getEmail());
		String secondToken = loginExisting(second.getEmail());

		// 1. First sends interest to second
		mockMvc.perform(post("/api/v1/interactions")
				.header("Authorization", bearer(firstToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("toUserId", second.getId(), "type", "interest_sent"))))
			.andExpect(status().isOk());

		// Verify first has it in sent
		mockMvc.perform(get("/api/v1/interactions/sent")
				.header("Authorization", bearer(firstToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].fullName").value("Recipient"));

		// Verify second has it in received
		mockMvc.perform(get("/api/v1/interactions/received")
				.header("Authorization", bearer(secondToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].fullName").value("Sender"));

		// 2. Second declines the interest from first
		mockMvc.perform(post("/api/v1/interactions")
				.header("Authorization", bearer(secondToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("toUserId", first.getId(), "type", "match_declined"))))
			.andExpect(status().isOk());

		// Verify second no longer has it in received
		mockMvc.perform(get("/api/v1/interactions/received")
				.header("Authorization", bearer(secondToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(0)));

		// Verify first no longer has it in sent since it is declined
		mockMvc.perform(get("/api/v1/interactions/sent")
				.header("Authorization", bearer(firstToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(0)));

		// 3. Make them mutual match to test matches endpoint
		interactionRepository.deleteAll();

		mockMvc.perform(post("/api/v1/interactions")
				.header("Authorization", bearer(firstToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("toUserId", second.getId(), "type", "interest_sent"))))
			.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/interactions")
				.header("Authorization", bearer(secondToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("toUserId", first.getId(), "type", "interest_sent"))))
			.andExpect(status().isOk());

		// Verify mutual match in matches endpoint for first
		mockMvc.perform(get("/api/v1/interactions/matches")
				.header("Authorization", bearer(firstToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].fullName").value("Recipient"));

		// Verify mutual match in matches endpoint for second
		mockMvc.perform(get("/api/v1/interactions/matches")
				.header("Authorization", bearer(secondToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].fullName").value("Sender"));
	}

	@Test
	void uploadEndpointReturnsUserScopedUrl() throws Exception {
		String token = login("test888@example.com");

		mockMvc.perform(get("/api/v1/upload/presigned-url")
				.header("Authorization", bearer(token))
				.param("fileName", "../profile photo.jpg")
				.param("contentType", "image/jpeg"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.uploadUrl").value(org.hamcrest.Matchers.containsString("contentType=image%2Fjpeg")))
			.andExpect(jsonPath("$.fileUrl").value(org.hamcrest.Matchers.containsString("profile_photo.jpg")));
	}

	private User createUserWithProfile(String email, String name, Gender gender, int age, String gotra,
		String location, String profession, Long income) {
		User user = userRepository.save(new User(email));
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

	private String login(String email) throws Exception {
		return loginExisting(email);
	}

	private String loginExisting(String email) throws Exception {
		mockMvc.perform(post("/api/v1/auth/request-otp")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("email", email))))
			.andExpect(status().isOk());
		MvcResult result = mockMvc.perform(post("/api/v1/auth/verify-otp")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("email", email, "otp", "123456"))))
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
