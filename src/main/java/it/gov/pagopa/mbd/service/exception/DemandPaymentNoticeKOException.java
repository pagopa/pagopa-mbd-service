package it.gov.pagopa.mbd.service.exception;

import it.gov.pagopa.pagopa_api.node.nodeforpsp.DemandPaymentNoticeResponse;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;

public class DemandPaymentNoticeKOException extends RuntimeException {

  public DemandPaymentNoticeKOException(String message) {
    super(message);
  }

  public DemandPaymentNoticeKOException(String message, DemandPaymentNoticeResponse response) {
    super(buildMessage(message, response));
  }

  private static String buildMessage(String message, DemandPaymentNoticeResponse response) {
    String faultInfo = "";
    if (response != null && response.getFault() != null) {
      CtFaultBean fault = response.getFault();
      faultInfo = fault.getFaultCode() + " - " + fault.getDescription();
    }
    return message + " " + faultInfo;
  }
}
