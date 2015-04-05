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

package com.naryx.tagfusion.cfm.sql;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

/**
 * cfSTOREDPROC implements the CFSTOREDPROC tag of ColdFusion.  
 */

public class cfPROCRESULT extends cfTag implements Serializable{

  static final long serialVersionUID = 1;

  protected void defaultParameters( String _tag ) throws cfmBadFileException {
		defaultAttribute( "MAXROWS", -1 );
		defaultAttribute( "RESULTSET", 1 );
    parseTagHeader( _tag );

    if ( !containsAttribute("NAME") )
  	  throw newBadFileException( "Missing Attribute", "You need to provide a NAME for the query" );
  }

  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
    cfStoredProcData storedProcData = (cfStoredProcData)_Session.getDataBin( cfSTOREDPROC.DATA_BIN_KEY );

    if( storedProcData == null )
      throw newRunTimeException( "CFPROCPARAM must be nested inside tag CFSTOREDPROC");
  
    storedProcData.setResultSet( getDynamic(_Session, "NAME").getString(), getDynamic(_Session, "RESULTSET").getInt(), getDynamic(_Session, "MAXROWS").getInt()  );
    
    return cfTagReturnType.NORMAL;
	}	
}
