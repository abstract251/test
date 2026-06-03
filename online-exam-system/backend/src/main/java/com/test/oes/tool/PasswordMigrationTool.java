package com.test.oes.tool;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PasswordMigrationTool {

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$.{56}$");
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public static void main(String[] args) throws Exception {
        String host = env("DB_HOST", "localhost");
        String port = env("DB_PORT", "3306");
        String database = env("DB_NAME", "online_exam");
        String username = env("DB_USERNAME", "root");
        String password = env("DB_PASSWORD", "123456");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useUnicode=true&characterEncoding=UTF-8&useSSL=false"
                + "&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            connection.setAutoCommit(false);
            int adminUpdated = migrateTable(connection, "admin", "adminId");
            int teacherUpdated = migrateTable(connection, "teacher", "teacherId");
            int studentUpdated = migrateTable(connection, "student", "studentId");
            connection.commit();
            System.out.println("Password migration finished.");
            System.out.println("admin updated: " + adminUpdated);
            System.out.println("teacher updated: " + teacherUpdated);
            System.out.println("student updated: " + studentUpdated);
        }
    }

    private static int migrateTable(Connection connection, String tableName, String idColumn) throws SQLException {
        List<Row> rows = new ArrayList<>();
        String selectSql = "SELECT " + idColumn + ", pwd FROM " + tableName;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectSql)) {
            while (resultSet.next()) {
                String id = resultSet.getString(1);
                String pwd = resultSet.getString(2);
                if (pwd == null || pwd.isBlank() || BCRYPT_PATTERN.matcher(pwd).matches()) {
                    continue;
                }
                rows.add(new Row(id, ENCODER.encode(pwd)));
            }
        }

        if (rows.isEmpty()) {
            return 0;
        }

        String updateSql = "UPDATE " + tableName + " SET pwd = ? WHERE " + idColumn + " = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateSql)) {
            for (Row row : rows) {
                preparedStatement.setString(1, row.hashedPassword());
                preparedStatement.setString(2, row.id());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        return rows.size();
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private record Row(String id, String hashedPassword) {
    }
}
