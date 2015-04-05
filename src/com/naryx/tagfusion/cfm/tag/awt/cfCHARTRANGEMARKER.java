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

public class cfCHARTRANGEMARKER extends cfTag implements Serializable
{
	static final long serialVersionUID = 1;
	
	private Map<String, String> nonDefaultAttributes;
	
	@SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
		return createInfo("output", "The child tag of CFCHART. Used to add a range marker to a CFCHART.");
	}
  
	@SuppressWarnings("unchecked")
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
			createAttInfo( 	"COLOR", 	
											"Background colour of the range marker. This can be a hexadecimal colour " +
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
 											"gray", 
 											false ),
			createAttInfo( 	"START", 	
											"Start point of the marker.", 	
											"", 
											true ),
			createAttInfo( 	"END", 	
											"End point of the marker.", 	
											"", 
											true ),
			createAttInfo( 	"LABEL", 	
											"The label text of the marker.", 	
											"", 
											false ),
			createAttInfo( 	"LABELCOLOR", 	
											"Colour of the text of the label. This can be a hexadecimal colour " +
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
											false ),
			createAttInfo( 	"LABELPOSITION", 	
											"Position of the range marker label. Options are: top_left, top, top_right, left, center, " +
											"right, bottom_left, bottom or bottom_right", 	
											"center", 
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
		cfTag defaultChartRangeMarkerTag = chartData.getDefaultChartRangeMarkerTag();
		
		// Set the default values for attributes that were not set
		if ( !nonDefaultAttributes.containsKey( "COLOR" ) )
		{
			if ( ( defaultChartRangeMarkerTag != null ) && ( defaultChartRangeMarkerTag.containsAttribute( "COLOR" ) ) )
				defaultAttribute( "COLOR",	defaultChartRangeMarkerTag.getConstant( "COLOR" ) );
			else
				defaultAttribute( "COLOR",	"gray" );
		}
		if ( !nonDefaultAttributes.containsKey( "START" ) )
		{
			if ( ( defaultChartRangeMarkerTag != null ) && ( defaultChartRangeMarkerTag.containsAttribute( "START" ) ) )
				defaultAttribute( "START",	defaultChartRangeMarkerTag.getConstant( "START" ) );
			else
				removeAttribute( "START" );
		}
		if ( !nonDefaultAttributes.containsKey( "END" ) )
		{
			if ( ( defaultChartRangeMarkerTag != null ) && ( defaultChartRangeMarkerTag.containsAttribute( "END" ) ) )
				defaultAttribute( "END",	defaultChartRangeMarkerTag.getConstant( "END" ) );
			else
				removeAttribute( "END" );
		}
		if ( !nonDefaultAttributes.containsKey( "LABEL" ) )
		{
			if ( ( defaultChartRangeMarkerTag != null ) && ( defaultChartRangeMarkerTag.containsAttribute( "LABEL" ) ) )
				defaultAttribute( "LABEL",	defaultChartRangeMarkerTag.getConstant( "LABEL" ) );
			else
				removeAttribute( "LABEL" );
		}
		if ( !nonDefaultAttributes.containsKey( "LABELCOLOR" ) )
		{
			if ( ( defaultChartRangeMarkerTag != null ) && ( defaultChartRangeMarkerTag.containsAttribute( "LABELCOLOR" ) ) )
				defaultAttribute( "LABELCOLOR",	defaultChartRangeMarkerTag.getConstant( "LABELCOLOR" ) );
			else
				defaultAttribute( "LABELCOLOR", "black" );
		}
		if ( !nonDefaultAttributes.containsKey( "LABELPOSITION" ) )
		{
			if ( ( defaultChartRangeMarkerTag != null ) && ( defaultChartRangeMarkerTag.containsAttribute( "LABELPOSITION" ) ) )
				defaultAttribute( "LABELPOSITION",	defaultChartRangeMarkerTag.getConstant( "LABELPOSITION" ) );
			else
				defaultAttribute( "LABELPOSITION",	"center" );
		}
		if ( !nonDefaultAttributes.containsKey( "FONT" ) )
		{
			if ( ( defaultChartRangeMarkerTag != null ) && ( defaultChartRangeMarkerTag.containsAttribute( "FONT" ) ) )
				defaultAttribute( "FONT",	defaultChartRangeMarkerTag.getConstant( "FONT" ) );
			else
				defaultAttribute( "FONT",	"arial" );
		}
		if ( !nonDefaultAttributes.containsKey( "FONTBOLD" ) )
		{
			if ( ( defaultChartRangeMarkerTag != null ) && ( defaultChartRangeMarkerTag.containsAttribute( "FONTBOLD" ) ) )
				defaultAttribute( "FONTBOLD",	defaultChartRangeMarkerTag.getConstant( "FONTBOLD" ) );
			else
				defaultAttribute( "FONTBOLD",	"no" );
		}
		if ( !nonDefaultAttributes.containsKey( "FONTITALIC" ) )
		{
			if ( ( defaultChartRangeMarkerTag != null ) && ( defaultChartRangeMarkerTag.containsAttribute( "FONTITALIC" ) ) )
				defaultAttribute( "FONTITALIC",	defaultChartRangeMarkerTag.getConstant( "FONTITALIC" ) );
			else
				defaultAttribute( "FONTITALIC",	"no" );
		}
		if ( !nonDefaultAttributes.containsKey( "FONTSIZE" ) )
		{
			if ( ( defaultChartRangeMarkerTag != null ) && ( defaultChartRangeMarkerTag.containsAttribute( "FONTSIZE" ) ) )
				defaultAttribute( "FONTSIZE",	defaultChartRangeMarkerTag.getConstant( "FONTSIZE" ) );
			else
				defaultAttribute( "FONTSIZE",	"11" );
		}
	}
	
	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
	    //--[ Get the internal chart data in which this relates to
	    cfCHARTInternalData chartData = (cfCHARTInternalData) _Session.getDataBin(cfCHART.DATA_BIN_KEY);
	    if (chartData == null)
	    	throw newRunTimeException("CFCHARTRANGEMARKER must be used inside a CFCHART tag");
		
	    setDefaultParameters( _Session, chartData );
	    
		if ( !containsAttribute("START") )
			throw newBadFileException( "Missing START", "You need to provide a START" );
	    
		if ( !containsAttribute("END") )
			throw newBadFileException( "Missing END", "You need to provide a END" );

		cfCHARTRANGEMARKERData rangeMarkerData = new cfCHARTRANGEMARKERData(getDynamic(_Session,"START").getDouble(),getDynamic(_Session,"END").getDouble(),getDynamic(_Session,"COLOR").toString());
		
		if ( containsAttribute("LABEL") )
		{
			rangeMarkerData.setLabel(getDynamic(_Session,"LABEL").toString());
			rangeMarkerData.setLabelColor(getDynamic(_Session,"LABELCOLOR").toString());

			String labelPos = getDynamic(_Session,"LABELPOSITION").toString().toLowerCase();
		    if ( !labelPos.equals("top_left") && 
				 !labelPos.equals("top") &&
				 !labelPos.equals("top_right") &&
				 !labelPos.equals("left") &&
			     !labelPos.equals("center") &&
			     !labelPos.equals("right") &&
			     !labelPos.equals("bottom_left") &&
			     !labelPos.equals("bottom") &&
			     !labelPos.equals("bottom_right") )
			{
			    throw newBadFileException("Invalid LABELPOSITION Attribute", "The labelPosition attribute cannot have a value of '"+labelPos+"'" );
			}
			rangeMarkerData.setLabelPosition(labelPos);
			
			rangeMarkerData.setFont(getDynamic(_Session,"FONT").toString());
			rangeMarkerData.setFontBold(getDynamic(_Session,"FONTBOLD").getBoolean());
			rangeMarkerData.setFontItalic(getDynamic(_Session,"FONTITALIC").getBoolean());
			rangeMarkerData.setFontSize(getDynamic(_Session,"FONTSIZE").getInt());
		}
			
		chartData.add(rangeMarkerData);
		
		return cfTagReturnType.NORMAL;
	}
}
