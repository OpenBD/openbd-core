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

package com.naryx.tagfusion.expression.function.image;

import java.awt.Font;
import java.awt.Graphics2D;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class ImageDrawText extends ImageInfo {
	private static final long serialVersionUID = 1L;


	public ImageDrawText() {
		min = 5; max = 6;
		setNamedParams( new String[]{ "name", "str", "x", "y", "fontstyle" } );
	}
	
	
	public String[] getParamInfo() {
		return new String[] { 
				"the image object", 
				"the string to be drawn",
				"the x coordinate.",
				"the y coordinate.", 
				"the structure containing the font properties; {'font' - name of the font face, 'size' - size of the font face, default 10, 'style' - values are bold/italic/bolditalic/plain }"};
	}

	
	public java.util.Map getInfo(){
		return makeInfo(
				"image", 
				"Draws the text given by the specified string", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		cfImageData im	= getImage( _session, argStruct );

		String str	= getNamedStringParam(argStruct, "str", null );
		int x	= getNamedIntParam(argStruct, "x", Integer.MIN_VALUE );
		int y	= getNamedIntParam(argStruct, "y", Integer.MIN_VALUE );

		cfData	s = getNamedParam(argStruct, "fontstyle", null );
		if ( s != null && s.getDataType() != cfData.CFSTRUCTDATA )
			throwException(_session,"fontstyle must be a structure");
		
		//Setup the font
		Font font = getFont( s );
		
		if ( str == null )
			throwException(_session, "str not specified" );
		if ( x == Integer.MIN_VALUE )
			throwException(_session, "x not specified" );
		if ( y == Integer.MIN_VALUE )
			throwException(_session, "y not specified" );
		
		Graphics2D g2 = im.createGraphics();
		
		if ( font != null )
			g2.setFont(font);
		
		g2.drawString(str, x, y);
		
		im.dispose(g2);
		return cfBooleanData.TRUE;
	}


	private Font getFont(cfData s)  throws cfmRunTimeException {
		if ( s == null )
			return null;
		
		cfStructData sd	= (cfStructData)s;
		
		int size	= 10;
		int style	= Font.PLAIN;
		String fontstr	= "";
		
		if ( sd.containsKey("size") ){
			size	= sd.getData("size").getInt();
		}
		
		if ( sd.containsKey("font") ){
			fontstr	= sd.getData("font").getString();
		}
		
		if ( sd.containsKey("style") ){
			String st = sd.getData("style").getString().toLowerCase();
			
			if ( st.equals("bold") ){
				style = Font.BOLD;
			}else if ( st.equals("italic") ){
				style = Font.ITALIC;
			}else if ( st.equals("bolditalic") ){
				style = Font.BOLD | Font.ITALIC;
			}
		}
		
		return new Font( fontstr, style, size );
	}
	
}