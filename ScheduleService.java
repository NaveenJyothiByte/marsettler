import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;

public class ScheduleService {
    private final Map<String, Map<LocalDate, List<Task>>> store = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastUpdate = new ConcurrentHashMap<>();
    
    // Array storage for all tasks across all users
    private final List<Task> allTasksList = new ArrayList<>();

    public void seedResidentTasks(String username) {
        Map<LocalDate, List<Task>> cal = store.computeIfAbsent(username, k -> new ConcurrentHashMap<>());
        LocalDate today = LocalDate.now();

        Task[] todaysTasks = new Task[] { // Array of tasks
            new Task("t1", "Inspect hydroponics", 2, today, java.time.LocalTime.of(9, 0)),
            new Task("t2", "Check airlock seals", 1, today, java.time.LocalTime.of(10, 30)),
            new Task("t3", "Rover battery check", 3, today, java.time.LocalTime.of(13, 0))
        };

        Task[] tomorrowsTasks = new Task[] { // Array of tasks
            new Task("t4", "Soil sample catalog", 2, today.plusDays(1), java.time.LocalTime.of(11, 0))
        };

        // Add to map storage
        cal.put(today, new ArrayList<>(Arrays.asList(todaysTasks)));
        cal.put(today.plusDays(1), new ArrayList<>(Arrays.asList(tomorrowsTasks)));

        // Add to array storage
        allTasksList.addAll(Arrays.asList(todaysTasks));
        allTasksList.addAll(Arrays.asList(tomorrowsTasks));

        lastUpdate.put(username, Instant.now());
    }

    public List<Task> getTasks(String username, LocalDate date) {
        List<Task> list = store
            .getOrDefault(username, Collections.emptyMap())
            .getOrDefault(date, new ArrayList<Task>());
        List<Task> copy = new ArrayList<>(list);
        copy.sort(Comparator.comparing((Task t) -> t.time).thenComparingInt(t -> t.priority));
        return copy;
    }

    public void upsertTask(String username, Task task) {
        Map<LocalDate, List<Task>> cal = store.computeIfAbsent(username, k -> new ConcurrentHashMap<>());
        List<Task> day = cal.computeIfAbsent(task.date, k -> new ArrayList<>());
        
        // Remove existing task with same ID
        day.removeIf(t -> t.id.equals(task.id));
        // Remove from array storage if exists
        allTasksList.removeIf(t -> t.id.equals(task.id));
        
        day.add(task);
        allTasksList.add(task); // Add to array storage
        lastUpdate.put(username, Instant.now());
    }

    // Get all tasks as array
    public Task[] getAllTasksArray() {
        return allTasksList.toArray(new Task[0]);
    }

    // Get tasks for specific user as array
    public Task[] getUserTasksArray(String username) {
        List<Task> userTasks = new ArrayList<>();
        Map<LocalDate, List<Task>> userSchedule = store.get(username);
        if (userSchedule != null) {
            for (List<Task> tasks : userSchedule.values()) {
                userTasks.addAll(tasks);
            }
        }
        return userTasks.toArray(new Task[0]);
    }

    public Instant getLastUpdate(String username) {
        return lastUpdate.getOrDefault(username, Instant.EPOCH);
    }

    public Map<String, Map<LocalDate, List<Task>>> snapshot() {
        Map<String, Map<LocalDate, List<Task>>> snap = new HashMap<>();
        for (Map.Entry<String, Map<LocalDate, List<Task>>> e : store.entrySet()) {
            Map<LocalDate, List<Task>> perDay = new HashMap<>();
            for (Map.Entry<LocalDate, List<Task>> d : e.getValue().entrySet()) {
                perDay.put(d.getKey(), new ArrayList<>(d.getValue()));
            }
            snap.put(e.getKey(), perDay);
        }
        return snap;
    }

    public void restore(Map<String, Map<LocalDate, List<Task>>> snap) {
        store.clear();
        allTasksList.clear(); // Clear array storage
        
        if (snap == null) return;
        
        for (Map.Entry<String, Map<LocalDate, List<Task>>> e : snap.entrySet()) {
            Map<LocalDate, List<Task>> perDay = new HashMap<>();
            for (Map.Entry<LocalDate, List<Task>> d : e.getValue().entrySet()) {
                perDay.put(d.getKey(), new ArrayList<>(d.getValue()));
                allTasksList.addAll(d.getValue()); // Restore to array storage
            }
            store.put(e.getKey(), perDay);
        }
    }
}