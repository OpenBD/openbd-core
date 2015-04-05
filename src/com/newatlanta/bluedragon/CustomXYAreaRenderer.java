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

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.PublicCloneable;

/**
 * Area item renderer for an {@link XYPlot}. This class can draw (a) shapes at
 * each point, or (b) lines between points, or (c) both shapes and lines, or (d)
 * filled areas, or (e) filled areas and shapes.
 */
public class CustomXYAreaRenderer extends XYAreaRenderer implements Cloneable, PublicCloneable, Serializable {

	/** For serialization. */
	private static final long serialVersionUID = -4481971353973876747L;

	/**
	 * Constructs a new renderer.
	 */
	public CustomXYAreaRenderer() {
		super();
	}

	/**
	 * Constructs a new renderer.
	 * 
	 * @param type
	 *          the type of the renderer.
	 */
	public CustomXYAreaRenderer(int type) {
		super(type, null, null);
	}

	/**
	 * Constructs a new renderer.
	 * <p>
	 * To specify the type of renderer, use one of the constants: SHAPES, LINES,
	 * SHAPES_AND_LINES, AREA or AREA_AND_SHAPES.
	 * 
	 * @param type
	 *          the type of renderer.
	 * @param toolTipGenerator
	 *          the tool tip generator to use (<code>null</code> permitted).
	 * @param urlGenerator
	 *          the URL generator (<code>null</code> permitted).
	 */
	public CustomXYAreaRenderer(int type, XYToolTipGenerator toolTipGenerator, XYURLGenerator urlGenerator) {

		super(type, toolTipGenerator, urlGenerator);
	}

	public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, CrosshairState crosshairState, int pass) {
		super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState, pass);

		// The complete area chart is drawn when drawItem() is called for the last
		// item
		// so we need to draw all of the item labels after the last item so they'll
		// be
		// visible if they overlap the area chart.
		int itemCount = dataset.getItemCount(series);
		if (getPlotArea() && item > 0 && item == (itemCount - 1)) {
			// this is the last item so draw the item labels
			PlotOrientation orientation = plot.getOrientation();

			for (int i = 0; i < itemCount; i++) {
				if (isItemLabelVisible(series, i)) {
					double xValue = dataset.getXValue(series, i);
					double yValue = dataset.getYValue(series, i);
					if (Double.isNaN(yValue)) {
						yValue = 0.0;
					}
					double transXValue = domainAxis.valueToJava2D(xValue, dataArea, plot.getDomainAxisEdge());
					double transYValue = rangeAxis.valueToJava2D(yValue, dataArea, plot.getRangeAxisEdge());

					double xx = transXValue;
					double yy = transYValue;
					if (orientation == PlotOrientation.HORIZONTAL) {
						xx = transYValue;
						yy = transXValue;
					}
					drawItemLabel(g2, orientation, dataset, series, i, xx, yy, (yValue < 0.0));
				}
			}
		}
	}
}
