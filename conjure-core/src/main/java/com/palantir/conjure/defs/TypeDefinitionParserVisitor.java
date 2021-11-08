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

package com.palantir.conjure.defs;

import com.palantir.conjure.parser.types.TypeDefinitionVisitor;
import com.palantir.conjure.parser.types.complex.EnumTypeDefinition;
import com.palantir.conjure.parser.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.parser.types.complex.UnionTypeDefinition;
import com.palantir.conjure.parser.types.reference.AliasTypeDefinition;
import com.palantir.conjure.spec.TypeDefinition;
import java.util.Optional;

/** The core translator between parsed/raw type definitions and the IR spec representation exposed to compilers. */
public final class TypeDefinitionParserVisitor implements TypeDefinitionVisitor<TypeDefinition> {

    private final String name;
    private final Optional<String> defaultPackage;
    private final ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver;
    private final ConjureOptions options;

    public TypeDefinitionParserVisitor(
            String typeName,
            Optional<String> defaultPackage,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver,
            ConjureOptions options) {
        this.name = typeName;
        this.defaultPackage = defaultPackage;
        this.typeResolver = typeResolver;
        this.options = options;
    }

    @Override
    public TypeDefinition visit(AliasTypeDefinition def) {
        return ConjureParserUtils.parseAliasType(
                ConjureParserUtils.createTypeName(name, def, defaultPackage), def, typeResolver);
    }

    @Override
    public TypeDefinition visit(EnumTypeDefinition def) {
        return ConjureParserUtils.parseEnumType(
                ConjureParserUtils.createTypeName(name, def, defaultPackage), def, options);
    }

    @Override
    public TypeDefinition visit(ObjectTypeDefinition def) {
        return ConjureParserUtils.parseObjectType(
                ConjureParserUtils.createTypeName(name, def, defaultPackage), def, typeResolver, options);
    }

    @Override
    public TypeDefinition visit(UnionTypeDefinition def) {
        return ConjureParserUtils.parseUnionType(
                ConjureParserUtils.createTypeName(name, def, defaultPackage), def, typeResolver, options);
    }
}
