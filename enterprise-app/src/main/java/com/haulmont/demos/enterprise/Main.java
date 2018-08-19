/*
 * Copyright (c) 2008-2018 Haulmont.
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

package com.haulmont.demos.enterprise;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;

import java.io.InputStream;
import java.util.List;

/**
 * A consumer of the Jandex index. We simply print out all types annotated with {@link Entity}.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        try (InputStream input = Main.class.getResourceAsStream("/META-INF/jandex.idx")) {
            IndexReader reader = new IndexReader(input);
            Index index = reader.read();

            List<AnnotationInstance> entityInstances = index.getAnnotations(
                    DotName.createSimple(Entity.class.getName())
            );

            for (AnnotationInstance annotationInstance : entityInstances) {
                System.out.println(annotationInstance.target().asClass().name());
            }
        }
    }
}