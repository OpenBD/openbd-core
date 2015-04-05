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

import com.jhlabs.image.BoxBlurFilter;
import com.jhlabs.image.SharpenFilter;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class ImageSharpen extends ImageInfo {
	private static final long serialVersionUID = 1L;

	public ImageSharpen() {
		min = 2; max = 2;
		setNamedParams( new String[]{ "name", "gain" } );
	}

	public String[] getParamInfo() {
		return new String[] { 
				"the image object", 
				"if gain less than 0, then it is blurred,  if gain == 0, then no effect, if gain greater than 0 then sharpened" };
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"image", 
				"Sharpens or Blurs the image", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		cfImageData im	= getImage( _session, argStruct );
		
		int	gain	= getNamedIntParam(argStruct, "gain", Integer.MIN_VALUE );
		if ( gain == Integer.MIN_VALUE )
			throwException(_session, "please specify a value for gain");
		
		if ( gain == 0 )
			return cfBooleanData.TRUE;
		
		//Check boundaries
		BufferedImage	bim	= im.getImage();
		
		if ( gain < 0 ){
			BoxBlurFilter	filter	= new BoxBlurFilter();
			filter.setRadius( (float)3 );
			im.setImage( filter.filter(bim, null) );
		}else{
			SharpenFilter	filter	= new SharpenFilter();
			im.setImage( filter.filter(bim, null) );
		}

		return cfBooleanData.TRUE;
	}
}