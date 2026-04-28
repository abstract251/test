package com.test.oes.service.impl;

import com.test.oes.async.AsyncEventEnvelope;
import com.test.oes.async.AsyncEventTypes;
import com.test.oes.async.AsyncRoutingKeys;
import com.test.oes.async.OutboxEventService;
import com.test.oes.async.payload.AuthRefreshRevokedPayload;
import com.test.oes.cache.RefreshTokenHotState;
import com.test.oes.cache.RefreshTokenHotStateService;
import com.test.oes.dto.auth.AuthTokenResponse;
import com.test.oes.dto.auth.LoginRequest;
import com.test.oes.entity.Admin;
import com.test.oes.entity.AuthRefreshToken;
import com.test.oes.entity.Student;
import com.test.oes.entity.Teacher;
import com.test.oes.exception.ExamBusinessException;
import com.test.oes.mapper.AdminMapper;
import com.test.oes.mapper.AuthRefreshTokenMapper;
import com.test.oes.mapper.StudentMapper;
import com.test.oes.mapper.TeacherMapper;
import com.test.oes.security.AccountRole;
import com.test.oes.security.CurrentUserService;
import com.test.oes.security.JwtTokenService;
import com.test.oes.security.LoginUser;
import com.test.oes.security.PasswordService;
import com.test.oes.service.AuthService;
import com.test.oes.vo.CurrentUserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class AuthServiceImpl implements AuthService {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final AdminMapper adminMapper;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final PasswordService passwordService;
    private final JwtTokenService jwtTokenService;
    private final AuthRefreshTokenMapper authRefreshTokenMapper;
    private final CurrentUserService currentUserService;
    private final RefreshTokenHotStateService refreshTokenHotStateService;
    private final OutboxEventService outboxEventService;

    public AuthServiceImpl(AdminMapper adminMapper, TeacherMapper teacherMapper, StudentMapper studentMapper,
                           PasswordService passwordService, JwtTokenService jwtTokenService,
                           AuthRefreshTokenMapper authRefreshTokenMapper, CurrentUserService currentUserService,
                           RefreshTokenHotStateService refreshTokenHotStateService,
                           OutboxEventService outboxEventService) {
        this.adminMapper = adminMapper;
        this.teacherMapper = teacherMapper;
        this.studentMapper = studentMapper;
        this.passwordService = passwordService;
        this.jwtTokenService = jwtTokenService;
        this.authRefreshTokenMapper = authRefreshTokenMapper;
        this.currentUserService = currentUserService;
        this.refreshTokenHotStateService = refreshTokenHotStateService;
        this.outboxEventService = outboxEventService;
    }

    @Override
    @Transactional
    public AuthTokenResponse login(LoginRequest request) {
        if (request == null || request.getRole() == null || request.getUsername() == null || request.getPassword() == null) {
            throw new ExamBusinessException(400, "用户名、密码和角色不能为空");
        }
        LoginUser loginUser = loadUser(request.getRole(), request.getUsername());
        if (!passwordService.matches(request.getPassword(), loginUser.getPassword())) {
            throw new ExamBusinessException(400, "用户名或密码错误");
        }
        return buildAuthResponse(loginUser);
    }

    @Override
    @Transactional
    public AuthTokenResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ExamBusinessException(400, "refreshToken 不能为空");
        }
        JwtTokenService.RefreshTokenClaims claims = jwtTokenService.parseRefreshToken(refreshToken);
        RefreshTokenHotState hotState = refreshTokenHotStateService.getOrLoad(
                claims.jti(),
                () -> authRefreshTokenMapper.findByJti(claims.jti())
        );
        if (hotState == null || Boolean.TRUE.equals(hotState.getRevoked())) {
            throw new ExamBusinessException(401, "refreshToken 已失效");
        }
        if (hotState.getExpiresAt() != null && hotState.getExpiresAt().isBefore(LocalDateTime.now(SHANGHAI))) {
            authRefreshTokenMapper.revokeById(hotState.getId());
            AuthRefreshToken expired = buildRevokedToken(hotState, claims.jti());
            refreshTokenHotStateService.markRevoked(claims.jti(), expired);
            appendRefreshRevokedEvent(expired, "EXPIRED");
            throw new ExamBusinessException(401, "refreshToken 已过期");
        }
        if (authRefreshTokenMapper.revokeIfActiveById(hotState.getId()) == 0) {
            throw new ExamBusinessException(401, "refreshToken 已失效");
        }
        AuthRefreshToken revoked = buildRevokedToken(hotState, claims.jti());
        refreshTokenHotStateService.markRevoked(claims.jti(), revoked);
        appendRefreshRevokedEvent(revoked, "ROTATED");
        LoginUser loginUser = loadUser(AccountRole.valueOf(hotState.getRole()), hotState.getUsername());
        return buildAuthResponse(loginUser);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        JwtTokenService.RefreshTokenClaims claims = jwtTokenService.parseRefreshToken(refreshToken);
        AuthRefreshToken stored = authRefreshTokenMapper.findByJti(claims.jti());
        if (stored != null) {
            authRefreshTokenMapper.revokeById(stored.getId());
            stored.setRevoked(1);
            refreshTokenHotStateService.markRevoked(claims.jti(), stored);
            appendRefreshRevokedEvent(stored, "LOGOUT");
        }
    }

    @Override
    public CurrentUserVO currentUser() {
        return CurrentUserVO.from(currentUserService.requireCurrentUser());
    }

    private AuthTokenResponse buildAuthResponse(LoginUser loginUser) {
        String accessToken = jwtTokenService.generateAccessToken(loginUser);
        JwtTokenService.RefreshTokenPayload refreshToken = jwtTokenService.generateRefreshToken(loginUser);
        AuthRefreshToken entity = new AuthRefreshToken();
        entity.setJti(refreshToken.jti());
        entity.setUserId(loginUser.getUserId());
        entity.setRole(loginUser.getAccountRole().name());
        entity.setUsername(loginUser.getUsername());
        entity.setExpiresAt(LocalDateTime.ofInstant(refreshToken.expiresAt(), SHANGHAI));
        entity.setRevoked(0);
        entity.setCreatedAt(LocalDateTime.now(SHANGHAI));
        authRefreshTokenMapper.insert(entity);
        refreshTokenHotStateService.store(entity);
        return new AuthTokenResponse(
                accessToken,
                refreshToken.token(),
                "Bearer",
                jwtTokenService.getAccessTokenExpiresInSeconds(),
                CurrentUserVO.from(loginUser)
        );
    }

    private LoginUser loadUser(AccountRole role, String username) {
        Integer numericId = parseNumericId(username);
        return switch (role) {
            case ADMIN -> {
                Admin admin = adminMapper.findById(numericId);
                if (admin == null) {
                    throw new ExamBusinessException(400, "用户名或密码错误");
                }
                yield new LoginUser(admin.getAdminId(), String.valueOf(admin.getAdminId()), admin.getAdminName(), role, admin.getPwd());
            }
            case TEACHER -> {
                Teacher teacher = teacherMapper.findById(numericId);
                if (teacher == null) {
                    throw new ExamBusinessException(400, "用户名或密码错误");
                }
                yield new LoginUser(Integer.parseInt(teacher.getTeacherId()), teacher.getTeacherId(), teacher.getTeacherName(), role, teacher.getPwd());
            }
            case STUDENT -> {
                Student student = studentMapper.findById(numericId);
                if (student == null) {
                    throw new ExamBusinessException(400, "用户名或密码错误");
                }
                yield new LoginUser(student.getStudentId(), String.valueOf(student.getStudentId()), student.getStudentName(), role, student.getPwd());
            }
        };
    }

    private Integer parseNumericId(String username) {
        try {
            return Integer.parseInt(username.trim());
        } catch (NumberFormatException e) {
            throw new ExamBusinessException(400, "账号必须是数字编号");
        }
    }

    private AuthRefreshToken buildRevokedToken(RefreshTokenHotState hotState, String jti) {
        AuthRefreshToken token = new AuthRefreshToken();
        token.setId(hotState.getId());
        token.setJti(jti);
        token.setUserId(hotState.getUserId());
        token.setRole(hotState.getRole());
        token.setUsername(hotState.getUsername());
        token.setExpiresAt(hotState.getExpiresAt());
        token.setRevoked(1);
        return token;
    }

    private void appendRefreshRevokedEvent(AuthRefreshToken token, String revokeReason) {
        if (token == null || token.getJti() == null) {
            return;
        }
        AuthRefreshRevokedPayload payload = new AuthRefreshRevokedPayload();
        payload.setJti(token.getJti());
        payload.setTokenId(token.getId());
        payload.setUserId(token.getUserId());
        payload.setUsername(token.getUsername());
        payload.setRole(token.getRole());
        payload.setRevokedAt(LocalDateTime.now(SHANGHAI));
        payload.setExpiresAt(token.getExpiresAt());
        payload.setRevokeReason(revokeReason);

        AsyncEventEnvelope<AuthRefreshRevokedPayload> envelope = new AsyncEventEnvelope<>();
        envelope.setEventType(AsyncEventTypes.AUTH_REFRESH_REVOKED);
        envelope.setAggregateType("refreshToken");
        envelope.setAggregateId(token.getJti());
        envelope.setOccurredAt(payload.getRevokedAt());
        envelope.setPayloadVersion(1);
        envelope.setPayload(payload);
        outboxEventService.append(
                AsyncEventTypes.AUTH_REFRESH_REVOKED,
                "refreshToken",
                token.getJti(),
                AsyncRoutingKeys.AUTH_REFRESH_REVOKED,
                envelope
        );
    }
}
