tscfg-docgen
############

Tools for automatic documentation generation from `typesafe config`_ configurations.

.. contents::

.. section-numbering::


Main features
=============

* Documentation generated using predefined `FreeMarker`_ templates
* Ability to use custom templates
* Injection of documentation into existing files
* Prefix-based grouping of configuration keys for better structure
* Generated documentation is environment-agnostic
* Maven plugin


Installation
============

Using the core library
----------------------

.. code-block:: xml

    <dependency>
        <groupId>io.github.okvee</groupId>
        <artifactId>tscfg-docgen-core</artifactId>
        <version>x.y.z</version>
    </dependency>


Using Maven plugin
------------------

.. code-block:: xml

    <plugin>
        <groupId>io.github.okvee</groupId>
        <artifactId>tscfg-docgen-maven-plugin</artifactId>
        <version>x.y.z</version>
    </plugin>


Usage
=====

Maven plugin setup
------------------

.. code-block:: xml

    <plugin>
        ...
        <executions>
            <executions>
                <id>gen-docs</id>                 (1)
                <phase>process-resources</phase>  (2)
                <goals>
                    <goal>generate-docs</goal>    (3)
                </goals>
            </executions>
        </executions>
    </plugin>

1. A unique ID of this execution
2. Phase in which the execution should take place (by default, this is ``site``)
3. Goal to be executed (currently, ``generate-docs`` is the only supported goal)


Configuration options
---------------------

All supported configuration options will be demonstrated using maven plugin configuration.
The same set of options can be used when working with the core library directly.

inputFilePattern
  Pattern used to identify configuration files to read the configuration from (see
  `FileSystem.getPathMatcher(String)`_ for pattern syntax information). Defaults to
  ``glob:/**/src/main/resources/reference.conf`` (This pattern matches ``reference.conf``
  files in all (sub)directories, i.e. even in sub-modules in case the module using the
  plugin has some. This makes it possible to generated one global configuration
  documentation for an entire multi-module project).

outputFile
  Path to the file that will contain generated documentation. Defaults to
  ``${basedir}/config.md``.

overwriteExisting
  Indicates whether an already existing output file can be overwritten. Defaults to
  ``false``.

templateName
  Name of the predefined template to use. This must be one of the templates found in
  `templates directory`_ (name of the template file without ``.ftl`` extension).
  Defaults to ``markdown-gitlab``.

customTemplateFile
  Path to a custom `FreeMarker`_ template file. If set, this custom template file will
  be used instead of the predefined template specified by ``templateName`` option.

injectGeneratedDocs
  This option allows "injecting" the generated configuration to an already existing
  file without overwriting its original contents. Also see ``injectionStartPlaceholder``
  and ``injectionEndPlaceholder`` options. Defaults to ``false``.

injectionStartPlaceholder
  In case ``injectGeneratedDocs`` is ``true``, this option's value is used to identify
  a line in the output file which marks the beginning of the area where generated
  documentation will be injected. Defaults to ``<!-- tscfg-docgen-start -->``.

injectionEndPlaceholder
  In case ``injectGeneratedDocs`` is ``true``, this option's value is used to identify
  a line in the output file which marks the end of the area where generated
  documentation will be injected. Defaults to ``<!-- tscfg-docgen-end -->``.

ignoredPrefixes
  In order to exclude some of the configuration keys from generated documentation,
  you can specify one or more prefixes here. All keys matching one of the prefixes
  will be excluded from the documentation. No prefixes are ignored by default.

groups
  Allows grouping of configuration keys into separate sections in order to give
  the generated documentation a more structured form. See example below for more
  details.


Examples
--------

TODO: basic with non-default options, ignored prefixes

TODO: injecting generated docs

TODO: grouping (all keys must fall into one of the groups, at lest to defaut one with empty prefix)

TODO: avoid generation of docs in sub-modules by ``<inherited>false</inherited>``


Substitutions
=============

According to `standard typesafe config behavior`_, the ``reference.conf`` stack must be
self-contained. However, to keep the generated configuration documentation
environment-agnostic, we don't want it to contain resolved system environment
substitutions because that would make the generated documentation specific for the
environment where it was generated. Instead, any unresolved substitutions (incl. system
environment ones) will be rendered as such.


.. _typesafe config: https://lightbend.github.io/config/
.. _FreeMarker: http://freemarker.org/
.. _standard typesafe config behavior: https://github.com/lightbend/config#standard-behavior
.. _FileSystem.getPathMatcher(String): https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String-
.. _templates directory: tscfg-docgen-core/src/main/resources/templates
