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

package com.palantir.conjure.defs;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ReadmeTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void prove_readme_example_is_valid_conjure() throws IOException {
        Path path = folder.getRoot().toPath().resolve("example1.yml");
        Files.write(path, extractSnippetFromReadme("example1").getBytes(StandardCharsets.UTF_8));
        Conjure.parse(ImmutableList.of(path.toFile()));
    }

    private static String extractSnippetFromReadme(String name) throws IOException {
        String markdown = new String(Files.readAllBytes(Paths.get("..", "readme.md")), StandardCharsets.UTF_8);
        Matcher matcher = Pattern.compile("```yaml\\+" + name + "([^`]+)```", Pattern.DOTALL).matcher(markdown);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }
}
