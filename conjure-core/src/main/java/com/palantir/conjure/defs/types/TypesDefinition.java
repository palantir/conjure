/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.reference.ExternalTypeDefinition;
import com.palantir.conjure.parser.types.reference.ConjureImports;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface TypesDefinition {

    List<ExternalTypeDefinition> externalImports();

    /**
     * The object and error definitions imported from a particular ConjureDefinition. Compilers should typically not
     * generate these types, but only use them to resolve types referenced in {@link #definitions()}.
     */
    ObjectsDefinition imports();

    /** The object and error definitions local to a particular ConjureDefinition. */
    ObjectsDefinition definitions();

    /** The unions of {@link #definitions() locally defined} and {@link #imports() imported} objects and errors . */
    @Value.Derived
    default ObjectsDefinition definitionsAndImports() {
        return ObjectsDefinition.builder()
                .addAllObjects(imports().objects())
                .addAllObjects(definitions().objects())
                .addAllErrors(imports().errors())
                .addAllErrors(definitions().errors())
                .build();
    }

    static TypesDefinition fromParse(
            com.palantir.conjure.parser.types.TypesDefinition parsed,
            ConjureTypeParserVisitor.TypeNameResolver typeResolver) {

        // Collect all imported object and error definitions.
        ObjectsDefinition directDefinitions = ObjectsDefinition.fromParse(parsed.definitions(), typeResolver);
        ObjectsDefinition.Builder imports = ObjectsDefinition.builder();
        for (ConjureImports imported : parsed.conjureImports().values()) {
            // Since we don't support transitive imports, the type resolver for the imported types consist of
            // its direct objects and external imports only.
            ConjureTypeParserVisitor.TypeNameResolver importResolver =
                    new ConjureTypeParserVisitor.ByParsedRepresentationTypeNameResolver(imported.conjure().types());
            ObjectsDefinition importedObjects =
                    ObjectsDefinition.fromParse(imported.conjure().types().definitions(), importResolver);
            imports.addAllObjects(importedObjects.objects());
            imports.addAllErrors(importedObjects.errors());
        }

        return builder()
                .externalImports(parseImports(parsed.imports()))
                .imports(imports.build())
                .definitions(ObjectsDefinition.builder()
                        .addAllObjects(directDefinitions.objects())
                        .addAllErrors(directDefinitions.errors()).build())
                .build();
    }

    static Collection<ExternalTypeDefinition> parseImports(
            Map<com.palantir.conjure.parser.types.names.TypeName,
                    com.palantir.conjure.parser.types.reference.ExternalTypeDefinition> parsed) {
        List<ExternalTypeDefinition> imports = new ArrayList<>();
        parsed.forEach((name, def) -> imports.add(ExternalTypeDefinition.fromParse(
                TypeName.of(name.name(), ConjurePackage.EXTERNAL_IMPORT), def)));
        return imports;
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypesDefinition.Builder {}
}
