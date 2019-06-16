package com.example.tmsemu;

// Got from here: https://github.com/googlesamples/android-architecture-components/issues/29

import android.arch.lifecycle.ViewModel;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class GlobalViewModel extends ViewModel {
    private final AtomicInteger mRefCounter;

    private final Runnable mOnShareCleared;

    public GlobalViewModel(Runnable onShareCleared, Integer param) {
        mRefCounter = new AtomicInteger(0);
        mOnShareCleared = onShareCleared;
    }

    protected final void onCleared() {
        decRefCount();
    }

    protected abstract void onShareCleared();

    protected final int incRefCount() {
        return mRefCounter.incrementAndGet();
    }

    private final int decRefCount() {
        int counter = mRefCounter.decrementAndGet();
        if (counter == 0) {
            if (mOnShareCleared != null) {
                mOnShareCleared.run();
            }
            onShareCleared();
        } else if (counter < 0) {
            mRefCounter.set(0);
            counter = 0;
        }
        return counter;
    }

}
