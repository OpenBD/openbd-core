/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

/** 
 * This class handles dynamic datasources used for accessing database resources.
 */

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfDynamicDataSource extends cfDataSource implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
  public cfDynamicDataSource( String _DataSource, String _ConnectString ) throws cfmRunTimeException {
		throw new cfmRunTimeException( catchDataFactory.generalException( "errorCode.runtimeError", "Dynamic data sources are not supported by BD/Java" ));
	}
	
  public cfDynamicDataSource( String _DataSource, cfSession _Session, String _ConnectString ) throws cfmRunTimeException {
		throw new cfmRunTimeException( catchDataFactory.generalException( "errorCode.runtimeError", "Dynamic data sources are not supported by BD/Java" ));
	}
}
