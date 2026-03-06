package org.utilities.exceptions;


public class FrameworkException extends RuntimeException {
    
    private final FrameworkExceptionHandler.ExceptionInfo exceptionInfo;
    
    /**
     * Creates a FrameworkException with detailed exception information
     * 
     * @param exceptionInfo Comprehensive exception information
     */
    public FrameworkException(FrameworkExceptionHandler.ExceptionInfo exceptionInfo) {
        super(buildMessage(exceptionInfo), exceptionInfo.getException());
        this.exceptionInfo = exceptionInfo;
    }
    
    /**
     * Creates a FrameworkException with message and cause
     * 
     * @param message Exception message
     * @param cause Root cause exception
     */
    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
        this.exceptionInfo = null;
    }
    
    /**
     * Creates a FrameworkException with message only
     * 
     * @param message Exception message
     */
    public FrameworkException(String message) {
        super(message);
        this.exceptionInfo = null;
    }
    
    /**
     * Gets the comprehensive exception information
     * 
     * @return Exception information or null if not available
     */
    public FrameworkExceptionHandler.ExceptionInfo getExceptionInfo() {
        return exceptionInfo;
    }
    
    /**
     * Gets the exception category
     * 
     * @return Exception category or UNKNOWN if not available
     */
    public FrameworkExceptionHandler.ExceptionCategory getCategory() {
        return exceptionInfo != null ? exceptionInfo.getCategory() : FrameworkExceptionHandler.ExceptionCategory.UNKNOWN;
    }
    
    /**
     * Gets the test name where exception occurred
     * 
     * @return Test name or null if not available
     */
    public String getTestName() {
        return exceptionInfo != null ? exceptionInfo.getTestName() : null;
    }
    
    /**
     * Gets the context where exception occurred
     * 
     * @return Context information or null if not available
     */
    public String getContext() {
        return exceptionInfo != null ? exceptionInfo.getContext() : null;
    }
    
    /**
     * Gets the screenshot path captured during exception
     * 
     * @return Screenshot path or null if not available
     */
    public String getScreenshotPath() {
        return exceptionInfo != null ? exceptionInfo.getScreenshotPath() : null;
    }
    
    /**
     * Gets recovery suggestion for this exception
     * 
     * @return Recovery suggestion or null if not available
     */
    public String getRecoverySuggestion() {
        return exceptionInfo != null ? exceptionInfo.getRecoverySuggestion() : null;
    }
    
    /**
     * Builds a comprehensive error message from exception information
     * 
     * @param exceptionInfo Exception information
     * @return Formatted error message
     */
    private static String buildMessage(FrameworkExceptionHandler.ExceptionInfo exceptionInfo) {
        if (exceptionInfo == null) {
            return "Framework exception occurred";
        }
        
        StringBuilder message = new StringBuilder();
        message.append("Framework Exception in test '").append(exceptionInfo.getTestName()).append("'");
        
        if (exceptionInfo.getContext() != null && !exceptionInfo.getContext().isEmpty()) {
            message.append(" - Context: ").append(exceptionInfo.getContext());
        }
        
        message.append(" - Category: ").append(exceptionInfo.getCategory());
        
        if (exceptionInfo.getException() != null && exceptionInfo.getException().getMessage() != null) {
            message.append(" - ").append(exceptionInfo.getException().getMessage());
        }
        
        if (exceptionInfo.getScreenshotPath() != null) {
            message.append(" - Screenshot: ").append(exceptionInfo.getScreenshotPath());
        }
        
        return message.toString();
    }
    
    /**
     * Checks if this exception has detailed information
     * 
     * @return true if detailed information is available
     */
    public boolean hasDetailedInfo() {
        return exceptionInfo != null;
    }
    
    /**
     * Checks if a screenshot was captured for this exception
     * 
     * @return true if screenshot is available
     */
    public boolean hasScreenshot() {
        return exceptionInfo != null && exceptionInfo.getScreenshotPath() != null;
    }
    
    @Override
    public String toString() {
        if (exceptionInfo != null) {
            return String.format("FrameworkException[test=%s, category=%s, type=%s, hasScreenshot=%s]",
                exceptionInfo.getTestName(),
                exceptionInfo.getCategory(),
                exceptionInfo.getException().getClass().getSimpleName(),
                hasScreenshot());
        } else {
            return super.toString();
        }
    }
} 