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
 * Provides information about a thread for which a report was requested.
 */
public class ThreadInfo extends Throwable {

    /**
     * The name of the {@link Thread}.
     *
     * @see Thread#getName()
     */
    @NotNull
    public final String name;

    /**
     * The name of the {@link ThreadGroup} the thread belongs to.
     *
     * @see Thread#getThreadGroup()
     */
    @NotNull
    public final String groupName;

    /**
     * The id of the {@link Thread}.
     *
     * @see Thread#getId()
     */
    public final long id;

    /**
     * The priority of the {@link Thread}.
     *
     * @see Thread#getPriority()
     */
    public final int priority;

    /**
     * The stack trace of the {@link Thread}.
     *
     * @see Thread#getStackTrace()
     */
    @NotNull
    public final StackTraceElement @NotNull [] stackTrace;

    /**
     * @param name       The name of the {@link Thread}.
     * @param groupName  The name of the {@link ThreadGroup} the thread belongs to.
     * @param id         The id of the {@link Thread}.
     * @param priority   The priority of the {@link Thread}.
     * @param stackTrace The stack trace of the {@link Thread}.
     */
    ThreadInfo(
            @NotNull String name,
            @NotNull String groupName,
            long id,
            int priority,
            @NotNull StackTraceElement @NotNull [] stackTrace
    ) {
        super(String.format("Stacktrace of the thread '%s' (id: %d, group: '%s').", name, id, groupName));
        setStackTrace(stackTrace);
        this.name = name;
        this.groupName = groupName;
        this.id = id;
        this.priority = priority;
        this.stackTrace = stackTrace;
    }

    /**
     * Create a {@link ThreadInfo} instance from a {@link Thread}.
     *
     * @param thread The thread for which the {@link ThreadInfo} should be created.
     * @return The created {@link ThreadInfo} instance provides information about the provided thread.
     */
    @NotNull
    static ThreadInfo fromThread(@NotNull Thread thread) {
        return new ThreadInfo(
                thread.getName(),
                thread.getThreadGroup().getName(),
                thread.getId(),
                thread.getPriority(),
                thread.getStackTrace()
        );
    }

}