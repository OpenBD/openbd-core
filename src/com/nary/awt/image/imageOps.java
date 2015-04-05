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

package com.nary.awt.image;

/**
 * A library of static functions for loading, manipulating and saving
 * images.
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

public class imageOps extends Object {
	public static final byte GIF = 0, JPG = 1, PNG = 2;
	

	/**
	 * loads the image found at the given source (e.g. "picture.gif" ) with the
	 * given image type.
	 */

	public static BufferedImage loadImage(String _source, byte _imageType) {
		switch (_imageType) {
		case GIF:
			return loadGif(_source);
		case JPG:
			return loadJpeg(_source);
		case PNG:
			return loadPng(_source);
		default:
			return null;
			// throw new Exception( "Invalid image type" );
		}

	}// loadImage()

	/**
	 * saves the image to the given destination (e.g. "picture.gif" ) in the given
	 * image format.
	 */

	public static void saveImage(BufferedImage _img, File _dest, byte _imageType) throws IOException {
		switch (_imageType) {
		case GIF:
			saveGif(_img, _dest);
			break;
		case JPG:
			saveJpeg(_img, _dest);
			break;
		case PNG:
			savePng(_img, _dest);
			break;
		default:
			// throw new Exception( "Invalid image type" );
		}

	}// saveImage()

	/**
	 * returns a scaled duplicate of the given image. The dimensions of the
	 * returned image will be as much as possible close to the given parameters -
	 * _width, _height
	 */

	public static BufferedImage resize(BufferedImage _origImg, int _width, int _height) {
		return scaleToSize(_origImg, _width, _height, true);
	}// resize()

	/**
	 * returns a scaled duplicate of the given image. The dimensions of the
	 * returned image will be ( _percentWidth * _origImg width ) by (
	 * _percentHeight * _origImg height )
	 */

	public static BufferedImage resize(BufferedImage _origImg, double _percentWidth, double _percentHeight) {
		return resize(_origImg, (int) (_origImg.getWidth() * _percentWidth), (int) (_origImg.getHeight() * _percentHeight));
	}// resize()

	public static BufferedImage contrastAndBrightness(BufferedImage _origImg, float contrast, float brightness) {
		java.awt.image.RescaleOp rescale = new java.awt.image.RescaleOp(contrast, brightness, null);
		return rescale.filter(_origImg, null);
	}

	public static BufferedImage applyGrayScaleFilter(BufferedImage _origImg) {
		java.awt.color.ColorSpace cs = java.awt.color.ColorSpace.getInstance(java.awt.color.ColorSpace.CS_GRAY);
		java.awt.image.ColorConvertOp convert = new java.awt.image.ColorConvertOp(cs, null);
		return convert.filter(_origImg, null);
	}

	public static BufferedImage crop(BufferedImage _origImg, int x, int y, int width, int height) {
		return _origImg.getSubimage(x, y, width, height);
	}

	public static BufferedImage rotate(BufferedImage _origImg, int angle, Color bgColor) {
		// Convert the degrees to radians
		double radians = Math.toRadians(angle);

		// Determine the sin and cos of the angle
		double sin = Math.abs(Math.sin(radians));
		double cos = Math.abs(Math.cos(radians));

		// Store the original width and height of the image
		int w = _origImg.getWidth();
		int h = _origImg.getHeight();

		// Determine the width and height of the rotated image
		int neww = (int) Math.floor(w * cos + h * sin);
		int newh = (int) Math.floor(h * cos + w * sin);

		// Create a BufferedImage to store the rotated image
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage result = gc.createCompatibleImage(neww, newh, Transparency.OPAQUE);

		// Render the rotated image
		Graphics2D g = result.createGraphics();
		g.setBackground(bgColor);
		g.clearRect(0, 0, neww, newh);
		g.translate((neww - w) / 2, (newh - h) / 2);
		g.rotate(radians, w / 2, h / 2);
		g.drawRenderedImage(_origImg, null);
		g.dispose();

		// Return the rotated image
		return result;
	}

	public static BufferedImage addBorder(BufferedImage _origImg, int thickness, Color color) {

		int neww = _origImg.getWidth() + thickness * 2;
		int newh = _origImg.getHeight() + thickness * 2;

		// Create a BufferedImage to store the image with a border
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage result = gc.createCompatibleImage(neww, newh, Transparency.OPAQUE);

		// Render the image with a border
		Graphics2D g = result.createGraphics();
		g.setBackground(color);
		g.clearRect(0, 0, neww, newh);
		g.translate(thickness, thickness);
		g.drawRenderedImage(_origImg, null);
		g.dispose();

		// Return the image with a border
		return result;
	}

	/**
	 * Superimposes the given string on the given image at the given x,y
	 * co-ordinate positions
	 */

	public static void drawString(BufferedImage _img, Font _font, Color _color, String _str, int _posX, int _posY) {
		Graphics g = _img.getGraphics();
		g.setFont(_font);
		g.setColor(_color);
		g.drawString(_str, _posX, _posY);
	}// drawString()

	
	
	// ==> PRIVATE METHODS >>
	private static BufferedImage loadJpeg(String _source) {
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(_source);
			return ImageIO.read(fin);
		} catch (Exception e) {
			return null;
		} finally {
			try {
				if (fin != null)
					fin.close();
			} catch (Exception ignored) {
			}
		}
	}// loadJpeg()

	private static BufferedImage loadGif(String _source) {
		return loadJpeg(_source);
	}// loadGif()

	private static BufferedImage loadPng(String _source) {
		return loadJpeg(_source);
	}// loadPng()

	private static void saveGif(BufferedImage _img, File _dest) throws IOException {
		ImageIO.write(_img, "gif", _dest);
	}// saveGif()

	
	private static void savePng(BufferedImage _img, File _dest) throws IOException {
		ImageIO.write(_img, "png", _dest);
	}// savePng()

	
	private static void saveJpeg(BufferedImage _img, File _dest) throws IOException {
		ImageWriteParam iwparam = new ImageWriteParam(); 
		iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ; 
		iwparam.setCompressionQuality( 0.75F ); 		
		iwparam.setProgressiveMode( ImageWriteParam.MODE_DEFAULT );
		
		ImageWriter writer = null; 
		Iterator iter = ImageIO.getImageWritersByFormatName("jpg"); 
		if (iter.hasNext()) { 
			writer = (ImageWriter)iter.next(); 
		} 
		
		ImageOutputStream ios = ImageIO.createImageOutputStream(_dest); 
		writer.setOutput(ios); 
		writer.write(null, new IIOImage( _img, null, null), iwparam); 
		
		ios.flush(); 
		writer.dispose(); 
		ios.close(); 
	}// saveJpeg()

	
	private static class ImageWriteParam extends JPEGImageWriteParam { 
		public ImageWriteParam() { super(Locale.getDefault()); }  
		
		public void setCompressionQuality(float quality) { 
			if (quality < 0.0F || quality > 1.0F) { 
				throw new IllegalArgumentException("Quality out-of-bounds!"); 
			} 
			this.compressionQuality = 256 - (quality * 256); 
		} 
	}

	
	/*
	 * scaleToSize
	 * 
	 * This method greatly improves the quality of a resized image when
	 * higherQuality is set to true.
	 */
	private static BufferedImage scaleToSize(BufferedImage img, int targetWidth, int targetHeight, boolean higherQuality) {
		if (targetWidth == img.getWidth() && targetHeight == img.getHeight()) {
			return img;
		}
		
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
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();

			ret = tmp;
		} while (w != targetWidth || h != targetHeight);

		return ret;
	}

	public static void main( String [] args ) throws IOException{
		BufferedImage	h = loadJpeg( "e:\\tmp\\test.jpg" );
		
		saveImage( h, new File("e:\\tmp\\test2.jpg"), JPG );
		
	}
	
	/*
	 * public static void main( String [] args ){ BufferedImage bi =
	 * imageOps.loadImage( "golden.jpg", imageOps.JPG ); //imageOps.setFont( bi,
	 * new Font( "courier", Font.ITALIC, 12 ) ); imageOps.drawString( bi, new
	 * Font( "courier", Font.ITALIC, 12 ), Color.pink, "Hello", 30, 30 );
	 * imageOps.saveImage( bi, "testOne.jpg", imageOps.JPG ); bi.flush(); bi =
	 * null; }// main
	 */
}// imageLibrary
