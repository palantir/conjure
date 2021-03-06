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

package com.palantir.conjure.defs.validator;

import com.palantir.conjure.exceptions.ConjureIllegalStateException;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.visitor.TypeVisitor;

public final class FieldDefinitionValidator {

    private FieldDefinitionValidator() {}

    public static void validate(FieldDefinition definition) {
        checkForComplexType(definition);
    }

    private static void checkForComplexType(FieldDefinition typeDef) {
        if (typeDef.getType().accept(TypeVisitor.IS_MAP)) {
            MapType mapType = typeDef.getType().accept(TypeVisitor.MAP);
            if (!mapType.getKeyType().accept(TypeVisitor.IS_PRIMITIVE_OR_REFERENCE)) {
                throw new ConjureIllegalStateException(
                        String.format("Complex type '%s' not allowed in map key: %s.", mapType.getKeyType(), typeDef));
            }
        }
    }
}
