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
 * Implementation of {@link ThreadFilter} which filters out daemon threads.
 * It is not necessary to combine this filter with the {@link LibraryThreadFilter},
 * because all library threads are daemon threads.
 */
public final class DaemonThreadFilter implements ThreadFilter {

    @Override
    public boolean isAllowed(@NotNull Thread thread) {
        return !thread.isDaemon();
    }

}