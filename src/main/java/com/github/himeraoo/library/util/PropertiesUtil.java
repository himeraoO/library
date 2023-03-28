package com.github.himeraoo.library.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtil {

    protected static Properties properties;

    static {
        try(FileInputStream fileInputStream = new FileInputStream("src/java/resources/app.properties")) {
            properties = new Properties();
            properties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
