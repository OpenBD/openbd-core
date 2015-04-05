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

import java.io.IOException;

import javax.imageio.ImageIO;

import org.aw20.io.ByteArrayOutputStreamRaw;

import com.nary.net.Base64;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class ToDataURI extends ImageInfo {
	private static final long serialVersionUID = 1L;

	public ToDataURI() {
		min = 2; max = 2;
		setNamedParams( new String[]{ "data", "mimetype" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"the binary or string object to encode to base64",
			"the mime-type of the object"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"image", 
				"Creates a dataURI for use within HTML IMG/CSS/Javascript tags ", 
				ReturnType.STRING );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		cfData	data	= getNamedParam(argStruct, "data", null );
		if ( data == null )
			throwException( _session, "please specify a data parameter");
		
		String mimeType	= getNamedStringParam(argStruct, "mimetype", null );
		if ( mimeType == null )
			throwException( _session, "please specify a mimeType parameter");
		
		
		String base64 = null;
		if ( data.getDataType() == cfData.CFBINARYDATA ){
			base64 = new String(Base64.base64Encode(((cfBinaryData) data).getByteArray()));
		} else if ( data instanceof cfImageData ){
			base64 = getImageBase64( _session, (cfImageData)data );
		}else{
			base64 = new String(Base64.base64Encode(data.getString()));
		}
		
		base64 = "data:" + mimeType + ";base64," + base64;
		return new cfStringData( base64 );
	}
	
	public String getImageBase64( cfSession _session, cfImageData im ) throws cfmRunTimeException{
		ByteArrayOutputStreamRaw	baos = new ByteArrayOutputStreamRaw(64000);
		try {
			ImageIO.write(im.getImage(), "png", baos );
		} catch (IOException e) {
			throwException(_session, "failed to get image blob: " + e.getMessage() );
		}
		
		return  new String( Base64.base64Encode(baos.getByteArray()));
	}

}