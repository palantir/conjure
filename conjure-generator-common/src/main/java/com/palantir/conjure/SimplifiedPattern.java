/*
 * (c) Copyright 2021 Palantir Technologies Inc. All rights reserved.
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
 */

package com.palantir.conjure;

/**
 * A simplified/restricted pattern matcher. Goal is to execute faster than general regex at the cost of reduced
 * features.
 *
 * JMH benchmarks suggest a ~40x increased throughput using the manual coded versions.
 */
public interface SimplifiedPattern {
    String pattern();

    boolean matches(String value);
}
