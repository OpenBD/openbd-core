/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  $Id: ImagePaste.java 1931 2012-02-03 00:49:31Z alan $
 */

package com.naryx.tagfusion.expression.function.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class ImagePaste extends ImageInfo {
	private static final long serialVersionUID = 1L;

	public ImagePaste() {
		min = 4; max = 4;
		setNamedParams( new String[]{ "name", "name2", "x", "y" } );
	}

	public String[] getParamInfo() {
		return new String[] { 
				"the image object", 
				"the image to paste in", 
				"x co-ordinate of where the pasted image will go",
				"y co-ordinate of where the pasted image will go" };
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"image", 
				"Pastes an image into the new one", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		cfImageData im	= getImage( _session, argStruct );
		cfImageData im2	= getImage2( _session, argStruct );
		
		int x1	= getNamedIntParam(argStruct, "x", Integer.MIN_VALUE );
		int y1	= getNamedIntParam(argStruct, "y", Integer.MIN_VALUE );
				
		if ( x1 == Integer.MIN_VALUE )
			throwException(_session, "x1 not specified" );
		if ( y1 == Integer.MIN_VALUE )
			throwException(_session, "y1 not specified" );		

		
		//Check boundaries
		BufferedImage	bim	= im2.getImage();

		Graphics2D g2 = im.createGraphics();
    g2.drawImage( bim, x1, y1, im2.getWidth(), im2.getHeight(), null );
    im.dispose(g2);
		return im;
	}
	
	private cfImageData getImage2(cfSession _session,cfArgStructData argStruct) throws cfmRunTimeException{
  	cfData imd	= getNamedParam( argStruct, "name2" );
  	
  	if ( imd == null || !(imd instanceof cfImageData) )
  		throwException(_session, "attribute 'name2' passed in was not of type image" );
  	
  	return (cfImageData)imd;
	}

}