package com.test.oes.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class Replay {
    private Integer messageId;
    private Integer replayId;
    private String replay;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date replayTime;

    private Integer creatorId;
    private String creatorRole;
    private String creatorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
