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

package com.palantir.conjure.parser.types;

import com.palantir.conjure.parser.types.complex.EnumTypeDefinition;
import com.palantir.conjure.parser.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.parser.types.complex.UnionTypeDefinition;
import com.palantir.conjure.parser.types.reference.AliasTypeDefinition;

public interface TypeDefinitionVisitor<T> {
    T visit(AliasTypeDefinition def);

    T visit(EnumTypeDefinition def);

    T visit(ObjectTypeDefinition def);

    T visit(UnionTypeDefinition def);
}
