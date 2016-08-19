/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.services;

import com.palantir.conjure.defs.services.EndpointDefinition;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public final class ServiceGeneratorUtils {
    private ServiceGeneratorUtils() {
        // no
    }

    public static Optional<String> getJavaDoc(EndpointDefinition endpointDef) {
        Optional<String> deprecatedDocs = endpointDef.deprecated().map(
                deprecatedDocsValue -> StringUtils.appendIfMissing("@deprecated " + deprecatedDocsValue, "\n"));
        Optional<String> docs = endpointDef.docs().map(docsValue -> StringUtils.appendIfMissing(docsValue, "\n"));
        if (deprecatedDocs.isPresent() || docs.isPresent()) {
            StringBuilder content = new StringBuilder("");
            docs.ifPresent(docsValue -> content.append(docsValue));
            deprecatedDocs.ifPresent(deprecatedDocsValue -> content.append(deprecatedDocsValue));
            return Optional.of(content.toString());
        }
        return Optional.empty();
    }
}
