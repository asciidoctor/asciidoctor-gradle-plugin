require 'asciidoctor'
require 'find'

def document_load(input, options = {}, &block)
  lines = nil
  if input.is_a?(File)
    options[:attributes] ||= {}
    attrs = options[:attributes]
    lines = input.readlines
    input_mtime = input.mtime
    input_path = File.expand_path(input.path)
    # hold off on setting infile and indir until we get a better sense of their purpose
    attrs['docfile'] = input_path
    attrs['docdir'] = File.dirname(input_path)
    attrs['docname'] = File.basename(input_path, File.extname(input_path))
    attrs['docdate'] = input_mtime.strftime('%Y-%m-%d')
    attrs['doctime'] = input_mtime.strftime('%H:%M:%S %Z')
    attrs['docdatetime'] = [attrs['docdate'], attrs['doctime']] * ' '
    # TODO: figure out why the following do not work if != html
    attrs['backend'] = 'html5'
    attrs['outfilesuffix'] = '.html'
  elsif input.respond_to?(:readlines)
    input.rewind rescue nil
    lines = input.readlines
  elsif input.is_a?(String)
    lines = input.lines.entries
  elsif input.is_a?(Array)
    lines = input.dup
  else
    raise "Unsupported input type: #{input.class}"
  end

  Asciidoctor::Document.new(lines, options, &block)
end

def document_render(input, options = {}, &block)
  in_place = options.delete(:in_place) || false
  to_file = options.delete(:to_file)

  if !options.has_key?(:header_footer) && (in_place || to_file)
    options[:header_footer] = true
  end

  doc = document_load(input, options, &block)

  if in_place && input.is_a?(File)
    to_file = File.join(File.dirname(input.path), "#{doc.attributes['docname']}#{doc.attributes['outfilesuffix']}")
  elsif to_file
    if File.directory?(File.dirname(to_file))
      to_file = File.join(to_file, "#{doc.attributes['docname']}#{doc.attributes['outfilesuffix']}")
    end
  end

  if !to_file.nil?
    File.open(to_file, 'w') { |file| file.write doc.render }
    nil
  else
    doc.render
  end
end

def document_render_file(filename, options = {}, &block)
  document_render(File.new(filename), options, &block)
end

Find.find($srcDir) do |path|
  if path =~ /.*\.a((sc(iidoc)?)|d(oc)?)$/
    doc = document_render_file(path, :header_footer => true, :to_file => $outputDir,
                               :attributes => {:safe => Asciidoctor::SafeMode::UNSAFE,
                                              :base_dir => $srcDir,
                                              'backend' => $backend})
    puts doc
  end
end
