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

import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.ClusteredXYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;

import com.naryx.tagfusion.cfm.tag.awt.cfCHART;

public class CustomClusteredXYBarRenderer extends ClusteredXYBarRenderer implements CustomColorRenderer {
	private static final long serialVersionUID = 1L;
	/** The colors. */
	private Vector colors = new Vector();

	/**
	 * Creates a new renderer.
	 * 
	 * @param colors
	 *          the colors.
	 */
	public CustomClusteredXYBarRenderer(Paint[] colors) {
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

	/*
	 * This method is exactly the same as the method in the superclass except that
	 * it contains a fix for horizontal bar charts that have a margin. The fix is
	 * highlighted with comments below.
	 * 
	 * It was also necessary to comment out the reference to
	 * this.centerBarAtStartValue since this member is private. With BlueDragon it
	 * is always false so it is safe to comment it out.
	 * 
	 * It was also necessary to change the parameters passed to drawItemLabel to
	 * fix a problem with it.
	 */
	public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, CrosshairState crosshairState, int pass) {

		IntervalXYDataset intervalDataset = (IntervalXYDataset) dataset;

		Paint seriesPaint = getItemPaint(series, item);

		double value0;
		double value1;
		if (getUseYInterval()) {
			value0 = intervalDataset.getStartYValue(series, item);
			value1 = intervalDataset.getEndYValue(series, item);
		} else {
			value0 = getBase();
			value1 = intervalDataset.getYValue(series, item);
		}
		if (Double.isNaN(value0) || Double.isNaN(value1)) {
			return;
		}

		double translatedValue0 = rangeAxis.valueToJava2D(value0, dataArea, plot.getRangeAxisEdge());
		double translatedValue1 = rangeAxis.valueToJava2D(value1, dataArea, plot.getRangeAxisEdge());

		RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
		double x1 = intervalDataset.getStartXValue(series, item);
		double translatedX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);

		double x2 = intervalDataset.getEndXValue(series, item);
		double translatedX2 = domainAxis.valueToJava2D(x2, dataArea, xAxisLocation);

		double translatedWidth = Math.max(1, Math.abs(translatedX2 - translatedX1));
		double translatedHeight = Math.abs(translatedValue0 - translatedValue1);

		/*
		 * With BlueDragon, this value is always false so it's safe to comment this
		 * code out. if (this.centerBarAtStartValue) { translatedX1 -=
		 * translatedWidth / 2; }
		 */

		PlotOrientation orientation = plot.getOrientation();
		if (getMargin() > 0.0) {
			if (orientation == PlotOrientation.HORIZONTAL) {
				// BEGIN fix for horizontal bar charts that have a margin
				double cut = translatedWidth * getMargin();
				translatedWidth = translatedWidth - cut;
				translatedX1 = translatedX1 - cut / 2;
				// END fix for horizontal bar charts that have a margin
			} else if (orientation == PlotOrientation.VERTICAL) {
				double cut = translatedWidth * getMargin();
				translatedWidth = translatedWidth - cut;
				translatedX1 = translatedX1 + cut / 2;
			}
		}

		int numSeries = dataset.getSeriesCount();
		double seriesBarWidth = translatedWidth / numSeries;

		Rectangle2D bar = null;
		if (orientation == PlotOrientation.HORIZONTAL) {
			bar = new Rectangle2D.Double(Math.min(translatedValue0, translatedValue1), translatedX1 - seriesBarWidth * (numSeries - series), translatedHeight, seriesBarWidth);
		} else if (orientation == PlotOrientation.VERTICAL) {

			bar = new Rectangle2D.Double(translatedX1 + seriesBarWidth * series, Math.min(translatedValue0, translatedValue1), seriesBarWidth, translatedHeight);

		}
		g2.setPaint(seriesPaint);
		g2.fill(bar);
		if (isDrawBarOutline() && Math.abs(translatedX2 - translatedX1) > 3) {
			g2.setStroke(getItemOutlineStroke(series, item));
			g2.setPaint(getItemOutlinePaint(series, item));
			g2.draw(bar);
		}

		// TODO: we need something better for the item labels
		if (isItemLabelVisible(series, item)) {
			// Change parameters passed to this method to call our local version
			drawItemLabel(g2, orientation, dataset, series, item, bar, value1 < 0.0);
		}

		// add an entity for the item...
		if (info != null) {
			EntityCollection entities = info.getOwner().getEntityCollection();
			if (entities != null) {
				String tip = null;
				XYToolTipGenerator generator = getToolTipGenerator(series, item);
				if (generator != null) {
					tip = generator.generateToolTip(dataset, series, item);
				}
				String url = null;
				if (getURLGenerator() != null) {
					url = getURLGenerator().generateURL(dataset, series, item);
				}
				XYItemEntity entity = new XYItemEntity(bar, dataset, series, item, tip, url);
				entities.add(entity);
			}
		}

	}

	/*
	 * This method follows the algorithm of the same method in
	 * org.jfree.chart.renderer.category.BarRenderer except that for an inside
	 * position it doesn't check if the label will fit inside the bar. Instead it
	 * just always displays the label.
	 */
	private void drawItemLabel(Graphics2D g2, PlotOrientation orientation, XYDataset dataset, int series, int item, Rectangle2D bar, boolean negative) {

		XYItemLabelGenerator generator = getItemLabelGenerator(series, item);
		if (generator == null)
			return;

		String label = generator.generateLabel(dataset, series, item);
		if (label == null) {
			return; // nothing to do
		}

		Font labelFont = getItemLabelFont(series, item);
		g2.setFont(labelFont);
		Paint paint = getItemLabelPaint(series, item);
		g2.setPaint(paint);

		// find out where to place the label...
		ItemLabelPosition position = null;
		if (!negative) {
			position = getPositiveItemLabelPosition(series, item);
		} else {
			position = getNegativeItemLabelPosition(series, item);
		}

		// work out the label anchor point...
		Point2D anchorPoint = calculateLabelAnchorPoint(position.getItemLabelAnchor(), bar, orientation);

		TextUtilities.drawRotatedString(label, g2, (float) anchorPoint.getX(), (float) anchorPoint.getY(), position.getTextAnchor(), position.getAngle(), position.getRotationAnchor());
	}

	/*
	 * This method was copied from org.jfree.chart.renderer.category.BarRenderer.
	 */
	private Point2D calculateLabelAnchorPoint(ItemLabelAnchor anchor, Rectangle2D bar, PlotOrientation orientation) {
		Point2D result = null;
		double offset = getItemLabelAnchorOffset();
		double x0 = bar.getX() - offset;
		double x1 = bar.getX();
		double x2 = bar.getX() + offset;
		double x3 = bar.getCenterX();
		double x4 = bar.getMaxX() - offset;
		double x5 = bar.getMaxX();
		double x6 = bar.getMaxX() + offset;

		double y0 = bar.getMaxY() + offset;
		double y1 = bar.getMaxY();
		double y2 = bar.getMaxY() - offset;
		double y3 = bar.getCenterY();
		double y4 = bar.getMinY() + offset;
		double y5 = bar.getMinY();
		double y6 = bar.getMinY() - offset;

		if (anchor == ItemLabelAnchor.CENTER) {
			result = new Point2D.Double(x3, y3);
		} else if (anchor == ItemLabelAnchor.INSIDE1) {
			result = new Point2D.Double(x4, y4);
		} else if (anchor == ItemLabelAnchor.INSIDE2) {
			result = new Point2D.Double(x4, y4);
		} else if (anchor == ItemLabelAnchor.INSIDE3) {
			result = new Point2D.Double(x4, y3);
		} else if (anchor == ItemLabelAnchor.INSIDE4) {
			result = new Point2D.Double(x4, y2);
		} else if (anchor == ItemLabelAnchor.INSIDE5) {
			result = new Point2D.Double(x4, y2);
		} else if (anchor == ItemLabelAnchor.INSIDE6) {
			result = new Point2D.Double(x3, y2);
		} else if (anchor == ItemLabelAnchor.INSIDE7) {
			result = new Point2D.Double(x2, y2);
		} else if (anchor == ItemLabelAnchor.INSIDE8) {
			result = new Point2D.Double(x2, y2);
		} else if (anchor == ItemLabelAnchor.INSIDE9) {
			result = new Point2D.Double(x2, y3);
		} else if (anchor == ItemLabelAnchor.INSIDE10) {
			result = new Point2D.Double(x2, y4);
		} else if (anchor == ItemLabelAnchor.INSIDE11) {
			result = new Point2D.Double(x2, y4);
		} else if (anchor == ItemLabelAnchor.INSIDE12) {
			result = new Point2D.Double(x3, y4);
		} else if (anchor == ItemLabelAnchor.OUTSIDE1) {
			result = new Point2D.Double(x5, y6);
		} else if (anchor == ItemLabelAnchor.OUTSIDE2) {
			result = new Point2D.Double(x6, y5);
		} else if (anchor == ItemLabelAnchor.OUTSIDE3) {
			result = new Point2D.Double(x6, y3);
		} else if (anchor == ItemLabelAnchor.OUTSIDE4) {
			result = new Point2D.Double(x6, y1);
		} else if (anchor == ItemLabelAnchor.OUTSIDE5) {
			result = new Point2D.Double(x5, y0);
		} else if (anchor == ItemLabelAnchor.OUTSIDE6) {
			result = new Point2D.Double(x3, y0);
		} else if (anchor == ItemLabelAnchor.OUTSIDE7) {
			result = new Point2D.Double(x1, y0);
		} else if (anchor == ItemLabelAnchor.OUTSIDE8) {
			result = new Point2D.Double(x0, y1);
		} else if (anchor == ItemLabelAnchor.OUTSIDE9) {
			result = new Point2D.Double(x0, y3);
		} else if (anchor == ItemLabelAnchor.OUTSIDE10) {
			result = new Point2D.Double(x0, y5);
		} else if (anchor == ItemLabelAnchor.OUTSIDE11) {
			result = new Point2D.Double(x1, y6);
		} else if (anchor == ItemLabelAnchor.OUTSIDE12) {
			result = new Point2D.Double(x3, y6);
		}

		return result;
	}

	/**
	 * Returns a default legend item for the specified series. Subclasses should
	 * override this method to generate customised items.
	 * 
	 * @param datasetIndex
	 *          the dataset index (zero-based).
	 * @param series
	 *          the series index (zero-based).
	 * 
	 * @return A legend item for the series.
	 */
	public LegendItem getLegendItem(int datasetIndex, int series) {
		LegendItem result = null;
		XYPlot xyplot = getPlot();
		if (xyplot != null) {
			XYDataset dataset = xyplot.getDataset(datasetIndex);
			if (dataset != null) {
				XYSeriesLabelGenerator lg = getLegendItemLabelGenerator();
				String label = lg.generateLabel(dataset, series);
				String description = label;
				String toolTipText = null;
				if (getLegendItemToolTipGenerator() != null) {
					toolTipText = getLegendItemToolTipGenerator().generateLabel(dataset, series);
				}
				String urlText = null;
				if (getLegendItemURLGenerator() != null) {
					urlText = getLegendItemURLGenerator().generateLabel(dataset, series);
				}
				Shape shape = getLegendBar();
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

				result = new LegendItem(label, description, toolTipText, urlText, shape, paint, outlineStroke, outlinePaint);
			}
		}
		return result;
	}
}
