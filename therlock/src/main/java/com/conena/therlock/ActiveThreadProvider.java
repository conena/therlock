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

import java.util.Arrays;
import java.util.Comparator;


/**
 * Implementation of {@link ThreadProvider} that provides all active threads sorted by their id.
 */
public class ActiveThreadProvider implements ThreadProvider {

    /**
     * Compare {@link Thread} instances by their id.
     */
    // Comparator.comparingLong would break compatibility with older android versions.
    // https://developer.android.com/reference/java/util/Comparator#comparingLong(java.util.function.ToLongFunction%3C?%20super%20T%3E)
    @SuppressWarnings("ComparatorCombinators")
    @NotNull
    private final static Comparator<Thread> threadIdComparator = (Thread t1, Thread t2) ->
            Long.compare(t1.getId(), t2.getId());

    /**
     * @return All active threads sorted by id.
     */
    @NotNull
    @Override
    public Thread @NotNull [] provideThreads() {
        ThreadGroup root = getRootThreadGroup();
        Thread[] threadHolder = new Thread[root.activeCount() * 2];
        Thread[] threadArray = Arrays.copyOf(threadHolder, root.enumerate(threadHolder));
        Arrays.sort(threadArray, threadIdComparator);
        return threadArray;
    }

    /**
     * @return The root {@link ThreadGroup} that all other groups belong to.
     */
    @NotNull
    private ThreadGroup getRootThreadGroup() {
        ThreadGroup group = getCurrentThreadGroup();
        ThreadGroup parent = group.getParent();
        while (parent != null) {
            group = parent;
            parent = group.getParent();
        }
        return group;
    }

    /**
     * @return The {@link ThreadGroup} the current thread belongs to.
     */
    @NotNull
    private ThreadGroup getCurrentThreadGroup() {
        return Thread.currentThread().getThreadGroup();
    }

}