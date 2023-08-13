package com.garcia.ignacio.storeclassic.androidtesting;

import org.jetbrains.annotations.NotNull;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import kotlinx.coroutines.CoroutineDispatcher;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.test.TestCoroutineDispatchersKt;
import kotlinx.coroutines.test.TestDispatcher;
import kotlinx.coroutines.test.TestDispatchers;

public class CoroutineTestRule extends TestWatcher {
    private final TestDispatcher testDispatcher;

    public CoroutineTestRule(TestDispatcher testDispatcher) {
        this.testDispatcher = testDispatcher;
    }

    public TestDispatcher getTestDispatcher() {
        return testDispatcher;
    }

    public CoroutineTestRule() {
        this(TestCoroutineDispatchersKt.StandardTestDispatcher(null, null));
    }

    public DispatcherProvider getTestDispatcherProvider() {
        return new DispatcherProvider() {
            @NotNull
            @Override
            public CoroutineDispatcher main() {
                return testDispatcher;
            }

            @NotNull
            @Override
            public CoroutineDispatcher defaultDispatcher() {
                return testDispatcher;
            }

            @NotNull
            @Override
            public CoroutineDispatcher io() {
                return testDispatcher;
            }

            @NotNull
            @Override
            public CoroutineDispatcher unconfined() {
                return testDispatcher;
            }

        };
    }

    @Override
    protected void starting(@NotNull Description description) {
        TestDispatchers.setMain(Dispatchers.INSTANCE, testDispatcher);
    }

    @Override
    protected void finished(@NotNull Description description) {
        TestDispatchers.resetMain(Dispatchers.INSTANCE);
    }
}
