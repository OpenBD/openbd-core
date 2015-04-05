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

public class ImageDrawOval extends ImageInfo {
	private static final long serialVersionUID = 1L;


	public ImageDrawOval() {
		min = 5; max = 6;
		setNamedParams( new String[]{ "name", "x", "y", "width", "height", "filled" } );
	}
	
	
	public String[] getParamInfo() {
		return new String[] { 
				"the image object", 
				"the left of the bounding oval",
				"the right of the bounding oval",
				"the width of the oval", 
				"the height of the oval",
				"a flag to determine if the oval is filled with the current color; default to false"};
	}

	
	public java.util.Map getInfo(){
		return makeInfo(
				"image", 
				"Draws the outline of an oval. The result is a circle or ellipse that fits within the rectangle specified by the x, y, width, and height arguments", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		cfImageData im	= getImage( _session, argStruct );
		
		int x	= getNamedIntParam(argStruct, "x", Integer.MIN_VALUE );
		int y	= getNamedIntParam(argStruct, "y", Integer.MIN_VALUE );
		int w	= getNamedIntParam(argStruct, "width", Integer.MIN_VALUE );
		int h	= getNamedIntParam(argStruct, "height", Integer.MIN_VALUE );
		
		boolean bFilled	= getNamedBooleanParam(argStruct, "filled", false );
		
		//Check boundaries
		if ( x == Integer.MIN_VALUE )
			throwException(_session, "x not specifed" );
		
		if ( y == Integer.MIN_VALUE )
			throwException(_session, "y not specifed" );
		
		if ( w == Integer.MIN_VALUE )
			throwException(_session, "width not specifed" );
		
		if ( h == Integer.MIN_VALUE )
			throwException(_session, "height not specifed" );
		
		Graphics2D g2 = im.createGraphics();
		if ( bFilled ){
			g2.fillOval(x, y, w, h );	
		}else{
			g2.drawOval(x, y, w, h );	
		}
		
		im.dispose(g2);
		return cfBooleanData.TRUE;
	}
}