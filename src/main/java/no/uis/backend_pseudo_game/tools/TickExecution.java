package no.uis.backend_pseudo_game.tools;

public class TickExecution {
    private long fixedDeltaTime;
    private Runnable tickCallback;
    private Thread thread;
    public TickExecution(long milliseconds, Runnable callback) {
        fixedDeltaTime = milliseconds;
        tickCallback = callback;
        thread = new Thread(() -> {
            try {
                while (true) {
                    callback.run();
                    Thread.sleep(fixedDeltaTime);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void execute() {
        thread.start();
    }
}
