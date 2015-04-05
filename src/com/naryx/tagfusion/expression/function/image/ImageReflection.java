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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class ImageReflection extends ImageInfo {
	private static final long serialVersionUID = 1L;

	public ImageReflection() {
		min = 1; max = 1;
		setNamedParams( new String[]{ "name" } );
	}

	public String[] getParamInfo() {
		return new String[] { 
				"the image object"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"image", 
				"Creates a reflection of the image", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		cfImageData im	= getImage( _session, argStruct );
		
		float opacity = 0.4f;
		float fadeHeight = 0.001f;
	
		int imageWidth 	= im.getWidth();
		int imageHeight = im.getHeight();
		int width				= imageWidth;
		int height 			= (imageHeight * 2);

		BufferedImage newImage = new BufferedImage( width, height, im.getImage().getType() );
		Graphics2D g2	= newImage.createGraphics();
		
		g2.setPaint( new GradientPaint(0,0, Color.black, 0, height, Color.darkGray ) );
		g2.fillRect(0, 0, width, height );
		g2.translate( (width-imageWidth)/2, height/2-imageHeight );
		g2.drawRenderedImage( im.getImage(), null );
		g2.translate( 0, 2*imageHeight);
		g2.scale( 1, -1 );
		
		BufferedImage reflection	= new BufferedImage( imageWidth, imageHeight, im.getImage().getType() );
		Graphics2D	rg = reflection.createGraphics();
		rg.drawRenderedImage( im.getImage(), null);
		
		rg.setPaint( new GradientPaint(
				0, imageHeight * fadeHeight, new Color(1.0f,0.0f,0.0f,0.0f),
				0, imageHeight, new Color(0.0f,0.0f,0.0f,opacity)
		) );
		rg.fillRect(0,0,imageWidth,imageHeight);
		rg.dispose();
		
		g2.drawRenderedImage(reflection, null);
		g2.dispose();
		im.setImage(newImage);
		return cfBooleanData.TRUE;
	}
}