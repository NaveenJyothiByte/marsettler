import java.util.*;

public class UserStore {
    private final Map<String, User> users = new HashMap<>();
    private final List<User> userList = new ArrayList<>(); // Array-based storage
    private int nextUserNum = 2000;

    public String normalize(String s) {
        return (s == null) ? "" : s.trim().toLowerCase();
    }

    public Map<String, User> backingMap() { 
        return users; 
    }
    
    // Get all users as List
    public List<User> getUserList() { 
        return userList; 
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
        userList.add(u); // Add to array storage
        return u;
    }

    /** Seed three ACTIVE users so they can log in immediately. */
    public void seedSamples() {
        // Clear existing data first
        users.clear();
        userList.clear();
        
        User[] sampleUsers = new User[] {
            new User("U1001", "resident.valid@mars.local", "Passw0rd!", AccountStatus.ACTIVE, Role.COLONY_RESIDENT),
            new User("U1002", "resident.expired@mars.local", "AnyPass", AccountStatus.EXPIRED, Role.MISSION_CONTROL_OPERATOR),
            new User("U1003", "resident.locked@mars.local", "Pass123", AccountStatus.LOCKED, Role.INFRASTRUCTURE_TECHNICIAN)
        };

        for (User user : sampleUsers) {
            String normalizedUsername = normalize(user.username);
            users.put(normalizedUsername, user);
            userList.add(user);
        }
    }

    // Method to get users as array - THIS IS THE METHOD YOU NEED
    public User[] getUsersArray() {
        return userList.toArray(new User[0]);
    }
    
    // Additional helper method to see if storage is working
    public void printAllUsers() {
        System.out.println("=== Stored Users ===");
        for (User user : userList) {
            System.out.println("ID: " + user.userId + " | Username: " + user.username + 
                             " | Status: " + user.status + " | Role: " + user.role);
        }
        System.out.println("Total users: " + userList.size());
    }
}