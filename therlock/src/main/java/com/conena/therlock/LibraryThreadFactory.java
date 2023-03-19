/*
 * Copyright (C) 2023 Fabian Andera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.conena.therlock;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;

/**
 * Internal class which is used to create the threads used by this library.
 * In practice, each instance is only used to create a single thread.
 */
final class LibraryThreadFactory implements ThreadFactory {

    /**
     * The {@link ThreadGroup} for all threads of this library.
     */
    @NotNull
    static final ThreadGroup libraryGroup = new ThreadGroup("therlock");

    /**
     * The name of the threads provided by {@link ThreadFactory#newThread(Runnable)}.
     */
    @NotNull
    private final String name;

    /**
     * @param threadName The name of the threads provided by {@link ThreadFactory#newThread(Runnable)}.
     */
    LibraryThreadFactory(@NotNull String threadName) {
        super();
        this.name = threadName;
    }

    /**
     * @return A {@link ThreadFactory} to create the thread used by the {@link BlockedThreadDetector}
     * to monitor the selected thread.
     */
    @NotNull
    static ThreadFactory createInspectorFactory() {
        return new LibraryThreadFactory("inspector-thread");
    }

    /**
     * @return A {@link ThreadFactory} to create the thread used by the {@link BlockedThreadDetector}
     * to to report when the monitored thread is blocked.
     */
    @NotNull
    static ThreadFactory createReporterFactory() {
        return new LibraryThreadFactory("reporter-thread");
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        Thread thread = new Thread(libraryGroup, r, name);
        thread.setDaemon(true);
        return thread;
    }

}