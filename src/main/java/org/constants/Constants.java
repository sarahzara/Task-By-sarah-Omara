package org.constants;

public class Constants {
    private Constants() {
        throw new UnsupportedOperationException("Constants class — do not instantiate");
    }


    public static final String CONFIG_DIR = "src/test/resources/config/";

    public static final String APPLICATION_PROPERTIES = "application.properties";

    public static final String DEV_PROPERTIES     = "dev.properties";
    public static final String QA_PROPERTIES      = "qa.properties";
    public static final String STAGING_PROPERTIES = "staging.properties";
    public static final String PROD_PROPERTIES    = "prod.properties";


    public static final String ENVIRONMENT_KEY = "environment";
    public static final String BROWSER_KEY     = "browser";
    public static final String HEADLESS_KEY    = "headless";
    public static final String REMOTE_KEY      = "remote";
    public static final String HUB_URL_KEY     = "hub.url";

    // =========================================================================
    //  ENVIRONMENT NAMES  (must match pom.xml profile <id> values)
    // =========================================================================

    public static final String DEV_ENVIRONMENT     = "dev";
    public static final String QA_ENVIRONMENT      = "qa";
    public static final String STAGING_ENVIRONMENT = "staging";
    public static final String PROD_ENVIRONMENT    = "prod";

    /** Default used when no -Denvironment= is passed — matches activeByDefault profile */
    public static final String DEFAULT_ENVIRONMENT = QA_ENVIRONMENT;

    // =========================================================================
    //  BROWSER NAMES
    // =========================================================================

    public static final String CHROME  = "chrome";
    public static final String FIREFOX = "firefox";
    public static final String EDGE    = "edge";
    public static final String SAFARI  = "safari";

    public static final String DEFAULT_BROWSER = CHROME;

    // =========================================================================
    //  DEFAULT TIMEOUT VALUES  (seconds)

    public static final int IMPLICIT_WAIT_TIMEOUT = 10;
    public static final int EXPLICIT_WAIT_TIMEOUT = 20;
    public static final int PAGE_LOAD_TIMEOUT     = 30;
    public static final int SCRIPT_TIMEOUT        = 30;

    // =========================================================================
    //  TEST DATA
    // =========================================================================

    public static final String TEST_DATA_FILE     = "testData.json";
    public static final String TEST_DATA_DIR      = "src/test/resources/testdata/";

    // =========================================================================
    //  ALLURE
    // =========================================================================

    public static final String ALLURE_RESULTS_DIR = "target/allure-results";
    public static final String ALLURE_REPORT_DIR  = "target/allure-report";


    // ========== LOG CONSTANTS ==========
    public static final String TEST_START_MESSAGE = "Starting test: {}";
    public static final String TEST_END_MESSAGE = "Completed test: {}";
    public static final String TEST_FAILED_MESSAGE = "Test failed: {}";
    public static final String DRIVER_INITIALIZATION_MESSAGE = "Initializing {} driver";
    public static final String DRIVER_QUIT_MESSAGE = "Quitting driver for thread: {}";


    // ========== ERROR MESSAGES ==========
    public static final String ELEMENT_NOT_FOUND_ERROR = "Element not found: {}";
    public static final String TIMEOUT_ERROR = "Timeout occurred while waiting for element: {}";
    public static final String DRIVER_INITIALIZATION_ERROR = "Failed to initialize driver: {}";
    public static final String CONFIG_LOAD_ERROR = "Failed to load configuration: {}";
    public static final String SCREENSHOT_ERROR = "Failed to capture screenshot: {}";

    // ========== SUCCESS MESSAGES ==========
    public static final String ELEMENT_FOUND_SUCCESS = "Element found successfully: {}";
    public static final String DRIVER_INITIALIZED_SUCCESS = "Driver initialized successfully: {}";
    public static final String CONFIG_LOADED_SUCCESS = "Configuration loaded successfully: {}";
    public static final String SCREENSHOT_CAPTURED_SUCCESS = "Screenshot captured successfully: {}";

    // ========== SYSTEM PROPERTY KEYS ==========
    public static final String BROWSER_PROPERTY = "browser";
    public static final String ENVIRONMENT_PROPERTY = "environment";
    public static final String HEADLESS_PROPERTY = "headless";
    public static final String REMOTE_PROPERTY = "remote";


}
