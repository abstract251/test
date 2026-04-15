package com.test.oes;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.test.oes.mapper")
public class OnlineExamSystemApplication {

    // 主方法：Java 程序入口
    public static void main(String[] args) {
        // 启动 SpringBoot 项目
        SpringApplication.run(OnlineExamSystemApplication.class, args);
    }

    // 启动分页插件
    @Bean
    public MybatisPlusInterceptor innerInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}