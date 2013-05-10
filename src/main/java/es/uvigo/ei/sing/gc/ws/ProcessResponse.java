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
 *         &lt;element name="processResult" type="{gb2.ua.pt}ArrayOfString" minOccurs="0"/>
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
    "processResult"
})
@XmlRootElement(name = "processResponse")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2013-05-02T06:57:14+02:00", comments = "JAXB RI v2.2.6")
public class ProcessResponse {

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2013-05-02T06:57:14+02:00", comments = "JAXB RI v2.2.6")
    protected ArrayOfString processResult;

    /**
     * Gets the value of the processResult property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfString }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2013-05-02T06:57:14+02:00", comments = "JAXB RI v2.2.6")
    public ArrayOfString getProcessResult() {
        return processResult;
    }

    /**
     * Sets the value of the processResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfString }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2013-05-02T06:57:14+02:00", comments = "JAXB RI v2.2.6")
    public void setProcessResult(ArrayOfString value) {
        this.processResult = value;
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
