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

public class cfCHARTLEGEND extends cfTag implements Serializable
{
	static final long serialVersionUID = 1;
	
	private Map<String, String> nonDefaultAttributes;

	@SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
		return createInfo("output", "The child tag of CFCHART. Used to set the legend of a CFCHART object.");
	}
  
	@SuppressWarnings("unchecked")
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
			createAttInfo( 	"BACKGROUNDCOLOR", 	
											"Background colour of the legend. This can be a hexadecimal colour " +
											"or one of the following predefined colours: " +
											"aliceblue, antiquewhite, aqua, aquamarine, azure, beige, bisque, black, " +
											"blanchedalmond, blue, blueviolet, brown, burlywood, cadetblue, chartreuse, " +
											"chocolate, coral, cornflowerblue, cornsilk, crimson, cyan, darkblue, darkcyan, " +
											"darkgoldenrod, darkgray, darkgreen, darkgrey, darkkhaki, darkmagenta, darkolivegreen, " +
											"darkorange, darkorchid, darkred, darksalmon, darkslateblue, darkslategray, " +
											"darkslategrey, darkturquoise, darkviolet, deeppink, deepskyblue, dimgray, dimgrey, " +
											"dodgerblue, firebrick, floralwhite, forestgreen, fuchsia, gainsboro, ghostwhite, " +
											"gold, goldenrod, gray, green, greenyellow, grey, honeydew, hotpink, indianred, " +
											"indigo, ivory, khaki, lavender, lavenderblush, lawngreen, lemonchiffon, lightblue, " +
											"lightcoral, lightcyan, lightgoldenrodyellow, lightgray, lightgreen, lightgrey, " +
											"lightpink, lightsalmon, lightseagreen, lightskyblue, lightslategray, lightslategrey, " +
											"lightsteelblue, lightyellow, lime, limegreen, linen, magenta, maroon, mediumaquamarine, " +
											"mediumblue, mediumorchid, mediumpurple, mediumseagreen, mediumslateblue, mediumspringgreen, " +
											"mediumturquoise, mediumvioletred, midnightblue, mintcream, mistyrose, moccasin, navajowhite, " +
											"navy, oldlace, olive, olivedrab, orange, orangered, orchid, palegoldenrod, palegreen, " +
											"paleturquoise, palevioletred, papayawhip, peachpuff, peru, pink, plum, powderblue, purple, " +
											"red, rosybrown, royalblue, saddlebrown, salmon, sandybrown, seagreen, seashell, sienna, " +
											"silver, skyblue, slateblue, slategray, slategrey, snow, springgreen, steelblue, tan, teal, " +
											"thistle, tomato, turquoise, violet, violetred, wheat, white, whitesmoke, yellow, yellowgreen", 	
 											"white", 
 											true ),
 			createAttInfo( 	"LABELCOLOR", 
						 					"Colour of the Legend Label. This can be a hexadecimal colour " +
											"or one of the following predefined colours: " +
											"aliceblue, antiquewhite, aqua, aquamarine, azure, beige, bisque, black, " +
											"blanchedalmond, blue, blueviolet, brown, burlywood, cadetblue, chartreuse, " +
											"chocolate, coral, cornflowerblue, cornsilk, crimson, cyan, darkblue, darkcyan, " +
											"darkgoldenrod, darkgray, darkgreen, darkgrey, darkkhaki, darkmagenta, darkolivegreen, " +
											"darkorange, darkorchid, darkred, darksalmon, darkslateblue, darkslategray, " +
											"darkslategrey, darkturquoise, darkviolet, deeppink, deepskyblue, dimgray, dimgrey, " +
											"dodgerblue, firebrick, floralwhite, forestgreen, fuchsia, gainsboro, ghostwhite, " +
											"gold, goldenrod, gray, green, greenyellow, grey, honeydew, hotpink, indianred, " +
											"indigo, ivory, khaki, lavender, lavenderblush, lawngreen, lemonchiffon, lightblue, " +
											"lightcoral, lightcyan, lightgoldenrodyellow, lightgray, lightgreen, lightgrey, " +
											"lightpink, lightsalmon, lightseagreen, lightskyblue, lightslategray, lightslategrey, " +
											"lightsteelblue, lightyellow, lime, limegreen, linen, magenta, maroon, mediumaquamarine, " +
											"mediumblue, mediumorchid, mediumpurple, mediumseagreen, mediumslateblue, mediumspringgreen, " +
											"mediumturquoise, mediumvioletred, midnightblue, mintcream, mistyrose, moccasin, navajowhite, " +
											"navy, oldlace, olive, olivedrab, orange, orangered, orchid, palegoldenrod, palegreen, " +
											"paleturquoise, palevioletred, papayawhip, peachpuff, peru, pink, plum, powderblue, purple, " +
											"red, rosybrown, royalblue, saddlebrown, salmon, sandybrown, seagreen, seashell, sienna, " +
											"silver, skyblue, slateblue, slategray, slategrey, snow, springgreen, steelblue, tan, teal, " +
											"thistle, tomato, turquoise, violet, violetred, wheat, white, whitesmoke, yellow, yellowgreen", 
 											"black", 
 											false ) ,
 			createAttInfo( 	"POSITION", 
 											"Position of the Legend on the CFCHART object. Options are: top, bottom, left or right", 
 											"top", 
 											false ),
			createAttInfo( 	"SHOWBORDER", 
											"Whether to show the border of the legend box. Options: Yes or No.", 
											"no", 
											false ),
			createAttInfo( 	"FONT", 
											"Font of the title and legend. Options are: arial, times and courier", 
											"arial", 
											false ),
			createAttInfo( 	"FONTBOLD", 
											"Whether font is bold : yes or no", 
											"no", 
											false ),
			createAttInfo( 	"FONTITALIC", 
											"Whether font is italic : yes or no", 
											"no", 
											false ),
			createAttInfo( 	"FONTSIZE", 
											"Size of font", 
											"11", 
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
		cfTag defaultChartLegendTag = chartData.getDefaultChartLegendTag();
		
		// Set the default values for attributes that were not set
		if ( !nonDefaultAttributes.containsKey( "BACKGROUNDCOLOR" ) )
		{
			if ( ( defaultChartLegendTag != null ) && ( defaultChartLegendTag.containsAttribute( "BACKGROUNDCOLOR" ) ) )
				defaultAttribute( "BACKGROUNDCOLOR", defaultChartLegendTag.getConstant( "BACKGROUNDCOLOR" ) );
			else
				defaultAttribute( "BACKGROUNDCOLOR", "white" );
		}
		if ( !nonDefaultAttributes.containsKey( "LABELCOLOR" ) )
		{
			if ( ( defaultChartLegendTag != null ) && ( defaultChartLegendTag.containsAttribute( "LABELCOLOR" ) ) )
				defaultAttribute( "LABELCOLOR",	defaultChartLegendTag.getConstant( "LABELCOLOR" ) );
			else
				defaultAttribute( "LABELCOLOR", "black" );
		}
		if ( !nonDefaultAttributes.containsKey( "POSITION" ) )
		{
			if ( ( defaultChartLegendTag != null ) && ( defaultChartLegendTag.containsAttribute( "POSITION" ) ) )
				defaultAttribute( "POSITION", defaultChartLegendTag.getConstant( "POSITION" ) );
			else
				defaultAttribute( "POSITION", "top" );
		}
		if ( !nonDefaultAttributes.containsKey( "SHOWBORDER" ) )
		{
			if ( ( defaultChartLegendTag != null ) && ( defaultChartLegendTag.containsAttribute( "SHOWBORDER" ) ) )
				defaultAttribute( "SHOWBORDER",	defaultChartLegendTag.getConstant( "SHOWBORDER" ) );
			else
				defaultAttribute( "SHOWBORDER", "no" );
		}
		if ( !nonDefaultAttributes.containsKey( "FONT" ) )
		{
			if ( ( defaultChartLegendTag != null ) && ( defaultChartLegendTag.containsAttribute( "FONT" ) ) )
				defaultAttribute( "FONT", defaultChartLegendTag.getConstant( "FONT" ) );
			else
				defaultAttribute( "FONT", "arial" );
		}
		if ( !nonDefaultAttributes.containsKey( "FONTBOLD" ) )
		{
			if ( ( defaultChartLegendTag != null ) && ( defaultChartLegendTag.containsAttribute( "FONTBOLD" ) ) )
				defaultAttribute( "FONTBOLD", defaultChartLegendTag.getConstant( "FONTBOLD" ) );
			else
				defaultAttribute( "FONTBOLD", "no" );
		}
		if ( !nonDefaultAttributes.containsKey( "FONTITALIC" ) )
		{
			if ( ( defaultChartLegendTag != null ) && ( defaultChartLegendTag.containsAttribute( "FONTITALIC" ) ) )
				defaultAttribute( "FONTITALIC",	defaultChartLegendTag.getConstant( "FONTITALIC" ) );
			else
				defaultAttribute( "FONTITALIC", "no" );
		}
		if ( !nonDefaultAttributes.containsKey( "FONTSIZE" ) )
		{
			if ( ( defaultChartLegendTag != null ) && ( defaultChartLegendTag.containsAttribute( "FONTSIZE" ) ) )
				defaultAttribute( "FONTSIZE", defaultChartLegendTag.getConstant( "FONTSIZE" ) );
			else
				defaultAttribute( "FONTSIZE", "11" );
		}
	}
	
	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
	    //--[ Get the internal chart data in which this relates to
	    cfCHARTInternalData chartData = (cfCHARTInternalData) _Session.getDataBin(cfCHART.DATA_BIN_KEY);
	    if (chartData == null)
	    	throw newRunTimeException("CFCHARTLEGEND must be used inside a CFCHART tag");
	    
	    if ( chartData.getLegendData() != null )
	    	throw newRunTimeException("There can only be one CFCHARTLEGEND tag inside a CFCHART tag");
		
	    setDefaultParameters( _Session, chartData );

		String pos = getDynamic(_Session,"POSITION").toString().toLowerCase();
	    if ( !pos.equals("top") && 
			 !pos.equals("bottom") &&
		     !pos.equals("left") &&
		     !pos.equals("right") )
		{
		    throw newBadFileException("Invalid POSITION Attribute", "The position attribute cannot have a value of '"+pos+"'" );
		}

		cfCHARTLEGENDData legendData = new cfCHARTLEGENDData(getDynamic(_Session,"BACKGROUNDCOLOR").toString(),
															 getDynamic(_Session,"LABELCOLOR").toString(),
															 pos,
															 getDynamic(_Session,"FONT").toString().toLowerCase(),
															 getDynamic(_Session,"FONTBOLD").getBoolean(),
															 getDynamic(_Session,"FONTITALIC").getBoolean(),
															 getDynamic(_Session,"FONTSIZE").getInt(),
															 getDynamic(_Session,"SHOWBORDER").getBoolean()
															 );
			
		chartData.setLegendData(legendData);
		
		return cfTagReturnType.NORMAL;
	}
}
