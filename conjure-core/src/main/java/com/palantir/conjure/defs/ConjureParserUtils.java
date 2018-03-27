/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.services.EndpointDefinitionValidator;
import com.palantir.conjure.defs.services.HttpPathWrapper;
import com.palantir.conjure.defs.services.ServiceDefinitionValidator;
import com.palantir.conjure.defs.types.ConjureTypeParserVisitor;
import com.palantir.conjure.defs.types.TypeDefinitionParserVisitor;
import com.palantir.conjure.defs.types.complex.EnumTypeDefinitionValidator;
import com.palantir.conjure.defs.types.complex.EnumValueDefinitionValidator;
import com.palantir.conjure.defs.types.complex.ErrorTypeDefinitionValidator;
import com.palantir.conjure.defs.types.complex.ObjectTypeDefinitionValidator;
import com.palantir.conjure.defs.types.complex.UnionTypeDefinitionValidator;
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
import com.palantir.conjure.spec.ErrorCode;
import com.palantir.conjure.spec.ErrorDefinition;
import com.palantir.conjure.spec.ErrorNamespace;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.FieldName;
import com.palantir.conjure.spec.HeaderAuthType;
import com.palantir.conjure.spec.HeaderParameterType;
import com.palantir.conjure.spec.HttpMethod;
import com.palantir.conjure.spec.HttpPath;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility methods used to parse {@code com.palantir.conjure.parser} definitions into {@code com.palantir.conjure.defs}
 * definitions.
 */
public final class ConjureParserUtils {

    private ConjureParserUtils() {}

    public static String parseConjurePackage(com.palantir.conjure.parser.types.names.ConjurePackage parsed) {
        return parsed.name();
    }

    public static String parsePackageOrElseThrow(
            Optional<com.palantir.conjure.parser.types.names.ConjurePackage> conjurePackage,
            Optional<String> defaultPackage) {
        return conjurePackage
                .map(p -> p.name())
                .orElseGet(() -> defaultPackage.orElseThrow(() -> new IllegalArgumentException(
                        // TODO(rfink): Better errors: Can we provide context on where exactly no package was provided?
                        "Must provide default conjure package or "
                                + "explicit conjure package for every object and service")));
    }

    public static ErrorDefinition parseErrorType(
            TypeName name,
            com.palantir.conjure.parser.types.complex.ErrorTypeDefinition def,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        ErrorDefinition errorType = ErrorDefinition.builder()
                .errorName(name)
                .namespace(ErrorNamespace.of(def.namespace().name()))
                .code(ErrorCode.valueOf(def.code().name()))
                .safeArgs(parseField(def.safeArgs(), typeResolver))
                .unsafeArgs(parseField(def.unsafeArgs(), typeResolver))
                .docs(def.docs().map(Documentation::of))
                .build();

        ErrorTypeDefinitionValidator.validateAll(errorType);
        return errorType;
    }

    public static TypeDefinition parseEnumType(
            TypeName name,
            com.palantir.conjure.parser.types.complex.EnumTypeDefinition def) {

        EnumDefinition enumType =  EnumDefinition.builder()
                .typeName(name)
                .values(def.values().stream().map(ConjureParserUtils::parseEnumValue).collect(Collectors.toList()))
                .docs(def.docs().map(Documentation::of))
                .build();

        EnumTypeDefinitionValidator.validateAll(enumType);
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

        UnionTypeDefinitionValidator.validateAll(unionType);
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

        ObjectTypeDefinitionValidator.validateAll(objectType);
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
                .build());
    }

    public static Type parsePrimitiveType(
            com.palantir.conjure.parser.types.primitive.PrimitiveType primitiveType) {
        return Type.primitive(PrimitiveType.valueOf(primitiveType.name()));
    }

    public static TypeName createTypeName(
            String name,
            com.palantir.conjure.parser.types.BaseObjectTypeDefinition def,
            Optional<String> defaultPackage) {
        return TypeName.of(name, parsePackageOrElseThrow(def.conjurePackage(), defaultPackage));
    }

    static ConjureDefinition parseConjureDef(Collection<com.palantir.conjure.parser.ConjureDefinition> parsedDefs) {
        ImmutableList.Builder<ServiceDefinition> servicesBuilder = ImmutableList.builder();
        ImmutableList.Builder<ErrorDefinition> errorsBuilder = ImmutableList.builder();
        ImmutableList.Builder<TypeDefinition> typesBuilder = ImmutableList.builder();


        parsedDefs.forEach(parsed -> {
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver =
                    new ConjureTypeParserVisitor.ByParsedRepresentationTypeNameResolver(parsed.types());

            parsed.services().forEach((serviceName, service) -> {
                servicesBuilder.add(parseService(
                        service,
                        TypeName.of(serviceName.name(), parseConjurePackage(service.conjurePackage())),
                        typeResolver));
            });

            typesBuilder.addAll(parseObjects(parsed.types(), typeResolver));
            errorsBuilder.addAll(parseErrors(parsed.types().definitions(), typeResolver));
        });

        ConjureDefinition definition = ConjureDefinition.builder()
                .version(Conjure.SUPPORTED_IR_VERSION)
                .types(typesBuilder.build())
                .errors(errorsBuilder.build())
                .services(servicesBuilder.build())
                .build();

        ConjureDefinitionValidator.validateAll(definition);
        return definition;
    }

    static ServiceDefinition parseService(
            com.palantir.conjure.parser.services.ServiceDefinition parsed,
            TypeName serviceName,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        List<EndpointDefinition> endpoints = new ArrayList<>();
        parsed.endpoints().forEach((name, def) -> endpoints.add(
                ConjureParserUtils.parseEndpoint(
                        name, def, parsed.basePath(), parseAuthType(parsed.defaultAuth()), typeResolver)));
        ServiceDefinition service = ServiceDefinition.builder()
                .serviceName(serviceName)
                .docs(parsed.docs().map(Documentation::of))
                .addAllEndpoints(endpoints)
                .build();

        ServiceDefinitionValidator.validateAll(service);
        return service;
    }

    static List<TypeDefinition> parseObjects(
            com.palantir.conjure.parser.types.TypesDefinition parsed,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        Optional<String> defaultPackage =
                parsed.definitions().defaultConjurePackage().map(p -> p.name());

        ImmutableList.Builder<TypeDefinition> objectsBuilder = ImmutableList.builder();
        objectsBuilder.addAll(
                parsed.definitions().objects().entrySet().stream()
                        .map(entry -> entry.getValue().visit(
                                new TypeDefinitionParserVisitor(entry.getKey().name(), defaultPackage, typeResolver)))
                .collect(Collectors.toList()));
        return objectsBuilder.build();
    }

    static List<ErrorDefinition> parseErrors(
            com.palantir.conjure.parser.types.ObjectsDefinition defs,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        Optional<String> defaultPackage = defs.defaultConjurePackage().map(p -> p.name());
        ImmutableList.Builder<ErrorDefinition> errorsBuidler = ImmutableList.builder();
        errorsBuidler.addAll(defs.errors().entrySet().stream().map(entry -> {
            TypeName typeName = TypeName.of(
                    entry.getKey().name(), parsePackageOrElseThrow(entry.getValue().conjurePackage(), defaultPackage));
            return parseErrorType(typeName, entry.getValue(), typeResolver);
        }).collect(Collectors.toList()));
        return errorsBuidler.build();
    }

    private static EnumValueDefinition parseEnumValue(
            com.palantir.conjure.parser.types.complex.EnumValueDefinition def) {
        EnumValueDefinition enumValue = EnumValueDefinition.builder()
                .value(def.value())
                .docs(def.docs().map(Documentation::of))
                .build();

        EnumValueDefinitionValidator.validateAll(enumValue);
        return enumValue;
    }

    private static List<TypeDefinition> parseAllTypes(
            com.palantir.conjure.parser.types.TypesDefinition parsed,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {

        ImmutableList.Builder<TypeDefinition> typesBuilder = ImmutableList.builder();
        typesBuilder.addAll(parseObjects(parsed, typeResolver));

        for (com.palantir.conjure.parser.types.reference.ConjureImports imported : parsed.conjureImports().values()) {
            // Since we don't support transitive imports, the type resolver for the imported types consist of
            // its direct objects and external imports only.
            ConjureTypeParserVisitor.ReferenceTypeResolver importResolver =
                    new ConjureTypeParserVisitor.ByParsedRepresentationTypeNameResolver(imported.conjure().types());
            List<TypeDefinition> importedObjects = parseObjects(imported.conjure().types(), importResolver);

            typesBuilder.addAll(importedObjects);
        }
        return typesBuilder.build();
    }

    // similar to parseAllTypes but for specific ErrorTypeDefinitions
    private static List<ErrorDefinition> parseAllErrors(
            com.palantir.conjure.parser.types.TypesDefinition parsed,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {

        // Collect all imported object.
        ImmutableList.Builder<ErrorDefinition> errorsBuilder = ImmutableList.builder();
        errorsBuilder.addAll(parseErrors(parsed.definitions(), typeResolver));

        for (com.palantir.conjure.parser.types.reference.ConjureImports imported : parsed.conjureImports().values()) {
            // Since we don't support transitive imports, the type resolver for the imported types consist of
            // its direct types and external imports only.
            ConjureTypeParserVisitor.ReferenceTypeResolver importResolver =
                    new ConjureTypeParserVisitor.ByParsedRepresentationTypeNameResolver(imported.conjure().types());
            List<ErrorDefinition> importedErrors =
                    parseErrors(imported.conjure().types().definitions(), importResolver);
            errorsBuilder.addAll(importedErrors);
        }

        return errorsBuilder.build();
    }

    static List<FieldDefinition> parseField(
            Map<com.palantir.conjure.parser.types.names.FieldName,
                    com.palantir.conjure.parser.types.complex.FieldDefinition> def,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {

        return def.entrySet().stream().map(entry ->
                FieldDefinition.builder()
                    .fieldName(FieldName.of(entry.getKey().name()))
                    .type(entry.getValue().type().visit(new ConjureTypeParserVisitor(typeResolver)))
                    .docs(entry.getValue().docs().map(Documentation::of)).build())
                .collect(Collectors.toList());
    }

    private static EndpointDefinition parseEndpoint(
            String name,
            com.palantir.conjure.parser.services.EndpointDefinition def,
            com.palantir.conjure.parser.services.PathDefinition basePath,
            Optional<AuthType> defaultAuth,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {

        HttpPath httpPath = HttpPathWrapper.httpPath(basePath.resolve(def.http().path()).toString());
        EndpointDefinition endpoint = EndpointDefinition.builder()
                .endpointName(EndpointName.of(name))
                .httpMethod(HttpMethod.valueOf(def.http().method()))
                .httpPath(httpPath)
                .auth(def.auth().map(ConjureParserUtils::parseAuthType).orElse(defaultAuth))
                .args(parseArgs(def.args(), httpPath, typeResolver))
                .markers(parseMarkers(def.markers(), typeResolver))
                .returns(def.returns().map(t -> t.visit(new ConjureTypeParserVisitor(typeResolver))))
                .docs(def.docs().map(Documentation::of))
                .deprecated(def.deprecated().map(Documentation::of))
                .build();

        EndpointDefinitionValidator.validateAll(endpoint);
        return endpoint;
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
                throw new IllegalArgumentException("Unrecognized auth type.");
        }
    }

    private static List<ArgumentDefinition> parseArgs(
            Map<com.palantir.conjure.parser.services.ParameterName,
                    com.palantir.conjure.parser.services.ArgumentDefinition> args,
            HttpPath httpPath,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        ImmutableList.Builder<ArgumentDefinition> resultBuilder = ImmutableList.builder();
        for (Map.Entry<com.palantir.conjure.parser.services.ParameterName,
                com.palantir.conjure.parser.services.ArgumentDefinition> entry : args.entrySet()) {
            com.palantir.conjure.parser.services.ArgumentDefinition original = entry.getValue();
            ArgumentName argName = ArgumentName.of(entry.getKey().name());
            ParameterType paramType = parseParameterType(original, argName, httpPath);
            ArgumentDefinition.Builder builder = ArgumentDefinition.builder()
                    .argName(argName)
                    .type(original.type().visit(new ConjureTypeParserVisitor(typeResolver)))
                    .paramType(paramType)
                    .docs(original.docs().map(Documentation::of))
                    .markers(parseMarkers(original.markers(), typeResolver));
            resultBuilder.add(builder.build());
        }
        return resultBuilder.build();
    }

    private static ParameterType parseParameterType(
            com.palantir.conjure.parser.services.ArgumentDefinition argumentDef,
            ArgumentName argName,
            HttpPath httpPath) {

        Set<ArgumentName> args = HttpPathWrapper.pathArgs(httpPath.get());
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
                String headerParamId = argumentDef.paramId().map(id -> id.name()).orElse(argName.get());
                return ParameterType.header(HeaderParameterType.of(ParameterId.of(headerParamId)));
            case PATH:
                return ParameterType.path(PathParameterType.of());
            case BODY:
                return ParameterType.body(BodyParameterType.of());
            case QUERY:
                String queryParamId = argumentDef.paramId().map(id -> id.name()).orElse(argName.get());
                return ParameterType.query(QueryParameterType.of(ParameterId.of(queryParamId)));
            default:
                throw new IllegalArgumentException("Unknown parameter type: " + argumentDef.paramType());
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
