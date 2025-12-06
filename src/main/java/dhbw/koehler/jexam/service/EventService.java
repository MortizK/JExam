package dhbw.koehler.jexam.service;

public class EventService {
    private static Runnable onXmlUpdate;
    private static Runnable onPdfUpdate;

    public static void triggerAllUpdate() {
        triggerXmlUpdate();
        triggerPdfUpdate();
    }

    // Update XML Tab
    public static void setXmlUpdate(Runnable r) {
        onXmlUpdate = r;
    }

    public static void triggerXmlUpdate() {
        if (onXmlUpdate != null) onXmlUpdate.run();
    }

    // Update PDF Tab
    public static void setPdfUpdate(Runnable r) {
        onPdfUpdate = r;
    }

    public static void triggerPdfUpdate() {
        if (onPdfUpdate != null) onPdfUpdate.run();
    }
}

