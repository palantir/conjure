/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.types;

import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.TypesDefinition;
import com.palantir.conjure.defs.types.complex.EnumTypeDefinition;
import com.palantir.conjure.defs.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.complex.UnionMemberTypeDefinition;
import com.palantir.conjure.defs.types.complex.UnionTypeDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.FieldName;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.gen.python.PackageNameProcessor;
import com.palantir.conjure.gen.python.poet.PythonBean;
import com.palantir.conjure.gen.python.poet.PythonBean.PythonField;
import com.palantir.conjure.gen.python.poet.PythonClass;
import com.palantir.conjure.gen.python.poet.PythonEnum;
import com.palantir.conjure.gen.python.poet.PythonEnum.PythonEnumValue;
import com.palantir.conjure.gen.python.poet.PythonIdentifierSanitizer;
import com.palantir.conjure.gen.python.poet.PythonImport;
import com.palantir.conjure.gen.python.poet.PythonUnionTypeDefinition;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class DefaultBeanGenerator implements PythonBeanGenerator {

    private final Set<ExperimentalFeatures> enabledExperimentalFeatures;

    public DefaultBeanGenerator(Set<ExperimentalFeatures> enabledExperimentalFeatures) {
        this.enabledExperimentalFeatures = ImmutableSet.copyOf(enabledExperimentalFeatures);
    }

    @Override
    public PythonClass generateObject(TypesDefinition types,
            PackageNameProcessor packageNameProcessor,
            TypeName typeName,
            BaseObjectTypeDefinition typeDef) {
        if (typeDef instanceof ObjectTypeDefinition) {
            return generateObject(types, packageNameProcessor, typeName, (ObjectTypeDefinition) typeDef);
        } else if (typeDef instanceof EnumTypeDefinition) {
            return generateObject(packageNameProcessor, typeName, (EnumTypeDefinition) typeDef);
        } else if (typeDef instanceof UnionTypeDefinition) {
            return generateObject(types, packageNameProcessor, typeName, (UnionTypeDefinition) typeDef);
        } else {
            throw new UnsupportedOperationException("cannot generate type for type def: " + typeDef);
        }
    }

    private PythonClass generateObject(TypesDefinition types, PackageNameProcessor packageNameProcessor,
            TypeName typeName, UnionTypeDefinition typeDef) {

        TypeMapper mapper = new TypeMapper(new DefaultTypeNameVisitor(types));
        TypeMapper myPyMapper = new TypeMapper(new MyPyTypeNameVisitor(types));
        ReferencedTypeNameVisitor referencedTypeNameVisitor = new ReferencedTypeNameVisitor(
                types, packageNameProcessor);

        List<PythonField> options = typeDef.union()
                .entrySet()
                .stream()
                .map(entry -> {
                    UnionMemberTypeDefinition unionMember = entry.getValue();
                    ConjureType conjureType = unionMember.type();
                    return PythonField.builder()
                        .attributeName(PythonIdentifierSanitizer.sanitize(
                                FieldName.of(entry.getKey())))
                        .docs(unionMember.docs())
                        .jsonIdentifier(entry.getKey())
                        .myPyType(myPyMapper.getTypeName(conjureType))
                        .pythonType(mapper.getTypeName(conjureType))
                        .build();
                })
                .collect(Collectors.toList());

        ConjurePackage packageName = packageNameProcessor.getPackageName(typeDef.conjurePackage());

        return PythonUnionTypeDefinition.builder()
            .packageName(packageName.name())
            .className(typeName.name())
            .docs(typeDef.docs())
            .addAllOptions(options)
            .build();
    }

    private PythonEnum generateObject(
            PackageNameProcessor packageNameProcessor,
            TypeName typeName,
            EnumTypeDefinition typeDef) {

        ConjurePackage packageName = packageNameProcessor.getPackageName(typeDef.conjurePackage());

        return PythonEnum.builder()
                .packageName(packageName.name())
                .className(typeName.name())
                .docs(typeDef.docs())
                .values(typeDef.values().stream()
                        .map(value -> PythonEnumValue.of(value.value(), value.docs()))
                        .collect(Collectors.toList()))
                .build();
    }

    private PythonBean generateObject(
            TypesDefinition types,
            PackageNameProcessor packageNameProcessor,
            TypeName typeName,
            ObjectTypeDefinition typeDef) {

        TypeMapper mapper = new TypeMapper(new DefaultTypeNameVisitor(types));
        TypeMapper myPyMapper = new TypeMapper(new MyPyTypeNameVisitor(types));
        ReferencedTypeNameVisitor referencedTypeNameVisitor = new ReferencedTypeNameVisitor(
                types, packageNameProcessor);

        ConjurePackage packageName = packageNameProcessor.getPackageName(typeDef.conjurePackage());

        Set<PythonImport> imports = typeDef.fields().entrySet()
                .stream()
                .flatMap(entry -> entry.getValue().type().visit(referencedTypeNameVisitor).stream())
                .filter(entry -> !entry.conjurePackage().equals(packageName)) // don't need to import if in this file
                .map(referencedClassName -> PythonImport.of(referencedClassName, Optional.empty()))
                .collect(Collectors.toSet());

        return PythonBean.builder()
                .packageName(packageName.name())
                .addAllRequiredImports(PythonBean.DEFAULT_IMPORTS)
                .addAllRequiredImports(imports)
                .className(typeName.name())
                .docs(typeDef.docs())
                .fields(typeDef.fields()
                        .entrySet()
                        .stream()
                        .map(entry -> PythonField.builder()
                                .attributeName(PythonIdentifierSanitizer.sanitize(entry.getKey()))
                                .jsonIdentifier(entry.getKey().name())
                                .docs(entry.getValue().docs())
                                .pythonType(mapper.getTypeName(entry.getValue().type()))
                                .myPyType(myPyMapper.getTypeName(entry.getValue().type()))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
