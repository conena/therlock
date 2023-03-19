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
 * An implementation of {@link DetectionExemption} that accepts multiple DetectionExemptions
 * and affirms an exemption as soon as one of the defined exemptions applies.
 */
public class CombinedDetectionExemption implements DetectionExemption {

    /**
     * The applied {@link DetectionExemption} instances.
     */
    @NotNull
    private final DetectionExemption @NotNull [] exemptions;

    /**
     * @param exemptions The exemptions that are checked one after the other.
     */
    public CombinedDetectionExemption(@NotNull DetectionExemption @NotNull ... exemptions) {
        this.exemptions = exemptions;
    }

    @Override
    public final boolean isExemptionActive() {
        for (DetectionExemption exemption : exemptions) {
            if (exemption.isExemptionActive()) {
                return true;
            }
        }
        return false;
    }

}