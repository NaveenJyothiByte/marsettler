import java.util.*;

public class UserStore {
    private final Map<String, User> users = new HashMap<>();
    private int nextUserNum = 2000;

    /** normalize login key: trim + lowercase */
    public String normalize(String s) {
        return (s == null) ? "" : s.trim().toLowerCase();
    }

    public Map<String, User> backingMap() {  // for AuthService constructor
        return users;
    }

    public boolean exists(String usernameRaw) {
        return users.containsKey(normalize(usernameRaw));
    }

    public User get(String usernameRaw) {
        return users.get(normalize(usernameRaw));
    }

    public User addNew(String usernameRaw, String password, AccountStatus status, Role role) {
        String key = normalize(usernameRaw);
        String userId = "U" + (nextUserNum++);
        User u = new User(userId, key, password, status, role);
        users.put(key, u);
        return u;
    }

    /** Pre-created sample users — set ACTIVE so they can log in */
    public void seedSamples() {
        users.put(normalize("resident.valid@mars.local"),
            new User("U1001", "resident.valid@mars.local", "Passw0rd!", AccountStatus.ACTIVE, Role.COLONY_RESIDENT));
        users.put(normalize("resident.expired@mars.local"),
            new User("U1002", "resident.expired@mars.local", "AnyPass", AccountStatus.ACTIVE, Role.MISSION_CONTROL_OPERATOR));
        users.put(normalize("resident.locked@mars.local"),
            new User("U1003", "resident.locked@mars.local", "Pass123", AccountStatus.ACTIVE, Role.INFRASTRUCTURE_TECHNICIAN));
    }
}
