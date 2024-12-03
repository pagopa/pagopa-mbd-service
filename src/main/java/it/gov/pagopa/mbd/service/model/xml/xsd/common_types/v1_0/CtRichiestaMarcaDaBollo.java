//
// Questo file è stato generato dall'Eclipse Implementation of JAXB, v3.0.0 
// Vedere https://eclipse-ee4j.github.io/jaxb-ri 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2024.11.21 alle 04:46:32 PM CET 
//


package it.gov.pagopa.mbd.service.model.xml.xsd.common_types.v1_0;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per ctRichiestaMarcaDaBollo complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="ctRichiestaMarcaDaBollo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="hashDocumento" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stBase64Binary72"/&gt;
 *         &lt;element name="tipoBollo" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stTipoBolloDigitale"/&gt;
 *         &lt;element name="provinciaResidenza" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stNazioneProvincia"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctRichiestaMarcaDaBollo", propOrder = {
    "hashDocumento",
    "tipoBollo",
    "provinciaResidenza"
})
public class CtRichiestaMarcaDaBollo {

    @XmlElement(required = true)
    protected byte[] hashDocumento;
    @XmlElement(required = true)
    protected String tipoBollo;
    @XmlElement(required = true)
    protected String provinciaResidenza;

    /**
     * Recupera il valore della proprietà hashDocumento.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getHashDocumento() {
        return hashDocumento;
    }

    /**
     * Imposta il valore della proprietà hashDocumento.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setHashDocumento(byte[] value) {
        this.hashDocumento = value;
    }

    /**
     * Recupera il valore della proprietà tipoBollo.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTipoBollo() {
        return tipoBollo;
    }

    /**
     * Imposta il valore della proprietà tipoBollo.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTipoBollo(String value) {
        this.tipoBollo = value;
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
