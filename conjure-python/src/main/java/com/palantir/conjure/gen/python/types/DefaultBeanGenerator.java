/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.types;

import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.validator.FieldNameValidator;
import com.palantir.conjure.defs.visitor.TypeDefinitionVisitor;
import com.palantir.conjure.gen.python.PackageNameProcessor;
import com.palantir.conjure.gen.python.poet.PythonAlias;
import com.palantir.conjure.gen.python.poet.PythonBean;
import com.palantir.conjure.gen.python.poet.PythonBean.PythonField;
import com.palantir.conjure.gen.python.poet.PythonClass;
import com.palantir.conjure.gen.python.poet.PythonEnum;
import com.palantir.conjure.gen.python.poet.PythonEnum.PythonEnumValue;
import com.palantir.conjure.gen.python.poet.PythonImport;
import com.palantir.conjure.gen.python.poet.PythonUnionTypeDefinition;
import com.palantir.conjure.spec.AliasDefinition;
import com.palantir.conjure.spec.EnumDefinition;
import com.palantir.conjure.spec.ObjectDefinition;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.spec.UnionDefinition;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class DefaultBeanGenerator implements PythonBeanGenerator {

    // TODO(qchen): remove?
    private final Set<ExperimentalFeatures> enabledExperimentalFeatures;

    public DefaultBeanGenerator(Set<ExperimentalFeatures> enabledExperimentalFeatures) {
        this.enabledExperimentalFeatures = ImmutableSet.copyOf(enabledExperimentalFeatures);
    }

    @Override
    public PythonClass generateObject(List<TypeDefinition> types,
            PackageNameProcessor packageNameProcessor,
            TypeDefinition typeDef) {
        if (typeDef.accept(TypeDefinitionVisitor.IS_OBJECT)) {
            return generateObject(types, packageNameProcessor, typeDef.accept(TypeDefinitionVisitor.OBJECT));
        } else if (typeDef.accept(TypeDefinitionVisitor.IS_ENUM)) {
            return generateObject(packageNameProcessor, typeDef.accept(TypeDefinitionVisitor.ENUM));
        } else if (typeDef.accept(TypeDefinitionVisitor.IS_UNION)) {
            return generateObject(types, packageNameProcessor, typeDef.accept(TypeDefinitionVisitor.UNION));
        } else if (typeDef.accept(TypeDefinitionVisitor.IS_ALIAS)) {
            return generateObject(types, packageNameProcessor, typeDef.accept(TypeDefinitionVisitor.ALIAS));
        } else {
            throw new UnsupportedOperationException("cannot generate type for type def: " + typeDef);
        }
    }

    private PythonClass generateObject(
            List<TypeDefinition> types,
            PackageNameProcessor packageNameProcessor,
            UnionDefinition typeDef) {

        TypeMapper mapper = new TypeMapper(new DefaultTypeNameVisitor(types));
        TypeMapper myPyMapper = new TypeMapper(new MyPyTypeNameVisitor(types));

        ReferencedTypeNameVisitor referencedTypeNameVisitor = new ReferencedTypeNameVisitor(
                types, packageNameProcessor);

        String packageName = packageNameProcessor.getPackageName(typeDef.getTypeName().getPackage());

        List<PythonField> options = typeDef.getUnion()
                .stream()
                .map(unionMember -> {
                    Type conjureType = unionMember.getType();
                    return PythonField.builder()
                            .attributeName(FieldNameValidator.toCase(
                                    unionMember.getFieldName(), FieldNameValidator.Case.SNAKE_CASE).get())
                            .docs(unionMember.getDocs())
                            .jsonIdentifier(unionMember.getFieldName().get())
                            .myPyType(myPyMapper.getTypeName(conjureType))
                            .pythonType(mapper.getTypeName(conjureType))
                            .build();
                })
                .collect(Collectors.toList());

        Set<PythonImport> imports = typeDef.getUnion()
                .stream()
                .flatMap(entry -> entry.getType().accept(referencedTypeNameVisitor).stream())
                .filter(entry -> !entry.conjurePackage().equals(packageName)) // don't need to import if in this file
                .map(referencedClassName -> PythonImport.of(referencedClassName, Optional.empty()))
                .collect(Collectors.toSet());

        return PythonUnionTypeDefinition.builder()
                .packageName(packageName)
                .className(typeDef.getTypeName().getName())
                .docs(typeDef.getDocs())
                .addAllOptions(options)
                .addAllRequiredImports(imports)
                .build();
    }

    private PythonEnum generateObject(PackageNameProcessor packageNameProcessor, EnumDefinition typeDef) {

        String packageName = packageNameProcessor.getPackageName(typeDef.getTypeName().getPackage());

        return PythonEnum.builder()
                .packageName(packageName)
                .className(typeDef.getTypeName().getName())
                .docs(typeDef.getDocs())
                .values(typeDef.getValues().stream()
                        .map(value -> PythonEnumValue.of(value.getValue(), value.getDocs()))
                        .collect(Collectors.toList()))
                .build();
    }

    private PythonBean generateObject(
            List<TypeDefinition> types,
            PackageNameProcessor packageNameProcessor,
            ObjectDefinition typeDef) {

        TypeMapper mapper = new TypeMapper(new DefaultTypeNameVisitor(types));
        TypeMapper myPyMapper = new TypeMapper(new MyPyTypeNameVisitor(types));
        ReferencedTypeNameVisitor referencedTypeNameVisitor = new ReferencedTypeNameVisitor(
                types, packageNameProcessor);

        String packageName = packageNameProcessor.getPackageName(typeDef.getTypeName().getPackage());

        Set<PythonImport> imports = typeDef.getFields()
                .stream()
                .flatMap(entry -> entry.getType().accept(referencedTypeNameVisitor).stream())
                .filter(entry -> !entry.conjurePackage().equals(packageName)) // don't need to import if in this file
                .map(referencedClassName -> PythonImport.of(referencedClassName, Optional.empty()))
                .collect(Collectors.toSet());

        return PythonBean.builder()
                .packageName(packageName)
                .addAllRequiredImports(PythonBean.DEFAULT_IMPORTS)
                .addAllRequiredImports(imports)
                .className(typeDef.getTypeName().getName())
                .docs(typeDef.getDocs())
                .fields(typeDef.getFields()
                        .stream()
                        .map(entry -> PythonField.builder()
                                .attributeName(FieldNameValidator.toCase(
                                        entry.getFieldName(), FieldNameValidator.Case.SNAKE_CASE).get())
                                .jsonIdentifier(entry.getFieldName().get())
                                .docs(entry.getDocs())
                                .pythonType(mapper.getTypeName(entry.getType()))
                                .myPyType(myPyMapper.getTypeName(entry.getType()))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private PythonAlias generateObject(
            List<TypeDefinition> types,
            PackageNameProcessor packageNameProcessor,
            AliasDefinition typeDef) {
        TypeMapper mapper = new TypeMapper(new DefaultTypeNameVisitor(types));
        ReferencedTypeNameVisitor referencedTypeNameVisitor = new ReferencedTypeNameVisitor(
                types, packageNameProcessor);
        String packageName = packageNameProcessor.getPackageName(typeDef.getTypeName().getPackage());

        Set<PythonImport> imports = typeDef.getAlias().accept(referencedTypeNameVisitor)
                .stream()
                .filter(entry -> !entry.conjurePackage().equals(packageName)) // don't need to import if in this file
                .map(referencedClassName -> PythonImport.of(referencedClassName, Optional.empty()))
                .collect(Collectors.toSet());

        return PythonAlias.builder()
                .aliasName(typeDef.getTypeName().getName())
                .aliasTarget(mapper.getTypeName(typeDef.getAlias()))
                .packageName(packageName)
                .addAllRequiredImports(imports)
                .build();
    }

}
