package com.test.oes.entity;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
//管理原实体类
public class Admin {
    private Integer adminId;

    private String adminName;

    private String sex;

    private String tel;

    private String email;

    private String pwd;

    private String cardId;

    private String role;
}
