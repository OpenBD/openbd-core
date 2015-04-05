/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
 *  $Id: ImageNew.java 2376 2013-06-10 22:42:23Z alan $
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
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.aw20.io.ByteArrayInputStreamRaw;

import com.nary.awt.colour;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class ImageNew extends ImageRead {
	private static final long serialVersionUID = 1L;

	public ImageNew() {
		min = 0;
		max = 5;
		setNamedParams(new String[] { "src", "width", "height", "type", "canvascolor" });
	}

	public String[] getParamInfo() {
		return new String[] { 
				"this is the source of the image; url, blob, binary object, file location, BufferedImage", 
				"if the source is blank, then a basic image is created using this width - default 250", 
				"if the source is blank, then a basic image is created using this width - default 250",
				"if the source is blank the type of image that will be created. valid types: rgb, argb, grayscale, 3byte_bgr, 4byte_bgr, 4byte_abgr, 4byte_abgr_pre, byte_binary, byte_indexed, int_argb_pre, int_bgr ", 
				"the color of the image" };
	}

	public java.util.Map getInfo() {
		return makeInfo("image", "Creates a new image from a variety of different sources, or creates a brand new one", ReturnType.IMAGE);
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {

		// if they have specified src
		cfData srcData = getNamedParam(argStruct, "src", null);
		if (srcData != null) {

			if (srcData instanceof cfImageData)
				return srcData;
			else if (srcData.getDataType() == cfData.CFBINARYDATA) {
				return loadImage(_session, ((cfBinaryData) srcData).getByteArray());
			} else if (srcData.getDataType() == cfData.CFJAVAOBJECTDATA) {
				cfJavaObjectData jdo = (cfJavaObjectData) srcData;
				if (jdo.getInstance() instanceof BufferedImage) {
					cfImageData im = new cfImageData();
					im.setImage((BufferedImage) jdo.getInstance());
					return im;
				} else {
					throwException(_session, "invalid java object passed in.  Only java.awt.image.BufferedImage can be used");
				}
			} else if (srcData.getDataType() == cfData.CFSTRINGDATA) {
				
				if ( !srcData.getString().isEmpty() )
					return loadImage(_session, srcData.getString());

			} else
				throwException(_session, "invalid java object passed in");
		}

		// So at this point we are creating a brand new one
		int w = getNamedIntParam(argStruct, "width", 250);
		int h = getNamedIntParam(argStruct, "height", 250);

		if (w < 0 || h < 0)
			throwException(_session, "invalid width/height specified.  make sure they are greater than 0");

		// Is the color model specifed
		BufferedImage bim;

		cfData colorData = getNamedParam(argStruct, "canvascolor", null);
		if (colorData != null) {
			Color color = colour.getColor(colorData.getString());
			bim = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY, createColorModel(color.getRed(), color.getGreen(), color.getBlue()) );
		} else {
			int imageType = getImageType(_session, getNamedStringParam(argStruct, "type", "rgb"));
			bim = new BufferedImage(w, h, imageType);
		}

		cfImageData im = new cfImageData();
		im.setImage(bim);
		return im;
	}

	/**
	 * This determines the image type
	 * 
	 * @param namedStringParam
	 * @return
	 * @throws cfmRunTimeException
	 */
	private int getImageType(cfSession _session, String imageType) throws cfmRunTimeException {
		imageType = imageType.toUpperCase().trim();

		if (imageType.equals("3BYTE_BGR")) {
			return BufferedImage.TYPE_3BYTE_BGR;
		} else if (imageType.equals("4BYTE_ABGR")) {
			return BufferedImage.TYPE_4BYTE_ABGR;
		} else if (imageType.equals("4BYTE_ABGR_PRE")) {
			return BufferedImage.TYPE_4BYTE_ABGR_PRE;
		} else if (imageType.equals("BYTE_BINARY")) {
			return BufferedImage.TYPE_BYTE_BINARY;
		} else if (imageType.equals("BYTE_GRAY") || imageType.equals("GRAYSCALE")) {
			return BufferedImage.TYPE_BYTE_GRAY;
		} else if (imageType.equals("BYTE_INDEXED")) {
			return BufferedImage.TYPE_BYTE_INDEXED;
		} else if (imageType.equals("INT_ARGB") || imageType.equals("ARGB")) {
			return BufferedImage.TYPE_INT_ARGB;
		} else if (imageType.equals("INT_ARGB_PRE")) {
			return BufferedImage.TYPE_INT_ARGB_PRE;
		} else if (imageType.equals("INT_BGR")) {
			return BufferedImage.TYPE_INT_BGR;
		} else if (imageType.equals("INT_RGB") || imageType.equals("RGB")) {
			return BufferedImage.TYPE_INT_RGB;
		} else if (imageType.equals("USHORT_555_RGB")) {
			return BufferedImage.TYPE_USHORT_555_RGB;
		} else if (imageType.equals("USHORT_565_RGB")) {
			return BufferedImage.TYPE_USHORT_565_RGB;
		} else if (imageType.equals("USHORT_GRAY")) {
			return BufferedImage.TYPE_USHORT_GRAY;
		} else {
			throwException(_session, "invalid type specified: " + imageType);
		}

		return 0;
	}

	
	/**
	 * Creates the IndexColorModel
	 * 
	 * @param _r
	 * @param _g
	 * @param _b
	 * @return
	 */
	private IndexColorModel createColorModel(int _r, int _g, int _b) {
		byte[] r = new byte[16];
		byte[] g = new byte[16];
		byte[] b = new byte[16];

		for (int i = 0; i < r.length; i++) {
			r[i] = (byte) _r;
			g[i] = (byte) _g;
			b[i] = (byte) _b;
		}
		return new IndexColorModel(4, 16, r, g, b);
	}

	/**
	 * Create an image object from the binary data
	 * 
	 * @param session
	 * @param byteArray
	 * @return
	 * @throws cfmRunTimeException
	 */
	protected cfData loadImage(cfSession session, byte[] byteArray) throws cfmRunTimeException {
		cfImageData im = new cfImageData();

		try {
			im.setImage(ImageIO.read(new ByteArrayInputStreamRaw(byteArray, byteArray.length)));
		} catch (IOException e) {
			throwException(session, "invalid binary stream: " + e.getMessage());
		}

		return im;
	}
}