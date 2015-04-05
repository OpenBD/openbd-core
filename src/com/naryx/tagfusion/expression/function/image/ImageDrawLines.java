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
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class ImageDrawLines extends ImageInfo {
	private static final long serialVersionUID = 1L;


	public ImageDrawLines() {
		min = 3; max = 5;
		setNamedParams( new String[]{ "name", "xarray", "yarray", "isploygon", "filled"  } );
	}
	
	
	public String[] getParamInfo() {
		return new String[] { 
				"the image object", 
				"an array of x points",
				"an array of y points",
				"a flag if the lines are connected; default to false", 
				"a flag to determine if the rectangle is filled with the current color; default to false"
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
		
		cfData	xa	= getNamedParam(argStruct, "xarray", null );
		if ( xa == null || xa.getDataType() != cfData.CFARRAYDATA )
			throwException(_session, "please specify the xarray and make sure it is an array");
		
		cfData	ya	= getNamedParam(argStruct, "yarray", null );
		if ( ya == null || ya.getDataType() != cfData.CFARRAYDATA )
			throwException(_session, "please specify the yarray and make sure it is an array");
		
		cfArrayData xarray = (cfArrayData)xa;
		cfArrayData yarray = (cfArrayData)ya;
		
		if ( xarray.size() != yarray.size() )
			throwException(_session, "the xarray is not the same size as the yarray");
		
		int x[]	= new int[xarray.size()];
		int y[]	= new int[yarray.size()];
		
		for ( int q=0; q < xarray.size(); q++ ){
			x[q]	= xarray.getData(q+1).getInt();
		}
		
		for ( int q=0; q < yarray.size(); q++ ){
			y[q]	= yarray.getData(q+1).getInt();
		}
		
		boolean bPolygon	= getNamedBooleanParam(argStruct, "isploygon", false );
		
		Graphics2D g2 = im.createGraphics();
		
		if ( bPolygon ){
			boolean bFilled	= getNamedBooleanParam(argStruct, "filled", false );
			
			if ( bFilled ){
				g2.fillPolygon(x, y, x.length);
			}else{
				g2.drawPolygon(x, y, x.length);
			}
			
		}else{
			g2.drawPolyline(x, y, x.length );
		}
		
		im.dispose(g2);
		
		return cfBooleanData.TRUE;
	}
}