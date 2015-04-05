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
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.PublicCloneable;

/**
 * Line/Step item renderer for an {@link XYPlot}. This class draws lines
 * between data points, only allowing horizontal or vertical lines (steps).
 * 
 * @author Roger Studner
 */
public class CustomXYStepRenderer extends XYStepRenderer implements Cloneable, PublicCloneable, Serializable {

	/** For serialization. */
	private static final long serialVersionUID = -8918141928884796108L;

	/**
	 * Constructs a new renderer with no tooltip or URL generation.
	 */
	public CustomXYStepRenderer() {
		super();
	}

	/**
	 * Constructs a new renderer.
	 * 
	 * @param toolTipGenerator
	 *          the item label generator.
	 * @param urlGenerator
	 *          the URL generator.
	 */
	public CustomXYStepRenderer(XYToolTipGenerator toolTipGenerator, XYURLGenerator urlGenerator) {
		super(toolTipGenerator, urlGenerator);
	}

	public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, CrosshairState crosshairState, int pass) {
		super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState, pass);

		// draw the item label if there is one...
		if (isItemLabelVisible(series, item)) {
			// get the data point...
			double x1 = dataset.getXValue(series, item);
			double y1 = dataset.getYValue(series, item);
			if (Double.isNaN(y1)) {
				return;
			}

			RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
			RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
			double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
			double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

			double xx = transX1;
			double yy = transY1;
			PlotOrientation orientation = plot.getOrientation();
			if (orientation == PlotOrientation.HORIZONTAL) {
				xx = transY1;
				yy = transX1;
			}
			drawItemLabel(g2, orientation, dataset, series, item, xx, yy, (y1 < 0.0));
		}
	}

	/**
	 * Returns a clone of the renderer.
	 * 
	 * @return A clone.
	 * 
	 * @throws CloneNotSupportedException
	 *           if the renderer cannot be cloned.
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
