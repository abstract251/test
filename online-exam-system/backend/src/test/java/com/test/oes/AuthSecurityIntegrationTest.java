package com.test.oes;

import com.test.oes.cache.ExamCacheFacade;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.oes.runtime.RuntimeControlService;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "DB_HOST=localhost",
        "DB_PORT=3306",
        "DB_NAME=online_exam",
        "DB_USERNAME=root",
        "DB_PASSWORD=root"
})
class AuthSecurityIntegrationTest {

    private static final String DB_ROUTE_PRIMARY = "primary";

    private static final int ADMIN_ID = 9991;
    private static final int TEACHER_ID = 20081001;
    private static final int STUDENT_ID = 20224001;
    private static final int OTHER_STUDENT_ID = 20224084;
    private static final int EXAM_CODE = 21990001;
    private static final int HIDDEN_EXAM_CODE = 21990002;
    private static final int PAPER_ID = 1001;
    private static final int TEST_MESSAGE_ID = 991001;
    private static final int TEST_MESSAGE_ID_2 = 991002;
    private static final int TEST_MESSAGE_ID_3 = 991003;
    private static final int TEST_REPLAY_ID = 992001;
    private static final int TEST_REPLAY_ID_2 = 992002;

    private static final String ADMIN_PASSWORD = "123456";
    private static final String TEACHER_PASSWORD = "123456";
    private static final String STUDENT_PASSWORD = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Optional<StringRedisTemplate> stringRedisTemplate;

    @Autowired
    private ExamCacheFacade examCacheFacade;

    @Autowired
    private RuntimeControlService runtimeControlService;

    private boolean examAttemptTableExists;
    private boolean asyncEventOutboxTableExists;
    private boolean asyncProjectionTableExists;
    private boolean runtimeToggleTableExists;

    @BeforeEach
    void setUpAccounts() {
        ensureRuntimeControlTables();
        examAttemptTableExists = hasTable("exam_attempt");
        asyncEventOutboxTableExists = hasTable("async_event_outbox");
        asyncProjectionTableExists = hasTable("exam_score_statistics_projection");
        runtimeToggleTableExists = hasTable("runtime_feature_toggle");
        jdbcTemplate.update("UPDATE admin SET pwd = ?, role = '0' WHERE adminId = ?",
                passwordEncoder.encode(ADMIN_PASSWORD), ADMIN_ID);
        jdbcTemplate.update("UPDATE teacher SET pwd = ?, role = '1' WHERE teacherId = ?",
                passwordEncoder.encode(TEACHER_PASSWORD), TEACHER_ID);
        jdbcTemplate.update("UPDATE student SET pwd = ?, role = '2' WHERE studentId = ?",
                passwordEncoder.encode(STUDENT_PASSWORD), STUDENT_ID);
        jdbcTemplate.update("UPDATE student SET role = '2' WHERE studentId = ?", OTHER_STUDENT_ID);
        jdbcTemplate.update("DELETE FROM auth_refresh_token WHERE username IN (?, ?, ?)",
                String.valueOf(ADMIN_ID), String.valueOf(TEACHER_ID), String.valueOf(STUDENT_ID));
        jdbcTemplate.update("DELETE FROM replay WHERE replayId IN (?, ?)", TEST_REPLAY_ID, TEST_REPLAY_ID_2);
        jdbcTemplate.update("DELETE FROM message WHERE id IN (?, ?, ?)", TEST_MESSAGE_ID, TEST_MESSAGE_ID_2, TEST_MESSAGE_ID_3);
        if (examAttemptTableExists) {
            jdbcTemplate.update("DELETE FROM exam_attempt WHERE exam_code IN (?, ?) ", EXAM_CODE, HIDDEN_EXAM_CODE);
        }
        jdbcTemplate.update("DELETE FROM exam_shared_snapshot_item WHERE exam_code IN (?, ?)", EXAM_CODE, HIDDEN_EXAM_CODE);
        jdbcTemplate.update("DELETE FROM exam_shared_snapshot WHERE exam_code IN (?, ?)", EXAM_CODE, HIDDEN_EXAM_CODE);
        jdbcTemplate.update("DELETE FROM score WHERE examCode IN (?, ?)", EXAM_CODE, HIDDEN_EXAM_CODE);
        if (asyncEventOutboxTableExists) {
            jdbcTemplate.update("DELETE FROM async_event_outbox WHERE aggregate_id IN (?, ?, ?)",
                    String.valueOf(EXAM_CODE), EXAM_CODE + ":" + STUDENT_ID, EXAM_CODE + ":" + OTHER_STUDENT_ID);
            jdbcTemplate.update("DELETE FROM async_event_consume_record WHERE event_id NOT IN ('__keep__')");
        }
        if (asyncProjectionTableExists) {
            jdbcTemplate.update("DELETE FROM exam_score_statistics_projection WHERE exam_code IN (?, ?)", EXAM_CODE, HIDDEN_EXAM_CODE);
        }
        if (runtimeToggleTableExists) {
            jdbcTemplate.update("DELETE FROM runtime_feature_toggle");
            jdbcTemplate.update("DELETE FROM runtime_feature_toggle_audit");
            runtimeControlService.refreshSnapshot();
        }
        jdbcTemplate.update("DELETE FROM exam_manage WHERE examCode IN (?, ?)", EXAM_CODE, HIDDEN_EXAM_CODE);
        ensurePaperQuestions();
        clearExamCaches();
        insertVisibleExam();
    }

    @AfterEach
    void cleanUpExamData() {
        if (examAttemptTableExists) {
            jdbcTemplate.update("DELETE FROM exam_attempt WHERE exam_code IN (?, ?) ", EXAM_CODE, HIDDEN_EXAM_CODE);
        }
        jdbcTemplate.update("DELETE FROM replay WHERE replayId IN (?, ?)", TEST_REPLAY_ID, TEST_REPLAY_ID_2);
        jdbcTemplate.update("DELETE FROM message WHERE id IN (?, ?, ?)", TEST_MESSAGE_ID, TEST_MESSAGE_ID_2, TEST_MESSAGE_ID_3);
        jdbcTemplate.update("DELETE FROM exam_shared_snapshot_item WHERE exam_code IN (?, ?)", EXAM_CODE, HIDDEN_EXAM_CODE);
        jdbcTemplate.update("DELETE FROM exam_shared_snapshot WHERE exam_code IN (?, ?)", EXAM_CODE, HIDDEN_EXAM_CODE);
        jdbcTemplate.update("DELETE FROM score WHERE examCode IN (?, ?)", EXAM_CODE, HIDDEN_EXAM_CODE);
        if (asyncEventOutboxTableExists) {
            jdbcTemplate.update("DELETE FROM async_event_outbox WHERE aggregate_id IN (?, ?, ?)",
                    String.valueOf(EXAM_CODE), EXAM_CODE + ":" + STUDENT_ID, EXAM_CODE + ":" + OTHER_STUDENT_ID);
            jdbcTemplate.update("DELETE FROM async_event_consume_record WHERE event_id NOT IN ('__keep__')");
        }
        if (asyncProjectionTableExists) {
            jdbcTemplate.update("DELETE FROM exam_score_statistics_projection WHERE exam_code IN (?, ?)", EXAM_CODE, HIDDEN_EXAM_CODE);
        }
        if (runtimeToggleTableExists) {
            jdbcTemplate.update("DELETE FROM runtime_feature_toggle");
            jdbcTemplate.update("DELETE FROM runtime_feature_toggle_audit");
            runtimeControlService.refreshSnapshot();
        }
        jdbcTemplate.update("DELETE FROM exam_manage WHERE examCode IN (?, ?)", EXAM_CODE, HIDDEN_EXAM_CODE);
        jdbcTemplate.update("DELETE FROM auth_refresh_token WHERE username IN (?, ?, ?)",
                String.valueOf(ADMIN_ID), String.valueOf(TEACHER_ID), String.valueOf(STUDENT_ID));
        deleteRedisKey("oes:student:draft:" + EXAM_CODE + ":" + STUDENT_ID);
        clearExamCaches();
    }

    @Test
    void loginShouldReturnTokensForCorrectCredentials() throws Exception {
        MvcResult result = login("ADMIN", String.valueOf(ADMIN_ID), ADMIN_PASSWORD);

        result.getResponse().setCharacterEncoding("UTF-8");
        assertThat(json(result).path("code").asInt()).isEqualTo(200);
        assertThat(json(result).path("data").path("accessToken").asText()).startsWith("eyJ");
        assertThat(json(result).path("data").path("refreshToken").asText()).startsWith("eyJ");
        assertThat(json(result).path("data").path("user").path("role").asText()).isEqualTo("ADMIN");
    }

    @Test
    void loginShouldFailForWrongPassword() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "9991",
                                  "password": "wrong-password",
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void loginShouldFailForWrongRole() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "9991",
                                  "password": "123456",
                                  "role": "STUDENT"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void accessTokenShouldAllowProtectedEndpoint() throws Exception {
        String accessToken = accessTokenOf("ADMIN", String.valueOf(ADMIN_ID), ADMIN_PASSWORD);

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(ADMIN_ID));
    }

    @Test
    void actuatorHealthAndPrometheusShouldBePublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        MvcResult result = mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("# HELP");
        assertThat(body).contains("jvm_memory_used_bytes");
    }

    @Test
    void readOnlyQueriesShouldExposeReplicaRouteMetrics() throws Exception {
        String teacherToken = accessTokenOf("TEACHER", String.valueOf(TEACHER_ID), TEACHER_PASSWORD);

        mockMvc.perform(get("/score/statistics/" + EXAM_CODE)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MvcResult result = mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("oes_db_route_requests_total");
        assertThat(body).contains("target=\"replica\"");
        assertThat(body).contains("result=\"selected\"");
        assertThat(body).contains("oes_db_replica_lag_seconds");
    }

    @Test
    void tamperedAccessTokenShouldBeRejected() throws Exception {
        String accessToken = accessTokenOf("ADMIN", String.valueOf(ADMIN_ID), ADMIN_PASSWORD);

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + accessToken + "tampered"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshShouldRotateTokensAndPersistRecord() throws Exception {
        JsonNode loginData = json(login("ADMIN", String.valueOf(ADMIN_ID), ADMIN_PASSWORD)).path("data");
        String oldRefreshToken = loginData.path("refreshToken").asText();

        MvcResult refreshResult = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(oldRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode refreshData = json(refreshResult).path("data");
        assertThat(refreshData.path("refreshToken").asText()).isNotEqualTo(oldRefreshToken);

        Integer tokenCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM auth_refresh_token WHERE username = ?",
                Integer.class,
                String.valueOf(ADMIN_ID)
        );
        assertThat(tokenCount).isNotNull();
        assertThat(tokenCount).isGreaterThanOrEqualTo(2);
    }

    @Test
    void refreshShouldRejectReusedRefreshToken() throws Exception {
        JsonNode loginData = json(login("ADMIN", String.valueOf(ADMIN_ID), ADMIN_PASSWORD)).path("data");
        String refreshToken = loginData.path("refreshToken").asText();

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void refreshShouldWriteAuthRefreshRevokedOutbox() throws Exception {
        Assumptions.assumeTrue(asyncEventOutboxTableExists, "async_event_outbox table is not present in current database");
        JsonNode loginData = json(login("ADMIN", String.valueOf(ADMIN_ID), ADMIN_PASSWORD)).path("data");
        String refreshToken = loginData.path("refreshToken").asText();

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM async_event_outbox WHERE event_type = 'auth.refresh.revoked'",
                Integer.class
        );
        assertThat(count).isNotNull();
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    void logoutShouldInvalidateRefreshToken() throws Exception {
        JsonNode loginData = json(login("ADMIN", String.valueOf(ADMIN_ID), ADMIN_PASSWORD)).path("data");
        String refreshToken = loginData.path("refreshToken").asText();

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + loginData.path("accessToken").asText())
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));

        if (asyncEventOutboxTableExists) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM async_event_outbox WHERE event_type = 'auth.refresh.revoked'",
                    Integer.class
            );
            assertThat(count).isNotNull();
            assertThat(count).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    void adminShouldAccessAdminEndpointButTeacherCannotRevokeExam() throws Exception {
        String adminToken = accessTokenOf("ADMIN", String.valueOf(ADMIN_ID), ADMIN_PASSWORD);
        String teacherToken = accessTokenOf("TEACHER", String.valueOf(TEACHER_ID), TEACHER_PASSWORD);

        mockMvc.perform(get("/admins")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/admin/exam/1001/revoke")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "forbidden-test"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentShouldOnlyAccessOwnProfileAndOwnPasswordUpdate() throws Exception {
        String studentToken = accessTokenOf("STUDENT", String.valueOf(STUDENT_ID), STUDENT_PASSWORD);

        mockMvc.perform(get("/student/" + STUDENT_ID)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.studentId").value(STUDENT_ID));

        mockMvc.perform(get("/student/" + OTHER_STUDENT_ID)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/studentPWD")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": %d,
                                  "pwd": "Reset@123"
                                }
                                """.formatted(OTHER_STUDENT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        String updated = jdbcTemplate.queryForObject(
                "SELECT pwd FROM student WHERE studentId = ?",
                String.class,
                STUDENT_ID
        );
        assertThat(updated).startsWith("$2");
    }

    @Test
    void studentShouldBeForbiddenFromPaperPreview() throws Exception {
        String studentToken = accessTokenOf("STUDENT", String.valueOf(STUDENT_ID), STUDENT_PASSWORD);

        mockMvc.perform(get("/paper/1")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void teacherCanReadStudentScoresButStudentCannotReadOthers() throws Exception {
        String teacherToken = accessTokenOf("TEACHER", String.valueOf(TEACHER_ID), TEACHER_PASSWORD);
        String studentToken = accessTokenOf("STUDENT", String.valueOf(STUDENT_ID), STUDENT_PASSWORD);

        mockMvc.perform(get("/score/1/10/" + OTHER_STUDENT_ID)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/score/" + OTHER_STUDENT_ID)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void scoreStatisticsShouldReturnAggregatedSummary() throws Exception {
        String teacherToken = accessTokenOf("TEACHER", String.valueOf(TEACHER_ID), TEACHER_PASSWORD);
        jdbcTemplate.update(
                "INSERT INTO score(examCode, studentId, subject, ptScore, etScore, score, answerDate) VALUES (?, ?, ?, ?, ?, ?, ?)",
                EXAM_CODE, STUDENT_ID, "phase8-subject", 1, 80, 100, "2026-04-19"
        );
        jdbcTemplate.update(
                "INSERT INTO score(examCode, studentId, subject, ptScore, etScore, score, answerDate) VALUES (?, ?, ?, ?, ?, ?, ?)",
                EXAM_CODE, OTHER_STUDENT_ID, "phase8-subject", 0, 50, 100, "2026-04-19"
        );
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM score WHERE examCode = ?",
                Integer.class,
                EXAM_CODE
        )).isEqualTo(2);
        examCacheFacade.markScoreProjectionDirty(EXAM_CODE);
        examCacheFacade.bumpScoreStatisticsVersion(EXAM_CODE);
        examCacheFacade.evictScoreStatistics(EXAM_CODE);
        mockMvc.perform(get("/score/statistics/" + EXAM_CODE)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.maxScore").value(80))
                .andExpect(jsonPath("$.data.minScore").value(50))
                .andExpect(jsonPath("$.data.avgScore").value(65.0))
                .andExpect(jsonPath("$.data.passRate").value(0.5))
                .andExpect(jsonPath("$.data.distribution").isArray());
    }

    @Test
    void messageEndpointsShouldReturnRepliesWithoutNPlusOneShapeRegression() throws Exception {
        String teacherToken = accessTokenOf("TEACHER", String.valueOf(TEACHER_ID), TEACHER_PASSWORD);
        jdbcTemplate.update(
                "INSERT INTO message(id, title, content, time) VALUES (?, ?, ?, CURRENT_DATE())",
                TEST_MESSAGE_ID, "phase5-message-1", "message-one"
        );
        jdbcTemplate.update(
                "INSERT INTO message(id, title, content, time) VALUES (?, ?, ?, CURRENT_DATE())",
                TEST_MESSAGE_ID_2, "phase5-message-2", "message-two"
        );
        jdbcTemplate.update(
                "INSERT INTO replay(messageId, replayId, replay, replayTime) VALUES (?, ?, ?, CURRENT_DATE())",
                TEST_MESSAGE_ID, TEST_REPLAY_ID, "reply-one"
        );
        jdbcTemplate.update(
                "INSERT INTO replay(messageId, replayId, replay, replayTime) VALUES (?, ?, ?, CURRENT_DATE())",
                TEST_MESSAGE_ID_2, TEST_REPLAY_ID_2, "reply-two"
        );

        MvcResult listResult = mockMvc.perform(get("/messages/1/50")
                        .header("Authorization", "Bearer " + teacherToken)
                        .header("X-DB-Route", DB_ROUTE_PRIMARY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andReturn();

        JsonNode records = json(listResult).path("data").path("records");
        JsonNode message = findMessage(records, TEST_MESSAGE_ID);
        JsonNode secondMessage = findMessage(records, TEST_MESSAGE_ID_2);
        assertThat(message).isNotNull();
        assertThat(secondMessage).isNotNull();
        assertThat(message.path("replays").isArray()).isTrue();
        assertThat(message.path("replays").size()).isEqualTo(1);
        assertThat(secondMessage.path("replays").size()).isEqualTo(1);

        mockMvc.perform(get("/message/" + TEST_MESSAGE_ID)
                        .header("Authorization", "Bearer " + teacherToken)
                        .header("X-DB-Route", DB_ROUTE_PRIMARY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(TEST_MESSAGE_ID))
                .andExpect(jsonPath("$.data.replays").isArray())
                .andExpect(jsonPath("$.data.replays[0].replay").value("reply-one"));
    }

    @Test
    void createMessageShouldUseCurrentUserAndWriteMessageCreatedOutbox() throws Exception {
        Assumptions.assumeTrue(asyncEventOutboxTableExists, "async_event_outbox table is not present in current database");
        String studentToken = accessTokenOf("STUDENT", String.valueOf(STUDENT_ID), STUDENT_PASSWORD);

        mockMvc.perform(post("/message")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": %d,
                                  "title": "phase6-created",
                                  "content": "message-created-content",
                                  "creatorId": %d,
                                  "creatorRole": "ADMIN",
                                  "creatorName": "fake"
                                }
                                """.formatted(TEST_MESSAGE_ID_3, ADMIN_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT creator_id, creator_role, creator_name, created_at, updated_at FROM message WHERE title = 'phase6-created' ORDER BY id DESC LIMIT 1"
        );
        assertThat(row.get("creator_id")).isEqualTo(STUDENT_ID);
        assertThat(String.valueOf(row.get("creator_role"))).isEqualTo("STUDENT");
        assertThat(String.valueOf(row.get("creator_name"))).isNotBlank();
        assertThat(row.get("created_at")).isNotNull();
        assertThat(row.get("updated_at")).isNotNull();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM async_event_outbox WHERE event_type = 'message.created'",
                Integer.class
        );
        assertThat(count).isNotNull();
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    void createReplyShouldUseCurrentTeacherMetadata() throws Exception {
        String teacherToken = accessTokenOf("TEACHER", String.valueOf(TEACHER_ID), TEACHER_PASSWORD);
        jdbcTemplate.update(
                "INSERT INTO message(id, title, content, time, creator_id, creator_role, creator_name, created_at, updated_at) VALUES (?, ?, ?, CURRENT_DATE(), ?, ?, ?, NOW(), NOW())",
                TEST_MESSAGE_ID_3, "reply-target", "reply-target-content", STUDENT_ID, "STUDENT", "student"
        );

        mockMvc.perform(post("/replay")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "messageId": %d,
                                  "replay": "phase6-reply",
                                  "creatorId": %d,
                                  "creatorRole": "STUDENT",
                                  "creatorName": "fake"
                                }
                                """.formatted(TEST_MESSAGE_ID_3, STUDENT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT creator_id, creator_role, creator_name, created_at FROM replay WHERE messageId = ? ORDER BY replayId DESC LIMIT 1",
                TEST_MESSAGE_ID_3
        );
        assertThat(Integer.parseInt(String.valueOf(row.get("creator_id")))).isEqualTo(TEACHER_ID);
        assertThat(String.valueOf(row.get("creator_role"))).isEqualTo("TEACHER");
        assertThat(String.valueOf(row.get("creator_name"))).isNotBlank();
        assertThat(row.get("created_at")).isNotNull();
    }

    @Test
    void teacherExamListShouldExposeAggregatedPolicyFields() throws Exception {
        String teacherToken = accessTokenOf("TEACHER", String.valueOf(TEACHER_ID), TEACHER_PASSWORD);
        jdbcTemplate.update("""
                INSERT INTO exam_shared_snapshot(exam_code, paper_id, created_at)
                VALUES (?, ?, NOW())
                """, EXAM_CODE, PAPER_ID);

        MvcResult result = mockMvc.perform(get("/exams/1/100")
                        .header("Authorization", "Bearer " + teacherToken)
                        .header("X-DB-Route", DB_ROUTE_PRIMARY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andReturn();

        JsonNode records = json(result).path("data").path("records");
        JsonNode currentExam = findExam(records, EXAM_CODE);
        assertThat(currentExam).isNotNull();
        assertThat(currentExam.path("totalScore").asInt()).isEqualTo(6);
        assertThat(currentExam.path("paperLocked").isMissingNode()).isFalse();
        assertThat(currentExam.path("revoked").asBoolean()).isFalse();
        assertThat(currentExam.path("inExamWindow").asBoolean()).isTrue();
        assertThat(currentExam.path("snapshotReady").asBoolean()).isTrue();
        assertThat(currentExam.path("windowEndAt").isMissingNode()).isFalse();
        assertThat(currentExam.path("freezeAt").isMissingNode()).isFalse();
        assertThat(currentExam.path("paperId").asInt()).isEqualTo(PAPER_ID);
    }

    @Test
    void studentExamListShouldFilterByScopeAndExposeStudentFacingStatusFields() throws Exception {
        Assumptions.assumeTrue(examAttemptTableExists, "exam_attempt table is not present in current database");
        String studentToken = accessTokenOf("STUDENT", String.valueOf(STUDENT_ID), STUDENT_PASSWORD);

        jdbcTemplate.update("""
                INSERT INTO exam_manage(
                    examCode, description, source, paperId, examDate, exam_start_at, totalTime,
                    grade, term, major, institute, totalScore, type, tips, paper_frozen_at, revoked_at, revoke_reason
                ) VALUES (?, ?, ?, ?, DATE_FORMAT(DATE_ADD(NOW(), INTERVAL 1 DAY), '%Y-%m-%d %H:%i:%s'),
                          DATE_ADD(NOW(), INTERVAL 1 DAY), ?, ?, ?, ?, ?, ?, ?, ?, NULL, NULL, NULL)
                """,
                HIDDEN_EXAM_CODE, "不属于当前学生的考试", "数据库系统", PAPER_ID, 60,
                "2025", "1", "软件工程", "信息工程学院", 6, "阶段测验", "hidden");

        jdbcTemplate.update("""
                INSERT INTO exam_attempt(exam_code, student_id, status, answers_json, started_at, submitted_at)
                VALUES (?, ?, 0, '{}', DATE_SUB(NOW(), INTERVAL 2 MINUTE), NULL)
                """, EXAM_CODE, STUDENT_ID);

        MvcResult result = mockMvc.perform(get("/student/exams")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.serverTime").exists())
                .andExpect(jsonPath("$.data.records").isArray())
                .andReturn();

        JsonNode records = json(result).path("data").path("records");
        assertThat(records.isArray()).isTrue();
        JsonNode currentExam = findExam(records, EXAM_CODE);
        assertThat(currentExam).isNotNull();
        assertThat(findExam(records, HIDDEN_EXAM_CODE)).isNull();
        assertThat(currentExam.path("examState").asText()).isEqualTo("ONGOING");
        assertThat(currentExam.path("attemptStatus").asText()).isEqualTo("IN_PROGRESS");
        assertThat(currentExam.path("canEnter").asBoolean()).isTrue();
        assertThat(currentExam.path("totalScore").asInt()).isEqualTo(6);
        assertThat(currentExam.path("windowEndAt").isMissingNode()).isFalse();
        assertThat(currentExam.path("freezeAt").isMissingNode()).isFalse();
    }

    @Test
    void studentExamDetailShouldReturnSummaryWithoutLeakingPaperContent() throws Exception {
        String studentToken = accessTokenOf("STUDENT", String.valueOf(STUDENT_ID), STUDENT_PASSWORD);

        MvcResult result = mockMvc.perform(get("/student/exam/" + EXAM_CODE)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.serverTime").exists())
                .andExpect(jsonPath("$.data.exam.examCode").value(EXAM_CODE))
                .andExpect(jsonPath("$.data.exam.examState").value("ONGOING"))
                .andExpect(jsonPath("$.data.exam.attemptStatus").value("NOT_STARTED"))
                .andExpect(jsonPath("$.data.exam.questionSummary").isArray())
                .andExpect(jsonPath("$.data.exam.totalQuestionCount").value(3))
                .andExpect(jsonPath("$.data.exam.summarySource").value("FROZEN_SNAPSHOT"))
                .andReturn();

        JsonNode exam = json(result).path("data").path("exam");
        assertThat(exam.path("paper").isMissingNode()).isTrue();
        assertThat(exam.path("questions").isMissingNode()).isTrue();
        assertThat(exam.path("question").isMissingNode()).isTrue();

        JsonNode summary = exam.path("questionSummary");
        assertThat(summary.size()).isEqualTo(3);
        assertThat(summary.get(0).path("questionType").asInt()).isEqualTo(1);
        assertThat(summary.get(0).path("count").asInt()).isEqualTo(1);
        assertThat(summary.get(0).path("score").asInt()).isEqualTo(2);
        assertThat(summary.get(1).path("questionType").asInt()).isEqualTo(2);
        assertThat(summary.get(1).path("count").asInt()).isEqualTo(1);
        assertThat(summary.get(2).path("questionType").asInt()).isEqualTo(3);
        assertThat(summary.get(2).path("count").asInt()).isEqualTo(1);
    }

    @Test
    void answerSubmitShouldUseCurrentStudentInsteadOfPayloadStudentId() throws Exception {
        String studentToken = accessTokenOf("STUDENT", String.valueOf(STUDENT_ID), STUDENT_PASSWORD);

        mockMvc.perform(post("/answer/submit")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "examCode": %d,
                                  "studentId": %d,
                                  "answers": []
                                }
                                """.formatted(EXAM_CODE, OTHER_STUDENT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.studentId").value(STUDENT_ID));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM score WHERE examCode = ? AND studentId = ?",
                Integer.class,
                EXAM_CODE,
                STUDENT_ID
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void answerSubmitShouldRejectDuplicateSubmission() throws Exception {
        String studentToken = accessTokenOf("STUDENT", String.valueOf(STUDENT_ID), STUDENT_PASSWORD);
        String body = """
                {
                  "examCode": %d,
                  "studentId": %d,
                  "answers": []
                }
                """.formatted(EXAM_CODE, OTHER_STUDENT_ID);

        mockMvc.perform(post("/answer/submit")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/answer/submit")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void studentExamAttemptFlowShouldMaskPaperPersistAnswersAndPreventResubmit() throws Exception {
        Assumptions.assumeTrue(examAttemptTableExists, "exam_attempt table is not present in current database");
        String studentToken = accessTokenOf("STUDENT", String.valueOf(STUDENT_ID), STUDENT_PASSWORD);

        MvcResult startResult = mockMvc.perform(post("/student/exam/" + EXAM_CODE + "/attempt/start")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode startData = json(startResult).path("data");
        assertThat(startData.path("attemptId").asInt()).isPositive();
        assertThat(startData.path("paper").isObject()).isTrue();

        String answersJson = "{\"answers\":{\"1_10001\":\"A\",\"2_20001\":\"TCP\"}}";
        mockMvc.perform(put("/student/exam/" + EXAM_CODE + "/attempt/answers")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answersJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        String persistedAnswers = jdbcTemplate.queryForObject(
                "SELECT answers_json FROM exam_attempt WHERE exam_code = ? AND student_id = ?",
                String.class,
                EXAM_CODE,
                STUDENT_ID
        );
        String draftKey = "oes:student:draft:" + EXAM_CODE + ":" + STUDENT_ID;
        String draftJson = stringRedisTemplate.map(template -> template.opsForValue().get(draftKey)).orElse(null);
        assertThat(draftJson).contains("1_10001");
        assertThat(draftJson).contains("2_20001");
        assertThat(persistedAnswers).isNotNull();

        mockMvc.perform(post("/student/exam/" + EXAM_CODE + "/attempt/submit")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.etScore").exists())
                .andExpect(jsonPath("$.data.totalQuestions").isNumber());

        Integer scoreCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM score WHERE examCode = ? AND studentId = ?",
                Integer.class,
                EXAM_CODE,
                STUDENT_ID
        );
        assertThat(scoreCount).isEqualTo(1);

        Integer status = jdbcTemplate.queryForObject(
                "SELECT status FROM exam_attempt WHERE exam_code = ? AND student_id = ?",
                Integer.class,
                EXAM_CODE,
                STUDENT_ID
        );
        assertThat(status).isEqualTo(1);

        String finalAnswers = jdbcTemplate.queryForObject(
                "SELECT answers_json FROM exam_attempt WHERE exam_code = ? AND student_id = ?",
                String.class,
                EXAM_CODE,
                STUDENT_ID
        );
        assertThat(finalAnswers).contains("1_10001");
        assertThat(finalAnswers).contains("2_20001");
        assertThat(stringRedisTemplate.map(template -> template.opsForValue().get(draftKey)).orElse(null)).isNull();

        mockMvc.perform(post("/student/exam/" + EXAM_CODE + "/attempt/submit")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void studentExamAttemptStartShouldReuseExistingInProgressAttempt() throws Exception {
        Assumptions.assumeTrue(examAttemptTableExists, "exam_attempt table is not present in current database");
        String studentToken = accessTokenOf("STUDENT", String.valueOf(STUDENT_ID), STUDENT_PASSWORD);

        MvcResult first = mockMvc.perform(post("/student/exam/" + EXAM_CODE + "/attempt/start")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        MvcResult second = mockMvc.perform(post("/student/exam/" + EXAM_CODE + "/attempt/start")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        assertThat(json(first).path("data").path("attemptId").asLong())
                .isEqualTo(json(second).path("data").path("attemptId").asLong());
    }

    @Test
    void studentSubmitShouldWriteExamSubmittedOutbox() throws Exception {
        Assumptions.assumeTrue(examAttemptTableExists, "exam_attempt table is not present in current database");
        Assumptions.assumeTrue(asyncEventOutboxTableExists, "async_event_outbox table is not present in current database");
        String studentToken = accessTokenOf("STUDENT", String.valueOf(STUDENT_ID), STUDENT_PASSWORD);

        mockMvc.perform(post("/student/exam/" + EXAM_CODE + "/attempt/start")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(put("/student/exam/" + EXAM_CODE + "/attempt/answers")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\":{\"1_10001\":\"A\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/student/exam/" + EXAM_CODE + "/attempt/submit")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM async_event_outbox WHERE event_type = 'exam.submitted' AND aggregate_id = ?",
                Integer.class,
                EXAM_CODE + ":" + STUDENT_ID
        );
        assertThat(count).isNotNull();
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    void scoreStatisticsShouldFallbackToRealtimeWhenProjectionDirty() throws Exception {
        Assumptions.assumeTrue(asyncProjectionTableExists, "exam_score_statistics_projection table is not present in current database");
        String teacherToken = accessTokenOf("TEACHER", String.valueOf(TEACHER_ID), TEACHER_PASSWORD);
        jdbcTemplate.update("""
                INSERT INTO score(examCode, studentId, subject, ptScore, etScore, score, answerDate)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE())
                """, EXAM_CODE, STUDENT_ID, "计算机网络", 1, 6, 6);
        examCacheFacade.markScoreProjectionDirty(EXAM_CODE);
        examCacheFacade.bumpScoreStatisticsVersion(EXAM_CODE);
        examCacheFacade.evictScoreStatistics(EXAM_CODE);

        mockMvc.perform(get("/score/statistics/" + EXAM_CODE)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.distribution").isArray());
    }

    @Test
    void adminRuntimeControlsShouldRequireAdminAndToggleMessageGate() throws Exception {
        Assumptions.assumeTrue(runtimeToggleTableExists, "runtime_feature_toggle table is not present in current database");
        String adminToken = accessTokenOf("ADMIN", String.valueOf(ADMIN_ID), ADMIN_PASSWORD);
        String teacherToken = accessTokenOf("TEACHER", String.valueOf(TEACHER_ID), TEACHER_PASSWORD);

        MvcResult updateResult = mockMvc.perform(put("/admin/runtime/controls/NON_CORE_MESSAGE_ENABLED")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "enabled": false,
                                  "reason": "integration-test disable messages"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.features.NON_CORE_MESSAGE_ENABLED").value(false))
                .andReturn();

        mockMvc.perform(get("/messages/1/10")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(503));

        mockMvc.perform(get("/admin/runtime/controls/local")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());

        String batchId = jdbcTemplate.queryForObject(
                "SELECT batch_id FROM runtime_feature_toggle_audit ORDER BY id DESC LIMIT 1",
                String.class
        );
        assertThat(batchId).isNotBlank();
        assertThat(json(updateResult).path("data").path("version").asLong()).isPositive();
    }

    @Test
    void adminRuntimeRollbackShouldRestoreMessageAccess() throws Exception {
        Assumptions.assumeTrue(runtimeToggleTableExists, "runtime_feature_toggle table is not present in current database");
        String adminToken = accessTokenOf("ADMIN", String.valueOf(ADMIN_ID), ADMIN_PASSWORD);
        String teacherToken = accessTokenOf("TEACHER", String.valueOf(TEACHER_ID), TEACHER_PASSWORD);

        mockMvc.perform(put("/admin/runtime/controls/NON_CORE_MESSAGE_ENABLED")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "enabled": false,
                                  "reason": "integration-test disable messages"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        String batchId = jdbcTemplate.queryForObject(
                "SELECT batch_id FROM runtime_feature_toggle_audit ORDER BY id DESC LIMIT 1",
                String.class
        );

        mockMvc.perform(post("/admin/runtime/controls/history/" + batchId + "/rollback")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "integration-test rollback messages"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.features.NON_CORE_MESSAGE_ENABLED").value(true));

        mockMvc.perform(get("/messages/1/10")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private MvcResult login(String role, String username, String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s",
                                  "role": "%s"
                                }
                                """.formatted(username, password, role)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String accessTokenOf(String role, String username, String password) throws Exception {
        return json(login(role, username, password)).path("data").path("accessToken").asText();
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode findExam(JsonNode records, int examCode) {
        for (JsonNode item : records) {
            if (item.path("examCode").asInt() == examCode) {
                return item;
            }
        }
        return null;
    }

    private JsonNode findMessage(JsonNode records, int messageId) {
        for (JsonNode item : records) {
            if (item.path("id").asInt() == messageId) {
                return item;
            }
        }
        return null;
    }

    private boolean hasTable(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                """, Integer.class, tableName);
        return count != null && count > 0;
    }

    private void ensureRuntimeControlTables() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS runtime_feature_toggle (
                    feature_key VARCHAR(64) NOT NULL PRIMARY KEY,
                    enabled TINYINT(1) NOT NULL,
                    version BIGINT NOT NULL,
                    updated_by_id INT NULL,
                    updated_by_role VARCHAR(32) NULL,
                    updated_by_name VARCHAR(64) NULL,
                    updated_reason VARCHAR(255) NOT NULL,
                    preset_name VARCHAR(64) NULL,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS runtime_feature_toggle_audit (
                    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    batch_id VARCHAR(64) NOT NULL,
                    feature_key VARCHAR(64) NOT NULL,
                    old_enabled TINYINT(1) NOT NULL,
                    new_enabled TINYINT(1) NOT NULL,
                    operator_id INT NULL,
                    operator_role VARCHAR(32) NULL,
                    operator_name VARCHAR(64) NULL,
                    reason VARCHAR(255) NOT NULL,
                    preset_name VARCHAR(64) NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
    }

    private void ensurePaperQuestions() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM paper_manage WHERE paperId = ?",
                Integer.class,
                PAPER_ID
        );
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update("INSERT INTO paper_manage(paperId, questionType, questionId) VALUES (?, 1, 10001)", PAPER_ID);
        jdbcTemplate.update("INSERT INTO paper_manage(paperId, questionType, questionId) VALUES (?, 2, 10001)", PAPER_ID);
        jdbcTemplate.update("INSERT INTO paper_manage(paperId, questionType, questionId) VALUES (?, 3, 10001)", PAPER_ID);
    }

    private void insertVisibleExam() {
        jdbcTemplate.update("""
                INSERT INTO exam_manage(
                    examCode, description, source, paperId, examDate, exam_start_at, totalTime,
                    grade, term, major, institute, totalScore, type, tips, paper_frozen_at, revoked_at, revoke_reason
                ) VALUES (
                    ?, ?, ?, ?,
                    DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 5 MINUTE), '%Y-%m-%d %H:%i:%s'),
                    DATE_SUB(NOW(), INTERVAL 5 MINUTE),
                    ?, ?, ?, ?, ?, ?, ?, ?, NULL, NULL, NULL
                )
                """,
                EXAM_CODE,
                "测试专用考试",
                "计算机网络",
                PAPER_ID,
                90,
                "2023",
                "1",
                "计算机科学与技术",
                "软件工程学院",
                6,
                "期末考试",
                "integration-test"
        );
    }

    private void deleteRedisKey(String key) {
        stringRedisTemplate.ifPresent(template -> template.delete(key));
    }

    private void clearExamCaches() {
        examCacheFacade.evictExamMeta(EXAM_CODE);
        examCacheFacade.evictExamMeta(HIDDEN_EXAM_CODE);
        examCacheFacade.evictSnapshotCaches(EXAM_CODE);
        examCacheFacade.evictSnapshotCaches(HIDDEN_EXAM_CODE);
        examCacheFacade.evictScoreStatistics(EXAM_CODE);
        examCacheFacade.evictScoreStatistics(HIDDEN_EXAM_CODE);
        examCacheFacade.clearScoreProjectionDirty(EXAM_CODE);
        examCacheFacade.clearScoreProjectionDirty(HIDDEN_EXAM_CODE);
        examCacheFacade.bumpScoreStatisticsVersion(EXAM_CODE);
        examCacheFacade.bumpScoreStatisticsVersion(HIDDEN_EXAM_CODE);
        examCacheFacade.evictPaperAggregates(PAPER_ID);
        examCacheFacade.evictStudentExamList(STUDENT_ID);
        examCacheFacade.evictStudentExamDetail(STUDENT_ID, EXAM_CODE);
        examCacheFacade.bumpScopeExamVersion();
    }
}
