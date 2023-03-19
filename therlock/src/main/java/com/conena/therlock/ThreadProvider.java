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
 * Provides the threads for which the {@link BlockedThreadDetector} collects the stack traces
 * once it is detected that the monitored thread is blocked.
 */
@FunctionalInterface
public interface ThreadProvider {

    /**
     * @return The threads for which the {@link BlockedThreadDetector} collects the stack traces
     * once it is detected that the monitored thread is blocked.
     * The order of the threads in the array determines the subsequent order of the
     * {@link ThreadInfo} objects in {@link BlockedThreadEvent}.
     */
    @NotNull
    Thread @NotNull [] provideThreads();

}