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

public class ImageDrawLine extends ImageInfo {
	private static final long serialVersionUID = 1L;


	public ImageDrawLine() {
		min = 5; max = 5;
		setNamedParams( new String[]{ "name", "x1", "y1", "x2", "y2"  } );
	}
	
	
	public String[] getParamInfo() {
		return new String[] { 
				"the image object", 
				"the first point's x coordinate",
				"the first point's y coordinate",
				"the second point's x coordinate", 
				"the second point's y coordinate"
		};
	}

	
	public java.util.Map getInfo(){
		return makeInfo(
				"image", 
				"Draws a line, using the current color, between the points (x1, y1) and (x2, y2) in this graphics context's coordinate system", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		cfImageData im	= getImage( _session, argStruct );
		
		int x1	= getNamedIntParam(argStruct, "x1", Integer.MIN_VALUE );
		int y1	= getNamedIntParam(argStruct, "y1", Integer.MIN_VALUE );
		int x2	= getNamedIntParam(argStruct, "x2", Integer.MIN_VALUE );
		int y2	= getNamedIntParam(argStruct, "y2", Integer.MIN_VALUE );
				
		if ( x1 == Integer.MIN_VALUE )
			throwException(_session, "x1 not specified" );
		if ( x2 == Integer.MIN_VALUE )
			throwException(_session, "x2 not specified" );
		if ( y1 == Integer.MIN_VALUE )
			throwException(_session, "y1 not specified" );
		if ( y2 == Integer.MIN_VALUE )
			throwException(_session, "y2 not specified" );
		
		
		Graphics2D g2 = im.createGraphics();
		g2.drawLine(x1, y1, x2, y2 );
		im.dispose(g2);
		
		return cfBooleanData.TRUE;
	}
}