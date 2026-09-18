package it.gov.pagopa.mbd.service.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.gov.pagopa.mbd.service.config.MarshalerConfig;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.DemandPaymentNoticeRequest;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.DemandPaymentNoticeResponse;
import it.gov.pagopa.pagopa_api.pa.marcadabollo.TipoMarcaDaBollo;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import jakarta.xml.bind.JAXBElement;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xmlsoap.schemas.soap.envelope.Envelope;

@SpringBootTest(classes = {MarshalerConfig.class, SoapEnvelopeSerializer.class})
class SoapEnvelopeSerializerTest {

  @Autowired private SoapEnvelopeSerializer serializer;

  @Test
  void marshalDemandPaymentNoticeEnvelopeShouldProduceExpectedXml() {
    DemandPaymentNoticeRequest request = new DemandPaymentNoticeRequest();
    request.setIdPSP("1234");
    request.setIdBrokerPSP("5678");
    request.setIdChannel("channel-01");
    request.setPassword("PLACEHOLDER");
    request.setIdSoggettoServizio("mbd-service");
    request.setDatiSpecificiServizio(Base64.getMimeEncoder().encode("payload".getBytes()));

    String xml = serializer.marshalDemandPaymentNoticeEnvelope(request);

    assertNotNull(xml);
    assertTrue(xml.contains("Envelope"));
    assertTrue(xml.contains("Body"));
    assertTrue(xml.contains("demandPaymentNoticeRequest"));
    assertTrue(xml.contains("<idPSP>1234</idPSP>"));
    assertTrue(xml.contains("<idBrokerPSP>5678</idBrokerPSP>"));
    assertTrue(xml.contains("<idChannel>channel-01</idChannel>"));
    assertTrue(xml.contains("<password>PLACEHOLDER</password>"));
    assertTrue(xml.contains("<idSoggettoServizio>mbd-service</idSoggettoServizio>"));
    assertTrue(xml.contains("http://schemas.xmlsoap.org/soap/envelope/"));
    assertTrue(xml.contains("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd"));
  }

  @Test
  void marshalMarcaDaBolloShouldProduceExpectedXml() {
    TipoMarcaDaBollo marcaDaBollo = new TipoMarcaDaBollo();
    marcaDaBollo.setAmount(new BigDecimal("16.00"));
    marcaDaBollo.setFiscalCode("77777777777");
    marcaDaBollo.setProvince("RM");
    marcaDaBollo.setDocumentHash("hash".getBytes(StandardCharsets.UTF_8));

    String xml = serializer.marshalMarcaDaBollo(marcaDaBollo);

    assertNotNull(xml);
    assertTrue(xml.contains("marcaDaBollo"));
    assertTrue(xml.contains("<amount>16.00</amount>"));
    assertTrue(xml.contains("<fiscalCode>77777777777</fiscalCode>"));
    assertTrue(xml.contains("<province>RM</province>"));
    assertTrue(xml.contains("http://pagopa-api.pagopa.gov.it/pa/MarcaDaBollo"));
  }

  @Test
  void unmarshalEnvelopeShouldParseDemandPaymentNoticeResponse() {
    String xml =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="no" ?>
        <soapenv:Envelope
            xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
            xmlns:nfp="http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd">
          <soapenv:Body>
            <nfp:demandPaymentNoticeResponse>
              <outcome>OK</outcome>
              <fiscalCodePA>00493410583</fiscalCodePA>
              <companyName>Test Company</companyName>
            </nfp:demandPaymentNoticeResponse>
          </soapenv:Body>
        </soapenv:Envelope>
        """;

    Envelope envelope = serializer.unmarshalEnvelope(xml);

    assertNotNull(envelope);
    assertNotNull(envelope.getBody());
    assertEquals(1, envelope.getBody().getAny().size());

    Object first = envelope.getBody().getAny().get(0);
    JAXBElement<?> jaxbElement = assertInstanceOf(JAXBElement.class, first);
    DemandPaymentNoticeResponse response =
        assertInstanceOf(DemandPaymentNoticeResponse.class, jaxbElement.getValue());
    assertEquals(StOutcome.OK, response.getOutcome());
    assertEquals("00493410583", response.getFiscalCodePA());
    assertEquals("Test Company", response.getCompanyName());
  }

  @Test
  void marshalAndUnmarshalRoundTripShouldPreserveEnvelopeStructure() {
    DemandPaymentNoticeRequest request = new DemandPaymentNoticeRequest();
    request.setIdPSP("psp");
    request.setIdBrokerPSP("broker");
    request.setIdChannel("channel");
    request.setPassword("PLACEHOLDER");
    request.setIdSoggettoServizio("svc");
    request.setDatiSpecificiServizio("data".getBytes(StandardCharsets.UTF_8));

    String xml = serializer.marshalDemandPaymentNoticeEnvelope(request);
    Envelope envelope = serializer.unmarshalEnvelope(xml);

    assertNotNull(envelope);
    assertNotNull(envelope.getBody());
    assertEquals(1, envelope.getBody().getAny().size());
    Object first = envelope.getBody().getAny().get(0);
    JAXBElement<?> jaxbElement = assertInstanceOf(JAXBElement.class, first);
    DemandPaymentNoticeRequest parsed =
        assertInstanceOf(DemandPaymentNoticeRequest.class, jaxbElement.getValue());
    assertEquals("psp", parsed.getIdPSP());
    assertEquals("broker", parsed.getIdBrokerPSP());
    assertEquals("channel", parsed.getIdChannel());
    assertEquals("svc", parsed.getIdSoggettoServizio());
  }
}
