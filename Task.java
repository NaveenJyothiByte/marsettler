import java.time.LocalDate;
import java.time.LocalTime;

public class Task {
    public final String id;
    public final String title;
    /** 1 = highest priority, 5 = lowest */
    public final int priority;
    public final LocalDate date;
    public final LocalTime time;

    public Task(String id, String title, int priority, LocalDate date, LocalTime time) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.date = date;
        this.time = time;
    }

    @Override
    public String toString() {
        return String.format("[%s %s] p%d %s", date, time, priority, title);
    }
}
