import java.time.Duration;
import java.time.Instant;
import java.util.*;

/** In-memory authentication + sessions + simple audit. */
public class AuthService {
    private final Map<String, User> users; // keyed by normalized username (see UserStore)
    private final int maxAttempts;
    private final Duration sessionTimeout;
    private final List<String> audit = new ArrayList<>();
    private String lastError = null;

    public AuthService(Map<String, User> users, int maxAttempts, Duration sessionTimeout) {
        if (users == null) throw new IllegalArgumentException("users map is null");
        if (maxAttempts <= 0) throw new IllegalArgumentException("maxAttempts must be > 0");
        if (sessionTimeout == null || sessionTimeout.isZero() || sessionTimeout.isNegative())
            throw new IllegalArgumentException("sessionTimeout must be positive");

        this.users = users;
        this.maxAttempts = maxAttempts;
        this.sessionTimeout = sessionTimeout;
    }

    /** Returns user on success, else Optional.empty; sets lastError on failure. */
    public Optional<User> login(String usernameKey, String password) {
        clearLastError();

        if (usernameKey == null || usernameKey.trim().isEmpty()) {
            setError("username is empty");
            audit.add("FAIL empty-username");
            return Optional.empty();
        }

        if (password == null) password = "";

        User u = users.get(usernameKey);
        if (u == null) {
            setError("unknown user: " + usernameKey);
            audit.add("FAIL unknown-user:" + usernameKey);
            return Optional.empty();
        }

        if (u.status == AccountStatus.EXPIRED) {
            setError("account expired: " + u.username);
            audit.add("FAIL expired:" + u.username);
            return Optional.empty();
        }

        // Handle case sensitivity for LOCKED status
        if (u.status.toString().equalsIgnoreCase("LOCKED")) {
            setError("account locked: " + u.username);
            audit.add("FAIL locked:" + u.username);
            return Optional.empty();
        }

        if (!Objects.equals(u.password, password)) {
            u.failedAttempts++;
            setError("invalid password (attempt " + u.failedAttempts + "): " + u.username);
            audit.add("FAIL bad-password:" + u.username + ":attempt=" + u.failedAttempts);

            if (u.failedAttempts >= maxAttempts) {
                u.status = AccountStatus.LOCKED;
                audit.add("LOCKED:" + u.username);
                setError("account locked after max attempts: " + u.username);
            }
            return Optional.empty();
        }

        u.failedAttempts = 0;
        u.loggedIn = true;
        u.lastActivity = Instant.now();
        audit.add("SUCCESS:" + u.username);
        return Optional.of(u);
    }

    public void touch(User u) {
        if (u != null) u.lastActivity = Instant.now();
    }

    public boolean isSessionExpired(User u, Instant now) {
        if (u == null || !u.loggedIn || u.lastActivity == null) return true;
        if (now == null) now = Instant.now();
        return Duration.between(u.lastActivity, now).compareTo(sessionTimeout) > 0;
    }

    public void logout(User u) { 
        if (u != null) u.loggedIn = false; 
    }

    public List<String> getAudit() { 
        return Collections.unmodifiableList(audit); 
    }
    
    // Get audit log as array for data storage
    public String[] getAuditArray() {
        return audit.toArray(new String[0]);
    }
    
    // Get all active users as array
    public User[] getActiveUsersArray() {
        List<User> activeUsers = new ArrayList<>();
        for (User user : users.values()) {
            if (user.loggedIn && user.status == AccountStatus.ACTIVE) {
                activeUsers.add(user);
            }
        }
        return activeUsers.toArray(new User[0]);
    }
    
    // Get all users as array
    public User[] getAllUsersArray() {
        return users.values().toArray(new User[0]);
    }

    public String getLastError() { 
        return lastError; 
    }

    private void setError(String msg) { 
        lastError = msg; 
    }

    private void clearLastError() { 
        lastError = null; 
    }
}