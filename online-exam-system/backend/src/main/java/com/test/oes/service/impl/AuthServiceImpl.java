package com.test.oes.service.impl;

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

    public AuthServiceImpl(AdminMapper adminMapper, TeacherMapper teacherMapper, StudentMapper studentMapper,
                           PasswordService passwordService, JwtTokenService jwtTokenService,
                           AuthRefreshTokenMapper authRefreshTokenMapper, CurrentUserService currentUserService) {
        this.adminMapper = adminMapper;
        this.teacherMapper = teacherMapper;
        this.studentMapper = studentMapper;
        this.passwordService = passwordService;
        this.jwtTokenService = jwtTokenService;
        this.authRefreshTokenMapper = authRefreshTokenMapper;
        this.currentUserService = currentUserService;
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
        AuthRefreshToken stored = authRefreshTokenMapper.findByJti(claims.jti());
        if (stored == null || (stored.getRevoked() != null && stored.getRevoked() == 1)) {
            throw new ExamBusinessException(401, "refreshToken 已失效");
        }
        if (stored.getExpiresAt() != null && stored.getExpiresAt().isBefore(LocalDateTime.now(SHANGHAI))) {
            authRefreshTokenMapper.revokeById(stored.getId());
            throw new ExamBusinessException(401, "refreshToken 已过期");
        }
        authRefreshTokenMapper.revokeById(stored.getId());
        LoginUser loginUser = loadUser(AccountRole.valueOf(stored.getRole()), stored.getUsername());
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
                Admin admin = adminMapper.findForLogin(numericId);
                if (admin == null) {
                    throw new ExamBusinessException(400, "用户名或密码错误");
                }
                yield new LoginUser(admin.getAdminId(), String.valueOf(admin.getAdminId()), admin.getAdminName(), role, admin.getPwd());
            }
            case TEACHER -> {
                Teacher teacher = teacherMapper.findForLogin(String.valueOf(numericId));
                if (teacher == null) {
                    throw new ExamBusinessException(400, "用户名或密码错误");
                }
                yield new LoginUser(Integer.parseInt(teacher.getTeacherId()), teacher.getTeacherId(), teacher.getTeacherName(), role, teacher.getPwd());
            }
            case STUDENT -> {
                Student student = studentMapper.findForLogin(numericId);
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
}
