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


/**
 * Wrapper class to the xmlElements attributes.
 */

  
public class TagAttributeElement extends Object{  
  
  private String name;
  private boolean required;
  private boolean supported;
  private String info;
  private String errorMessage;
  
  public TagAttributeElement(){
		required 			= false;
		supported 		= false;
		info					= "";
		errorMessage 	= "";
  }
  
  public String getName(){ return name; }
  public boolean getRequired(){ return required; }
  public boolean getSupported(){ return supported; }
  public String getInfo(){ return info; }
  public String getErrorMessage(){ return errorMessage; }

  public void setName(String _name){ name = _name.toUpperCase(); }
  public void setRequired(String _required){ required = com.nary.util.string.convertToBoolean( _required, false ); }
  public void setSupported(String _supported){ supported = com.nary.util.string.convertToBoolean( _supported, false ); }
  public void setInfo(String _info){ info = _info; }
  public void setErrorMessage(String _errorMessage){ errorMessage = _errorMessage; }

}
