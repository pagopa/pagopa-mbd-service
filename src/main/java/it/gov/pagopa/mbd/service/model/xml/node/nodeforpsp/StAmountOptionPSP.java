//
// Questo file è stato generato dall'Eclipse Implementation of JAXB, v3.0.0 
// Vedere https://eclipse-ee4j.github.io/jaxb-ri 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2024.11.21 alle 04:46:32 PM CET 
//


package it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per stAmountOptionPSP.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * <pre>
 * &lt;simpleType name="stAmountOptionPSP"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="EQ"/&gt;
 *     &lt;enumeration value="LS"/&gt;
 *     &lt;enumeration value="GT"/&gt;
 *     &lt;enumeration value="ANY"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "stAmountOptionPSP")
@XmlEnum
public enum StAmountOptionPSP {

    EQ,
    LS,
    GT,
    ANY;

    public String value() {
        return name();
    }

    public static StAmountOptionPSP fromValue(String v) {
        return valueOf(v);
    }

}
