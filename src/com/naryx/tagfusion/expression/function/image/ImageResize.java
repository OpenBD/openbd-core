/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  http://openbd.org/
 *  $Id: ImageResize.java 1839 2011-11-29 12:48:45Z alan $
 *  
 *  Notes:
 *  - scaleToSize() method tweeked by Heath Provost
 */

package com.naryx.tagfusion.expression.function.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class ImageResize extends ImageInfo {
	private static final long serialVersionUID = 1L;

	public ImageResize() {
		min = 2; max = 4;
		setNamedParams( new String[]{ "name", "width", "height", "quality" } );
	}
	
	public String[] getParamInfo() {
		return new String[] { 
				"the image object", 
				"width of the new image, can be a percentage value (add %) (blank/missing if to be scaled in proportion to height)",
				"height of the new image, can be a percentage value (add %) (blank/missing if to be scaled in proportion to width)",
				"values: bicubic (default), bilinear, nearest" };
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"image", 
				"Resizes the image to the values accordingly", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		cfImageData im	= getImage( _session, argStruct );
		
		String sw = getNamedStringParam(argStruct, "width", "" ).trim();
		String sh = getNamedStringParam(argStruct, "height", "" ).trim();
		if ( sw.length() == 0 && sh.length() == 0 )	{
			throwException(_session, "missing both width and height parameters. Specify at least one" );
		}

		int	targetWidth = -1, targetHeight = -1;
		
		if ( sw.length() != 0 ){
			if ( sw.endsWith("%") ){
				int percentage = Integer.valueOf( sw.substring(0,sw.length()-1) ).intValue();
				targetWidth	= (int)((double)im.getWidth() * (double)((double)percentage/100.0));
			}else{
				targetWidth = Integer.valueOf( sw ).intValue();
			}
		}
		
		if ( sh.length() != 0 ){
			if ( sh.endsWith("%") ){
				int percentage = Integer.valueOf( sh.substring(0,sh.length()-1) ).intValue();
				targetHeight	= (int)((double)im.getHeight() * (double)((double)percentage/100.0));
			}else{
				targetHeight = Integer.valueOf( sh ).intValue();
			}
		}
		
		
		if ( targetWidth == -1 ){
			//this is now a scale of the target height
			double scale	= (double)im.getWidth() / (double)im.getHeight();
			targetWidth		= (int)((double)targetHeight * scale);
		}
		if ( targetHeight == -1 ){
			//this is now a scale of the target height
			double scale	= (double)im.getWidth() / (double)im.getHeight();
			targetHeight		= (int)((double)targetWidth / scale);
		}
		
		String	quality	= getNamedStringParam(argStruct, "quality", "bicubic" ).toLowerCase();
		Object rh = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
		if ( quality.equals("bilinear") ){
			rh = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
		}else if ( quality.equals("nearest") ){
			rh = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
		}
		
		im.setImage( scaleToSize( im.getImage(), targetWidth, targetHeight, rh ) );
    return cfBooleanData.TRUE;
	}
	
	
	private BufferedImage scaleToSize(BufferedImage img, int targetWidth, int targetHeight, Object interpolation) {
		if (targetWidth == img.getWidth() && targetHeight == img.getHeight()) {
			return img;
		}
		
    boolean higherQuality = (
			// Set flag to use multi-step technique only if the
      // target size is less than 50% of the original size
			// and the interpolation mode is bilinear or bicubic
      (targetWidth < (int)(img.getWidth() * 0.5)) &&
      (
        (interpolation == RenderingHints.VALUE_INTERPOLATION_BILINEAR) ||
        (interpolation == RenderingHints.VALUE_INTERPOLATION_BICUBIC)
      )
    );
    
		int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage ret = (BufferedImage) img;
		int w, h;
		if (higherQuality) {
			// Use multi-step technique: start with original size, then
			// scale down in multiple passes with drawImage()
			// until the target size is reached
			w = img.getWidth();
			h = img.getHeight();
		} else {
			// Use one-step technique: scale directly from original
			// size to target size with a single drawImage() call
			w = targetWidth;
			h = targetHeight;
		}

		do {
			if (higherQuality && w > targetWidth) {
				w /= 2;
				if (w < targetWidth) {
					w = targetWidth;
				}
			}

			if (higherQuality && h > targetHeight) {
				h /= 2;
				if (h < targetHeight) {
					h = targetHeight;
				}
			}

			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION, interpolation );
  		g2.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
	  	g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();
			ret = tmp;
		} while (w != targetWidth || h != targetHeight);

		return ret;
	}

	
}