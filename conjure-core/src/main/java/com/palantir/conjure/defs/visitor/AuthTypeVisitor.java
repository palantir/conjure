/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.visitor;

import com.palantir.conjure.spec.AuthType;
import com.palantir.conjure.spec.CookieAuthType;
import com.palantir.conjure.spec.HeaderAuthType;

public final class AuthTypeVisitor {

    private AuthTypeVisitor(){}

    // TODO(qchen): move somewhere more appropriate
    public static final String HEADER_NAME = "Authorization";

    public static final IsHeaderAuthTypeVisitor IS_HEADER = new IsHeaderAuthTypeVisitor();
    public static final IsCookieAuthTypeVisitor IS_COOKIE = new IsCookieAuthTypeVisitor();

    public static final CookieAuthTypeVisitor COOKIE = new CookieAuthTypeVisitor();

    private static class DefaultAuthTypeVisitor<T> implements AuthType.Visitor<T> {
        @Override
        public T visitHeader(HeaderAuthType value) {
            throw new IllegalStateException("Unknown type: " + value);
        }

        @Override
        public T visitCookie(CookieAuthType value) {
            throw new IllegalStateException("Unknown type: " + value);
        }

        @Override
        public T visitUnknown(String unknownType) {
            throw new IllegalStateException("Unknown type: " + unknownType);
        }
    }

    private static class CookieAuthTypeVisitor extends DefaultAuthTypeVisitor<CookieAuthType> {
        @Override
        public CookieAuthType visitCookie(CookieAuthType value) {
            return value;
        }
    }

    private static class DefaultIsAuthTypeVisitor implements AuthType.Visitor<Boolean> {
        @Override
        public Boolean visitHeader(HeaderAuthType value) {
            return false;
        }

        @Override
        public Boolean visitCookie(CookieAuthType value) {
            return false;
        }

        @Override
        public Boolean visitUnknown(String unknownType) {
            return false;
        }
    }

    private static class IsHeaderAuthTypeVisitor extends DefaultIsAuthTypeVisitor {
        @Override
        public Boolean visitHeader(HeaderAuthType value) {
            return true;
        }
    }

    private static class IsCookieAuthTypeVisitor extends DefaultIsAuthTypeVisitor {
        @Override
        public Boolean visitCookie(CookieAuthType value) {
            return true;
        }
    }
}
