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

package com.newatlanta.bluedragon;

import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import org.jfree.chart.LegendItem;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;

import com.naryx.tagfusion.cfm.tag.awt.cfCHART;

public class CustomBarRenderer extends BarRenderer implements CustomColorRenderer {
	private static final long serialVersionUID = 1L;

	/** The colors. */
	private Vector colors = new Vector();

	/**
	 * Creates a new renderer.
	 * 
	 * @param colors
	 *          the colors.
	 */
	public CustomBarRenderer(Paint[] colors) {
		this.colors.addElement(colors);
	}

	public void addColors(Paint[] colors) {
		this.colors.addElement(colors);
	}

	/**
	 * Returns the paint for an item. Overrides the default behaviour inherited
	 * from AbstractSeriesRenderer.
	 * 
	 * @param row
	 *          the series.
	 * @param column
	 *          the category.
	 * 
	 * @return The item color.
	 */
	public Paint getItemPaint(int row, int column) {
		if (row >= this.colors.size())
			return super.getItemPaint(row, column);

		Paint[] seriesColors = (Paint[]) this.colors.elementAt(row);
		if (seriesColors.length == 0)
			return super.getItemPaint(row, column);

		return seriesColors[column % seriesColors.length];
	}

	/**
	 * Returns a legend item for a series.
	 * 
	 * @param datasetIndex
	 *          the dataset index (zero-based).
	 * @param series
	 *          the series index (zero-based).
	 * 
	 * @return The legend item.
	 */
	public LegendItem getLegendItem(int datasetIndex, int series) {

		CategoryPlot cp = getPlot();
		if (cp == null) {
			return null;
		}

		CategoryDataset dataset;
		dataset = cp.getDataset(datasetIndex);
		String label = getLegendItemLabelGenerator().generateLabel(dataset, series);
		String description = label;
		String toolTipText = null;
		if (getLegendItemToolTipGenerator() != null) {
			toolTipText = getLegendItemToolTipGenerator().generateLabel(dataset, series);
		}
		String urlText = null;
		if (getLegendItemURLGenerator() != null) {
			urlText = getLegendItemURLGenerator().generateLabel(dataset, series);
		}
		Shape shape = new Rectangle2D.Double(-4.0, -4.0, 8.0, 8.0);
		Paint paint = getSeriesPaint(series);
		Paint outlinePaint = getSeriesOutlinePaint(series);
		Stroke outlineStroke = getSeriesOutlineStroke(series);

		// This is the fix for bug #2695
		if (paint instanceof java.awt.GradientPaint) {
			// When the paintstyle is "shade" use the lighter
			// color while with "light" use the darker color
			// NOTE: if we take the lighter color (Color2) and make it darker
			// and it equals the darker color (Color1) then the paintstyle
			// is "shade".
			GradientPaint gp = ((GradientPaint) paint);
			if (cfCHART.getDarkerColor(gp.getColor2()).equals(gp.getColor1()))
				paint = gp.getColor2(); // the lighter color
			else
				paint = gp.getColor1(); // the darker color
		}

		return new LegendItem(label, description, toolTipText, urlText, true, shape, true, paint, isDrawBarOutline(), outlinePaint, outlineStroke, false, new Line2D.Float(), new BasicStroke(1.0f), Color.black);
	}
}
