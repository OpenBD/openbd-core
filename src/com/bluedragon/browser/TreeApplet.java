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
 * @author Alan Williamson
 */
package com.bluedragon.browser;

import java.applet.Applet;
import java.awt.Font;
import java.io.StringBufferInputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import netscape.javascript.JSObject;

import com.bluedragon.browser.thinlet.FrameLauncher;
import com.bluedragon.browser.thinlet.Thinlet;

@SuppressWarnings("deprecation")
public class TreeApplet extends Thinlet {
	private static final long serialVersionUID = 1L;
	private Applet		thisApplet;
	private String		currentValue, delimiter;
	private JSObject 	win;
	private boolean		appendKey, showLinks, bCompletePath;


	public TreeApplet(Applet parentApp) throws Exception {
		thisApplet		= parentApp;
		currentValue	= "";
		win = JSObject.getWindow(thisApplet);   

		System.out.println("<CFTREE> BlueDragon 2004. http://www.newatlanta.com/");
		System.out.println("Based on Thinlet Technology, http://www.thinlet.com/");

		if (thisApplet != null)
			setFont();

		add(parse("tree.xml",this));

		System.out.println( "UI Ready for use" );


		//-- The appendkey
		appendKey = false;
		try{
			if ( thisApplet.getParameter("APPENDKEY") != null )
				appendKey	= Boolean.valueOf(thisApplet.getParameter("APPENDKEY")).booleanValue();
		}catch(Exception E){}

		showLinks = true;
		try{
			if ( thisApplet.getParameter("HIGHLIGHTHREF") != null )
				showLinks	= Boolean.valueOf(thisApplet.getParameter("HIGHLIGHTHREF")).booleanValue();
		}catch(Exception E){}

		bCompletePath = true;
		try{
			if ( thisApplet.getParameter("COMPLETEPATH") != null )
				bCompletePath	= Boolean.valueOf(thisApplet.getParameter("COMPLETEPATH")).booleanValue();
		}catch(Exception E){}

		delimiter	= thisApplet.getParameter("DELIMITER");

		Vector nodes = getData("treedata" );
		if ( nodes == null ){
			nodes = new Vector();
			Hashtable n = new Hashtable();
			n.put("d","Invalid Data");
			n.put("e","false");
			nodes.addElement( n );
			System.out.println("Invalid Data");
		}else
			System.out.println("Data Good");

		try{
			buildTree( find("rootTree"), nodes );
		}catch(Exception ee){
			ee.printStackTrace();
		}
	}

	private void setFont(){
		int fontsize = 10;		
		String fontface = thisApplet.getParameter("FONT");
		if ( fontface == null )
			fontface 	= getFont().getName();
		
		try{
			fontsize = getFont().getSize(); 
			fontsize = Integer.parseInt( thisApplet.getParameter("FONTSIZE") );
			if ( fontsize < 6) fontsize = 6;
		}catch(Exception E){}
		
		boolean bold = false, italic = false;
		try{
			if ( thisApplet.getParameter("BOLD") != null )
				bold	= Boolean.valueOf(thisApplet.getParameter("BOLD")).booleanValue();
		}catch(Exception E){}
		
		try{
			if ( thisApplet.getParameter("ITALIC") != null )
			italic	= Boolean.valueOf(thisApplet.getParameter("ITALIC")).booleanValue();
		}catch(Exception E){}
		
		setFont( new Font(fontface, (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0), fontsize ) );
	}


	private Vector getData(String a){
		//-- Put them together
		int x = 0;
		StringBuilder appletP	= new StringBuilder(128);
		String line	= thisApplet.getParameter("treedata" + x);
		while (line!=null){
			appletP.append( line.substring(1,line.length()-1) );
			x++;
			line	= thisApplet.getParameter("treedata" + x);
		}

		String appletParam	= appletP.toString();

		tags		= new Stack();
		params	= new Stack();
		StringBufferInputStream s = new StringBufferInputStream(appletParam);	

		try{
			parseXML(s);
		}catch(Throwable E){
			E.printStackTrace();
			return null;
		}

		return (Vector)params.peek();
	}

	//--------------------------------

	Stack tags, params;
	String lastData;

	public void characters(String text){
		lastData	= text;
	}

	public void startElement( String name, Hashtable attributelist){
		if ( name.equals("v")){
			tags.push( new Vector() );
		}else if ( name.equals("h")){
			tags.push( new Hashtable() );
		}else if ( name.equals("p") ){
			tags.push( (String)attributelist.get("k") );
		}else if ( name.equals("s") ){
		  tags.push( new StringBuilder() );
		}else if ( name.equals("e") ){
		  tags.push( new Integer(0) );
		}
	}

	public void endElement(){
		Object	o	= tags.pop();
		
		if ( o instanceof StringBuilder ){
			
			StringBuilder b = (StringBuilder)o;
			b.append( lastData );
			params.push( b.toString() );
			
		}else if ( o instanceof Hashtable ){
			
			params.push( o );
			
		}else if ( o instanceof Vector ){
			
			params.push( o );
			
		}else if ( o instanceof String ){
			
			Hashtable	h	= (Hashtable)tags.peek();
			h.put( (String)o, params.pop() );
						
		}else if ( o instanceof Integer ){

			Vector	v			= (Vector)tags.peek();
			v.addElement( params.pop() );
		}
	}

	//--------------------------------

	public void expand(Object tree, Object node) {}

	public void action(Object tree, Object node) {
		Hashtable ht	= (Hashtable)getProperty(node,"data");
		if ( ht.containsKey("href") ){
			jumpToUrl( (String)ht.get("href"), (String)ht.get("target"), (String)ht.get("value") );
		}else if ( ht.containsKey("value") ){
			String path	= (String)ht.get("path");
			path	= path.substring( delimiter.length() );
			if ( !bCompletePath )
				path = path.substring( path.indexOf(delimiter)+1 );
			
			currentValue = "PATH=" + path + ";NODE=" + (String)ht.get("value");
				
			Object testArray[] = new Object[3];
			testArray[0] = thisApplet.getParameter("FORMNAME");
			testArray[1] = thisApplet.getParameter("OBJECTNAME");
			testArray[2] = currentValue;    
			win.call( "tf_setFormParam", testArray );	 
		}
	}
	
	private void buildTree( Object root, Vector nodes ){
		if ( root == null ){
			System.out.println("Root node was null");
			return;
		}
		
		String pathToHere = "";
		Hashtable ht	= (Hashtable)getProperty( root, "data" );
		if ( ht != null )
			pathToHere	= (String)ht.get("path");
				
		Enumeration E	= nodes.elements();
		while ( E.hasMoreElements() ){
			ht = (Hashtable)E.nextElement();
			
			Object subnode = Thinlet.create("node");
			setString(subnode, "text", (String)ht.get("d") );

			if ( ht.containsKey("e") ){
				setBoolean( subnode, "expanded", Boolean.valueOf((String)ht.get("e")).booleanValue() );
			}

			putProperty( subnode, "data", ht );
			ht.put( "path", pathToHere + delimiter + (String)ht.get("value") );
			
			//-- If the links to be displayed is turned on
			if ( ht.containsKey("href") && showLinks )
				putProperty( subnode, "link", "1" );

			if ( ht.containsKey("img") ){
				setIcon(subnode, "icon", getIcon( (String)ht.get("img") ) );
			}

			//-- If children then add to this list
			Vector children	= (Vector)ht.get("c");
			if (children != null && children.size() > 0 )
				buildTree( subnode, children );

			//-- Add to the node
			add(root, subnode);
		}
	}

	private void jumpToUrl(String url, String target, String value){
		if ( thisApplet != null ){
			if ( appendKey ){
				if (url.indexOf("?") == -1 )
					url = url + "?CFTREEITEMKEY=" + URLEncoder.encode(value);
				else
					url = url + "&CFTREEITEMKEY=" + URLEncoder.encode(value);
			}
			
			try{
				if ( target == null )
					thisApplet.getAppletContext().showDocument( new URL(url) );
				else
					thisApplet.getAppletContext().showDocument( new URL(url), target );
			}catch(Exception E){}
		}
	}

	public static void main(String[] args) {
		try{
			new FrameLauncher("Tree", new TreeApplet(null), 320, 320);
		}catch(Exception E){
			E.printStackTrace();
		}
	}
}
