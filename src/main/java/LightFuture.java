import java.util.function.Function;

public interface LightFuture<R> {

    boolean isReady();

    R get() throws LightExecutionException;

    <S> LightFuture<S> thenApply(Function<? super R, S> function) throws LightExecutionException;
}
