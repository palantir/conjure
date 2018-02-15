/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import com.palantir.product.EmptyPathService;

public final class EmptyPathResource implements EmptyPathService {
    @Override
    public boolean emptyPath() {
        return true;
    }
}
