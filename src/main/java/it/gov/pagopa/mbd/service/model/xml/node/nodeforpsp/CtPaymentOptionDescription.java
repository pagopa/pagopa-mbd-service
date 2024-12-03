//
// Questo file è stato generato dall'Eclipse Implementation of JAXB, v3.0.0 
// Vedere https://eclipse-ee4j.github.io/jaxb-ri 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2024.11.21 alle 04:46:32 PM CET 
//


package it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp;

import java.math.BigDecimal;
import javax.xml.datatype.XMLGregorianCalendar;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * <p>Classe Java per ctPaymentOptionDescription complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="ctPaymentOptionDescription"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="amount" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stAmountNotZero"/&gt;
 *         &lt;element name="options" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stAmountOptionPSP"/&gt;
 *         &lt;element name="dueDate" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stISODate" minOccurs="0"/&gt;
 *         &lt;element name="paymentNote" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stText210" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctPaymentOptionDescription", propOrder = {
    "amount",
    "options",
    "dueDate",
    "paymentNote"
})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CtPaymentOptionDescription {

    @XmlElement(required = true)
    protected BigDecimal amount;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected StAmountOptionPSP options;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar dueDate;
    protected String paymentNote;

    /**
     * Recupera il valore della proprietà amount.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Imposta il valore della proprietà amount.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAmount(BigDecimal value) {
        this.amount = value;
    }

    /**
     * Recupera il valore della proprietà options.
     * 
     * @return
     *     possible object is
     *     {@link StAmountOptionPSP }
     *     
     */
    public StAmountOptionPSP getOptions() {
        return options;
    }

    /**
     * Imposta il valore della proprietà options.
     * 
     * @param value
     *     allowed object is
     *     {@link StAmountOptionPSP }
     *     
     */
    public void setOptions(StAmountOptionPSP value) {
        this.options = value;
    }

    /**
     * Recupera il valore della proprietà dueDate.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDueDate() {
        return dueDate;
    }

    /**
     * Imposta il valore della proprietà dueDate.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDueDate(XMLGregorianCalendar value) {
        this.dueDate = value;
    }

    /**
     * Recupera il valore della proprietà paymentNote.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPaymentNote() {
        return paymentNote;
    }

    /**
     * Imposta il valore della proprietà paymentNote.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPaymentNote(String value) {
        this.paymentNote = value;
    }

}
