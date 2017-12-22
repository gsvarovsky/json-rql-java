[![Build Status](https://travis-ci.org/gsvarovsky/json-rql-java.svg?branch=master)](https://travis-ci.org/gsvarovsky/json-rql)

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
construct(
    subject("?person")
        .type("dbpedia-owl:Artist")
        .with("dbpedia-owl:birthPlace", "?city")
        .with("rdfs:label", "?name"))
             .where(
                 subject("?person")
                     .type("dbpedia-owl:Artist")
                     .with("dbpedia-owl:birthPlace", subject("?city")
                         .with("dbpedia-owl:country", subject("?country")
                             .with("rdfs:label", literal("Belgium").language("en")))
                         .with("rdfs:label", id("?cityName"), literal("Ghent").language("en")))
                     .with("rdfs:label", "?name"))
             .prefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
             .prefix("dbpedia", "http://dbpedia.org/resource/")
             .prefix("dbpedia-owl", "http://dbpedia.org/ontology/")
```