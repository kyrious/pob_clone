package de.op.pob.simulation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public abstract class AbstractSimulation<T> {

    protected abstract T doSimulation();

    protected abstract T aggregateResults(Set<T> results);

    public T simulateNTimes(int n) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Set<Callable<T>> runner = new HashSet<>();
        for (int i = 0; i < n; i++) {
            runner.add(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    return AbstractSimulation.this.doSimulation();
                }
            });
        }
        List<Future<T>> resultFutures;
        try {
            resultFutures = executor.invokeAll(runner);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executor.shutdown();

        Set<T> results = resultFutures.stream()
                                      .map(this::getFromFuture)
                                      .collect(Collectors.toSet());

        return aggregateResults(results);
    }

    private T getFromFuture(Future<T> f) {
        try {
            return f.get();
        } catch (InterruptedException
                | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
