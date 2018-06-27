package ru.vitaliybelyaev.sreader;

public final class Workers {
    private Workers() {
    }

    private static WorkerThread instance;

    public static WorkerThread getWorkerThread() {
        if (instance == null) {
            instance = new WorkerThread("workerThread");
            return instance;
        }
        return instance;
    }

    public static void clear() {
        instance.quit();
        instance = null;
    }
}
