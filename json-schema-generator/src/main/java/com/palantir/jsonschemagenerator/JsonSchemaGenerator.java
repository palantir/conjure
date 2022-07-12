/*
 * (c) Copyright 2022 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.jsonschemagenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import com.palantir.jsonschemagenerator.jsonschema.Part;
import com.palantir.jsonschemagenerator.jsonschema.PartType;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@AutoService(Processor.class)
public final class JsonSchemaGenerator extends AbstractProcessor {
    private ProcessingEnvironment processingEnvironment;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        this.processingEnvironment = processingEnv;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(JsonSchema.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        TypeElement annotation = annotations.iterator().next();
        Set<? extends Element> entryPoints = roundEnv.getElementsAnnotatedWithAny(annotation);
        Element entryPoint = entryPoints.iterator().next();
        JsonSchema jsonSchema = entryPoint.getAnnotation(JsonSchema.class);
        List<String> names = entryPoint.getEnclosedElements().stream()
                .filter(element -> element.getKind().equals(ElementKind.METHOD))
                .map(Element::getSimpleName)
                .map(Name::toString)
                .collect(Collectors.toList());

        Part part = Part.builder()
                .type(PartType.OBJECT)
                .properties(names.stream().collect(Collectors.toMap(Function.identity(), _ignored -> Part.builder()
                        .type(PartType.OBJECT)
                        .build())))
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            FileObject resource = processingEnvironment
                    .getFiler()
                    .createResource(StandardLocation.CLASS_OUTPUT, "", jsonSchema.outputLocation(), entryPoint);
            try (Writer writer = resource.openWriter()) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, part);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }
}
