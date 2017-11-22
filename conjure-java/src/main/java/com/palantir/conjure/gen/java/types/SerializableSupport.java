/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.Serializable;
import javax.lang.model.element.Modifier;

/**
 * {@link SerializableSupport} utility to make generated objects implement {@link Serializable}
 * with <pre>serialVersionUID</pre> hard coded to <pre>1L</pre>.
 *
 * @see <a href="https://github.palantir.build/foundry/conjure/pull/705">705</a>
 */
final class SerializableSupport {

    /** serialVersionUID field used by java serialization to determine compatibility. **/
    private static final String SERIAL_VERSION_UID =  "serialVersionUID";

    private SerializableSupport() {
        // Utility
    }

    static void enable(TypeSpec.Builder typeBuilder) {
        typeBuilder.addSuperinterface(Serializable.class);
        typeBuilder.addField(FieldSpec.builder(
                Long.TYPE,
                SERIAL_VERSION_UID,
                Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("1L")
                .build());
    }
}
