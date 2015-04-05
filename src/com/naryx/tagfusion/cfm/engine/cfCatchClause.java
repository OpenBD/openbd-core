/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  $Id: cfCatchClause.java 2332 2013-02-25 20:26:14Z alan $
 */

package com.naryx.tagfusion.cfm.engine;

import com.naryx.tagfusion.cfm.tag.cfCATCH;


public class cfCatchClause implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
  protected String type;
  private cfCATCH parent;

  protected cfCatchClause(){}

  public cfCatchClause( cfCATCH _parent ){
    parent = _parent;
    type = _parent.getConstant( "TYPE" );
  }


  public cfCATCH getParentTag(){
    return parent;
  }
  
  public String getType(){
    return type;
  }
  
  public boolean isCatchAny() {
    // Java exception types are case-sensitive, CFML exception types are not
    return type.equalsIgnoreCase( "any" );
  }

  public boolean checkType( String _type ){
    String thisType = type.toLowerCase();
    if ( _type.equalsIgnoreCase( thisType ) )
      return true;
    else if ( thisType.equals("any") || thisType.equals("application") || thisType.length() == 0 ){
      return true;  
    }
    return false;
  }

  // returns true if the given exception matches the TYPE attribute or
  // is a subclass of the type attribute
  public boolean checkException( String exception ){
    try{
      Class typeExc = Class.forName( type );
      Class javaexc = Class.forName( exception );
      return typeExc.isAssignableFrom( javaexc );
    }catch( ClassNotFoundException ce ){
      return false;
    }
  }
  
  
  public boolean checkCustomType( String throwType ){
    String catchType = type.toLowerCase();
    if ( throwType.equalsIgnoreCase(catchType) ){
      return true;
    }

    int c1;
    while ( ( c1 = throwType.lastIndexOf(".") ) != -1 ){
      throwType = throwType.substring( 0, c1 );
      if ( throwType.equalsIgnoreCase(catchType) ){
        return true;
      }
    }
    return false;
  }
}
