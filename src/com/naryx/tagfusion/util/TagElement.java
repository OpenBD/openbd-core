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

package com.naryx.tagfusion.util;

import java.util.Vector;


/**
 * Wrapper class to the XML tag values.
 *
 */

  
public class TagElement extends Object{  
  
  private String  name = "";
  private boolean endTag = false;
  private String  tagClass = null;
  private boolean supported = false;
  private String  info = "";
  private String  errorMessage = "";
  private Vector  attributes;
  private boolean plugin = false;

	public TagElement( String _name, boolean _supported, String _tagClass ){
	    this( _name, _supported, _tagClass, false, false );
	  }

 	public TagElement( String _name, boolean _supported, String _tagClass, boolean _endTag ){
 	    this( _name, _supported, _tagClass, _endTag, false );
 	  }

 	public TagElement( String _name, boolean _supported, String _tagClass, boolean _endTag, boolean _plugin ){
		name			= _name;
		supported	= _supported;
		tagClass	= _tagClass;
		endTag    = _endTag;
    	plugin = _plugin;
	}
	
  public TagElement(){ 
    attributes = new Vector();
  }
  
  public void addAttribute( TagAttributeElement _attr ){
    attributes.addElement( _attr );
  }

  public String getName(){ return name; }
  public String getTagClass(){ return tagClass; }
  public boolean getSupported(){ return supported; }
  public boolean hasEndTag(){ return endTag; }
  public String getInfo(){ return info; }
  public String getErrorMessage(){ return errorMessage; }
  public Vector getAttributes(){ return attributes; }
  public boolean isPlugin() { return plugin; }

  public void setName(String _name){ name = _name.toUpperCase(); }
  public void setTagClass(String _tagClass){ tagClass = _tagClass; }
  public void setSupported(String _supported){ supported = com.nary.util.string.convertToBoolean( _supported, false ); }
  public void setErrorMessage(String _errorMessage){ errorMessage = _errorMessage; }
  public void setInfo(String _info){ info = _info; }

  public String toString(){
    return "TagElement:" + name + ";" + tagClass + "; " + supported;
  }
}


