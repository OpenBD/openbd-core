/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  $Id: $
 */

package com.naryx.tagfusion.cfm.tag;

/**
 * This implements the CFASSOCIATE tag. A tag used by nested custom tags
 * to pass it's own attributes to it's parent custom tag.
 */

import java.io.Serializable;

import org.aw20.collections.FastStack;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.customTagVariableWrapper;

public class cfASSOCIATE extends cfTag implements Serializable {
	static final long serialVersionUID = 1;
  
	public static final String DATA_BIN_KEY = "CFASSOCIATE_DATA";


  @SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
  	return createInfo("control", "Used by customtags, to its own attributes to a parent custom tag");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
   			createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
   			createAttInfo( "BASETAG", "The tag to associate with", 	"", true ),
   			createAttInfo( "DATACOLLECTION", "Structure used to store the data", 	"assocattribs", false )
  	};
  }

	
	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		defaultAttribute( "DATACOLLECTION", "assocattribs" );
		parseTagHeader( _tag );
		
		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;
		
		if ( !containsAttribute("BASETAG") )
			throw newBadFileException( "Missing BASETAG", "CFASSOCIATE requires a BASETAG attribute to be specified." );
	}
  
  
	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if ( !containsAttribute(attributes,"BASETAG") )
			throw newBadFileException( "Missing BASETAG", "CFASSOCIATE requires a BASETAG attribute to be specified." );

		return	attributes;
	}

	
	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);
		
		String basetag = getDynamic( attributes, _Session, "BASETAG" ).getString().toUpperCase();
		String datacollectionName = getDynamic( attributes, _Session, "DATACOLLECTION" ).getString();
					
		// loop thru the data stack stored in the cfSession. It will contain the attributes
		// for the required parentTag
		FastStack<customTagVariableWrapper> vStack  = _Session.getBaseTagData(); 
		cfData currentTagAttrs = vStack.peek().variables.getData( "attributes" );
		cfData customTagVars = null;
    
		for ( int x=vStack.size()-1; x > 0; x-- ){
			customTagVariableWrapper cTW  = (customTagVariableWrapper)vStack.elementAt(x-1);
			if ( cTW.tagName.equalsIgnoreCase( basetag ) ){
				customTagVars = cTW.variables;
				break;
			}
		}

		if ( customTagVars == null ){
			throw this.newRunTimeException( "Invalid base tag specified. Check that the custom tag " + basetag + " exists." ); 
		}

		// now get the datacollection
		cfData thisTagData = customTagVars.getData( "thistag" );
		cfArrayData theDataColl = (cfArrayData) thisTagData.getData( datacollectionName );
		// again if it doesn't exist, create it
		if ( theDataColl == null){
			theDataColl = cfArrayData.createArray(1);
			thisTagData.setData( datacollectionName, theDataColl );
		}
          
		theDataColl.addElement( currentTagAttrs );
    	
		return cfTagReturnType.NORMAL;
	}
}
