//
// Questo file è stato generato dall'Eclipse Implementation of JAXB, v3.0.0 
// Vedere https://eclipse-ee4j.github.io/jaxb-ri 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2024.11.28 alle 03:00:54 PM CET 
//


package it.gov.pagopa.mbd.service.model.xml.node.marcaDaBollo;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * <p>Classe Java per ctebollo complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="ctebollo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="debitore" type="{http://PuntoAccessoPSP.spcoop.gov.it/ebollo}ctDebitore"/&gt;
 *         &lt;element name="enteCreditore" type="{http://PuntoAccessoPSP.spcoop.gov.it/ebollo}ctEnteCreditore"/&gt;
 *         &lt;element name="hashDocumento" type="{http://PuntoAccessoPSP.spcoop.gov.it/ebollo}ctHashDocumento"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctebollo", propOrder = {
    "debitore",
    "enteCreditore",
    "hashDocumento"
})
@XmlRootElement
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Ctebollo {

    @XmlElement(required = true)
    protected CtDebitore debitore;
    @XmlElement(required = true)
    protected CtEnteCreditore enteCreditore;
    @XmlElement(required = true)
    protected CtHashDocumento hashDocumento;

    /**
     * Recupera il valore della proprietà debitore.
     * 
     * @return
     *     possible object is
     *     {@link CtDebitore }
     *     
     */
    public CtDebitore getDebitore() {
        return debitore;
    }

    /**
     * Imposta il valore della proprietà debitore.
     * 
     * @param value
     *     allowed object is
     *     {@link CtDebitore }
     *     
     */
    public void setDebitore(CtDebitore value) {
        this.debitore = value;
    }

    /**
     * Recupera il valore della proprietà enteCreditore.
     * 
     * @return
     *     possible object is
     *     {@link CtEnteCreditore }
     *     
     */
    public CtEnteCreditore getEnteCreditore() {
        return enteCreditore;
    }

    /**
     * Imposta il valore della proprietà enteCreditore.
     * 
     * @param value
     *     allowed object is
     *     {@link CtEnteCreditore }
     *     
     */
    public void setEnteCreditore(CtEnteCreditore value) {
        this.enteCreditore = value;
    }

    /**
     * Recupera il valore della proprietà hashDocumento.
     * 
     * @return
     *     possible object is
     *     {@link CtHashDocumento }
     *     
     */
    public CtHashDocumento getHashDocumento() {
        return hashDocumento;
    }

    /**
     * Imposta il valore della proprietà hashDocumento.
     * 
     * @param value
     *     allowed object is
     *     {@link CtHashDocumento }
     *     
     */
    public void setHashDocumento(CtHashDocumento value) {
        this.hashDocumento = value;
    }

}
