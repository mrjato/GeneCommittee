/*
	This file is part of GeneCommittee.

	GeneCommittee is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	GeneCommittee is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with GeneCommittee.  If not, see <http://www.gnu.org/licenses/>.
*/
package es.uvigo.ei.sing.gc.ws;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.cxf.xjc.runtime.JAXBToStringStyle;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="specieName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="genelist" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "specieName",
    "genelist"
})
@XmlRootElement(name = "process")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2013-05-02T06:57:14+02:00", comments = "JAXB RI v2.2.6")
public class Process {
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2013-05-02T06:57:14+02:00", comments = "JAXB RI v2.2.6")
    protected String specieName;
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2013-05-02T06:57:14+02:00", comments = "JAXB RI v2.2.6")
    protected String genelist;

    /**
     * Gets the value of the specieName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2013-05-02T06:57:14+02:00", comments = "JAXB RI v2.2.6")
    public String getSpecieName() {
        return specieName;
    }

    /**
     * Sets the value of the specieName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2013-05-02T06:57:14+02:00", comments = "JAXB RI v2.2.6")
    public void setSpecieName(String value) {
        this.specieName = value;
    }

    /**
     * Gets the value of the genelist property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2013-05-02T06:57:14+02:00", comments = "JAXB RI v2.2.6")
    public String getGenelist() {
        return genelist;
    }

    /**
     * Sets the value of the genelist property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2013-05-02T06:57:14+02:00", comments = "JAXB RI v2.2.6")
    public void setGenelist(String value) {
        this.genelist = value;
    }

    /**
     * Generates a String representation of the contents of this type.
     * This is an extension method, produced by the 'ts' xjc plugin
     * 
     */
    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2013-05-02T06:57:14+02:00", comments = "JAXB RI v2.2.6")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, JAXBToStringStyle.MULTI_LINE_STYLE);
    }

}
