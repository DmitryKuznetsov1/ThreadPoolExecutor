import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

class ThreadPoolImplTest {

    private static final int N = 100;

    @Test
    void execute() {

        ThreadPoolImpl threadPoolImpl = new ThreadPoolImpl(4);

        List<Integer> actual = new ArrayList<>();

        List<LightFuture<Integer> > futureList = new ArrayList<>();

        for (int i = 0; i < N; ++i) {
            final int I = i;
            LightFuture<Integer> future = threadPoolImpl.execute(() -> {
                Thread.sleep(300);
                return I;});

            futureList.add(future);
            actual.add(I);
        }

        threadPoolImpl.shutdown();

        List<Integer> expected = futureList.stream().map(future -> {
            try {
                return future.get();
            } catch (LightExecutionException e) {
                return null;
            }
        }).collect(Collectors.toList());


        Assert.assertEquals(expected, actual);
    }

    @RepeatedTest(10)
    void executeAfterShutdown() {

        boolean error = false;
        ThreadPoolImpl threadPoolImpl = new ThreadPoolImpl(4);

        try {
            LightFuture<Integer> lasttask = threadPoolImpl.execute(() -> {
                Thread.sleep(1000);
                return 10;
            });
        } catch (UnsupportedOperationException uoe) {
            error = true;
        }
        Assert.assertFalse(error);

        threadPoolImpl.shutdown();

        try {
            LightFuture<Integer> lasttask = threadPoolImpl.execute(() -> 10);
        } catch (UnsupportedOperationException uoe) {
            error = true;
        }
        Assert.assertTrue(error);
    }

    @Test
    void thenApply() throws LightExecutionException {

        ThreadPoolImpl threadPoolImpl = new ThreadPoolImpl(4);

        List<Integer> actual = new ArrayList<>();

        List<LightFuture<Integer> > futureList = new ArrayList<>();

        for (int i = 0; i < N; ++i) {
            LightFuture<String> future = threadPoolImpl.execute(() -> {
                Thread.sleep(100);
                return "xyz";
            });

            LightFuture<Integer> newFuture = future.thenApply((p) -> p.toString().length());

            futureList.add(newFuture);
            actual.add(3);
        }
        List<Integer> expected = futureList.stream().map(futureElement -> {
            try {
                return futureElement.get();
            } catch (LightExecutionException e) {
                return null;
            }
        }).collect(Collectors.toList());

        Assert.assertEquals(actual, expected);

        threadPoolImpl.shutdown();
    }

    @RepeatedTest(10)
    void isReady() throws LightExecutionException {

        ThreadPoolImpl threadPoolImpl = new ThreadPoolImpl(3);
        for (int i = 0; i < N / 2; ++i) {
            LightFuture<Integer> future = threadPoolImpl.execute(() -> {
                Thread.sleep(50);
                return 10;
            });
            Assert.assertFalse(future.isReady());

            int m = future.get();
            Assert.assertTrue(future.isReady());
        }

        threadPoolImpl.shutdown();
    }

    @RepeatedTest(10)
    void checkNumberOfRunningCustomThreads() {

        final int beforeRunningPool = Thread.activeCount();

        final int M = 4;
        ThreadPoolImpl threadPoolImpl = new ThreadPoolImpl(M);
        final int afterRunningPool = Thread.activeCount();

        Assert.assertTrue((afterRunningPool - beforeRunningPool) >= M);
    }

    @RepeatedTest(10)
    void shutdown() throws InterruptedException {

        final int beforeRunningPool = Thread.activeCount();

        ThreadPoolImpl threadPoolImpl = new ThreadPoolImpl(10);
        threadPoolImpl.shutdown();
        Thread.sleep(1 * 1000);

        final int afterShutdown = Thread.activeCount();
        Assert.assertEquals(beforeRunningPool, afterShutdown);
    }

    @RepeatedTest(10)
    void LightExecutionException() {

        boolean error = false;

        ThreadPoolImpl threadPoolImpl = new ThreadPoolImpl(4);

        LightFuture<Integer> futureWithoutException = threadPoolImpl.execute(() -> 10);
        LightFuture<Integer> futureWithException = threadPoolImpl.execute(() -> 10 / 0);

        try {
            futureWithoutException.get();
        } catch (LightExecutionException lee) {
            error = true;
        }
        Assert.assertFalse(error);

        try {
            futureWithException.get();
        } catch (LightExecutionException lee) {
            error = true;
        }
        Assert.assertTrue(error);
    }

    @RepeatedTest(50)
    void shutdownNow() {

        int poolSize = 10;
        ThreadPoolImpl threadPoolImpl = new ThreadPoolImpl(poolSize);
        for (int i = 0; i < N; ++i) {
            final int I = i;
            LightFuture<Integer> future = threadPoolImpl.execute(() -> {
                Thread.sleep((3 * 1000));
                return I;});
        }
        threadPoolImpl.shutdownNow();

        int queueSize = threadPoolImpl.getQueue().size();
        Assert.assertTrue(queueSize >= N - poolSize);
    }
}