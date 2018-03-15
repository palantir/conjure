/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.services.ArgumentDefinition;
import com.palantir.conjure.defs.services.ArgumentName;
import com.palantir.conjure.defs.services.AuthType;
import com.palantir.conjure.defs.services.CookieAuthType;
import com.palantir.conjure.defs.services.EndpointDefinition;
import com.palantir.conjure.defs.services.EndpointName;
import com.palantir.conjure.defs.services.HeaderAuthType;
import com.palantir.conjure.defs.services.ParameterId;
import com.palantir.conjure.defs.services.RequestLineDefinition;
import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.defs.types.ConjureTypeParserVisitor;
import com.palantir.conjure.defs.types.Documentation;
import com.palantir.conjure.defs.types.Type;
import com.palantir.conjure.defs.types.TypeDefinition;
import com.palantir.conjure.defs.types.TypeDefinitionParserVisitor;
import com.palantir.conjure.defs.types.complex.AliasTypeDefinition;
import com.palantir.conjure.defs.types.complex.EnumTypeDefinition;
import com.palantir.conjure.defs.types.complex.EnumValueDefinition;
import com.palantir.conjure.defs.types.complex.ErrorTypeDefinition;
import com.palantir.conjure.defs.types.complex.FieldDefinition;
import com.palantir.conjure.defs.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.complex.UnionTypeDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.ErrorCode;
import com.palantir.conjure.defs.types.names.ErrorNamespace;
import com.palantir.conjure.defs.types.names.FieldName;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import java.util.ArrayList;
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

    public static ConjurePackage parseConjurePackage(com.palantir.conjure.parser.types.names.ConjurePackage parsed) {
        return ConjurePackage.of(parsed.name());
    }

    public static ConjurePackage parsePackageOrElseThrow(
            Optional<com.palantir.conjure.parser.types.names.ConjurePackage> conjurePackage,
            Optional<ConjurePackage> defaultPackage) {
        return conjurePackage
                .map(p -> ConjurePackage.of(p.name()))
                .orElseGet(() -> defaultPackage.orElseThrow(() -> new IllegalArgumentException(
                        // TODO(rfink): Better errors: Can we provide context on where exactly no package was provided?
                        "Must provide default conjure package or "
                                + "explicit conjure package for every object and service")));
    }

    public static ErrorTypeDefinition parseErrorType(
            TypeName name,
            com.palantir.conjure.parser.types.complex.ErrorTypeDefinition def,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        return ErrorTypeDefinition.builder()
                .typeName(name)
                .namespace(ErrorNamespace.of(def.namespace().name()))
                .code(ErrorCode.of(def.code().name()))
                .safeArgs(parseField(def.safeArgs(), typeResolver))
                .unsafeArgs(parseField(def.unsafeArgs(), typeResolver))
                .docs(def.docs().map(Documentation::of))
                .build();
    }

    public static TypeDefinition parseEnumType(
            TypeName name,
            com.palantir.conjure.parser.types.complex.EnumTypeDefinition def) {
        return EnumTypeDefinition.builder()
                .typeName(name)
                .values(def.values().stream().map(ConjureParserUtils::parseEnumValue).collect(Collectors.toList()))
                .docs(def.docs().map(Documentation::of))
                .build();
    }

    public static TypeDefinition parseUnionType(
            TypeName name,
            com.palantir.conjure.parser.types.complex.UnionTypeDefinition def,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        return UnionTypeDefinition.builder()
                .typeName(name)
                .union(parseField(def.union(), typeResolver))
                .docs(def.docs().map(Documentation::of))
                .build();
    }

    public static TypeDefinition parseObjectType(
            TypeName name,
            com.palantir.conjure.parser.types.complex.ObjectTypeDefinition def,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        return ObjectTypeDefinition.builder()
                .typeName(name)
                .fields(parseField(def.fields(), typeResolver))
                .docs(def.docs().map(Documentation::of))
                .build();
    }

    public static TypeDefinition parseAliasType(
            TypeName name,
            com.palantir.conjure.parser.types.reference.AliasTypeDefinition def,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        return AliasTypeDefinition.builder()
                .typeName(name)
                .alias(def.alias().visit(new ConjureTypeParserVisitor(typeResolver)))
                .docs(def.docs().map(Documentation::of))
                .build();
    }

    public static PrimitiveType parsePrimitiveType(
            com.palantir.conjure.parser.types.primitive.PrimitiveType primitiveType) {
        return PrimitiveType.valueOf(primitiveType.name());
    }

    public static TypeName createTypeName(
            String name,
            com.palantir.conjure.parser.types.BaseObjectTypeDefinition def,
            Optional<ConjurePackage> defaultPackage) {
        return TypeName.of(name, parsePackageOrElseThrow(def.conjurePackage(), defaultPackage));
    }

    static ConjureDefinition parseConjureDef(com.palantir.conjure.parser.ConjureDefinition parsed) {
        List<ServiceDefinition> services = new ArrayList<>();
        ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver =
                new ConjureTypeParserVisitor.ByParsedRepresentationTypeNameResolver(parsed.types());
        parsed.services().forEach((serviceName, service) -> {
            services.add(parseSevice(
                    service,
                    TypeName.of(serviceName.name(), parseConjurePackage(service.conjurePackage())),
                    typeResolver));
        });

        return ConjureDefinition.builder()
                .types(parseAllTypes(parsed.types(), typeResolver))
                .errors(parseAllErrors(parsed.types(), typeResolver))
                .services(services)
                .build();
    }

    static ServiceDefinition parseSevice(
            com.palantir.conjure.parser.services.ServiceDefinition parsed,
            TypeName serviceName,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        List<EndpointDefinition> endpoints = new ArrayList<>();
        parsed.endpoints().forEach((name, def) -> endpoints.add(
                ConjureParserUtils.parseEndpoint(
                        name, def, parsed.basePath(), parseAuthType(parsed.defaultAuth()), typeResolver)));
        return ServiceDefinition.builder()
                .serviceName(serviceName)
                .docs(parsed.docs().map(Documentation::of))
                .addAllEndpoints(endpoints)
                .build();
    }

    static List<TypeDefinition> parseObjects(
            com.palantir.conjure.parser.types.TypesDefinition parsed,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        Optional<ConjurePackage> defaultPackage =
                parsed.definitions().defaultConjurePackage().map(p -> ConjurePackage.of(p.name()));

        ImmutableList.Builder<TypeDefinition> objectsBuilder = ImmutableList.builder();
        objectsBuilder.addAll(
                parsed.definitions().objects().entrySet().stream()
                        .map(entry -> entry.getValue().visit(
                                new TypeDefinitionParserVisitor(entry.getKey().name(), defaultPackage, typeResolver)))
                .collect(Collectors.toList()));
        return objectsBuilder.build();
    }

    static List<ErrorTypeDefinition> parseErrors(
            com.palantir.conjure.parser.types.ObjectsDefinition defs,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        Optional<ConjurePackage> defaultPackage = defs.defaultConjurePackage().map(p -> ConjurePackage.of(p.name()));
        ImmutableList.Builder<ErrorTypeDefinition> errorsBuidler = ImmutableList.builder();
        errorsBuidler.addAll(defs.errors().entrySet().stream().map(entry -> {
            TypeName typeName = TypeName.of(
                    entry.getKey().name(), parsePackageOrElseThrow(entry.getValue().conjurePackage(), defaultPackage));
            return parseErrorType(typeName, entry.getValue(), typeResolver);
        }).collect(Collectors.toList()));
        return errorsBuidler.build();
    }

    private static EnumValueDefinition parseEnumValue(
            com.palantir.conjure.parser.types.complex.EnumValueDefinition def) {
        return EnumValueDefinition.builder()
                .value(def.value())
                .docs(def.docs().map(Documentation::of))
                .build();
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
    private static List<ErrorTypeDefinition> parseAllErrors(
            com.palantir.conjure.parser.types.TypesDefinition parsed,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {

        // Collect all imported object.
        ImmutableList.Builder<ErrorTypeDefinition> errorsBuilder = ImmutableList.builder();
        errorsBuilder.addAll(parseErrors(parsed.definitions(), typeResolver));

        for (com.palantir.conjure.parser.types.reference.ConjureImports imported : parsed.conjureImports().values()) {
            // Since we don't support transitive imports, the type resolver for the imported types consist of
            // its direct types and external imports only.
            ConjureTypeParserVisitor.ReferenceTypeResolver importResolver =
                    new ConjureTypeParserVisitor.ByParsedRepresentationTypeNameResolver(imported.conjure().types());
            List<ErrorTypeDefinition> importedErrors =
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
                FieldDefinition.of(
                        FieldName.of(entry.getKey().name()),
                        entry.getValue().type().visit(new ConjureTypeParserVisitor(typeResolver)),
                        entry.getValue().docs().map(Documentation::of)))
                .collect(Collectors.toList());
    }

    private static EndpointDefinition parseEndpoint(
            String name,
            com.palantir.conjure.parser.services.EndpointDefinition def,
            com.palantir.conjure.parser.services.PathDefinition basePath,
            Optional<AuthType> defaultAuth,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        RequestLineDefinition requestDef = parseRequestLine(basePath, def.http());

        return EndpointDefinition.builder()
                .endpointName(EndpointName.of(name))
                .http(requestDef)
                .auth(def.auth().map(ConjureParserUtils::parseAuthType).orElse(defaultAuth))
                .args(parseArgs(def.args(), requestDef, typeResolver))
                .markers(parseMarkers(def.markers(), typeResolver))
                .returns(def.returns().map(t -> t.visit(new ConjureTypeParserVisitor(typeResolver))))
                .docs(def.docs().map(Documentation::of))
                .deprecated(def.deprecated().map(Documentation::of))
                .build();
    }

    private static Optional<AuthType> parseAuthType(
            com.palantir.conjure.parser.services.AuthDefinition authDefinition) {

        switch (authDefinition.type())  {
            case HEADER:
                return Optional.of(HeaderAuthType.header());
            case COOKIE:
                return Optional.of(CookieAuthType.cookie(authDefinition.id()));
            case NONE:
                return Optional.empty();
            default:
                throw new IllegalArgumentException("Unrecognized auth type.");
        }
    }

    private static RequestLineDefinition parseRequestLine(com.palantir.conjure.parser.services.PathDefinition basePath,
            com.palantir.conjure.parser.services.RequestLineDefinition def) {
        return RequestLineDefinition.of(def.method(),
                com.palantir.conjure.defs.services.PathDefinition.of(basePath.resolve(def.path()).toString()));
    }

    private static List<ArgumentDefinition> parseArgs(
            Map<com.palantir.conjure.parser.services.ParameterName,
                    com.palantir.conjure.parser.services.ArgumentDefinition> args,
            RequestLineDefinition requestDef,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        ImmutableList.Builder<ArgumentDefinition> resultBuilder = ImmutableList.builder();
        for (Map.Entry<com.palantir.conjure.parser.services.ParameterName,
                com.palantir.conjure.parser.services.ArgumentDefinition> entry : args.entrySet()) {
            com.palantir.conjure.parser.services.ArgumentDefinition original = entry.getValue();
            ArgumentName argName = ArgumentName.of(entry.getKey().name());
            ParameterId paramId =
                    ParameterId.of(original.paramId().map(id -> id.name()).orElse(entry.getKey().name()));
            ArgumentDefinition.Builder builder = ArgumentDefinition.builder()
                    .argName(argName)
                    .type(original.type().visit(new ConjureTypeParserVisitor(typeResolver)))
                    .paramId(paramId)
                    .docs(original.docs().map(Documentation::of))
                    .markers(parseMarkers(original.markers(), typeResolver));

            if (original.paramType() != com.palantir.conjure.parser.services.ArgumentDefinition.ParamType.AUTO) {
                builder.paramType(ArgumentDefinition.ParamType.valueOf(original.paramType().name()));
            } else {
                // AUTO type
                if (requestDef.pathArgs().contains(argName)) {
                    // argument exists in request line -- it is a path arg
                    builder.paramType(ArgumentDefinition.ParamType.PATH);
                } else {
                    // argument does not exist in request line -- it is a body arg
                    builder.paramType(ArgumentDefinition.ParamType.BODY);
                }
            }
            resultBuilder.add(builder.build());
        }
        return resultBuilder.build();
    }

    private static Set<Type> parseMarkers(
            Set<com.palantir.conjure.parser.types.ConjureType> markers,
            ConjureTypeParserVisitor.ReferenceTypeResolver typeResolver) {
        return markers.stream()
                .map(m -> m.visit(new ConjureTypeParserVisitor(typeResolver)))
                .collect(Collectors.toSet());
    }

}
