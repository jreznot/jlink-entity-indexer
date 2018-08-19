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
package com.haulmont.demos.jlink;

import jdk.tools.jlink.plugin.*;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A plug-in for jlink which adds a Jandex index to the image.
 */
public class AddIndexPlugin implements Plugin {

    private static final String NAME = "add-index";

    private String targetModule;
    private List<String> modules;

    @Override

    public String getName() {
        return NAME;
    }

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public void configure(Map<String, String> config) {
        targetModule = config.get(NAME);

        var modulesToIndex = config.get("for-modules");
        this.modules = Arrays.asList(modulesToIndex.split(","));
    }

    @Override
    public String getDescription() {
        return "Adds an annotation index for one or more modules." + System.lineSeparator() +
                "<target-module>: name of the module which will host the index" + System.lineSeparator() +
                "<source-module-list>: comma-separated list of modules to include within the index";
    }

    @Override
    public String getArgumentsDescription() {
        return "<target-module>:for-modules=<source-module-list>";
    }

    @Override
    public ResourcePool transform(ResourcePool in, ResourcePoolBuilder out) {
        var indexer = new Indexer();

        for (String moduleName : modules) {
            ResourcePoolModule module = in.moduleView()
                    .findModule(moduleName)
                    .orElseThrow(() -> new RuntimeException(String.format("Module %s not found", moduleName)));

            module.entries()
                    .filter(this::shouldAddToIndex)
                    .forEach(e -> addToIndex(indexer, e));
        }

        var index = writeToOutputStream(indexer);
        out.add(ResourcePoolEntry.create(String.format("/%s/META-INF/jandex.idx", targetModule), index.toByteArray()));

        in.transformAndCopy(Function.identity(), out);

        return out.build();
    }

    private void addToIndex(Indexer indexer, ResourcePoolEntry entry) {
        try {
            indexer.index(entry.content());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private ByteArrayOutputStream writeToOutputStream(Indexer indexer) {
        var outStream = new ByteArrayOutputStream();

        try (outStream) {
            var index = indexer.complete();
            var writer = new IndexWriter(outStream);
            writer.write(index);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outStream;
    }

    private boolean shouldAddToIndex(ResourcePoolEntry entry) {
        return entry.path().endsWith("class");
    }
}
