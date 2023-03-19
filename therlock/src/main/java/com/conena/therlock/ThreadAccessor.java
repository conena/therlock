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
 * Used to allow the {@link BlockedThreadDetector} to post a {@link Runnable} on the monitored thread.
 * In fact, you can also use it to monitor things other than a thread. For example, a thread group.
 */
@FunctionalInterface
public interface ThreadAccessor {

    /**
     * Post the provided {@link Runnable} on the monitored thread.
     *
     * @param runnable The runnable with the code that the BlockedThreadDetector
     *                 uses to determine if the thread is blocked.
     */
    void post(@NotNull Runnable runnable);

}