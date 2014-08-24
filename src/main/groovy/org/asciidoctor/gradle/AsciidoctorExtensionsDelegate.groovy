package org.asciidoctor.gradle;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger

public class AsciidoctorExtensionsDelegate {

    private class ExtensionHolder {
        Map options
        Closure closure
    }

    private AsciidoctorTask task

    private List<ExtensionHolder> blockProcessors = []

    private List<ExtensionHolder> preProcessors = []

    private List<ExtensionHolder> postProcessors = []

    private List<ExtensionHolder> includeProcessors = []

    private List<ExtensionHolder> blockMacroProcessors = []

    private List<ExtensionHolder> inlineMacroProcessors = []

    private List<ExtensionHolder> treeProcessors = []

    private static AtomicInteger extensionClassCounter = new AtomicInteger(0)

    AsciidoctorExtensionsDelegate(AsciidoctorTask task) {
        this.task = task
    }

    void configure(Closure cl) {
        cl.delegate = this
        cl.run()
    }


    void registerExtensions(Object javaExtensionRegistry, GroovyClassLoader groovyClassLoader) {
        blockProcessors.each {
            javaExtensionRegistry.block(createBlockProcessor(it.options, it.closure, groovyClassLoader))
        }
        postProcessors.each {
            javaExtensionRegistry.postprocessor(createPostProcessor(it.options, it.closure, groovyClassLoader))
        }
        preProcessors.each {
            javaExtensionRegistry.preprocessor(createPreProcessor(it.options, it.closure, groovyClassLoader))
        }
        includeProcessors.each {
            javaExtensionRegistry.includeProcessor(createIncludeProcessor(it.options, it.closure, groovyClassLoader))
        }
        blockMacroProcessors.each {
            javaExtensionRegistry.blockMacro(createBlockMacroProcessor(it.options, it.closure, groovyClassLoader))
        }
        inlineMacroProcessors.each {
            javaExtensionRegistry.inlineMacro(createInlineMacroProcessor(it.options, it.closure, groovyClassLoader))
        }
        treeProcessors.each {
            javaExtensionRegistry.treeprocessor(createTreeProcessor(it.options, it.closure, groovyClassLoader))
        }
    }

    // Everything for BlockProcessors

    Object createBlockProcessor(Map options, Closure cl, GroovyClassLoader groovyClassLoader) {

        def simpleClassName = "Extension_" + extensionClassCounter.getAndIncrement()
        def packageName = "org.asciidoctor.gradle.extensions"
        def classContent = """package $packageName
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
        Object processor = clazz.newInstance(options["name"], options, cl)
        cl.delegate = processor
        return processor
    }

    void block(Map options=[:], Closure cl) {
        if (!options.containsKey("name")) {
            throw new IllegalArgumentException("Block must define a name");
        }
        if (!options.containsKey("contexts")) {
            //TODO: What are sensible defaults?
            options["contexts"] = [":open", ":paragraph"]
        }
        blockProcessors << new ExtensionHolder(options: options, closure: cl)
    }

    void block(String blockName, Closure cl) {
        block(["name": blockName], cl)
    }

    // Everything for PostProcessors

    Object createPostProcessor(Map options, Closure cl, GroovyClassLoader groovyClassLoader) {

        def simpleClassName = "Extension_" + extensionClassCounter.getAndIncrement()
        def packageName = "org.asciidoctor.gradle.extensions"
        def classContent = """package $packageName
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
        return processor
    }

    void postprocessor(Map options=[:], Closure cl) {
        postProcessors << new ExtensionHolder(options: options, closure: cl)
    }

    // Everything for PreProcessors

    Object createPreProcessor(Map options, Closure cl, GroovyClassLoader groovyClassLoader) {

        def simpleClassName = "Extension_" + extensionClassCounter.getAndIncrement()
        def packageName = "org.asciidoctor.gradle.extensions"
        def classContent = """package $packageName
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
        return processor
    }

    void preprocessor(Map options=[:], Closure cl) {
        preProcessors << new ExtensionHolder(options: options, closure: cl)
    }

    // Everything for IncludeProcessors

    Object createIncludeProcessor(Map options, Closure cl, GroovyClassLoader groovyClassLoader) {

        def simpleClassName = "Extension_" + extensionClassCounter.getAndIncrement()
        def packageName = "org.asciidoctor.gradle.extensions"
        def classContent = """package $packageName
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
        Closure filter = options["filter"]
        options.remove("filter")
        Object processor = clazz.newInstance(options, filter, cl)
        cl.delegate = processor
        return processor
    }

    void includeprocessor(Map options=[:], Closure cl) {
        includeProcessors << new ExtensionHolder(options: options, closure: cl)
    }


    // Everything for MacroProcessors

    Object createBlockMacroProcessor(Map options, Closure cl, GroovyClassLoader groovyClassLoader) {

        def simpleClassName = "Extension_" + extensionClassCounter.getAndIncrement()
        def packageName = "org.asciidoctor.gradle.extensions"
        def classContent = """package $packageName
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
        Object processor = clazz.newInstance(options["name"], options, cl)
        cl.delegate = processor
        return processor
    }

    void blockMacro(Map options=[:], Closure cl) {
        blockMacroProcessors << new ExtensionHolder(options: options, closure: cl)
    }

    // Everything for InlineMacroProcessors (?? Configure in the same way as a MacroProcessor?)

    Object createInlineMacroProcessor(Map options, Closure cl, GroovyClassLoader groovyClassLoader) {

        def simpleClassName = "Extension_" + extensionClassCounter.getAndIncrement()
        def packageName = "org.asciidoctor.gradle.extensions"
        def classContent = """package $packageName
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
        Object processor = clazz.newInstance(options["name"], options, cl)
        cl.delegate = processor
        return processor
    }

    void inlineMacro(Map options=[:], Closure cl) {
        inlineMacroProcessors << new ExtensionHolder(options: options, closure: cl)
    }

    void inlineMacro(String macroName, Closure cl) {
        inlineMacroProcessors << new ExtensionHolder(["name": macroName], closure: cl)
    }

    // Everything for TreeProcessors

    Object createTreeProcessor(Map options, Closure cl, GroovyClassLoader groovyClassLoader) {

        def simpleClassName = "Extension_" + extensionClassCounter.getAndIncrement()
        def packageName = "org.asciidoctor.gradle.extensions"
        def classContent = """package $packageName
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
        return processor
    }

    void treeprocessor(Map options=[:], Closure cl) {
        treeProcessors << new ExtensionHolder(options: options, closure: cl)
    }

}
