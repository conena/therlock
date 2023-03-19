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

/**
 * Implementation of {@link ThreadProvider} that provides all active threads sorted by their id
 * which match an optional {@link ThreadFilter}.
 */
public final class FilteredThreadProvider extends ActiveThreadProvider {

    /**
     * The {@link ThreadFilter} to use.
     */
    @NotNull
    private final ThreadFilter threadFilter;

    /**
     * @param filter The {@link ThreadFilter} to use. You can use {@link CombinedThreadFilter}
     *               to combine multiple filter.
     * @see CombinedThreadFilter
     * @see DaemonThreadFilter
     * @see LibraryThreadFilter
     * @see PriorityThreadFilter
     */
    public FilteredThreadProvider(@NotNull ThreadFilter filter) {
        threadFilter = filter;
    }

    /**
     * @return All active threads that match the applied {@link ThreadFilter} sorted by id.
     */
    @NotNull
    @Override
    public Thread @NotNull [] provideThreads() {
        Thread[] activeThreads = super.provideThreads();
        Thread[] filteredThreads = new Thread[activeThreads.length];
        int threadCount = 0;
        for (Thread thread : activeThreads) {
            if (threadFilter.isAllowed(thread)) {
                filteredThreads[threadCount++] = thread;
            }
        }
        return Arrays.copyOf(filteredThreads, threadCount);
    }

}