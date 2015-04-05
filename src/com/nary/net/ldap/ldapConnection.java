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

package com.nary.net.ldap;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.AttributeModificationException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.NoSuchAttributeException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.nary.util.FastMap;


/**
 * ldapConnection
 *
 * A class for creating a connection to an ldap server, and performing
 * a query, entry addition, entry deletion, or entry modification.
 */


public class ldapConnection{

  public final static int ONELEVEL_SCOPE = SearchControls.ONELEVEL_SCOPE, 
                           SUBTREE_SCOPE = SearchControls.SUBTREE_SCOPE, 
                           BASE_SCOPE = SearchControls.OBJECT_SCOPE;
  public final static byte MODIFY_ADD=0, MODIFY_DELETE=1, MODIFY_REPLACE=2;

  private Hashtable<String, String> env;

  private String dn = null;
  private int scope;

  // params for search
  private String searchbase; // the entry in the directory where a search will begin from
  private String filter; // e.g. "(sn=Carter)";
  private String server;
  
  // attributes - req'd for search, add, modify, modifydn
  private String [] attributes = null;
  private char separator; // char that separates values in multivalue attributes
  
  // max. number of entries to be returned in a query. O equiv. to return all.
  private int maxEntries; 

  byte modifyType;


  public ldapConnection(String _server){
    // defaults
    scope = SearchControls.ONELEVEL_SCOPE;
    maxEntries = 0; // translated as no limit when sent to server
    modifyType = MODIFY_REPLACE;
    separator = ',';
    searchbase = null;
    filter = null;
    server = _server;

    env = new Hashtable<String, String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    if ( _server.indexOf(':') == -1 ){
      env.put(Context.PROVIDER_URL, "ldap://" + _server + ":389");
    }else{
      env.put(Context.PROVIDER_URL, "ldap://" + _server);
    }
    env.put("java.naming.ldap.referral.limit", "0");
    env.put(Context.REFERRAL, "follow");

  }// ldapConnection()


  /**
   * set the scope of the ldap connection. 
   * This must either be one level (default), base, or subtree
   */

  public void setScope(int _scope){
    scope = _scope;
  }// setScope()

  
  /**
   * set the port to connect to on the ldap server (defaults to 389).
   */

  public void setPort(int _port){
    // overwrites the port number if the server doesn't provide one
    if ( server.indexOf(':') == -1 ){
      env.put(Context.PROVIDER_URL, "ldap://" + server + ":" + _port);
    }
  }// setPort()


  /**
   * set the username and password for a simple authentication.
   */

  public void addAuthentication(String _username, String _password){
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.SECURITY_PRINCIPAL, _username);
    env.put(Context.SECURITY_CREDENTIALS, _password);
    
  }// addAuthentication()


  // 0 disables cfldaps ability to use referred addresses thus no data is returned
  // For other values of _referralLimit, entries that go beyond this hop limit will be ignored.
  public void setReferral(int _referralLimit){
    if( _referralLimit == 0 ){
      env.put(Context.REFERRAL, "ignore");
    }else{
      env.put("java.naming.ldap.referral.limit", "" + _referralLimit);
      env.put(Context.REFERRAL, "follow");
    }
  }// setReferral()

  
  // required for query
  // specifies the distinguished name to be used to start the search
  public void setStart(String _start){
    searchbase = _start;
  }// setStart()


  public void setMaxEntries(int _maxEntries){
    maxEntries = _maxEntries;
  }// setMaxEntries()


  // required for query, add, modifydn, modify
  public void setAttributes(String[] _attribs){
		if ( _attribs.length == 1 && _attribs[0].equals( "*" ) ){
			attributes = null;
		}else{
    	attributes = _attribs;
		}
  }// setAttributes()


  //req'd for query
  public void setFilter(String _filter){
    filter = _filter;
  }// setFilter()

  
  // required for add, modify, modifydn and delete
  public void setDN(String _dn){
    dn = _dn;
  }//setDN()

  
  // add, delete or replace(default)
  public void setModifyType(byte _modifyType){
    modifyType = _modifyType;
  }// setModifyType()

  public void setSSL(){
    env.put(Context.SECURITY_PROTOCOL, "ssl");
  }

  /**
   * sets the character that is used to separate values in multi-valued attributes.
   */

  public void setSeparator(char _separator){
    separator = _separator;
  }// setSeparator()


  public void modify() throws AttributeModificationException, NamingException{
    //Get a reference to a directory context
    int modType;
    // decode the modification type to one which the context will understand
    switch (modifyType){
      case ldapConnection.MODIFY_REPLACE: // attributes require name=value pairs
        modType = DirContext.REPLACE_ATTRIBUTE;
        break;
      
      case ldapConnection.MODIFY_ADD:
        modType = DirContext.ADD_ATTRIBUTE; // attributes require name=value pairs
        break;

      case ldapConnection.MODIFY_DELETE:
        modType = DirContext.REMOVE_ATTRIBUTE; // attributes require names only
        break;
      default:
        modType = DirContext.REPLACE_ATTRIBUTE;

    }// switch

    DirContext ctx = new InitialDirContext(env);
    Attributes attributes = processAttributes();
    ctx.modifyAttributes(dn, modType, attributes);
    ctx.close();

  }// modify()


  /**
   * modifies the dn to the one provided by the attributes.
   * No attempt is made to verify the attributes are valid i.e
   * in the format dn = ....
   */

  public void modifyDN() throws NamingException{
    DirContext ctx = new InitialDirContext(env);
    ctx.rename(dn, attributes[0]);
    ctx.close();

  }// modifyDN()


  /**
   * adds the entry given when attributes were set, to the directory
   * with the set DN.
   */

  public void add() throws NamingException{
    DirContext ctx = new InitialDirContext(env);
    ldapEntry e = new ldapEntry(processAttributes());
    ctx.bind(dn, e);
    ctx.close();
  }// add()


  /**
   * deletes the entry with the set DN.
   */

  public void delete() throws NamingException{
    //Get a reference to a directory context
    DirContext ctx = new InitialDirContext(env);
    ctx.destroySubcontext(dn);
    ctx.close();
  }// delete()

  
  /**
   * searches for the entries with the given start, search scope, attributes and filter;
   * returning the max entries set.
   * Returns the results in a Vector of Strings.
   */

  public List<Map<String, String>> search() throws NamingException, NoSuchAttributeException{
    boolean returnDN = false;
    DirContext ctx = new InitialDirContext(env);
    
    SearchControls constraints = new SearchControls();
    constraints.setSearchScope(scope);
    constraints.setCountLimit(maxEntries);
    
    constraints.setReturningAttributes(attributes);
      
    if (attributes != null ){
      // restricts the attributes of the entries that will be returned
      // otherwise all attributes are returned
      for ( int i = 0; i < attributes.length; i++ ){
        // Only return the DN attribute if it is listed in the attributes parameter - refer to bug #1302
        if ( attributes[i].equalsIgnoreCase( "DN" ) ){
          returnDN = true;
          break;
        }
      }

    }

	NamingEnumeration<SearchResult> results = ctx.search(searchbase, filter, constraints);
	List<Map<String, String>> resultVector = new ArrayList<Map<String, String>>();

    while (results != null && results.hasMore()) {
      SearchResult sr = (SearchResult) results.next();
      Map<String, String> entry = new FastMap<String, String>( FastMap.CASE_INSENSITIVE );
      if ( returnDN ){
          entry.put( "dn", sr.getNameInNamespace() );        
      }
      
      Attributes attrs;
      attrs = sr.getAttributes();


      for (NamingEnumeration<? extends Attribute> ne = attrs.getAll(); ne.hasMoreElements();)
      {
        Attribute attr = ne.next();
        String attrID = attr.getID();
        
        /* Only store the first attribute value to match the behaviour of CFMX - refer to bug #1301
         * entry.put( attrID.toLowerCase(), attr.get() ); //this was causing bug #1353, so it's been replaced with the code below
         *
         * More details regarding attributes in the LDAP server that have multiple values:
         * Matt M's testing showed that a cfldap query using attributes="*"
         * will display the attribute values (when they are comprised of multiple values) differently
         * between BD and CFMX 6.1.
         * For example if the value of an attribute in the LDAP server is "a, b" then using
         * attributes="*" in the query, BD will return "a, b" as the value for that attribute, while
         * CFMX 6.1 will only return the 1st value "a". This would appear to be a bug in CFMX 6.1.
         * 
         * So for now we won't try to match CFMX behavior in the attributes="*" scenario.
         * 
         * Note that explicitly listing the attribute(s) you want (instead of using "*") gets CFMX
         * to give the full list of values for a multi-valued attribute.
         */
        NamingEnumeration<?> nameEnum = attr.getAll();
        if(nameEnum != null)
        {
        	StringBuilder valueBuffer = new StringBuilder();
        	int i=0;
        	
        	while(nameEnum.hasMore())
        	{
        		Object o = nameEnum.next();
        		if(o != null)
        		{
        			if(i > 0)
        			{
        				valueBuffer.append(separator);
        				valueBuffer.append(" ");
        			}
        			
        			valueBuffer.append(o);
        			++i;
        		}
        	}
        	
        	entry.put(attrID, valueBuffer.toString()); //removed the .toLowerCase() call on attrID to fix bug #2035
        }
      }
      resultVector.add(entry);
    }
    ctx.close();
    return resultVector;
  }// search()



  // req'd to be called when modifying or adding an entry
  // takes the attribute list and produces a Attributes object from it.
  private Attributes processAttributes(){
    String name; // name of the attribute
    Attribute att; // the attribute itself, used to build up the Attributes object
      
    Attributes attrs = new BasicAttributes(true);
  

    for (int i = 0; i < attributes.length; i++){
      int indexOfEquals = attributes[i].indexOf('=');
      // in the case of modify delete, attributes don't have values
      if (indexOfEquals == -1){ 
        name = attributes[i];
        att = new BasicAttribute(name);
        attrs.put(att);
        continue;
      }

      name = attributes[i].substring(0, indexOfEquals);
      att = new BasicAttribute(name);

      String values = attributes[i].substring(indexOfEquals+1).trim();
      int valStartIndex = 0;
      int indexOfSeparator = values.indexOf(separator); 

      while (indexOfSeparator != -1){
        att.add(values.substring(valStartIndex, indexOfSeparator).trim());
        valStartIndex = indexOfSeparator+1;
        indexOfSeparator = values.indexOf(separator, indexOfSeparator+1);
      }
      att.add(values.substring(valStartIndex).trim());

      // add the attribute to the set of attributes
      attrs.put(att);
    }

    return attrs;
  }// processAttributes()


  public static void main(String [] args){
    // ldap://ldap.bigfoot.com:389/o=airius.com?cn,mail,?sub?(cn=billybob thornton)
    
    /*
    // Exchange server testing
    ldapConnection conn = new ldapConnection("arran.n-ary.com");
    conn.setFilter("(&(uid=A*)(sn=W*))");
    conn.setDN("");
    conn.setStart("");//cn=AndrewW, cn=Recipients, ou=N-ARY, o=n-ary group");
    conn.addAuthenticate("dc=N-ARY, cn=administrator, ou=N-ARY, o=n-ary group", "nokia1989");
    conn.setScope(ldapConnection.SUBTREE_SCOPE);
    conn.search();

    */
    try{
    ldapConnection conn = new ldapConnection("62.189.55.7");//"arden.n-ary.com");//
    conn.setFilter("(&(objectclass=*))");
    conn.setStart("dc=example,dc=com");
    conn.addAuthentication("cn=Administrator, dc=example, dc=com", "secret");
    conn.setScope(ldapConnection.SUBTREE_SCOPE);
    List<Map<String, String>> results = conn.search();
	for ( int i = 0; i < results.size(); i++ ) {
      System.out.println( results.get( i ) );
    }
    }catch(NamingException e){
      e.printStackTrace();
    }

  }// main()


}// ldapConnection
