[![Build Status](https://travis-ci.org/gsvarovsky/json-rql-java.svg?branch=master)](https://travis-ci.org/gsvarovsky/json-rql-java)
![stability-wip](https://img.shields.io/badge/stability-work_in_progress-lightgrey.svg)

# json-rql-java
Java library for the 
_[JSON Resource Query Language](http://json-rql.org), for simple, consistent query APIs_

**Work in Progress** - see [testcases.json](/src/test/resources/org/jsonrql/testcases.json) for working serialisation cases.

In contrast to the JavaScript library, this library:
* Presents an Abstract Syntax Tree (AST) for **json-rql** as [Jackson](https://github.com/FasterXML/jackson)-serializeable classes
* Presents a Domain-Specific Language (DSL) for building the AST in code
* Separately supports translation of the AST to SPARQL via [Jena ARQ](https://jena.apache.org/documentation/query/) and, potentially, rdf4j and other non-RDF query languages
* Does not support translation of SPARQL into **json-rql**

DSL example:
```
select("?p", "?c")
    .where(
        subject("?p")
            .type("dbpedia-owl:Artist")
            .with("dbpedia-owl:birthPlace", subject("?c")
                .with("http://xmlns.com/foaf/0.1/name", literal("York").language("en"))))
    .context(prefix("dbpedia-owl", "http://dbpedia.org/ontology/"))
```
See the [Jena tests](/src/test/java/org/jsonrql/jena/JsonRqlJenaTest.java) for more examples.

**[Feedback](https://github.com/gsvarovsky/json-rql-java/issues) and contributions welcome!**
