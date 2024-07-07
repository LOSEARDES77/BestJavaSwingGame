package com.loseardes77.common;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.loseardes77.common.Logger.error;
import static com.loseardes77.common.Logger.info;
import static com.loseardes77.common.Logger.warning;

public class ThreadPool {
    private final Worker[] workers;
    private final BlockingQueue<Runnable> queue;

    public ThreadPool(int size) {
        this(size, false);
    }

    public ThreadPool(int size, boolean debug) {
        assert size > 0;

        this.queue = new LinkedBlockingQueue<>();
        this.workers = new Worker[size];
        for (int id = 0; id < size; id++) {
            workers[id] = new Worker(id, queue, debug);
        }

        if (debug)
            info("Created a new ThreadPool with " + workers.length + " workers");
    }

    public void execute(Runnable task) {
        queue.add(task);
    }

    public void shutdown() {
        awaitTermination();
        for (Worker worker : workers) {
            worker.thread.interrupt();
        }
    }

    private void awaitTermination() {
        while (!queue.isEmpty()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                error("ThreadPool: Error while waiting for the queue to be empty");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void join() {
        awaitTermination();
    }

    private static class Worker implements Runnable {
        private final int id;
        private final Thread thread;
        private final BlockingQueue<Runnable> queue;
        private final boolean debug;

        public Worker(int id, BlockingQueue<Runnable> queue, boolean debug) {
            this.debug = debug;
            this.id = id;
            this.queue = queue;
            this.thread = new Thread(this);
            this.thread.setPriority(4);
            this.thread.start();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Runnable task = queue.take();
                    if (debug)
                        info("Worker " + (id + 1) + " got a job, executing.");
                    task.run();
                } catch (InterruptedException e) {
                    if (debug)
                        warning("Worker " + (id + 1) + " interrupted, shutting down.");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}