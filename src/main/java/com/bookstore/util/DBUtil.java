package com.bookstore.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接工具类，基于 HikariCP 连接池。
 * 从 classpath 下的 db.properties 读取配置。
 */
public class DBUtil {

    private static HikariDataSource dataSource;

    static {
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new RuntimeException("无法找到 db.properties，请确认文件在 src/main/resources 目录下。");
            }
            Properties props = new Properties();
            props.load(in);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            config.setMaximumPoolSize(
                    Integer.parseInt(props.getProperty("db.maximumPoolSize", "10"))
            );
            // MySQL 8.x 驱动类
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            dataSource = new HikariDataSource(config);
        } catch (IOException e) {
            throw new RuntimeException("加载 db.properties 失败", e);
        }
    }

    /**
     * 获取一个数据库连接，使用完后请及时关闭。
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 关闭连接池（一般在应用停止时调用一次即可）。
     */
    public static void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}


