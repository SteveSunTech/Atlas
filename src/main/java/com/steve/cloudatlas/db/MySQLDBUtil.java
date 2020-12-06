package com.steve.cloudatlas.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MySQLDBUtil {
    private static final String INSTANCE = "atlas-instance.cnxcpbkmu6r1.us-east-1.rds.amazonaws.com"; //Endpoint in your RDS
    private static final String PORT_NUM = "3306";
    private static final String DB_NAME = "atlas";

    public static String getMySQLAddress() throws IOException {
        Properties prop = new Properties();
        String propFileName = "conf.properties";


        InputStream inputStream = MySQLDBUtil.class.getClassLoader().getResourceAsStream(propFileName);
        prop.load(inputStream);


        String username = prop.getProperty("user");
        String password = prop.getProperty("password");
        return String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&autoReconnect=true&serverTimezone=UTC&createDatabaseIfNotExist=true",
                INSTANCE, PORT_NUM, DB_NAME, username, password);
    }




}
