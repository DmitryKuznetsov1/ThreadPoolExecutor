import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class ThreadPoolImpl {

    private final int poolSize;
    private final Thread[] threads;
    private final BlockingQueue<LightFutureTask<?>> queue;
    private volatile boolean running = true;
//    Logger logger;

    public ThreadPoolImpl(int poolSize) {

//        logger = LoggerFactory.getLogger(ThreadPoolImpl.class);
        this.poolSize = poolSize;
        queue = new BlockingQueue<>();
        threads = new Thread[poolSize];
        for (int i = 0; i < poolSize; i++) {
            threads[i] = new Thread(this::run);
            threads[i].start();
        }
    }

    private void run() {

        while (!Thread.interrupted() && running) {
            try {
                LightFutureTask<?> lightFuture = queue.poll();
                lightFuture.run();
            } catch (Exception ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public <R> LightFuture<R> execute(Callable<R> task) {

        LightFutureTask<R> lightFutureTask = new LightFutureTask<R>(task);
        queue.add(lightFutureTask);
        return lightFutureTask;
    }

    public void shutdown() {

//        logger.info("Shutting down thread pool ...");
        queue.breakWaiting();
//        logger.info("Successfully");
    }

    public void shutdownNow() {

//        logger.info("Shutting down thread pool ...");
        running = false;
        for (Thread thread : threads) {
            thread.interrupt();
        }   
//        logger.info("Successfully");
    }

    public BlockingQueue<LightFutureTask<?>> getQueue() {
        return queue;
    }




    private enum State {
        NEW,
        DONE,
        ERROR
    }

    class LightFutureTask<R> implements LightFuture<R> {

        private Callable<R> callable;
        private R outcome;
        private Exception exception;

        private volatile State state;

        LightFutureTask(Callable<R> callable) {

            this.callable = callable;
            this.state = State.NEW;
        }

        @Override
        public boolean isReady() {

            return state == State.DONE;
        }

        @Override
        public synchronized R get() throws LightExecutionException {

            if (exception != null) {
                throw new LightExecutionException(exception);
            }

            while (!isReady()) {
                try {
                    this.wait(10 * 1000);
                } catch (Exception ex) {
                    throw new LightExecutionException(ex);
                }
            }


            return outcome;
        }

        @Override
        public <S> LightFutureTask<S> thenApply(Function<? super R, S> apply) throws LightExecutionException{

            Callable<S> callable = () -> apply.apply(get());
            LightFutureTask<S> newLightFutureTask = new LightFutureTask(callable);
            queue.add(newLightFutureTask);
            return newLightFutureTask;
        }

        public synchronized void run() {

            try {
                outcome = callable.call();
                callable = null;
                state = State.DONE;
                this.notify();
            } catch (Exception ex) {
                exception = new LightExecutionException(ex);
            }
        }
    }
}