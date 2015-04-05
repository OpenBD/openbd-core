/*
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
 *
 *  This file is part of Open BlueDragon (OpenBD) CFML Server Engine.
 *
 *  OpenBD is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  Free Software Foundation,version 3.
 *
 *  OpenBD is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenBD.  If not, see http://www.gnu.org/licenses/
 *
 *  Additional permission under GNU GPL version 3 section 7
 *
 *  If you modify this Program, or any covered work, by linking or combining
 *  it with any of the JARS listed in the README.txt (or a modified version of
 *  (that library), containing parts covered by the terms of that JAR, the
 *  licensors of this Program grant you additional permission to convey the
 *  resulting work.
 *  README.txt @ http://www.openbluedragon.org/license/README.txt
 *
 *  http://www.openbluedragon.org/
 */

/*
 * Created on June 19, 2004
 *
 * BlueDragon only tag for use in calling XML-RPC servers
 *
 * Basically a wrapper to the Apache XML-RPC library
 *
 * Usage: <cfxmlrpc server="" method="" params="">
 * Returns: a structure named 'xmlrpc' which has
 * success = false | true
 * result  = cfml structure with results
 *
 * error 	= error message
 */
package com.naryx.tagfusion.cfm.tag.net;

import java.io.Serializable;
import java.net.URL;
import java.util.Vector;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.cfm.tag.tagUtils;

public class cfXMLRPC extends cfTag implements Serializable {
  static final long serialVersionUID = 1;

  protected void defaultParameters( String _tag ) throws cfmBadFileException {
    parseTagHeader( _tag );

    if ( !containsAttribute("SERVER") || !containsAttribute("PARAMS") || !containsAttribute("METHOD") ){
      throw newBadFileException( "Missing Attributes", "You need to provide SERVER, PARAMS and METHOD" );
    }
  }

  public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
    String server	= getDynamic( _Session, "SERVER").getString();
    String method	= getDynamic( _Session, "METHOD").getString();
    cfData params	= getDynamic( _Session, "PARAMS");


		//-- Create the return structure, and prefill it in with debug data
		cfStructData	xmlrpc = new cfStructData();
		xmlrpc.setData("server", 	new cfStringData(server) );
		xmlrpc.setData("method", 	new cfStringData(method) );


    //-- The params must be an array
    if ( params.getDataType() != cfData.CFARRAYDATA ){
			xmlrpc.setData("success", cfBooleanData.FALSE);
			xmlrpc.setData("error", 	new cfStringData("PARAMS must be an array type") );
			_Session.setData("xmlrpc", xmlrpc);
			return cfTagReturnType.NORMAL;
		}

    //-- Clean up the server
    if ( !server.toLowerCase().startsWith("http://") )
      server	= "http://" + server;

    try{
    	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(server));
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);

	  	//-- Need to convert the CFML structure into a Java structure
	    Vector javaParamsV 		= (Vector) tagUtils.convertCFtoJava( params, java.util.Vector.class );
	    Object[] javaParams     = new Object[javaParamsV.size()];
	    for(int i=0; i<javaParams.length; i++)
	    	javaParams[i] = javaParamsV.get(i);

        Object xmlResult 			= client.execute(method,javaParams);

      xmlrpc.setData("result", 	tagUtils.convertToCfData( xmlResult ) );
      xmlrpc.setData("success", cfBooleanData.TRUE);

    }catch(Exception E){
      xmlrpc.setData("success", cfBooleanData.FALSE);
      xmlrpc.setData("error", 	new cfStringData(E.getMessage()) );
    }

    _Session.setData("xmlrpc", xmlrpc);

    return cfTagReturnType.NORMAL;
  }
}
