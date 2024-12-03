//
// Questo file è stato generato dall'Eclipse Implementation of JAXB, v3.0.0 
// Vedere https://eclipse-ee4j.github.io/jaxb-ri 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2024.11.28 alle 03:55:01 PM CET 
//


package it.gov.pagopa.mbd.service.model.xml.node.soap.envelope;

import jakarta.xml.bind.annotation.*;

import javax.xml.namespace.QName;


/**
 * <p>Classe Java per Fault complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="Fault"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="faultcode" type="{http://www.w3.org/2001/XMLSchema}QName"/&gt;
 *         &lt;element name="faultstring" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="faultactor" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/&gt;
 *         &lt;element name="detail" type="{http://schemas.xmlsoap.org/soap/envelope/}detail" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Fault", propOrder = {
    "faultcode",
    "faultstring",
    "faultactor",
    "detail"
})
public class Fault {

    @XmlElement(required = true)
    protected QName faultcode;
    @XmlElement(required = true)
    protected String faultstring;
    @XmlSchemaType(name = "anyURI")
    protected String faultactor;
    protected Detail detail;

    /**
     * Recupera il valore della proprietà faultcode.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getFaultcode() {
        return faultcode;
    }

    /**
     * Imposta il valore della proprietà faultcode.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setFaultcode(QName value) {
        this.faultcode = value;
    }

    /**
     * Recupera il valore della proprietà faultstring.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFaultstring() {
        return faultstring;
    }

    /**
     * Imposta il valore della proprietà faultstring.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFaultstring(String value) {
        this.faultstring = value;
    }

    /**
     * Recupera il valore della proprietà faultactor.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFaultactor() {
        return faultactor;
    }

    /**
     * Imposta il valore della proprietà faultactor.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFaultactor(String value) {
        this.faultactor = value;
    }

    /**
     * Recupera il valore della proprietà detail.
     * 
     * @return
     *     possible object is
     *     {@link Detail }
     *     
     */
    public Detail getDetail() {
        return detail;
    }

    /**
     * Imposta il valore della proprietà detail.
     * 
     * @param value
     *     allowed object is
     *     {@link Detail }
     *     
     */
    public void setDetail(Detail value) {
        this.detail = value;
    }

}
