/* 
 *  Copyright (C) 2011 TagServlet Ltd
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
 *  
 *  $Id: $
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/**
 * This class implements the CFSWITCH class. It relies on the
 * CFCASE,CFDEFAULTCASE to be present in its tags.
 */

public class cfSWITCH extends cfTag implements Serializable {
	static final long serialVersionUID = 1;

	private transient FastMap<String, cfTag> caseMap = null;

	private transient cfDEFAULTCASE defaultCase;

	@SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
		return createInfo("control", "Evaluates a specified expression. Parent tag of CFCASE and CFDEFAULTCASE");
	}
  
	@SuppressWarnings("unchecked")
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
			createAttInfo("EXPRESSION", "The expression to be evaluated by the CFSWITCH tag", "", true ),
			createAttInfo("BREAK", 			"Flag to determine if a match is found whether or not to stop when found, or drop through to the next one if set to false.", "true", false )
		};
	}
  
  protected void defaultParameters(String _tag) throws cfmBadFileException {
  	defaultAttribute( "BREAK",  "TRUE" );
  	
  	parseTagHeader(_tag);

    if (!containsAttribute("EXPRESSION"))
      throw newBadFileException("Missing EXPRESSION", "You must specify the EXPRESSION attribute");
    
    if ( this.getConstantAsBoolean("BREAK") )
    	caseMap = new FastMap<String, cfTag>( FastMap.CASE_INSENSITIVE );
  }

  public String getEndMarker() {
    return "</CFSWITCH>";
  }

  protected void tagLoadingComplete() throws cfmBadFileException {
    cfTag tagList[] = getTagList();
    for (int i = 0; i < tagList.length; i++) {
      cfTag tag = tagList[i];
      if (caseMap != null && tag instanceof cfCASE) {

        String[] values = ((cfCASE)tag).getValues();
        for (int j = 0; j < values.length; j++) {
          if (caseMap.containsKey(values[j])) {
            caseMap = null;
            cfCatchData catchData = new cfCatchData();
            catchData.setType( cfCatchData.TYPE_TEMPLATE );
            catchData.setTagname( tag.getTagName() );
            catchData.setLine( tag.posLine  );
            catchData.setColumn( tag.posColumn  );
            catchData.setMessage( "CFSWITCH has duplicate CFCASE values" );
            catchData.setDetail( "Duplicate CFCASE value: \"" + values[j] + "\"" );
            throw new cfmBadFileException( catchData );
          }
          caseMap.put(values[j], tag);
        }

      } else {
        if (tag instanceof cfDEFAULTCASE) {
          defaultCase = (cfDEFAULTCASE) tag;
        }
      }
    }
  }

  
	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		// Evaluate the expression
		String expr = getDynamic( _Session, "EXPRESSION" ).toNormalString();

		if ( caseMap != null )
			return renderOptimzedSwitch( expr, _Session );
		else
			return renderDropThruSwitch( expr, _Session );
	}
		

	private cfTagReturnType renderDropThruSwitch(String expr, cfSession _Session ) throws cfmRunTimeException {

		cfTagReturnType rt = cfTagReturnType.NORMAL;
		boolean	dropThruExec = false;
		
    cfTag tagList[] = getTagList();
    
    for (int i = 0; i < tagList.length; i++) {
      cfTag tag = tagList[i];
      if (tag instanceof cfCASE) {

      	if ( dropThruExec ){
      		
      		_Session.pushTag( tag );
      		rt = tag.render( _Session );
      		_Session.popTag();
      		if ( rt.isBreak() )
    				return cfTagReturnType.NORMAL;
      		
      	}else{

      		String[] values = ((cfCASE)tag).getValues();
          for (int j = 0; j < values.length; j++) {
          	if ( expr.equals(values[j]) ){
          		_Session.pushTag( tag );
        			rt = tag.render( _Session );
        			_Session.popTag();
        			if ( rt.isBreak() )
        				return cfTagReturnType.NORMAL;
        			else
        				dropThruExec = true;
          	}
          }
      		
      	}
      }
    }

    
    // Got here, but no CASE was matched
    if ( defaultCase != null ){
    	_Session.pushTag( defaultCase );
			rt = defaultCase.render( _Session );
			_Session.popTag();
    }
    
    return rt;
	}
	
	
	private cfTagReturnType renderOptimzedSwitch(String expr, cfSession _Session ) throws cfmRunTimeException {
		cfCASE thisCase = (cfCASE)caseMap.get( expr );
		cfTagReturnType rt = null;

		if ( thisCase != null ) {
			_Session.pushTag( thisCase );
			rt = thisCase.render( _Session );
		} else if ( defaultCase != null ) {
			_Session.pushTag( defaultCase );
			rt = defaultCase.render( _Session );
		} else {
			// we didn't do anything, so don't invoke _Session.popTag() or return rt
			return cfTagReturnType.NORMAL;
		}

		_Session.popTag();
		return rt;
	}
}
