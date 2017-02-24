/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript;

import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import java.util.Set;

public interface DuplicateExportHandler {

    Set<ExportStatement> handleDuplicates(Set<ExportStatement> exports);

}
