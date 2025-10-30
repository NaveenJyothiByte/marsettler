import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages supply requests, inventory, and user quotas for Colony Residents
 * Handles UAT-S2-01 through UAT-S2-05
 */
public class SupplyService {
    private final Map<String, List<SupplyRequest>> userRequests = new ConcurrentHashMap<>();
    private final Map<SupplyItem, Integer> inventory = new ConcurrentHashMap<>();
    private final Map<String, Map<SupplyItem, Integer>> userQuotas = new ConcurrentHashMap<>();
    private int nextRequestId = 1000;

    public SupplyService() {
        initializeInventory();
        initializeQuotas();
    }

    private void initializeInventory() {
        inventory.put(SupplyItem.WATER, 1000);
        inventory.put(SupplyItem.OXYGEN, 500);
        inventory.put(SupplyItem.FOOD_A, 200);
        inventory.put(SupplyItem.FOOD_B, 150);
        inventory.put(SupplyItem.MEDICAL, 50);
    }

    private void initializeQuotas() {
        // Default daily quotas for all users
        Map<SupplyItem, Integer> defaultQuotas = new HashMap<>();
        defaultQuotas.put(SupplyItem.WATER, 10);
        defaultQuotas.put(SupplyItem.OXYGEN, 5);
        defaultQuotas.put(SupplyItem.FOOD_A, 3);
        defaultQuotas.put(SupplyItem.FOOD_B, 3);
        defaultQuotas.put(SupplyItem.MEDICAL, 1);
        
        // In a real system, we'd load user-specific quotas
    }

    /**
     * Submits a supply request with validation for quotas and inventory
     * UAT-S2-01: Resident submits valid supply request
     * UAT-S2-02: Request over quota is blocked
     * UAT-S2-03: Out-of-stock handling
     */
    public Optional<SupplyRequest> submitRequest(String username, SupplyItem item, int quantity) {
        // Check inventory
        if (!isInStock(item, quantity)) {
            return Optional.empty(); // UAT-S2-03
        }

        // Check quota
        if (!isWithinQuota(username, item, quantity)) {
            return Optional.empty(); // UAT-S2-02
        }

        // Create and store request
        String requestId = "REQ-" + (nextRequestId++);
        SupplyRequest request = new SupplyRequest(requestId, username, item, quantity, RequestStatus.SUBMITTED);
        
        userRequests.computeIfAbsent(username, k -> new ArrayList<>()).add(request);
        
        // Update inventory
        inventory.put(item, inventory.get(item) - quantity);
        
        return Optional.of(request); // UAT-S2-01
    }

    public List<SupplyRequest> getRequestHistory(String username) {
        return userRequests.getOrDefault(username, new ArrayList<>())
                .stream()
                .sorted((a, b) -> b.timestamp.compareTo(a.timestamp))
                .collect(Collectors.toList());
    }

    public boolean cancelRequest(String username, String requestId) {
        List<SupplyRequest> requests = userRequests.get(username);
        if (requests != null) {
            for (SupplyRequest request : requests) {
                if (request.requestId.equals(requestId) && request.status == RequestStatus.SUBMITTED) {
                    request.status = RequestStatus.CANCELLED;
                    // Restore inventory
                    inventory.put(request.item, inventory.get(request.item) + request.quantity);
                    return true; // UAT-S2-05
                }
            }
        }
        return false;
    }

    private boolean isInStock(SupplyItem item, int quantity) {
        return inventory.getOrDefault(item, 0) >= quantity;
    }

    private boolean isWithinQuota(String username, SupplyItem item, int quantity) {
        Map<SupplyItem, Integer> userQuota = userQuotas.computeIfAbsent(username, k -> new HashMap<>());
        int usedToday = getUsedToday(username, item);
        int quota = userQuota.getOrDefault(item, 10); // Default quota
        
        return (usedToday + quantity) <= quota;
    }

    private int getUsedToday(String username, SupplyItem item) {
        return userRequests.getOrDefault(username, new ArrayList<>())
                .stream()
                .filter(req -> req.item == item && req.status != RequestStatus.CANCELLED)
                .mapToInt(req -> req.quantity)
                .sum();
    }

    public Map<SupplyItem, Integer> getInventory() {
        return new HashMap<>(inventory);
    }

    public int getRemainingQuota(String username, SupplyItem item) {
        Map<SupplyItem, Integer> userQuota = userQuotas.computeIfAbsent(username, k -> new HashMap<>());
        int quota = userQuota.getOrDefault(item, 10);
        int usedToday = getUsedToday(username, item);
        return quota - usedToday;
    }
}