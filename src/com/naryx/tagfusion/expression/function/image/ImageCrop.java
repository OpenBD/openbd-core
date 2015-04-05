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

import java.awt.image.BufferedImage;

import com.jhlabs.image.CropFilter;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class ImageCrop extends ImageInfo {
	private static final long serialVersionUID = 1L;

	public ImageCrop() {
		min = max = 5;
		setNamedParams( new String[]{ "name", "x", "y", "width", "height" } );
	}
	
	
	public String[] getParamInfo() {
		return new String[] { 
				"the image object", 
				"the left of the bounding rectangle",
				"the right of the bounding rectangle",
				"the width of the rectangle", 
				"the height of the rectangle"};
	}

	
	public java.util.Map getInfo(){
		return makeInfo(
				"image", 
				"Crops the image down to the area specified.  The bounding rectangle must be inside the current image boundaries", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		cfImageData im	= getImage( _session, argStruct );
		
		int x	= getNamedIntParam(argStruct, "x", 0 );
		int y	= getNamedIntParam(argStruct, "y", 0 );
		int w	= getNamedIntParam(argStruct, "width", 0 );
		int h	= getNamedIntParam(argStruct, "height", 0 );
		
		//Check boundaries
		BufferedImage	bim	= im.getImage();
		
		if ( x < 0 || x > bim.getWidth() )
			throwException(_session, "x (" + x + ") is outside the image" );
		
		if ( y < 0 || y > bim.getHeight() )
			throwException(_session, "y (" + y + ") is outside the image" );

		if ( (x+w) < 0 || (x+w) > bim.getWidth() )
			throwException(_session, "w (" + w + ") is outside the image width (" + bim.getWidth() + ")" );

		if ( (y+h) < 0 || (y+h) > bim.getHeight() )
			throwException(_session, "h (" + h + ") is outside the image height (" + bim.getHeight() + ")" );

		
		CropFilter	filter	= new CropFilter(x,y,w,h);
		im.setImage( filter.filter(bim, null) );
		return cfBooleanData.TRUE;
	}
}