package it.gov.pagopa.mbd.service.exception;

import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeResponse;
import it.gov.pagopa.mbd.service.model.xml.node.soap.envelope.Body;
import it.gov.pagopa.mbd.service.model.xml.xsd.common_types.v1_0.CtFaultBean;

public class DemandPaymentNoticeKOException extends RuntimeException {

  public DemandPaymentNoticeKOException(String message) {
    super(message);
  }

  public DemandPaymentNoticeKOException(String message, Body body) {
    super(buildMessage(message, body));
  }

  private static String buildMessage(String message, Body body) {
    String faultInfo = "";
    if (body != null) {
      DemandPaymentNoticeResponse response = body.getDemandPaymentNoticeResponse();
      if (response != null && response.getFault() != null) {
        CtFaultBean fault = response.getFault();
        faultInfo = fault.getFaultCode() + " - " + fault.getDescription();
      }
    }
    return message + " " + faultInfo;
  }
}
