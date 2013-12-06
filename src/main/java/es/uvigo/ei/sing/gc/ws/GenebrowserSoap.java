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

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService(targetNamespace = "gb2.ua.pt", name = "genebrowserSoap")
public interface GenebrowserSoap {
    @WebResult(name = "processResult", targetNamespace = "gb2.ua.pt")
    @RequestWrapper(localName = "process", targetNamespace = "gb2.ua.pt", className = "es.uvigo.ei.sing.gc.ws.Process")
    @WebMethod(action = "gb2.ua.pt/process")
    @ResponseWrapper(localName = "processResponse", targetNamespace = "gb2.ua.pt", className = "es.uvigo.ei.sing.gc.ws.ProcessResponse")
    public ArrayOfString process(
        @WebParam(name = "specieName", targetNamespace = "gb2.ua.pt")
        String specieName,
        @WebParam(name = "genelist", targetNamespace = "gb2.ua.pt")
        String genelist
    );
}
