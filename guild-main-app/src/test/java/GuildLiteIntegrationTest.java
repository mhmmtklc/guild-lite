import com.guildlite.GuildLiteApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(classes = GuildLiteApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GuildLiteIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String USER_1_USERNAME = "Tester-1";
    private static final String USER_1_EMAIL = "tester1@test.com";
    private static final String USER_1_PASSWORD = "test1pass135";

    private static final String USER_2_USERNAME = "Tester-2";
    private static final String USER_2_EMAIL = "tester2@test.com";
    private static final String USER_2_PASSWORD = "test2pass135";

    private static String teamId;

    private static String authToken1;
    private static String teamUserToken1;

    private static String authToken2;
    private static String teamUserToken2;


    private String baseUrl() {
        return "http://localhost:" + port;
    }


    @Test
    @Order(1)
    @DisplayName("AuthController: register")
    void registerAndSaveToken_test() {
        Map<String, String> register = Map.of(
                "username", USER_1_USERNAME,
                "email", USER_1_EMAIL,
                "password", USER_1_PASSWORD
        );

        ResponseEntity<Map> regRes = restTemplate.postForEntity(baseUrl() + "/api/auth/register", register, Map.class);
        assertEquals(201, regRes.getStatusCode().value());
        assertNotNull(regRes.getBody());
        assertTrue(regRes.getBody().containsKey("token"));
        authToken1 = (String) regRes.getBody().get("token");
        assertNotNull(authToken1);
    }

    @Test
    @Order(2)
    @DisplayName("AuthController: login with email & username")
    void loginAndSaveToken_test() {
        Map<String, String> loginWithUsername = Map.of(
                "usernameOrEmail", USER_1_USERNAME,
                "password", USER_1_PASSWORD
        );

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(baseUrl() + "/api/auth/login", loginWithUsername, Map.class);
        assertEquals(200, loginResponse.getStatusCode().value());
        assertNotNull(loginResponse.getBody());
        assertTrue(loginResponse.getBody().containsKey("token"));
        authToken1 = (String) loginResponse.getBody().get("token");
        assertNotNull(authToken1);

        Map<String, String> loginWithEmail = Map.of(
                "usernameOrEmail", USER_1_EMAIL,
                "password", USER_1_PASSWORD
        );


        loginResponse = restTemplate.postForEntity(baseUrl() + "/api/auth/login", loginWithEmail, Map.class);
        assertEquals(200, loginResponse.getStatusCode().value());
        assertNotNull(loginResponse.getBody());
        assertTrue(loginResponse.getBody().containsKey("token"));
        authToken1 = (String) loginResponse.getBody().get("token");
        assertNotNull(authToken1);
    }

    @Test
    @Order(3)
    @DisplayName("TeamController: create team with auth token, save new token")
    void teamCreateAndSaveToken_test() {
        assertNotNull(authToken1, "authToken must be set by registration test");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken1);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> teamReq = Map.of(
                "name", "Team " + System.currentTimeMillis(),
                "description", "Test team"
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(teamReq, headers);

        ResponseEntity<Map> res = restTemplate.exchange(
                baseUrl() + "/api/teams/create",
                HttpMethod.POST,
                entity,
                Map.class
        );

        assertEquals(201, res.getStatusCode().value());
        assertNotNull(res.getBody());
        assertTrue(res.getBody().containsKey("newToken"));
        assertTrue(res.getBody().containsKey("team"));

        teamUserToken1 = (String) res.getBody().get("newToken");
        assertNotNull(teamUserToken1);

        Object teamObj = res.getBody().get("team");
        assertNotNull(teamObj);

        @SuppressWarnings("unchecked")
        Map<String, Object> teamMap = (Map<String, Object>) teamObj;
        teamId = String.valueOf(teamMap.get("id"));
        assertNotNull(teamId);
    }

    @Test
    @Order(4)
    @DisplayName("TeamController: authorized get team with team token")
    void teamGetAuthorizedWithTeamToken_test() {
        assertNotNull(teamUserToken1);
        assertNotNull(teamId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(teamUserToken1);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> res = restTemplate.exchange(
                baseUrl() + "/api/teams/" + teamId + "/get",
                HttpMethod.GET,
                entity,
                Map.class
        );

        assertEquals(200, res.getStatusCode().value());
        assertNotNull(res.getBody());
        assertEquals(teamId, String.valueOf(res.getBody().get("id")));
    }

    @Test
    @Order(5)
    @DisplayName("CoinController: authorized get balance with team token")
    void coinBalanceControl_test() {
        assertNotNull(teamUserToken1);
        assertNotNull(teamId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(teamUserToken1);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> res = restTemplate.exchange(
                baseUrl() + "/api/coins/" + teamId + "/get-balance",
                HttpMethod.GET,
                entity,
                Map.class
        );

        assertEquals(200, res.getStatusCode().value());
        assertNotNull(res.getBody());
        assertTrue(Boolean.TRUE.equals(res.getBody().get("success")) || res.getBody().get("success") == null);
    }

    @Test
    @Order(6)
    @DisplayName("TeamController: unauthorized get team without token")
    void teamGetUnauthorizedWithoutToken_test() {
        assertNotNull(teamId);
        ResponseEntity<String> res = restTemplate.getForEntity(baseUrl() + "/api/teams/" + teamId + "/get", String.class);
        assertTrue(res.getStatusCode() == HttpStatus.UNAUTHORIZED || res.getStatusCode() == HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(7)
    @DisplayName("TeamController: unauthorized create without token")
    void teamCreateUnauthorizedWithoutToken_test() {
        Map<String, String> teamReq = Map.of(
                "name", "NoToken Team " + System.currentTimeMillis(),
                "description", "Test team without token"
        );
        ResponseEntity<String> res = restTemplate.postForEntity(baseUrl() + "/api/teams/create", teamReq, String.class);
        assertTrue(res.getStatusCode() == HttpStatus.UNAUTHORIZED || res.getStatusCode() == HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(8)
    @DisplayName("CoinController: unauthorized get balance without token")
    void coinBalanceControlUnauthorizedWithoutToken_test() {
        assertNotNull(teamId);
        ResponseEntity<String> res = restTemplate.getForEntity(baseUrl() + "/api/coins/" + teamId + "/get-balance", String.class);
        assertTrue(res.getStatusCode() == HttpStatus.UNAUTHORIZED || res.getStatusCode() == HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(9)
    @DisplayName("ChatController: unauthorized history without token")
    void chatHistoryUnauthorizedWithoutToken_test() {
        ResponseEntity<String> res = restTemplate.getForEntity(baseUrl() + "/api/chat/history", String.class);
        assertTrue(res.getStatusCode() == HttpStatus.UNAUTHORIZED || res.getStatusCode() == HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(10)
    @DisplayName("TeamController: register second user and save token")
    void secondUserRegisterAndSaveToken_test() {
        Map<String, String> register = Map.of(
                "username", USER_2_USERNAME,
                "email", USER_2_EMAIL,
                "password", USER_2_PASSWORD
        );

        ResponseEntity<Map> regRes = restTemplate.postForEntity(baseUrl() + "/api/auth/register", register, Map.class);
        assertEquals(201, regRes.getStatusCode().value());
        assertNotNull(regRes.getBody());
        assertTrue(regRes.getBody().containsKey("token"));
        authToken2 = (String) regRes.getBody().get("token");
        assertNotNull(authToken2);
    }

    @Test
    @Order(11)
    @DisplayName("TeamController: second user joins first user's team and saves new token")
    void secondUserJoinsTeamAndSavesNewToken_test() {
        assertNotNull(teamId);
        assertNotNull(authToken2);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken2);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> res = restTemplate.exchange(
                baseUrl() + "/api/teams/" + teamId + "/join",
                HttpMethod.POST,
                entity,
                Map.class
        );

        assertEquals(200, res.getStatusCode().value());
        assertNotNull(res.getBody());
        assertTrue(res.getBody().containsKey("newToken"));
        assertTrue(res.getBody().containsKey("team"));

        teamUserToken2 = (String) res.getBody().get("newToken");
        assertNotNull(teamUserToken2);

        Object teamObj = res.getBody().get("team");
        assertNotNull(teamObj);

        @SuppressWarnings("unchecked")
        Map<String, Object> teamMap = (Map<String, Object>) teamObj;
        assertEquals(teamId, String.valueOf(teamMap.get("id")));
    }

}
