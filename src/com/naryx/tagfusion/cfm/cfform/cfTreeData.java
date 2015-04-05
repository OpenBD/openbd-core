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

package com.naryx.tagfusion.cfm.cfform;

/**
 * Works exclusively with CFTREE.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfTreeData extends Object {

  private List nodes;
  private String		defaultNodeTerminal, defaultNonNodeTerminal;

  public cfTreeData(){
		nodes = new ArrayList();
  }

	public void setDefaultNode(String _defaultNodeTerminal, String _defaultNodeNonTerminal){
		defaultNodeTerminal			= _defaultNodeTerminal;
		defaultNonNodeTerminal	= _defaultNodeNonTerminal;
	}

  public void addNode( String value, String display, String parent, String href, String img, boolean expand, String target ){
	  	List rootNode;
		if ( parent == null ){
			rootNode	= nodes;
		}else{  //-- find Vector to insert into
			rootNode	= findRootNode( nodes, parent );
			if ( rootNode == null )
				rootNode	= nodes;
		}
	
		//- Create the hashtable
		Map nodeData	= new FastMap();
		
		nodeData.put("value", 	value);
		if ( display != null )
			nodeData.put("d",	display );
		else
			nodeData.put("d",	value );
		
		if ( href != null )
			nodeData.put("href", href );
			
		if ( target != null )
			nodeData.put("target", target );
	
		if ( img != null )
			nodeData.put("img",	img );
			
		nodeData.put("e",	"" + expand );
		nodeData.put("parent", rootNode );

		rootNode.add( nodeData );		
  }

	private List findRootNode( List root, String parent ){
		Iterator it = root.iterator();
		while ( it.hasNext() ){
			Map ht = (Map)it.next();
			
			if ( ht.get("value").equals(parent) ){
				List v = (List)ht.get("c");
				if ( v == null ){
					v = new ArrayList();
					ht.put("c", v);
				}
				return v;
			} else if ( ht.containsKey("c") ){
				List v = findRootNode( (List)ht.get("c"), parent );
				if ( v != null )
					return v;
			}
		}
		return null;
	}
  
  private void defaultNodes(List v){
  	//-- Need to visit every node and set the image accordingly
		Iterator	it	= v.iterator();
		while (it.hasNext()){
			Map nodeData	=	(Map)it.next();
			
			if ( !nodeData.containsKey("img") ){	//- No default node was given
				if ( nodeData.containsKey("c") )
					nodeData.put("img", defaultNonNodeTerminal );
				else
					nodeData.put("img", defaultNodeTerminal );
			}
			
			if ( nodeData.containsKey("c") )
				defaultNodes( (List)nodeData.get("c") );
		}
  }
  
  public String getTreeModelString() throws cfmRunTimeException{
		try{
			StringBuilder buf =	new StringBuilder(128);
			defaultNodes( nodes );
			encodeVector( buf, nodes );
			return buf.toString();
		}catch( Exception E ){
			throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.runtimeError", "cftree.treeData", null ) );
		}
  }
  
  
  private void encodeVector(StringBuilder out, List v){
  	out.append("<v>");
  	Iterator	it	= v.iterator();
  	while (it.hasNext()){
  		Object o = it.next();
			out.append("<e>");
			if ( o instanceof List )
				encodeVector( out, (List)o );
			else if ( o instanceof String )
				encodeString( out, (String)o );
			else if ( o instanceof Map )
				encodeHashtable( out, (Map)o );

			out.append("</e>");
  	}
  	out.append("</v>");
  }
  
  private void encodeHashtable( StringBuilder out, Map s){
		out.append("<h>");
		Iterator	it	= s.keySet().iterator();
		while (it.hasNext()){
			String key	= (String)it.next();
			if ( !key.equals("parent")){
				out.append( "<p k='");
				out.append( key );
				out.append( "'>" );
				
				Object o = s.get(key);
				if ( o instanceof List )
					encodeVector( out, (List)o );
				else if ( o instanceof String )
					encodeString( out, (String)o );
				else if ( o instanceof Map )
					encodeHashtable( out, (Map)o );

				out.append("</p>");
			}
		}
		
		out.append("</h>");
  }
  
  private static void encodeString( StringBuilder out, String s){
		out.append("<s>");
		out.append( s );
		out.append("</s>");
  }
}
