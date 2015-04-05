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
import java.awt.Color;
import java.awt.Font;
import java.awt.TextField;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import netscape.javascript.JSObject;

public class TextInput implements KeyListener{
  JSObject win;
  String 	align,
  				name,
  				value,
  				message,
  				error,
  				onValidate,
  				font,
          formName,
          objectName,
  				notSupportedMessage;  //value = textfield default value or current value;
  int 		size,
  				maxLength,
  				fontSize,
  				min,max,
  				validateType,
  				width,height; 
  boolean required,
  				bold,
  				italic,
  				hasFocus = false;
  Color 	backCol = Color.white;
  TextField field;
  Applet parent;
  
/*
**a text box that can support additional validation routines
*/

  public TextInput(String _formName, String _objectName, Applet _parent){
    field = new TextField();
    setBackCol( Color.white );
    win = JSObject.getWindow(_parent);     
    field.addKeyListener( this );
    formName = _formName;
    objectName = _objectName;
  }

  //--get and set methods
	@SuppressWarnings("deprecation")
	public void setType( String _type ){
		if ( _type != null)
			if ( _type.equalsIgnoreCase ("password") )
		  	field.setEchoCharacter('*'); 
	}


  public void setValidateType ( int _thisType ){
  	validateType = _thisType;
  }
  
  public int getValidateType(){
  	return validateType;
  }
  
  public void setOnValidate(String _f){
    onValidate = _f;
  }
  
  public String getOnValidate( ){
    return onValidate;
  }
  
  public void setMaxLength( int _length ){
  	maxLength = _length;
  }
  
  public int getMaxLength(){
  	return maxLength;
  }
  
  public void setNotSupported( String _message ){
  	if ( _message != null)
  		notSupportedMessage = _message;
  	else 
  	 	notSupportedMessage = "Browser does not support Java";
  }
  
  public String getNotSupported( ){
  	return notSupportedMessage;
  }
  

  public void setOnError( String _error) {  
  	error = _error;
  }
  
  public String getOnError() {
  	return error;
  }
  public void setAlign( String _align ){
  	align = _align;
  }
  
  public String getAlign (){
  	return align;
  }
  
  public void setRange( int startValue, int endValue ) {
  	
  	if ( startValue != 0 )
  		setMin( startValue );
		else 
			setMin( 1 );
		
		if ( endValue != 0 )
			setMax( endValue );
  	else 
  		setMax ( 100 );
  }
  
  public String getRange(){
  	return ( "" + getMin() + " to " + getMax() );
  }
  
  public void setName(String _s){
    name = _s;
  }
  public String getName(){
    return name;
  }
  
  public void setValue(String _v){
    value = _v;
    field.setText(value);
  }
  
  public String getValue(){
    System.out.println("gettext="+field.getText());
    return field.getText();
  }
  
  public void setRequired(boolean _b){
    required = _b;
  }	
  public boolean getRequired(){
    return required;
  }
  public void setMin(int _mn){
    min= _mn;
  }
  public int getMin(){
    return min;
  }
  public void setMax(int _mx){
    max= _mx;
  }
  public int getMax(){
    return max;
  }
  
  public void setBackCol(Color _c){
    field.setBackground(_c);
  }
  
  public Color getBackCol(){
    return field.getBackground();
  }
  
  public void setTextCol(Color _c){
    field.setForeground(_c);
  }
  
  public Color getTextCol(){
    return field.getForeground();
  }
  
  
  public void setMessage(String _m){
    message = _m;
  }
  public String getMessage(){
    return message;
  }
  
  public void setHeight(int _h){
    height = _h;
  }
  
  public int getHeight(){
    return height;
  }
  
  public void setWidth(int _w){
    width = _w;
  }
  
  public int getWidth(){
    return width;
  }
  
  public void setSize(){
    field.setSize( getWidth(), getHeight() );
  }
  
  public void setBold(boolean _b){
    bold = _b;
  }
  
  public boolean getBold(){
    return bold;
  }
  
  public void setItalic( boolean _italic ){
    italic = _italic;
  }

  public boolean getItalic(){
    return italic;
  }
  
  public void hasFocus(boolean _b){
    hasFocus =_b;
  }
  
  public boolean hasFocus(){
    return hasFocus;
  }
  
  public void font(String _font){
    //System.out.println("fontSize="+getFontSize());
    try{
      if(getBold() && getItalic())
        field.setFont(new Font(_font,Font.ITALIC+Font.BOLD, getFontSize()));
      else if (getBold())
        field.setFont(new Font(_font,Font.BOLD, getFontSize())); 
      else if (getItalic())
        field.setFont(new Font(_font,Font.ITALIC, getFontSize())); 
      else
        field.setFont(new Font(_font,Font.PLAIN, getFontSize())); 
    }catch(Exception e){
    }
  }
  public Font font(){
    return field.getFont();
  }
  
  
  public void setFontSize(int _fs){
    fontSize = _fs;
  }
  
  public int getFontSize(){
    return fontSize;
  }
 
  public void keyReleased( KeyEvent e ){
    //System.out.println(getValue());
    setValue();
  }

   private void setValue(){    
    //System.out.println(getValue());
    Object testArray[] = new Object[3];
    testArray[0] = formName;
    testArray[1] = objectName;
    testArray[2] = getValue();   
    win.call( "tf_setFormParam", testArray );	   
  }




  //--helper methods
  
  /* method in range, checks that a value in the textfield is within the range
  ** specified for that textfield.
  **
  **params _val= the current value of the textfield
  **
  **returns true or false
  */
  
  public boolean inRange(int _val){
    try{
     if(_val>=getMin() && _val<=getMax())
      return true;
    }catch(Exception e){
      return false;
    }
    return false;
  }
  
  /*Validate Input
  **Validates the input to a textfield according to the user selected function
  **
  **returns true or false
  */
  public boolean validateInput(){
    int fn = getValidateType(); 
    if( fn == 0 || fn == -1)
      return true;
    boolean b = false;
    switch(fn){
      
      case 1: b = Validation.validateCreditCard (getText());break;
      case 2: b = Validation.checkTime  				(getText());break;
      case 3: b = Validation.checkEUDate				(getText());break;
      case 4: b = Validation.checkUSDate				(getText());break;
      case 5: b = Validation.checkInteger 			(getText());break;
      case 6: b = Validation.checkFloat   			(getText());break;
      case 7: b = Validation.checkZipCode 			(getText());break;
      case 8: b = Validation.checkSocialSecNo		(getText());break;
      case 9: b = Validation.checkTelNo   			(getText());break;
   
      default: return true;
    }
   	return b;
  }  
  
  public String getText(){
    return field.getText();
  }
  
  public TextField getTextField(){
    return field;
  }
  
  public void keyTyped( KeyEvent e ){}
  public void keyPressed( KeyEvent e ){}

  //--modify the text color
}
