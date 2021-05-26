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

package com.palantir.conjure.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.parser.types.TypeDefinitionVisitor;
import com.palantir.conjure.parser.types.complex.EnumTypeDefinition;
import com.palantir.conjure.parser.types.complex.EnumValueDefinition;
import com.palantir.conjure.parser.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.parser.types.complex.UnionTypeDefinition;
import com.palantir.conjure.parser.types.names.FieldName;
import com.palantir.conjure.parser.types.names.Namespace;
import com.palantir.conjure.parser.types.names.TypeName;
import com.palantir.conjure.parser.types.primitive.PrimitiveType;
import com.palantir.conjure.parser.types.reference.AliasTypeDefinition;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.StringJoiner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ConjureParserTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testConjureInlinedImports() throws IOException {
        ConjureSourceFile conjure = ConjureParser.parse(new File("src/test/resources/example-conjure-imports.yml"));
        assertThat(conjure.types().conjureImports()).containsKey(Namespace.of("imports"));
    }

    @Test
    public void cyclicImportsAreNotAllowed() throws IOException {
        assertThatThrownBy(() -> ConjureParser.parse(new File("src/test/resources/example-recursive-imports.yml")))
                .isInstanceOf(ConjureParser.CyclicImportException.class);
    }

    @Test
    public void duplicate_keys_fail_to_parse() throws Exception {
        assertThatThrownBy(() -> ConjureParser.parse(new File("src/test/resources/duplicate-keys.yml")))
                .getRootCause()
                .hasMessageContaining("Duplicate field 'services'");
    }

    @Test
    public void testConjureExternalImports() {
        ConjureSourceFile conjure = ConjureParser.parse(new File("src/test/resources/example-external-types.yml"));
        assertThat(conjure.types()
                        .imports()
                        .get(TypeName.of("ExampleAnyImport"))
                        .baseType())
                .isEqualTo(PrimitiveType.fromString("any"));
    }

    @Test
    public void testConjureFieldDeprecation() {
        ConjureSourceFile conjure = ConjureParser.parse(new File("src/test/resources/example-deprecation.yml"));
        ObjectTypeDefinition object = conjure.types()
                .definitions()
                .objects()
                .get(TypeName.of("BeanWithDeprecatedField"))
                .visit(new TypeDefinitionVisitor<ObjectTypeDefinition>() {
                    @Override
                    public ObjectTypeDefinition visit(AliasTypeDefinition _def) {
                        throw new SafeIllegalArgumentException("Expected ObjectTypeDefinition");
                    }

                    @Override
                    public ObjectTypeDefinition visit(EnumTypeDefinition _def) {
                        throw new SafeIllegalArgumentException("Expected ObjectTypeDefinition");
                    }

                    @Override
                    public ObjectTypeDefinition visit(ObjectTypeDefinition def) {
                        return def;
                    }

                    @Override
                    public ObjectTypeDefinition visit(UnionTypeDefinition _def) {
                        throw new SafeIllegalArgumentException("Expected ObjectTypeDefinition");
                    }
                });
        assertThat(object.fields().get(FieldName.of("old")).deprecated()).hasValue("Test deprecated.");
    }

    @Test
    public void testConjureEnumValueDeprecation() {
        ConjureSourceFile conjure = ConjureParser.parse(new File("src/test/resources/example-deprecation.yml"));
        EnumTypeDefinition enumType = conjure.types()
                .definitions()
                .objects()
                .get(TypeName.of("EnumWithDeprecatedValues"))
                .visit(new TypeDefinitionVisitor<EnumTypeDefinition>() {
                    @Override
                    public EnumTypeDefinition visit(AliasTypeDefinition _def) {
                        throw new SafeIllegalArgumentException("Expected EnumTypeDefinition");
                    }

                    @Override
                    public EnumTypeDefinition visit(EnumTypeDefinition def) {
                        return def;
                    }

                    @Override
                    public EnumTypeDefinition visit(ObjectTypeDefinition _def) {
                        throw new SafeIllegalArgumentException("Expected EnumTypeDefinition");
                    }

                    @Override
                    public EnumTypeDefinition visit(UnionTypeDefinition _def) {
                        throw new SafeIllegalArgumentException("Expected EnumTypeDefinition");
                    }
                });
        EnumValueDefinition one = enumType.values().get(0);
        EnumValueDefinition two = enumType.values().get(1);
        assertThat(one.value()).isEqualTo("ONE");
        assertThat(one.deprecated()).isNotPresent();
        assertThat(two.value()).isEqualTo("TWO");
        assertThat(two.deprecated()).hasValue("Prefer ONE.");
    }

    @Test
    public void testConjureUnionTypeDeprecation() {
        ConjureSourceFile conjure = ConjureParser.parse(new File("src/test/resources/example-deprecation.yml"));
        UnionTypeDefinition union = conjure.types()
                .definitions()
                .objects()
                .get(TypeName.of("UnionWithDeprecatedTypes"))
                .visit(new TypeDefinitionVisitor<UnionTypeDefinition>() {
                    @Override
                    public UnionTypeDefinition visit(AliasTypeDefinition _def) {
                        throw new SafeIllegalArgumentException("Expected EnumTypeDefinition");
                    }

                    @Override
                    public UnionTypeDefinition visit(EnumTypeDefinition _def) {
                        throw new SafeIllegalArgumentException("Expected EnumTypeDefinition");
                    }

                    @Override
                    public UnionTypeDefinition visit(ObjectTypeDefinition _def) {
                        throw new SafeIllegalArgumentException("Expected EnumTypeDefinition");
                    }

                    @Override
                    public UnionTypeDefinition visit(UnionTypeDefinition def) {
                        return def;
                    }
                });
        assertThat(union.union().get(FieldName.of("first")).deprecated()).isNotPresent();
        assertThat(union.union().get(FieldName.of("second")).deprecated()).hasValue("Use 'first'.");
    }

    @Test
    public void testConjureRevisitedImports() throws IOException {
        // create a hierarchy of dependencies such that a significant amount of files is visited repeatedly
        // each file depends on all files from the previous level
        List<String> inners = ImmutableList.of(
                "innerOne",
                "innerTwo",
                "innerThree",
                "innerFour",
                "innerFive",
                "innerSix",
                "innerSeven",
                "innerEight",
                "innerNine",
                "innerTen");
        List<String> mid = ImmutableList.of("midOne", "midTwo", "midThree", "midFour", "midFive");
        List<String> top = ImmutableList.of("topOne", "topTwo", "topThree", "topFour", "topFive", "topSix");

        String root = "root";

        generateFiles(inners, ImmutableList.of());
        generateFiles(mid, inners);
        generateFiles(top, mid);
        generateFiles(ImmutableList.of(root), top);

        ConjureSourceFile result = ConjureParser.parse(
                temporaryFolder.getRoot().toPath().resolve(root + ".yml").toFile());
        assertThat(result.types().conjureImports()).isNotEmpty();
    }

    private void generateFiles(List<String> names, List<String> importedNamespaces) throws IOException {
        for (String name : names) {
            File file = temporaryFolder.newFile(name + ".yml");
            StringJoiner sj = new StringJoiner(System.lineSeparator());
            sj.add("types:");
            if (!importedNamespaces.isEmpty()) {
                sj.add("  conjure-imports:");
                for (String namespace : importedNamespaces) {
                    sj.add("    " + namespace + ": " + namespace + ".yml");
                }
            }
            // just add a definition so the file isn't empty
            sj.add("  definitions:");
            sj.add("    default-package: com.palantir.conjure.parser.test");
            sj.add("    objects:");
            sj.add("      TestObject:");
            sj.add("        alias: string");
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            writer.write(sj.toString());
            writer.close();
        }
    }
}
