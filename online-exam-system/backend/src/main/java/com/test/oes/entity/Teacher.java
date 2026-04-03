package com.test.oes.entity;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
//老师实体类
public class Teacher {
    private String teacherId;

    private String teacherName;

    private String institute;

    private String sex;

    private String tel;

    private String email;

    private String pwd;

    private String cardId;

    private String type;

    private String role;
}
