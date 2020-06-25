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

import com.palantir.conjure.spec.AuthType;
import com.palantir.conjure.spec.CookieAuthType;
import com.palantir.conjure.spec.HeaderAuthType;

public final class AuthTypeVisitor {

    private AuthTypeVisitor(){}

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

    private static final class CookieAuthTypeVisitor extends DefaultAuthTypeVisitor<CookieAuthType> {
        @Override
        public CookieAuthType visitCookie(CookieAuthType value) {
            return value;
        }
    }

    private static class DefaultIsAuthTypeVisitor implements AuthType.Visitor<Boolean> {
        @Override
        public Boolean visitHeader(HeaderAuthType _value) {
            return false;
        }

        @Override
        public Boolean visitCookie(CookieAuthType _value) {
            return false;
        }

        @Override
        public Boolean visitUnknown(String _unknownType) {
            return false;
        }
    }

    private static final class IsHeaderAuthTypeVisitor extends DefaultIsAuthTypeVisitor {
        @Override
        public Boolean visitHeader(HeaderAuthType _value) {
            return true;
        }
    }

    private static final class IsCookieAuthTypeVisitor extends DefaultIsAuthTypeVisitor {
        @Override
        public Boolean visitCookie(CookieAuthType _value) {
            return true;
        }
    }
}
