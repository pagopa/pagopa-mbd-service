//
// Questo file è stato generato dall'Eclipse Implementation of JAXB, v3.0.0 
// Vedere https://eclipse-ee4j.github.io/jaxb-ri 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2024.11.28 alle 03:00:54 PM CET 
//


package it.gov.pagopa.mbd.service.model.xml.node.marcaDaBollo;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;

import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the it.gov.spcoop.puntoaccessopsp.ebollo package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Service_QNAME = new QName("http://PuntoAccessoPSP.spcoop.gov.it/ebollo", "service");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: it.gov.spcoop.puntoaccessopsp.ebollo
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Ctebollo }
     * 
     */
    public Ctebollo createCtebollo() {
        return new Ctebollo();
    }

    /**
     * Create an instance of {@link CtDebitore }
     * 
     */
    public CtDebitore createCtDebitore() {
        return new CtDebitore();
    }

    /**
     * Create an instance of {@link CtEnteCreditore }
     * 
     */
    public CtEnteCreditore createCtEnteCreditore() {
        return new CtEnteCreditore();
    }

    /**
     * Create an instance of {@link CtHashDocumento }
     * 
     */
    public CtHashDocumento createCtHashDocumento() {
        return new CtHashDocumento();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Ctebollo }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Ctebollo }{@code >}
     */
    @XmlElementDecl(namespace = "http://PuntoAccessoPSP.spcoop.gov.it/ebollo", name = "service")
    public JAXBElement<Ctebollo> createService(Ctebollo value) {
        return new JAXBElement<Ctebollo>(_Service_QNAME, Ctebollo.class, null, value);
    }

}
