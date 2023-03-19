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

/**
 * An implementation of {@link ThreadFilter} that accepts multiple ThreadFilters
 * and allows a thread only if all accepted ThreadFilters allow the thread.
 */
public class CombinedThreadFilter implements ThreadFilter {

    /**
     * The applied {@link ThreadFilter} instances.
     */
    @NotNull
    private final ThreadFilter @NotNull [] filters;

    /**
     * @param threadFilter The filters that are applied one after the other.
     */
    public CombinedThreadFilter(@NotNull ThreadFilter @NotNull ... threadFilter) {
        this.filters = threadFilter;
    }

    @Override
    public final boolean isAllowed(@NotNull Thread thread) {
        for (ThreadFilter filter : filters) {
            if (!filter.isAllowed(thread)) {
                return false;
            }
        }
        return true;
    }

}
