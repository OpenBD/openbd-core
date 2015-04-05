package com.naryx.tagfusion.cfm.xml.ws.dynws;

import org.apache.axis.client.Stub;

import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.ws.CallParameters;

public interface DynamicWebServiceStubGeneratorInterface {

	public void setCacheDir(String javaCache);
	
	public Stub generateStub(String wsdlURL, String portName, CallParameters cp) throws cfmRunTimeException;
	
	public StubInfo getStubInfo();
	
}
