package org.utilities.retry;


import org.config.ConfigReader;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.utilities.logs.LogManager;
import org.utilities.screenshot.Screenshot;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class SmartRetryAnalyzer implements IRetryAnalyzer {
    
    private static final ConfigReader config = ConfigReader.getInstance();

    // ========== RETRY CONSTANTS ==========
    public static final int DEFAULT_RETRY_COUNT = 2;
    public static final long RETRY_DELAY_MILLISECONDS = 1000;
    public static final int MAX_RETRY_ATTEMPTS = 3;
    
    // Track retry counts per test method to prevent infinite retries
    private static final ConcurrentHashMap<String, AtomicInteger> retryCountMap = new ConcurrentHashMap<>();
    
    // Track retry history for reporting and analysis
    private static final ConcurrentHashMap<String, RetryHistory> retryHistoryMap = new ConcurrentHashMap<>();
    
    @Override
    public boolean retry(ITestResult result) {
        String testKey = getTestKey(result);
        Method testMethod = result.getMethod().getConstructorOrMethod().getMethod();
        
        LogManager.debug("Analyzing retry for test: {}", testKey);
        
        // Check if test has SmartRetry annotation
        SmartRetry smartRetry = testMethod.getAnnotation(SmartRetry.class);
        
        if (smartRetry != null) {
            return handleAnnotationBasedRetry(result, smartRetry, testKey);
        } else {
            return handleConfigurationBasedRetry(result, testKey);
        }
    }
    
    /**
     * Handles retry logic for tests with SmartRetry annotation
     * 
     * @param result Test result
     * @param smartRetry SmartRetry annotation
     * @param testKey Unique test identifier
     * @return true if retry should be attempted
     */
    private boolean handleAnnotationBasedRetry(ITestResult result, SmartRetry smartRetry, String testKey) {
        AtomicInteger retryCount = retryCountMap.computeIfAbsent(testKey, k -> new AtomicInteger(0));
        int currentRetryCount = retryCount.get();
        
        LogManager.info("Test {} has SmartRetry annotation - Retry attempt {}/{}", 
            testKey, currentRetryCount + 1, smartRetry.maxRetries());
        
        // Check if we've exceeded max retries
        if (currentRetryCount >= smartRetry.maxRetries()) {
            //LogManager.warn("Maximum retries ({}) exceeded for test: {}", smartRetry.maxRetries(), testKey);
            recordRetryHistory(testKey, smartRetry.maxRetries(), false, "Max retries exceeded");
            return false;
        }
        
        // Check if the exception type matches retryOn conditions
        Throwable exception = result.getThrowable();
        if (!shouldRetryForException(exception, smartRetry.retryOn())) {
            LogManager.info("Exception type {} not in retryOn list for test: {}", 
                exception.getClass().getSimpleName(), testKey);
            recordRetryHistory(testKey, currentRetryCount, false, "Exception type not retryable");
            return false;
        }
        
        // Increment retry count
        retryCount.incrementAndGet();
        
        // Apply delay between retries
        applyRetryDelay(smartRetry, currentRetryCount);
        
        // Capture screenshot if enabled
        if (smartRetry.captureScreenshotOnRetry()) {
            captureRetryScreenshot(testKey, currentRetryCount + 1);
        }
        
        // Log retry attempt
        LogManager.info("Retrying test: {} (Attempt {}/{}) - Reason: {}", 
            testKey, currentRetryCount + 1, smartRetry.maxRetries(), smartRetry.reason());
        
        recordRetryHistory(testKey, currentRetryCount + 1, true, smartRetry.reason());
        
        return true;
    }
    
    /**
     * Handles retry logic based on global configuration
     * 
     * @param result Test result
     * @param testKey Unique test identifier
     * @return true if retry should be attempted
     */
    private boolean handleConfigurationBasedRetry(ITestResult result, String testKey) {
        AtomicInteger retryCount = retryCountMap.computeIfAbsent(testKey, k -> new AtomicInteger(0));
        int currentRetryCount = retryCount.get();
        int maxRetries = config.getPropertyAsInt("retry.max.attempts", DEFAULT_RETRY_COUNT);
        
        LogManager.debug("Using configuration-based retry for test: {} - Retry attempt {}/{}", 
            testKey, currentRetryCount + 1, maxRetries);
        
        // Check if we've exceeded max retries
        if (currentRetryCount >= maxRetries) {
            //LogManager.warn("Maximum retries ({}) exceeded for test: {}", maxRetries, testKey);
            recordRetryHistory(testKey, maxRetries, false, "Max retries exceeded (config-based)");
            return false;
        }
        
        // Analyze failure pattern
        Throwable exception = result.getThrowable();
        if (!shouldRetryBasedOnFailureAnalysis(exception)) {
            LogManager.info("Failure analysis indicates test should not be retried: {}", testKey);
            recordRetryHistory(testKey, currentRetryCount, false, "Failure analysis - non-retryable");
            return false;
        }
        
        // Increment retry count
        retryCount.incrementAndGet();
        
        // Apply default delay
//        long delay = config.getPropertyAsInt("retry.delay.milliseconds",
//            (int) FrameworkConstants.RETRY_DELAY_MILLISECONDS);
//        applyDelay(delay);
        
        // Capture screenshot
        captureRetryScreenshot(testKey, currentRetryCount + 1);
        
        LogManager.info("Retrying test: {} (Attempt {}/{}) - Configuration-based retry", 
            testKey, currentRetryCount + 1, maxRetries);
        
        recordRetryHistory(testKey, currentRetryCount + 1, true, "Configuration-based retry");
        
        return true;
    }
    
    /**
     * Checks if the exception type should trigger a retry
     * 
     * @param exception The exception that occurred
     * @param retryOnExceptions Array of exception types to retry on
     * @return true if should retry
     */
    private boolean shouldRetryForException(Throwable exception, Class<? extends Throwable>[] retryOnExceptions) {
        if (exception == null) {
            return false;
        }
        
        for (Class<? extends Throwable> retryableException : retryOnExceptions) {
            if (retryableException.isAssignableFrom(exception.getClass())) {
                LogManager.debug("Exception {} matches retryable exception {}", 
                    exception.getClass().getSimpleName(), retryableException.getSimpleName());
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Analyzes failure patterns to determine if retry should be attempted
     * 
     * @param exception The exception that occurred
     * @return true if should retry based on failure analysis
     */
    private boolean shouldRetryBasedOnFailureAnalysis(Throwable exception) {
        if (exception == null) {
            return false;
        }
        
        String exceptionMessage = exception.getMessage() != null ? exception.getMessage().toLowerCase() : "";
        
        // Retryable exceptions/conditions
        String[] retryablePatterns = {
            "timeout", "connection", "network", "stale element", "element not found",
            "element not interactable", "server error", "socket", "session"
        };
        
        for (String pattern : retryablePatterns) {
            if (exceptionMessage.contains(pattern)) {
                LogManager.debug("Exception message contains retryable pattern '{}': {}", pattern, exceptionMessage);
                return true;
            }
        }
        
        // Non-retryable exceptions/conditions
        String[] nonRetryablePatterns = {
            "assertion", "no such method", "class not found", "illegal argument",
            "null pointer", "array index", "security"
        };
        
        for (String pattern : nonRetryablePatterns) {
            if (exceptionMessage.contains(pattern)) {
                LogManager.debug("Exception message contains non-retryable pattern '{}': {}", pattern, exceptionMessage);
                return false;
            }
        }
        
        // Default: retry for most exceptions
        return true;
    }
    
    /**
     * Applies retry delay with progressive backoff if enabled
     * 
     * @param smartRetry SmartRetry annotation
     * @param currentRetryCount Current retry attempt number
     */
    private void applyRetryDelay(SmartRetry smartRetry, int currentRetryCount) {
        long delay = smartRetry.delayBetweenRetries();
        
        if (smartRetry.progressiveDelay()) {
            // Exponential backoff: delay * (2 ^ retryCount)
            delay = Math.min(delay * (long) Math.pow(2, currentRetryCount), smartRetry.maxDelay());
            LogManager.debug("Applied progressive delay: {}ms for retry attempt {}", delay, currentRetryCount + 1);
        }
        
        applyDelay(delay);
    }
    
    /**
     * Applies delay between retry attempts
     * 
     * @param delayMilliseconds Delay in milliseconds
     */
    private void applyDelay(long delayMilliseconds) {
        if (delayMilliseconds > 0) {
            LogManager.debug("Applying retry delay: {}ms", delayMilliseconds);
            try {
                Thread.sleep(delayMilliseconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                //LogManager.warn("Retry delay interrupted: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Captures screenshot during retry attempt
     * 
     * @param testKey Test identifier
     * @param retryAttempt Retry attempt number
     */
    private void captureRetryScreenshot(String testKey, int retryAttempt) {
        try {
            String screenshotPath = Screenshot.captureRetryScreenshot(testKey, retryAttempt);
            if (screenshotPath != null) {
                LogManager.debug("Retry screenshot captured: {} for test: {}, attempt: {}", 
                    screenshotPath, testKey, retryAttempt);
            }
        } catch (Exception e) {
            //LogManager.warn("Failed to capture retry screenshot for test {}: {}", testKey, e.getMessage());
        }
    }
    
    /**
     * Records retry history for reporting and analysis
     * 
     * @param testKey Test identifier
     * @param attemptNumber Attempt number
     * @param retryPerformed Whether retry was performed
     * @param reason Reason for retry decision
     */
    private void recordRetryHistory(String testKey, int attemptNumber, boolean retryPerformed, String reason) {
        RetryHistory history = retryHistoryMap.computeIfAbsent(testKey, k -> new RetryHistory(testKey));
        history.addAttempt(attemptNumber, retryPerformed, reason, System.currentTimeMillis());
        
        LogManager.debug("Recorded retry history for {}: Attempt {}, Retry: {}, Reason: {}", 
            testKey, attemptNumber, retryPerformed, reason);
    }
    
    /**
     * Gets unique test identifier
     * 
     * @param result Test result
     * @return Unique test key
     */
    private String getTestKey(ITestResult result) {
        return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
    }
    
    /**
     * Gets retry statistics for a specific test
     * 
     * @param testKey Test identifier
     * @return Retry count for the test
     */
    public static int getRetryCount(String testKey) {
        AtomicInteger retryCount = retryCountMap.get(testKey);
        return retryCount != null ? retryCount.get() : 0;
    }
    
    /**
     * Gets retry history for a specific test
     * 
     * @param testKey Test identifier
     * @return Retry history
     */
    public static RetryHistory getRetryHistory(String testKey) {
        return retryHistoryMap.get(testKey);
    }
    
    /**
     * Clears retry data (useful for test cleanup)
     */
    public static void clearRetryData() {
        retryCountMap.clear();
        retryHistoryMap.clear();
        LogManager.debug("Retry data cleared");
    }
    
    /**
     * Gets total retry statistics across all tests
     * 
     * @return Retry statistics summary
     */
    public static String getRetryStatistics() {
        int totalTests = retryCountMap.size();
        int totalRetries = retryCountMap.values().stream().mapToInt(AtomicInteger::get).sum();
        int retriedTests = (int) retryCountMap.values().stream().filter(count -> count.get() > 0).count();
        
        return String.format("Retry Statistics - Tests: %d, Retried Tests: %d, Total Retries: %d", 
            totalTests, retriedTests, totalRetries);
    }
} 