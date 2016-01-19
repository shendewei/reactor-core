package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.Callable;

import org.reactivestreams.Subscriber;
import reactor.core.error.Exceptions;
import reactor.core.subscriber.SubscriberDeferredScalar;
import reactor.core.support.ReactiveState;

/**
 * Executes a Callable function and emits a single value to each individual Subscriber.
 * <p>
 *  Preferred to {@link Supplier} because the Callable may throw.
 *
 * @param <T> the returned value type
 */

/**
 * {@see https://github.com/reactor/reactive-streams-commons}
 * @since 2.5
 */
public final class MonoCallable<T> 
extends reactor.Mono<T>
implements 
												   ReactiveState.Factory,
												   ReactiveState.Upstream {

	final Callable<? extends T> callable;

	public MonoCallable(Callable<? extends T> callable) {
		this.callable = Objects.requireNonNull(callable, "callable");
	}

	@Override
	public Object upstream() {
		return callable;
	}

	@Override
	public void subscribe(Subscriber<? super T> s) {

		SubscriberDeferredScalar<T, T> sds = new SubscriberDeferredScalar<>(s);

		s.onSubscribe(sds);

		if (sds.isCancelled()) {
			return;
		}

		T t;
		try {
			t = callable.call();
		} catch (Throwable e) {
			Exceptions.throwIfFatal(e);
			s.onError(e);
			return;
		}

		if (t == null) {
			s.onError(new NullPointerException("The callable returned null"));
			return;
		}

		sds.complete(t);
	}
}
