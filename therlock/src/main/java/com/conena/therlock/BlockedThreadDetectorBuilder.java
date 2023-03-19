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

/**
 * Build a {@link BlockedThreadDetector} instance.
 */
public class BlockedThreadDetectorBuilder {

    /**
     * Used to post {@link Runnable} instances on the monitored thread.
     */
    @NotNull
    private final ThreadAccessor threadAccessor;

    /**
     * Provides the threads that are included in the {@link BlockedThreadEvent},
     * with the current stack trace, in the order provided by the {@link ThreadProvider}, in the detection case.
     */
    @Nullable
    private ThreadProvider threadProvider;

    /**
     * To receive {@link BlockedThreadEvent} instances.
     * The listener is called on a separate background thread.
     */
    @Nullable
    private BlockedThreadListener listener;

    /**
     * Defines an exemption when a thread should not be considered as blocked.
     * Can be used e.g. to create an exemption for debuggers.
     * This does not stop monitoring, it only suppresses reporting.
     */
    @Nullable
    private DetectionExemption exemption;

    /**
     * The minimum time in milliseconds a thread must be blocked for a {@link BlockedThreadEvent} to be triggered.
     */
    @Nullable
    private Long threshold;

    /**
     * The interval in milliseconds in which it is checked whether a thread is blocked.
     */
    @Nullable
    private Long inspectionInterval;

    /**
     * @param threadAccessor Used to post {@link Runnable} instances on the monitored thread.
     */
    public BlockedThreadDetectorBuilder(@NotNull ThreadAccessor threadAccessor) {
        this.threadAccessor = threadAccessor;
    }

    /**
     * @param threadProvider If a blocked thread is detected, the {@link ThreadProvider} is used to retrieve
     *                       the threads for which a stack trace should be saved in the {@link BlockedThreadEvent}.
     *                       The sort order of the returned threads will also be the order of the corresponding
     *                       stack traces in the {@link BlockedThreadEvent}.
     * @return A reference to this instance.
     * @see ActiveThreadProvider
     * @see FilteredThreadProvider
     * @see PredefinedThreadProvider
     */
    @NotNull
    public BlockedThreadDetectorBuilder setThreadProvider(@NotNull ThreadProvider threadProvider) {
        this.threadProvider = threadProvider;
        return this;
    }

    /**
     * @param listener To receive {@link BlockedThreadEvent} instances.
     *                 The listener is called on a separate background thread.
     * @return A reference to this instance.
     */
    @NotNull
    public BlockedThreadDetectorBuilder setListener(@NotNull BlockedThreadListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * @param exemption Defines an exemption when a thread should not be considered as blocked.
     *                  Can be used e.g. to create an exemption for debuggers.
     *                  This does not stop monitoring, it only suppresses reporting.
     * @return A reference to this instance.
     */
    @NotNull
    public BlockedThreadDetectorBuilder setExemption(@NotNull DetectionExemption exemption) {
        this.exemption = exemption;
        return this;
    }

    /**
     * @param threshold The minimum time in milliseconds a thread must be blocked for a {@link BlockedThreadEvent} to be triggered.
     * @return A reference to this instance.
     */
    @NotNull
    public BlockedThreadDetectorBuilder setThreshold(long threshold) {
        this.threshold = threshold;
        return this;
    }

    /**
     * @param interval The interval in milliseconds in which it is checked whether a thread is blocked.
     * @return A reference to this instance.
     */
    @NotNull
    public BlockedThreadDetectorBuilder setInspectionInterval(long interval) {
        this.inspectionInterval = interval;
        return this;
    }

    /**
     * Build a {@link BlockedThreadDetector} with the parameters supplied to the builder methods.
     * If {@link #setThreadProvider(ThreadProvider)} was not called, a {@link FilteredThreadProvider}
     * with a {@link DaemonThreadFilter} will be used.
     * If {@link #setListener(BlockedThreadListener)} was not called a {@link BlockedThreadLogWriter} will be used.
     * If {@link #setThreshold(long)} was not called 1000 milliseconds will be used.
     * If {@link #setInspectionInterval(long)} was not called, one fifth of the threshold value, but at least 100 ms and at most 500ms, is used.
     *
     * @return The created {@link BlockedThreadDetector}.
     */
    @NotNull
    public BlockedThreadDetector build() {
        long threshold = this.threshold == null ? 1_000L : this.threshold;
        return new BlockedThreadDetector(
                threadAccessor,
                threadProvider == null ? new FilteredThreadProvider(new DaemonThreadFilter()) : threadProvider,
                listener == null ? new BlockedThreadLogWriter() : listener,
                exemption,
                threshold,
                inspectionInterval == null ? Math.min(500L, Math.max(100L, threshold / 5L)) : inspectionInterval
        );
    }

}