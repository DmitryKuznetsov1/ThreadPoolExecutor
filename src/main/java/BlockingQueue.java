import java.util.LinkedList;

public class BlockingQueue<E> {

    private final LinkedList<E> queue = new LinkedList<>();
    private volatile boolean running = true;

    public void add(E e) throws UnsupportedOperationException {

        synchronized (queue) {
            if (running) {
                queue.add(e);
                queue.notifyAll();
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    public E poll() throws InterruptedException {

        synchronized (queue) {
            while (queue.isEmpty() && running) {
                queue.wait();
            }
            E polled = queue.poll();
            return polled;
        }
    }

    public boolean breakWaiting() {

        synchronized (queue) {
            running = false;
            queue.notifyAll();
        }
        return false;
    }

    public int size() {

        synchronized (queue) {
            return queue.size();
        }
    }

//    public E peek() {
//
//    }
}