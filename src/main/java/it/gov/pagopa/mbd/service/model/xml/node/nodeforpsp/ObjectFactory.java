//
// Questo file è stato generato dall'Eclipse Implementation of JAXB, v3.0.0 
// Vedere https://eclipse-ee4j.github.io/jaxb-ri 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2024.11.21 alle 04:46:32 PM CET 
//


package it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp;

import javax.xml.namespace.QName;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the it.gov.pagopa.pagopa_api.node.nodeforpsp package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Envelope_QNAME = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
    private final static QName _Header_QNAME = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Header");
    private final static QName _Body_QNAME = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Body");
    private final static QName _Fault_QNAME = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Fault");
    private final static QName _DemandPaymentNoticeRequest_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "demandPaymentNoticeRequest");
    private final static QName _DemandPaymentNoticeResponse_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "demandPaymentNoticeResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: it.gov.pagopa.pagopa_api.node.nodeforpsp
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DemandPaymentNoticeRequest }
     * 
     */
    public DemandPaymentNoticeRequest createDemandPaymentNoticeRequest() {
        return new DemandPaymentNoticeRequest();
    }

    /**
     * Create an instance of {@link DemandPaymentNoticeResponse }
     * 
     */
    public DemandPaymentNoticeResponse createDemandPaymentNoticeResponse() {
        return new DemandPaymentNoticeResponse();
    }

    /**
     * Create an instance of {@link FaultBean }
     * 
     */
    public FaultBean createFaultBean() {
        return new FaultBean();
    }

    /**
     * Create an instance of {@link CtQrCode }
     * 
     */
    public CtQrCode createCtQrCode() {
        return new CtQrCode();
    }

    /**
     * Create an instance of {@link CtPaymentOptionDescription }
     * 
     */
    public CtPaymentOptionDescription createCtPaymentOptionDescription() {
        return new CtPaymentOptionDescription();
    }

    /**
     * Create an instance of {@link CtPaymentOptionsDescriptionList }
     * 
     */
    public CtPaymentOptionsDescriptionList createCtPaymentOptionsDescriptionList() {
        return new CtPaymentOptionsDescriptionList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DemandPaymentNoticeRequest }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link DemandPaymentNoticeRequest }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "demandPaymentNoticeRequest")
    public JAXBElement<DemandPaymentNoticeRequest> createDemandPaymentNoticeRequest(DemandPaymentNoticeRequest value) {
        return new JAXBElement<DemandPaymentNoticeRequest>(_DemandPaymentNoticeRequest_QNAME, DemandPaymentNoticeRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DemandPaymentNoticeResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link DemandPaymentNoticeResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "demandPaymentNoticeResponse")
    public JAXBElement<DemandPaymentNoticeResponse> createDemandPaymentNoticeResponse(DemandPaymentNoticeResponse value) {
        return new JAXBElement<DemandPaymentNoticeResponse>(_DemandPaymentNoticeResponse_QNAME, DemandPaymentNoticeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.Envelope }{@code >}
     *
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.Envelope }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/soap/envelope/", name = "Envelope")
    public JAXBElement<it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.Envelope> createEnvelope(it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.Envelope value) {
        return new JAXBElement<it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.Envelope>(_Envelope_QNAME, Envelope.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.Header }{@code >}
     *
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.Header }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/soap/envelope/", name = "Header")
    public JAXBElement<it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.Header> createHeader(it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.Header value) {
        return new JAXBElement<it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.Header>(_Header_QNAME, Header.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.Body }{@code >}
     *
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.Body }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/soap/envelope/", name = "Body")
    public JAXBElement<it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.Body> createBody(it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.Body value) {
        return new JAXBElement<>(_Body_QNAME, Body.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.Fault }{@code >}
     *
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.Fault }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/soap/envelope/", name = "Fault")
    public JAXBElement<it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.Fault> createFault(it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.Fault value) {
        return new JAXBElement<>(_Fault_QNAME, Fault.class, null, value);
    }

}
