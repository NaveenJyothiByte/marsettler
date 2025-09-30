import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * AuthService (defensive version)
 * - In-memory auth with clear error reporting and guard rails.
 */
public class AuthService {
    private final Map<String, User> users;
    private final int maxAttempts;
    private final Duration sessionTimeout;
    private final List<String> audit = new ArrayList<>();
    private String lastError = null;   // <- for quick diagnostics

    public AuthService(Map<String, User> users, int maxAttempts, Duration sessionTimeout) {
        if (users == null) throw new IllegalArgumentException("users map is null");
        if (maxAttempts <= 0) throw new IllegalArgumentException("maxAttempts must be > 0");
        if (sessionTimeout == null || sessionTimeout.isNegative() || sessionTimeout.isZero())
            throw new IllegalArgumentException("sessionTimeout must be positive");

        this.users = users;
        this.maxAttempts = maxAttempts;
        this.sessionTimeout = sessionTimeout;
    }

    /** Returns Optional<User> if login successful; sets lastError on failure. */
    public Optional<User> login(String username, String password) {
        clearLastError();

        if (username == null || username.trim().isEmpty()) {
            setError("username is empty");
            audit.add("FAIL empty-username");
            return Optional.empty();
        }
        if (password == null) password = ""; // avoid NPE comparisons

        User u = users.get(username);
        if (u == null) {
            setError("unknown user: " + username);
            audit.add("FAIL unknown-user:" + username);
            return Optional.empty();
        }
        if (u.status == AccountStatus.EXPIRED) {
            setError("account expired: " + username);
            audit.add("FAIL expired:" + username);
            return Optional.empty();
        }
        if (u.status == AccountStatus.LOCKED) {
            setError("account locked: " + username);
            audit.add("FAIL locked:" + username);
            return Optional.empty();
        }
        if (!Objects.equals(u.password, password)) {
            u.failedAttempts++;
            setError("invalid password (attempt " + u.failedAttempts + "): " + username);
            audit.add("FAIL bad-password:" + username + ":attempt=" + u.failedAttempts);
            if (u.failedAttempts >= maxAttempts) {
                u.status = AccountStatus.LOCKED;
                audit.add("LOCKED:" + username);
                setError("account locked after max attempts: " + username);
            }
            return Optional.empty();
        }

        u.failedAttempts = 0;
        u.loggedIn = true;
        u.lastActivity = Instant.now();
        audit.add("SUCCESS:" + username);
        return Optional.of(u);
    }

    public void touch(User u) {
        if (u == null) return;
        u.lastActivity = Instant.now();
    }

    /** Returns true if session has expired or user is not logged in. */
    public boolean isSessionExpired(User u, Instant now) {
        if (u == null) return true;
        if (!u.loggedIn || u.lastActivity == null) return true;
        if (now == null) now = Instant.now();
        return Duration.between(u.lastActivity, now).compareTo(sessionTimeout) > 0;
    }

    public void logout(User u) {
        if (u != null) u.loggedIn = false;
    }

    public List<String> getAudit() {
        return Collections.unmodifiableList(audit);
    }

    public String getLastError() {
        return lastError;
    }

    private void setError(String msg) { this.lastError = msg; }
    private void clearLastError() { this.lastError = null; }
}
