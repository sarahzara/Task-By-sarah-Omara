package org.utilities.logs;
import org.apache.logging.log4j.Logger;
import org.constants.Constants;


public final class LogManager {

    private static final Logger TEST_LOGGER = org.apache.logging.log4j.LogManager.getLogger("AUTOMATION TEST CASES ");
    private static final Logger LAYER_LOGGER = org.apache.logging.log4j.LogManager.getLogger("AUTOMATION FRAMEWORK");

    private LogManager() {
        throw new UnsupportedOperationException("LogManager is a static class");
    }


        // =================== FRAMEWORK LAYER LOGGING METHODS ===================

        /**
         * Logs an informational message for framework operations.
         *
         * @param message the message to log
         * @param args arguments to format into the message
         */
        public static void info(String message, Object... args) {
            LAYER_LOGGER.info(message, args);
        }

    public static void debug(String message, Object... args) {
        LAYER_LOGGER.debug(message, args);
    }

        /**
         * Logs an error message for framework operations.
         *
         * @param message the error message to log
         * @param args arguments to format into the message
         */
        public static void error(String message, Object... args) {
            LAYER_LOGGER.error(message, args);
        }

        /**
         * Logs an error message with throwable for framework operations.
         *
         * @param message the error message to log
         * @param throwable the exception/error to log
         */
        public static void error(String message, Throwable throwable) {
            LAYER_LOGGER.error(message, throwable);
        }

        // =================== TEST EXECUTION LOGGING METHODS ===================

        /**
         * Logs an informational message for test execution.
         *
         * @param message the message to log
         * @param args arguments to format into the message
         */
        public static void testInfo(String message, Object... args) {
            TEST_LOGGER.info(message, args);
        }

        /**
         * Logs an error message for test execution.
         *
         * @param message the error message to log
         * @param args arguments to format into the message
         */
        public static void testError(String message, Object... args) {
            TEST_LOGGER.error(message, args);
        }

        /**
         * Logs an error message with throwable for test execution.
         *
         * @param message the error message to log
         * @param throwable the exception/error to log
         */
        public static void testError(String message, Throwable throwable) {
            TEST_LOGGER.error(message, throwable);
        }

        // =================== TEST LIFE CYCLE METHODS ===================

        /**
         * Logs the start of a test case.
         *
         * @param testName the name of the test case
         */
        public static void logTestStart(String testName) {
            testInfo("Test - Start : {}", testName);
        }

        /**
         * Logs the end of a test case.
         *
         * @param testName the name of the test case
         */
        public static void logTestEnd(String testName) {
            testInfo("Test - END : {}", testName);
        }

        /**
         * Logs a test case failure with exception details.
         *
         * @param testName the name of the test case
         * @param throwable the exception that caused the failure
         */
        public static void logTestFailure(String testName, Throwable throwable) {
            testError("Test - Failed : {}", testName);
            testError("Exception details: ", throwable);
        }

    public static void logTestSuccess(String testName) {
        testError("Test - Success : {}", testName);
    }

    public static void logTestSkipped(String testName, String throwable) {
        testError("Test - Skipped : {}", testName);
        testError("Exception details: ", throwable);
    }



    // =================== FRAMEWORK SPECIFIC METHODS ===================

        /**
         * Logs a timeout event while waiting for an element.
         *
         * @param elementDescription description of the element that timed out
         */
        public static void logTimeout(String elementDescription) {
            error("Timeout occurred while waiting for element: {}", elementDescription);
        }


    /**
     * Logs screenshot capture success
     *
     * @param screenshotPath The path where the screenshot was saved
     */
    public static void logScreenshotCaptured(String screenshotPath) {
        info("Screenshot captured successfully: {}", screenshotPath);
    }

    /**
     * Logs screenshot capture failure
     *
     * @param throwable The exception that occurred during screenshot capture
     */
    public static void logScreenshotFailure(Throwable throwable) {
        error( "Failed to capture screenshot: {}", throwable.getMessage());
        error("Screenshot capture exception: ", throwable);
    }

    /**
     * Logs configuration loading success
     *
     * @param configFile The configuration file that was loaded
     */
    public static void logConfigLoaded(String configFile) {
        info(Constants.CONFIG_LOADED_SUCCESS, configFile);
    }

    /**
     * Logs configuration loading failure
     *
     * @param configFile The configuration file that failed to load
     * @param throwable The exception that occurred
     */
    public static void logConfigLoadFailure(String configFile, Throwable throwable) {
        error(Constants.CONFIG_LOAD_ERROR, configFile);
        error("Configuration load exception: ", throwable);
    }

}
