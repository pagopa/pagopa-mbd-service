package it.gov.pagopa.mbd.service.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class MarshalerConfig {

  @Bean
  public Jaxb2Marshaller marshaler() {
    Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
    jaxb2Marshaller.setPackagesToScan(
        "it.gov.pagopa.pagopa_api",
        "it.gov.pagopa.pagopa_api.pa",
        "it.gov.agenziaentrate._2014.marcadabollo",
        "org.xmlsoap.schemas.soap.envelope");
    jaxb2Marshaller.setMarshallerProperties(
        Map.of(
            jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE,
            jakarta.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"));
    return jaxb2Marshaller;
  }
}
