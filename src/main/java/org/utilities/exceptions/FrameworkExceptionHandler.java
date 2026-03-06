package org.utilities.exceptions;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.utilities.logs.LogManager;
import org.utilities.screenshot.Screenshot;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public final class FrameworkExceptionHandler {
    
    // Exception statistics tracking
    private static final Map<String, Integer> exceptionCounts = new ConcurrentHashMap<>();
    private static final Map<String, ExceptionInfo> lastExceptions = new ConcurrentHashMap<>();
    
    // Date formatter for error reporting
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Prevent instantiation
    private FrameworkExceptionHandler() {
        throw new UnsupportedOperationException("FrameworkExceptionHandler is a utility class and cannot be instantiated");
    }
    
    /**
     * Handles exceptions with automatic screenshot capture and detailed logging
     * 
     * @param exception The exception that occurred
     * @param context Context information about where the exception occurred
     * @param testName Name of the test or method where exception occurred
     * @return FrameworkException with detailed information
     */
    public static FrameworkException handleException(Throwable exception, String context, String testName) {
        // Capture screenshot immediately
        String screenshotPath = captureExceptionScreenshot(testName, exception);
        
        // Classify the exception
        ExceptionCategory category = classifyException(exception);
        
        // Log the exception with full details
        logException(exception, context, testName, category, screenshotPath);
        
        // Update exception statistics
        updateExceptionStatistics(exception, context);
        
        // Get recovery suggestions
        String recoverySuggestion = getRecoverySuggestion(exception, category);
        
        // Create detailed exception information
        ExceptionInfo exceptionInfo = new ExceptionInfo(
            exception,
            context,
            testName,
            category,
            screenshotPath,
            recoverySuggestion,
            LocalDateTime.now()
        );
        
        // Store for analysis
        lastExceptions.put(testName, exceptionInfo);
        
        // Return comprehensive framework exception
        return new FrameworkException(exceptionInfo);
    }
    
    /**
     * Handles exceptions with minimal context
     * 
     * @param exception The exception that occurred
     * @param testName Name of the test where exception occurred
     * @return FrameworkException with detailed information
     */
    public static FrameworkException handleException(Throwable exception, String testName) {
        return handleException(exception, "Unknown context", testName);
    }
    
    /**
     * Handles unexpected exceptions during test execution
     * 
     * @param exception The unexpected exception
     * @param methodName Name of the method where exception occurred
     * @param className Name of the class where exception occurred
     * @return FrameworkException with detailed information
     */
    public static FrameworkException handleUnexpectedException(Throwable exception, String methodName, String className) {
        String context = String.format("Unexpected exception in %s.%s", className, methodName);
        String testName = String.format("%s_%s_unexpected", className, methodName);
        
        LogManager.error("Unexpected exception caught: {}", exception.getMessage());
        
        return handleException(exception, context, testName);
    }
    
    /**
     * Classifies exceptions into categories for better handling
     * 
     * @param exception The exception to classify
     * @return Exception category
     */
    private static ExceptionCategory classifyException(Throwable exception) {
        if (exception == null) {
            return ExceptionCategory.UNKNOWN;
        }
        
        // WebDriver specific exceptions
        if (exception instanceof NoSuchElementException) {
            return ExceptionCategory.ELEMENT_NOT_FOUND;
        }
        if (exception instanceof StaleElementReferenceException) {
            return ExceptionCategory.STALE_ELEMENT;
        }
        if (exception instanceof TimeoutException) {
            return ExceptionCategory.TIMEOUT;
        }
        if (exception instanceof WebDriverException) {
            return ExceptionCategory.WEBDRIVER_ERROR;
        }
        
        // Network and connection issues
        if (isNetworkRelated(exception)) {
            return ExceptionCategory.NETWORK_ERROR;
        }
        
        // Assertion failures
        if (isAssertionError(exception)) {
            return ExceptionCategory.ASSERTION_FAILURE;
        }
        
        // Configuration errors
        if (isConfigurationError(exception)) {
            return ExceptionCategory.CONFIGURATION_ERROR;
        }
        
        // Test data issues
        if (isTestDataError(exception)) {
            return ExceptionCategory.TEST_DATA_ERROR;
        }
        
        // Application errors
        if (isApplicationError(exception)) {
            return ExceptionCategory.APPLICATION_ERROR;
        }
        
        // Default to unknown
        return ExceptionCategory.UNKNOWN;
    }
    
    /**
     * Captures screenshot when exception occurs
     * 
     * @param testName Name of the test
     * @param exception The exception that occurred
     * @return Path to the captured screenshot
     */
    private static String captureExceptionScreenshot(String testName, Throwable exception) {
        try {
            String exceptionType = exception.getClass().getSimpleName();
            String screenshotName = String.format("exception_%s_%s", testName, exceptionType);
            return Screenshot.captureFailureScreenshot(screenshotName);
        } catch (Exception screenshotException) {
            //LogManager.warn("Failed to capture exception screenshot: {}", screenshotException.getMessage());
            return null;
        }
    }
    
    /**
     * Logs exception with comprehensive details
     * 
     * @param exception The exception
     * @param context Context information
     * @param testName Test name
     * @param category Exception category
     * @param screenshotPath Path to screenshot
     */
    private static void logException(Throwable exception, String context, String testName, 
                                   ExceptionCategory category, String screenshotPath) {
        
        LogManager.error("=== EXCEPTION DETAILS ===");
        LogManager.error("Test: {}", testName);
        LogManager.error("Context: {}", context);
        LogManager.error("Category: {}", category);
        LogManager.error("Exception Type: {}", exception.getClass().getName());
        LogManager.error("Message: {}", exception.getMessage());
        LogManager.error("Timestamp: {}", LocalDateTime.now().format(dateFormatter));
        
        if (screenshotPath != null) {
            LogManager.error("Screenshot: {}", screenshotPath);
        }
        
        // Log stack trace
        LogManager.error("Stack Trace:");
        LogManager.error(getStackTraceAsString(exception));
        
        // Log root cause if different
        Throwable rootCause = getRootCause(exception);
        if (rootCause != exception) {
            LogManager.error("Root Cause: {}", rootCause.getClass().getName());
            LogManager.error("Root Cause Message: {}", rootCause.getMessage());
        }
        
        LogManager.error("=== END EXCEPTION DETAILS ===");
    }
    
    /**
     * Updates exception statistics for analysis
     * 
     * @param exception The exception
     * @param context Context information
     */
    private static void updateExceptionStatistics(Throwable exception, String context) {
        String exceptionKey = exception.getClass().getSimpleName();
        exceptionCounts.merge(exceptionKey, 1, Integer::sum);
        
        // Log if this exception type is becoming frequent
        int count = exceptionCounts.get(exceptionKey);
        if (count > 1 && count % 5 == 0) {
            //LogManager.warn("Exception type '{}' has occurred {} times", exceptionKey, count);
        }
    }
    
    /**
     * Provides recovery suggestions based on exception type
     * 
     * @param exception The exception
     * @param category Exception category
     * @return Recovery suggestion
     */
    private static String getRecoverySuggestion(Throwable exception, ExceptionCategory category) {
        return switch (category) {
            case ELEMENT_NOT_FOUND -> "Verify element locator is correct and element exists. Consider increasing wait time or checking if element is in a different frame.";
            case STALE_ELEMENT -> "Re-find the element before interacting with it. This usually happens when page content changes after element is found.";
            case TIMEOUT -> "Increase wait timeout or verify that the expected condition is achievable. Check if page is loading slowly.";
            case WEBDRIVER_ERROR -> "Check WebDriver session is active. Consider reinitializing the driver or using a different browser.";
            case NETWORK_ERROR -> "Check network connectivity and application availability. Verify URLs and endpoints are accessible.";
            case ASSERTION_FAILURE -> "Review test expectations and verify application behavior. Check if test data or application state is correct.";
            case CONFIGURATION_ERROR -> "Verify configuration files and environment settings. Check property values and file paths.";
            case TEST_DATA_ERROR -> "Validate test data format and availability. Ensure data files exist and contain expected values.";
            case APPLICATION_ERROR -> "Check application logs for server-side errors. Verify application is in expected state.";
            default -> "Review the exception details and consider debugging the specific failure scenario.";
        };
    }
    
    /**
     * Gets the root cause of an exception
     * 
     * @param exception The exception
     * @return Root cause exception
     */
    private static Throwable getRootCause(Throwable exception) {
        Throwable cause = exception;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }
    
    /**
     * Converts stack trace to string
     * 
     * @param exception The exception
     * @return Stack trace as string
     */
    private static String getStackTraceAsString(Throwable exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }
    
    /**
     * Checks if exception is network related
     * 
     * @param exception The exception
     * @return true if network related
     */
    private static boolean isNetworkRelated(Throwable exception) {
        String message = exception.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("connection") || 
                   lowerMessage.contains("network") || 
                   lowerMessage.contains("timeout") ||
                   lowerMessage.contains("unreachable") ||
                   lowerMessage.contains("socket");
        }
        return false;
    }
    
    /**
     * Checks if exception is an assertion error
     * 
     * @param exception The exception
     * @return true if assertion error
     */
    private static boolean isAssertionError(Throwable exception) {
        return exception instanceof AssertionError ||
               exception.getClass().getName().contains("Assert") ||
               (exception.getMessage() != null && exception.getMessage().contains("expected"));
    }
    
    /**
     * Checks if exception is configuration related
     * 
     * @param exception The exception
     * @return true if configuration error
     */
    private static boolean isConfigurationError(Throwable exception) {
        String message = exception.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("configuration") ||
                   lowerMessage.contains("property") ||
                   lowerMessage.contains("file not found") ||
                   lowerMessage.contains("invalid");
        }
        return false;
    }
    
    /**
     * Checks if exception is test data related
     * 
     * @param exception The exception
     * @return true if test data error
     */
    private static boolean isTestDataError(Throwable exception) {
        String message = exception.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("data") ||
                   lowerMessage.contains("csv") ||
                   lowerMessage.contains("json") ||
                   lowerMessage.contains("excel");
        }
        return false;
    }
    
    /**
     * Checks if exception is application related
     * 
     * @param exception The exception
     * @return true if application error
     */
    private static boolean isApplicationError(Throwable exception) {
        String message = exception.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("server error") ||
                   lowerMessage.contains("500") ||
                   lowerMessage.contains("404") ||
                   lowerMessage.contains("application");
        }
        return false;
    }
    
    /**
     * Gets exception statistics
     * 
     * @return Map of exception types and their counts
     */
    public static Map<String, Integer> getExceptionStatistics() {
        return new HashMap<>(exceptionCounts);
    }
    
    /**
     * Gets the last exception for a specific test
     * 
     * @param testName Name of the test
     * @return Exception information
     */
    public static ExceptionInfo getLastException(String testName) {
        return lastExceptions.get(testName);
    }
    
    /**
     * Clears all exception statistics and history
     */
    public static void clearExceptionData() {
        exceptionCounts.clear();
        lastExceptions.clear();
        LogManager.debug("Exception data cleared");
    }
    
    /**
     * Gets a summary of all exceptions
     * 
     * @return Exception summary string
     */
    public static String getExceptionSummary() {
        if (exceptionCounts.isEmpty()) {
            return "No exceptions recorded";
        }
        
        StringBuilder summary = new StringBuilder("Exception Summary:\n");
        exceptionCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> summary.append(String.format("  %s: %d times\n", entry.getKey(), entry.getValue())));
        
        return summary.toString();
    }
    
    /**
     * Exception categories for classification
     */
    public enum ExceptionCategory {
        ELEMENT_NOT_FOUND,
        STALE_ELEMENT,
        TIMEOUT,
        WEBDRIVER_ERROR,
        NETWORK_ERROR,
        ASSERTION_FAILURE,
        CONFIGURATION_ERROR,
        TEST_DATA_ERROR,
        APPLICATION_ERROR,
        UNKNOWN
    }
    
    /**
     * Comprehensive exception information holder
     */
    public static class ExceptionInfo {
        private final Throwable exception;
        private final String context;
        private final String testName;
        private final ExceptionCategory category;
        private final String screenshotPath;
        private final String recoverySuggestion;
        private final LocalDateTime timestamp;
        
        public ExceptionInfo(Throwable exception, String context, String testName, 
                           ExceptionCategory category, String screenshotPath, 
                           String recoverySuggestion, LocalDateTime timestamp) {
            this.exception = exception;
            this.context = context;
            this.testName = testName;
            this.category = category;
            this.screenshotPath = screenshotPath;
            this.recoverySuggestion = recoverySuggestion;
            this.timestamp = timestamp;
        }
        
        // Getters
        public Throwable getException() { return exception; }
        public String getContext() { return context; }
        public String getTestName() { return testName; }
        public ExceptionCategory getCategory() { return category; }
        public String getScreenshotPath() { return screenshotPath; }
        public String getRecoverySuggestion() { return recoverySuggestion; }
        public LocalDateTime getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("ExceptionInfo[test=%s, category=%s, type=%s, time=%s]",
                testName, category, exception.getClass().getSimpleName(), timestamp.format(dateFormatter));
        }
    }
} 