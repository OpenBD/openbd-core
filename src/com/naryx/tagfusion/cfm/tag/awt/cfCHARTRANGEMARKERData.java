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

public class cfCHARTRANGEMARKERData extends Object {
	private double start;
	private double end;
	private String color;
	private String label;
	private String labelColor;
	private String labelPosition;
	private String font;
	private boolean fontBold;
	private boolean fontItalic;
	private int fontSize;

	public cfCHARTRANGEMARKERData(double start, double end, String color) {
		this.start = start;
		this.end = end;
		this.color = color;
	}

	public double getStart() {
		return start;
	}

	public double getEnd() {
		return end;
	}

	public String getColor() {
		return color;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabelColor(String labelColor) {
		this.labelColor = labelColor;
	}

	public String getLabelColor() {
		return labelColor;
	}

	public void setLabelPosition(String labelPos) {
		this.labelPosition = labelPos;
	}

	public String getLabelPosition() {
		return labelPosition;
	}

	public void setFont(String font) {
		this.font = font;
	}

	public String getFont() {
		return font;
	}

	public void setFontBold(boolean bold) {
		this.fontBold = bold;
	}

	public boolean getFontBold() {
		return fontBold;
	}

	public void setFontItalic(boolean italic) {
		this.fontItalic = italic;
	}

	public boolean getFontItalic() {
		return fontItalic;
	}

	public void setFontSize(int size) {
		this.fontSize = size;
	}

	public int getFontSize() {
		return fontSize;
	}
}
