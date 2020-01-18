/*
 * Copyright 2013-2019 the original author or authors.
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
package example.extension

import groovy.transform.CompileStatic
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockProcessor
import org.asciidoctor.extension.Reader


@CompileStatic
class Big extends BlockProcessor {

    Big(String name, Map<String, Object> config) {
        super(name, config)
    }

    @Override
    Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
        createBlock(
            parent,
            'paragraph',
            reader.readLines()*.toUpperCase().join("\n"),
            attributes,
            [:]
        )
    }

}
