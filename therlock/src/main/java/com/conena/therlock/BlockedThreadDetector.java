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
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Used to detect blocked threads.
 * This does not necessarily mean threads in the {@link Thread.State#BLOCKED} state,
 * but that a thread that is intended to constantly accept tasks is no longer
 * capable of executing new tasks for a specified period of time.
 */
public class BlockedThreadDetector {

    /**
     * Used to post {@link Runnable} instances on the monitored thread.
     */
    @NotNull
    private final ThreadAccessor threadAccessor;

    /**
     * If a blocked thread is detected, the {@link ThreadProvider} is used to retrieve the threads
     * for which a stack trace should be saved in the {@link BlockedThreadEvent}.
     * The sort order of the returned threads will also be the order of the corresponding
     * stack traces in the {@link BlockedThreadEvent}.
     */
    @NotNull
    private final ThreadProvider threadProvider;

    /**
     * To receive {@link BlockedThreadEvent} instances.
     * The listener is called on a separate background thread.
     */
    @NotNull
    private final BlockedThreadListener listener;

    /**
     * Defines an exemption when a thread should not be considered as blocked.
     * Can be used e.g. to create an exemption for debuggers.
     * This does not stop monitoring, it only suppresses reporting.
     */
    @Nullable
    private final DetectionExemption exemption;

    /**
     * The minimum time in milliseconds a thread must be blocked for a {@link BlockedThreadEvent} to be triggered.
     */
    private final long threshold;

    /**
     * The interval in milliseconds in which it is checked whether a thread is blocked.
     */
    private final long inspectionInterval;

    /**
     * Used to ping the monitored thread on a regular basis.
     */
    @NotNull
    private final ScheduledExecutorService inspectionService = Executors.newSingleThreadScheduledExecutor(
            LibraryThreadFactory.createInspectorFactory()
    );

    /**
     * Used to report {@link BlockedThreadEvent}s to the listener.
     */
    @NotNull
    private final ExecutorService reportService = Executors.newSingleThreadExecutor(
            LibraryThreadFactory.createReporterFactory()
    );

    /**
     * The time in milliseconds how long the monitored thread has been blocked.
     */
    private final AtomicLong blockingTime = new AtomicLong();

    /**
     * If the current {@link BlockedThreadEvent} has already been reported to the listener.
     */
    private final AtomicBoolean reported = new AtomicBoolean();

    /**
     * A Future that represents the Inspection Task.
     */
    @Nullable
    private ScheduledFuture<?> inspectionTask = null;

    /**
     * @param threadAccessor     Used to post {@link Runnable} instances on the monitored thread.
     * @param threadProvider     If a blocked thread is detected, the {@link ThreadProvider} is used to retrieve
     *                           the threads for which a stack trace should be saved in the {@link BlockedThreadEvent}.
     *                           The sort order of the returned threads will also be the order of the corresponding
     *                           stack traces in the {@link BlockedThreadEvent}.
     * @param listener           To receive {@link BlockedThreadEvent} instances.
     *                           The listener is called on a separate background thread.
     * @param exemption          Defines an exemption when a thread should not be considered as blocked.
     *                           Can be used e.g. to create an exemption for debuggers.
     *                           This does not stop monitoring, it only suppresses reporting.
     * @param threshold          The minimum time in milliseconds a thread must be blocked for a {@link BlockedThreadEvent}
     *                           to be triggered.
     * @param inspectionInterval The interval in milliseconds in which it is checked whether
     *                           a thread is blocked. Together with the threshold this value decides if and how soon
     *                           blocked threads are detected.
     */
    public BlockedThreadDetector(
            @NotNull ThreadAccessor threadAccessor,
            @NotNull ThreadProvider threadProvider,
            @NotNull BlockedThreadListener listener,
            @Nullable DetectionExemption exemption,
            long threshold,
            long inspectionInterval
    ) {
        this.threadAccessor = threadAccessor;
        this.threadProvider = threadProvider;
        this.listener = listener;
        this.exemption = exemption;
        this.threshold = threshold;
        this.inspectionInterval = inspectionInterval;
    }

    /**
     * Start the detector. If it is already running, the method just returns.
     *
     * @return The current instance to chain multiple calls.
     */
    public synchronized BlockedThreadDetector startDetection() {
        return startDetection(0L);
    }

    /**
     * Start the detector. If it is already running, the method just returns.
     *
     * @param delay An initial delay in milliseconds from when the detection starts.
     *              Note that {@link #isRunning()} returns true as soon as this method returns,
     *              regardless of whether the detection is delayed or not.
     * @return The current instance to chain multiple calls.
     */
    public synchronized BlockedThreadDetector startDetection(long delay) {
        if (isRunning()) {
            return this;
        }
        resetAsync();
        inspectionTask = inspectionService.scheduleWithFixedDelay(
                this::checkIfThreadIsBlocked,
                delay,
                inspectionInterval,
                TimeUnit.MILLISECONDS
        );
        return this;
    }

    /**
     * Stop the detector. If the detector is not running, nothing happens.
     */
    public synchronized void stopDetection() {
        if (inspectionTask != null) {
            inspectionTask.cancel(false);
            inspectionTask = null;
        }
    }

    /**
     * @return True if the detector is running.
     * This is the case as soon as startDetection was called but stopDetection was not yet called.
     */
    public synchronized boolean isRunning() {
        return inspectionTask != null;
    }

    /**
     * Recurring task that checks whether the observed thread is blocked and
     * reports this as soon as the blocking time exceeds the {@link #threshold}.
     * This method must only be executed in the {@link #inspectionService}.
     */
    private void checkIfThreadIsBlocked() {
        if (exemption != null && exemption.isExemptionActive()) {
            reset();
            return;
        }
        long blockedSince = blockingTime.getAndAdd(inspectionInterval);
        if (blockedSince == 0L) {
            threadAccessor.post(this::resetAsync);
        } else if (blockedSince >= threshold && !reported.getAndSet(true)) {
            reportBlockedThread(blockedSince);
        }
    }

    /**
     * Reset the {@link #blockingTime} and the {@link #reported} state.
     * This method must only be executed in the {@link #inspectionService}.
     */
    private void reset() {
        blockingTime.set(0L);
        reported.set(false);
    }

    /**
     * Submits a {@link #reset()} in the {@link #inspectionService}.
     */
    private void resetAsync() {
        inspectionService.submit(this::reset);
    }

    /**
     * Reports a {@link BlockedThreadEvent} asynchronously.
     *
     * @param blockedFor The time in milliseconds how long the thread is blocked.
     */
    private void reportBlockedThread(final long blockedFor) {
        reportService.submit(() -> {
            final Thread[] threadsToReport = threadProvider.provideThreads();
            final ThreadInfo[] threadInfos = new ThreadInfo[threadsToReport.length];
            int reported = 0;
            for (Thread thread : threadsToReport) {
                final ThreadInfo threadInfo = ThreadInfo.fromThread(thread);
                if (threadInfo.stackTrace.length > 0) {
                    threadInfos[reported++] = threadInfo;
                }
            }
            listener.onBlockedThreadDetected(
                    this,
                    new BlockedThreadEvent(blockedFor, Arrays.copyOf(threadInfos, reported))
            );
        });
    }

}