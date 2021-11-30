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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public final class CachedPatternMatcher {
    private final Pattern pattern;

    @SuppressWarnings("checkstyle:IllegalType")
    private final LoadingCache<String, Boolean> cachedMatchers;

    private CachedPatternMatcher(Pattern pattern) {
        this.pattern = pattern;

        this.cachedMatchers = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(10))
                .maximumSize(5_000)
                .build(new CacheLoader<String, Boolean>() {
                    @Override
                    public Boolean load(String key) {
                        return pattern.matcher(key).matches();
                    }
                });
    }

    public static CachedPatternMatcher wrap(Pattern pattern) {
        return new CachedPatternMatcher(pattern);
    }

    public String pattern() {
        return this.pattern.pattern();
    }

    public boolean matches(String value) {
        try {
            return cachedMatchers.get(value);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new RuntimeException(e.getCause());
        }
    }
}
