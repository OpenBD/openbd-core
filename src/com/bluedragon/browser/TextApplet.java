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

package com.bluedragon.browser;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;

public class TextApplet extends Applet{
	private static final long serialVersionUID = 1L;
	TextInput t;
  
  public void init(){
  int[] array;
   
    BufferApplet buffer = new BufferApplet();
    t = new TextInput(getParameter( "FORMNAME" ),getParameter( "OBJECTNAME" ), this);

    setLayout(new BorderLayout());
    array = buffer.stringToIntArray	( getParameter( "range" ), "," );
    t.setType					( getParameter( "type" ) );
    t.setRange   			( array[0], array [1] );
    t.setRequired 		( buffer.decodeBoolean( getParameter( "required" ), false ) );
  	t.setValue  		 	( getParameter	( "value" ) );
  	String message = getParameter	( "message" );
  	
  	if ( message == null || message.length() == 0 )
  	  message = "Error cfValidateText";
  	  
  	t.setMessage 			( message );
  	t.setOnError			( getParameter 	( "onerror" ) );
    t.setNotSupported	( getParameter 	( "notsupported" ) );
    t.setSize();
  	t.setAlign				( buffer.determineAlign		( getParameter( "align" ) ) );
    t.setWidth				(	getSize().width);
    t.setHeight				(	getSize().height); 
    t.setBackCol			( buffer.decodeColor	( getParameter( "bgcolor" ), Color.white )); 
    t.setTextCol			( buffer.decodeColor	( getParameter( "textcolor" ), Color.black ));
    t.setBold    			( buffer.decodeBoolean( getParameter( "bold" ), false) );
    t.setItalic  			( buffer.decodeBoolean( getParameter( "italic" ), false) );    
    t.setFontSize			( BufferApplet.convertToInteger( getParameter( "fontsize" ),	10) );
    t.font       			( getParameter( "font" ));
    t.setMaxLength		( BufferApplet.convertToInteger( getParameter( "maxlength" ), -1 ) );
    t.setValidateType ( buffer.checkValidateType ( getParameter ( "validate" ) ) );
		t.setOnValidate   ( getParameter ( "onvalidate" ) );
	  //System.out.println(t.getValue());
    t.hasFocus   			( buffer.decodeBoolean( getParameter("focus"), false ) );    
   
    this.add(t.getTextField());

   	add( "Center", t.getTextField() );

  }
  
  public void start(){
    if(t.hasFocus())
      t.getTextField().requestFocus();
  }
  
  public String getMsg(){
    if(t.getMessage() != null)
      return t.getMessage();
    return "no message";
  }   
  
  public boolean inRange(){
    try{
      int val = (new Integer(t.getText())).intValue();
      return t.inRange(val);
    }catch(Exception e){
      return false;
    }
  }

  public Color decodeColor(String _color, Color _default){
    try{
      return java.awt.Color.decode(_color);
    }catch(Exception e){
      return _default;
    }
  }
  
  public String getValue(){
    return t.getValue();
  }
  
  public boolean validateInput(){
    return t.validateInput();
  }
  
  public String getMessage(){
    return t.getMessage();
  }

}
