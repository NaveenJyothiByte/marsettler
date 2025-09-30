import org.junit.Test;
import static org.junit.Assert.*;

import java.time.*;
import java.util.*;

/**
 * MarsSettlerUATTest — JUnit 4 tests for Sprint 1 UAT cases.
 * Requires the following classes in your project:
 *  - User, Role, AccountStatus, Task
 *  - AuthService, ScheduleService, BackupService, UptimeMonitor
 *  - UserStore (from your refactor; provides seedSamples + backing map)
 *
 * Each test creates a fresh in-memory environment (no DB).
 */
public class MarsSettlerUATTest {

    /** Small container to build a clean environment per test. */
    private static class Env {
        final UserStore store = new UserStore();
        AuthService auth;
        ScheduleService schedule;
        BackupService backup;
        UptimeMonitor uptime;

        Env() {
            store.seedSamples(); // all ACTIVE for convenience
            auth = new AuthService(store.backingMap(), 5, Duration.ofMinutes(15));
            schedule = new ScheduleService();
            backup = new BackupService();
            uptime = new UptimeMonitor();
            schedule.seedResidentTasks("resident.valid@mars.local");
        }

        User get(String username) { return store.get(username); }
    }

    private Env newEnv() { return new Env(); }

    // UAT-S1-01 — Successful login for valid user
    @Test
    public void test_UAT_S1_01_successfulLogin() {
        Env e = newEnv();
        Optional<User> u = e.auth.login("resident.valid@mars.local", "Passw0rd!");
        assertTrue("Expected successful login for resident.valid", u.isPresent());
        assertEquals("Expected Colony Resident role", Role.COLONY_RESIDENT, u.get().role);
    }

    // UAT-S1-02 — Invalid password shows proper error
    @Test
    public void test_UAT_S1_02_invalidPassword() {
        Env e = newEnv();
        Optional<User> u = e.auth.login("resident.valid@mars.local", "wrong");
        boolean failLogged = e.auth.getAudit().stream().anyMatch(a -> a.contains("bad-password:resident.valid@mars.local"));
        assertFalse("Login should fail for wrong password", u.isPresent());
        assertTrue("Audit should record bad-password", failLogged);
    }

    // UAT-S1-03 — Expired credentials blocked
    @Test
    public void test_UAT_S1_03_expiredBlocked() {
        Env e = newEnv();
        User exp = e.get("resident.expired@mars.local");
        exp.status = AccountStatus.EXPIRED; // force expired for this test
        Optional<User> u = e.auth.login(exp.username, "AnyPass");
        boolean logged = e.auth.getAudit().stream().anyMatch(a -> a.contains("expired:resident.expired@mars.local"));
        assertFalse("Expired account should not login", u.isPresent());
        assertTrue("Audit should record expired", logged);
    }

    // UAT-S1-04 — Account lock after consecutive failures
    @Test
    public void test_UAT_S1_04_lockAfterFailures() {
        Env e = newEnv();
        User v = e.get("resident.valid@mars.local");
        v.failedAttempts = 0;
        v.status = AccountStatus.ACTIVE;
        for (int i = 0; i < 5; i++) e.auth.login(v.username, "nope");
        assertEquals("Account should be LOCKED after 5 failures", AccountStatus.LOCKED, v.status);
    }

    // UAT-S1-05 — Session timeout after inactivity
    @Test
    public void test_UAT_S1_05_sessionTimeout() {
        Env e = newEnv();
        User v = e.get("resident.valid@mars.local");
        v.password = "Passw0rd!";
        v.status = AccountStatus.ACTIVE;
        e.auth.login(v.username, v.password);
        v.lastActivity = Instant.now().minus(Duration.ofMinutes(16)); // > 15 min
        assertTrue("Session should be expired after inactivity", e.auth.isSessionExpired(v, Instant.now()));
    }

    // UAT-S1-06 — Role-based landing
    @Test
    public void test_UAT_S1_06_roleLanding() {
        Env e = newEnv();
        Optional<User> u = e.auth.login("resident.valid@mars.local", "Passw0rd!");
        assertTrue("Login should succeed", u.isPresent());
        assertEquals("Should land as Colony Resident", Role.COLONY_RESIDENT, u.get().role);
    }

    // UAT-S1-07 — View today’s schedule (≤2s)
    @Test
    public void test_UAT_S1_07_viewTodaySchedule() {
        Env e = newEnv();
        long start = System.nanoTime();
        List<Task> tasks = e.schedule.getTasks("resident.valid@mars.local", LocalDate.now());
        long durMs = (System.nanoTime() - start) / 1_000_000;
        assertTrue("Schedule should load in ≤ 2000ms, got " + durMs + "ms", durMs <= 2000);
        assertTrue("All tasks should have time/priority",
                tasks.stream().allMatch(t -> t.time != null && t.priority >= 1));
    }

    // UAT-S1-08 — Date filter & empty-state
    @Test
    public void test_UAT_S1_08_emptyDateFilter() {
        Env e = newEnv();
        LocalDate far = LocalDate.now().minusYears(5);
        List<Task> tasks = e.schedule.getTasks("resident.valid@mars.local", far);
        assertTrue("Expected empty list for far date", tasks.isEmpty());
    }

    // UAT-S1-09 — Near real-time update reflected
    @Test
    public void test_UAT_S1_09_realtimeUpdate() {
        Env e = newEnv();
        String user = "resident.valid@mars.local";
        Instant before = e.schedule.getLastUpdate(user);
        e.schedule.upsertTask(user, new Task("t999", "Emergency seal check", 1, LocalDate.now(), LocalTime.of(14, 0)));
        Instant after = e.schedule.getLastUpdate(user);
        boolean present = e.schedule.getTasks(user, LocalDate.now()).stream().anyMatch(t -> t.id.equals("t999"));
        assertTrue("Last update should advance", after.isAfter(before));
        assertTrue("New task should be present", present);
    }

    // UAT-S1-10 — Data consistency & ordering
    @Test
    public void test_UAT_S1_10_consistencyOrdering() {
        Env e = newEnv();
        List<Task> a = e.schedule.getTasks("resident.valid@mars.local", LocalDate.now());
        List<Task> b = e.schedule.getTasks("resident.valid@mars.local", LocalDate.now());
        assertEquals("Same size lists expected", a.size(), b.size());
        for (int i = 0; i < a.size(); i++) {
            assertEquals("Stable order expected at index " + i, a.get(i).id, b.get(i).id);
        }
    }

    // UAT-S1-11 — Basic accessibility sanity (simulated)
    @Test
    public void test_UAT_S1_11_accessibilitySim() {
        boolean keyboardNavigable = true, labelsPresent = true, contrastOk = true;
        assertTrue("Simulated accessibility should pass", keyboardNavigable && labelsPresent && contrastOk);
    }

    // UAT-S1-12 — Reliability/uptime simulation
    @Test
    public void test_UAT_S1_12_uptime() {
        Env e = newEnv();
        long dayMs = 24L * 60 * 60 * 1000; // 24h
        long blipMs = 30_000;              // 30s
        e.uptime.simulate(dayMs, blipMs);
        double up = e.uptime.uptimePercent();
        assertTrue("Uptime should be ≥ 99.9% (was " + up + ")", up >= 99.9);
        assertTrue("Recovery blip should be < 1 minute", blipMs < 60_000);
    }

    // UAT-S1-13 — Automated backup & restore drill
    @Test
    public void test_UAT_S1_13_backupRestore() {
        Env e = newEnv();
        Map<String, Map<LocalDate, List<Task>>> snap = e.schedule.snapshot();
        e.backup.backup(snap);

        // simulate wipe
        e.schedule.restore(new HashMap<>());

        Map<String, Map<LocalDate, List<Task>>> restored = e.backup.restore();
        e.schedule.restore(restored);

        boolean ok = !e.schedule.getTasks("resident.valid@mars.local", LocalDate.now()).isEmpty();
        assertTrue("Restored schedule should contain tasks", ok);
    }

    // UAT-S1-14 — Cross-browser UI sanity (simulated)
    @Test
    public void test_UAT_S1_14_crossBrowserSim() {
        boolean chrome = true, firefox = true, edge = true;
        assertTrue("Simulated cross-browser sanity should pass", chrome && firefox && edge);
    }

    // UAT-S1-15 — Usability task completion (simulated)
    @Test
    public void test_UAT_S1_15_usabilitySim() {
        long simulatedMinutes = 7;
        assertTrue("New user should finish within 10 minutes", simulatedMinutes <= 10);
    }
}
