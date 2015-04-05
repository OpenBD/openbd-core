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

import java.awt.Graphics2D;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class ImageDrawBeveledRect extends ImageInfo {
	private static final long serialVersionUID = 1L;


	public ImageDrawBeveledRect() {
		min = 6; max = 7;
		setNamedParams( new String[]{ "name", "x", "y", "width", "height", "raised", "filled" } );
	}
	
	
	public String[] getParamInfo() {
		return new String[] { 
				"the image object", 
				"the x coordinate of the upper-left corner of the rectangle to be drawn",
				"the y coordinate of the upper-left corner of the rectangle to be drawn",
				"the width of the rectangle to be drawn", 
				"the height of the rectangle to be drawn",
				"a flag that determines whether the rectangle appears to be raised above the surface or etched into the surface",
				"a flag to determine if the rectangle is filled with the current color; default to false"};
	}

	
	public java.util.Map getInfo(){
		return makeInfo(
				"image", 
				"Paints a 3-D highlighted rectangle filled with the current color. The edges of the rectangle will be highlighted so that it appears as if the edges were beveled and lit from the upper left corner. The colors used for the highlighting effect will be determined from the current color.", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		cfImageData im	= getImage( _session, argStruct );
		
		int x	= getNamedIntParam(argStruct, "x", Integer.MIN_VALUE );
		int y	= getNamedIntParam(argStruct, "y", Integer.MIN_VALUE );
		int w	= getNamedIntParam(argStruct, "width", Integer.MIN_VALUE );
		int h	= getNamedIntParam(argStruct, "height", Integer.MIN_VALUE );

		boolean bRaised	= getNamedBooleanParam(argStruct, "raised", false );
		boolean bFilled	= getNamedBooleanParam(argStruct, "filled", false );

		
		if ( x == Integer.MIN_VALUE )
			throwException(_session, "x not specifed" );
		
		if ( y == Integer.MIN_VALUE )
			throwException(_session, "y not specifed" );
		
		if ( w == Integer.MIN_VALUE )
			throwException(_session, "width not specifed" );
		
		if ( h == Integer.MIN_VALUE )
			throwException(_session, "height not specifed" );

		// Perform the operation
		Graphics2D g2 = im.createGraphics();
		
		if ( bFilled ){
			g2.fill3DRect( x, y, w, h, bRaised );	
		}else{
			g2.draw3DRect(x, y, w, h, bRaised );	
		}
		
		im.dispose(g2);
		return cfBooleanData.TRUE;
	}
}