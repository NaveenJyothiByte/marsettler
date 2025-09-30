import java.time.Instant;

public class User {
    public final String userId;   // NEW: unique ID for every user
    public final String username; // login name (email-like)
    public String password;
    public AccountStatus status;
    public final Role role;

    // Session & auth tracking (in-memory)
    public int failedAttempts = 0;
    public boolean loggedIn = false;
    public Instant lastActivity = null;

    public User(String userId, String username, String password, AccountStatus status, Role role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.status = status;
        this.role = role;
    }
}
