package it.gov.pagopa.mbd.service.exception;

import it.gov.pagopa.pagopa_api.node.nodeforpsp.DemandPaymentNoticeResponse;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import org.xmlsoap.schemas.soap.envelope.Body;

public class DemandPaymentNoticeKOException extends RuntimeException {

  public DemandPaymentNoticeKOException(String message) {
    super(message);
  }

  public DemandPaymentNoticeKOException(String message, Body body) {
    super(buildMessage(message, body));
  }

  private static String buildMessage(String message, Body body) {
    String faultInfo = "";
    if (body != null && body.getAny() != null && !body.getAny().isEmpty()) {
      DemandPaymentNoticeResponse response = ((DemandPaymentNoticeResponse) body.getAny().get(0));
      if (response != null && response.getFault() != null) {
        CtFaultBean fault = response.getFault();
        faultInfo = fault.getFaultCode() + " - " + fault.getDescription();
      }
    }
    return message + " " + faultInfo;
  }
}
