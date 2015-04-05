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

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;

public class cfIMPORT extends cfTag implements Serializable
{

  static final long serialVersionUID = 1;

  protected void defaultParameters(String _tag) throws cfmBadFileException {
    parseTagHeader(_tag);

    if ( containsAttribute( "PATH" ) ){
    	if ( containsAttribute( "PREFIX" ) || containsAttribute( "TAGLIB" ) ){
    		throw newBadFileException( "Invalid Attribute Combination", "The PATH attribute cannot be used with the PREFIX/TAGLIB attributes" );
    	}

      if ( getConstant( "PATH" ).indexOf('#') >= 0 ){
        throw newBadFileException( "Invalid Attribute", "The PATH attribute must have a constant value." );
      }
    }else{

	    if (!containsAttribute("PREFIX") )
	      throw this.missingAttributeException( "The CFIMPORT tag requires a PREFIX attribute to be specified.", null );
	      
	    if (!containsAttribute("TAGLIB") )
	          throw this.missingAttributeException( "The CFIMPORT tag requires a TAGLIB attribute to be specified.", null );
	  
	    if ( getConstant( "PREFIX" ).indexOf('#') >= 0 ){
	      throw newBadFileException( "Invalid Attribute", "The PREFIX attribute must have a constant value." );
	    }
	
	    if ( getConstant( "TAGLIB" ).indexOf('#') >= 0 ){
	      throw newBadFileException( "Invalid Attribute", "The TAGLIB attribute must have a constant value." );
	    }
    }
    
  }
  
  @Override
  protected void tagLoadingComplete() throws cfmBadFileException {
  	if ( containsAttribute( "PATH" ) ){
  		getFile().addImportPath( getPath() );
  	}
  }

	public String getPrefix(){
    return getConstant( "PREFIX" );
  }


  public String getDirectory(){
    return getConstant( "TAGLIB" );
  }

  public String getPath(){
    return getConstant( "PATH" );
  }
  
  //----------------------------------------------------

  public cfTagReturnType render( cfSession _Session ) {
	  return cfTagReturnType.NORMAL;
  }

}
