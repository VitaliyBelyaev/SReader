package ru.vitaliybelyaev.sreader;

public final class Workers {
    private Workers(){}

    private static final WorkerThread workerThread = new WorkerThread("workerThread");

    public static WorkerThread getWorkerThread(){
        return workerThread;
    }
}
