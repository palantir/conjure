/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.conjure.either;

import java.util.function.Function;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

public interface Either<L, R> {

    static <L, R> Either<L, R> left(L left) {
        return ImmutableLeft.of(left);
    }

    static <L, R> Either<L, R> right(R right) {
        return ImmutableRight.of(right);
    }

    <T> T fold(Function<? super L, ? extends T> mapLeft, Function<? super R, ? extends T> mapRight);

    @Immutable
    abstract class Left<L, R> implements Either<L, R> {

        @Parameter
        abstract L value();

        @Override
        public <T> T fold(Function<? super L, ? extends T> mapLeft, Function<? super R, ? extends T> mapRight) {
            return mapLeft.apply(value());
        }
    }

    @Immutable
    abstract class Right<L, R> implements Either<L, R> {
        @Parameter
        abstract R value();

        @Override
        public <T> T fold(Function<? super L, ? extends T> mapLeft, Function<? super R, ? extends T> mapRight) {
            return mapRight.apply(value());
        }
    }
}
