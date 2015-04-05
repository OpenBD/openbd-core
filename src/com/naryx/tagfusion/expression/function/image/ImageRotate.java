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
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class ImageRotate extends ImageInfo {
	private static final long serialVersionUID = 1L;

	public ImageRotate() {
		min = 2; max = 5;
		setNamedParams( new String[]{ "name", "angle", "x", "y", "interpolation" } );
	}

	public String[] getParamInfo() {
		return new String[] { 
				"the image object", 
				"the angle to rotate the image",
				"the x co-ordinate to rotate the image at",
				"the y co-ordinate to rotate the image at",
				"the type of interpolation to use; nearest, bilinear, bicubic" };
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"image", 
				"Rotates the image", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		cfImageData im	= getImage( _session, argStruct );
		
		cfData radiusData	= getNamedParam(argStruct,"angle",null);
		if ( radiusData == null )
			throwException(_session,"please specify an angle");
		
		double angle = radiusData.getDouble();

		int x1	= getNamedIntParam(argStruct, "x", Integer.MIN_VALUE );
		int y1	= getNamedIntParam(argStruct, "y", Integer.MIN_VALUE );

		BufferedImage dimg =new BufferedImage( im.getWidth(),im.getHeight(), im.getImage().getType() );
		Graphics2D g = dimg.createGraphics();
		
		if ( im.isAntialise() )
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		
		String interpolation	= getNamedStringParam(argStruct, "interpolation", "nearest").toLowerCase();
		if ( interpolation.equals("bilinear")){
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		}else if ( interpolation.equals("bicubic")){
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		}else{
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		}
		
		if ( x1 == Integer.MIN_VALUE || y1 == Integer.MIN_VALUE ){
			g.rotate( Math.toRadians(angle), im.getWidth() / 2, im.getHeight() / 2);
		}else{
			g.rotate( Math.toRadians(angle), x1, y1 );
		}
		
		g.drawImage( im.getImage(), null, 0, 0);
		g.dispose();
		
		im.setImage( dimg );
		
		return cfBooleanData.TRUE;
	}
}