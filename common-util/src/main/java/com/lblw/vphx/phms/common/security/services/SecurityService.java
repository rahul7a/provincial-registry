package com.lblw.vphx.phms.common.security.services;

import com.lblw.vphx.phms.common.constants.CommonConstants;
import com.lblw.vphx.phms.common.constants.ErrorConstants;
import com.lblw.vphx.phms.common.exceptions.InternalProcessorException;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.pharmacy.ProvincialPharmacyCertificateDetails;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Service
@Slf4j
public class SecurityService {
  private static final String PROPERTY_KEY_SECURE_VALIDATION = "org.jcp.xml.dsig.secureValidation";

  // Flag to not add any linebreaks in the signature when it is generated
  static {
    System.setProperty("com.sun.org.apache.xml.internal.security.ignoreLineBreaks", "true");
  }

  private final ProvincialRequestProperties.Security security;

  /**
   * Constructor
   *
   * @param provincialRequestProperties {@link ProvincialRequestProperties}
   */
  public SecurityService(ProvincialRequestProperties provincialRequestProperties) {
    this.security = provincialRequestProperties.getRequest().getSecurity();
  }

  /**
   * Signs and returns a given XML request
   *
   * @param request as a {@link String} to sign. Return null if input is null
   * @param useObjectStoreCert as whether to use the certificate from object storage or from project
   *     resources/certs folder
   * @return {@link Document} representation of the XML request that has been signed. If the
   *     corresponding certificate is not found, logs an error and returns back the unsigned request
   * @throws InvalidAlgorithmParameterException
   * @throws NoSuchAlgorithmException
   * @throws UnrecoverableKeyException
   * @throws KeyStoreException
   * @throws CertificateException
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws MarshalException
   * @throws XMLSignatureException
   */
  public Document signRequest(String request, boolean useObjectStoreCert)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
          UnrecoverableKeyException, KeyStoreException, CertificateException, IOException,
          ParserConfigurationException, SAXException, MarshalException, XMLSignatureException {
    return this.signRequestWithParams(
        request,
        security.getCertificate().getAlias(),
        security.getCertificate().getPassword(),
        security.getCertificate().getDefaultLocation(),
        useObjectStoreCert);
  }

  /**
   * Signs and returns a given XML request
   *
   * @param request as a {@link String} to sign. Return null if input is null
   * @param alias as a {@link String} is the alias of the certificate in keystore
   * @param password as a {@link String} is the password of the keystore
   * @param fileLocation as a {@link String} is the location of the keystore PFX file
   * @param useObjectStoreCert as whether to use the certificate from object storage or from project
   *     resources/certs folder
   * @return {@link Document} representation of the XML request that has been signed. If the
   *     corresponding certificate is not found, logs an error and returns back the unsigned request
   * @throws InvalidAlgorithmParameterException
   * @throws NoSuchAlgorithmException
   * @throws UnrecoverableKeyException
   * @throws KeyStoreException
   * @throws CertificateException
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws MarshalException
   * @throws XMLSignatureException
   */
  private Document signRequestWithParams(
      String request,
      String alias,
      String password,
      String fileLocation,
      boolean useObjectStoreCert) // TODO: Remove useObjectStoreCert
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
          UnrecoverableKeyException, KeyStoreException, CertificateException, IOException,
          ParserConfigurationException, SAXException, MarshalException, XMLSignatureException {

    if (StringUtils.isEmpty(request)) {
      log.info("HL7 request to sign was null or empty. Returning empty Document.");
      return null;
    }

    request = removeWhitespaceBetweenTags(request);

    // Create a DOM XMLSignatureFactory that will be used to generate the
    // enveloped signature
    XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance(CommonConstants.DOM);

    // Create a Reference to the enveloped document and
    // also specify the digest algorithm and the Transform.
    Reference ref =
        xmlSignatureFactory.newReference(
            "#_0",
            xmlSignatureFactory.newDigestMethod(DigestMethod.SHA1, null),
            Collections.singletonList(
                xmlSignatureFactory.newTransform(
                    CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec) null)),
            null,
            null);

    // Create the SignedInfo
    SignedInfo signedInfo =
        xmlSignatureFactory.newSignedInfo(
            xmlSignatureFactory.newCanonicalizationMethod(
                CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null),
            xmlSignatureFactory.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
            Collections.singletonList(ref));

    KeyStore keyStore = getKeyStore(fileLocation, password.toCharArray(), useObjectStoreCert);
    PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());

    // Instantiate the document to be signed
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setIgnoringComments(true);
    documentBuilderFactory.setIgnoringElementContentWhitespace(true);
    documentBuilderFactory.setNamespaceAware(true);
    documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    documentBuilderFactory.setFeature(
        "http://xml.org/sax/features/external-general-entities", false);
    documentBuilderFactory.setFeature(
        "http://xml.org/sax/features/external-parameter-entities", false);

    Document document =
        documentBuilderFactory
            .newDocumentBuilder()
            .parse(new InputSource(new StringReader(request)));

    // Get x509 Data from Certificate
    X509Certificate x509Certificate = (X509Certificate) keyStore.getCertificate(alias);

    // If cert is not found, return unsigned document
    if (x509Certificate == null) {
      log.error("Certificate was not found to sign request. Returning unsigned document");
      return document;
    }

    KeyInfoFactory keyInfoFactory = xmlSignatureFactory.getKeyInfoFactory();
    List<Object> x509ContentList = new ArrayList<>();

    x509ContentList.add(x509Certificate.getSubjectX500Principal().getName());
    x509ContentList.add(x509Certificate);

    X509Data x509Data = keyInfoFactory.newX509Data(x509ContentList);

    // Create a KeyInfo and add the KeyValue to it
    KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(x509Data));

    // Create a DOMSignContext and specify the PrivateKey and
    // location of the resulting XMLSignature's parent element
    Node securityNode = document.getElementsByTagName(CommonConstants.SECURITY).item(0);
    DOMSignContext domSignContext = new DOMSignContext(privateKey, securityNode);

    // Set Id attribute on SOAP Body since that is what we are signing
    Element body = (Element) document.getElementsByTagName(CommonConstants.SOAP_ENV_BODY).item(0);
    Attr idAttr = body.getAttributeNode(CommonConstants.ID);
    body.setIdAttributeNode(idAttr, true);
    domSignContext.setIdAttributeNS(body, null, CommonConstants.ID);

    // Create the XMLSignature (but don't sign it yet)
    XMLSignature signature = xmlSignatureFactory.newXMLSignature(signedInfo, keyInfo);

    // Marshal, generate (and sign) the enveloped signature
    signature.sign(domSignContext);
    return document;
  }

  /**
   * Signs and returns a given XML request with custom certificate properties
   *
   * @param request as a {@link String} to sign. Return null if input is null
   * @param certificateDetails as {@link ProvincialPharmacyCertificateDetails} the certificate
   *     metadata that contains location of PFX file
   * @return {@link Document} representation of the XML request that has been signed. If the
   *     corresponding certificate is not found, logs an error and returns back the unsigned request
   * @throws InvalidAlgorithmParameterException
   * @throws NoSuchAlgorithmException
   * @throws UnrecoverableKeyException
   * @throws KeyStoreException
   * @throws CertificateException
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws MarshalException
   * @throws XMLSignatureException
   */
  public Document signRequestUsingCertificate(
      String request, ProvincialPharmacyCertificateDetails certificateDetails)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
          UnrecoverableKeyException, KeyStoreException, CertificateException, IOException,
          ParserConfigurationException, SAXException, MarshalException, XMLSignatureException {
    // TODO: If condition will be removed once E2E is established for object storage service
    if (certificateDetails.getCertificateFileLocation() == null) {
      return this.signRequestWithParams(
          request,
          security.getCertificate().getAlias(),
          security.getCertificate().getPassword(),
          security.getCertificate().getDefaultLocation(),
          false);
    } else {
      return this.signRequestWithParams(
          request,
          certificateDetails.getAlias(),
          certificateDetails.getPassword(),
          certificateDetails.getCertificateFileLocation(),
          true);
    }
  }

  /**
   * Access and return a key store given its file name and password. A boolean flag to control
   * whether to load key store file from resource bundle or filesystem.
   *
   * @param keyStoreFileName file name of the key store
   * @param keyStorePassword Password for the key store
   * @param useObjectStoreCert as whether to use the certificate from object storage or from project
   *     resources/certs folder
   * @return {@link KeyStore} to access
   * @throws KeyStoreException
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws CertificateException
   */
  private KeyStore getKeyStore(
      String keyStoreFileName, char[] keyStorePassword, boolean useObjectStoreCert)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
    KeyStore keyStore = KeyStore.getInstance(CommonConstants.PKCS_12);
    try (InputStream stream =
        (useObjectStoreCert)
            ? new FileInputStream(keyStoreFileName)
            : getClass().getClassLoader().getResourceAsStream(keyStoreFileName)) {
      keyStore.load(stream, keyStorePassword);
    } catch (Exception e) {
      var errorMessage = String.format(ErrorConstants.EXCEPTION_WHILE_FETCHING_CERTIFICATE);
      log.error(errorMessage);
      throw new InternalProcessorException(errorMessage);
    }
    return keyStore;
  }

  /**
   * Returns whether or not a signed XML request has a valid signature
   *
   * @param request Signed {@link String} XML request to validate. Return false if input is null
   * @return True if the signature for the signed request is valid, false otherwise
   * @throws ParserConfigurationException
   * @throws IOException
   * @throws SAXException
   * @throws CertificateException
   * @throws KeyStoreException
   * @throws NoSuchAlgorithmException
   * @throws MarshalException
   * @throws XMLSignatureException
   */
  public boolean validateSignature(String request)
      throws ParserConfigurationException, IOException, SAXException, CertificateException,
          KeyStoreException, NoSuchAlgorithmException, MarshalException, XMLSignatureException {

    if (StringUtils.isEmpty(request)) {
      log.info(
          "Signature failed validation. String request to validate signature was empty or null.");
      return false;
    }

    InputStream stream = new ByteArrayInputStream(removeWhitespaceBetweenTags(request).getBytes());

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    documentBuilderFactory.setFeature(
        "http://xml.org/sax/features/external-general-entities", false);
    documentBuilderFactory.setFeature(
        "http://xml.org/sax/features/external-parameter-entities", false);

    Document document = documentBuilderFactory.newDocumentBuilder().parse(stream);

    NodeList nodeList = document.getElementsByTagName(CommonConstants.SIGNATURE);

    if (nodeList.getLength() == 0) {
      log.info("Signature failed validation. Cannot find Signature element.");
      return false;
    }

    // Set Id attribute on SOAP Body since that is what we are signing
    Element body = (Element) document.getElementsByTagName(CommonConstants.SOAP_ENV_BODY).item(0);
    Attr idAttr = body.getAttributeNode(CommonConstants.ID);
    body.setIdAttributeNode(idAttr, true);

    KeyStore keyStore =
        getKeyStore(
            security.getCertificate().getDefaultLocation(),
            security.getCertificate().getPassword().toCharArray(),
            false);
    X509Certificate x509Certificate =
        (X509Certificate) keyStore.getCertificate(security.getCertificate().getAlias());
    DOMValidateContext domValidateContext =
        new DOMValidateContext(x509Certificate.getPublicKey(), nodeList.item(0));
    domValidateContext.setProperty(PROPERTY_KEY_SECURE_VALIDATION, Boolean.TRUE);
    XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance(CommonConstants.DOM);

    XMLSignature signature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);
    boolean coreValidity = signature.validate(domValidateContext);

    if (coreValidity) {
      log.info("Signature passed core validation");
    } else {
      log.info("Signature failed core validation");
      boolean statusValidation = signature.getSignatureValue().validate(domValidateContext);
      log.error("Signature validation status: " + statusValidation);

      // check the validation status of each Reference
      Iterator<Reference> references = signature.getSignedInfo().getReferences().iterator();
      for (int j = 0; references.hasNext(); j++) {
        boolean refValid = references.next().validate(domValidateContext);
        log.error("ref[" + j + "] validity status: " + refValid);
      }
    }

    return coreValidity;
  }

  /**
   * Returns {@link SslContext} to make an HTTP connection with the given associated certificates
   *
   * @return {@link SslContext} to make an HTTP connection with the given associated certificates
   * @throws CertificateException
   * @throws KeyStoreException
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws UnrecoverableKeyException
   */
  public SslContext getSslContext()
      throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException,
          UnrecoverableKeyException {
    KeyStore keyStore =
        getKeyStore(
            security.getCertificate().getDefaultLocation(),
            security.getCertificate().getPassword().toCharArray(),
            false);
    Certificate[] certificateChain =
        keyStore.getCertificateChain(security.getCertificate().getAlias());
    X509Certificate[] x509CertificateChain =
        Arrays.stream(certificateChain)
            .map(X509Certificate.class::cast)
            .collect(Collectors.toList())
            .toArray(new X509Certificate[certificateChain.length]);

    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(CommonConstants.SUN_X_509);
    keyManagerFactory.init(keyStore, security.getCertificate().getPassword().toCharArray());
    KeyManager keyManager = keyManagerFactory.getKeyManagers()[0];

    return SslContextBuilder.forClient()
        .trustManager(x509CertificateChain)
        .keyManager(keyManager)
        .build();
  }

  /**
   * Given an XML string, remove white space between tags
   *
   * @param xml {@link String} representation to remove whitespace between tags
   * @return XML string with all white space removed between tags
   */
  private String removeWhitespaceBetweenTags(String xml) {
    return xml.replaceAll(">\\s+<", "><");
  }
}
