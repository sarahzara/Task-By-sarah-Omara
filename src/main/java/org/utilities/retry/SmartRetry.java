package org.utilities.retry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SmartRetry {
    
    /**
     * Maximum number of retry attempts for the test method
     * 
     * @return Maximum retry count (default: 2)
     */
    int maxRetries() default 2;
    
    /**
     * Exception types that should trigger a retry
     * 
     * @return Array of exception classes (default: Exception.class)
     */
    Class<? extends Throwable>[] retryOn() default {Exception.class};
    
    /**
     * Delay in milliseconds between retry attempts
     * 
     * @return Delay between retries in milliseconds (default: 1000ms)
     */
    long delayBetweenRetries() default 1000;
    
    /**
     * Reason for enabling retry for this test method
     * Used for logging and reporting purposes
     * 
     * @return Reason for retry (default: "Transient failure recovery")
     */
    String reason() default "Transient failure recovery";
    
    /**
     * Whether to enable progressive delay (exponential backoff)
     * If true, delay will increase with each retry attempt
     * 
     * @return true to enable progressive delay (default: false)
     */
    boolean progressiveDelay() default false;
    
    /**
     * Maximum delay in milliseconds when using progressive delay
     * 
     * @return Maximum delay in milliseconds (default: 10000ms)
     */
    long maxDelay() default 10000;
    
    /**
     * Whether to capture screenshot on retry
     * 
     * @return true to capture screenshot on each retry (default: true)
     */
    boolean captureScreenshotOnRetry() default true;
    
    /**
     * Priority level for retry processing
     * Higher values indicate higher priority
     * 
     * @return Priority level (default: 0)
     */
    int priority() default 0;
} 