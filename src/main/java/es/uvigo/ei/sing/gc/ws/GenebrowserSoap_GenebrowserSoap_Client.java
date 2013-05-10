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

/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

/**
 * This class was generated by Apache CXF 2.7.4 2013-05-02T18:57:14.872+02:00
 * Generated source version: 2.7.4
 * 
 */
public final class GenebrowserSoap_GenebrowserSoap_Client {
	private static final QName SERVICE_NAME = new QName("gb2.ua.pt",
			"genebrowser");

	private GenebrowserSoap_GenebrowserSoap_Client() {
	}

	public static ArrayOfString query(String specieName, String geneList) {
		final Genebrowser ss = new Genebrowser(Genebrowser.WSDL_LOCATION,
				GenebrowserSoap_GenebrowserSoap_Client.SERVICE_NAME);
		final GenebrowserSoap port = ss.getGenebrowserSoap();

		((BindingProvider) port).getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				Genebrowser.WSDL_LOCATION.toExternalForm());

		((BindingProvider) port).getRequestContext().put(
				"com.sun.xml.internal.ws.connect.timeout", 300000);
		((BindingProvider) port).getRequestContext().put(
				"com.sun.xml.internal.ws.request.timeout", 300000);
		((BindingProvider) port).getResponseContext().put(
				"com.sun.xml.internal.ws.connect.timeout", 300000);
		((BindingProvider) port).getResponseContext().put(
				"com.sun.xml.internal.ws.request.timeout", 300000);

		return port.process(specieName, geneList);
	}

	public static void main(String args[]) throws Exception {
		URL wsdlURL = Genebrowser.WSDL_LOCATION;
		if (args.length > 0 && args[0] != null && !"".equals(args[0])) {
			File wsdlFile = new File(args[0]);
			try {
				if (wsdlFile.exists()) {
					wsdlURL = wsdlFile.toURI().toURL();
				} else {
					wsdlURL = new URL(args[0]);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		System.out.println(wsdlURL);

		{
			System.out.println("Invoking process...");
			final java.lang.String _process_specieName = "Homo sapiens (human)";
			final java.lang.String _process_genelist = "CCND2,WT1,FGR,PRKAR1A,TNFRSF1B";
			es.uvigo.ei.sing.gc.ws.ArrayOfString _process__return = 
				GenebrowserSoap_GenebrowserSoap_Client.query(_process_specieName, _process_genelist);
			System.out.println("process.result=" + _process__return);
		}

		System.exit(0);
	}

}