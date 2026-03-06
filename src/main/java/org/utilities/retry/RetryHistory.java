package org.utilities.retry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RetryHistory {
    
    private final String testKey;
    private final List<RetryAttempt> attempts;
    private final long creationTime;
    
    /**
     * Creates a new RetryHistory for a test
     * 
     * @param testKey Unique identifier for the test
     */
    public RetryHistory(String testKey) {
        this.testKey = testKey;
        this.attempts = new CopyOnWriteArrayList<>(); // Thread-safe for concurrent access
        this.creationTime = System.currentTimeMillis();
    }
    
    /**
     * Adds a retry attempt to the history
     * 
     * @param attemptNumber The attempt number
     * @param retryPerformed Whether a retry was actually performed
     * @param reason The reason for the retry decision
     * @param timestamp The timestamp of the attempt
     */
    public void addAttempt(int attemptNumber, boolean retryPerformed, String reason, long timestamp) {
        RetryAttempt attempt = new RetryAttempt(attemptNumber, retryPerformed, reason, timestamp);
        attempts.add(attempt);
    }
    
    /**
     * Gets the test key
     * 
     * @return The unique test identifier
     */
    public String getTestKey() {
        return testKey;
    }
    
    /**
     * Gets all retry attempts
     * 
     * @return List of retry attempts
     */
    public List<RetryAttempt> getAttempts() {
        return new ArrayList<>(attempts); // Return defensive copy
    }
    
    /**
     * Gets the total number of retry attempts
     * 
     * @return Total number of attempts
     */
    public int getTotalAttempts() {
        return attempts.size();
    }
    
    /**
     * Gets the number of successful retry attempts
     * 
     * @return Number of successful retries
     */
    public int getSuccessfulRetries() {
        return (int) attempts.stream().filter(RetryAttempt::isRetryPerformed).count();
    }
    
    /**
     * Gets the number of failed retry attempts
     * 
     * @return Number of failed retries
     */
    public int getFailedRetries() {
        return (int) attempts.stream().filter(attempt -> !attempt.isRetryPerformed()).count();
    }
    
    /**
     * Gets the creation time of this history
     * 
     * @return Creation timestamp
     */
    public long getCreationTime() {
        return creationTime;
    }
    
    /**
     * Gets the last attempt
     * 
     * @return The most recent retry attempt, or null if no attempts
     */
    public RetryAttempt getLastAttempt() {
        return attempts.isEmpty() ? null : attempts.get(attempts.size() - 1);
    }
    
    /**
     * Gets the total duration from first to last attempt
     * 
     * @return Duration in milliseconds, or 0 if less than 2 attempts
     */
    public long getTotalDuration() {
        if (attempts.size() < 2) {
            return 0;
        }
        
        long firstAttemptTime = attempts.get(0).getTimestamp();
        long lastAttemptTime = attempts.get(attempts.size() - 1).getTimestamp();
        
        return lastAttemptTime - firstAttemptTime;
    }
    
    /**
     * Checks if the test was ultimately successful after retries
     * 
     * @return true if the last attempt was a successful retry
     */
    public boolean wasUltimatelySuccessful() {
        RetryAttempt lastAttempt = getLastAttempt();
        return lastAttempt != null && lastAttempt.isRetryPerformed();
    }
    
    /**
     * Gets a summary of the retry history
     * 
     * @return Summary string
     */
    public String getSummary() {
        return String.format("RetryHistory[test=%s, attempts=%d, successful=%d, failed=%d, duration=%dms]",
            testKey, getTotalAttempts(), getSuccessfulRetries(), getFailedRetries(), getTotalDuration());
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RetryHistory for ").append(testKey).append(":\n");
        
        for (RetryAttempt attempt : attempts) {
            sb.append("  ").append(attempt.toString()).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Inner class representing a single retry attempt
     */
    public static class RetryAttempt {
        private final int attemptNumber;
        private final boolean retryPerformed;
        private final String reason;
        private final long timestamp;
        
        /**
         * Creates a new retry attempt record
         * 
         * @param attemptNumber The attempt number
         * @param retryPerformed Whether retry was performed
         * @param reason The reason for the decision
         * @param timestamp The timestamp of the attempt
         */
        public RetryAttempt(int attemptNumber, boolean retryPerformed, String reason, long timestamp) {
            this.attemptNumber = attemptNumber;
            this.retryPerformed = retryPerformed;
            this.reason = reason;
            this.timestamp = timestamp;
        }
        
        /**
         * Gets the attempt number
         * 
         * @return Attempt number
         */
        public int getAttemptNumber() {
            return attemptNumber;
        }
        
        /**
         * Checks if retry was performed
         * 
         * @return true if retry was performed
         */
        public boolean isRetryPerformed() {
            return retryPerformed;
        }
        
        /**
         * Gets the reason for the retry decision
         * 
         * @return Reason string
         */
        public String getReason() {
            return reason;
        }
        
        /**
         * Gets the timestamp of the attempt
         * 
         * @return Timestamp in milliseconds
         */
        public long getTimestamp() {
            return timestamp;
        }
        
        @Override
        public String toString() {
            return String.format("Attempt %d: %s - %s (timestamp: %d)",
                attemptNumber, retryPerformed ? "RETRY" : "NO_RETRY", reason, timestamp);
        }
    }
} 