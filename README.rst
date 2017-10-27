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
* Prefix-based grouping of configuration keys
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
