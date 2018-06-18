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

package com.palantir.conjure.cli;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.base.Preconditions;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.spec.ConjureDefinition;
import java.io.IOException;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public final class ConjureCli {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

    private ConjureCli() {}

    public static void main(String[] args) throws IOException {
        generate(parseCliConfiguration(args));
    }

    static CliConfiguration parseCliConfiguration(String[] args) {
        CommandLineParser parser = new BasicParser();

        try {
            CommandLine cmd = parser.parse(new Options(), args, false);
            String[] parsedArgs = cmd.getArgs();

            Preconditions.checkArgument(parsedArgs.length == 2, "Usage: conjure <target> <output>");

            return CliConfiguration.of(parsedArgs[1], parsedArgs[2]);
        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void generate(CliConfiguration config) throws IOException {
        ConjureDefinition definition = Conjure.parse(config.inputFiles());
        OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(config.outputDirectory(), definition);
    }

}
