package org.opengis.cite.iso19142.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.iso19142.Namespaces;
import org.opengis.cite.iso19142.WFS2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the WFSRequest class.
 */
public class VerifyWFSRequest {

    private static final String NS1 = "http://example.org/ns1";
    private static final String NS2 = "http://example.org/ns2";
    private static DocumentBuilder docBuilder;

    public VerifyWFSRequest() {
    }

    @BeforeClass
    public static void setUpClass() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
    }

    @Test
    public void transformGetCapabilitiesToKVP() {
        InputStream inStream = getClass().getResourceAsStream(
                "/GetCapabilities-AcceptSections.xml");
        String kvp = WFSRequest
                .transformEntityToKVP(new StreamSource(inStream));
        assertTrue("Expected result to contain 'acceptversions=2.0.0,1.1.0'",
                kvp.contains("acceptversions=2.0.0,1.1.0"));
        assertTrue(
                "Expected result to contain 'sections=ServiceIdentification'",
                kvp.contains("sections=ServiceIdentification"));
    }

    @Test
    public void transformGetFeatureBBOXToKVP() {
        InputStream inStream = getClass().getResourceAsStream(
                "/GetFeature/GetFeature-BBOX.xml");
        String kvp = WFSRequest
                .transformEntityToKVP(new StreamSource(inStream));
        // expect <fes:BBOX> to be percent-encoded
        assertTrue("Expected result to contain '%3Cfes%3ABBOX%3E'",
                kvp.contains("%3Cfes%3ABBOX%3E"));
    }

    @Test
    public void transformStoredQueryToKVP() {
        InputStream inStream = getClass().getResourceAsStream(
                "/GetFeature/GetFeatureById.xml");
        String kvp = WFSRequest
                .transformEntityToKVP(new StreamSource(inStream));
        assertTrue(
                "Expected result to contain 'storedquery_id=urn:ogc:def:query:OGC-WFS::GetFeatureById'",
                kvp.contains("storedquery_id=urn:ogc:def:query:OGC-WFS::GetFeatureById"));
        assertTrue("Expected result to contain 'id=id-1'",
                kvp.contains("id=id-1"));
        assertTrue("Expected result to contain 'count=10'",
                kvp.contains("count=10"));
    }

    @Test
    public void transformGetFeatureQuery2TypesToKVP() {
        InputStream inStream = getClass().getResourceAsStream(
                "/GetFeature/GetFeature-Query2Types.xml");
        String kvp = WFSRequest
                .transformEntityToKVP(new StreamSource(inStream));
        assertTrue(
                "Expected result to contain 'typenames=tns:PrimitiveGeoFeature,tns:AggregateGeoFeature'",
                kvp.contains("typenames=tns:PrimitiveGeoFeature,tns:AggregateGeoFeature"));
        assertTrue(
                "Expected result to contain 'xmlns(tns,http://cite.opengeospatial.org/gmlsf)'",
                kvp.contains("xmlns(tns,http://cite.opengeospatial.org/gmlsf)"));
    }

    @Test
    public void transformDescribeFeatureTypeToKVP() {
        InputStream inStream = getClass().getResourceAsStream(
                "/DescribeFeatureType.xml");
        String kvp = WFSRequest
                .transformEntityToKVP(new StreamSource(inStream));
        assertTrue("Expected result to contain 'version=2.0.0'",
                kvp.contains("version=2.0.0"));
        assertTrue(
                "Expected result to contain 'typenames=tns:ComplexGeoFeature,tns:AggregateGeoFeature'",
                kvp.contains("typenames=tns:ComplexGeoFeature,tns:AggregateGeoFeature"));
        assertTrue(
                "Expected result to contain 'xmlns(tns,http://cite.opengeospatial.org/gmlsf)'",
                kvp.contains("xmlns(tns,http://cite.opengeospatial.org/gmlsf)"));
    }

    @Test
    public void transformDescribeStoredQueriesToKVP() {
        InputStream inStream = getClass().getResourceAsStream(
                "/DescribeStoredQueries.xml");
        String kvp = WFSRequest
                .transformEntityToKVP(new StreamSource(inStream));
        assertTrue(
                "Expected result to contain 'request=DescribeStoredQueries'",
                kvp.contains("request=DescribeStoredQueries"));
        assertTrue(
                "Expected result to contain 'storedquery_id=urn:ogc:def:query:OGC-WFS::GetFeatureById,urn:ogc:def:query:OGC-WFS::GetFeatureByType'",
                kvp.contains("storedquery_id=urn:ogc:def:query:OGC-WFS::GetFeatureById,urn:ogc:def:query:OGC-WFS::GetFeatureByType"));
    }

    @Test
    public void wrapGetFeatureRequestInSOAP11Envelope() {
        InputStream inStream = getClass().getResourceAsStream(
                "/GetFeature/GetFeature-Query2Types.xml");
        Document soapDoc = WFSRequest.wrapEntityInSOAPEnvelope(
                new StreamSource(inStream), WFS2.SOAP_VERSION);
        assertEquals("Document element has unexpected namespace.",
                Namespaces.SOAP11, soapDoc.getDocumentElement()
                        .getNamespaceURI());
        NodeList nodes = soapDoc.getElementsByTagNameNS(Namespaces.WFS,
                WFS2.GET_FEATURE);
        assertEquals("Unexpected number of wfs:GetFeature nodes.", 1,
                nodes.getLength());
    }

    @Test
    public void wrapRequestInSOAP12Envelope() {
        InputStream inStream = getClass().getResourceAsStream(
                "/GetFeature/GetFeature-Query2Types.xml");
        Document soapDoc = WFSRequest.wrapEntityInSOAPEnvelope(
                new StreamSource(inStream), null);
        assertEquals("Document element has unexpected namespace.",
                Namespaces.SOAP_ENV, soapDoc.getDocumentElement()
                        .getNamespaceURI());
    }

    @Test
    public void setStoredQueryParameterAsQName() {
        Document doc = WFSRequest.createRequestEntity("GetFeature");
        QName qName = new QName("http://example.org/ns1", "Alpha");
        WFSRequest.appendStoredQuery(doc, WFS2.QRY_GET_FEATURE_BY_TYPE,
                Collections.singletonMap("typeName", (Object) qName));
        Element param = (Element) doc.getElementsByTagNameNS(Namespaces.WFS,
                WFS2.PARAM_ELEM).item(0);
        assertEquals("Unexpected parameter value.", "tns:Alpha",
                param.getTextContent());
    }

    @Test
    public void setStoredQueryParameterAsString() throws SAXException,
            IOException {
        Document doc = WFSRequest.createRequestEntity("GetFeature");
        WFSRequest.appendStoredQuery(doc, "q1",
                Collections.singletonMap("p1", (Object) "v1"));
        Element param = (Element) doc.getElementsByTagNameNS(Namespaces.WFS,
                WFS2.PARAM_ELEM).item(0);
        assertEquals("Unexpected parameter name.", "p1",
                param.getAttribute("name"));
        assertEquals("Unexpected parameter value.", "v1",
                param.getTextContent());
    }

    @Test
    public void createResourceIdFilter() {
        String identifier = "alpha";
        Element result = WFSRequest.newResourceIdFilter(identifier);
        assertEquals("Unexpected element name.", "Filter",
                result.getLocalName());
        Element resourceId = (Element) result.getElementsByTagNameNS(
                Namespaces.FES, "ResourceId").item(0);
        assertEquals("Unexpected rid value.", identifier,
                resourceId.getAttribute("rid"));
    }

    @Test
    public void insertGMLIdentifier_featureWithoutGMLProps()
            throws SAXException, IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/Gamma.xml"));
        QName propName = new QName(Namespaces.GML, "identifier");
        Element identifier = XMLUtils.createElement(propName);
        identifier.setAttribute("codeSpace", "http://cite.opengeospatial.org/");
        String uuid = UUID.randomUUID().toString();
        identifier.setTextContent(uuid);
        WFSRequest.insertGMLProperty(doc.getDocumentElement(), identifier);
        Element gmlIdentifier = (Element) doc.getElementsByTagNameNS(
                Namespaces.GML, "identifier").item(0);
        assertEquals("Unexpected gml:identifier value.", uuid,
                gmlIdentifier.getTextContent());
    }

    @Test
    public void insertGMLIdentifier_featureWithGMLProps() throws SAXException,
            IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/Alpha-1.xml"));
        QName propName = new QName(Namespaces.GML, "identifier");
        Element identifier = XMLUtils.createElement(propName);
        identifier.setAttribute("codeSpace", "http://cite.opengeospatial.org/");
        String uuid = UUID.randomUUID().toString();
        identifier.setTextContent(uuid);
        WFSRequest.insertGMLProperty(doc.getDocumentElement(), identifier);
        Element gmlIdentifier = (Element) doc.getElementsByTagNameNS(
                Namespaces.GML, "identifier").item(0);
        assertEquals("Unexpected gml:identifier value.", uuid,
                gmlIdentifier.getTextContent());
        assertEquals("Unexpected name of next sibling.", "name", gmlIdentifier
                .getNextSibling().getLocalName());
    }

    @Test
    public void replaceGmlName() throws SAXException, IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/Alpha-1.xml"));
        QName propName = new QName(Namespaces.GML, "name");
        Element name = XMLUtils.createElement(propName);
        name.setAttribute("codeSpace", "http://cite.opengeospatial.org/");
        String newName = "New name";
        name.setTextContent(newName);
        WFSRequest.insertGMLProperty(doc.getDocumentElement(), name);
        Element gmlName = (Element) doc.getElementsByTagNameNS(Namespaces.GML,
                "name").item(0);
        assertEquals("Unexpected gml:name value.", newName,
                gmlName.getTextContent());
    }

    @Test
    public void append2QueryElementsToGetFeature() throws SAXException,
            IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/GetFeature/GetFeature-Minimal.xml"));
        QName typeName1 = new QName(NS1, "Type1");
        WFSRequest.appendSimpleQuery(doc, typeName1);
        QName typeName2 = new QName(NS2, "Type2");
        WFSRequest.appendSimpleQuery(doc, typeName2);
        NodeList queries = doc.getElementsByTagNameNS(Namespaces.WFS,
                WFS2.QUERY_ELEM);
        assertEquals("Unexpected number of wfs:Query elements.", 2,
                queries.getLength());
        Element query2 = (Element) queries.item(1);
        assertTrue(
                "Expected Query[2]/@typeNames to end with "
                        + typeName2.getLocalPart(),
                query2.getAttribute("typeNames").endsWith(
                        typeName2.getLocalPart()));
    }

    @Test
    public void appendStoredQuery() throws SAXException, IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/GetFeature/GetFeature-Minimal.xml"));
        QName typeName1 = new QName(NS1, "Type1");
        WFSRequest.appendStoredQuery(doc, WFS2.QRY_GET_FEATURE_BY_TYPE,
                Collections.singletonMap("typeName", (Object) typeName1));
        NodeList queries = doc.getElementsByTagNameNS(Namespaces.WFS,
                WFS2.STORED_QRY_ELEM);
        assertEquals("Unexpected number of query expressions.", 1,
                queries.getLength());
        Element query = (Element) queries.item(0);
        assertEquals("Unexpected query id.", WFS2.QRY_GET_FEATURE_BY_TYPE,
                query.getAttribute("id"));
    }

    @Test
    public void appendQueryToLockFeatureRequest() throws SAXException,
            IOException {
        Document req = docBuilder.parse(this.getClass().getResourceAsStream(
                "/LockFeature-Empty.xml"));
        QName typeName = new QName(NS1, "River");
        WFSRequest.appendSimpleQuery(req, typeName);
        NodeList queries = req.getElementsByTagNameNS(Namespaces.WFS,
                WFS2.QUERY_ELEM);
        assertEquals("Unexpected number of wfs:Query elements.", 1,
                queries.getLength());
        Element query = (Element) queries.item(0);
        assertTrue(
                "Expected Query/@typeNames to end with "
                        + typeName.getLocalPart(),
                query.getAttribute("typeNames").endsWith(
                        typeName.getLocalPart()));
    }
}
