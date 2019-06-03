/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.conjure.parser;

import com.palantir.conjure.spec.ConjureDefinition;
import com.palantir.conjure.spec.ErrorDefinition;
import com.palantir.conjure.spec.ServiceDefinition;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.spec.TypeName;
import com.palantir.conjure.visitor.TypeDefinitionVisitor;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class NormalizeDefinition {

    /** Ensures the order of types, services and endpoints is sorted. */
    public static ConjureDefinition normalize(ConjureDefinition input) {
        return ConjureDefinition.builder()
                .version(input.getVersion())
                .types(sortTypeDefinitions(input.getTypes()))
                .services(sortServiceDefinitions(input.getServices()))
                .errors(sortErrorDefinitions(input.getErrors()))
                .build();
    }

    private static List<TypeDefinition> sortTypeDefinitions(List<TypeDefinition> types) {
        return types.stream()
                .sorted(Comparator.comparing(def -> typeNameToString(def.accept(TypeDefinitionVisitor.TYPE_NAME))))
                .collect(Collectors.toList());
    }

    private static List<ServiceDefinition> sortServiceDefinitions(List<ServiceDefinition> services) {
        // we intentionally don't sort the Endpoints _within_ a ServiceDefinition, because these can be preserved
        // from the source yml
        return services.stream()
                .sorted(Comparator.comparing(def -> typeNameToString(def.getServiceName())))
                .collect(Collectors.toList());
    }

    private static List<ErrorDefinition> sortErrorDefinitions(List<ErrorDefinition> errors) {
        return errors.stream()
                .sorted(Comparator.comparing(def -> typeNameToString(def.getErrorName())))
                .collect(Collectors.toList());
    }

    private static String typeNameToString(TypeName typeName) {
        return typeName.getPackage() + "." + typeName.getName();
    }

    private NormalizeDefinition() {}
}
