package it.gov.pagopa.mbd.service.mapper;

import it.gov.pagopa.mbd.service.exception.CartMappingException;
import it.gov.pagopa.mbd.service.model.carts.CartPaymentNotice;
import it.gov.pagopa.mbd.service.model.carts.GetCartRequest;
import it.gov.pagopa.mbd.service.model.mdb.PaymentNotice;
import it.gov.pagopa.mbd.service.model.xml.node.marcaDaBollo.CtDebitore;
import it.gov.pagopa.mbd.service.model.xml.node.marcaDaBollo.CtEnteCreditore;
import it.gov.pagopa.mbd.service.model.xml.node.marcaDaBollo.CtHashDocumento;
import it.gov.pagopa.mbd.service.model.xml.node.marcaDaBollo.Ctebollo;
import it.gov.pagopa.mbd.service.model.carts.ReturnUrls;
import it.gov.pagopa.mbd.service.model.mdb.GetMbdRequest;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.CtPaymentOptionDescription;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.CtPaymentOptionsDescriptionList;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeRequest;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeResponse;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringResult;

import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.hibernate.validator.internal.util.Contracts.assertTrue;

public class RequestMapper {

    public static DemandPaymentNoticeRequest mapDemandPaymentNoticeRequest(
            String idPsp, String idBrokerPsp, String idChannel,
            Jaxb2Marshaller marshaller, GetMbdRequest getMdbRequest) {

        PaymentNotice paymentNotice = getMdbRequest.getPaymentNotices().get(0);

        Ctebollo ctMarcaDaBollo =
                Ctebollo.builder()
                        .debitore(CtDebitore.builder()
                                .codiceFiscaleDebitore(paymentNotice.getFiscalCode())
                                .nomeDebitore(paymentNotice.getFirstName())
                                .cognomeDebitore(paymentNotice.getLastName())
                                .emailDebitore(paymentNotice.getEmail())
                                .provinciaResidenza(paymentNotice.getProvince())
                                .build())
                        .enteCreditore(CtEnteCreditore.builder()
                                        .codiceFiscaleEnte(paymentNotice.getFiscalCodeEC())
                                        .build())
                        .hashDocumento(CtHashDocumento.builder()
                                .hashDocumento(paymentNotice.getDocumentHash())
                                .build())
                        .build();

        StringResult sw = new StringResult();
        marshaller.marshal(ctMarcaDaBollo, sw);

        return DemandPaymentNoticeRequest.builder()
                .idPSP(idPsp)
                .idBrokerPSP(idBrokerPsp)
                .idChannel(idChannel)
                .idSoggettoServizio(getMdbRequest.getIdCIService())
                .password("")
                .datiSpecificiServizio(Base64.getMimeEncoder().encode(sw.toString().getBytes()))
                .build();
    }

    public static GetCartRequest mapCartRequest(
            GetMbdRequest request, DemandPaymentNoticeResponse demandPaymentNoticeResponse) {
        try {
            assertNotNull(demandPaymentNoticeResponse);
            CtPaymentOptionsDescriptionList ctPaymentOptionsDescriptionList = demandPaymentNoticeResponse.getPaymentList();
            assertNotNull(ctPaymentOptionsDescriptionList);
            List<CtPaymentOptionDescription> ctPaymentOptionsDescriptions =
                    ctPaymentOptionsDescriptionList.getPaymentOptionDescription();
            assertNotNull(ctPaymentOptionsDescriptions);
            assertTrue(!ctPaymentOptionsDescriptions.isEmpty(), "Missing PaymentOption");
            CtPaymentOptionDescription ctPaymentOptionDescription = ctPaymentOptionsDescriptions.get(0);
            assertNotNull(demandPaymentNoticeResponse.getQrCode());
            return GetCartRequest.builder()
                    .emailNotice(request.getPaymentNotices().get(0).getEmail())
                    .returnUrls(ReturnUrls.builder()
                            .cancelUrl(request.getReturnUrls().getCancelUrl())
                            .errorUrl(request.getReturnUrls().getErrorUrl())
                            .successUrl(request.getReturnUrls().getSuccessUrl())
                            .build())
                    .paymentNotices(Collections.singletonList(
                            CartPaymentNotice.builder()
                                    .fiscalCode(demandPaymentNoticeResponse.getQrCode().getFiscalCode())
                                    .amount(ctPaymentOptionDescription.getAmount().toBigIntegerExact().longValue())
                                    .companyName(demandPaymentNoticeResponse.getOfficeName())
                                    .description(demandPaymentNoticeResponse.getPaymentDescription())
                                    .noticeNumber(demandPaymentNoticeResponse.getQrCode().getNoticeNumber())
                                    .build()
                    ))
                    .build();
        } catch (Exception e) {
            throw new CartMappingException(e.getMessage(), e);
        }
    }

}
