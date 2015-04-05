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
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfStructReadOnlyData;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;

public class cfImageData extends cfStructReadOnlyData {
	private static final long serialVersionUID = 1L;

	private BufferedImage	image;
	private Color					activeColor = null, backgroundColor = null, xorMode = null;
	private boolean 			bAntiAlias = true;
	private cfStructData 	cfMetaData = null;
	
	public void setImage(BufferedImage image){
		this.image = image;
		updateMetaData();
	}
	
	public BufferedImage getImage(){
		return this.image;
	}
	
	public int getWidth(){
		return this.image.getWidth();
	}
	
	public int getHeight(){
		return this.image.getHeight();
	}
	
	public Color getActiveColor(){
		return (activeColor == null) ? Color.black : activeColor;
	}
	
	public Color getBackgroundColor(){
		return (backgroundColor == null) ? Color.black : backgroundColor;
	}
	
	public boolean isAntialise(){
		return bAntiAlias;
	}
	
	public void setActiveColor( Color c ){
		activeColor = c;
	}

	public void setBackgroundColor(Color c) {
		backgroundColor = c;
	}

	public void setSrc(String src){
		setPrivateData( "source", new cfStringData(src) );
	}
	
	public String getSrc() throws dataNotSupportedException{
		cfData	s = getData("source");
		if ( s == null )
			return null;
		else
			return s.getString();
	}
	
	public void setSize(long l){
		setPrivateData( "size", new cfNumberData(l) );
	}

	public void setAntialiseOn(boolean b) {
		bAntiAlias = b;
	}

	public void updateMetaData(){
		setPrivateData( "height", new cfNumberData( this.image.getHeight() ) );
		setPrivateData( "width", 	new cfNumberData( this.image.getWidth() ) );
		setPrivateData( "type", 	new cfNumberData( this.image.getType() ) );

		//Set the colormodel
		ColorModel cm = this.image.getColorModel();
		if ( cm != null ){
			cfStructReadOnlyData	colormodel	= new cfStructReadOnlyData();
			colormodel.setPrivateData("pixel_size", new cfNumberData(cm.getPixelSize()) );
			colormodel.setPrivateData("transparency", new cfNumberData(cm.getTransparency()) );
			setPrivateData( "colormodel",	colormodel );
		}
	}
	
	
	protected synchronized void dump(java.io.PrintWriter out, boolean longVersion, String _lbl, int _top) {
		dump("image",out,longVersion,_lbl,_top);
	}

	
	/**
	 * Gets the Graphics2D object, applying all the standard propeties to it
	 * @return
	 */
	public Graphics2D createGraphics() {
		Graphics2D g2 = image.createGraphics();
		
		if ( bAntiAlias )
			g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		
		g2.setBackground( getBackgroundColor() );
		g2.setColor( getActiveColor() );
		
		if ( xorMode != null )
			g2.setXORMode(xorMode);
		else
			g2.setPaintMode();
		
		return g2;
	}

	public void dispose(Graphics2D g2) {
		g2.dispose();
	}

	public void setXORColor(Color color) {
		xorMode	= color;
	}

	public void setMetaData(cfStructData cfMetaData) {
		this.cfMetaData	= cfMetaData;
	}
	
	public cfStructData getMetaData(){
		return this.cfMetaData;
	}
}