/* 
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
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

package com.nary.awt;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class Graphics2D extends Object {

	java.awt.Graphics2D g;

	Image img;

	public Graphics2D(int width, int height) {
		BufferedImage iText = new BufferedImage(width, height, 8);
		img = iText;
		g = iText.createGraphics();
	}

	public Image getImage() {
		return img;
	}

	public Color createColor(int r, int g, int b, int a) {
		return new Color(r, g, b, a);
	}

	public Color createColor(int r, int g, int b) {
		return new Color(r, g, b);
	}

	public void setColor(Color thisColor) {
		g.setColor(thisColor);
	}

	public void fillRect(int x, int y, int width, int height) {
		g.fillRect(x, y, width, height);
	}

	public void setFont(Font regularFont) {
		g.setFont(regularFont);
	}

	public Font deriveFont(float size) {
		return g.getFont().deriveFont(size);
	}

	public Font deriveFont(int type, float size) {
		return g.getFont().deriveFont(type, size);
	}

	public void fillArc(int x, int y, int width, int height, int startAngle, int endAngle) {
		g.fillArc(x, y, width, height, startAngle, endAngle);
	}

	public void rotate(double radians) {
		g.rotate(radians);
	}

	public Font getFont() {
		return g.getFont();
	}

	public FontMetrics getFontMetrics() {
		return g.getFontMetrics();
	}

	public void drawString(String displayText, int x, int y) {
		g.drawString(displayText, x, y);
	}
}
