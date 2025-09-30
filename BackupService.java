import java.time.LocalDate;
import java.util.*;

/**
 * BackupService: holds an in-memory copy of schedule data.
 * Use with ScheduleService.snapshot() and restore().
 */
public class BackupService {
    private Map<String, Map<LocalDate, List<Task>>> snapshot;

    /** Capture a deep-ish copy of the provided schedule data */
    public void backup(Map<String, Map<LocalDate, List<Task>>> source) {
        snapshot = new HashMap<>();
        for (Map.Entry<String, Map<LocalDate, List<Task>>> e : source.entrySet()) {
            Map<LocalDate, List<Task>> days = new HashMap<>();
            for (Map.Entry<LocalDate, List<Task>> d : e.getValue().entrySet()) {
                days.put(d.getKey(), new ArrayList<>(d.getValue()));
            }
            snapshot.put(e.getKey(), days);
        }
    }

    /** Return a copy of the last backup (may be empty if none) */
    public Map<String, Map<LocalDate, List<Task>>> restore() {
        Map<String, Map<LocalDate, List<Task>>> restored = new HashMap<>();
        if (snapshot == null) return restored;
        for (Map.Entry<String, Map<LocalDate, List<Task>>> e : snapshot.entrySet()) {
            Map<LocalDate, List<Task>> days = new HashMap<>();
            for (Map.Entry<LocalDate, List<Task>> d : e.getValue().entrySet()) {
                days.put(d.getKey(), new ArrayList<>(d.getValue()));
            }
            restored.put(e.getKey(), days);
        }
        return restored;
    }
}
