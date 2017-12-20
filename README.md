# json-rql-java
Java library for the 
_[JSON Resource Query Language](http://json-rql.org), for simple, consistent query APIs_

**Work in Progress**

In contrast to the JavaScript library, this library:
* Presents an Abstract Syntax Tree (AST) for **json-rql** as [Jackson](https://github.com/FasterXML/jackson)-serializeable classes
* Separately supports translation of the AST to SPARQL via [Jena ARQ](https://jena.apache.org/documentation/query/) and, potentially, rdf4j and other non-RDF query languages
* Does not support translation of SPARQL into **json-rql**
