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

import java.util.List;
import java.util.Vector;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfData;

public class cfCHARTSERIESData extends Object {
	private String type;
	private String seriesLabel;
	private String markerStyle;
	private String paintStyle;
	private String dataLabelStyle;
	private String negDataLabelPos;
	private String posDataLabelPos;
	private double dataLabelAngle;
	private String dataLabelColor;
	private String dataLabelFont;
	private boolean dataLabelFontBold;
	private boolean dataLabelFontItalic;
	private int dataLabelFontSize;

	// The list of colors to be used for the items in a series. Only used with
	// Pie, Bar and Horizontal Bar charts. Ignored with all other charts.
	private List<String> colorList;

	// The color used for all items in a series. Ignored for all Pie charts and
	// for
	// Bar and Horizontal Bar charts when a colorList is specified.
	private String seriesColor;

	@SuppressWarnings("unchecked")
	private Vector items = new Vector(); // sometimes Double, sometimes String
	@SuppressWarnings("unchecked")
	private Vector values = new Vector(); // sometimes Double, sometimes String

	// The following are used so XY Charts can be determined to be either
	// numeric ( default )
	// or cfData.CFDATEDATA
	public static final int CATEGORY_SERIES = 0;
	public static final int XY_NUMERIC_SERIES = 1;
	public static final int XY_DATE_SERIES = 2;

	// The series type based on values contained in series.
	private int seriesDataType;

	public cfCHARTSERIESData(String type, String seriesLabel,
			String markerStyle, String paintStyle, cfData dataLabelStyle,
			String negDataLabelPos, String posDataLabelPos,
			double dataLabelAngle, String dataLabelColor, String dataLabelFont,
			boolean dataLabelFontBold, boolean dataLabelFontItalic,
			int dataLabelFontSize) {
		this.type = type;
		this.seriesLabel = seriesLabel;
		this.markerStyle = markerStyle;
		this.paintStyle = paintStyle;

		if (dataLabelStyle != null) {
			this.dataLabelStyle = dataLabelStyle.toString().toLowerCase();
		} else {
			// For pie and ring charts the default dataLabelStyle is 'value'
			// while
			// for all other charts it is 'none'.
			if (type.equals("pie") || type.equals("ring"))
				this.dataLabelStyle = "value";
			else
				this.dataLabelStyle = "none";
		}

		this.negDataLabelPos = negDataLabelPos;
		this.posDataLabelPos = posDataLabelPos;
		this.dataLabelAngle = dataLabelAngle;
		this.dataLabelColor = dataLabelColor;
		this.dataLabelFont = dataLabelFont;
		this.dataLabelFontBold = dataLabelFontBold;
		this.dataLabelFontItalic = dataLabelFontItalic;
		this.dataLabelFontSize = dataLabelFontSize;
	}

	public String getType() {
		return type;
	}

	public String getSeriesLabel() {
		return seriesLabel;
	}

	public String getMarkerStyle() {
		return markerStyle;
	}

	public String getPaintStyle() {
		return paintStyle;
	}

	public String getDataLabelStyle() {
		return dataLabelStyle;
	}

	public String getNegativeDataLabelPosition() {
		return negDataLabelPos;
	}

	public String getPositiveDataLabelPosition() {
		return posDataLabelPos;
	}

	public double getDataLabelAngle() {
		return dataLabelAngle;
	}

	public String getDataLabelColor() {
		return dataLabelColor;
	}

	public String getDataLabelFont() {
		return dataLabelFont;
	}

	public boolean getDataLabelFontBold() {
		return dataLabelFontBold;
	}

	public boolean getDataLabelFontItalic() {
		return dataLabelFontItalic;
	}

	public int getDataLabelFontSize() {
		return dataLabelFontSize;
	}

	public int getNumItems() {
		return items.size();
	}

	public String getItemName(int i) {
		Object o = null;
		try { 
			o = items.elementAt(i);
			return (String) o;
		} catch ( Exception ex ) { 
			return "Unknown";
		}
	}

	public Double getItemValue(int i) {
		return (Double) values.elementAt(i);
	}

	/*
	 * add - this add method is called for category charts.
	 */
	@SuppressWarnings("unchecked")
	public void add(String item, double value) {
		items.addElement(item);
		values.addElement(new Double(value));
	}

	public Double getXValue(int i) {
		return (Double) items.elementAt(i);
	}

	public Double getYValue(int i) {
		return (Double) values.elementAt(i);
	}

	/*
	 * add - this add method is called for scale(xy) charts.
	 */
	@SuppressWarnings("unchecked")
	public void add(double x, double y) {
		items.addElement(new Double(x));
		values.addElement(new Double(y));
	}

	public void setColorList(String colorList) {
		this.colorList = string.split(colorList, ',');
	}

	public List<String> getColorList() {
		return colorList;
	}

	public void setSeriesColor(String color) {
		this.seriesColor = color;
	}

	public String getSeriesColor() {
		return seriesColor;
	}

	@SuppressWarnings("unchecked")
	public void sort() {
		Vector newItems = new Vector(items.size());
		Vector newValues = new Vector(values.size());

		for (int x = 0; x < items.size(); x++) {
			String item = (String) items.elementAt(x);
			Object value = values.elementAt(x);
			boolean inserted = false;
			for (int i = 0; i < newItems.size(); i++) {
				if (item.compareTo((String) newItems.elementAt(i)) < 0) {
					newItems.insertElementAt(item, i);
					newValues.insertElementAt(value, i);
					inserted = true;
					break;
				}
			}

			if (!inserted) {
				newItems.addElement(item);
				newValues.addElement(value);
			}
		}

		items = newItems;
		values = newValues;
	}

	/**
	 * @param seriesDataType
	 *            the seriesDataType to set
	 */
	public void setSeriesDataType(int seriesDataType) {
		this.seriesDataType = seriesDataType;
	}

	/**
	 * @return the seriesDataType
	 */
	public int getSeriesDataType() {
		return seriesDataType;
	}
}
