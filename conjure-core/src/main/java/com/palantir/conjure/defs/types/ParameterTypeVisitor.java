/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

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

    public static final IsBodyIsParameterType IS_BODY = new IsBodyIsParameterType();
    public static final IsPathIsParameterType IS_PATH = new IsPathIsParameterType();
    public static final IsHeaderIsParameterType IS_HEADER = new IsHeaderIsParameterType();
    public static final IsQueryIsParameterType IS_QUERY = new IsQueryIsParameterType();

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

    private static class QueryParameterTypeVisitor extends DefaultParameterType<QueryParameterType> {
        @Override
        public QueryParameterType visitQuery(QueryParameterType value) {
            return value;
        }
    }

    private static class HeaderParameterTypeVisitor extends DefaultParameterType<HeaderParameterType> {
        @Override
        public HeaderParameterType visitHeader(HeaderParameterType value) {
            return value;
        }
    }

    private static class BodyIsParameterType extends DefaultParameterType<BodyParameterType> {
        @Override
        public BodyParameterType visitBody(BodyParameterType value) {
            return value;
        }
    }

    private static class PathIsParameterType extends DefaultParameterType<PathParameterType> {
        @Override
        public PathParameterType visitPath(PathParameterType value) {
            return value;
        }
    }

    private static class IsParameterTypeVisitor implements ParameterType.Visitor<Boolean> {
        @Override
        public Boolean visitBody(BodyParameterType value) {
            return false;
        }

        @Override
        public Boolean visitHeader(HeaderParameterType value) {
            return false;
        }

        @Override
        public Boolean visitPath(PathParameterType value) {
            return false;
        }

        @Override
        public Boolean visitQuery(QueryParameterType value) {
            return false;
        }

        @Override
        public Boolean visitUnknown(String unknownType) {
            return false;
        }
    }

    private static class IsBodyIsParameterType extends IsParameterTypeVisitor {
        @Override
        public Boolean visitBody(BodyParameterType value) {
            return true;
        }
    }

    private static class IsPathIsParameterType extends IsParameterTypeVisitor {
        @Override
        public Boolean visitPath(PathParameterType value) {
            return true;
        }
    }

    private static class IsHeaderIsParameterType extends IsParameterTypeVisitor {
        @Override
        public Boolean visitHeader(HeaderParameterType value) {
            return true;
        }
    }

    private static class IsQueryIsParameterType extends IsParameterTypeVisitor {
        @Override
        public Boolean visitQuery(QueryParameterType value) {
            return true;
        }
    }
}
