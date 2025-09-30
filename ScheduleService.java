import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ScheduleService: in-memory schedule storage per user.
 * - Map<username, Map<date, List<Task>>>
 * - Sorted reads (by time then priority)
 * - Tracks lastUpdate per user
 */
public class ScheduleService {
    private final Map<String, Map<LocalDate, List<Task>>> store = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastUpdate = new ConcurrentHashMap<>();

    /** Create or replace tasks for today/tomorrow with sample data for a user */
    public void seedResidentTasks(String username) {
        Map<LocalDate, List<Task>> cal = store.computeIfAbsent(username, k -> new ConcurrentHashMap<>());
        LocalDate today = LocalDate.now();

        cal.put(today, new ArrayList<>(Arrays.asList(
            new Task("t1", "Inspect hydroponics", 2, today, java.time.LocalTime.of(9, 0)),
            new Task("t2", "Check airlock seals", 1, today, java.time.LocalTime.of(10, 30)),
            new Task("t3", "Rover battery check", 3, today, java.time.LocalTime.of(13, 0))
        )));

        cal.put(today.plusDays(1), new ArrayList<>(Arrays.asList(
            new Task("t4", "Soil sample catalog", 2, today.plusDays(1), java.time.LocalTime.of(11, 0))
        )));

        lastUpdate.put(username, Instant.now());
    }

    /** Returns a sorted copy (by time then priority). Empty list if none. */
    public List<Task> getTasks(String username, LocalDate date) {
        List<Task> list = store
            .getOrDefault(username, Collections.emptyMap())
            .getOrDefault(date, new ArrayList<Task>());

        List<Task> copy = new ArrayList<>(list);
        copy.sort(Comparator
            .comparing((Task t) -> t.time)
            .thenComparingInt(t -> t.priority));
        return copy;
    }

    /** Insert or update a single task */
    public void upsertTask(String username, Task task) {
        Map<LocalDate, List<Task>> cal = store.computeIfAbsent(username, k -> new ConcurrentHashMap<>());
        List<Task> day = cal.computeIfAbsent(task.date, k -> new ArrayList<>());
        day.removeIf(t -> t.id.equals(task.id));
        day.add(task);
        lastUpdate.put(username, Instant.now());
    }

    public Instant getLastUpdate(String username) {
        return lastUpdate.getOrDefault(username, Instant.EPOCH);
    }

    /** Deep-ish copy of internal store for backup/export purposes */
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

    /** Overwrite current store from a snapshot (e.g., restore) */
    public void restore(Map<String, Map<LocalDate, List<Task>>> snap) {
        store.clear();
        for (Map.Entry<String, Map<LocalDate, List<Task>>> e : snap.entrySet()) {
            Map<LocalDate, List<Task>> perDay = new HashMap<>();
            for (Map.Entry<LocalDate, List<Task>> d : e.getValue().entrySet()) {
                perDay.put(d.getKey(), new ArrayList<>(d.getValue()));
            }
            store.put(e.getKey(), perDay);
        }
    }
}
