package org.config;
import org.utilities.logs.LogManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.constants.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigReader {

    private static ConfigReader instance;
    private static final Object LOCK = new Object();

    private Properties properties;
    private JsonNode   testData;
    private String     currentEnvironment;



    private ConfigReader() {
        loadConfiguration();
    }


    public static ConfigReader getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new ConfigReader();
                }
            }
        }
        return instance;
    }


    private void loadConfiguration() {
        properties = new Properties();
        currentEnvironment = getEnvironment();

        try {
            // Load base application properties first
            loadPropertiesFile(Constants.APPLICATION_PROPERTIES);

            // Override with environment-specific properties
            String envPropertiesFile = getEnvironmentPropertiesFile(currentEnvironment);
            loadPropertiesFile(envPropertiesFile);

            LogManager.logConfigLoaded(envPropertiesFile);

        } catch (Exception e) {
            LogManager.logConfigLoadFailure("configuration files", e);
            throw new RuntimeException("Failed to load configuration", e);
        }
    }


    private void loadPropertiesFile(String fileName) {
        String filePath = Constants.CONFIG_DIR + fileName;

        try (InputStream inputStream = new FileInputStream(filePath)) {
            properties.load(inputStream);
            LogManager.debug("Loaded properties from: {}", filePath);

        } catch (IOException e) {
            // LogManager.warn("Could not load properties file: {}. Continuing with existing properties.", filePath);

            // Try to load from classpath as fallback
            try (InputStream classpathStream = getClass().getClassLoader().getResourceAsStream("config/" + fileName)) {
                if (classpathStream != null) {
                    properties.load(classpathStream);
                    LogManager.debug("Loaded properties from classpath: {}", fileName);
                } else {
                    // LogManager.warn("Properties file not found in classpath: {}", fileName);
                }
            } catch (IOException classpathException) {
                // LogManager.warn("Failed to load properties from classpath: {}", fileName);
            }
        }
    }


    private String getEnvironment() {
        String env = System.getProperty(Constants.ENVIRONMENT_PROPERTY);
        if (env == null || env.trim().isEmpty()) {
            env = Constants.QA_ENVIRONMENT; // Default to QA
        }
        LogManager.info("Current environment: {}", env);
        return env.toLowerCase();
    }


    private String getEnvironmentPropertiesFile(String environment) {
        return switch (environment.toLowerCase()) {
            case Constants.DEV_ENVIRONMENT -> Constants.DEV_PROPERTIES;
            case Constants.QA_ENVIRONMENT -> Constants.QA_PROPERTIES;
            case Constants.STAGING_ENVIRONMENT -> Constants.STAGING_PROPERTIES;
            case Constants.PROD_ENVIRONMENT -> Constants.PROD_PROPERTIES;
            default -> Constants.QA_PROPERTIES;
        };
    }


    public String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            // LogManager.warn("Property not found: {}", key);
        }
        return value;
    }


    public String getProperty(String key, String defaultValue) {
        String value = properties.getProperty(key, defaultValue);
        if (value.equals(defaultValue)) {
            LogManager.debug("Using default value for property {}: {}", key, defaultValue);
        }
        return value;
    }


    public int getPropertyAsInt(String key, int defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                //LogManager.warn("Invalid integer value for property {}: {}. Using default: {}", key, value, defaultValue);
            }
        }
        return defaultValue;
    }


    public boolean getPropertyAsBoolean(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }


    public String getCurrentEnvironment() {
        return currentEnvironment;
    }

    // ========== COMMONLY USED CONFIGURATION GETTERS ==========


    public String getBaseUrl() {
        return getProperty("base.url");
    }


    public String getBaseUrl_API() {
        return getProperty("api.base.url");
    }

    public String getUserName() {
        return getProperty("username");
    }

    public String getPassword() {
        return getProperty("password");
    }

    /**
     * Gets the browser type from configuration
     *
     * @return The browser type
     */
    public String getBrowser() {
        return getProperty(Constants.BROWSER_PROPERTY, Constants.CHROME);
    }

    /**
     * Gets whether to run in headless mode
     *
     * @return true if headless mode is enabled
     */
    public boolean isHeadless() {
        return getPropertyAsBoolean(Constants.HEADLESS_PROPERTY, false);
    }

    /**
     * Gets whether to run tests remotely
     *
     * @return true if remote execution is enabled
     */
    public boolean isRemote() {
        return getPropertyAsBoolean(Constants.REMOTE_PROPERTY, false);
    }


    public int getImplicitWaitTimeout() {
        return getPropertyAsInt("implicit.wait.timeout", Constants.IMPLICIT_WAIT_TIMEOUT);
    }


    public int getExplicitWaitTimeout() {
        return getPropertyAsInt("explicit.wait.timeout", Constants.EXPLICIT_WAIT_TIMEOUT);
    }

    public int getPageLoadTimeout() {
        return getPropertyAsInt("page.load.timeout", Constants.PAGE_LOAD_TIMEOUT);
    }


    private void loadTestData() {
        String testDataPath = getProperty("test.data.file");
        if (testDataPath == null) {
            LogManager.debug("No test.data.file property defined — skipping test data load.");
            return;
        }

        String fullPath = Constants.CONFIG_DIR + testDataPath;
        try {
            ObjectMapper mapper = new ObjectMapper();
            testData = mapper.readTree(new File(fullPath));
            LogManager.debug("Loaded test data from: {}", fullPath);
        } catch (IOException e) {
            LogManager.logConfigLoadFailure(fullPath, e);
            throw new RuntimeException("Failed to load test data from: " + fullPath, e);
        }
    }


    public String getTestData(String path) {
        if (testData == null) {
            synchronized (LOCK) {
                if (testData == null) loadTestData();
            }
        }
        JsonNode node = testData;
        for (String key : path.split("\\.")) {
            if (node == null || !node.has(key)) {
                LogManager.debug("Test data key not found at path segment '{}' in '{}'", key, path);
                return null;
            }
            node = node.get(key);
        }
        return (node != null && !node.isNull()) ? node.asText() : null;
    }


    public void reloadConfiguration() {
        LogManager.info("Reloading configuration for environment: {}", currentEnvironment);
        loadConfiguration();
    }


    public String get(String key) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.trim().isEmpty()) {
            return sys.trim();
        }
        return getProperty(key);
    }


    public String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }


    public int getInt(String key, int defaultValue) {
        return getPropertyAsInt(key, defaultValue);
    }


    public boolean getBoolean(String key, boolean defaultValue) {
        return getPropertyAsBoolean(key, defaultValue);
    }


    public String getHubUrl() {
        return get("hub.url");
    }



}