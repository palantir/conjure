/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.utils;

import com.google.common.base.Preconditions;
import com.palantir.conjure.gen.typescript.poet.ImportStatement;
import com.palantir.conjure.gen.typescript.poet.ImportStatementVisitor;
import com.palantir.conjure.gen.typescript.poet.StandardImportStatement;
import com.palantir.conjure.gen.typescript.poet.StarImportStatement;
import java.util.Comparator;

public enum ImportStatements {
    ; // Non-instantiable.

    public static final Comparator<ImportStatement> COMPARATOR =
            Comparator.comparing(ImportStatement::filepathToImport)
                    .thenComparing(ImportStatements::getTypeOrder)
                    .thenComparing(ImportStatements::compareSamePathSameType);

    private static int getTypeOrder(ImportStatement statement) {
        return statement.accept(new ImportStatementVisitor<Integer>() {
            @Override
            public Integer visit(StandardImportStatement importStatement) {
                return 0;
            }

            @Override
            public Integer visit(StarImportStatement importStatement) {
                return 1;
            }
        });
    }

    private static int compareSamePathSameType(ImportStatement statement1, ImportStatement statement2) {
        return statement1.accept(new ImportStatementVisitor<Integer>() {
            @Override
            public Integer visit(StandardImportStatement statement) {
                Preconditions.checkArgument(
                        statement2 instanceof StandardImportStatement,
                        "Expected second ImportStatement to be a StandardImportStatement, but was a %s",
                        statement2.getClass().getSimpleName());
                return compareSamePathStandardImportStatements(statement, (StandardImportStatement) statement2);
            }

            @Override
            public Integer visit(StarImportStatement statement) {
                Preconditions.checkArgument(
                        statement2 instanceof StarImportStatement,
                        "Expected second ImportStatement to be a StarImportStatement, but was a %s",
                        statement2.getClass().getSimpleName());
                return compareSamePathStarImportStatements(statement, (StarImportStatement) statement2);
            }
        });
    }

    private static int compareSamePathStandardImportStatements(
            StandardImportStatement statement1,
            StandardImportStatement statement2) {
        for (int nameIndex = 0; nameIndex < Math.min(statement1.namesToImport().size(),
                statement2.namesToImport().size()); nameIndex++) {
            int nameCompare = statement1.namesToImport().get(nameIndex).compareTo(
                    statement2.namesToImport().get(nameIndex));
            if (nameCompare != 0) {
                return nameCompare;
            }
        }
        return statement1.namesToImport().size() - statement2.namesToImport().size();
    }

    private static int compareSamePathStarImportStatements(
            StarImportStatement statement1,
            StarImportStatement statement2) {
        return statement1.variableName().compareTo(statement2.variableName());
    }
}
