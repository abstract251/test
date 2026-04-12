package com.test.oes.service.exam;

import com.test.oes.entity.ExamManage;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 考试业务时间：统一 Asia/Shanghai；冻结时刻 = 开考时刻前 1 小时。
 */
@Component
public class ExamTimeHelper {

    public static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private static final DateTimeFormatter ISO_LOCAL = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DT_SPACE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DT_SPACE_SHORT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public LocalDateTime nowShanghai() {
        return LocalDateTime.now(SHANGHAI);
    }

    /**
     * 开考时刻：优先 {@code examStartAt}，否则解析 {@code examDate}（日期或日期时间），否则 null。
     */
    public LocalDateTime effectiveExamStart(ExamManage e) {
        if (e == null) {
            return null;
        }
        if (e.getExamStartAt() != null) {
            return e.getExamStartAt();
        }
        return parseExamDateField(e.getExamDate());
    }

    /**
     * 冻结时刻：开考前一整小时。
     */
    public LocalDateTime freezeInstant(ExamManage e) {
        LocalDateTime start = effectiveExamStart(e);
        if (start == null) {
            return null;
        }
        return start.minusHours(1);
    }

    public boolean isRevoked(ExamManage e) {
        return e != null && e.getRevokedAt() != null;
    }

    /**
     * 已到冻结时刻（试卷内容与结构不可再改）。与快照是否已落库无关。
     */
    public boolean isPaperLocked(ExamManage e, LocalDateTime now) {
        if (e == null || isRevoked(e)) {
            return true;
        }
        LocalDateTime freeze = freezeInstant(e);
        if (freeze == null) {
            return false;
        }
        return !now.isBefore(freeze);
    }

    public boolean shouldNotMaterializeSnapshot(ExamManage e, LocalDateTime now) {
        return isRevoked(e) || !isPaperLocked(e, now);
    }

    /**
     * 考试开放窗口结束：开考 + totalTime（分钟），默认 90。
     */
    public LocalDateTime examWindowEnd(ExamManage e) {
        LocalDateTime start = effectiveExamStart(e);
        if (start == null) {
            return null;
        }
        int minutes = e.getTotalTime() == null ? 90 : e.getTotalTime();
        return start.plusMinutes(minutes);
    }

    /** 是否在可作答时间窗内（含结束时刻，与交卷校验一致）。 */
    public boolean isWithinExamWindow(ExamManage e, LocalDateTime now) {
        LocalDateTime start = effectiveExamStart(e);
        if (start == null) {
            return false;
        }
        LocalDateTime end = examWindowEnd(e);
        return !now.isBefore(start) && !now.isAfter(end);
    }

    /**
     * 从请求体解析开考时刻：优先已有 examStartAt；否则解析 examDate 字符串。
     */
    public LocalDateTime resolveExamStartFromPayload(ExamManage payload, LocalDateTime defaultIfBlank) {
        if (payload.getExamStartAt() != null) {
            return payload.getExamStartAt();
        }
        LocalDateTime parsed = parseExamDateField(payload.getExamDate());
        if (parsed != null) {
            return parsed;
        }
        return defaultIfBlank;
    }

    public LocalDateTime parseExamDateField(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            if (s.contains("T")) {
                return LocalDateTime.parse(s, ISO_LOCAL);
            }
            if (s.length() <= 10) {
                LocalDate d = LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
                return d.atStartOfDay();
            }
            if (s.length() == 16) {
                return LocalDateTime.parse(s, DT_SPACE_SHORT);
            }
            return LocalDateTime.parse(s, DT_SPACE);
        } catch (DateTimeParseException ignored) {
            try {
                LocalDate d = LocalDate.parse(s.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE);
                return d.atStartOfDay();
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * 与现有前端约定一致：examDate 存展示用字符串（完整开考时间优先）。
     */
    public String formatExamDateDisplay(LocalDateTime start) {
        if (start == null) {
            return null;
        }
        return start.format(DT_SPACE);
    }
}
