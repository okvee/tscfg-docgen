# tscfg-docgen

Tools for automatic documentation generation from
[typesafe config](https://typesafehub.github.io/config/) configurations.

## Motivation

(TODO) Motivation: it can be useful to have a centralized and
automatically generated documentation of default configuration at hand instead
of having to look inside raw configuration files

Assumption: all configuration keys in reference.conf files are documented,
documentation is not duplicated in application.conf and application.conf does not
contain any keys that would not be present in reference.conf as well.

Assumption: Generated documentation should contain only default (reference)
configuration values that apply to all environments the application is used
at unless the reference configuration is overridden.

## Usage

```$xml
<plugin>
    <groupId>io.github.okvee</groupId>
    <artifactId>tscfg-docgen-maven-plugin</artifactId>
    <version>x.y.z</version>
</plugin>
```

## Substitutions

According to [standard typesafe config behavior](https://github.com/typesafehub/config#standard-behavior),
the `reference.conf` stack must be self-contained. However, to keep the generated
configuration documentation environment-agnostic, we don't want it to contain resolved
system environment substitutions because that would make the generated documentation
specific for the environment where it was generated. Instead, any unresolved
substitutions (incl. system environment ones) will be rendered as such.

## TODO

* make sure versions of maven-related dependencies are correct
* javadoc
* tests
* documentation (inspiration: https://github.com/asciidoctor/asciidoctor-maven-plugin, https://searls.github.io/jasmine-maven-plugin/)
* maven plugin release
* propose link in typesafe config github readme.md (pull request)
* ??? typesafe config: origin comment not overridden by local one when using variable substitution (even if local is non-empty and origin is empty!!!) ???
