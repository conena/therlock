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
 * Provides information about an event where the monitored thread was blocked.
 * This class inherits from Throwable for the sole purpose of making it compatible
 * with various bug reporting tools as well as making the event easy to display in the log.
 * {@link #getStackTrace()} shows all threads returned by the {@link ThreadProvider} for which a stack trace is available.
 * The order corresponds to the order in which {@link ThreadProvider#provideThreads()} has returned the threads.
 * Depending on the configuration of the {@link ThreadProvider},
 * it is therefore possible that the stack trace has nothing at all to do with the blocked thread.
 * Avoiding this is the responsibility of the implementer.
 * Also, don't get confused by the "Caused by" in the stack trace.
 * The order of the causes is given by the {@link ThreadProvider}, the caused by has no meaning and can be ignored.
 */
public final class BlockedThreadEvent extends Throwable {

    /**
     * The duration in milliseconds for which the monitored thread was blocked.
     */
    public final long blockedDuration;

    /**
     * Information about the threads that should be reported at the moment when the monitored thread was blocked.
     */
    @NotNull
    public final ThreadInfo @NotNull [] threadInfos;

    /**
     * @param blockedDuration The duration in milliseconds for which the monitored thread was blocked.
     * @param threadInfos     Information about the threads that should be reported at the moment when the monitored thread was blocked.
     */
    BlockedThreadEvent(long blockedDuration, @NotNull ThreadInfo @NotNull [] threadInfos) {
        super("The monitored thread was blocked for at least " + blockedDuration + " milliseconds. The stack trace contains the stack traces of all threads selected for reporting. Please refer to the documentation when interpreting the stack traces.");
        setStackTrace(new StackTraceElement[0]);
        Throwable lastProcessed = this;
        for (ThreadInfo threadInfo : threadInfos) {
            lastProcessed.initCause(threadInfo);
            lastProcessed = threadInfo;
        }
        this.blockedDuration = blockedDuration;
        this.threadInfos = threadInfos;
    }

}