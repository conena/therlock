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

/**
 * Defines an exemption when a thread should not be considered as blocked.
 * Can be used e.g. to create an exemption for debuggers.
 * This does not stop monitoring, it only suppresses reporting.
 */
@FunctionalInterface
public interface DetectionExemption {

    /**
     * @return True if the exception rule is active and a blocked thread should not currently be reported.
     */
    boolean isExemptionActive();

}