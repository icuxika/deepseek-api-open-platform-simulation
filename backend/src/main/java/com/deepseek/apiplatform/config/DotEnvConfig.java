package com.deepseek.apiplatform.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class DotEnvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            File envFile = new File(".env");
            if (!envFile.exists()) {
                envFile = new File("backend/.env");
            }
            
            if (envFile.exists()) {
                Map<String, Object> envMap = new HashMap<>();
                
                try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        
                        int separatorIndex = line.indexOf('=');
                        if (separatorIndex > 0) {
                            String key = line.substring(0, separatorIndex).trim();
                            String value = line.substring(separatorIndex + 1).trim();
                            
                            if (value.startsWith("\"") && value.endsWith("\"")) {
                                value = value.substring(1, value.length() - 1);
                            }
                            
                            envMap.put(key, value);
                            
                            if (System.getenv(key) == null && System.getProperty(key) == null) {
                                System.setProperty(key, value);
                            }
                        }
                    }
                }
                
                ConfigurableEnvironment environment = applicationContext.getEnvironment();
                environment.getPropertySources().addFirst(
                    new MapPropertySource("dotenvProperties", envMap)
                );
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load .env file: " + e.getMessage());
        }
    }
}
