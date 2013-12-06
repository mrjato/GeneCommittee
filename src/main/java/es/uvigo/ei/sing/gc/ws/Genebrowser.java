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

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

@WebServiceClient(
	name = "genebrowser", 
	wsdlLocation = "http://bioinformatics.ua.pt/genebrowser2/genebrowser.asmx?WSDL",
	targetNamespace = "gb2.ua.pt"
) 
public class Genebrowser extends Service {
    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("gb2.ua.pt", "genebrowser");
    public final static QName SOAP = new QName("gb2.ua.pt", "genebrowserSoap");
    public final static QName SOAP12 = new QName("gb2.ua.pt", "genebrowserSoap12");
    
    static {
        URL url = null;
        try {
            url = new URL("http://bioinformatics.ua.pt/genebrowser2/genebrowser.asmx?WSDL");
        } catch (MalformedURLException e) {
        	e.printStackTrace();
        }
        
        WSDL_LOCATION = url;
    }

    public Genebrowser(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public Genebrowser(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public Genebrowser() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    @WebEndpoint(name = "genebrowserSoap")
    public GenebrowserSoap getGenebrowserSoap() {
        return super.getPort(SOAP, GenebrowserSoap.class);
    }

    @WebEndpoint(name = "genebrowserSoap")
    public GenebrowserSoap getGenebrowserSoap(WebServiceFeature... features) {
        return super.getPort(SOAP, GenebrowserSoap.class, features);
    }

    @WebEndpoint(name = "genebrowserSoap12")
    public GenebrowserSoap getGenebrowserSoap12() {
        return super.getPort(SOAP12, GenebrowserSoap.class);
    }

    @WebEndpoint(name = "genebrowserSoap12")
    public GenebrowserSoap getGenebrowserSoap12(WebServiceFeature... features) {
        return super.getPort(SOAP12, GenebrowserSoap.class, features);
    }

}
