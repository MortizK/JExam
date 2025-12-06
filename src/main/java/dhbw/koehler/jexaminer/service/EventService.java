package dhbw.koehler.jexaminer.service;

public class EventService {
    private static Runnable onUpdate;

    public static void setOnUpdate(Runnable r) {
        onUpdate = r;
    }

    public static void triggerUpdate() {
        if (onUpdate != null) onUpdate.run();
    }
}

