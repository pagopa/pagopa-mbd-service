//
// Questo file è stato generato dall'Eclipse Implementation of JAXB, v3.0.0 
// Vedere https://eclipse-ee4j.github.io/jaxb-ri 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2024.11.28 alle 03:00:54 PM CET 
//


package it.gov.pagopa.mbd.service.model.xml.node.marcaDaBollo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * <p>Classe Java per ctDebitore complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="ctDebitore"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="codiceFiscaleDebitore" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="nomeDebitore" type="{http://PuntoAccessoPSP.spcoop.gov.it/ebollo}stText50"/&gt;
 *         &lt;element name="cognomeDebitore" type="{http://PuntoAccessoPSP.spcoop.gov.it/ebollo}stText50"/&gt;
 *         &lt;element name="emailDebitore" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="provinciaResidenza" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctDebitore", propOrder = {
    "codiceFiscaleDebitore",
    "nomeDebitore",
    "cognomeDebitore",
    "emailDebitore",
    "provinciaResidenza"
})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CtDebitore {

    @XmlElement(required = true)
    protected String codiceFiscaleDebitore;
    @XmlElement(required = true)
    protected String nomeDebitore;
    @XmlElement(required = true)
    protected String cognomeDebitore;
    @XmlElement(required = true)
    protected String emailDebitore;
    @XmlElement(required = true)
    protected String provinciaResidenza;

    /**
     * Recupera il valore della proprietà codiceFiscaleDebitore.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodiceFiscaleDebitore() {
        return codiceFiscaleDebitore;
    }

    /**
     * Imposta il valore della proprietà codiceFiscaleDebitore.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodiceFiscaleDebitore(String value) {
        this.codiceFiscaleDebitore = value;
    }

    /**
     * Recupera il valore della proprietà nomeDebitore.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNomeDebitore() {
        return nomeDebitore;
    }

    /**
     * Imposta il valore della proprietà nomeDebitore.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNomeDebitore(String value) {
        this.nomeDebitore = value;
    }

    /**
     * Recupera il valore della proprietà cognomeDebitore.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCognomeDebitore() {
        return cognomeDebitore;
    }

    /**
     * Imposta il valore della proprietà cognomeDebitore.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCognomeDebitore(String value) {
        this.cognomeDebitore = value;
    }

    /**
     * Recupera il valore della proprietà emailDebitore.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmailDebitore() {
        return emailDebitore;
    }

    /**
     * Imposta il valore della proprietà emailDebitore.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmailDebitore(String value) {
        this.emailDebitore = value;
    }

    /**
     * Recupera il valore della proprietà provinciaResidenza.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProvinciaResidenza() {
        return provinciaResidenza;
    }

    /**
     * Imposta il valore della proprietà provinciaResidenza.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProvinciaResidenza(String value) {
        this.provinciaResidenza = value;
    }

}
