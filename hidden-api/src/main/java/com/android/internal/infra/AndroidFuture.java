package com.android.internal.infra;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AndroidFuture<T> implements Future<T> {
    @Override
    public boolean cancel(boolean b) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public T get() throws ExecutionException, InterruptedException {
        return null;
    }

    @Override
    public T get(long l, TimeUnit timeUnit) throws ExecutionException, InterruptedException, TimeoutException {
        return null;
    }
}
