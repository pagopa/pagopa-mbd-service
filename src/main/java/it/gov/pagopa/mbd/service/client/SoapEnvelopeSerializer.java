package it.gov.pagopa.mbd.service.client;

import it.gov.pagopa.pagopa_api.node.nodeforpsp.DemandPaymentNoticeRequest;
import it.gov.pagopa.pagopa_api.pa.marcadabollo.TipoMarcaDaBollo;
import jakarta.xml.bind.JAXBElement;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.xmlsoap.schemas.soap.envelope.Body;
import org.xmlsoap.schemas.soap.envelope.Envelope;
import org.xmlsoap.schemas.soap.envelope.ObjectFactory;

/** Centralizes SOAP envelope build/parse operations and JAXB (un)marshalling. */
@Component
public class SoapEnvelopeSerializer {

  private static final ObjectFactory SOAP_OBJECT_FACTORY = new ObjectFactory();
  private static final it.gov.pagopa.pagopa_api.node.nodeforpsp.ObjectFactory NODE_OBJECT_FACTORY =
      new it.gov.pagopa.pagopa_api.node.nodeforpsp.ObjectFactory();
  private static final it.gov.pagopa.pagopa_api.pa.marcadabollo.ObjectFactory MBD_OBJECT_FACTORY =
      new it.gov.pagopa.pagopa_api.pa.marcadabollo.ObjectFactory();

  private final Jaxb2Marshaller jaxb2Marshaller;

  public SoapEnvelopeSerializer(Jaxb2Marshaller jaxb2Marshaller) {
    this.jaxb2Marshaller = jaxb2Marshaller;
  }

  /**
   * Wraps the given {@link DemandPaymentNoticeRequest} in a SOAP {@link Envelope} and marshals it
   * to its XML string representation.
   */
  public String marshalDemandPaymentNoticeEnvelope(DemandPaymentNoticeRequest request) {
    JAXBElement<DemandPaymentNoticeRequest> jaxbRequest =
        NODE_OBJECT_FACTORY.createDemandPaymentNoticeRequest(request);

    Body body = new Body();
    body.getAny().add(jaxbRequest);

    Envelope envelope = new Envelope();
    envelope.setBody(body);

    JAXBElement<Envelope> jaxbEnvelope = SOAP_OBJECT_FACTORY.createEnvelope(envelope);
    return marshalToString(jaxbEnvelope);
  }

  /** Marshals a {@link TipoMarcaDaBollo} to its XML string representation. */
  public String marshalMarcaDaBollo(TipoMarcaDaBollo marcaDaBollo) {
    JAXBElement<TipoMarcaDaBollo> element = MBD_OBJECT_FACTORY.createMarcaDaBollo(marcaDaBollo);
    return marshalToString(element);
  }

  /** Unmarshals a SOAP {@link Envelope} from its XML string representation. */
  public Envelope unmarshalEnvelope(String xml) {
    Object unmarshalled =
        jaxb2Marshaller.unmarshal(
            new StreamSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
    if (unmarshalled instanceof JAXBElement<?> jaxbElement) {
      unmarshalled = jaxbElement.getValue();
    }
    return (Envelope) unmarshalled;
  }

  private String marshalToString(JAXBElement<?> element) {
    StringWriter writer = new StringWriter();
    jaxb2Marshaller.marshal(element, new StreamResult(writer));
    return writer.toString();
  }
}
