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

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.ConjureTypeParserVisitor.ReferenceTypeResolver;
import com.palantir.conjure.defs.validator.ConjureDefinitionValidator;
import com.palantir.conjure.defs.validator.EndpointDefinitionValidator;
import com.palantir.conjure.defs.validator.EnumDefinitionValidator;
import com.palantir.conjure.defs.validator.EnumValueDefinitionValidator;
import com.palantir.conjure.defs.validator.ErrorDefinitionValidator;
import com.palantir.conjure.defs.validator.FieldDefinitionValidator;
import com.palantir.conjure.defs.validator.FieldNameValidator;
import com.palantir.conjure.defs.validator.HttpPathValidator;
import com.palantir.conjure.defs.validator.ObjectDefinitionValidator;
import com.palantir.conjure.defs.validator.PackageValidator;
import com.palantir.conjure.defs.validator.ServiceDefinitionValidator;
import com.palantir.conjure.defs.validator.TypeNameValidator;
import com.palantir.conjure.defs.validator.UnionDefinitionValidator;
import com.palantir.conjure.exceptions.ConjureIllegalArgumentException;
import com.palantir.conjure.exceptions.ConjureRuntimeException;
import com.palantir.conjure.parser.AnnotatedConjureSourceFile;
import com.palantir.conjure.parser.ConjureSourceFile;
import com.palantir.conjure.parser.LogSafetyDefinition;
import com.palantir.conjure.parser.services.ParameterName;
import com.palantir.conjure.parser.services.PathString;
import com.palantir.conjure.parser.types.NamedTypesDefinition;
import com.palantir.conjure.parser.types.names.ConjurePackage;
import com.palantir.conjure.parser.types.names.Namespace;
import com.palantir.conjure.parser.types.reference.ConjureImports;
import com.palantir.conjure.spec.AliasDefinition;
import com.palantir.conjure.spec.ArgumentDefinition;
import com.palantir.conjure.spec.ArgumentName;
import com.palantir.conjure.spec.AuthType;
import com.palantir.conjure.spec.BodyParameterType;
import com.palantir.conjure.spec.ConjureDefinition;
import com.palantir.conjure.spec.CookieAuthType;
import com.palantir.conjure.spec.Documentation;
import com.palantir.conjure.spec.EndpointDefinition;
import com.palantir.conjure.spec.EndpointName;
import com.palantir.conjure.spec.EnumDefinition;
import com.palantir.conjure.spec.EnumValueDefinition;
import com.palantir.conjure.spec.ErrorDefinition;
import com.palantir.conjure.spec.ErrorNamespace;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.FieldName;
import com.palantir.conjure.spec.HeaderAuthType;
import com.palantir.conjure.spec.HeaderParameterType;
import com.palantir.conjure.spec.HttpMethod;
import com.palantir.conjure.spec.HttpPath;
import com.palantir.conjure.spec.LogSafety;
import com.palantir.conjure.spec.ObjectDefinition;
import com.palantir.conjure.spec.ParameterId;
import com.palantir.conjure.spec.ParameterType;
import com.palantir.conjure.spec.PathParameterType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.QueryParameterType;
import com.palantir.conjure.spec.ServiceDefinition;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.spec.TypeName;
import com.palantir.conjure.spec.UnionDefinition;
import com.palantir.conjure.visitor.DealiasingTypeVisitor;
import com.palantir.conjure.visitor.TypeDefinitionVisitor;
import com.palantir.logsafe.Preconditions;
import com.palantir.logsafe.UnsafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import com.palantir.logsafe.exceptions.SafeIllegalStateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility methods used to parse {@code com.palantir.conjure.parser} definitions into {@code com.palantir.conjure.defs}
 * definitions.
 */
public final class ConjureParserUtils {

    private ConjureParserUtils() {}

    public static String parseConjurePackage(ConjurePackage parsed) {
        return parsed.name();
    }

    public static String parsePackageOrElseThrow(
            Optional<ConjurePackage> conjurePackage, Optional<String> defaultPackage) {
        String packageName = conjurePackage
                .map(ConjurePackage::name)
                .orElseGet(() -> defaultPackage.orElseThrow(() -> new SafeIllegalArgumentException(
                        // TODO(rfink): Better errors: Can we provide context on where exactly no package was provided?
                        "Must provide default conjure package or "
                                + "explicit conjure package for every object and service")));
        PackageValidator.validate(packageName);
        return packageName;
    }

    public static ErrorDefinition parseErrorType(
            TypeName name,
            com.palantir.conjure.parser.types.complex.ErrorTypeDefinition def,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        ErrorDefinition errorType = ErrorDefinition.builder()
                .errorName(name)
                .namespace(ErrorNamespace.of(def.namespace().name()))
                .code(def.code().asSpecErrorCode())
                .safeArgs(parseField(def.safeArgs(), typeResolver))
                .unsafeArgs(parseField(def.unsafeArgs(), typeResolver))
                .docs(def.docs().map(Documentation::of))
                .build();

        ErrorDefinitionValidator.validate(errorType);
        return errorType;
    }

    public static TypeDefinition parseEnumType(
            TypeName name, com.palantir.conjure.parser.types.complex.EnumTypeDefinition def) {

        EnumDefinition enumType = EnumDefinition.builder()
                .typeName(name)
                .values(def.values().stream()
                        .map(ConjureParserUtils::parseEnumValue)
                        .collect(Collectors.toList()))
                .docs(def.docs().map(Documentation::of))
                .build();

        EnumDefinitionValidator.validateAll(enumType);
        return TypeDefinition.enum_(enumType);
    }

    public static TypeDefinition parseUnionType(
            TypeName name,
            com.palantir.conjure.parser.types.complex.UnionTypeDefinition def,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        UnionDefinition unionType = UnionDefinition.builder()
                .typeName(name)
                .union(parseField(def.union(), typeResolver))
                .docs(def.docs().map(Documentation::of))
                .build();

        UnionDefinitionValidator.validateAll(unionType);
        return TypeDefinition.union(unionType);
    }

    public static TypeDefinition parseObjectType(
            TypeName name,
            com.palantir.conjure.parser.types.complex.ObjectTypeDefinition def,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        ObjectDefinition objectType = ObjectDefinition.builder()
                .typeName(name)
                .fields(parseField(def.fields(), typeResolver))
                .docs(def.docs().map(Documentation::of))
                .build();

        ObjectDefinitionValidator.validate(objectType);
        return TypeDefinition.object(objectType);
    }

    public static TypeDefinition parseAliasType(
            TypeName name,
            com.palantir.conjure.parser.types.reference.AliasTypeDefinition def,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        return TypeDefinition.alias(AliasDefinition.builder()
                .typeName(name)
                .alias(def.alias().visit(new ConjureTypeParserVisitor(typeResolver)))
                .docs(def.docs().map(Documentation::of))
                .safety(def.safety().map(ConjureParserUtils::parseLogSafety))
                .build());
    }

    public static Type parsePrimitiveType(com.palantir.conjure.parser.types.primitive.PrimitiveType primitiveType) {
        return Type.primitive(PrimitiveType.valueOf(primitiveType.name()));
    }

    public static TypeName createTypeName(
            String name,
            com.palantir.conjure.parser.types.BaseObjectTypeDefinition def,
            Optional<String> defaultPackage) {
        TypeName type = TypeName.of(name, parsePackageOrElseThrow(def.conjurePackage(), defaultPackage));
        TypeNameValidator.validate(type);
        return type;
    }

    @Deprecated
    static ConjureDefinition parseConjureDef(Collection<AnnotatedConjureSourceFile> annotatedParsedDefs) {
        return parseConjureDef(annotatedParsedDefs.stream()
                .collect(Collectors.toMap(source -> source.sourceFile().getAbsolutePath(), Function.identity())));
    }

    @Deprecated
    static ConjureDefinition parseConjureDef(Map<String, AnnotatedConjureSourceFile> annotatedParsedDefs) {
        return parseConjureDef(annotatedParsedDefs, SafetyDeclarationRequirements.ALLOWED);
    }

    static ConjureDefinition parseConjureDef(
            Map<String, AnnotatedConjureSourceFile> annotatedParsedDefs,
            SafetyDeclarationRequirements safetyDeclarations) {
        ImmutableList.Builder<ServiceDefinition> servicesBuilder = ImmutableList.builder();
        ImmutableList.Builder<ErrorDefinition> errorsBuilder = ImmutableList.builder();
        ImmutableList.Builder<TypeDefinition> typesBuilder = ImmutableList.builder();

        annotatedParsedDefs.values().forEach(annotatedParsed -> {
            ConjureSourceFile parsed = annotatedParsed.conjureSourceFile();

            try {
                ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver =
                        new ConjureTypeParserVisitor.ByParsedRepresentationTypeNameResolver(
                                parsed.types(), annotatedParsed.importProviders(), annotatedParsedDefs);

                // Resolve objects first, so we can use them in service validations
                Map<TypeName, TypeDefinition> objects = parseObjects(parsed.types(), typeResolver);
                Map<TypeName, TypeDefinition> importedObjects =
                        parseImportObjects(parsed.types().conjureImports(), annotatedParsedDefs);
                Map<TypeName, TypeDefinition> allObjects = new HashMap<>();
                allObjects.putAll(objects);
                allObjects.putAll(importedObjects);

                DealiasingTypeVisitor dealiasingVisitor = new DealiasingTypeVisitor(allObjects);

                parsed.services().forEach((serviceName, service) -> {
                    servicesBuilder.add(parseService(
                            service,
                            TypeName.of(serviceName.name(), parseConjurePackage(service.conjurePackage())),
                            typeResolver,
                            dealiasingVisitor));
                });

                typesBuilder.addAll(objects.values());
                errorsBuilder.addAll(parseErrors(parsed.types().definitions(), typeResolver));
            } catch (RuntimeException e) {
                throw new ConjureRuntimeException(
                        String.format("Encountered error trying to parse file '%s'", annotatedParsed.sourceFile()), e);
            }
        });

        ConjureDefinition definition = ConjureDefinition.builder()
                .version(Conjure.SUPPORTED_IR_VERSION)
                .types(typesBuilder.build())
                .errors(errorsBuilder.build())
                .services(servicesBuilder.build())
                .build();

        ConjureDefinitionValidator.validateAll(definition, safetyDeclarations);
        return definition;
    }

    /*
     * Recursively resolve all imported types
     */
    private static Map<TypeName, TypeDefinition> parseImportObjects(
            Map<Namespace, ConjureImports> conjureImports, Map<String, AnnotatedConjureSourceFile> externalTypes) {
        return innerParseImportObjects(conjureImports, externalTypes, new HashSet<>());
    }

    private static Map<TypeName, TypeDefinition> innerParseImportObjects(
            Map<Namespace, ConjureImports> conjureImports,
            Map<String, AnnotatedConjureSourceFile> externalTypes,
            Set<String> loadedFiles) {
        Map<TypeName, TypeDefinition> allDefinitions = new HashMap<>();
        conjureImports.values().forEach(conjureImport -> {
            String pathKey = conjureImport
                    .absoluteFile()
                    .orElseThrow(() ->
                            new SafeIllegalStateException("Absolute file MUST be resolved as part of parsing stage"))
                    .getAbsolutePath();

            // These structures are potentially recursive; load in any given conjure file once
            if (loadedFiles.contains(pathKey)) {
                return;
            }
            loadedFiles.add(pathKey);

            AnnotatedConjureSourceFile annotatedConjureSourceFile = externalTypes.get(pathKey);

            Preconditions.checkNotNull(
                    annotatedConjureSourceFile, "Couldn't find import", UnsafeArg.of("file", conjureImport.file()));

            ConjureSourceFile conjureDef = annotatedConjureSourceFile.conjureSourceFile();
            Map<Namespace, String> importProviders = annotatedConjureSourceFile.importProviders();
            ReferenceTypeResolver importTypeResolver =
                    new ConjureTypeParserVisitor.ByParsedRepresentationTypeNameResolver(
                            conjureDef.types(), importProviders, externalTypes);
            allDefinitions.putAll(parseObjects(conjureDef.types(), importTypeResolver));
            allDefinitions.putAll(
                    innerParseImportObjects(conjureDef.types().conjureImports(), externalTypes, loadedFiles));
        });

        return allDefinitions;
    }

    static ServiceDefinition parseService(
            com.palantir.conjure.parser.services.ServiceDefinition parsed,
            TypeName serviceName,
            ReferenceTypeResolver typeResolver,
            DealiasingTypeVisitor dealiasingVisitor) {
        List<EndpointDefinition> endpoints = new ArrayList<>();
        parsed.endpoints()
                .forEach((name, def) -> endpoints.add(ConjureParserUtils.parseEndpoint(
                        name,
                        def,
                        parsed.basePath(),
                        parseAuthType(parsed.defaultAuth()),
                        typeResolver,
                        dealiasingVisitor)));
        ServiceDefinition service = ServiceDefinition.builder()
                .serviceName(serviceName)
                .docs(parsed.docs().map(Documentation::of))
                .addAllEndpoints(endpoints)
                .build();

        ServiceDefinitionValidator.validateAll(service);
        return service;
    }

    static Map<TypeName, TypeDefinition> parseObjects(
            com.palantir.conjure.parser.types.TypesDefinition parsed,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        Optional<String> defaultPackage =
                parsed.definitions().defaultConjurePackage().map(ConjurePackage::name);

        // no need to use validator here since TypeDefinitionParserVisitor calls each TypeDefinition parser that
        // validates its type.
        return parsed.definitions().objects().entrySet().stream()
                .map(entry -> entry.getValue()
                        .visit(new TypeDefinitionParserVisitor(entry.getKey().name(), defaultPackage, typeResolver)))
                .collect(Collectors.toMap(td -> td.accept(TypeDefinitionVisitor.TYPE_NAME), td -> td));
    }

    static List<ErrorDefinition> parseErrors(
            NamedTypesDefinition defs, ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        Optional<String> defaultPackage = defs.defaultConjurePackage().map(ConjurePackage::name);
        ImmutableList.Builder<ErrorDefinition> errorsBuidler = ImmutableList.builder();
        errorsBuidler.addAll(defs.errors().entrySet().stream()
                .map(entry -> {
                    TypeName typeName = TypeName.of(
                            entry.getKey().name(),
                            parsePackageOrElseThrow(entry.getValue().conjurePackage(), defaultPackage));
                    return parseErrorType(typeName, entry.getValue(), typeResolver);
                })
                .collect(Collectors.toList()));
        return errorsBuidler.build();
    }

    private static EnumValueDefinition parseEnumValue(
            com.palantir.conjure.parser.types.complex.EnumValueDefinition def) {
        EnumValueDefinition enumValue = EnumValueDefinition.builder()
                .value(def.value())
                .docs(def.docs().map(Documentation::of))
                .deprecated(def.deprecated().map(Documentation::of))
                .build();

        EnumValueDefinitionValidator.validateAll(enumValue);
        return enumValue;
    }

    static List<FieldDefinition> parseField(
            Map<
                            com.palantir.conjure.parser.types.names.FieldName,
                            com.palantir.conjure.parser.types.complex.FieldDefinition>
                    def,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        return def.entrySet().stream()
                .map(entry -> {
                    FieldDefinition fieldDefinition = FieldDefinition.builder()
                            .fieldName(parseFieldName(entry.getKey()))
                            .type(entry.getValue().type().visit(new ConjureTypeParserVisitor(typeResolver)))
                            .docs(entry.getValue().docs().map(Documentation::of))
                            .deprecated(entry.getValue().deprecated().map(Documentation::of))
                            .safety(entry.getValue().safety().map(ConjureParserUtils::parseLogSafety))
                            .build();
                    FieldDefinitionValidator.validate(fieldDefinition);
                    return fieldDefinition;
                })
                .collect(Collectors.toList());
    }

    public static LogSafety parseLogSafety(LogSafetyDefinition def) {
        switch (def) {
            case SAFE:
                return LogSafety.SAFE;
            case UNSAFE:
                return LogSafety.UNSAFE;
            case DO_NOT_LOG:
                return LogSafety.DO_NOT_LOG;
        }
        throw new ConjureIllegalArgumentException("Unknown log safety type: " + def);
    }

    private static FieldName parseFieldName(com.palantir.conjure.parser.types.names.FieldName parserFieldName) {
        FieldName fieldName = FieldName.of(parserFieldName.name());
        FieldNameValidator.validate(fieldName);

        return fieldName;
    }

    private static EndpointDefinition parseEndpoint(
            String name,
            com.palantir.conjure.parser.services.EndpointDefinition def,
            PathString basePath,
            Optional<AuthType> defaultAuth,
            ReferenceTypeResolver typeResolver,
            DealiasingTypeVisitor dealiasingVisitor) {

        HttpPath httpPath = parseHttpPath(def, basePath);
        EndpointDefinition endpoint = EndpointDefinition.builder()
                .endpointName(EndpointName.of(name))
                .httpMethod(HttpMethod.valueOf(def.http().method()))
                .httpPath(httpPath)
                .auth(def.auth().map(ConjureParserUtils::parseAuthType).orElse(defaultAuth))
                .args(parseArgs(def.args(), httpPath, typeResolver))
                .tags(def.tags().stream()
                        .peek(tag -> Preconditions.checkArgument(!tag.isEmpty(), "tag must not be empty"))
                        .collect(Collectors.toSet()))
                .markers(parseMarkers(def.markers(), typeResolver))
                .returns(def.returns().map(t -> t.visit(new ConjureTypeParserVisitor(typeResolver))))
                .docs(def.docs().map(Documentation::of))
                .deprecated(def.deprecated().map(Documentation::of))
                .build();

        EndpointDefinitionValidator.validateAll(endpoint, dealiasingVisitor);
        return endpoint;
    }

    private static HttpPath parseHttpPath(
            com.palantir.conjure.parser.services.EndpointDefinition def, PathString basePath) {
        HttpPath httpPath = HttpPath.of(basePath.resolve(def.http().path()).toString());
        HttpPathValidator.validate(httpPath);
        return httpPath;
    }

    private static Optional<AuthType> parseAuthType(
            com.palantir.conjure.parser.services.AuthDefinition authDefinition) {

        switch (authDefinition.type()) {
            case HEADER:
                return Optional.of(AuthType.header(HeaderAuthType.of()));
            case COOKIE:
                return Optional.of(AuthType.cookie(CookieAuthType.of(authDefinition.id())));
            case NONE:
                return Optional.empty();
            default:
                throw new ConjureIllegalArgumentException("Unrecognized auth type.");
        }
    }

    private static List<ArgumentDefinition> parseArgs(
            Map<ParameterName, com.palantir.conjure.parser.services.ArgumentDefinition> args,
            HttpPath httpPath,
            ReferenceTypeResolver typeResolver) {
        ImmutableList.Builder<ArgumentDefinition> resultBuilder = ImmutableList.builder();
        for (Map.Entry<ParameterName, com.palantir.conjure.parser.services.ArgumentDefinition> entry :
                args.entrySet()) {
            com.palantir.conjure.parser.services.ArgumentDefinition original = entry.getValue();
            ArgumentName argName = ArgumentName.of(entry.getKey().name());
            ParameterType paramType = parseParameterType(original, argName, httpPath);
            ArgumentDefinition.Builder builder = ArgumentDefinition.builder()
                    .argName(argName)
                    .type(original.type().visit(new ConjureTypeParserVisitor(typeResolver)))
                    .paramType(paramType)
                    .docs(original.docs().map(Documentation::of))
                    .safety(original.safety().map(ConjureParserUtils::parseLogSafety))
                    .markers(parseMarkers(original.markers(), typeResolver))
                    .tags(original.tags().stream()
                            .peek(tag -> Preconditions.checkArgument(!tag.isEmpty(), "tag must not be empty"))
                            .collect(Collectors.toSet()));
            resultBuilder.add(builder.build());
        }
        return resultBuilder.build();
    }

    private static ParameterType parseParameterType(
            com.palantir.conjure.parser.services.ArgumentDefinition argumentDef,
            ArgumentName argName,
            HttpPath httpPath) {

        Set<ArgumentName> args = HttpPathValidator.pathArgs(httpPath.get());
        switch (argumentDef.paramType()) {
            case AUTO:
                // AUTO type
                if (args.contains(argName)) {
                    // argument exists in request line -- it is a path arg
                    return ParameterType.path(PathParameterType.of());
                } else {
                    // argument does not exist in request line -- it is a body arg
                    return ParameterType.body(BodyParameterType.of());
                }
            case HEADER:
                String headerParamId =
                        argumentDef.paramId().map(ParameterName::name).orElseGet(argName::get);
                return ParameterType.header(HeaderParameterType.of(ParameterId.of(headerParamId)));
            case PATH:
                return ParameterType.path(PathParameterType.of());
            case BODY:
                return ParameterType.body(BodyParameterType.of());
            case QUERY:
                String queryParamId =
                        argumentDef.paramId().map(ParameterName::name).orElseGet(argName::get);
                return ParameterType.query(QueryParameterType.of(ParameterId.of(queryParamId)));
            default:
                throw new ConjureIllegalArgumentException("Unknown parameter type: " + argumentDef.paramType());
        }
    }

    private static Set<Type> parseMarkers(
            Set<com.palantir.conjure.parser.types.ConjureType> markers,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        return markers.stream()
                .map(m -> m.visit(new ConjureTypeParserVisitor(typeResolver)))
                .collect(Collectors.toSet());
    }
}
