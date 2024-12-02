//
// Questo file è stato generato dall'Eclipse Implementation of JAXB, v3.0.0 
// Vedere https://eclipse-ee4j.github.io/jaxb-ri 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2024.11.27 alle 04:50:09 PM CET
//


package it.gov.pagopa.mbd.service.model.xml.node.soap.envelope;

import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeRequest;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeResponse;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * <p>Classe Java per Body complex type.
 *
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 *
 * <pre>
 * &lt;complexType name="Body"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;any processContents='lax' maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Body", propOrder = {
        "demandPaymentNoticeRequest",
        "demandPaymentNoticeResponse"
})
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Body {

    @XmlElement(name = "demandPaymentNoticeRequest", required = false, namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd")
    protected DemandPaymentNoticeRequest demandPaymentNoticeRequest;

    @XmlElement(name = "demandPaymentNoticeResponse", required = false, namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd")
    protected DemandPaymentNoticeResponse demandPaymentNoticeResponse;

}
