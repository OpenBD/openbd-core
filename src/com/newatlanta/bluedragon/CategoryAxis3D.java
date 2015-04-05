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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.plot.Plot;
import org.jfree.ui.RectangleEdge;

/**
 * An axis that displays categories and has a 3D effect. Used for bar charts and
 * line charts.
 * 
 * @author Klaus Rheinwald
 */
public class CategoryAxis3D extends org.jfree.chart.axis.CategoryAxis3D implements Cloneable, Serializable {

	/** For serialization. */
	private static final long serialVersionUID = 4114732251353700972L;

	public CategoryAxis3D() {
		this(null);
	}

	public CategoryAxis3D(String label) {
		super(label);
	}

	/*
	 * This method is the same as in the parent class except that it includes a
	 * fix for category labels that wrap on to multiple lines. This fix is
	 * highlighted below with comments.
	 */
	public AxisSpace reserveSpace(Graphics2D g2, Plot plot, Rectangle2D plotArea, RectangleEdge edge, AxisSpace space) {

		// create a new space object if one wasn't supplied...
		if (space == null) {
			space = new AxisSpace();
		}

		// if the axis is not visible, no additional space is required...
		if (!isVisible()) {
			return space;
		}

		// calculate the max size of the tick labels (if visible)...
		double tickLabelHeight = 0.0;
		double tickLabelWidth = 0.0;
		if (isTickLabelsVisible()) {
			g2.setFont(getTickLabelFont());
			AxisState state = new AxisState();
			// we call refresh ticks just to get the maximum width or height
			if (RectangleEdge.isTopOrBottom(edge)) {
				// BEGIN fix for category labels that wrap
				// If space has been reserved to the left and right then we need to
				// reduce the plot area
				// by this amount so that the space needed by category labels that wrap
				// on to multiple lines
				// will be calculated properly.
				Rectangle2D newPlotArea = new Rectangle2D.Double(plotArea.getX(), plotArea.getY(), plotArea.getWidth() - space.getLeft() - space.getRight(), plotArea.getHeight());
				refreshTicks(g2, state, newPlotArea, edge);
				// END fix for category labels that wrap
			} else {
				refreshTicks(g2, state, plotArea, edge);
			}
			if (edge == RectangleEdge.TOP) {
				tickLabelHeight = state.getMax();
			} else if (edge == RectangleEdge.BOTTOM) {
				tickLabelHeight = state.getMax();
			} else if (edge == RectangleEdge.LEFT) {
				tickLabelWidth = state.getMax();
			} else if (edge == RectangleEdge.RIGHT) {
				tickLabelWidth = state.getMax();
			}
		}

		// get the axis label size and update the space object...
		Rectangle2D labelEnclosure = getLabelEnclosure(g2, edge);
		double labelHeight = 0.0;
		double labelWidth = 0.0;
		if (RectangleEdge.isTopOrBottom(edge)) {
			labelHeight = labelEnclosure.getHeight();
			space.add(labelHeight + tickLabelHeight + this.getCategoryLabelPositionOffset(), edge);
		} else if (RectangleEdge.isLeftOrRight(edge)) {
			labelWidth = labelEnclosure.getWidth();
			space.add(labelWidth + tickLabelWidth + this.getCategoryLabelPositionOffset(), edge);
		}
		return space;

	}
}
