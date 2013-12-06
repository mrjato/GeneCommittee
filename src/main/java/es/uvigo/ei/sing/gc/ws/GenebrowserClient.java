package es.uvigo.ei.sing.gc.ws;

import java.util.Map;

import javax.xml.ws.BindingProvider;

public final class GenebrowserClient {
	private GenebrowserClient() {}
	
	public static ArrayOfString querySoap(String specieName, String geneList) {
		final Genebrowser genebrowser = new Genebrowser(
			Genebrowser.WSDL_LOCATION, Genebrowser.SERVICE
		);
		
		return query(genebrowser.getGenebrowserSoap(), specieName, geneList);
	}
	
	public static ArrayOfString querySoap12(String specieName, String geneList) {
		final Genebrowser genebrowser = new Genebrowser(
			Genebrowser.WSDL_LOCATION, Genebrowser.SERVICE
		);
		
		return query(genebrowser.getGenebrowserSoap12(), specieName, geneList);
	}

	static ArrayOfString query(GenebrowserSoap port, String specieName, String geneList) {
		final Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
		final Map<String, Object> responseContext = ((BindingProvider) port).getResponseContext();
		
		requestContext.put(
			BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
			Genebrowser.WSDL_LOCATION.toExternalForm()
		);
		requestContext.put("com.sun.xml.internal.ws.connect.timeout", 300000);
		requestContext.put("com.sun.xml.internal.ws.request.timeout", 300000);
		
		responseContext.put("com.sun.xml.internal.ws.connect.timeout", 300000);
		responseContext.put("com.sun.xml.internal.ws.request.timeout", 300000);

		return port.process(specieName, geneList);
	}

	public static void main(String args[]) throws Exception {
		System.out.println(Genebrowser.WSDL_LOCATION);
		System.out.println("Invoking process...");
		final String specieName = "Homo sapiens (human)";
		final String genelist = "CCND2,WT1,FGR,PRKAR1A,TNFRSF1B";
//		final ArrayOfString result =  GenebrowserClient.querySoap(specieName, genelist);
		final ArrayOfString result =  GenebrowserClient.querySoap12(specieName, genelist);
		System.out.println(result);
	}
}
