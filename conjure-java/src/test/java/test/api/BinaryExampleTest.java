/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package test.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import org.junit.Test;

public class BinaryExampleTest {

    @Test
    public void cannotMutateForSubsequentGets() {
        ByteBuffer buffer = ByteBuffer.allocate(1).put((byte) 1);
        buffer.rewind();
        BinaryExample binaryExample = BinaryExample.of(buffer);

        ByteBuffer firstGet = binaryExample.getBinary();
        ByteBuffer secondGet = binaryExample.getBinary();

        firstGet.get();
        assertThat(secondGet.remaining()).isNotZero();
    }

    @Test
    public void cannotMutateAfterSetting() {
        byte value = (byte) 1;
        ByteBuffer buffer = ByteBuffer.allocate(1).put(value);
        buffer.rewind();
        BinaryExample binaryExample = BinaryExample.of(buffer);
        buffer.put((byte) (value + 1));
        buffer.rewind();
        assertThat(binaryExample.getBinary().get()).isEqualTo(value);
    }
}
