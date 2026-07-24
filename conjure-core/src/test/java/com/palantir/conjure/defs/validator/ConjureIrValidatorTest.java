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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.conjure.defs.SafetyDeclarationRequirements;
import com.palantir.conjure.spec.ArgumentDefinition;
import com.palantir.conjure.spec.ArgumentName;
import com.palantir.conjure.spec.ConjureDefinition;
import com.palantir.conjure.spec.EndpointDefinition;
import com.palantir.conjure.spec.EndpointName;
import com.palantir.conjure.spec.EnumDefinition;
import com.palantir.conjure.spec.EnumValueDefinition;
import com.palantir.conjure.spec.ErrorCode;
import com.palantir.conjure.spec.ErrorDefinition;
import com.palantir.conjure.spec.ErrorNamespace;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.FieldName;
import com.palantir.conjure.spec.HttpMethod;
import com.palantir.conjure.spec.HttpPath;
import com.palantir.conjure.spec.ObjectDefinition;
import com.palantir.conjure.spec.ParameterId;
import com.palantir.conjure.spec.ParameterType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.QueryParameterType;
import com.palantir.conjure.spec.ServiceDefinition;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.spec.TypeName;
import com.palantir.conjure.spec.UnionDefinition;
import java.util.List;
import org.junit.jupiter.api.Test;

public final class ConjureIrValidatorTest {

    private static final TypeName ENUM_NAME = TypeName.of("Color", "com.example.product");

    @Test
    public void rejectsEnumValueOutsideGrammar() {
        ConjureDefinition definition = ConjureDefinition.builder()
                .version(1)
                .types(List.of(TypeDefinition.enum_(EnumDefinition.builder()
                        .typeName(ENUM_NAME)
                        .values(List.of(
                                EnumValueDefinition.builder().value("RED").build(),
                                EnumValueDefinition.builder().value("not valid").build()))
                        .build())))
                .build();

        assertThatThrownBy(() -> ConjureIrValidator.validate(definition)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void rejectsInvalidTypeName() {
        ConjureDefinition definition = ConjureDefinition.builder()
                .version(1)
                .types(List.of(TypeDefinition.enum_(EnumDefinition.builder()
                        .typeName(TypeName.of("invalid name", "com.example.product"))
                        .values(List.of(
                                EnumValueDefinition.builder().value("RED").build()))
                        .build())))
                .build();

        assertThatThrownBy(() -> ConjureIrValidator.validate(definition)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void acceptsValidDefinition() {
        ConjureDefinition definition = ConjureDefinition.builder()
                .version(1)
                .types(List.of(TypeDefinition.enum_(EnumDefinition.builder()
                        .typeName(ENUM_NAME)
                        .values(List.of(
                                EnumValueDefinition.builder().value("RED").build(),
                                EnumValueDefinition.builder().value("GREEN").build()))
                        .build())))
                .build();

        assertThatCode(() -> ConjureIrValidator.validate(definition)).doesNotThrowAnyException();
    }

    @Test
    public void rejectsInvalidPackageName() {
        ConjureDefinition definition = ConjureDefinition.builder()
                .version(1)
                .types(List.of(TypeDefinition.enum_(EnumDefinition.builder()
                        .typeName(TypeName.of("Color", "Foo"))
                        .values(List.of(
                                EnumValueDefinition.builder().value("RED").build()))
                        .build())))
                .build();

        assertThatThrownBy(() -> ConjureIrValidator.validate(definition)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void rejectsInvalidFieldName() {
        ConjureDefinition definition = ConjureDefinition.builder()
                .version(1)
                .types(List.of(TypeDefinition.object(ObjectDefinition.builder()
                        .typeName(TypeName.of("Widget", "com.example.product"))
                        .fields(FieldDefinition.builder()
                                .fieldName(FieldName.of("not valid"))
                                .type(Type.primitive(PrimitiveType.STRING))
                                .build())
                        .build())))
                .build();

        assertThatThrownBy(() -> ConjureIrValidator.validate(definition)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void rejectsInvalidHttpPath() {
        ConjureDefinition definition = ConjureDefinition.builder()
                .version(1)
                .services(List.of(ServiceDefinition.builder()
                        .serviceName(TypeName.of("WidgetService", "com.example.product"))
                        .endpoints(EndpointDefinition.builder()
                                .endpointName(EndpointName.of("getWidget"))
                                .httpMethod(HttpMethod.GET)
                                .httpPath(HttpPath.of("widget"))
                                .build())
                        .build()))
                .build();

        assertThatThrownBy(() -> ConjureIrValidator.validate(definition)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void acceptsValidDefinitionWithObjectAndService() {
        ConjureDefinition definition = ConjureDefinition.builder()
                .version(1)
                .types(List.of(TypeDefinition.object(ObjectDefinition.builder()
                        .typeName(TypeName.of("Widget", "com.example.product"))
                        .fields(FieldDefinition.builder()
                                .fieldName(FieldName.of("displayName"))
                                .type(Type.primitive(PrimitiveType.STRING))
                                .build())
                        .build())))
                .services(List.of(ServiceDefinition.builder()
                        .serviceName(TypeName.of("WidgetService", "com.example.product"))
                        .endpoints(EndpointDefinition.builder()
                                .endpointName(EndpointName.of("getWidget"))
                                .httpMethod(HttpMethod.GET)
                                .httpPath(HttpPath.of("/widget"))
                                .build())
                        .build()))
                .build();

        assertThatCode(() -> ConjureIrValidator.validate(definition)).doesNotThrowAnyException();
    }

    @Test
    public void rejectsInvalidFieldNameInError() {
        ConjureDefinition definition = ConjureDefinition.builder()
                .version(1)
                .errors(List.of(ErrorDefinition.builder()
                        .errorName(TypeName.of("WidgetError", "com.example.product"))
                        .namespace(ErrorNamespace.of("Widget"))
                        .code(ErrorCode.INVALID_ARGUMENT)
                        .safeArgs(FieldDefinition.builder()
                                .fieldName(FieldName.of("not valid"))
                                .type(Type.primitive(PrimitiveType.STRING))
                                .build())
                        .build()))
                .build();

        assertThatThrownBy(() -> ConjureIrValidator.validate(definition)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void rejectsInvalidFieldNameInUnion() {
        ConjureDefinition definition = ConjureDefinition.builder()
                .version(1)
                .types(List.of(TypeDefinition.union(UnionDefinition.builder()
                        .typeName(TypeName.of("Widget", "com.example.product"))
                        .union(FieldDefinition.builder()
                                .fieldName(FieldName.of("not valid"))
                                .type(Type.primitive(PrimitiveType.STRING))
                                .build())
                        .build())))
                .build();

        assertThatThrownBy(() -> ConjureIrValidator.validate(definition)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void acceptsValidDefinitionWithEndpointArgument() {
        ConjureDefinition definition = ConjureDefinition.builder()
                .version(1)
                .services(List.of(ServiceDefinition.builder()
                        .serviceName(TypeName.of("WidgetService", "com.example.product"))
                        .endpoints(EndpointDefinition.builder()
                                .endpointName(EndpointName.of("getWidget"))
                                .httpMethod(HttpMethod.GET)
                                .httpPath(HttpPath.of("/widget"))
                                .args(ArgumentDefinition.builder()
                                        .argName(ArgumentName.of("widgetId"))
                                        .type(Type.primitive(PrimitiveType.STRING))
                                        .paramType(
                                                ParameterType.query(QueryParameterType.of(ParameterId.of("widgetId"))))
                                        .build())
                                .build())
                        .build()))
                .build();

        assertThatCode(() -> ConjureIrValidator.validate(definition)).doesNotThrowAnyException();
    }

    @Test
    public void validateWithSafetyDeclarationRequirementsDoesNotThrow() {
        ConjureDefinition definition = ConjureDefinition.builder()
                .version(1)
                .types(List.of(TypeDefinition.enum_(EnumDefinition.builder()
                        .typeName(ENUM_NAME)
                        .values(List.of(
                                EnumValueDefinition.builder().value("RED").build()))
                        .build())))
                .build();

        assertThatCode(() -> ConjureIrValidator.validate(definition, SafetyDeclarationRequirements.ALLOWED))
                .doesNotThrowAnyException();
    }
}
