# tscfg-docgen

Tools for automatic documentation generation for
[typesafe config](https://typesafehub.github.io/config/) configurations.

(TODO) Motivation: it can be useful to have a centralized and
automatically generated documentation of default configuration at hand instead
of having to look inside raw configuration files

## TODO

* tscfg-docgen-core
  * source of configuration: either list of Files/Paths to be loaded in the list order (and resolved only after all files are loaded) or ready-made Config instance
  * output always to OutputStream so that user has flexibility wrt. where the output will be written (console/file/string)?
* tests
* documentation (inspiration: https://github.com/asciidoctor/asciidoctor-maven-plugin, https://searls.github.io/jasmine-maven-plugin/)
* test other input formats (JSON, properties files) - do they still provide all the necessary attributes like default value or description?
* maven plugin release
* ask for link in typesafe config github readme.md
* ??? typesafe config: origin comment not overriden by local one when using variable substitution (even if local is non-empty and origin is empty!!!) ???
