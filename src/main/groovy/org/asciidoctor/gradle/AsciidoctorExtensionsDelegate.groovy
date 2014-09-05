/*
 * Copyright 2013-2014 the original author or authors.
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
package org.asciidoctor.gradle

class AsciidoctorExtensionsDelegate {

    private final List<AsciidoctorExtensionHolder> blockProcessors = []

    private final List<AsciidoctorExtensionHolder> preProcessors = []

    private final List<AsciidoctorExtensionHolder> postProcessors = []

    private final List<AsciidoctorExtensionHolder> includeProcessors = []

    private final List<AsciidoctorExtensionHolder> blockMacroProcessors = []

    private final List<AsciidoctorExtensionHolder> inlineMacroProcessors = []

    private final List<AsciidoctorExtensionHolder> treeProcessors = []

    private static int extensionClassCounter = 0

    private static final String PACKAGE_NAME = 'org.asciidoctor.gradle.extensions'

    private static final String OPTION_NAME = 'name'

    private static final String OPTION_FILTER = 'filter'

    private static final String OPTION_CONTEXTS = 'contexts'

    void configure(Closure cl) {
        cl.delegate = this
        cl.run()
    }


    void registerExtensions(Object javaExtensionRegistry, GroovyClassLoader groovyClassLoader) {
        blockProcessors.each {
            javaExtensionRegistry.block(makeBlockProcessor(it.options, it.closure, groovyClassLoader))
        }
        postProcessors.each {
            javaExtensionRegistry.postprocessor(makePostProcessor(it.options, it.closure, groovyClassLoader))
        }
        preProcessors.each {
            javaExtensionRegistry.preprocessor(makePreProcessor(it.options, it.closure, groovyClassLoader))
        }
        includeProcessors.each {
            javaExtensionRegistry.includeProcessor(makeIncludeProcessor(it.options, it.closure, groovyClassLoader))
        }
        blockMacroProcessors.each {
            javaExtensionRegistry.blockMacro(makeBlockMacroProcessor(it.options, it.closure, groovyClassLoader))
        }
        inlineMacroProcessors.each {
            javaExtensionRegistry.inlineMacro(makeInlineMacroProcessor(it.options, it.closure, groovyClassLoader))
        }
        treeProcessors.each {
            javaExtensionRegistry.treeprocessor(makeTreeProcessor(it.options, it.closure, groovyClassLoader))
        }
    }

    // Everything for BlockProcessors

    Object makeBlockProcessor(Map options, Closure cl, GroovyClassLoader groovyClassLoader) {

        def simpleClassName = this.simpleClassName
        def classContent = """package $PACKAGE_NAME
import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.extension.Reader;
class $simpleClassName extends org.asciidoctor.extension.BlockProcessor {

		private Closure cl

		public $simpleClassName(String name, Map options, Closure cl) {
			super(name, options)
			this.cl = cl
		}

		public Object process(AbstractBlock parent, Reader reader, Map attributes) {
			cl.call(parent, reader, attributes)
		}
}"""

        Class clazz = groovyClassLoader.parseClass(classContent)
        Object processor = clazz.newInstance(options[OPTION_NAME], options, cl)
        cl.delegate = processor
    }

    void block(Map options=[:], Closure cl) {
        if (!options.containsKey(OPTION_NAME)) {
            throw new IllegalArgumentException('Block must define a name!')
        }
        if (!options.containsKey(AsciidoctorExtensionsDelegate.OPTION_CONTEXTS)) {
            //TODO: What are sensible defaults?
            options[AsciidoctorExtensionsDelegate.OPTION_CONTEXTS] = [':open', ':paragraph']
        }
        blockProcessors << new AsciidoctorExtensionHolder(options: options, closure: cl)
    }

    void block(String blockName, Closure cl) {
        block([(AsciidoctorExtensionsDelegate.OPTION_NAME): blockName], cl)
    }

    // Everything for PostProcessors

    Object makePostProcessor(Map options, Closure cl, GroovyClassLoader groovyClassLoader) {

        def simpleClassName = this.simpleClassName
        def classContent = """package $PACKAGE_NAME
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;
class $simpleClassName extends org.asciidoctor.extension.Postprocessor {

		private Closure cl

		public $simpleClassName(Map options, Closure cl) {
			super(options)
			this.cl = cl
		}

		public String process(Document document, String output) {
			cl.call(document, output)
		}
}"""

        Class clazz = groovyClassLoader.parseClass(classContent)
        Object processor = clazz.newInstance(options, cl)
        cl.delegate = processor
    }

    void postprocessor(Map options=[:], Closure cl) {
        postProcessors << new AsciidoctorExtensionHolder(options: options, closure: cl)
    }

    // Everything for PreProcessors

    Object makePreProcessor(Map options, Closure cl, GroovyClassLoader groovyClassLoader) {

        def simpleClassName = this.simpleClassName
        def classContent = """package $PACKAGE_NAME
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.PreprocessorReader;
class $simpleClassName extends org.asciidoctor.extension.Preprocessor {

		private Closure cl

		public $simpleClassName(Map options, Closure cl) {
			super(options)
			this.cl = cl
		}

		public PreprocessorReader process(Document document, PreprocessorReader reader) {
			cl.call(document, reader)
		}
}"""

        Class clazz = groovyClassLoader.parseClass(classContent)
        Object processor = clazz.newInstance(options, cl)
        cl.delegate = processor
    }

    void preprocessor(Map options=[:], Closure cl) {
        preProcessors << new AsciidoctorExtensionHolder(options: options, closure: cl)
    }

    // Everything for IncludeProcessors

    Object makeIncludeProcessor(Map options, Closure cl, GroovyClassLoader groovyClassLoader) {

        def simpleClassName = this.simpleClassName
        def classContent = """package $PACKAGE_NAME
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.extension.PreprocessorReader;
class $simpleClassName extends org.asciidoctor.extension.IncludeProcessor {

		private Closure filter
		private Closure cl

		public $simpleClassName(Map options, Closure filter, Closure cl) {
			super(options)
			this.filter = filter
			this.cl = cl
		}
		public boolean handles(String target) {
			filter.call(target)
		}
		public void process(DocumentRuby document, PreprocessorReader reader, String target, Map<String, Object> attributes) {
			cl.call(document, reader, target, attributes)
		}
}"""

        Class clazz = groovyClassLoader.parseClass(classContent)
        Closure filter = options[(AsciidoctorExtensionsDelegate.OPTION_FILTER)]
        options.remove(AsciidoctorExtensionsDelegate.OPTION_FILTER)
        Object processor = clazz.newInstance(options, filter, cl)
        cl.delegate = processor
    }

    void includeprocessor(Map options=[:], Closure cl) {
        includeProcessors << new AsciidoctorExtensionHolder(options: options, closure: cl)
    }


    // Everything for MacroProcessors

    Object makeBlockMacroProcessor(Map options, Closure cl, GroovyClassLoader groovyClassLoader) {

        def simpleClassName = this.simpleClassName
        def classContent = """package $PACKAGE_NAME
import org.asciidoctor.ast.AbstractBlock;
class $simpleClassName extends org.asciidoctor.extension.BlockMacroProcessor {

		private Closure cl

		public $simpleClassName(String name, Map options, Closure cl) {
			super(name, options)
			this.cl = cl
		}
		public Object process(AbstractBlock parent, String target, Map<String, Object> attributes) {
			cl.call(parent, target, attributes)
		}
}"""

        Class clazz = groovyClassLoader.parseClass(classContent)
        Object processor = clazz.newInstance(options[OPTION_NAME], options, cl)
        cl.delegate = processor
    }

    void blockMacro(Map options=[:], Closure cl) {
        blockMacroProcessors << new AsciidoctorExtensionHolder(options: options, closure: cl)
    }

    // Everything for InlineMacroProcessors (?? Configure in the same way as a MacroProcessor?)

    Object makeInlineMacroProcessor(Map options, Closure cl, GroovyClassLoader groovyClassLoader) {

        def simpleClassName = this.simpleClassName
        def classContent = """package $PACKAGE_NAME
import org.asciidoctor.ast.AbstractBlock;
class $simpleClassName extends org.asciidoctor.extension.InlineMacroProcessor {

		private Closure cl

		public $simpleClassName(String name, Map options, Closure cl) {
			super(name, options)
			this.cl = cl
		}
		public Object process(AbstractBlock parent, String target, Map<String, Object> attributes) {
			cl.call(parent, target, attributes)
		}
}"""

        Class clazz = groovyClassLoader.parseClass(classContent)
        Object processor = clazz.newInstance(options[OPTION_NAME], options, cl)
        cl.delegate = processor
    }

    void inlineMacro(Map options=[:], Closure cl) {
        inlineMacroProcessors << new AsciidoctorExtensionHolder(options: options, closure: cl)
    }

    void inlineMacro(String macroName, Closure cl) {
        inlineMacroProcessors << new AsciidoctorExtensionHolder([OPTION_NAME: macroName], closure: cl)
    }

    // Everything for TreeProcessors

    Object makeTreeProcessor(Map options, Closure cl, GroovyClassLoader groovyClassLoader) {

        def simpleClassName = this.simpleClassName
        def classContent = """package $PACKAGE_NAME
import org.asciidoctor.ast.Document;
class $simpleClassName extends org.asciidoctor.extension.Treeprocessor {

    private Closure cl

    public $simpleClassName(Map options, Closure cl) {
        super(options)
        this.cl = cl
    }
    public Document process(Document document) {
        def ret = cl.call(document)
        if (!(ret instanceof Document)) {
            // Assume that the closure does something as last
            // statement that was not intended to be the return value
            // -> Return null
            null
        } else {
            ret
        }
    }
}"""

        Class clazz = groovyClassLoader.parseClass(classContent)
        Object processor = clazz.newInstance(options, cl)
        cl.delegate = processor
    }

    void treeprocessor(Map options=[:], Closure cl) {
        treeProcessors << new AsciidoctorExtensionHolder(options: options, closure: cl)
    }

    private String getSimpleClassName() {
        'Extension_' + extensionClassCounter++
    }
}
