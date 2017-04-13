/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.types;

import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ConjurePackage;
import com.palantir.conjure.defs.types.EnumTypeDefinition;
import com.palantir.conjure.defs.types.FieldName;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.TypeName;
import com.palantir.conjure.gen.python.PackageNameProcessor;
import com.palantir.conjure.gen.python.poet.PythonBean;
import com.palantir.conjure.gen.python.poet.PythonBean.PythonField;
import com.palantir.conjure.gen.python.poet.PythonClass;
import com.palantir.conjure.gen.python.poet.PythonEnum;
import com.palantir.conjure.gen.python.poet.PythonEnum.PythonEnumValue;
import com.palantir.conjure.gen.python.poet.PythonImport;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class DefaultBeanGenerator implements BeanGenerator {

    @Override
    public PythonClass generateObject(TypesDefinition types,
            ConjureImports importedTypes,
            PackageNameProcessor packageNameProcessor,
            TypeName typeName,
            BaseObjectTypeDefinition typeDef) {
        if (typeDef instanceof ObjectTypeDefinition) {
            return generateObject(types, importedTypes, packageNameProcessor, typeName, (ObjectTypeDefinition) typeDef);
        } else if (typeDef instanceof EnumTypeDefinition) {
            return generateObject(packageNameProcessor, typeName, (EnumTypeDefinition) typeDef);
        } else {
            throw new UnsupportedOperationException("cannot generate type for type def: " + typeDef);
        }
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

    private PythonBean generateObject(TypesDefinition types,
            ConjureImports importedTypes,
            PackageNameProcessor packageNameProcessor,
            TypeName typeName,
            ObjectTypeDefinition typeDef) {

        TypeMapper mapper = new TypeMapper(new DefaultTypeNameVisitor(types));
        TypeMapper myPyMapper = new TypeMapper(new MyPyTypeNameVisitor(types));
        ReferencedTypeNameVisitor referencedTypeNameVisitor = new ReferencedTypeNameVisitor(
                types, importedTypes, packageNameProcessor);

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
                                .attributeName(entry.getKey().toCase(FieldName.Case.SNAKE_CASE).name())
                                .jsonIdentifier(entry.getKey().name())
                                .docs(entry.getValue().docs())
                                .pythonType(mapper.getTypeName(entry.getValue().type()))
                                .myPyType(myPyMapper.getTypeName(entry.getValue().type()))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
