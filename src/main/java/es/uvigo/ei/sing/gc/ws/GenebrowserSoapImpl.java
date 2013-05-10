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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This class was generated by Apache CXF 2.7.4
 * 2013-05-02T18:57:14.941+02:00
 * Generated source version: 2.7.4
 * 
 */

@javax.jws.WebService(
	serviceName = "genebrowser", 
	portName = "genebrowserSoap", 
	targetNamespace = "gb2.ua.pt", 
	wsdlLocation = "http://bioinformatics.ua.pt/genebrowser2/genebrowser.asmx?WSDL", 
	endpointInterface = "es.uvigo.ei.sing.gc.ws.GenebrowserSoap"
)
public class GenebrowserSoapImpl implements GenebrowserSoap {
    private static final Logger LOG = Logger.getLogger(GenebrowserSoapImpl.class);

    /* (non-Javadoc)
     * @see es.uvigo.ei.sing.gc.ws.GenebrowserSoap#process(String  specieName ,)String  genelist )*
     */
    public ArrayOfString process(String specieName, String genelist) { 
        LOG.info("Executing operation process");
        try {
            ArrayOfString _return = new ArrayOfString();
            List<String> _returnString = new ArrayList<String>();
            String _returnStringVal1 = "_returnStringVal1731384471";
            _returnString.add(_returnStringVal1);
            _return.getString().addAll(_returnString);
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}