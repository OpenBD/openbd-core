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

package com.naryx.tagfusion.cfm.tag.awt;

import java.io.Serializable;
import java.util.Map;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfCHARTIMAGE extends cfTag implements Serializable
{
	static final long serialVersionUID = 1;
	
	private Map<String, String> nonDefaultAttributes;

	@SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
		return createInfo("output", "The child tag of CFCHART. Used to set the background of a CFCHART.");
	}
  
	@SuppressWarnings("unchecked")
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
			createAttInfo( 	"FILE", 	
											"The name of the image that you want to have as the graph's background image. " +
 											"Must be either a JPG, JPEG or GIF file.", 	
 											"", 
 											true ),
 			createAttInfo( 	"URIDIRECTORY", 
 											"Whether the path given for the file is absolute or relative. Yes = relative, No = absolute.", 
 											"no", 
 											false ) ,
 			createAttInfo( 	"ALIGNMENT", 
 											"Options are: top_left, top, top_right, left, center, right, " +
 											"bottom_left, bottom, bottom_right, fit, fit_horizontal or fit_vertical", 
 											"fit", 
 											false )
		};
	}	
	
	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		
		parseTagHeader( _tag );
		
		// Keep track of the attributes that don't use default values
		nonDefaultAttributes = new FastMap<String, String>( properties );
		
		// NOTE: the default values need to be set at render time in setDefaultParameters()
	}
	
	private void setDefaultParameters( cfSession _Session, cfCHARTInternalData chartData ) {
		unclipAttributes();
		cfTag defaultChartImageTag = chartData.getDefaultChartImageTag();
		
		// Set the default values for attributes that were not set
		if ( !nonDefaultAttributes.containsKey( "FILE" ) )
		{
			if ( ( defaultChartImageTag != null ) && ( defaultChartImageTag.containsAttribute( "FILE" ) ) )
				defaultAttribute( "FILE", defaultChartImageTag.getConstant( "FILE" ) );
			else
				removeAttribute( "FILE" );
		}
		if ( !nonDefaultAttributes.containsKey( "URIDIRECTORY" ) )
		{
			if ( ( defaultChartImageTag != null ) && ( defaultChartImageTag.containsAttribute( "URIDIRECTORY" ) ) )
				defaultAttribute( "URIDIRECTORY",	defaultChartImageTag.getConstant( "URIDIRECTORY" ) );
			else
				defaultAttribute( "URIDIRECTORY", "no" );
		}
		if ( !nonDefaultAttributes.containsKey( "ALIGNMENT" ) )
		{
			if ( ( defaultChartImageTag != null ) && ( defaultChartImageTag.containsAttribute( "ALIGNMENT" ) ) )
				defaultAttribute( "ALIGNMENT", defaultChartImageTag.getConstant( "ALIGNMENT" ) );
			else
				defaultAttribute( "ALIGNMENT", "fit" );
		}
	}
	
	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
	    //--[ Get the internal chart data in which this relates to
	    cfCHARTInternalData chartData = (cfCHARTInternalData) _Session.getDataBin(cfCHART.DATA_BIN_KEY);
	    if (chartData == null)
	    	throw newRunTimeException("CFCHARTIMAGE must be used inside a CFCHART tag");
	    
	    if ( chartData.getImageData() != null )
	    	throw newRunTimeException("There can only be one CFCHARTIMAGE tag inside a CFCHART tag");
		
	    setDefaultParameters( _Session, chartData );
	    
	    if ( !containsAttribute("FILE") )
	    	throw newRunTimeException("CFCHARTIMAGE must contain a FILE attribute");

		String pos = getDynamic(_Session,"ALIGNMENT").toString().toLowerCase();
	    if ( 	 !pos.equals("top_left") && 
				 !pos.equals("top") &&
				 !pos.equals("top_right") &&
				 !pos.equals("left") &&
				 !pos.equals("center") &&
				 !pos.equals("right") &&
				 !pos.equals("bottom_left") &&
				 !pos.equals("bottom") &&
				 !pos.equals("bottom_right") &&
				 !pos.equals("fit") &&
				 !pos.equals("fit_horizontal") &&
				 !pos.equals("fit_vertical") )
		{
		    throw newBadFileException("Invalid ALIGNMENT Attribute", "The ALIGNMENT attribute cannot have a value of '"+pos+"'" );
		}

		cfCHARTIMAGEData imageData = new cfCHARTIMAGEData(getDynamic(_Session,"FILE").toString(),
															 getDynamic(_Session,"URIDIRECTORY").getBoolean(),
															 pos
															 );
			
		chartData.setImageData(imageData);
		
		return cfTagReturnType.NORMAL;
	}
}
