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

package com.palantir.conjure.visitor;

import com.palantir.conjure.spec.BodyParameterType;
import com.palantir.conjure.spec.HeaderParameterType;
import com.palantir.conjure.spec.ParameterType;
import com.palantir.conjure.spec.PathParameterType;
import com.palantir.conjure.spec.QueryParameterType;

public final class ParameterTypeVisitor {

    public static final QueryParameterTypeVisitor QUERY = new QueryParameterTypeVisitor();
    public static final HeaderParameterTypeVisitor HEADER = new HeaderParameterTypeVisitor();
    public static final BodyIsParameterType BODY = new BodyIsParameterType();
    public static final PathIsParameterType PATH = new PathIsParameterType();

    public static final IsBodyParameterType IS_BODY = new IsBodyParameterType();
    public static final IsPathParameterType IS_PATH = new IsPathParameterType();
    public static final IsHeaderParameterType IS_HEADER = new IsHeaderParameterType();
    public static final IsQueryParameterType IS_QUERY = new IsQueryParameterType();

    private ParameterTypeVisitor() {}

    private static class DefaultParameterType<T> implements ParameterType.Visitor<T> {
        @Override
        public T visitBody(BodyParameterType value) {
            throw new IllegalStateException("Unsupported type: " + value);
        }

        @Override
        public T visitHeader(HeaderParameterType value) {
            throw new IllegalStateException("Unsupported type: " + value);
        }

        @Override
        public T visitPath(PathParameterType value) {
            throw new IllegalStateException("Unsupported type: " + value);
        }

        @Override
        public T visitQuery(QueryParameterType value) {
            throw new IllegalStateException("Unsupported type: " + value);
        }

        @Override
        public T visitUnknown(String unknownType) {
            throw new IllegalStateException("Unsupported type: " + unknownType);
        }
    }

    private static final class QueryParameterTypeVisitor extends DefaultParameterType<QueryParameterType> {
        @Override
        public QueryParameterType visitQuery(QueryParameterType value) {
            return value;
        }
    }

    private static final class HeaderParameterTypeVisitor extends DefaultParameterType<HeaderParameterType> {
        @Override
        public HeaderParameterType visitHeader(HeaderParameterType value) {
            return value;
        }
    }

    private static final class BodyIsParameterType extends DefaultParameterType<BodyParameterType> {
        @Override
        public BodyParameterType visitBody(BodyParameterType value) {
            return value;
        }
    }

    private static final class PathIsParameterType extends DefaultParameterType<PathParameterType> {
        @Override
        public PathParameterType visitPath(PathParameterType value) {
            return value;
        }
    }

    private static class IsParameterTypeVisitor implements ParameterType.Visitor<Boolean> {
        @Override
        public Boolean visitBody(BodyParameterType _value) {
            return false;
        }

        @Override
        public Boolean visitHeader(HeaderParameterType _value) {
            return false;
        }

        @Override
        public Boolean visitPath(PathParameterType _value) {
            return false;
        }

        @Override
        public Boolean visitQuery(QueryParameterType _value) {
            return false;
        }

        @Override
        public Boolean visitUnknown(String _unknownType) {
            return false;
        }
    }

    private static final class IsBodyParameterType extends IsParameterTypeVisitor {
        @Override
        public Boolean visitBody(BodyParameterType _value) {
            return true;
        }
    }

    private static final class IsPathParameterType extends IsParameterTypeVisitor {
        @Override
        public Boolean visitPath(PathParameterType _value) {
            return true;
        }
    }

    private static final class IsHeaderParameterType extends IsParameterTypeVisitor {
        @Override
        public Boolean visitHeader(HeaderParameterType _value) {
            return true;
        }
    }

    private static final class IsQueryParameterType extends IsParameterTypeVisitor {
        @Override
        public Boolean visitQuery(QueryParameterType _value) {
            return true;
        }
    }
}
