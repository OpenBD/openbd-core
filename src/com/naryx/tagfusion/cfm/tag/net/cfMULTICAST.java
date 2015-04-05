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
 * CFMULTICAST
 *
 * Sends and receives messages on a multicast network
 *
 * Listen for messages:
 * <cfmulticast action="listen" address="224.1.1.1" port="5600"
 *              cfc="cfc" cfcfunction="testMethod">
 * the CFC function must define the argument 'data' to receive the
 * message itself.  'data' will be a structure with various meta
 * data inside it to show destination.
 *
 * Send a CFML structure
 * <cfmulticast action="send" address="224.1.1.1" port="5600"
 *              cfmldata="#cgi#">
 *
 * Send a text message
 * <cfmulticast action="send" address="224.1.1.1" port="5600"
 *              textdata="this is a test">
 *
 * Status
 * <cfmulticast action="status" address="224.1.1.1" port="5600">
 * returns back an structure called 'cfmulticast' that has various
 * counters in it
 *
 * Stop Listening on a given multicast address
 * <cfmulticast action="stop" address="224.1.1.1" port="5600">
 *
 *
 * Notes:
 *  - address attribute is a multicast address 224.0.0.0+
 *
 *  - Bind address is configured in the bluedragon.xml
 *    server.cfmulticast.bindaddress
 *    defaults to all interfaces if not present
 *
 *  - Individual messages can't be greater than 64KB in size
 *
 *  - Log file tracks incoming and outgoing messages; multicast.log
 *
 *  - Works across .NET and J2EE; can interchange messages
 */
package com.naryx.tagfusion.cfm.tag.net;

import java.io.File;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.nary.io.FileUtils;
import com.nary.util.LogFile;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.cfm.tag.awt.MultiCastManager;
import com.naryx.tagfusion.cfm.wddx.cfWDDX;
import com.naryx.tagfusion.xmlConfig.xmlCFML;


public class cfMULTICAST extends cfTag implements Serializable {

  static final long serialVersionUID = 1;

  static private Map<String, MultiCastManager> MulticastManagers = new HashMap<String, MultiCastManager>();
  static private String bindAddress;

  public static void init(xmlCFML configFile) {
    LogFile.open("MultiCast", new File(cfEngine.thisPlatform.getFileIO().getTempDirectory(), "multicast.log").toString());

	bindAddress = configFile.getString("server.cfmulticast.bindaddress", "0.0.0.0" ); // default value is loopback IP address
  }

  // ----------------------------------

  public void defaultParameters(String _tag) throws cfmBadFileException {
    defaultAttribute("ACTION", "SEND");
    defaultAttribute("ADDRESS", "224.1.1.1");
    defaultAttribute("PORT",    "14141");

    parseTagHeader(_tag);

    // -- Make sure the parameters are available for listening
    if (!containsAttribute("ACTION")) throw newBadFileException("Missing Attribute",  "CFMULTICAST requires the ACTION attribute");
  }

  // ----------------------------------

  public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {

	  try
	  {
		  String ACTION = getDynamic(_Session, "ACTION").getString().toUpperCase();
		  String address = getDynamic(_Session, "ADDRESS").getString();
		  int port = getDynamic(_Session, "PORT").getInt();

		  MultiCastManager manager = getManager(_Session, address, port);

		  if (ACTION.equals("SEND"))
		  {

			  String data = "";
			  if (containsAttribute("CFMLDATA")){
				  data = cfWDDX.cfml2Wddx( getDynamic(_Session, "CFMLDATA"));
			  }else if (containsAttribute("TEXTDATA")){
				  data = getDynamic(_Session, "TEXTDATA").getString();
			  } else
				  throw newRunTimeException("Specify either CFMLDATA or TEXTDATA to send");

			  //-- Check the size of the data packet
			  if (data.length() > 64000)
				  throw newRunTimeException("Data Size must be less than 64KB. Packet=" + data.length() + " bytes");

			  manager.broadcastData(data);

		  }
		  else if (ACTION.equals("LISTEN"))
		  {

			  if (!containsAttribute("CFC") || !containsAttribute("CFCFUNCTION"))
				  throw newRunTimeException("Specify both CFC and CFCFUNCTION attributes");

			  String cfc = getDynamic(_Session, "CFC").getString();
			  String cfcmethod = getDynamic(_Session, "CFCFUNCTION").getString();
			  String webServerRoot = FileUtils.getRealPath( _Session.REQ, "/" );

			  manager.registerReceiver(cfc, cfcmethod, webServerRoot);

		  }
		  else if (ACTION.equals("STOP"))
		  {

			  manager.stopReceiving();

		  }
		  else if (ACTION.equals("STATUS"))
		  {

			  cfStructData mess = new cfStructData();
			  mess.setData("messages_in", new cfNumberData(manager.getMessagesRxd()));
			  mess.setData("messages_out", new cfNumberData(manager.getMessagesTxd()));
			  mess.setData("bytes_in", new cfNumberData(manager.getBytesRxd()));
			  mess.setData("bytes_out", new cfNumberData(manager.getBytesTxd()));
			  _Session.setData("cfmulticast", mess);

		  }
	  }
	  catch (SecurityException secExc)
	  {
		  throw newRunTimeException("CFMULTICAST is not supported when the SecurityPermission UnmanagedCode attribute is not set. ("+secExc+")");
	  }

	  return cfTagReturnType.NORMAL;
  }

  private synchronized MultiCastManager getManager( cfSession _session, String address, int port ) throws cfmRunTimeException{
    MultiCastManager manager = MulticastManagers.get( address + ":" + port  );
    if ( manager == null ){
      try {
				manager = new MultiCastManager( bindAddress, address, port );
			} catch (UnknownHostException e) {
				throw newRunTimeException( "Failed to open multicast address " + address );
			}
      MulticastManagers.put(  address + ":" + port, manager );
    }
    return manager;
  }
}
