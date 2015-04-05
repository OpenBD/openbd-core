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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import com.nary.awt.colour;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class ImageAddBorder extends ImageInfo {
	private static final long serialVersionUID = 1L;

	public ImageAddBorder() {
		min = 2; max = 4;
		setNamedParams( new String[]{ "name", "thickness", "color", "type" } );
	}
	
	public String[] getParamInfo() {
		return new String[] { 
				"the image object", 
				"thickness of the border.  Must be greater than zero (0)",
				"color of the border.  May be described in hexidecimal, or named color",
				"type of border; 'constant' (default) or 'zero'" };
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"image", 
				"Adds a border to the image.  The image will be resized to accomondate the border", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		cfImageData im	= getImage( _session, argStruct );
		
		int thickness	= getNamedIntParam(argStruct, "thickness", 0 );
		if ( thickness == 0 )
			throwException(_session, "please specify a thickness greater than zero (0)" );
		
		String bordertype = getNamedStringParam(argStruct, "type", "constant" ).trim();
		if ( !bordertype.equals("constant") && !bordertype.equals("zero") )	{
			throwException(_session, "type must be 'constant' or 'zero'" );
		}
		
		Color color;
		if ( bordertype.equals("zero") ){
			color = Color.black;
		}else{
			color	= colour.getColor( getNamedStringParam(argStruct, "color", "black") );
		}
		
		int targetWidth		= im.getWidth() + ( thickness*2 );
		int targetHeight	= im.getHeight() + ( thickness*2 );
		
		BufferedImage	img = im.getImage();
		int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage tmp = new BufferedImage(targetWidth, targetHeight, type);
		Graphics2D g2 = tmp.createGraphics();
		
		g2.setBackground(color);
		g2.clearRect(0, 0, targetWidth, targetHeight );
		g2.translate(thickness, thickness);
		g2.drawRenderedImage(img, null);
		g2.dispose();

		im.setImage( tmp );
		
		return cfBooleanData.TRUE;
	}
}