XSLTC compile-time error messages
=================================

General notes to translators and definitions:

1. XSLTC is the name of the product. It is an acronym for "XSLT Compiler".
XSLT is an acronym for "XML Stylesheet Language: Transformations".

2. A stylesheet is a description of how to transform an input XML document
into a resultant XML document (or HTML document or text). The stylesheet
itself is described in the form of an XML document.

3. A template is a component of a stylesheet that is used to match a
particular portion of an input document and specifies the form of the
corresponding portion of the output document.

4. An axis is a particular "dimension" in a tree representation of an XML
document; the nodes in the tree are divided along different axes.
Traversing the "child" axis, for instance, means that the program would
visit each child of a particular node; traversing the "descendant" axis
means that the program would visit the child nodes of a particular node,
their children, and so on until the leaf nodes of the tree are reached.

5. An iterator is an object that traverses nodes in a tree along a
particular axis, one at a time.

6. An element is a mark-up tag in an XML document; an attribute is a
modifier on the tag. For example, in <elem attr='val' attr2='val2'> "elem"
is an element name, "attr" and "attr2" are attribute names with the values
"val" and "val2", respectively.

7. A namespace declaration is a special attribute that is used to associate
a prefix with a URI (the namespace). The meanings of element names and
attribute names that use that prefix are defined with respect to that
namespace.

8. DOM is an acronym for Document Object Model. It is a tree representation
of an XML document.  
SAX is an acronym for the Simple API for XML processing. It is an API used
inform an XML processor (in this case XSLTC) of the structure and content
of an XML document.  
Input to the stylesheet processor can come from an XML parser in the form
of a DOM tree or through the SAX API.

9. DTD is a document type declaration. It is a way of specifying the
grammar for an XML file, the names and types of elements, attributes, etc.

10. XPath is a specification that describes a notation for identifying
nodes in a tree-structured representation of an XML document. An instance
of that notation is referred to as an XPath expression.

11. Translet is an invented term that refers to the class file that
contains the compiled form of a stylesheet.
