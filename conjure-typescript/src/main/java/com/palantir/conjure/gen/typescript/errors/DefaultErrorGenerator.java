/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.errors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.complex.ErrorTypeDefinition;
import com.palantir.conjure.defs.types.complex.FieldDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.FieldName;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.StringExpression;
import com.palantir.conjure.gen.typescript.poet.TypescriptFieldSignature;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import com.palantir.conjure.gen.typescript.poet.TypescriptInterface;
import com.palantir.conjure.gen.typescript.poet.TypescriptSimpleType;
import com.palantir.conjure.gen.typescript.poet.TypescriptStringLiteralType;
import com.palantir.conjure.gen.typescript.poet.TypescriptType;
import com.palantir.conjure.gen.typescript.utils.GenerationUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.immutables.value.Value;

@SuppressWarnings("Slf4jLogsafeArgs")
public final class DefaultErrorGenerator implements ErrorGenerator {

    @Override
    public Set<TypescriptFile> generate(
            Map<TypeName, ErrorTypeDefinition> definitions, Optional<ConjurePackage> defaultPackage) {
        return generateAllFiles(definitions, defaultPackage).stream()
                .map(FileWithExports::file)
                .collect(toSet());
    }

    @Override
    public Map<ConjurePackage, Collection<ExportStatement>> generateExports(
            Map<TypeName, ErrorTypeDefinition> definitions, Optional<ConjurePackage> defaultPackage) {
        return generateAllFiles(definitions, defaultPackage).stream().collect(Collectors.toMap(
                FileWithExports::conjurePackage,
                fileWithExports -> ImmutableList.of(fileWithExports.toExportStatement()),
                this::concatLists));
    }

    @ConjureImmutablesStyle
    @Value.Immutable
    interface FileWithExports {
        ConjurePackage conjurePackage();
        TypescriptFile file();
        List<String> namesToExport();

        default ExportStatement toExportStatement() {
            return ExportStatement.builder()
                    .filepathToExport(file().path().toString())
                    .namesToExport(namesToExport())
                    .build();
        }
    }

    private Set<FileWithExports> generateAllFiles(
            Map<TypeName, ErrorTypeDefinition> definitions, Optional<ConjurePackage> defaultPackage) {

        Multimap<ConjurePackage, TypescriptInterface> codeByPackage =
                generateCodeByPackage(definitions, defaultPackage);

        return codeByPackage.asMap().entrySet()
                .stream()
                .map(entry -> {
                    TypescriptFile file = TypescriptFile.builder()
                            .name("Errors")
                            .parentFolderPath(GenerationUtils.packageToScopeAndModule(entry.getKey()))
                            .emittables(entry.getValue())
                            .build();

                    List<String> names = entry.getValue().stream()
                            .map(TypescriptInterface::name)
                            .map(Optional::get)
                            .collect(toList());

                    return ImmutableFileWithExports.builder()
                            .conjurePackage(entry.getKey())
                            .file(file)
                            .namesToExport(names)
                            .build();
                })
                .collect(toSet());
    }

    private Multimap<ConjurePackage, TypescriptInterface> generateCodeByPackage(
            Map<TypeName, ErrorTypeDefinition> definitions, Optional<ConjurePackage> defaultPackage) {

        // we're going to have a single 'errors' file per package
        ListMultimap<ConjurePackage, TypescriptInterface> codeByPackage = ArrayListMultimap.create();

        definitions.forEach((typeName, definition) -> {
            ConjurePackage conjurePackage = definition.conjurePackage().orElse(defaultPackage
                    .orElseThrow(() -> new IllegalArgumentException("No package for: " + typeName.name())));

            TypescriptInterface emittable = createSingleInterface(typeName, definition);

            codeByPackage.put(conjurePackage, emittable);
        });

        return codeByPackage;
    }

    /**
     * Generates the interface for a single error definition.
     */
    private TypescriptInterface createSingleInterface(TypeName typeName, ErrorTypeDefinition definition) {

        // errorCode is more like a category or 'bucket' of errors.
        TypescriptFieldSignature errorCode = TypescriptFieldSignature.builder()
                .name("errorCode")
                .typescriptType(TypescriptStringLiteralType.of(StringExpression.of(definition.code().name())))
                .build();

        // this is the discriminant and uniquely identifes an error type
        TypescriptFieldSignature errorName = TypescriptFieldSignature.builder()
                .name("errorName")
                .typescriptType(TypescriptStringLiteralType.of(
                        StringExpression.of(definition.namespace().name() + ":" + typeName.name())))
                .build();

        TypescriptFieldSignature errorInstanceId = TypescriptFieldSignature.builder()
                .name("errorInstanceId")
                .typescriptType(TypescriptSimpleType.of("string"))
                .build();

        TypescriptFieldSignature parameters = TypescriptFieldSignature.builder()
                .name("parameters")
                .typescriptType(createParametersInterface(definition.safeArgs()))
                .build();

        SortedSet<TypescriptFieldSignature> propertySignatures = Sets.newTreeSet();
        propertySignatures.add(errorCode);
        propertySignatures.add(errorName);
        propertySignatures.add(errorInstanceId);
        propertySignatures.add(parameters);

        return TypescriptInterface.builder()
                .name("I" + typeName.name())
                .propertySignatures(propertySignatures)
                .build();
    }

    /**
     * We need a nice inline interface for the safeargs block.
     */
    private TypescriptType createParametersInterface(Map<FieldName, FieldDefinition> safeArgs) {

        Set<TypescriptFieldSignature> signatures = safeArgs.entrySet()
                .stream()
                .map(safeArg -> TypescriptFieldSignature.builder()
                        .name(safeArg.getKey().name())
                        .typescriptType(TypescriptSimpleType.of("any"))
                        .build())
                .collect(toSet());

        return TypescriptInterface.builder()
                .export(false)
                .propertySignatures(new TreeSet<>(signatures))
                .build();
    }

    private <T> List<T> concatLists(Collection<T> left, Collection<T> right) {
        return Stream.concat(left.stream(), right.stream()).collect(toList());
    }
}
