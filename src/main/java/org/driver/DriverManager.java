package org.driver;
import org.openqa.selenium.WebDriver;
import org.utilities.logs.LogManager;

public final class DriverManager {
    
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> browserTypeThreadLocal = new ThreadLocal<>();
    
    private DriverManager() {
        throw new UnsupportedOperationException("DriverManager is a utility class and cannot be instantiated");
    }
    

    public static void setDriver(WebDriver driver) {
        if (driver == null) {
            LogManager.error("Attempted to set null driver for thread: {}", getCurrentThreadId());
            throw new IllegalArgumentException("WebDriver cannot be null");
        }
        
        driverThreadLocal.set(driver);
        LogManager.debug("Driver set for thread: {}", getCurrentThreadId());
    }
    
    /**
     * Sets the WebDriver instance and browser type for the current thread
     * 
     * @param driver The WebDriver instance to set
     * @param browserType The browser type being used
     */
    public static void setDriver(WebDriver driver, String browserType) {
        setDriver(driver);
        setBrowserType(browserType);
    }
    
    /**
     * Gets the WebDriver instance for the current thread
     * 
     * @return WebDriver instance for current thread
     * @throws IllegalStateException if no driver is set for current thread
     */
    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        
        if (driver == null) {
            String threadId = getCurrentThreadId();
            LogManager.error("No driver found for thread: {}. Driver must be initialized before use.", threadId);
            throw new IllegalStateException("No WebDriver instance found for thread: " + threadId + 
                ". Please ensure driver is initialized before accessing it.");
        }
        
        return driver;
    }
    
    /**
     * Checks if a driver is set for the current thread
     * 
     * @return true if driver is set, false otherwise
     */
    public static boolean hasDriver() {
        return driverThreadLocal.get() != null;
    }
    
    /**
     * Sets the browser type for the current thread
     * 
     * @param browserType The browser type to set
     */
    public static void setBrowserType(String browserType) {
        browserTypeThreadLocal.set(browserType);
        LogManager.debug("Browser type set to '{}' for thread: {}", browserType, getCurrentThreadId());
    }
    
    /**
     * Gets the browser type for the current thread
     * 
     * @return Browser type for current thread, or "unknown" if not set
     */
    public static String getBrowserType() {
        String browserType = browserTypeThreadLocal.get();
        return browserType != null ? browserType : "unknown";
    }
    
    /**
     * Quits the WebDriver instance for the current thread and cleans up resources
     * This method should be called after test completion to prevent memory leaks
     */
    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        String threadId = getCurrentThreadId();
        
        if (driver != null) {
            try {
                driver.quit();
                LogManager.info("Driver successfully quit for thread: {}", threadId);
            } catch (Exception e) {
                LogManager.error("Error occurred while quitting driver for thread {}: {}", threadId, e.getMessage());
            } finally {
                // Clean up ThreadLocal variables to prevent memory leaks
                driverThreadLocal.remove();
                browserTypeThreadLocal.remove();
                LogManager.debug("ThreadLocal variables cleaned up for thread: {}", threadId);
            }
        } else {
        }
    }
    
    /**
     * Creates and sets a new WebDriver instance for the current thread
     * Uses DriverFactory to create the driver based on configuration
     * 
     * @return The created WebDriver instance
     */
    public static WebDriver initializeDriver() {
        try {
            WebDriver driver = DriverFactory.createDriver();
            setDriver(driver);
            LogManager.info("Driver initialized successfully for thread: {}", getCurrentThreadId());
            return driver;
        } catch (Exception e) {
            LogManager.error("Failed to initialize driver for thread {}: {}", getCurrentThreadId(), e.getMessage());
            throw new RuntimeException("Driver initialization failed", e);
        }
    }
    
    /**
     * Creates and sets a new WebDriver instance with specific browser type
     * 
     * @param browserType The browser type to create
     * @return The created WebDriver instance
     */
    public static WebDriver initializeDriver(String browserType) {
        try {
            WebDriver driver = DriverFactory.createDriver(browserType);
            setDriver(driver, browserType);
            LogManager.info("Driver initialized successfully for browser '{}' on thread: {}", 
                browserType, getCurrentThreadId());
            return driver;
        } catch (Exception e) {
            LogManager.error("Failed to initialize {} driver for thread {}: {}", 
                browserType, getCurrentThreadId(), e.getMessage());
            throw new RuntimeException("Driver initialization failed for browser: " + browserType, e);
        }
    }
    
    /**
     * Reinitializes the driver for the current thread
     * Quits the existing driver and creates a new one
     * 
     * @return The new WebDriver instance
     */
    public static WebDriver reinitializeDriver() {
        LogManager.info("Reinitializing driver for thread: {}", getCurrentThreadId());
        
        // Quit existing driver if present
        if (hasDriver()) {
            quitDriver();
        }
        
        // Initialize new driver
        return initializeDriver();
    }
    
    /**
     * Reinitializes the driver for the current thread with specific browser type
     * 
     * @param browserType The browser type to create
     * @return The new WebDriver instance
     */
    public static WebDriver reinitializeDriver(String browserType) {
        LogManager.info("Reinitializing driver for browser '{}' on thread: {}", 
            browserType, getCurrentThreadId());
        
        // Quit existing driver if present
        if (hasDriver()) {
            quitDriver();
        }
        
        // Initialize new driver
        return initializeDriver(browserType);
    }
    
    /**
     * Gets the current thread information for logging purposes
     * 
     * @return String representation of current thread
     */
    private static String getCurrentThreadId() {
        Thread currentThread = Thread.currentThread();
        return currentThread.getName() + "-" + currentThread.getId();
    }
    
    /**
     * Gets driver information for the current thread
     * 
     * @return String containing driver information
     */
    public static String getDriverInfo() {
        if (!hasDriver()) {
            return "No driver initialized for thread: " + getCurrentThreadId();
        }
        
        WebDriver driver = getDriver();
        String browserType = getBrowserType();
        String threadId = getCurrentThreadId();
        
        try {
            String currentUrl = driver.getCurrentUrl();
            String title = driver.getTitle();
            
            return String.format("Thread: %s, Browser: %s, URL: %s, Title: %s", 
                threadId, browserType, currentUrl, title);
        } catch (Exception e) {
            return String.format("Thread: %s, Browser: %s, Status: Driver session may be invalid", 
                threadId, browserType);
        }
    }
    
    /**
     * Closes the current browser window (but keeps driver session alive)
     * Use this method when you need to close a window but continue with other windows
     */
    public static void closeCurrentWindow() {
        if (hasDriver()) {
            try {
                WebDriver driver = getDriver();
                driver.close();
                LogManager.debug("Current window closed for thread: {}", getCurrentThreadId());
            } catch (Exception e) {
                LogManager.error("Error closing current window for thread {}: {}", 
                    getCurrentThreadId(), e.getMessage());
            }
        } else {
        }
    }
    
    /**
     * Quits all drivers across all threads
     * This method should only be used during framework shutdown
     * WARNING: Use with caution in parallel execution environments
     */
    public static void quitAllDrivers() {

        try {
            // This will only quit the driver for the current thread
            // ThreadLocal doesn't provide access to other threads' data
            quitDriver();
        } catch (Exception e) {
            LogManager.error("Error during driver cleanup: {}", e.getMessage());
        }
    }
    
    /**
     * Validates that the current driver session is still active
     *
     * @return true if driver session is active, false otherwise
     */
    public static boolean isDriverSessionActive() {
        if (!hasDriver()) {
            return false;
        }

        try {
            WebDriver driver = getDriver();
            // Try to get current URL - this will fail if session is invalid
            driver.getCurrentUrl();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 