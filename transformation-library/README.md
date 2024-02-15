# PHMS Transformation Library

This is a library translates PHMS domain objects into HL7 V3 XML and vice versa using Spring Thymeleaf and Jaxb
Unmarshalling, respectively.

The Transformation Library currently houses the security service that signs HL7 requests. This functionality is planned
to be refactored into its separate service in the future. In order for a project to use the transformation library, they
should have the valid certificate in their resource path and the following properties defined in their application.yml
file:

* phms.transformation.security.certificate.alias
* phms.transformation.security.certificate.password
* phms.transformation.security.keystore.location

## Conventions

#### Resources

* Each Thymeleaf HL7 xml template will live in `resources/templates`
* Each request will follow PascalCase and have the name of the request + `Request.xml`
    * e.g. `FindCandidateQueryRequest.xml`

#### transformService

There will be one generic service to transform an object to HL7 for Requests. This service would have two methods:

##### transformRequest

* Input parameters of a generic object and string key for template name to load
* Returns a string representation of the HL7 message after translation

##### transformResponse

* TODO: Look into how we can generically transform back to POJO with thymeleaf or jaxb unmarshalling

### Thymeleaf Examples

#### Values

To set a value of an xml node, like below:

```xml

<securityText>value</securityText>
```

use the following transformation

```xml

<securityText th:text="${request.securityText}"></securityText>
```

given that `securityText` is a property of `request` and `request` is set as a variable for the template's context (
see [PatientService.java](src/main/java/com/lblw/phms/hl7templateengine/service/PatientService.java))

#### Attributes

To set an attibrute (such as "root" in the following example):

```xml

<id root="7a8c286f-08d6-4776-a6a1-740ad00458b2"/>
```

use the following transformation

```xml

<id th:root="${request.uuid}"/>
```

given that `uuid` is a property of `request` and `request` is set as a variable for the template's context (
see [PatientService.java](src/main/java/com/lblw/phms/hl7templateengine/service/PatientService.java))

#### Date Formatting

You can format a java `Date` like below

```xml

<creationTime value="20210628114553.016-0400"/>
```

using the following helper function:

```xml

<creationTime th:value="${#dates.format(request.creationTime,'YYYYMMddHHmmss.SSSXX')}"/>
```

#### Conditional Node

You can conditionally include a node by using the following thymeleaf function:

```xml

<author th:if="${request.author}" contextControlCode="AP" typeCode="AUT"/>
```

The above will not render if the expression is false or null

#### Variables

You can set a variable in a parent tag to clean up nested references like in the following:

```xml

<author th:with="author = ${request.author}" contextControlCode="AP" typeCode="AUT">
    <time th:value="${#dates.format(author.time,'YYYYMMddHHmmss.SSSXX')}"/>
</author>
```

#### Iterations

You can iterate over a list of variables passed in the template to render multiple nodes to produce something like
below:

```xml

<personName>
    <value use="L">
        <given>John</given>
        <family>Doe</family>
    </value>
    <value use="L">
        <given>Joe</given>
        <family>Smith</family>
    </value>
    <value use="L">
        <given>Bob</given>
        <family>Jones</family>
    </value>
</personName>
```

By using the following templating

```xml

<personName>
    <value use="L" th:each="name : ${patient.names}">
        <given th:text="${name.givenName}"></given>
        <family th:text="${name.familyName}"></family>
    </value>
</personName>
```

## DSQ Call

The current strategy for making a DSQ call is the following:

1. Create the domain object for the Find Candidate Query Request
2. Use the Security Service to get a valid two-way SSL Context
    * Use the .pfx certificate provided with alias and password
    * In order for the connection to be two-way SSL, we must set the trust manager to have the certificate chain and the
      key manager to have the certificate
3. We use thymeleaf templating to transform the domain object into the string representation of the HL7 request. This
   HL7 request has not yet been signed, and should just have a blank Security tag in the SOAP Header
4. We must remove all whitespace and line breaks in between the XML tags in the HL7 string. If we do not, even though
   the signature we generate will be valid, DSQ will not accept it
5. We converted the cleaned HL7 string into a DOM Document and use javax.xml.crypto library to sign in
    * Use the .pfx certificate provided with alias and password for signature
    * We must create a reference saying that the generated signature elements should go underneath the Security tag
    * We must create a URI link (`Id="_0"`) to the SOAP Body so that we are only signing that section of the request,
      and not the full request
    * We must set the system property `com.sun.org.apache.xml.internal.security.ignoreLineBreaks` so that no linebreaks
      are added in the signature when it is generated
6. Once we have signed the request, we convert it back into a single-lined string. This string will then become the
   request body
7. When we make the HTTP call, we must ensure the following:
    * URI is correct for the environment (e.g. HTTPS:
      //CAIS.FORMA.S5.D4.RAMQ.GOUV.QC.CA/FOR/CA/XML/V02R02.0/1.0/Usagers_LIST/FindCandidates/ReceptionService.svc)
    * ContentType is set to "text/xml"
    * A `SoapAction` header is added with the correct URI for the environment (
      e.g. http://RAMQ.IR.IRA1_RechrUsag_svc/1/IFindCandidates/FindCandidates)

## Testing

Thymeleaf templates can be tested using the [Thymeleaf Testing](https://github.com/thymeleaf/thymeleaf-testing) library.
The format of the [test file](https://github.com/thymeleaf/thymeleaf-testing#test-file-format) (.thtest) is further
explained here.