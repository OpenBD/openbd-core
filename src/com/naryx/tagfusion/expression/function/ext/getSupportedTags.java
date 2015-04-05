/* 
 *  Copyright (C) 2000 - 2009 TagServlet Ltd
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

package com.naryx.tagfusion.expression.function.ext;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.expression.function.functionBase;

public class getSupportedTags extends functionBase {

  private static final long serialVersionUID = 1L;

  public getSupportedTags(){
     min = 0; max = 1;
  }
  
	public String[] getParamInfo(){
		return new String[]{
			"Category of tag to filter on"	
		};
	}
  
	public java.util.Map getInfo(){
		return makeInfo(
				"engine", 
				"Returns back all the core tags, optionally filtered on the given category", 
				ReturnType.ARRAY );
	}
  
  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{    
		String category	= null;
		if ( parameters.size() == 1 )
			category	= parameters.get(0).getString().toLowerCase().trim();

  	cfArrayData	arrData = cfArrayData.createArray( 1 );
  	Vector HT = com.naryx.tagfusion.cfm.engine.cfEngine.thisInstance.TagChecker.getSupportedTags();
		
		if ( category == null || category.length() == 0 ){
		
			Enumeration E = HT.elements();
			while ( E.hasMoreElements() ){
				arrData.addElement( new cfStringData( ((String)E.nextElement()).toUpperCase() ) );			
			}
			
		} else {
			
			Enumeration E = HT.elements();
			while ( E.hasMoreElements() ){
				String tagName	= (String)E.nextElement(); 
				
				cfTag tag = null;
				try{
					Class<?> C = Class.forName( com.naryx.tagfusion.cfm.engine.cfEngine.thisInstance.TagChecker.getClass( tagName ) ); 
					tag = (cfTag)C.newInstance();
					
					if ( tag.getInfo().get("category").equals(category) ){
						arrData.addElement( new cfStringData( tagName ) );
					}

				}catch (Exception e){}
			}
			
		}
		
		//--[ Need to sort array
		arrData.sortArray( "text", "asc" );
		return arrData;
  }
}
