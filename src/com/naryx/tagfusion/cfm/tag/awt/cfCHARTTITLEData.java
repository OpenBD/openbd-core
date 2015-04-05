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

package com.naryx.tagfusion.cfm.tag.awt;

public class cfCHARTTITLEData extends Object {
	private String title;
	private String backgroundColor;
	private String labelColor;
	private String position;
	private String font;
	private boolean fontBold;
	private boolean fontItalic;
	private int fontSize;
	private boolean showBorder;
	private int padding;
	private int margin;

	public cfCHARTTITLEData(String title, String backgroundColor, String labelColor, String position, String font, boolean fontBold, boolean fontItalic, int fontSize, boolean showBorder, int padding, int margin) {
		this.title = title;
		this.backgroundColor = backgroundColor;
		this.labelColor = labelColor;
		this.position = position;
		this.font = font;
		this.fontBold = fontBold;
		this.fontItalic = fontItalic;
		this.fontSize = fontSize;
		this.showBorder = showBorder;
		this.padding = padding;
		this.margin = margin;
	}

	public String getTitle() {
		return title;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public String getLabelColor() {
		return labelColor;
	}

	public String getPosition() {
		return position;
	}

	public String getFont() {
		return font;
	}

	public boolean getFontBold() {
		return fontBold;
	}

	public boolean getFontItalic() {
		return fontItalic;
	}

	public int getFontSize() {
		return fontSize;
	}

	public boolean getShowBorder() {
		return showBorder;
	}

	public int getPadding() {
		return padding;
	}

	public int getMargin() {
		return margin;
	}
}
