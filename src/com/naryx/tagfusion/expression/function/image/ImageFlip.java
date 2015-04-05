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

import com.jhlabs.image.FlipFilter;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class ImageFlip extends ImageInfo {
	private static final long serialVersionUID = 1L;

	public ImageFlip() {
		min = 1; max = 2;
		setNamedParams( new String[]{ "name", "direction" } );
	}

	public String[] getParamInfo() {
		return new String[] { 
				"the image object", 
				"the direction to flip the image. valid: vertical (default), horizontal, horizontalvertical, 90, -90, 180" };
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"image", 
				"Flips the image around", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		cfImageData im	= getImage( _session, argStruct );
		
		int op = -1;
		String opS	= getNamedStringParam(argStruct, "direction", "vertical").toLowerCase();
		
		if ( opS.equals("vertical") )
			op = FlipFilter.FLIP_V;
		else if ( opS.equals("horizontal") )
			op = FlipFilter.FLIP_H;
		else if ( opS.equals("horizontalvertical") )
			op = FlipFilter.FLIP_HV;
		else if ( opS.equals("90") )
			op = FlipFilter.FLIP_90CW;
		else if ( opS.equals("-90") )
			op = FlipFilter.FLIP_90CCW;
		else if ( opS.equals("180") )
			op = FlipFilter.FLIP_180;
		else
			throwException(_session, "invalid direction: " + opS + "; possible values: vertical, horizontal, horizontalvertical, 90, -90, 180");
	
		
		//Check boundaries
		BufferedImage	bim	= im.getImage();
		
		FlipFilter	filter	= new FlipFilter();
		filter.setOperation( op );
		im.setImage( filter.filter(bim, null) );
		return cfBooleanData.TRUE;
	}
}