/*
 * (c) Copyright 2026 Palantir Technologies Inc. All rights reserved.
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

import com.google.common.base.Preconditions;
import com.palantir.conjure.spec.ArgumentDefinition;
import com.palantir.conjure.spec.EndpointDefinition;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.visitor.DealiasingTypeVisitor;
import com.palantir.conjure.visitor.ParameterTypeVisitor;
import com.palantir.conjure.visitor.TypeDefinitionVisitor;
import com.palantir.conjure.visitor.TypeVisitor;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

final class RouteByValidator implements ConjureContextualValidator<EndpointDefinition> {
    private static final Pattern SELECTOR = Pattern.compile("[a-z][a-zA-Z0-9]*(\\.[a-z][a-zA-Z0-9]*)*");

    @Override
    public void validate(EndpointDefinition endpoint, DealiasingTypeVisitor types) {
        Set<String> seen = new HashSet<>();
        for (String selector : endpoint.getRouteBy()) {
            Preconditions.checkArgument(
                    SELECTOR.matcher(selector).matches(), "Invalid route-by selector: %s", selector);
            Preconditions.checkArgument(seen.add(selector), "Duplicate route-by selector: %s", selector);
            String[] path = selector.split("\\.");
            ArgumentDefinition argument = endpoint.getArgs().stream()
                    .filter(arg -> arg.getArgName().get().equals(path[0]))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown route-by argument: " + selector));
            Preconditions.checkArgument(
                    argument.getParamType().accept(ParameterTypeVisitor.IS_PATH)
                            || argument.getParamType().accept(ParameterTypeVisitor.IS_BODY),
                    "route-by requires a path or body argument: %s",
                    selector);
            validateType(argument.getType(), path, 1, selector, types.withoutExternalFallbacks());
        }
    }

    private static void validateType(
            Type type, String[] path, int offset, String selector, DealiasingTypeVisitor types) {
        types.dealias(type)
                .fold(
                        definition -> {
                            if (offset == path.length) {
                                Preconditions.checkArgument(
                                        definition.accept(TypeDefinitionVisitor.IS_ENUM),
                                        "route-by must select a scalar or enum: %s",
                                        selector);
                            } else {
                                Preconditions.checkArgument(
                                        definition.accept(TypeDefinitionVisitor.IS_OBJECT),
                                        "route-by can only traverse object fields: %s",
                                        selector);
                                FieldDefinition field =
                                        definition.accept(TypeDefinitionVisitor.OBJECT).getFields().stream()
                                                .filter(candidate -> candidate
                                                        .getFieldName()
                                                        .get()
                                                        .equals(path[offset]))
                                                .findFirst()
                                                .orElseThrow(() -> new IllegalArgumentException(
                                                        "Unknown route-by field: " + selector));
                                validateType(field.getType(), path, offset + 1, selector, types);
                            }
                            return true;
                        },
                        terminal -> {
                            if (terminal.accept(TypeVisitor.IS_OPTIONAL)) {
                                validateType(
                                        terminal.accept(TypeVisitor.OPTIONAL).getItemType(),
                                        path,
                                        offset,
                                        selector,
                                        types);
                            } else {
                                Preconditions.checkArgument(
                                        offset == path.length && terminal.accept(TypeVisitor.IS_PRIMITIVE),
                                        "route-by must select a scalar or enum: %s",
                                        selector);
                                PrimitiveType primitive = terminal.accept(TypeVisitor.PRIMITIVE);
                                Preconditions.checkArgument(
                                        !primitive.equals(PrimitiveType.ANY),
                                        "Unsupported route-by scalar: %s",
                                        selector);
                                Preconditions.checkArgument(
                                        !primitive.equals(PrimitiveType.BINARY) || path.length > 1,
                                        "route-by binary selectors must select a body field: %s",
                                        selector);
                            }
                            return true;
                        });
    }
}
