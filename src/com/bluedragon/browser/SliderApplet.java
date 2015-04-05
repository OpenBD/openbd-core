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

import netscape.javascript.JSObject;

import com.bluedragon.browser.thinlet.Thinlet;


public class SliderApplet extends Thinlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Applet		thisApplet;
	private JSObject 	win = null;
	private String		label;
	private int				currentValue;

	public SliderApplet(Applet parentApp) throws Exception {
		thisApplet		= parentApp;
		label					= thisApplet.getParameter("LABEL");
		win 					= JSObject.getWindow(thisApplet);   

		System.out.println("<CFSLIDER> BlueDragon 2004. http://www.newatlanta.com/");
		System.out.println("Based on Thinlet Technology,   http://www.thinlet.com/");

		//-- The appendkey
		currentValue = 0;
		try{
			if ( thisApplet.getParameter("VALUE") != null )
				currentValue	= Integer.parseInt( thisApplet.getParameter("VALUE") );
		}catch(Exception E){}

		setFont();

		if ( label != null ){
			add( parse("sliderLabel.xml",this));
			setLabel( currentValue );
		}else
			add( parse("slider.xml",this) );


		//-- Set the range
		if ( thisApplet.getParameter("RANGE") != null ){
			String range	 = thisApplet.getParameter("RANGE");
			int c1 = range.indexOf(",");
			if ( c1 != -1 ){
				try{	setInteger( find("rootSlider"), "minimum", Integer.parseInt( range.substring(0,c1) ) );		}catch(Exception E){}				
				try{	setInteger( find("rootSlider"), "maximum", Integer.parseInt( range.substring(c1+1) ) );		}catch(Exception E){}				
			}
		}
		
		
		//-- Set the orientation
		if ( thisApplet.getParameter("VERTICAL") != null ){
			boolean bVertical = false;
			try{ bVertical = Boolean.valueOf(thisApplet.getParameter("VERTICAL")).booleanValue(); }catch(Exception E){}
			Object slider = find("rootSlider");
			
			if ( bVertical ){
				setChoice( slider, "orientation", "vertical");
				setInteger( slider, "weighty", 1 );			
				setInteger( slider, "weightx", 0 );			
			}else{
				setInteger( slider, "weightx", 1 );
			}
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

	public void action(Object slider){
		
		currentValue	= getInteger(slider,"value");
		
		//-- Update the label if present
		if ( label != null )
			setLabel( currentValue );
		
		
		if ( win != null ){
			Object testArray[] = new Object[3];
			testArray[0] = thisApplet.getParameter("FORMNAME");
			testArray[1] = thisApplet.getParameter("OBJECTNAME");
			testArray[2] = currentValue + "";
			win.call( "tf_setFormParam", testArray );
		}	 
	}

	private void setLabel(int newValue){
		int c1 = label.indexOf("%value%");
		if ( c1 != -1 ){
			String newLabel	= label.substring(0,c1);
			newLabel	+= newValue + "";
			newLabel 	+= label.substring( c1 + 7 );
			setString( find("rootLabel"), "text", newLabel );
		}else
			setString( find("rootLabel"), "text", label );
	}
}
