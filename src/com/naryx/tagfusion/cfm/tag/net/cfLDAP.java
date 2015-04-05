/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  http://openbd.org/
 *  $Id: cfLDAP.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.tag.net;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import com.nary.net.ldap.ldapConnection;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfLDAP extends cfTag implements Serializable {
	static final long serialVersionUID = 1;

  
  @SuppressWarnings("unchecked")
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
   			createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
   			
   			createAttInfo( "ACTION", "The type of LDAP action: query, add, delete, modify, modifydn", 	"query", true ),
   			createAttInfo( "PORT", "The port to connection", 	"389", false ),
   			createAttInfo( "SERVER", "The server to connect", 	"", true ),
   			createAttInfo( "TIMEOUT", "The timeout to wait for a response (seconds)", 	"60", false ),
   			createAttInfo( "REBIND", "", 	"no", false ),
   			createAttInfo( "STARTROW", "", 	"1", false ),
   			createAttInfo( "MODIFYTYPE", "", 	"REPLACE", false ),
   			createAttInfo( "SEPARATOR", "", 	",", false ),
   			createAttInfo( "DELIMITER", "", 	";", false ),
   			createAttInfo( "MAXROWS", "", 	"0", false ),
   			createAttInfo( "SORT", "", 	"no", false ),
   			createAttInfo( "SORTCONTROL", "", 	"ASC", false ),
   			createAttInfo( "SCOPE", "", 	"ONELEVEL", false ),
   			createAttInfo( "FILTER", "", 	"(objectclass=*)", false ),
   			createAttInfo( "USERNAME", "The username to login with", 	"no", false ),
   			createAttInfo( "PASSWORD", "The password to login with", 	"no", false ),
  	};
  }

	
	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute("PORT", 389);
		defaultAttribute("TIMEOUT", 60);
		defaultAttribute("ACTION", "query");
		defaultAttribute("REBIND", "no");
		defaultAttribute("STARTROW", 1);
		defaultAttribute("MODIFYTYPE", "REPLACE");
		defaultAttribute("SEPARATOR", ",");
		defaultAttribute("DELIMITER", ";");
		defaultAttribute("MAXROWS", 0);
		defaultAttribute("SORTCONTROL", "ASC");
		defaultAttribute("SCOPE", "ONELEVEL");
		defaultAttribute("FILTER", "(objectclass=*)");
		defaultAttribute("PASSWORD", "");
		defaultAttribute("SORT", "");

		parseTagHeader(_tag);

		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;
		
		if (!containsAttribute("SERVER"))
			throw newBadFileException("Missing Attribute", "This tag requires the SERVER attribute");

		if (getConstant("SERVER").length() == 1)
			throw newBadFileException("Bad attribute value", "The server name is invalid. It must be at least 1 character in length.");
		
		if (getConstant("SEPARATOR").length() != 1)
			throw newBadFileException("Bad attribute value", "The SEPARATOR is invalid. It must be a single character in length.");

		if (getConstant("DELIMITER").length() != 1)
			throw newBadFileException("Bad attribute value", "The DELIMITER is invalid. It must be a single character in length.");

		setFlushable(true);
	}

	
  
	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if (!containsAttribute(attributes,"SERVER"))
			throw newBadFileException("Missing Attribute", "This tag requires the SERVER attribute");

		if (getDynamicAsString(attributes, _Session, "SERVER").length() == 1)
			throw newBadFileException("Bad attribute value", "The server name is invalid. It must be at least 1 character in length.");
		
		if (getDynamicAsString(attributes, _Session,"SEPARATOR").length() != 1)
			throw newBadFileException("Bad attribute value", "The SEPARATOR is invalid. It must be a single character in length.");

		if (getDynamicAsString(attributes, _Session,"DELIMITER").length() != 1)
			throw newBadFileException("Bad attribute value", "The DELIMITER is invalid. It must be a single character in length.");

    return	attributes;
	}

	
	
	private void validateAttributes(cfSession _Session, cfStructData attributes) throws cfmRunTimeException {
		
		// check a valid action is given
		String action = getDynamicAsString(attributes, _Session, "ACTION");
		if (!(action.equalsIgnoreCase("ADD") || action.equalsIgnoreCase("QUERY") || action.equalsIgnoreCase("MODIFY") || action.equalsIgnoreCase("MODIFYDN") || action.equalsIgnoreCase("DELETE"))) {
			throw newBadFileException("Bad Attribute value", "Invalid value for the ACTION attribute : " + action + "\n\nValid values for the ACTION attribute are :\n\tADD\n\tDELETE\n\tMODIFY\n\tMODIFYDN\n\tQUERY\n");
		}

		// check a valid modify type is given if the action is modify
		if (action.equalsIgnoreCase("MODIFY")) {
			String modifyType = getDynamicAsString(attributes, _Session, "MODIFYTYPE");
			if (!(modifyType.equalsIgnoreCase("ADD") || modifyType.equalsIgnoreCase("REPLACE") || modifyType.equalsIgnoreCase("DELETE"))) {
				throw newBadFileException("Bad attribute value", "Invalid value for the MODIFYTYPE attribute : " + action + "\n\nValid values for the ACTION attribute are :\n\tADD\n\tDELETE\n\tREPLACE\n");
			}
		}

		// check the NAME and START attributes are present if the action is Query and also that
		// a valid scope is given
		if (action.equalsIgnoreCase("QUERY")) {
			if (!containsAttribute(attributes, "NAME")) {
				throw newBadFileException("Invalid attribute", "Invalid attribute combination. A NAME attribute value is required for a QUERY.");
			}
			if (!containsAttribute(attributes, "START")) {
				throw newBadFileException("Invalid attribute combination", "A START attribute value is required for a QUERY.");
			}
			String scope = getDynamicAsString(attributes, _Session, "SCOPE");
			if (!(scope.equalsIgnoreCase("BASE") || scope.equalsIgnoreCase("ONELEVEL") || scope.equalsIgnoreCase("SUBTREE"))) {
				throw newBadFileException("Bad attribute value", "Invalid value for SCOPE attribute : " + scope + "\n\nValid values for the ACTION attribute are :\n\tBASE\n\tONELEVEL\n\tSUBTREE\n");
			}

		} else { // when it's not a query, a DN attribute is required
			if (!containsAttribute(attributes,"DN")) {
				throw newBadFileException("Invalid attribute combination", "A DN attribute value is required when action is " + getDynamic(_Session, "ACTION").getString().toUpperCase() + ".");
			}
		}

		// when action's not a delete, check if the ATTRIBUTES attribute is given
		if (!action.equalsIgnoreCase("DELETE")) {
			if (!containsAttribute(attributes,"ATTRIBUTES")) {
				throw newBadFileException("Invalid attribute combination", "An ATTRIBUTES attribute value is required when action is " + getDynamicAsString(attributes,_Session, "ACTION").toUpperCase() + ".");
			} else if (getDynamicAsString(attributes,_Session, "ATTRIBUTES").endsWith(getDynamicAsString(attributes,_Session, "DELIMITER"))) {
				throw newBadFileException("Invalid attribute combination.", "An ATTRIBUTES attribute value is required when action is " + getDynamicAsString(attributes,_Session, "ACTION").toUpperCase() + ".");
			}
		}

		if (getDynamicAsString(attributes,_Session, "SORTCONTROL").length() == 0) {
			throw newBadFileException("Bad attribute value", "The length of the SORTCONTROL attribute must be greater in length than 0 characters.");
		}

	}


	
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);
		
		validateAttributes(_Session, attributes);

		int timeout = getDynamic(attributes, _Session, "TIMEOUT").getInt();

		renderRunner runner = new renderRunner(_Session, attributes);
		runner.start();
		try {
			runner.join(timeout * 1000);
		} catch (java.lang.InterruptedException ignored) {}

		if (runner.rtException != null)
			throw runner.rtException;
		
		if (!runner.success)
			throw newRunTimeException("Time out limit was exceeded.");
		
		return cfTagReturnType.NORMAL;
	}
	

	private void realRender(cfSession _Session, cfStructData attributes) throws cfmRunTimeException {
		ldapConnection connection = createConnection(attributes, _Session);
		applyAuthentication(attributes, _Session, connection);
		connection.setSeparator(getDynamic(attributes, _Session, "SEPARATOR").getString().charAt(0));

		String action = getDynamic(attributes, _Session, "ACTION").getString(); 
		if (containsAttribute(attributes, "REFERRAL")) {
			connection.setReferral(getDynamic(attributes, _Session, "REFERRAL").getInt());
		}

		if (action.equalsIgnoreCase("QUERY")) {
			doQuery(attributes, _Session, connection);

		} else if (action.equalsIgnoreCase("ADD")) {
			doAdd(attributes, _Session, connection);

		} else if (action.equalsIgnoreCase("DELETE")) {
			doDelete(attributes, _Session, connection);

		} else if (action.equalsIgnoreCase("MODIFYDN")) {
			doModifyDN(attributes, _Session, connection);

		} else if (action.equalsIgnoreCase("MODIFY")) {
			try {
				doModify(attributes, _Session, connection);
			} catch (javax.naming.directory.AttributeModificationException ame) {
				throw newRunTimeException("Failed to make the required modifications");
			}
		}

	}// realRender()

	private ldapConnection createConnection(cfStructData attributes, cfSession _Session) throws cfmRunTimeException {
		String server = getDynamic(attributes, _Session, "SERVER").getString();
		int port = getDynamic(attributes, _Session, "PORT").getInt();
		ldapConnection conn = new ldapConnection(server);
		conn.setPort(port);

		if (containsAttribute(attributes, "SECURE")) {
			String secure = getDynamic(attributes, _Session, "SECURE").getString();
			if (secure.equalsIgnoreCase("CFSSL_BASIC")) {
				if (!containsAttribute(attributes, "USERNAME") || !containsAttribute(attributes, "PASSWORD")) {
					throw newRunTimeException("You must specify USERNAME and PASSWORD attributes when the value of SECURE is CFSSL_BASIC.");
				}
				conn.setSSL();
			} else {
				throw newRunTimeException("Unsupported SECURE value [" + secure + "].");

			}
		}

		return conn;
	}

	
	private String[] getAttributes(cfStructData attributes, cfSession _Session, char _delimiter) throws cfmRunTimeException {
		// add, modify, modify dn, query
		String att = getDynamic(attributes, _Session, "ATTRIBUTES").getString(); // fixed bug #2028 here by removing the toLowerCase() call
		return com.nary.util.string.convertToList(att, _delimiter);
	}

	
	private String getDN(cfStructData attributes, cfSession _Session) throws cfmRunTimeException {
		return (getDynamic(attributes, _Session, "DN").getString());
	}
	

	private void doQuery(cfStructData attributes, cfSession _Session, ldapConnection _conn) throws cfmRunTimeException {
		// query
		String[] att = getAttributes(attributes,_Session, ',');
		_conn.setAttributes(att);
		String scope = getDynamic(attributes,_Session, "SCOPE").getString();
		
		if (scope.equalsIgnoreCase("ONELEVEL")) {
			_conn.setScope(ldapConnection.ONELEVEL_SCOPE);
		} else if (scope.equalsIgnoreCase("BASE")) {
			_conn.setScope(ldapConnection.BASE_SCOPE);
		} else { // scope must be subtree due to earlier check
			_conn.setScope(ldapConnection.SUBTREE_SCOPE);
		}

		String queryName = getDynamic(attributes,_Session, "NAME").getString();
		_conn.setStart(getDynamic(attributes,_Session, "START").getString()); // the search base - a DN
		_conn.setFilter(getDynamic(attributes,_Session, "FILTER").getString());

		int startRow = getDynamic(attributes,_Session, "STARTROW").getInt();
		String sortAttribs = getDynamic(attributes,_Session, "SORT").getString(); // has def
		String sortControl = getDynamic(attributes,_Session, "SORTCONTROL").getString();

		List<Map<String, String>> results = null;
		try {
			results = _conn.search();
		} catch (NamingException e) {
			String msg = e.toString();
			Throwable rootCause = e.getRootCause();
			if (rootCause != null)
				msg += "; ROOT CAUSE=" + rootCause.getMessage();

			throw newRunTimeException(msg);
		}
		int maxRows = getDynamic(attributes,_Session, "MAXROWS").getInt();

		List<String> returnAsBinary = null;
		if (containsAttribute(attributes,"RETURNASBINARY")) {
			returnAsBinary = com.nary.util.string.split(getDynamic(attributes,_Session, "RETURNASBINARY").getString(), " ");
		}

		new cfldapQueryData(_Session, queryName, results, att, sortAttribs, sortControl, startRow, maxRows, returnAsBinary);

	}

	
	private void doAdd(cfStructData attributes, cfSession _Session, ldapConnection _conn) throws cfmRunTimeException {
		_conn.setDN(getDN(attributes,_Session));
		_conn.setAttributes(getAttributes(attributes,_Session, getDynamic(_Session, "DELIMITER").getString().charAt(0)));
		try {
			_conn.add();
		} catch (NamingException e) {
			String msg = e.toString();
			Throwable rootCause = e.getRootCause();
			if (rootCause != null)
				msg += "; ROOT CAUSE=" + rootCause.getMessage();

			throw newRunTimeException(msg);
		}
	}

	

	private void doDelete(cfStructData attributes,cfSession _Session, ldapConnection _conn) throws cfmRunTimeException {
		_conn.setDN(getDN(attributes,_Session));
		try {
			_conn.delete();
		} catch (NamingException e) {
			String msg = e.toString();
			Throwable rootCause = e.getRootCause();
			if (rootCause != null)
				msg += "; ROOT CAUSE=" + rootCause.getMessage();

			throw newRunTimeException(msg);
		}

	}

	
	private void doModify(cfStructData attributes, cfSession _Session, ldapConnection _conn) throws cfmRunTimeException, javax.naming.directory.AttributeModificationException {
		_conn.setDN(getDN(attributes,_Session));
		_conn.setAttributes(getAttributes(attributes,_Session, getDynamic(attributes, _Session, "DELIMITER").getString().charAt(0)));
		String modifyType = getDynamic(attributes,_Session, "MODIFYTYPE").getString();
		if (modifyType.equalsIgnoreCase("ADD")) {
			_conn.setModifyType(ldapConnection.MODIFY_ADD);
		} else if (modifyType.equalsIgnoreCase("DELETE")) {
			_conn.setModifyType(ldapConnection.MODIFY_DELETE);
		} else if (modifyType.equalsIgnoreCase("REPLACE")) {
			_conn.setModifyType(ldapConnection.MODIFY_REPLACE);
		}
		try {
			_conn.modify();
		} catch (NamingException e) {
			String msg = e.toString();
			Throwable rootCause = e.getRootCause();
			if (rootCause != null)
				msg += "; ROOT CAUSE=" + rootCause.getMessage();

			throw newRunTimeException(msg);
		}

	}
	

	private void doModifyDN(cfStructData attributes, cfSession _Session, ldapConnection _conn) throws cfmRunTimeException {
		_conn.setDN(getDN(attributes,_Session));
		String newDN = getDynamicAsString(attributes, _Session, "ATTRIBUTES");
		_conn.setAttributes(new String[] { newDN });
		try {
			_conn.modifyDN();
		} catch (NamingException e) {
			String msg = e.toString();
			Throwable rootCause = e.getRootCause();
			if (rootCause != null)
				msg += "; ROOT CAUSE=" + rootCause.getMessage();

			throw newRunTimeException(msg);
		}

	}
	

	private void applyAuthentication(cfStructData attributes, cfSession _Session, ldapConnection _conn) throws cfmRunTimeException {
		if (containsAttribute("USERNAME")) {
			String username = getDynamic(attributes, _Session, "USERNAME").getString();
			String password = getDynamic(attributes, _Session, "PASSWORD").getString();
			_conn.addAuthentication(username, password);
		}
	}

	
	
	class renderRunner extends Thread implements Serializable {
	private static final long serialVersionUID = 1L;

		private cfSession session;
		private cfStructData attributes;

		public cfmRunTimeException rtException = null;

		public boolean success = false;

		renderRunner(cfSession _Session, cfStructData attributes) {
			this.setName("OpenBD:cfLDAP");
			this.session 		= _Session;
			this.attributes = attributes;
		}

		public void run() {
			try {
				realRender(session, attributes);
				success = true;
			} catch (cfmRunTimeException e) {
				rtException = e;
			} catch (SecurityException secExc) {
				cfCatchData catchData = new cfCatchData(session);
				catchData.setType("Application");
				catchData.setMessage("CFLDAP tag failed");
				catchData.setDetail("CFLDAP is not supported when DirectoryServicesPermission is not enabled. (" + secExc.toString() + ")");
				rtException = new cfmRunTimeException(catchData);
			} catch (Throwable t) {
				rtException = new cfmRunTimeException(session, t);
			}
		}
	}

}
