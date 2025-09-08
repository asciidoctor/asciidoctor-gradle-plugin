require 'asciidoctor'
require 'json'

if Gem::Version.new(Asciidoctor::VERSION) <= Gem::Version.new('1.5.1')
  fail 'asciidoctor: FAILED: HTML5/Slim backend needs Asciidoctor >=1.5.2!'
end

unless defined? Slim::Include
  fail 'asciidoctor: FAILED: HTML5/Slim backend needs Slim >= 2.1.0!'
end

# Add custom functions to this module that you want to use in your Slim
# templates. Within the template you can invoke them as top-level functions
# just like in Haml.
module Slim::Helpers

  # URIs of external assets.
  FONT_AWESOME_URI     = '//cdnjs.cloudflare.com/ajax/libs/font-awesome/4.5.0/css/font-awesome.min.css'
  HIGHLIGHTJS_BASE_URI = '//cdnjs.cloudflare.com/ajax/libs/highlight.js/7.4'
  MATHJAX_JS_URI       = '//cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-MML-AM_HTMLorMML'
  PRETTIFY_BASE_URI    = '//cdnjs.cloudflare.com/ajax/libs/prettify/r298'

  # Defaults
  DEFAULT_HIGHLIGHTJS_THEME = 'github'
  DEFAULT_PRETTIFY_THEME = 'prettify'
  DEFAULT_SECTNUMLEVELS = 3
  DEFAULT_TOCLEVELS = 2

  # The MathJax configuration.
  MATHJAX_CONFIG = {
    tex2jax: {
      inlineMath:  [::Asciidoctor::INLINE_MATH_DELIMITERS[:latexmath]],
      displayMath: [::Asciidoctor::BLOCK_MATH_DELIMITERS[:latexmath]],
      ignoreClass: 'nostem|nolatexmath'
    },
    asciimath2jax: {
      delimiters:  [::Asciidoctor::BLOCK_MATH_DELIMITERS[:asciimath]],
      ignoreClass: 'nostem|noasciimath'
    }
  }.to_json

  VOID_ELEMENTS = %w(area base br col command embed hr img input keygen link meta param source track wbr)


  ##
  # Creates an HTML tag with the given name and optionally attributes. Can take
  # a block that will run between the opening and closing tags.
  #
  # @param name [#to_s] the name of the tag.
  # @param attributes [Hash]
  # @param content [#to_s] the content; +nil+ to call the block.
  # @yield The block of Slim/HTML code within the tag (optional).
  # @return [String] a rendered HTML element.
  #
  def html_tag(name, attributes = {}, content = nil)
    attrs = attributes.reject { |_, v|
      v.nil? || (v.respond_to?(:empty?) && v.empty?)
    }.map do |k, v|
      v = v.compact.join(' ') if v.is_a? Array
      v = nil if v == true
      v = %("#{v}") if v
      [k, v] * '='
    end
    attrs_str = attrs.empty? ? '' : attrs.join(' ').prepend(' ')

    if VOID_ELEMENTS.include? name.to_s
      %(<#{name}#{attrs_str}>)
    else
      content ||= yield if block_given?
      %(<#{name}#{attrs_str}>#{content}</#{name}>)
    end
  end

  ##
  # Conditionally wraps a block in an element. If condition is +true+ then it
  # renders the specified tag with optional attributes and the given
  # block inside, otherwise it just renders the block.
  #
  # For example:
  #
  #    = html_tag_if link?, 'a', {class: 'image', href: (attr :link)}
  #      img src='./img/tux.png'
  #
  # will produce:
  #
  #    <a href="http://example.org" class="image">
  #      <img src="./img/tux.png">
  #    </a>
  #
  # if +link?+ is truthy, and just
  #
  #   <img src="./img/tux.png">
  #
  # otherwise.
  #
  # @param condition [Boolean] the condition to test to determine whether to
  #        render the enclosing tag.
  # @param name (see #html_tag)
  # @param attributes (see #html_tag)
  # @yield (see #html_tag)
  # @return [String] a rendered HTML fragment.
  #
  def html_tag_if(condition, name, attributes = {}, &block)
    if condition
      html_tag name, attributes, &block
    else
      yield
    end
  end

  ##
  # Surrounds a block with strings, with no whitespace in between.
  #
  # @example
  #   = surround '[', ']' do
  #     a href="#_footnote_1" 1
  #
  #   [<a href="#_footnote_1">1</a>]
  #
  # @param front [String] the string to add before the block.
  # @param back [String] the string to add after the block.
  # @yield The block of Slim/HTML code to surround.
  # @return [String] a rendered HTML fragment.
  #
  def surround(front, back = front)
    [front, yield.chomp, back].join
  end

  ##
  # Wraps a block in a div element with the specified class and optionally
  # the node's +id+ and +role+(s). If the node's +captioned_title+ is not
  # empty, than a nested div with the class "title" and the title's content
  # is added as well.
  #
  # Note: Every node has method +captioned_title+; if it doesn't have a
  # caption, then this method returns just a naked title.
  #
  # @example When @id, @role and @title attributes are set.
  #   = block_with_title class: ['quoteblock', 'center']
  #     blockquote =content
  #
  #   <div id="myid" class="quoteblock center myrole1 myrole2">
  #     <div class="title">Block Title</div>
  #     <blockquote>Lorem ipsum</blockquote>
  #   </div>
  #
  # @example When @id, @role and @title attributes are empty.
  #   = block_with_title class: 'quoteblock center', style: style_value(float: 'left')
  #     blockquote =content
  #
  #   <div class="quoteblock center" style="float: left;">
  #     <blockquote>Lorem ipsum</blockquote>
  #   </div>
  #
  # @example When shorthand style for class attribute is used.
  #   = block_with_title 'quoteblock center'
  #     blockquote =content
  #
  #   <div class="quoteblock center">
  #     <blockquote>Lorem ipsum</blockquote>
  #   </div>
  #
  # @param attributes [Hash, String] the tag's attributes as Hash),
  #        or the tag's class if it's not a Hash.
  # @param title_position [:top, :bottom] position of the title element.
  # @yield The block of Slim/HTML code within the tag (optional).
  # @return [String] a rendered HTML fragment.
  #
  def block_with_title(attributes = {}, title_position = :top, &block)
    if attributes.is_a? Hash
      klass = attributes.delete(:class)
    else
      klass = attributes
      attributes = {}
    end
    klass = klass.split(' ') if klass.is_a? String
    attributes[:class] = [klass, role].flatten.uniq
    attributes[:id] = id

    html_tag 'div', attributes do
      if captioned_title.nil_or_empty?
        yield
      else
        ary = [ html_tag('div', {class: 'title'}, captioned_title), yield ]
        ary.reverse! if title_position == :bottom
        ary.compact.join "\n"
      end
    end
  end

  ##
  # Delimite the given equation as a STEM of the specified type.
  #
  # @param equation [String] the equation to delimite.
  # @param type [#to_sym] the type of the STEM renderer (latexmath, or asciimath).
  # @return [String] the delimited equation.
  #
  def delimit_stem(equation, type)
    if is_a? ::Asciidoctor::Block
      open, close = ::Asciidoctor::BLOCK_MATH_DELIMITERS[type.to_sym]
    else
      open, close = ::Asciidoctor::INLINE_MATH_DELIMITERS[type.to_sym]
    end

    unless equation.start_with?(open) && equation.end_with?(close)
      equation = [open, equation, close].join
    end
    equation
  end

  ##
  # Formats the given hash as CSS declarations for an inline style.
  #
  # @example
  #   style_value(text_align: 'right', float: 'left')
  #   => "text-align: right; float: left;"
  #
  #   style_value(text_align: nil, float: 'left')
  #   => "float: left;"
  #
  #   style_value(width: [90, '%'], height: '50px')
  #   => "width: 90%; height: 50px;"
  #
  #   style_value(width: ['120px', 'px'])
  #   => "width: 90px;"
  #
  #   style_value(width: [nil, 'px'])
  #   => nil
  #
  # @param declarations [Hash]
  # @return [String, nil]
  #
  def style_value(declarations)
    decls = []

    declarations.each do |prop, value|
      next if value.nil?

      if value.is_a? Array
        value, unit = value
        next if value.nil?
        value = value.to_s + unit unless value.end_with? unit
      end
      prop = prop.to_s.gsub('_', '-')
      decls << %(#{prop}: #{value})
    end

    decls.empty? ? nil : decls.join('; ') + ';'
  end

  def urlize(*segments)
    path = segments * '/'
    if path.start_with? '//'
      @_uri_scheme ||= document.attr 'asset-uri-scheme', 'https'
      path = %(#{@_uri_scheme}:#{path}) unless @_uri_scheme.empty?
    end
    normalize_web_path path
  end


  ##
  # @param index [Integer] the footnote's index.
  # @return [String] footnote id to be used in a link.
  def footnote_id(index = (attr :index))
    %(_footnote_#{index})
  end

  ##
  # @param index (see #footnote_id)
  # @return [String] footnoteref id to be used in a link.
  def footnoteref_id(index = (attr :index))
    %(_footnoteref_#{index})
  end

  def icons?
    document.attr? :icons
  end

  def font_icons?
    document.attr? :icons, 'font'
  end

  def nowrap?
    'nowrap' if !document.attr?(:prewrap) || option?('nowrap')
  end

  ##
  # Returns corrected section level.
  #
  # @param sec [Asciidoctor::Section] the section node (default: self).
  # @return [Integer]
  #
  def section_level(sec = self)
    @_section_level ||= (sec.level == 0 && sec.special) ? 1 : sec.level
  end

  ##
  # Returns the captioned section's title, optionally numbered.
  #
  # @param sec [Asciidoctor::Section] the section node (default: self).
  # @return [String]
  #
  def section_title(sec = self)
    sectnumlevels = document.attr(:sectnumlevels, DEFAULT_SECTNUMLEVELS).to_i

    if sec.numbered && !sec.caption && sec.level <= sectnumlevels
      [sec.sectnum, sec.captioned_title].join(' ')
    else
      sec.captioned_title
    end
  end

  #--------------------------------------------------------
  # block_listing
  #

  def source_lang
    attr :language, nil, false
  end

  #--------------------------------------------------------
  # block_open
  #

  ##
  # Returns +true+ if an abstract block is allowed in this document type,
  # otherwise prints warning and returns +false+.
  def abstract_allowed?
    if result = (parent == document && document.doctype == 'book')
      puts 'asciidoctor: WARNING: abstract block cannot be used in a document without a title when doctype is book. Excluding block content.'
    end
    !result
  end

  ##
  # Returns +true+ if a partintro block is allowed in this context, otherwise
  # prints warning and returns +false+.
  def partintro_allowed?
    if result = (level != 0 || parent.context != :section || document.doctype != 'book')
      puts 'asciidoctor: ERROR: partintro block can only be used when doctype is book and it\'s a child of a book part. Excluding block content.'
    end
    !result
  end

  #--------------------------------------------------------
  # block_table
  #

  def autowidth?
    option? :autowidth
  end

  def spread?
    'spread' if !(option? 'autowidth') && (attr :tablepcwidth) == 100
  end

  #--------------------------------------------------------
  # block_video
  #

  # @return [Boolean] +true+ if the video should be embedded in an iframe.
  def video_iframe?
    ['vimeo', 'youtube'].include?(attr :poster)
  end

  def video_uri
    case (attr :poster, '').to_sym
    when :vimeo
      params = {
        autoplay: (1 if option? 'autoplay'),
        loop: (1 if option? 'loop')
      }
      start_anchor = %(#at=#{attr :start}) if attr? :start
      %(//player.vimeo.com/video/#{attr :target}#{start_anchor}#{url_query params})

    when :youtube
      video_id, list_id = (attr :target).split('/', 2)
      params = {
        rel: 0,
        start: (attr :start),
        end: (attr :end),
        list: (attr :list, list_id),
        autoplay: (1 if option? 'autoplay'),
        loop: (1 if option? 'loop'),
        controls: (0 if option? 'nocontrols')
      }
      %(//www.youtube.com/embed/#{video_id}#{url_query params})
    else
      anchor = [(attr :start), (attr :end)].join(',').chomp(',')
      anchor.prepend '#t=' unless anchor.empty?
      media_uri %(#{attr :target}#{anchor})
    end
  end

  # Formats URL query parameters.
  def url_query(params)
    str = params.map { |k, v|
      next if v.nil? || v.to_s.empty?
      [k, v] * '='
    }.compact.join('&amp;')

    str.prepend('?') unless str.empty?
  end

  #--------------------------------------------------------
  # document
  #

  ##
  # Returns HTML meta tag if the given +content+ is not +nil+.
  #
  # @param name [#to_s] the name for the metadata.
  # @param content [#to_s, nil] the value of the metadata, or +nil+.
  # @return [String, nil] the meta tag, or +nil+ if the +content+ is +nil+.
  #
  def html_meta_if(name, content)
    %(<meta name="#{name}" content="#{content}">) if content
  end

  # Returns formatted style/link and script tags for header.
  def styles_and_scripts
    scripts = []
    styles = []
    tags = []

    stylesheet = attr :stylesheet
    stylesdir = attr :stylesdir, ''
    default_style = ::Asciidoctor::DEFAULT_STYLESHEET_KEYS.include? stylesheet
    linkcss = (attr? :linkcss) || safe >= ::Asciidoctor::SafeMode::SECURE
    ss = ::Asciidoctor::Stylesheets.instance

    if linkcss
      path = default_style ? ::Asciidoctor::DEFAULT_STYLESHEET_NAME : stylesheet
      styles << { href: [stylesdir, path] }
    elsif default_style
      styles << { text: ss.primary_stylesheet_data }
    else
      styles << { text: read_asset(normalize_system_path(stylesheet, stylesdir), true) }
    end

    if attr? :icons, 'font'
      if attr? 'iconfont-remote'
        styles << { href: (attr 'iconfont-cdn', FONT_AWESOME_URI) }
      else
        styles << { href: [stylesdir, %(#{attr 'iconfont-name', 'font-awesome'}.css)] }
      end
    end

    if attr? 'stem'
      scripts << { src: MATHJAX_JS_URI }
      scripts << { type: 'text/x-mathjax-config', text: %(MathJax.Hub.Config(#{MATHJAX_CONFIG});) }
    end

    case attr 'source-highlighter'
    when 'coderay'
      if (attr 'coderay-css', 'class') == 'class'
        if linkcss
          styles << { href: [stylesdir, ss.coderay_stylesheet_name] }
        else
          styles << { text: ss.coderay_stylesheet_data }
        end
      end

    when 'pygments'
      if (attr 'pygments-css', 'class') == 'class'
        if linkcss
          styles << { href: [stylesdir, ss.pygments_stylesheet_name(attr 'pygments-style')] }
        else
          styles << { text: ss.pygments_stylesheet_data(attr 'pygments-style') }
        end
      end

    when 'highlightjs'
      hjs_base = attr :highlightjsdir, HIGHLIGHTJS_BASE_URI
      hjs_theme = attr 'highlightjs-theme', DEFAULT_HIGHLIGHTJS_THEME

      scripts << { src: [hjs_base, 'highlight.min.js'] }
      scripts << { src: [hjs_base, 'lang/common.min.js'] }
      scripts << { text: 'hljs.initHighlightingOnLoad()' }
      styles  << { href: [hjs_base, %(styles/#{hjs_theme}.min.css)] }

    when 'prettify'
      prettify_base = attr :prettifydir, PRETTIFY_BASE_URI
      prettify_theme = attr 'prettify-theme', DEFAULT_PRETTIFY_THEME

      scripts << { src: [prettify_base, 'prettify.min.js'] }
      scripts << { text: 'document.addEventListener("DOMContentLoaded", prettyPrint)' }
      styles  << { href: [prettify_base, %(#{prettify_theme}.min.css)] }
    end

    styles.each do |item|
      if item.key?(:text)
        tags << html_tag(:style, {}, item[:text])
      else
        tags << html_tag(:link, rel: 'stylesheet', href: urlize(*item[:href]))
      end
    end

    scripts.each do |item|
      if item.key? :text
        tags << html_tag(:script, {type: item[:type]}, item[:text])
      else
        tags << html_tag(:script, type: item[:type], src: urlize(*item[:src]))
      end
    end

    tags.join "\n"
  end

  #--------------------------------------------------------
  # inline_anchor
  #

  # @return [String, nil] text of the xref anchor, or +nil+ if not found.
  def xref_text
    str = text || document.references[:ids][attr :refid || target]
    str.tr_s("\n", ' ') if str
  end

  #--------------------------------------------------------
  # inline_image
  #

  # @return [Array] style classes for a Font Awesome icon.
  def icon_fa_classes
    [ %(fa fa-#{target}),
      (%(fa-#{attr :size}) if attr? :size),
      (%(fa-rotate-#{attr :rotate}) if attr? :rotate),
      (%(fa-flip-#{attr :flip}) if attr? :flip)
    ].compact
  end
end
