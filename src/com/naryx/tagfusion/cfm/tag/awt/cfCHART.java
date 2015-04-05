/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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

import java.awt.Font;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryLabelPosition;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.CategoryLabelWidthType;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberAxis3D;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CategoryMarker;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AreaRendererEndType;
import org.jfree.chart.renderer.category.AbstractCategoryItemRenderer;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.CategoryStepRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.LineRenderer3D;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.text.TextBlockAnchor;
import org.jfree.ui.Layer;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import com.nary.awt.image.imageOps;
import com.nary.io.FileUtils;
import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.sql.cfDataSource;
import com.naryx.tagfusion.cfm.tag.cfINCLUDE;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.xmlConfig.xmlCFML;
import com.newatlanta.bluedragon.CategoryAxis;
import com.newatlanta.bluedragon.CategoryAxis3D;
import com.newatlanta.bluedragon.CategoryItemLabelGenerator;
import com.newatlanta.bluedragon.Color;
import com.newatlanta.bluedragon.CustomBarRenderer;
import com.newatlanta.bluedragon.CustomBarRenderer3D;
import com.newatlanta.bluedragon.CustomClusteredXYBarRenderer;
import com.newatlanta.bluedragon.CustomColorRenderer;
import com.newatlanta.bluedragon.CustomStackedBarRenderer;
import com.newatlanta.bluedragon.CustomStackedBarRenderer3D;
import com.newatlanta.bluedragon.CustomXYAreaRenderer;
import com.newatlanta.bluedragon.CustomXYStepRenderer;
import com.newatlanta.bluedragon.XYItemLabelGenerator;

public class cfCHART extends cfTag implements Serializable {
	static final long serialVersionUID = 1;

	private static byte FORMAT_JPG = 0, FORMAT_PNG = 1;

	public static final String DATA_BIN_KEY = "CFCHART_DATA";

	public static final String DEFAULT_STORAGE = "file";

	// A cache size of 0 disables caching. In this case a chart is deleted
	// immediately
	// after it is served up.
	public static final String DEFAULT_CACHE_SIZE_STR = "0";

	public static final int DEFAULT_CACHE_SIZE = 0;

	public static final int FILE = 0;

	public static final int SESSION = 1;

	public static final int DB = 2;

	private static final double DARKER_FACTOR = 0.25;

	private static final double LIGHTER_FACTOR = 0.25;

	private static File cfchartDirectory = new File(cfEngine.thisPlatform.getFileIO().getWorkingDirectory(), "cfchart");

	private static Map<String, Color> colorMap = new FastMap<String, Color>();

	private Map<String, String> nonDefaultAttributes;

	private static List<String> storageCache;

	private static int storageCacheSize = DEFAULT_CACHE_SIZE;

	private static int storage = FILE;

	private static String storageDataSourceName = null;

	private static cfDataSource storageDataSource;

	private static boolean storageDBInit = false;

	private static String SQL_INSERT = "INSERT INTO BDCHARTDATA (CHARTNAME,DATA) VALUES (?,?)";

	public static String SQL_SELECT = "SELECT DATA FROM BDCHARTDATA WHERE CHARTNAME = ?";

	public static String SQL_DELETE = "DELETE FROM BDCHARTDATA WHERE CHARTNAME = ?";

	private static String[] SQL_CREATE_TABLE = new String[] {
			// PointBase (NOTE: must come before Oracle entry otherwise it will be
			// used.)
			"CREATE TABLE BDCHARTDATA ( CHARTNAME VARCHAR(43) PRIMARY KEY, DATA BLOB(2G) )",
			// Oracle, MySQL, Informix
			"CREATE TABLE BDCHARTDATA ( CHARTNAME VARCHAR(43) PRIMARY KEY, DATA BLOB )",
			// Microsoft Access, Microsoft SQL Server, Sybase
			"CREATE TABLE BDCHARTDATA ( CHARTNAME VARCHAR(43) PRIMARY KEY, DATA IMAGE )",
			// PostgreSQL
			"CREATE TABLE BDCHARTDATA ( CHARTNAME VARCHAR(43) PRIMARY KEY, DATA BYTEA )",
			// DB2
			"CREATE TABLE BDCHARTDATA ( CHARTNAME VARCHAR(43) NOT NULL PRIMARY KEY, DATA BLOB )" };

	public java.util.Map getInfo() {
		return createInfo("output", "Used to produce charts from queries or specified chart series data");
	}

	public java.util.Map[] getAttInfo() {
		return new java.util.Map[] {
				createAttInfo("BACKGROUNDCOLOR", "Background colour of the chart. This can be a hexadecimal colour " + "or one of the following predefined colours: " + "aliceblue, antiquewhite, aqua, aquamarine, azure, beige, bisque, black, " + "blanchedalmond, blue, blueviolet, brown, burlywood, cadetblue, chartreuse, " + "chocolate, coral, cornflowerblue, cornsilk, crimson, cyan, darkblue, darkcyan, "
						+ "darkgoldenrod, darkgray, darkgreen, darkgrey, darkkhaki, darkmagenta, darkolivegreen, " + "darkorange, darkorchid, darkred, darksalmon, darkslateblue, darkslategray, " + "darkslategrey, darkturquoise, darkviolet, deeppink, deepskyblue, dimgray, dimgrey, " + "dodgerblue, firebrick, floralwhite, forestgreen, fuchsia, gainsboro, ghostwhite, "
						+ "gold, goldenrod, gray, green, greenyellow, grey, honeydew, hotpink, indianred, " + "indigo, ivory, khaki, lavender, lavenderblush, lawngreen, lemonchiffon, lightblue, " + "lightcoral, lightcyan, lightgoldenrodyellow, lightgray, lightgreen, lightgrey, " + "lightpink, lightsalmon, lightseagreen, lightskyblue, lightslategray, lightslategrey, "
						+ "lightsteelblue, lightyellow, lime, limegreen, linen, magenta, maroon, mediumaquamarine, " + "mediumblue, mediumorchid, mediumpurple, mediumseagreen, mediumslateblue, mediumspringgreen, " + "mediumturquoise, mediumvioletred, midnightblue, mintcream, mistyrose, moccasin, navajowhite, " + "navy, oldlace, olive, olivedrab, orange, orangered, orchid, palegoldenrod, palegreen, "
						+ "paleturquoise, palevioletred, papayawhip, peachpuff, peru, pink, plum, powderblue, purple, " + "red, rosybrown, royalblue, saddlebrown, salmon, sandybrown, seagreen, seashell, sienna, " + "silver, skyblue, slateblue, slategray, slategrey, snow, springgreen, steelblue, tan, teal, " + "thistle, tomato, turquoise, violet, violetred, wheat, white, whitesmoke, yellow, yellowgreen",
						"white", false),
				createAttInfo("CATEGORYLABELPOSITIONS", "Positions of category axis labels. Options are horizontal and vertical", "horizontal", false),
				createAttInfo("CHARTHEIGHT", "Physical height of the chart produced in pixels", "240", false),
				createAttInfo("CHARTWIDTH", "Physical width of the chart produced in pixels", "320", false),
				createAttInfo("DATABACKGROUNDCOLOR", "Background colour of the plot area. This can be a hexadecimal colour " + "or one of the following predefined colours: " + "aliceblue, antiquewhite, aqua, aquamarine, azure, beige, bisque, black, " + "blanchedalmond, blue, blueviolet, brown, burlywood, cadetblue, chartreuse, "
						+ "chocolate, coral, cornflowerblue, cornsilk, crimson, cyan, darkblue, darkcyan, " + "darkgoldenrod, darkgray, darkgreen, darkgrey, darkkhaki, darkmagenta, darkolivegreen, " + "darkorange, darkorchid, darkred, darksalmon, darkslateblue, darkslategray, " + "darkslategrey, darkturquoise, darkviolet, deeppink, deepskyblue, dimgray, dimgrey, "
						+ "dodgerblue, firebrick, floralwhite, forestgreen, fuchsia, gainsboro, ghostwhite, " + "gold, goldenrod, gray, green, greenyellow, grey, honeydew, hotpink, indianred, " + "indigo, ivory, khaki, lavender, lavenderblush, lawngreen, lemonchiffon, lightblue, " + "lightcoral, lightcyan, lightgoldenrodyellow, lightgray, lightgreen, lightgrey, "
						+ "lightpink, lightsalmon, lightseagreen, lightskyblue, lightslategray, lightslategrey, " + "lightsteelblue, lightyellow, lime, limegreen, linen, magenta, maroon, mediumaquamarine, " + "mediumblue, mediumorchid, mediumpurple, mediumseagreen, mediumslateblue, mediumspringgreen, " + "mediumturquoise, mediumvioletred, midnightblue, mintcream, mistyrose, moccasin, navajowhite, "
						+ "navy, oldlace, olive, olivedrab, orange, orangered, orchid, palegoldenrod, palegreen, " + "paleturquoise, palevioletred, papayawhip, peachpuff, peru, pink, plum, powderblue, purple, " + "red, rosybrown, royalblue, saddlebrown, salmon, sandybrown, seagreen, seashell, sienna, " + "silver, skyblue, slateblue, slategray, slategrey, snow, springgreen, steelblue, tan, teal, "
						+ "thistle, tomato, turquoise, violet, violetred, wheat, white, whitesmoke, yellow, yellowgreen", "white", false),
				createAttInfo("FOREGROUNDCOLOR", "Text colour of the chart's title and legend. This can be a hexadecimal colour " + "or one of the following predefined colours: " + "aliceblue, antiquewhite, aqua, aquamarine, azure, beige, bisque, black, " + "blanchedalmond, blue, blueviolet, brown, burlywood, cadetblue, chartreuse, "
						+ "chocolate, coral, cornflowerblue, cornsilk, crimson, cyan, darkblue, darkcyan, " + "darkgoldenrod, darkgray, darkgreen, darkgrey, darkkhaki, darkmagenta, darkolivegreen, " + "darkorange, darkorchid, darkred, darksalmon, darkslateblue, darkslategray, " + "darkslategrey, darkturquoise, darkviolet, deeppink, deepskyblue, dimgray, dimgrey, "
						+ "dodgerblue, firebrick, floralwhite, forestgreen, fuchsia, gainsboro, ghostwhite, " + "gold, goldenrod, gray, green, greenyellow, grey, honeydew, hotpink, indianred, " + "indigo, ivory, khaki, lavender, lavenderblush, lawngreen, lemonchiffon, lightblue, " + "lightcoral, lightcyan, lightgoldenrodyellow, lightgray, lightgreen, lightgrey, "
						+ "lightpink, lightsalmon, lightseagreen, lightskyblue, lightslategray, lightslategrey, " + "lightsteelblue, lightyellow, lime, limegreen, linen, magenta, maroon, mediumaquamarine, " + "mediumblue, mediumorchid, mediumpurple, mediumseagreen, mediumslateblue, mediumspringgreen, " + "mediumturquoise, mediumvioletred, midnightblue, mintcream, mistyrose, moccasin, navajowhite, "
						+ "navy, oldlace, olive, olivedrab, orange, orangered, orchid, palegoldenrod, palegreen, " + "paleturquoise, palevioletred, papayawhip, peachpuff, peru, pink, plum, powderblue, purple, " + "red, rosybrown, royalblue, saddlebrown, salmon, sandybrown, seagreen, seashell, sienna, " + "silver, skyblue, slateblue, slategray, slategrey, snow, springgreen, steelblue, tan, teal, "
						+ "thistle, tomato, turquoise, violet, violetred, wheat, white, whitesmoke, yellow, yellowgreen", "black", false), createAttInfo("FONT", "Font of the title and legend. Options are: arial, times and courier", "arial", false), createAttInfo("FONTBOLD", "Whether font is bold : yes or no", "no", false), createAttInfo("FONTITALIC", "Whether font is italic : yes or no", "no", false),
				createAttInfo("FONTSIZE", "Size of font", "11", false), createAttInfo("FORMAT", "Format that Chart is in. Options are jpg or png", "jpg", false), createAttInfo("LABELFORMAT", "The format of the data point label and the axis if applicable. Options: number, currency, percent, date", "number", false), createAttInfo("MARKERSIZE", "The size of the data point marker symbol", "6", false),
				createAttInfo("MAXCATEGORYLABELLINES", "The total maximum category label lines for the chart that are visible", "5", false), createAttInfo("MAXCATEGORYLABELWIDTHRATIO", "Sets the maximum category label width, expressed as a percentage of (a) the category label " + "rectangle, or (b) the length of the range axis.", "", false),
				createAttInfo("PIESLICESTYLE", "Whether pie is solid or slices are exploded. Options: solid, sliced", "solid", false), createAttInfo("SERIESPLACEMENT", "If chart is a category plot it allows you to produce stacked or percent based barcharts. " + "Options: stacked, percent, default", "default", false), createAttInfo("SHOW3D", "Whether chart is to be 3D: yes or no", "no", false),
				createAttInfo("SHOWBORDER", "Whether border is visible around chart: yes or no", "no", false), createAttInfo("SHOWMARKERS", "Whether data point markers are visible: yes or no", "yes", false), createAttInfo("SHOWXGRIDLINES", "Whether x axis grid lines are visible: yes or no", "no", false), createAttInfo("SHOWYGRIDLINES", "Whether y axis grid lines are visible: yes or no", "yes", false),
				createAttInfo("SORTXAXIS", "Whether x axis is to be sorted (Category graphs only)", "no", false), createAttInfo("SYMBOLSSEPARATOR", "Character used to seperate list of YAXISSYMBOLS provided", ",", false), createAttInfo("TIPSTYLE", "Sets the tooltips for the data points. Options: none or mouseover", "mouseover", false),
				createAttInfo("XAXISTYPE", "Determines the type of chart to display. Is overriden by the series data as it is now checked for date values. " + "Options: scale or category", "category", false), createAttInfo("XAXISUPPERMARGIN", "Used to set the x axis upper margin. Used to prevent labels from not being displayed correctly.", "0.05", false),
				createAttInfo("XOFFSET", "An integer value between 0 and the chart width for xOffset", "12", false), createAttInfo("YOFFSET", "An integer value between 0 and the chart height for xOffset", "8", false), createAttInfo("YAXISSYMBOLS", "Delimited list of symbols to be used as the Y axis label", "", false),
				createAttInfo("YAXISTYPE", "Determines the type of chart to display. Is overriden by the series data as it is now checked for date values. " + "Options: scale or category", "scale", false), createAttInfo("YAXISUPPERMARGIN", "Used to set the y axis upper margin. Used to prevent labels from not being displayed correctly.", "0.05", false),
				createAttInfo("PATH", 		"The path for the link to be seen to be coming from","current path", false)		
		};

	}

	private static final CategoryLabelPositions VERTICAL = new CategoryLabelPositions(new CategoryLabelPosition(RectangleAnchor.BOTTOM, TextBlockAnchor.CENTER_LEFT, TextAnchor.CENTER_LEFT, -Math.PI / 2.0, CategoryLabelWidthType.CATEGORY, 2.00f), // TOP
			new CategoryLabelPosition(RectangleAnchor.TOP, TextBlockAnchor.CENTER_RIGHT, TextAnchor.CENTER_RIGHT, -Math.PI / 2.0, CategoryLabelWidthType.CATEGORY, 2.00f), // BOTTOM
			new CategoryLabelPosition(RectangleAnchor.RIGHT, TextBlockAnchor.BOTTOM_CENTER, TextAnchor.BOTTOM_CENTER, -Math.PI / 2.0, CategoryLabelWidthType.CATEGORY, 2.00f), // LEFT
			new CategoryLabelPosition(RectangleAnchor.LEFT, TextBlockAnchor.TOP_CENTER, TextAnchor.TOP_CENTER, -Math.PI / 2.0, CategoryLabelWidthType.CATEGORY, 2.00f) // RIGHT
	);

	public static File getCfchartDirectory() {
		return cfchartDirectory;
	}

	public static int getStorage() {
		return storage;
	}

	public static int getStorageCacheSize() {
		return storageCacheSize;
	}

	public static String getStorageDataSourceName() {
		return storageDataSourceName;
	}

	public static cfDataSource getStorageDataSource() {
		return storageDataSource;
	}

	public static void init(xmlCFML configFile) {
		// Make sure the cfchart directory exists
		cfchartDirectory.mkdirs();

		storageCacheSize = configFile.getInt("server.cfchart.cachesize", DEFAULT_CACHE_SIZE);

		storageCache = new ArrayList<String>(storageCacheSize);

		// Determine where the generated charts need to be stored
		String storageString = configFile.getString("server.cfchart.storage", DEFAULT_STORAGE).toLowerCase();
		if (storageString.equals("file")) {
			storage = FILE;
		} else if (storageString.equals("session")) {
			storage = SESSION;
		} else {
			storage = DB;
			storageDataSourceName = storageString;
		}
		cfEngine.log("cfChart: storage=" + storageString + "; size=" + storageCacheSize );

		clearChartStorage();

		// Initialize the colorMap hashtable
		// NOTE: this values were taken from http://www.w3.org/TR/css3-color/
		colorMap.put("aliceblue", new Color(0xF0F8FF));
		colorMap.put("antiquewhite", new Color(0xFAEBD7));
		colorMap.put("aqua", new Color(0x00FFFF));
		colorMap.put("aquamarine", new Color(0x7FFFD4));
		colorMap.put("azure", new Color(0xF0FFFF));
		colorMap.put("beige", new Color(0xF5F5DC));
		colorMap.put("bisque", new Color(0xFFE4C4));
		colorMap.put("black", new Color(0x000000));
		colorMap.put("blanchedalmond", new Color(0xFFEBCD));
		colorMap.put("blue", new Color(0x0000FF));
		colorMap.put("blueviolet", new Color(0x8A2BE2));
		colorMap.put("brown", new Color(0xA52A2A));
		colorMap.put("burlywood", new Color(0xDEB887));
		colorMap.put("cadetblue", new Color(0x5F9EA0));
		colorMap.put("chartreuse", new Color(0x7FFF00));
		colorMap.put("chocolate", new Color(0xD2691E));
		colorMap.put("coral", new Color(0xFF7F50));
		colorMap.put("cornflowerblue", new Color(0x6495ED));
		colorMap.put("cornsilk", new Color(0xFFF8DC));
		colorMap.put("crimson", new Color(0xDC143C));
		colorMap.put("cyan", new Color(0x00FFFF));
		colorMap.put("darkblue", new Color(0x00008B));
		colorMap.put("darkcyan", new Color(0x008B8B));
		colorMap.put("darkgoldenrod", new Color(0xB8860B));
		colorMap.put("darkgray", new Color(0xA9A9A9));
		colorMap.put("darkgreen", new Color(0x006400));
		colorMap.put("darkgrey", new Color(0xA9A9A9));
		colorMap.put("darkkhaki", new Color(0xBDB76B));
		colorMap.put("darkmagenta", new Color(0x8B008B));
		colorMap.put("darkolivegreen", new Color(0x556B2F));
		colorMap.put("darkorange", new Color(0xFF8C00));
		colorMap.put("darkorchid", new Color(0x9932CC));
		colorMap.put("darkred", new Color(0x8B0000));
		colorMap.put("darksalmon", new Color(0xE9967A));
		colorMap.put("darkslateblue", new Color(0x483D8B));
		colorMap.put("darkslategray", new Color(0x2F4F4F));
		colorMap.put("darkslategrey", new Color(0x2F4F4F));
		colorMap.put("darkturquoise", new Color(0x00CED1));
		colorMap.put("darkviolet", new Color(0x9400D3));
		colorMap.put("deeppink", new Color(0xFF1493));
		colorMap.put("deepskyblue", new Color(0x00BFFF));
		colorMap.put("dimgray", new Color(0x696969));
		colorMap.put("dimgrey", new Color(0x696969));
		colorMap.put("dodgerblue", new Color(0x1E90FF));
		colorMap.put("firebrick", new Color(0xB22222));
		colorMap.put("floralwhite", new Color(0xFFFAF0));
		colorMap.put("forestgreen", new Color(0x228B22));
		colorMap.put("fuchsia", new Color(0xFF00FF));
		colorMap.put("gainsboro", new Color(0xDCDCDC));
		colorMap.put("ghostwhite", new Color(0xF8F8FF));
		colorMap.put("gold", new Color(0xFFD700));
		colorMap.put("goldenrod", new Color(0xDAA520));
		colorMap.put("gray", new Color(0x808080));
		colorMap.put("green", new Color(0x008000));
		colorMap.put("greenyellow", new Color(0xADFF2F));
		colorMap.put("grey", new Color(0x808080));
		colorMap.put("honeydew", new Color(0xF0FFF0));
		colorMap.put("hotpink", new Color(0xFF69B4));
		colorMap.put("indianred", new Color(0xCD5C5C));
		colorMap.put("indigo", new Color(0x4B0082));
		colorMap.put("ivory", new Color(0xFFFFF0));
		colorMap.put("khaki", new Color(0xF0E68C));
		colorMap.put("lavender", new Color(0xE6E6FA));
		colorMap.put("lavenderblush", new Color(0xFFF0F5));
		colorMap.put("lawngreen", new Color(0x7CFC00));
		colorMap.put("lemonchiffon", new Color(0xFFFACD));
		colorMap.put("lightblue", new Color(0xADD8E6));
		colorMap.put("lightcoral", new Color(0xF08080));
		colorMap.put("lightcyan", new Color(0xE0FFFF));
		colorMap.put("lightgoldenrodyellow", new Color(0xFAFAD2));
		colorMap.put("lightgray", new Color(0xD3D3D3));
		colorMap.put("lightgreen", new Color(0x90EE90));
		colorMap.put("lightgrey", new Color(0xD3D3D3));
		colorMap.put("lightpink", new Color(0xFFB6C1));
		colorMap.put("lightsalmon", new Color(0xFFA07A));
		colorMap.put("lightseagreen", new Color(0x20B2AA));
		colorMap.put("lightskyblue", new Color(0x87CEFA));
		colorMap.put("lightslategray", new Color(0x778899));
		colorMap.put("lightslategrey", new Color(0x778899));
		colorMap.put("lightsteelblue", new Color(0xB0C4DE));
		colorMap.put("lightyellow", new Color(0xFFFFE0));
		colorMap.put("lime", new Color(0x00FF00));
		colorMap.put("limegreen", new Color(0x32CD32));
		colorMap.put("linen", new Color(0xFAF0E6));
		colorMap.put("magenta", new Color(0xFF00FF));
		colorMap.put("maroon", new Color(0x800000));
		colorMap.put("mediumaquamarine", new Color(0x66CDAA));
		colorMap.put("mediumblue", new Color(0x0000CD));
		colorMap.put("mediumorchid", new Color(0xBA55D3));
		colorMap.put("mediumpurple", new Color(0x9370DB));
		colorMap.put("mediumseagreen", new Color(0x3CB371));
		colorMap.put("mediumslateblue", new Color(0x7B68EE));
		colorMap.put("mediumspringgreen", new Color(0x00FA9A));
		colorMap.put("mediumturquoise", new Color(0x48D1CC));
		colorMap.put("mediumvioletred", new Color(0xC71585));
		colorMap.put("midnightblue", new Color(0x191970));
		colorMap.put("mintcream", new Color(0xF5FFFA));
		colorMap.put("mistyrose", new Color(0xFFE4E1));
		colorMap.put("moccasin", new Color(0xFFE4B5));
		colorMap.put("navajowhite", new Color(0xFFDEAD));
		colorMap.put("navy", new Color(0x000080));
		colorMap.put("oldlace", new Color(0xFDF5E6));
		colorMap.put("olive", new Color(0x808000));
		colorMap.put("olivedrab", new Color(0x6B8E23));
		colorMap.put("orange", new Color(0xFFA500));
		colorMap.put("orangered", new Color(0xFF4500));
		colorMap.put("orchid", new Color(0xDA70D6));
		colorMap.put("palegoldenrod", new Color(0xEEE8AA));
		colorMap.put("palegreen", new Color(0x98FB98));
		colorMap.put("paleturquoise", new Color(0xAFEEEE));
		colorMap.put("palevioletred", new Color(0xDB7093));
		colorMap.put("papayawhip", new Color(0xFFEFD5));
		colorMap.put("peachpuff", new Color(0xFFDAB9));
		colorMap.put("peru", new Color(0xCD853F));
		colorMap.put("pink", new Color(0xFFC0CB));
		colorMap.put("plum", new Color(0xDDA0DD));
		colorMap.put("powderblue", new Color(0xB0E0E6));
		colorMap.put("purple", new Color(0x800080));
		colorMap.put("red", new Color(0xFF0000));
		colorMap.put("rosybrown", new Color(0xBC8F8F));
		colorMap.put("royalblue", new Color(0x4169E1));
		colorMap.put("saddlebrown", new Color(0x8B4513));
		colorMap.put("salmon", new Color(0xFA8072));
		colorMap.put("sandybrown", new Color(0xF4A460));
		colorMap.put("seagreen", new Color(0x2E8B57));
		colorMap.put("seashell", new Color(0xFFF5EE));
		colorMap.put("sienna", new Color(0xA0522D));
		colorMap.put("silver", new Color(0xC0C0C0));
		colorMap.put("skyblue", new Color(0x87CEEB));
		colorMap.put("slateblue", new Color(0x6A5ACD));
		colorMap.put("slategray", new Color(0x708090));
		colorMap.put("slategrey", new Color(0x708090));
		colorMap.put("snow", new Color(0xFFFAFA));
		colorMap.put("springgreen", new Color(0x00FF7F));
		colorMap.put("steelblue", new Color(0x4682B4));
		colorMap.put("tan", new Color(0xD2B48C));
		colorMap.put("teal", new Color(0x008080));
		colorMap.put("thistle", new Color(0xD8BFD8));
		colorMap.put("tomato", new Color(0xFF6347));
		colorMap.put("turquoise", new Color(0x40E0D0));
		colorMap.put("violet", new Color(0xEE82EE));
		colorMap.put("violetred", new Color(0xD02090)); // from
																										// http://www.w3schools.com/html/html_colornames.asp
		colorMap.put("wheat", new Color(0xF5DEB3));
		colorMap.put("white", new Color(0xFFFFFF));
		colorMap.put("whitesmoke", new Color(0xF5F5F5));
		colorMap.put("yellow", new Color(0xFFFF00));
		colorMap.put("yellowgreen", new Color(0x9ACD32));
	}

	private static void initDBStorage(cfSession _Session) throws cfmRunTimeException {
		try {
			storageDataSource = new cfDataSource(storageDataSourceName, _Session);
		} catch (cfmRunTimeException RFE) {
			RFE.getCatchData().setExtendedInfo(cfEngine.getMessage("cfapplication.cfchartBadDataSource"));
			throw RFE;
		}

		// If the BDCHARTDATA table doesn't exist then create it
		Connection con = null;

		try {
			con = storageDataSource.getConnection();

			if (!com.nary.db.metaDatabase.tableExist(con, storageDataSource.getCatalog(), "BDCHARTDATA")) {
				int x;

				for (x = 0; x < SQL_CREATE_TABLE.length; x++) {
					if (!com.nary.db.metaDatabase.createTable(con, SQL_CREATE_TABLE[x]))
						cfEngine.log("-] CFCHART.FailedToCreateTable:[" + SQL_CREATE_TABLE[x] + "]");
					else
						break;
				}

				if (x == SQL_CREATE_TABLE.length) {
					cfCatchData catchData = new cfCatchData();
					catchData.setType(cfCatchData.TYPE_APPLICATION);
					catchData.setDetail("Database error");
					catchData.setMessage("Failed to create the BDCHARTDATA table");
					catchData.setExtendedInfo("Failed to create the BDCHARTDATA table");
					throw new cfmRunTimeException(catchData);
				}
			}
		} catch (SQLException E) {
			cfEngine.log("Error initializing BDCHARTDATA table: " + E);
			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_APPLICATION);
			catchData.setDetail("Database error: " + E.getMessage());
			catchData.setMessage("Error occurred when attempting to initialize the BDCHARTDATA table");
			catchData.setExtendedInfo("Error occurred when attempting to initialize the BDCHARTDATA table");
			throw new cfmRunTimeException(catchData);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
		}

		storageDBInit = true;
	}

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		// Parse the tag header for attributes
		parseTagHeader(_tag);

		// Keep track of the attributes that don't use default values
		nonDefaultAttributes = new FastMap<String, String>(properties);

		// NOTE: the default values need to be set at render time in
		// setDefaultParameters()
	}

	private cfTag setDefaultParameters(cfSession _Session) throws cfmRunTimeException {
		unclipAttributes();

		// Check if a default file was specified
		cfTag defaultChartTag = null;
		if (containsAttribute("DEFAULT")) {
			String defaultFilePath = getDynamic(_Session, "DEFAULT").getString();
			cfFile defaultFile = null;
			try {
				defaultFile = cfINCLUDE.loadTemplate(_Session, defaultFilePath);
			} catch (cfmBadFileException bfe) {
				if (bfe.fileNotFound()) {
					cfCatchData catchData = catchDataFactory.missingFileException(this, defaultFilePath);
					catchData.setSession(_Session);
					throw new cfmRunTimeException(catchData);
				}

				cfCatchData catchData = catchDataFactory.summarizeBadFileException(this, "Badly formatted template: " + defaultFilePath, bfe);
				catchData.setSession(_Session);
				throw new cfmRunTimeException(catchData, bfe);
			}

			cfTag fileBody = defaultFile.getFileBody();
			cfTag[] childTags = fileBody.getTagList();
			if (childTags.length == 0)
				throw newRunTimeException("The default file must contain a cfchart tag");
			if (childTags.length > 1)
				throw newRunTimeException("The default file must contain a single cfchart tag");

			defaultChartTag = childTags[0];
		}

		// Set the default values for attributes that were not set
		if (!nonDefaultAttributes.containsKey("BACKGROUNDCOLOR")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("BACKGROUNDCOLOR")))
				defaultAttribute("BACKGROUNDCOLOR", defaultChartTag.getConstant("BACKGROUNDCOLOR"));
			else
				defaultAttribute("BACKGROUNDCOLOR", "white");
		}
		if (!nonDefaultAttributes.containsKey("CATEGORYLABELPOSITIONS")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("CATEGORYLABELPOSITIONS")))
				defaultAttribute("CATEGORYLABELPOSITIONS", defaultChartTag.getConstant("CATEGORYLABELPOSITIONS"));
			else
				defaultAttribute("CATEGORYLABELPOSITIONS", "horizontal");
		}
		if (!nonDefaultAttributes.containsKey("CHARTHEIGHT")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("CHARTHEIGHT")))
				defaultAttribute("CHARTHEIGHT", defaultChartTag.getConstant("CHARTHEIGHT"));
			else
				defaultAttribute("CHARTHEIGHT", "240");
		}
		if (!nonDefaultAttributes.containsKey("CHARTWIDTH")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("CHARTWIDTH")))
				defaultAttribute("CHARTWIDTH", defaultChartTag.getConstant("CHARTWIDTH"));
			else
				defaultAttribute("CHARTWIDTH", "320");
		}
		if (!nonDefaultAttributes.containsKey("DATABACKGROUNDCOLOR")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("DATABACKGROUNDCOLOR")))
				defaultAttribute("DATABACKGROUNDCOLOR", defaultChartTag.getConstant("DATABACKGROUNDCOLOR"));
			else
				defaultAttribute("DATABACKGROUNDCOLOR", "white");
		}
		if (!nonDefaultAttributes.containsKey("FOREGROUNDCOLOR")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("FOREGROUNDCOLOR")))
				defaultAttribute("FOREGROUNDCOLOR", defaultChartTag.getConstant("FOREGROUNDCOLOR"));
			else
				defaultAttribute("FOREGROUNDCOLOR", "black");
		}
		if (!nonDefaultAttributes.containsKey("FONT")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("FONT")))
				defaultAttribute("FONT", defaultChartTag.getConstant("FONT"));
			else
				defaultAttribute("FONT", "arial");
		}
		if (!nonDefaultAttributes.containsKey("FONTBOLD")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("FONTBOLD")))
				defaultAttribute("FONTBOLD", defaultChartTag.getConstant("FONTBOLD"));
			else
				defaultAttribute("FONTBOLD", "no");
		}
		if (!nonDefaultAttributes.containsKey("FONTITALIC")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("FONTITALIC")))
				defaultAttribute("FONTITALIC", defaultChartTag.getConstant("FONTITALIC"));
			else
				defaultAttribute("FONTITALIC", "no");
		}
		if (!nonDefaultAttributes.containsKey("FONTSIZE")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("FONTSIZE")))
				defaultAttribute("FONTSIZE", defaultChartTag.getConstant("FONTSIZE"));
			else
				defaultAttribute("FONTSIZE", "11");
		}
		if (!nonDefaultAttributes.containsKey("FORMAT")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("FORMAT")))
				defaultAttribute("FORMAT", defaultChartTag.getConstant("FORMAT"));
			else
				defaultAttribute("FORMAT", "jpg"); // CFMX 7 defaults to "flash"
		}
		if (!nonDefaultAttributes.containsKey("LABELFORMAT")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("LABELFORMAT")))
				defaultAttribute("LABELFORMAT", defaultChartTag.getConstant("LABELFORMAT"));
			else
				defaultAttribute("LABELFORMAT", "number");
		}
		if (!nonDefaultAttributes.containsKey("MARKERSIZE")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("MARKERSIZE")))
				defaultAttribute("MARKERSIZE", defaultChartTag.getConstant("MARKERSIZE"));
			else
				defaultAttribute("MARKERSIZE", "6");
		}
		if (!nonDefaultAttributes.containsKey("MAXCATEGORYLABELLINES")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("MAXCATEGORYLABELLINES")))
				defaultAttribute("MAXCATEGORYLABELLINES", defaultChartTag.getConstant("MAXCATEGORYLABELLINES"));
			else
				defaultAttribute("MAXCATEGORYLABELLINES", "5");
		}
		if (!nonDefaultAttributes.containsKey("MAXCATEGORYLABELWIDTHRATIO")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("MAXCATEGORYLABELWIDTHRATIO")))
				defaultAttribute("MAXCATEGORYLABELWIDTHRATIO", defaultChartTag.getConstant("MAXCATEGORYLABELWIDTHRATIO"));
			else
				removeAttribute("MAXCATEGORYLABELWIDTHRATIO");
		}
		if (!nonDefaultAttributes.containsKey("PIESLICESTYLE")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("PIESLICESTYLE")))
				defaultAttribute("PIESLICESTYLE", defaultChartTag.getConstant("PIESLICESTYLE"));
			else
				defaultAttribute("PIESLICESTYLE", "solid");
		}
		if (!nonDefaultAttributes.containsKey("SERIESPLACEMENT")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("SERIESPLACEMENT")))
				defaultAttribute("SERIESPLACEMENT", defaultChartTag.getConstant("SERIESPLACEMENT"));
			else
				defaultAttribute("SERIESPLACEMENT", "default");
		}
		if (!nonDefaultAttributes.containsKey("SHOW3D")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("SHOW3D")))
				defaultAttribute("SHOW3D", defaultChartTag.getConstant("SHOW3D"));
			else
				defaultAttribute("SHOW3D", "no");
		}
		if (!nonDefaultAttributes.containsKey("SHOWBORDER")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("SHOWBORDER")))
				defaultAttribute("SHOWBORDER", defaultChartTag.getConstant("SHOWBORDER"));
			else
				defaultAttribute("SHOWBORDER", "no");
		}
		if (!nonDefaultAttributes.containsKey("SHOWMARKERS")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("SHOWMARKERS")))
				defaultAttribute("SHOWMARKERS", defaultChartTag.getConstant("SHOWMARKERS"));
			else
				defaultAttribute("SHOWMARKERS", "yes");
		}
		if (!nonDefaultAttributes.containsKey("SHOWXGRIDLINES")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("SHOWXGRIDLINES")))
				defaultAttribute("SHOWXGRIDLINES", defaultChartTag.getConstant("SHOWXGRIDLINES"));
			else
				defaultAttribute("SHOWXGRIDLINES", "no");
		}
		if (!nonDefaultAttributes.containsKey("SHOWYGRIDLINES")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("SHOWYGRIDLINES")))
				defaultAttribute("SHOWYGRIDLINES", defaultChartTag.getConstant("SHOWYGRIDLINES"));
			else
				defaultAttribute("SHOWYGRIDLINES", "yes");
		}
		if (!nonDefaultAttributes.containsKey("SORTXAXIS")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("SORTXAXIS")))
				defaultAttribute("SORTXAXIS", defaultChartTag.getConstant("SORTXAXIS"));
			else
				defaultAttribute("SORTXAXIS", "no");
		}
		if (!nonDefaultAttributes.containsKey("SYMBOLSSEPARATOR")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("SYMBOLSSEPARATOR")))
				defaultAttribute("SYMBOLSSEPARATOR", defaultChartTag.getConstant("SYMBOLSSEPARATOR"));
			else
				defaultAttribute("SYMBOLSSEPARATOR", ",");
		}
		if (!nonDefaultAttributes.containsKey("TIPSTYLE")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("TIPSTYLE")))
				defaultAttribute("TIPSTYLE", defaultChartTag.getConstant("TIPSTYLE"));
			else
				defaultAttribute("TIPSTYLE", "mouseover");
		}
		if (!nonDefaultAttributes.containsKey("XAXISTYPE")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("XAXISTYPE")))
				defaultAttribute("XAXISTYPE", defaultChartTag.getConstant("XAXISTYPE"));
			else
				defaultAttribute("XAXISTYPE", "category");
		}
		if (!nonDefaultAttributes.containsKey("XAXISUPPERMARGIN")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("XAXISUPPERMARGIN")))
				defaultAttribute("XAXISUPPERMARGIN", defaultChartTag.getConstant("XAXISUPPERMARGIN"));
			else
				defaultAttribute("XAXISUPPERMARGIN", "0.05");
		}
		if (!nonDefaultAttributes.containsKey("XOFFSET")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("XOFFSET")))
				defaultAttribute("XOFFSET", defaultChartTag.getConstant("XOFFSET"));
			else
				defaultAttribute("XOFFSET", "12");
		}
		if (!nonDefaultAttributes.containsKey("YOFFSET")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("YOFFSET")))
				defaultAttribute("YOFFSET", defaultChartTag.getConstant("YOFFSET"));
			else
				defaultAttribute("YOFFSET", "8");
		}
		if (!nonDefaultAttributes.containsKey("YAXISSYMBOLS")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("YAXISSYMBOLS")))
				defaultAttribute("YAXISSYMBOLS", defaultChartTag.getConstant("YAXISSYMBOLS"));
			else
				removeAttribute("YAXISSYMBOLS");
		}
		if (!nonDefaultAttributes.containsKey("YAXISTYPE")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("YAXISTYPE")))
				defaultAttribute("YAXISTYPE", defaultChartTag.getConstant("YAXISTYPE"));
			else
				defaultAttribute("YAXISTYPE", "scale");
		}
		if (!nonDefaultAttributes.containsKey("YAXISUPPERMARGIN")) {
			if ((defaultChartTag != null) && (defaultChartTag.containsAttribute("YAXISUPPERMARGIN")))
				defaultAttribute("YAXISUPPERMARGIN", defaultChartTag.getConstant("YAXISUPPERMARGIN"));
			else
				defaultAttribute("YAXISUPPERMARGIN", "0.05");
		}

		return defaultChartTag;
	}

	private Color convertStringToColor(String s) throws cfmBadFileException {
		Color c = (Color) colorMap.get(s.toLowerCase());
		if (c != null)
			return c;

		if (s.startsWith("#"))
			s = s.substring(1);

		int len = s.length();
		if (len != 6 && len != 8)
			throw newBadFileException("Invalid Color Attribute", "The color value " + s + " is not supported.");

		try {
			int pos = 0;
			int alpha;
			if (len == 8) {
				alpha = Integer.parseInt(s.substring(pos, pos + 2), 16);
				pos += 2;
			} else {
				alpha = 255;
			}
			int red = (int) Long.parseLong(s.substring(pos, pos + 2), 16);
			pos += 2;
			int green = (int) Long.parseLong(s.substring(pos, pos + 2), 16);
			int blue = (int) Long.parseLong(s.substring(len - 2), 16);
			return new Color(red, green, blue, alpha);
		} catch (NumberFormatException e) {
			throw newBadFileException("Invalid Color Attribute", "The color value " + s + " is not supported.");
		}
	}

	public String getFontName(String fontName) {
		String fontNameLower = fontName.toLowerCase();

		if (fontNameLower.equals("arial"))
			return "Arial";
		if (fontNameLower.equals("times"))
			return "Times New Roman";
		if (fontNameLower.equals("courier"))
			return "Courier New";

		return fontName;
	}

	public Font getFont(cfSession _Session) throws cfmRunTimeException {
		String fontName = getDynamic(_Session, "FONT").toString();
		boolean fontBold = getDynamic(_Session, "FONTBOLD").getBoolean();
		boolean fontItalic = getDynamic(_Session, "FONTITALIC").getBoolean();
		int fontSize = getDynamic(_Session, "FONTSIZE").getInt();

		return getFont(fontName, fontBold, fontItalic, fontSize);
	}

	public Font getFont(String fontName, boolean fontBold, boolean fontItalic, int fontSize) {
		int fontStyle = Font.PLAIN;
		if (fontBold)
			fontStyle += Font.BOLD;
		if (fontItalic)
			fontStyle += Font.ITALIC;

		return new Font(getFontName(fontName), fontStyle, fontSize);
	}

	public String getEndMarker() {
		return "</CFCHART>";
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		if ((storage == DB) && (!storageDBInit))
			initDBStorage(_Session);

		try {
			JFreeChart chart = null;

			cfTag defaultChartTag = setDefaultParameters(_Session);

			// --[ Get the FORMAT property out
			byte format;
			String formatStr = getDynamic(_Session, "FORMAT").toString().toLowerCase();
			if (formatStr.equals("jpg")) {
				format = FORMAT_JPG;
			} else if (formatStr.equals("png")) {
				format = FORMAT_PNG;
			} else {
				throw newBadFileException("Invalid FORMAT Attribute", "Only the jpg and png formats are supported.");
			}

			if (getConstant("STYLE") != null)
				throw newBadFileException("Attribute not supported", "The STYLE attribute is not supported.  Use the DEFAULT attribute instead");

			int height = getDynamic(_Session, "CHARTHEIGHT").getInt();
			int width = getDynamic(_Session, "CHARTWIDTH").getInt();

			String xAxisType = getDynamic(_Session, "XAXISTYPE").toString().toLowerCase();

			cfCHARTInternalData data = new cfCHARTInternalData(xAxisType, defaultChartTag);
			_Session.setDataBin(DATA_BIN_KEY, data);

			// Render the CFCHARTSERIES tags
			renderToString(_Session);

			// Get the chart series
			List<cfCHARTSERIESData> series = data.getSeries();
			if (series.size() == 0)
				throw newRunTimeException("You must specify at least one series");

			String tipStyle = getDynamic(_Session, "TIPSTYLE").toString().toLowerCase();
			String drillDownUrl = null;
			if (containsAttribute("URL"))
				drillDownUrl = getDynamic(_Session, "URL").toString();
			String name = null;
			if (containsAttribute("NAME"))
				name = getDynamic(_Session, "NAME").toString();

			// Check if this is a Pie or Ring chart
			boolean isPieOrRingChart = false;
			if (series.size() == 1) {
				cfCHARTSERIESData seriesData = series.get(0);
				if (seriesData.getType().equals("pie") || seriesData.getType().equals("ring")) {
					// Render the pie or ring chart
					chart = renderPieChart(_Session, data, series, tipStyle, drillDownUrl);
					isPieOrRingChart = true;
				}
			}

			if (!isPieOrRingChart) {
				// This must be a category chart so make sure none of the series are set
				// to Pie or Ring type
				// Also, if show3D is true then make sure there are only bar, line and
				// horizontalbar types
				boolean bShow3D = getDynamic(_Session, "SHOW3D").getBoolean();

				String seriesPlacement = getDynamic(_Session, "SERIESPLACEMENT").toString().toLowerCase();
				if (!seriesPlacement.equals("default") && !seriesPlacement.equals("cluster") && !seriesPlacement.equals("stacked") && !seriesPlacement.equals("percent"))
					throw newRunTimeException("The seriesPlacement value '" + seriesPlacement + "' is not supported");
				if (bShow3D && seriesPlacement.equals("percent"))
					throw newRunTimeException("The seriesPlacement value '" + seriesPlacement + "' is not supported in 3D charts");

				boolean bCluster = true;
				if (seriesPlacement.equals("stacked") || seriesPlacement.equals("percent"))
					bCluster = false;

				if (series.size() > 1) {
					for (int i = 0; i < series.size(); i++) {
						cfCHARTSERIESData seriesData = series.get(i);
						if (seriesData.getType().equals("pie") || seriesData.getType().equals("ring"))
							throw newRunTimeException("A chart can only have 1 series when a pie or ring series is specified");
						if (bShow3D && !seriesData.getType().equals("bar") && !seriesData.getType().equals("line") && !seriesData.getType().equals("horizontalbar"))
							throw newRunTimeException("Only bar, line, horizontal bar and pie charts can be displayed in 3D");
						if (!bCluster && !seriesData.getType().equals("bar") && !seriesData.getType().equals("horizontalbar"))
							throw newRunTimeException("The seriesPlacement value '" + seriesPlacement + "' is only supported in bar and horizontal bar charts");
					}
				}

				int seriesDataType = series.get(0).getSeriesDataType();

				// Render the scale(xy) or category chart
				// This is overriding the type from the cfchart variable as it is
				// determined
				// by the series data.
				if (seriesDataType == cfCHARTSERIESData.CATEGORY_SERIES) {
					chart = renderCategoryChart(_Session, data, tipStyle, drillDownUrl, seriesPlacement, height);
				} else {
					chart = renderXYChart(_Session, data, tipStyle, drillDownUrl, seriesPlacement, height);
				}
			}

			// Output the chart
			try {
				ChartRenderingInfo info = null;
				if ((drillDownUrl != null) || (!tipStyle.equals("none")))
					info = new ChartRenderingInfo(new StandardEntityCollection());

				if (name != null) {
					// Generate the chart and save the contents in the name variable
					byte[] chartBytes = generateChart(chart, format, width, height, info);
					_Session.setData(name, new cfBinaryData(chartBytes));
				} else {
					
					// Return the chart to the browser
					String path = null;
					if (containsAttribute("PATH"))
						path = getDynamic(_Session, "PATH").toString();
					
					String filename = saveChart(_Session, chart, format, width, height, info);
					String chartUrl = generateChartUrl(_Session, filename, path );
					if (info == null) {
						_Session.write("<img src=\"" + chartUrl + "\" width=\"" + width + "\" height=\"" + height + "\" />");
					} else {
						ImageMapUtilities.writeImageMap(_Session.RES.getWriter(), filename, info);
						_Session.write("<img src=\"" + chartUrl + "\" width=\"" + width + "\" height=\"" + height + "\" usemap=\"#" + filename + "\" border=\"0\"/>");
					}
					
				}
			} catch (IOException ioe) {
				throw newRunTimeException("Failed to generate chart - " + ioe.toString());
			}
		} finally {
			_Session.deleteDataBin(DATA_BIN_KEY);
		}

		return cfTagReturnType.NORMAL;
	}

	/*
	 * renderPieChart
	 */
	private JFreeChart renderPieChart(cfSession _Session, cfCHARTInternalData chartData, List<cfCHARTSERIESData> series, String tipStyle, String drillDownUrl) throws cfmRunTimeException {
		// Retrieve the attributes of the chart
		String backgroundColorStr = getDynamic(_Session, "BACKGROUNDCOLOR").toString();
		Color backgroundColor = convertStringToColor(backgroundColorStr);

		String foregroundColorStr = getDynamic(_Session, "FOREGROUNDCOLOR").toString();
		Color foregroundColor = convertStringToColor(foregroundColorStr);

		String labelFormat = getDynamic(_Session, "LABELFORMAT").toString().toLowerCase();
		if (!labelFormat.equals("number") && !labelFormat.equals("currency") && !labelFormat.equals("percent"))
			throw newRunTimeException("The labelFormat value '" + labelFormat + "' is not supported with pie charts");

		String pieSliceStyle = getDynamic(_Session, "PIESLICESTYLE").toString().toLowerCase();

		String title = null;
		if (containsAttribute("TITLE"))
			title = getDynamic(_Session, "TITLE").toString();

		Font font = getFont(_Session);

		cfCHARTSERIESData seriesData = series.get(0);

		boolean bShow3D = getDynamic(_Session, "SHOW3D").getBoolean();
		if (bShow3D && !seriesData.getType().equals("pie"))
			throw newRunTimeException("Only bar, line, horizontal bar and pie charts can be displayed in 3D");

		boolean bShowBorder = getDynamic(_Session, "SHOWBORDER").getBoolean();

		boolean bShowLegend = true; // default to true for pie charts
		if (containsAttribute("SHOWLEGEND"))
			bShowLegend = getDynamic(_Session, "SHOWLEGEND").getBoolean();

		// Get the plot for the chart and configure it
		PiePlot plot = getPiePlot(series, pieSliceStyle, bShow3D);
		setBackgroundImage(_Session, plot, chartData.getImageData());
		plot.setBackgroundPaint(backgroundColor);
		plot.setLabelBackgroundPaint(backgroundColor);
		plot.setLabelShadowPaint(backgroundColor);

		// Set the labels color
		plot.setLabelPaint(convertStringToColor(seriesData.getDataLabelColor()));

		// Set the labels font
		plot.setLabelFont(getFont(seriesData.getDataLabelFont(), seriesData.getDataLabelFontBold(), seriesData.getDataLabelFontItalic(), seriesData.getDataLabelFontSize()));

		String dataLabelStyle = seriesData.getDataLabelStyle();

		NumberFormat percentInstance = NumberFormat.getPercentInstance();
		percentInstance.setMaximumFractionDigits(3);

		NumberFormat numberFormat = null;
		if (labelFormat.equals("number")) {
			// Only display the value in the Label as a number
			numberFormat = NumberFormat.getInstance();
			if (dataLabelStyle.equals("none"))
				plot.setLabelGenerator(null);
			else if (dataLabelStyle.equals("value"))
				plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator("{1}"));
			else if (dataLabelStyle.equals("rowlabel"))
				plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(seriesData.getSeriesLabel()));
			else if (dataLabelStyle.equals("columnlabel"))
				plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator("{0}"));
			else if (dataLabelStyle.equals("pattern"))
				plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator("{0} {1} ({2} of {3})"));
			else {
				String pattern = java.text.MessageFormat.format(dataLabelStyle, new Object[] { seriesData.getSeriesLabel(), "{0}", "{1}", "{2}", "{3}" });
				plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(pattern));
			}
		} else if (labelFormat.equals("currency")) {
			// Only display the value in the Label as a currency
			numberFormat = NumberFormat.getCurrencyInstance();
			if (dataLabelStyle.equals("none"))
				plot.setLabelGenerator(null);
			else if (dataLabelStyle.equals("value"))
				plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator("{1}", NumberFormat.getCurrencyInstance(), percentInstance));
			else if (dataLabelStyle.equals("rowlabel"))
				plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(seriesData.getSeriesLabel()));
			else if (dataLabelStyle.equals("columnlabel"))
				plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator("{0}"));
			else if (dataLabelStyle.equals("pattern"))
				plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator("{0} {1} ({2} of {3})", NumberFormat.getCurrencyInstance(), percentInstance));
			else {
				String pattern = java.text.MessageFormat.format(dataLabelStyle, new Object[] { seriesData.getSeriesLabel(), "{0}", "{1}", "{2}", "{3}" });
				plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(pattern, NumberFormat.getCurrencyInstance(), percentInstance));
			}
		} else if (labelFormat.equals("percent")) {
			// Only display the value in the Label as a percent
			numberFormat = percentInstance;
			if (dataLabelStyle.equals("none"))
				plot.setLabelGenerator(null);
			else if (dataLabelStyle.equals("value"))
				plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator("{1}", percentInstance, percentInstance));
			else if (dataLabelStyle.equals("rowlabel"))
				plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(seriesData.getSeriesLabel()));
			else if (dataLabelStyle.equals("columnlabel"))
				plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator("{0}"));
			else if (dataLabelStyle.equals("pattern"))
				plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator("{0} {1} ({2} of {3})", percentInstance, percentInstance));
			else {
				String pattern = java.text.MessageFormat.format(dataLabelStyle, new Object[] { seriesData.getSeriesLabel(), "{0}", "{1}", "{2}", "{3}" });
				plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(pattern, percentInstance, percentInstance));
			}
		}

		if (!tipStyle.equals("none")) {
			plot.setToolTipGenerator(new StandardPieToolTipGenerator(StandardPieToolTipGenerator.DEFAULT_SECTION_LABEL_FORMAT, numberFormat, numberFormat));
		}

		if (drillDownUrl != null) {
			plot.setURLGenerator(new com.newatlanta.bluedragon.PieURLGenerator(drillDownUrl, numberFormat));
		}

		// Get the chart and configure it
		JFreeChart chart = new JFreeChart(null, null, plot, false);
		chart.setBorderVisible(bShowBorder);
		chart.setBackgroundPaint(backgroundColor);
		setTitle(chart, title, font, foregroundColor, chartData.getTitles());
		setLegend(chart, bShowLegend, font, foregroundColor, backgroundColor, chartData.getLegendData());

		return chart;
	}

	private void setBackgroundImage(cfSession _Session, Plot plot, cfCHARTIMAGEData imgData) throws cfmRunTimeException {
		// If a background image wasn't specified then just return
		if (imgData == null)
			return;

		// Determine the location of the background image
		String file = imgData.getFile();
		if (imgData.isUriDirectory()) {
			file = FileUtils.getRealPath(_Session.REQ, file);
		}

		// Determine the type of the background image
		byte imgType;
		String lowerCaseFile = file.toLowerCase();
		if (lowerCaseFile.endsWith(".gif")) {
			imgType = imageOps.GIF;
		} else if (lowerCaseFile.endsWith(".jpg") || lowerCaseFile.endsWith(".jpeg")) {
			imgType = imageOps.JPG;
		} else {
			throw newRunTimeException("The background image must be a GIF or JPEG.");
		}

		// Load and set the background image
		plot.setBackgroundImage(com.nary.awt.image.imageOps.loadImage(file, imgType));

		// Set the alignment of the background image
		String alignment = imgData.getAlignment();
		if (alignment.equals("top_left"))
			plot.setBackgroundImageAlignment(org.jfree.ui.Align.TOP_LEFT);
		else if (alignment.equals("top"))
			plot.setBackgroundImageAlignment(org.jfree.ui.Align.TOP);
		else if (alignment.equals("top_right"))
			plot.setBackgroundImageAlignment(org.jfree.ui.Align.TOP_RIGHT);
		else if (alignment.equals("left"))
			plot.setBackgroundImageAlignment(org.jfree.ui.Align.LEFT);
		else if (alignment.equals("center"))
			plot.setBackgroundImageAlignment(org.jfree.ui.Align.CENTER);
		else if (alignment.equals("right"))
			plot.setBackgroundImageAlignment(org.jfree.ui.Align.RIGHT);
		else if (alignment.equals("bottom_left"))
			plot.setBackgroundImageAlignment(org.jfree.ui.Align.BOTTOM_LEFT);
		else if (alignment.equals("bottom"))
			plot.setBackgroundImageAlignment(org.jfree.ui.Align.BOTTOM);
		else if (alignment.equals("bottom_right"))
			plot.setBackgroundImageAlignment(org.jfree.ui.Align.BOTTOM_RIGHT);
		else if (alignment.equals("fit_horizontal"))
			plot.setBackgroundImageAlignment(org.jfree.ui.Align.FIT_HORIZONTAL);
		else if (alignment.equals("fit_vertical"))
			plot.setBackgroundImageAlignment(org.jfree.ui.Align.FIT_VERTICAL);
		else
			plot.setBackgroundImageAlignment(org.jfree.ui.Align.FIT);
	}

	private void setTitle(JFreeChart chart, String title, Font titleFont, Color foregroundColor, List<cfCHARTTITLEData> titles) throws cfmRunTimeException {
		if (titles.size() > 0) {
			// One or more CFCHARTTITLE tags were specified so use them to configure
			// the chart titles
			for (int i = 0; i < titles.size(); i++) {
				cfCHARTTITLEData data = titles.get(i);
				Font font = getFont(data.getFont(), data.getFontBold(), data.getFontItalic(), data.getFontSize());
				TextTitle textTitle = new TextTitle(data.getTitle(), font);
				textTitle.setPaint(convertStringToColor(data.getLabelColor()));
				textTitle.setBackgroundPaint(convertStringToColor(data.getBackgroundColor()));

				String pos = data.getPosition();
				if (pos.equals("top"))
					textTitle.setPosition(RectangleEdge.TOP);
				else if (pos.equals("bottom"))
					textTitle.setPosition(RectangleEdge.BOTTOM);
				else if (pos.equals("left"))
					textTitle.setPosition(RectangleEdge.LEFT);
				else if (pos.equals("right"))
					textTitle.setPosition(RectangleEdge.RIGHT);

				if (!data.getShowBorder())
					textTitle.setBorder(BlockBorder.NONE);
				else
					textTitle.setBorder(new BlockBorder());
				textTitle.setPadding(data.getPadding(), data.getPadding(), data.getPadding(), data.getPadding());
				textTitle.setMargin(data.getMargin(), data.getMargin(), data.getMargin(), data.getMargin());

				chart.addSubtitle(textTitle);
			}
		} else {
			// A CFCHARTTITLE tag was NOT specified so use the CFCHART attributes to
			// configure the chart title
			if (title == null)
				return;

			TextTitle textTitle = new TextTitle(title, titleFont);
			textTitle.setPaint(foregroundColor);

			// Add a border around the title to match CFMX 7
			textTitle.setBorder(new BlockBorder());
			textTitle.setPadding(10, 10, 10, 10);
			textTitle.setMargin(5, 5, 5, 5);

			chart.setTitle(textTitle);
		}
	}

	private void setLegend(JFreeChart chart, boolean bShowLegend, Font font, Color foregroundColor, Color backgroundColor, cfCHARTLEGENDData legendData) throws cfmRunTimeException {
		LegendTitle legend = new LegendTitle(chart.getPlot());
		legend.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));

		// If a CFCHARTLEGEND tag was used then use it's attributes to configure the
		// legend
		if (legendData != null) {
			// A CFCHARTLEGEND tag is present so use its attributes to configure the
			// legend
			legend.setItemFont(getFont(legendData.getFont(), legendData.getFontBold(), legendData.getFontItalic(), legendData.getFontSize()));
			legend.setItemPaint(convertStringToColor(legendData.getLabelColor()));
			legend.setBackgroundPaint(convertStringToColor(legendData.getBackgroundColor()));

			String pos = legendData.getPosition();
			if (pos.equals("top"))
				legend.setPosition(RectangleEdge.TOP);
			else if (pos.equals("bottom"))
				legend.setPosition(RectangleEdge.BOTTOM);
			else if (pos.equals("left"))
				legend.setPosition(RectangleEdge.LEFT);
			else if (pos.equals("right"))
				legend.setPosition(RectangleEdge.RIGHT);

			if (!legendData.getShowBorder())
				legend.setBorder(BlockBorder.NONE);
			else
				legend.setBorder(new BlockBorder());
		} else {
			// A CFCHARTLEGEND tag is NOT present so use the attributes from the
			// CFCHART tag to configure the legend
			if (!bShowLegend)
				return;

			legend.setItemFont(font);
			legend.setItemPaint(foregroundColor);
			legend.setBackgroundPaint(backgroundColor);

			// By default CFMX 7 places the legend at the top with no border
			legend.setPosition(RectangleEdge.TOP);
			legend.setBorder(BlockBorder.NONE);
		}

		// Add the legend to the chart
		chart.addSubtitle(legend);
	}

	/*
	 * getPiePlot
	 */
	private PiePlot getPiePlot(List<cfCHARTSERIESData> series, String pieSliceStyle, boolean bShow3D) throws cfmBadFileException {
		org.jfree.data.general.DefaultPieDataset dataset = new org.jfree.data.general.DefaultPieDataset();
		cfCHARTSERIESData seriesData = series.get(0);
		int num = seriesData.getNumItems();
		for (int j = 0; j < num; j++) {
			dataset.setValue(seriesData.getItemName(j), seriesData.getItemValue(j));
		}

		// Create a pie plot
		PiePlot plot;
		if (bShow3D) {
			plot = new PiePlot3D(dataset);
			((PiePlot3D) plot).setDepthFactor(0.1);
		} else {
			if (seriesData.getType().equals("pie")) {
				// It's a 2D pie chart
				plot = new PiePlot(dataset);
			} else {
				// It's a 2D ring chart
				plot = new org.jfree.chart.plot.RingPlot(dataset);
				((org.jfree.chart.plot.RingPlot) plot).setSeparatorsVisible(false);
			}
		}

		// If a colorList attribute was specified on the cfchartseries tag
		// then set the section paint for each slice of the pie.
		List<String> colorList = seriesData.getColorList();
		if (colorList != null) {
			for (int i = 0; i < colorList.size(); i++) {
				plot.setSectionPaint(i, convertStringToColor(colorList.get(i)));
			}
		}

		// Don't display a shadow
		plot.setShadowPaint(null);

		// Only display the name in the Legend
		plot.setLegendLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator("{0}"));

		// Don't draw an outline around the chart
		plot.setOutlinePaint(null);

		if (pieSliceStyle.equals("sliced")) {
			for (int j = 0; j < num; j++)
				plot.setExplodePercent(j, .1);
		}

		return plot;
	}

	/*
	 * renderCategoryChart
	 */
	private JFreeChart renderCategoryChart(cfSession _Session, cfCHARTInternalData chartData, String tipStyle, String drillDownUrl, String seriesPlacement, int height) throws cfmRunTimeException {
		List<cfCHARTSERIESData> series = chartData.getSeries();

		// Retrieve the attributes of the chart
		String backgroundColorStr = getDynamic(_Session, "BACKGROUNDCOLOR").toString();
		Color backgroundColor = convertStringToColor(backgroundColorStr);

		String dataBackgroundColorStr = getDynamic(_Session, "DATABACKGROUNDCOLOR").toString();
		Color dataBackgroundColor = convertStringToColor(dataBackgroundColorStr);

		String foregroundColorStr = getDynamic(_Session, "FOREGROUNDCOLOR").toString();
		Color foregroundColor = convertStringToColor(foregroundColorStr);

		String labelFormat = getDynamic(_Session, "LABELFORMAT").toString().toLowerCase();
		if (!labelFormat.equals("number") && !labelFormat.equals("currency") && !labelFormat.equals("percent") && !labelFormat.equals("date"))
			throw newRunTimeException("The labelFormat value '" + labelFormat + "' is not supported");

		String xAxisTitle = null;
		if (containsAttribute("XAXISTITLE"))
			xAxisTitle = getDynamic(_Session, "XAXISTITLE").toString();

		String yAxisTitle = null;
		if (containsAttribute("YAXISTITLE"))
			yAxisTitle = getDynamic(_Session, "YAXISTITLE").toString();

		String title = null;
		if (containsAttribute("TITLE"))
			title = getDynamic(_Session, "TITLE").toString();

		Font font = getFont(_Session);

		int yAxisUnits = 0;
		if (containsAttribute("YAXISUNITS")) {
			if (containsAttribute("GRIDLINES"))
				throw newRunTimeException("You cannot specify both yAxisUnits and gridLines");
			yAxisUnits = getDynamic(_Session, "YAXISUNITS").getInt();
			if (yAxisUnits < 0)
				throw newRunTimeException("You must specify a positive value for yAxisUnits");
		}
		int gridLines = -1;
		if (containsAttribute("GRIDLINES")) {
			gridLines = getDynamic(_Session, "GRIDLINES").getInt();
			if (gridLines < 2)
				throw newRunTimeException("You must specify a value greater than 1 for gridLines");
		}
		int markerSize = getDynamic(_Session, "MARKERSIZE").getInt();
		int xOffset = getDynamic(_Session, "XOFFSET").getInt();
		if ((xOffset < 0) || ((xOffset == 0) && (getDynamic(_Session, "XOFFSET").getDouble() != 0))) {
			// CFMX expects the xOffset value to be between -1 and 1 while BD expects
			// it to be
			// between 0 and the chartWidth. This exception should catch people who
			// are trying
			// to use this attribute with a CFMX value instead of a BD value.
			throw newRunTimeException("You must specify an integer value between 0 and the chartWidth for xOffset");
		}

		double xUpperMargin = getDynamic(_Session, "XAXISUPPERMARGIN").getDouble();
		if (xUpperMargin < 0)
			throw newRunTimeException("You must specify a positive value for xAxisUpperMargin");

		int yOffset = getDynamic(_Session, "YOFFSET").getInt();
		if ((yOffset < 0) || ((yOffset == 0) && (getDynamic(_Session, "YOFFSET").getDouble() != 0))) {
			// CFMX expects the xOffset value to be between -1 and 1 while BD expects
			// it to be
			// between 0 and the chartHeight. This exception should catch people who
			// are trying
			// to use this attribute with a CFMX value instead of a BD value.
			throw newRunTimeException("You must specify an integer value between 0 and the chartHeight for yOffset");
		}
		double yUpperMargin = getDynamic(_Session, "YAXISUPPERMARGIN").getDouble();
		if (yUpperMargin < 0)
			throw newRunTimeException("You must specify a positive value for yAxisUpperMargin");

		boolean bShow3D = getDynamic(_Session, "SHOW3D").getBoolean();
		boolean bShowMarkers = getDynamic(_Session, "SHOWMARKERS").getBoolean();
		boolean bShowBorder = getDynamic(_Session, "SHOWBORDER").getBoolean();
		boolean bShowXGridlines = getDynamic(_Session, "SHOWXGRIDLINES").getBoolean();
		boolean bShowYGridlines = getDynamic(_Session, "SHOWYGRIDLINES").getBoolean();
		boolean bSortXAxis = getDynamic(_Session, "SORTXAXIS").getBoolean();

		boolean bShowLegend = false; // default to false for category charts
		if (containsAttribute("SHOWLEGEND"))
			bShowLegend = getDynamic(_Session, "SHOWLEGEND").getBoolean();

		int scaleFrom = Integer.MIN_VALUE;
		if (containsAttribute("SCALEFROM"))
			scaleFrom = getDynamic(_Session, "SCALEFROM").getInt();

		int scaleTo = Integer.MIN_VALUE;
		if (containsAttribute("SCALETO"))
			scaleTo = getDynamic(_Session, "SCALETO").getInt();

		String yAxisType = getDynamic(_Session, "YAXISTYPE").toString().toLowerCase();
		String[] yAxisSymbols = null;
		if (yAxisType.equals("symbols")) {
			if (!containsAttribute("YAXISSYMBOLS"))
				throw newRunTimeException("You must specify a value for yAxisSymbols when yAxisType is set to 'symbols'");

			String symbols = getDynamic(_Session, "YAXISSYMBOLS").toString();

			String symSep = getDynamic(_Session, "SYMBOLSSEPARATOR").toString();
			if (symSep.length() != 1)
				throw newRunTimeException("The symbolsSeparator value must be a single character.  The value '" + symSep + "' is not valid.");
			char symbolsSeparator = symSep.charAt(0);

			List<String> symbolList = com.nary.util.string.split(symbols, symbolsSeparator);
			Object[] objs = symbolList.toArray();
			yAxisSymbols = new String[objs.length];
			for (int x = 0; x < objs.length; x++)
				yAxisSymbols[x] = (String) objs[x];
		}

		// Get the plot for the chart and configure it
		CategoryPlot plot = getCategoryPlot(series, xAxisTitle, yAxisTitle, labelFormat, bShowMarkers, markerSize, bShow3D, tipStyle, drillDownUrl, xOffset, yOffset, yAxisUnits, seriesPlacement, bSortXAxis, height, yAxisSymbols, gridLines);
		// Render the datasets/series in the order they appear in the cfchart tag
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		plot.getDomainAxis().setMaximumCategoryLabelLines(getDynamic(_Session, "MAXCATEGORYLABELLINES").getInt());
		String catLblPos = getDynamic(_Session, "CATEGORYLABELPOSITIONS").getString().toLowerCase();
		if (catLblPos.equals("vertical"))
			plot.getDomainAxis().setCategoryLabelPositions(VERTICAL);
		if (containsAttribute("MAXCATEGORYLABELWIDTHRATIO"))
			plot.getDomainAxis().setMaximumCategoryLabelWidthRatio((float) getDynamic(_Session, "MAXCATEGORYLABELWIDTHRATIO").getDouble());
		plot.getDomainAxis().setLabelFont(font);
		plot.getDomainAxis().setTickLabelFont(font);
		plot.getDomainAxis().setAxisLinePaint(foregroundColor);
		plot.getDomainAxis().setLabelPaint(foregroundColor);
		plot.getDomainAxis().setTickLabelPaint(foregroundColor);
		plot.getDomainAxis().setUpperMargin(xUpperMargin);
		plot.setDomainGridlinesVisible(bShowXGridlines);
		plot.setRangeGridlinesVisible(bShowYGridlines);
		plot.getRangeAxis().setLabelFont(font);
		plot.getRangeAxis().setTickLabelFont(font);
		plot.getRangeAxis().setAxisLinePaint(foregroundColor);
		plot.getRangeAxis().setLabelPaint(foregroundColor);
		plot.getRangeAxis().setTickLabelPaint(foregroundColor);
		plot.getRangeAxis().setUpperMargin(yUpperMargin);
		if (scaleFrom != Integer.MIN_VALUE)
			plot.getRangeAxis().setLowerBound(scaleFrom);
		if (scaleTo != Integer.MIN_VALUE)
			plot.getRangeAxis().setUpperBound(scaleTo);
		plot.setBackgroundPaint(dataBackgroundColor);
		plot.setOutlinePaint(foregroundColor);
		setBackgroundImage(_Session, plot, chartData.getImageData());

		// Add Range Markers
		List<cfCHARTRANGEMARKERData> rangeMarkers = chartData.getRangeMarkers();
		for (int i = 0; i < rangeMarkers.size(); i++)
			addRangeMarker(plot, rangeMarkers.get(i));

		// Add Domain Markers
		List<cfCHARTDOMAINMARKERData> domainMarkers = chartData.getDomainMarkers();
		for (int i = 0; i < domainMarkers.size(); i++)
			addDomainMarker(plot, domainMarkers.get(i));

		// Get the chart and configure it
		JFreeChart chart = new JFreeChart(null, null, plot, false);
		chart.setBorderVisible(bShowBorder);
		chart.setBackgroundPaint(backgroundColor);
		setTitle(chart, title, font, foregroundColor, chartData.getTitles());
		setLegend(chart, bShowLegend, font, foregroundColor, backgroundColor, chartData.getLegendData());

		return chart;
	}

	/*
	 * getCategoryPlot
	 */
	private CategoryPlot getCategoryPlot(List<cfCHARTSERIESData> series, String xAxisTitle, String yAxisTitle, String labelFormat, boolean bShowMarkers, int markerSize, boolean bShow3D, String tipStyle, String drillDownUrl, int xOffset, int yOffset, int yAxisUnits, String seriesPlacement, boolean bSortXAxis, int height, String[] yAxisSymbols, int gridLines) throws cfmRunTimeException {
		// Create a category plot
		CategoryPlot plot = new CategoryPlot();

		// Set the domain axis (the x-axis)
		org.jfree.chart.axis.CategoryAxis categoryAxis;
		if (bShow3D)
			categoryAxis = new CategoryAxis3D(xAxisTitle);
		else
			categoryAxis = new CategoryAxis(xAxisTitle);
		plot.setDomainAxis(categoryAxis);

		// Set the range axis (the y-axis)
		ValueAxis valueAxis;
		DateFormat dateFormat = null;
		NumberFormat numberFormat = null;

		// Ignore a label format of date if the y-axis is using symbols
		if (labelFormat.equals("date") && (yAxisSymbols == null)) {
			valueAxis = new DateAxis(yAxisTitle);
			dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
			((DateAxis) valueAxis).setDateFormatOverride(dateFormat);
		} else {
			if (yAxisSymbols != null) {
				valueAxis = new SymbolAxis(yAxisTitle, yAxisSymbols);
				((SymbolAxis) valueAxis).setGridBandsVisible(false);
				((SymbolAxis) valueAxis).setAutoRangeStickyZero(true);
			} else if (bShow3D) {
				valueAxis = new NumberAxis3D(yAxisTitle);
			} else {
				valueAxis = new NumberAxis(yAxisTitle);
			}

			if (labelFormat.equals("currency")) {
				((NumberAxis) valueAxis).setNumberFormatOverride(NumberFormat.getCurrencyInstance());
				numberFormat = NumberFormat.getCurrencyInstance();
			} else if (labelFormat.equals("percent")) {
				numberFormat = NumberFormat.getPercentInstance();
				numberFormat.setMaximumFractionDigits(3); // without this change .11443
																									// would be displayed as 11%
																									// instead of 11.443%
				((NumberAxis) valueAxis).setNumberFormatOverride(numberFormat);
			} else {
				numberFormat = NumberFormat.getInstance();
			}

			if (yAxisUnits != 0)
				((NumberAxis) valueAxis).setTickUnit(new NumberTickUnit(yAxisUnits));
		}
		plot.setRangeAxis(valueAxis);

		// Add a dataset and renderer for each series
		int barChartDatasetIndex = -1;
		int hBarChartDatasetIndex = -1;
		int num = 0;
		MinMaxData minMax = new MinMaxData();
		for (int i = 0; i < series.size(); i++) {
			cfCHARTSERIESData seriesData = series.get(i);

			// If the sortXAxis attribute was set to "yes" then sort the data.
			// NOTE: this attribute is only used with category charts.
			if (bSortXAxis)
				seriesData.sort();

			DefaultCategoryDataset dataset;
			if ((barChartDatasetIndex != -1) && (seriesData.getType().equals("bar"))) {
				dataset = (DefaultCategoryDataset) plot.getDataset(barChartDatasetIndex);

				addSeriesDataToDataset(seriesData, dataset, minMax);

				// Set the paint style for this series
				setPaintStyle(seriesData.getPaintStyle(), plot.getRenderer(barChartDatasetIndex), dataset.getRowCount() - 1, height);

				// Add the color list for this series to the custom color renderer
				CustomColorRenderer cr = (CustomColorRenderer) plot.getRenderer(barChartDatasetIndex);
				cr.addColors(getColorList(seriesData));

				continue;
			} else if ((hBarChartDatasetIndex != -1) && (seriesData.getType().equals("horizontalbar"))) {
				dataset = (DefaultCategoryDataset) plot.getDataset(hBarChartDatasetIndex);

				addSeriesDataToDataset(seriesData, dataset, minMax);

				// Set the paint style for this series
				setPaintStyle(seriesData.getPaintStyle(), plot.getRenderer(hBarChartDatasetIndex), dataset.getRowCount() - 1, height);

				// Add the color list for this series to the custom color renderer
				CustomColorRenderer cr = (CustomColorRenderer) plot.getRenderer(hBarChartDatasetIndex);
				cr.addColors(getColorList(seriesData));

				continue;
			} else {
				dataset = new DefaultCategoryDataset();

				addSeriesDataToDataset(seriesData, dataset, minMax);
			}

			plot.setDataset(num, dataset);

			AbstractCategoryItemRenderer renderer = null;
			if (seriesData.getType().equals("bar")) {
				plot.setOrientation(PlotOrientation.VERTICAL);
				renderer = getBarRenderer(seriesPlacement, bShow3D, xOffset, yOffset, getColorList(seriesData));
				ItemLabelPosition position1 = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER);
				renderer.setPositiveItemLabelPosition(position1);
				ItemLabelPosition position2 = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER);
				renderer.setNegativeItemLabelPosition(position2);
				((BarRenderer) renderer).setItemMargin(0.0); // The margin between items
																											// in the same category
				categoryAxis.setCategoryMargin(0.2); // The margin between each category

				barChartDatasetIndex = num;
			} else if (seriesData.getType().equals("horizontalbar")) {
				plot.setOrientation(PlotOrientation.HORIZONTAL);
				plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
				renderer = getBarRenderer(seriesPlacement, bShow3D, xOffset, yOffset, getColorList(seriesData));
				if (bShow3D) {
					// change rendering order to ensure that bar overlapping is the
					// right way around
					plot.setRowRenderingOrder(org.jfree.util.SortOrder.DESCENDING);
					plot.setColumnRenderingOrder(org.jfree.util.SortOrder.DESCENDING);
				}
				ItemLabelPosition position1 = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE3, TextAnchor.CENTER_LEFT);
				renderer.setPositiveItemLabelPosition(position1);
				ItemLabelPosition position2 = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE9, TextAnchor.CENTER_RIGHT);
				renderer.setNegativeItemLabelPosition(position2);
				((BarRenderer) renderer).setItemMargin(0.0); // The margin between items
																											// in the same category
				categoryAxis.setCategoryMargin(0.2); // The margin between each category

				hBarChartDatasetIndex = num;
			} else if (seriesData.getType().equals("line")) {
				if (bShow3D) {
					renderer = new LineRenderer3D();
					((LineRenderer3D) renderer).setXOffset(xOffset);
					((LineRenderer3D) renderer).setYOffset(yOffset);
				} else {
					renderer = new LineAndShapeRenderer(true, false);
				}

				// Enable/Disable displaying of markers
				((LineAndShapeRenderer) renderer).setShapesVisible(bShowMarkers);

				// Set the shape of the markers based on the markerSize value
				((LineAndShapeRenderer) renderer).setShape(getMarker(seriesData.getMarkerStyle(), markerSize));
			} else if (seriesData.getType().equals("area")) {
				if (seriesPlacement.equals("stacked"))
					renderer = new StackedAreaRenderer(); // this doesn't work for some
																								// reason
				else
					renderer = new AreaRenderer();

				// Truncate the first and last values to match CFMX 7
				((AreaRenderer) renderer).setEndType(AreaRendererEndType.TRUNCATE);

				categoryAxis.setCategoryMargin(0.0);
			} else if (seriesData.getType().equals("step")) {
				renderer = new CategoryStepRenderer(true);
			} else if (seriesData.getType().equals("scatter")) {
				renderer = new LineAndShapeRenderer(false, true);

				// Set the shape of the markers based on the markerSize value
				((LineAndShapeRenderer) renderer).setShape(getMarker(seriesData.getMarkerStyle(), markerSize));
			}

			if (!tipStyle.equals("none")) {
				if (dateFormat != null)
					renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("({0}, {1}) = {2}", dateFormat));
				else
					renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("({0}, {1}) = {2}", numberFormat));
			}

			if (drillDownUrl != null) {
				if (dateFormat != null)
					renderer.setBaseItemURLGenerator(new com.newatlanta.bluedragon.CategoryURLGenerator(drillDownUrl, dateFormat));
				else
					renderer.setBaseItemURLGenerator(new com.newatlanta.bluedragon.CategoryURLGenerator(drillDownUrl, numberFormat));
			}

			if (seriesData.getSeriesColor() != null)
				renderer.setSeriesPaint(0, convertStringToColor(seriesData.getSeriesColor()));

			String dataLabelStyle = seriesData.getDataLabelStyle();
			if (labelFormat.equals("date")) {
				if (dataLabelStyle.equals("none")) {
					renderer.setItemLabelsVisible(false);
				} else {
					setCategoryItemLabelsData(renderer, seriesData);
					if (dataLabelStyle.equals("value"))
						renderer.setItemLabelGenerator(new CategoryItemLabelGenerator("{2}", dateFormat, yAxisSymbols));
					else if (dataLabelStyle.equals("rowlabel"))
						renderer.setItemLabelGenerator(new CategoryItemLabelGenerator("{0}", dateFormat, yAxisSymbols));
					else if (dataLabelStyle.equals("columnlabel"))
						renderer.setItemLabelGenerator(new CategoryItemLabelGenerator("{1}", dateFormat, yAxisSymbols));
					else if (dataLabelStyle.equals("pattern"))
						renderer.setItemLabelGenerator(new CategoryItemLabelGenerator("{1} {2}", dateFormat, yAxisSymbols));
					else
						renderer.setItemLabelGenerator(new CategoryItemLabelGenerator(dataLabelStyle, dateFormat, yAxisSymbols));
				}
			} else {
				if (dataLabelStyle.equals("none")) {
					renderer.setItemLabelsVisible(false);
				} else {
					setCategoryItemLabelsData(renderer, seriesData);
					if (dataLabelStyle.equals("value"))
						renderer.setItemLabelGenerator(new CategoryItemLabelGenerator("{2}", numberFormat, yAxisSymbols));
					else if (dataLabelStyle.equals("rowlabel"))
						renderer.setItemLabelGenerator(new CategoryItemLabelGenerator("{0}", numberFormat, yAxisSymbols));
					else if (dataLabelStyle.equals("columnlabel"))
						renderer.setItemLabelGenerator(new CategoryItemLabelGenerator("{1}", numberFormat, yAxisSymbols));
					else if (dataLabelStyle.equals("pattern"))
						renderer.setItemLabelGenerator(new CategoryItemLabelGenerator("{1} {2} ({3} of {4})", numberFormat, yAxisSymbols));
					else
						renderer.setItemLabelGenerator(new CategoryItemLabelGenerator(dataLabelStyle, numberFormat, yAxisSymbols));
				}
			}

			// Add the renderer to the plot.
			// NOTE: this must be done before the setPaintStyle() call so the
			// DrawingSupplier object
			// will be set up properly for the generation of default colors.
			plot.setRenderer(num, renderer);

			// Set the paint style for this series (series 0)
			if (seriesData.getType().equals("bar") || seriesData.getType().equals("horizontalbar") || seriesData.getType().equals("area"))
				setPaintStyle(seriesData.getPaintStyle(), renderer, 0, height);

			num++;
		}

		// If gridLines was specified then we need to calculate the yAxisUnits
		if ((gridLines != -1) && (valueAxis instanceof NumberAxis)) {
			// Calculate the yAxisUnits we need to use to create the number of
			// gridLines
			yAxisUnits = calculateYAxisUnits(gridLines, minMax);

			// Set the yAxisUnits
			((NumberAxis) valueAxis).setTickUnit(new NumberTickUnit(yAxisUnits));
		}

		return plot;
	}

	/*
	 * calculateYAxisUnits
	 */
	private int calculateYAxisUnits(int gridLines, MinMaxData minMax) {
		// Calculate the range for the values
		double range = minMax.maxValue - minMax.minValue;

		// Calculate the rawUnits we need to use to create the number of gridLines
		int rawUnits = (int) range / (gridLines - 1);

		// Convert the rawUnits to a roundedUnits of 10
		int roundedUnits = 1;
		while ((rawUnits / (roundedUnits * 10)) > 0) {
			roundedUnits *= 10;
		}
		int multiplier = rawUnits / roundedUnits;
		if (rawUnits % roundedUnits != 0)
			multiplier++;
		int yAxisUnits = multiplier * roundedUnits;

		// If there are positive and negative values then we need
		// to do special processing
		if ((minMax.maxValue > 0) && (minMax.minValue < 0)) {
			while ((yAxisUnits < range) && (numGridLines(yAxisUnits, minMax) > gridLines))
				yAxisUnits += roundedUnits;
		}

		return yAxisUnits;
	}

	/*
	 * numGridLines
	 */
	private int numGridLines(int yAxisUnits, MinMaxData minMax) {
		// Calculate the number of positive grid lines
		int numLines = (int) minMax.maxValue / yAxisUnits;
		if (minMax.maxValue % yAxisUnits != 0)
			numLines++;

		// Add one for the grid line at 0
		numLines++;

		// Calculate the number of negative grid lines
		numLines -= (int) minMax.minValue / yAxisUnits;
		if (minMax.minValue % yAxisUnits != 0)
			numLines++;

		return numLines;
	}

	/*
	 * renderXYChart
	 */
	private JFreeChart renderXYChart(cfSession _Session, cfCHARTInternalData chartData, String tipStyle, String drillDownUrl, String seriesPlacement, int height) throws cfmRunTimeException {
		List<cfCHARTSERIESData> series = chartData.getSeries();

		if (seriesPlacement.equals("stacked") || seriesPlacement.equals("percent"))
			throw newRunTimeException("A chart with an xAxisType of 'scale' cannot be displayed with a seriesPlacement of '" + seriesPlacement + "'");

		// Retrieve the attributes of the chart
		String backgroundColorStr = getDynamic(_Session, "BACKGROUNDCOLOR").toString();
		Color backgroundColor = convertStringToColor(backgroundColorStr);

		String dataBackgroundColorStr = getDynamic(_Session, "DATABACKGROUNDCOLOR").toString();
		Color dataBackgroundColor = convertStringToColor(dataBackgroundColorStr);

		String foregroundColorStr = getDynamic(_Session, "FOREGROUNDCOLOR").toString();
		Color foregroundColor = convertStringToColor(foregroundColorStr);

		String labelFormat = getDynamic(_Session, "LABELFORMAT").toString().toLowerCase();
		if (!labelFormat.equals("number") && !labelFormat.equals("currency") && !labelFormat.equals("percent") && !labelFormat.equals("date"))
			throw newRunTimeException("The labelFormat value '" + labelFormat + "' is not supported");

		String xAxisTitle = null;
		if (containsAttribute("XAXISTITLE"))
			xAxisTitle = getDynamic(_Session, "XAXISTITLE").toString();

		String yAxisTitle = null;
		if (containsAttribute("YAXISTITLE"))
			yAxisTitle = getDynamic(_Session, "YAXISTITLE").toString();

		String title = null;
		if (containsAttribute("TITLE"))
			title = getDynamic(_Session, "TITLE").toString();

		Font font = getFont(_Session);

		int yAxisUnits = 0;
		if (containsAttribute("YAXISUNITS")) {
			if (containsAttribute("GRIDLINES"))
				throw newRunTimeException("You cannot specify both yAxisUnits and gridLines");
			yAxisUnits = getDynamic(_Session, "YAXISUNITS").getInt();
			if (yAxisUnits < 0)
				throw newRunTimeException("You must specify a positive value for yAxisUnits");
		}
		int gridLines = -1;
		if (containsAttribute("GRIDLINES")) {
			gridLines = getDynamic(_Session, "GRIDLINES").getInt();
			if (gridLines < 2)
				throw newRunTimeException("You must specify a value greater than 1 for gridLines");
		}
		int markerSize = getDynamic(_Session, "MARKERSIZE").getInt();
		int xOffset = getDynamic(_Session, "XOFFSET").getInt();
		if (xOffset < 0)
			throw newRunTimeException("You must specify a positive value for xOffset");

		double xUpperMargin = getDynamic(_Session, "XAXISUPPERMARGIN").getDouble();
		if (xUpperMargin < 0)
			throw newRunTimeException("You must specify a positive value for xAxisUpperMargin");

		int yOffset = getDynamic(_Session, "YOFFSET").getInt();
		if (yOffset < 0)
			throw newRunTimeException("You must specify a positive value for yOffset");

		double yUpperMargin = getDynamic(_Session, "YAXISUPPERMARGIN").getDouble();
		if (yUpperMargin < 0)
			throw newRunTimeException("You must specify a positive value for yAxisUpperMargin");

		boolean bShow3D = getDynamic(_Session, "SHOW3D").getBoolean();
		if (bShow3D)
			throw newRunTimeException("A chart with an xAxisType of 'scale' cannot be displayed in 3D");
		boolean bShowMarkers = getDynamic(_Session, "SHOWMARKERS").getBoolean();
		boolean bShowBorder = getDynamic(_Session, "SHOWBORDER").getBoolean();
		boolean bShowXGridlines = getDynamic(_Session, "SHOWXGRIDLINES").getBoolean();
		boolean bShowYGridlines = getDynamic(_Session, "SHOWYGRIDLINES").getBoolean();

		boolean bShowLegend = false; // default to false for category charts
		if (containsAttribute("SHOWLEGEND"))
			bShowLegend = getDynamic(_Session, "SHOWLEGEND").getBoolean();

		int scaleFrom = Integer.MIN_VALUE;
		if (containsAttribute("SCALEFROM"))
			scaleFrom = getDynamic(_Session, "SCALEFROM").getInt();

		int scaleTo = Integer.MIN_VALUE;
		if (containsAttribute("SCALETO"))
			scaleTo = getDynamic(_Session, "SCALETO").getInt();

		// Get the plot for the chart and configure it
		XYPlot plot = getXYPlot(series, xAxisTitle, yAxisTitle, labelFormat, bShowMarkers, markerSize, bShow3D, tipStyle, drillDownUrl, xOffset, yOffset, yAxisUnits, seriesPlacement, height, gridLines);

		// Render the datasets/series in the order they appear in the cfchart tag
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		plot.getDomainAxis().setLabelFont(font);
		plot.getDomainAxis().setTickLabelFont(font);
		plot.getDomainAxis().setAxisLinePaint(foregroundColor);
		plot.getDomainAxis().setLabelPaint(foregroundColor);
		plot.getDomainAxis().setTickLabelPaint(foregroundColor);
		plot.getDomainAxis().setUpperMargin(xUpperMargin);
		plot.setDomainGridlinesVisible(bShowXGridlines);
		plot.setRangeGridlinesVisible(bShowYGridlines);
		plot.getRangeAxis().setLabelFont(font);
		plot.getRangeAxis().setTickLabelFont(font);
		plot.getRangeAxis().setAxisLinePaint(foregroundColor);
		plot.getRangeAxis().setLabelPaint(foregroundColor);
		plot.getRangeAxis().setTickLabelPaint(foregroundColor);
		plot.getRangeAxis().setUpperMargin(yUpperMargin);
		if (scaleFrom != Integer.MIN_VALUE)
			plot.getRangeAxis().setLowerBound(scaleFrom);
		if (scaleTo != Integer.MIN_VALUE)
			plot.getRangeAxis().setUpperBound(scaleTo);
		plot.setBackgroundPaint(dataBackgroundColor);
		plot.setOutlinePaint(foregroundColor);
		setBackgroundImage(_Session, plot, chartData.getImageData());

		// Add Range Markers
		List<cfCHARTRANGEMARKERData> rangeMarkers = chartData.getRangeMarkers();
		for (int i = 0; i < rangeMarkers.size(); i++)
			addRangeMarker(plot, rangeMarkers.get(i));

		// Add Domain Markers
		List<cfCHARTDOMAINMARKERData> domainMarkers = chartData.getDomainMarkers();
		for (int i = 0; i < domainMarkers.size(); i++)
			addDomainMarker(plot, domainMarkers.get(i));

		// Get the chart and configure it
		JFreeChart chart = new JFreeChart(null, null, plot, false);
		chart.setBorderVisible(bShowBorder);
		chart.setBackgroundPaint(backgroundColor);
		setTitle(chart, title, font, foregroundColor, chartData.getTitles());
		setLegend(chart, bShowLegend, font, foregroundColor, backgroundColor, chartData.getLegendData());

		return chart;
	}

	/*
	 * getXYPlot
	 */
	private XYPlot getXYPlot(List<cfCHARTSERIESData> series, String xAxisTitle, String yAxisTitle, String labelFormat, boolean bShowMarkers, int markerSize, boolean bShow3D, String tipStyle, String drillDownUrl, int xOffset, int yOffset, int yAxisUnits, String seriesPlacement, int height, int gridLines) throws cfmRunTimeException {
		// Create an XY plot
		XYPlot plot = new XYPlot();
		ValueAxis domainAxis;

		if (series.get(0).getSeriesDataType() == cfCHARTSERIESData.XY_NUMERIC_SERIES) {
			if (bShow3D)
				domainAxis = new NumberAxis3D(xAxisTitle);
			else
				domainAxis = new NumberAxis(xAxisTitle);
		} else {
			domainAxis = new DateAxis(xAxisTitle);
		}
		plot.setDomainAxis(domainAxis);

		ValueAxis valueAxis;
		DateFormat dateFormat = null;
		NumberFormat numberFormat = null;
		if (labelFormat.equals("date")) {
			valueAxis = new DateAxis(yAxisTitle);
			dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
			((DateAxis) valueAxis).setDateFormatOverride(dateFormat);
		} else {
			if (bShow3D)
				valueAxis = new NumberAxis3D(yAxisTitle);
			else
				valueAxis = new NumberAxis(yAxisTitle);
			if (labelFormat.equals("currency")) {
				((NumberAxis) valueAxis).setNumberFormatOverride(NumberFormat.getCurrencyInstance());
				numberFormat = NumberFormat.getCurrencyInstance();
			} else if (labelFormat.equals("percent")) {
				numberFormat = NumberFormat.getPercentInstance();
				numberFormat.setMaximumFractionDigits(3); // without this change .11443
																									// would be displayed as 11%
																									// instead of 11.443%
				((NumberAxis) valueAxis).setNumberFormatOverride(numberFormat);
			} else {
				numberFormat = NumberFormat.getInstance();
			}

			if (yAxisUnits != 0)
				((NumberAxis) valueAxis).setTickUnit(new NumberTickUnit(yAxisUnits));
		}
		plot.setRangeAxis(valueAxis);

		// Add a dataset and renderer for each series
		int barChartDatasetIndex = -1;
		int hBarChartDatasetIndex = -1;
		int num = 0;
		MinMaxData minMax = new MinMaxData();
		for (int i = 0; i < series.size(); i++) {
			cfCHARTSERIESData seriesData = series.get(i);
			XYSeriesCollection dataset;
			if ((barChartDatasetIndex != -1) && (seriesData.getType().equals("bar"))) {
				dataset = (XYSeriesCollection) plot.getDataset(barChartDatasetIndex);

				addSeriesDataToDataset(seriesData, dataset, minMax);

				// Set the paint style for this series
				setPaintStyle(seriesData.getPaintStyle(), plot.getRenderer(barChartDatasetIndex), dataset.getSeriesCount() - 1, height);

				// Add the color list for this series to the custom color renderer
				CustomColorRenderer cr = (CustomColorRenderer) plot.getRenderer(barChartDatasetIndex);
				cr.addColors(getColorList(seriesData));

				continue;
			} else if ((hBarChartDatasetIndex != -1) && (seriesData.getType().equals("horizontalbar"))) {
				dataset = (XYSeriesCollection) plot.getDataset(hBarChartDatasetIndex);

				addSeriesDataToDataset(seriesData, dataset, minMax);

				// Set the paint style for this series
				setPaintStyle(seriesData.getPaintStyle(), plot.getRenderer(hBarChartDatasetIndex), dataset.getSeriesCount() - 1, height);

				// Add the color list for this series to the custom color renderer
				CustomColorRenderer cr = (CustomColorRenderer) plot.getRenderer(hBarChartDatasetIndex);
				cr.addColors(getColorList(seriesData));

				continue;
			} else {
				dataset = new XYSeriesCollection();

				addSeriesDataToDataset(seriesData, dataset, minMax);
			}

			plot.setDataset(num, dataset);

			XYItemRenderer renderer = null;
			if (seriesData.getType().equals("bar")) {
				plot.setOrientation(PlotOrientation.VERTICAL);
				renderer = getXYBarRenderer(seriesPlacement, bShow3D, xOffset, yOffset, getColorList(seriesData));
				ItemLabelPosition position1 = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER);
				renderer.setPositiveItemLabelPosition(position1);
				ItemLabelPosition position2 = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER);
				renderer.setNegativeItemLabelPosition(position2);
				((XYBarRenderer) renderer).setMargin(0.2); // The margin between each
																										// category

				barChartDatasetIndex = num;
			} else if (seriesData.getType().equals("horizontalbar")) {
				plot.setOrientation(PlotOrientation.HORIZONTAL);
				plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
				renderer = getXYBarRenderer(seriesPlacement, bShow3D, xOffset, yOffset, getColorList(seriesData));
				ItemLabelPosition position1 = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE3, TextAnchor.CENTER_LEFT);
				renderer.setPositiveItemLabelPosition(position1);
				ItemLabelPosition position2 = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE9, TextAnchor.CENTER_RIGHT);
				renderer.setNegativeItemLabelPosition(position2);
				((XYBarRenderer) renderer).setMargin(0.2); // The margin between each
																										// category

				hBarChartDatasetIndex = num;
			} else if (seriesData.getType().equals("line")) {
				renderer = new XYLineAndShapeRenderer(true, false);

				// Enable/Disable displaying of markers
				((XYLineAndShapeRenderer) renderer).setShapesVisible(bShowMarkers);

				// Set the shape of the markers based on the markerSize value
				((XYLineAndShapeRenderer) renderer).setShape(getMarker(seriesData.getMarkerStyle(), markerSize));
			} else if (seriesData.getType().equals("area")) {
				renderer = new CustomXYAreaRenderer();
			} else if (seriesData.getType().equals("step")) {
				renderer = new CustomXYStepRenderer();
			} else if (seriesData.getType().equals("scatter")) {
				renderer = new XYLineAndShapeRenderer(false, true);

				// Set the shape of the markers based on the markerSize value
				((XYLineAndShapeRenderer) renderer).setShape(getMarker(seriesData.getMarkerStyle(), markerSize));
			}

			if (!tipStyle.equals("none")) {
				if (series.get(0).getSeriesDataType() == cfCHARTSERIESData.XY_DATE_SERIES) {
					renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: {1}", DateFormat.getInstance(), DateFormat.getInstance()));
				} else {
					if (dateFormat != null)
						renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: {2}", dateFormat, dateFormat));
					else
						renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: ({1}, {2})", numberFormat, NumberFormat.getInstance()));
				}
			}

			if (drillDownUrl != null) {
				if (dateFormat != null)
					renderer.setURLGenerator(new com.newatlanta.bluedragon.XYURLGenerator(drillDownUrl, dateFormat));
				else
					renderer.setURLGenerator(new com.newatlanta.bluedragon.XYURLGenerator(drillDownUrl, numberFormat));
			}

			if (seriesData.getSeriesColor() != null)
				renderer.setSeriesPaint(0, convertStringToColor(seriesData.getSeriesColor()));

			String dataLabelStyle = seriesData.getDataLabelStyle();
			if (labelFormat.equals("date")) {
				if (dataLabelStyle.equals("none")) {
					renderer.setItemLabelsVisible(false);
				} else {
					setXYItemLabelsData(renderer, seriesData);
					if (dataLabelStyle.equals("value"))
						renderer.setItemLabelGenerator(new StandardXYItemLabelGenerator("{2}", dateFormat, dateFormat));
					else if (dataLabelStyle.equals("rowlabel"))
						renderer.setItemLabelGenerator(new StandardXYItemLabelGenerator("{0}", NumberFormat.getInstance(), NumberFormat.getInstance()));
					else if (dataLabelStyle.equals("columnlabel"))
						renderer.setItemLabelGenerator(new StandardXYItemLabelGenerator("{1}", NumberFormat.getInstance(), NumberFormat.getInstance()));
					else if (dataLabelStyle.equals("pattern"))
						renderer.setItemLabelGenerator(new XYItemLabelGenerator("{2}", dateFormat, dateFormat));
					else
						renderer.setItemLabelGenerator(new XYItemLabelGenerator(dataLabelStyle, dateFormat, dateFormat));
				}
			} else {
				if (dataLabelStyle.equals("none")) {
					renderer.setItemLabelsVisible(false);
				} else {
					setXYItemLabelsData(renderer, seriesData);
					if (dataLabelStyle.equals("value")) {

						renderer.setItemLabelGenerator(new StandardXYItemLabelGenerator("{2}", numberFormat, numberFormat));

					} else if (dataLabelStyle.equals("rowlabel")) {

						renderer.setItemLabelGenerator(new StandardXYItemLabelGenerator("{0}", numberFormat, numberFormat));

					} else if (dataLabelStyle.equals("columnlabel")) {

						if (series.get(0).getSeriesDataType() == cfCHARTSERIESData.XY_DATE_SERIES) {
							renderer.setItemLabelGenerator(new StandardXYItemLabelGenerator("{1}", SimpleDateFormat.getInstance(), NumberFormat.getInstance()));
						} else {
							renderer.setItemLabelGenerator(new StandardXYItemLabelGenerator("{1}", NumberFormat.getInstance(), NumberFormat.getInstance()));
						}

					} else if (dataLabelStyle.equals("pattern")) {

						if (series.get(0).getSeriesDataType() == cfCHARTSERIESData.XY_DATE_SERIES) {
							renderer.setItemLabelGenerator(new XYItemLabelGenerator("{1} {2} ({3} of {4})", SimpleDateFormat.getInstance(), numberFormat));
						} else {
							renderer.setItemLabelGenerator(new XYItemLabelGenerator("{1} {2} ({3} of {4})", NumberFormat.getInstance(), numberFormat));
						}

					} else {
						renderer.setItemLabelGenerator(new XYItemLabelGenerator(dataLabelStyle, NumberFormat.getInstance(), numberFormat));
					}
				}
			}

			// Add the renderer to the plot.
			// NOTE: this must be done before the setPaintStyle() call so the
			// DrawingSupplier object
			// will be set up properly for the generation of default colors.
			plot.setRenderer(num, renderer);

			// Set the paint style for this series (series 0)
			if (seriesData.getType().equals("bar") || seriesData.getType().equals("horizontalbar") || seriesData.getType().equals("area"))
				setPaintStyle(seriesData.getPaintStyle(), renderer, 0, height);

			num++;
		}

		// If gridLines was specified then we need to calculate the yAxisUnits
		if ((gridLines != -1) && (valueAxis instanceof NumberAxis)) {
			// Calculate the yAxisUnits we need to use to create the number of
			// gridLines
			yAxisUnits = calculateYAxisUnits(gridLines, minMax);

			// Set the yAxisUnits
			((NumberAxis) valueAxis).setTickUnit(new NumberTickUnit(yAxisUnits));
		}

		return plot;
	}

	private Shape getMarker(String markerStyle, int markerSize) {
		double size = markerSize;
		double delta = markerSize / 2.0;
		double delta3rd = markerSize / 3.0;
		double delta4th = markerSize / 4.0;
		double delta5th = markerSize / 5.0;
		double delta6th = markerSize / 6.0;
		int[] xpoints = null;
		int[] ypoints = null;

		if (markerStyle.equals("triangle")) {
			xpoints = intArray(0.0, delta, -delta);
			ypoints = intArray(-delta, delta, delta);
			return new Polygon(xpoints, ypoints, 3);
		} else if (markerStyle.equals("triangledown")) {
			xpoints = intArray(-delta, +delta, 0.0);
			ypoints = intArray(-delta, -delta, delta);
			return new Polygon(xpoints, ypoints, 3);
		} else if (markerStyle.equals("triangleright")) {
			xpoints = intArray(-delta, delta, -delta);
			ypoints = intArray(-delta, 0.0, delta);
			return new Polygon(xpoints, ypoints, 3);
		} else if (markerStyle.equals("triangleleft")) {
			xpoints = intArray(-delta, delta, delta);
			ypoints = intArray(0.0, -delta, +delta);
			return new Polygon(xpoints, ypoints, 3);
		} else if (markerStyle.equals("diamond")) {
			xpoints = intArray(0.0, delta, 0.0, -delta);
			ypoints = intArray(-delta, 0.0, delta, 0.0);
			return new Polygon(xpoints, ypoints, 4);
		} else if (markerStyle.equals("circle")) {
			return new java.awt.geom.Ellipse2D.Double(-delta, -delta, size, size);
		} else if (markerStyle.equals("letterx")) {
			xpoints = intArray(0.0, delta3rd, delta, delta6th, delta, delta3rd, 0.0, -delta3rd, -delta, -delta6th, -delta, -delta3rd);
			ypoints = intArray(-delta6th, -delta, -delta3rd, 0.0, delta3rd, delta, delta6th, delta, delta3rd, 0.0, -delta3rd, -delta);
			return new Polygon(xpoints, ypoints, 12);
		} else if (markerStyle.equals("mcross")) {
			xpoints = intArray(0.0, -delta5th, delta5th, 0.0, delta, delta, 0.0, delta5th, -delta5th, 0.0, -delta, -delta);
			ypoints = intArray(0.0, -delta, -delta, 0.0, -delta5th, delta5th, 0.0, delta, delta, 0.0, delta5th, -delta5th);
			return new Polygon(xpoints, ypoints, 12);
		} else if (markerStyle.equals("snow")) {
			xpoints = intArray(0.0, delta4th, delta, 0.0, delta, delta4th, 0.0, -delta4th, -delta, 0.0, -delta, -delta4th);
			ypoints = intArray(0.0, -delta, -delta4th, 0.0, delta4th, delta, 0.0, delta, delta4th, 0.0, -delta4th, -delta);
			return new Polygon(xpoints, ypoints, 12);
		} else if (markerStyle.equals("rcross")) {
			xpoints = intArray(-delta4th, delta4th, delta4th, delta, delta, delta4th, delta4th, -delta4th, -delta4th, -delta, -delta, -delta4th);
			ypoints = intArray(-delta, -delta, -delta4th, -delta4th, delta4th, delta4th, delta, delta, delta4th, delta4th, -delta4th, -delta4th);
			return new Polygon(xpoints, ypoints, 12);
		} else if (markerStyle.equals("horizontalrectangle")) {
			return new java.awt.geom.Rectangle2D.Double(-delta, -delta / 2, size, size / 2);
		} else if (markerStyle.equals("verticalrectangle")) {
			return new java.awt.geom.Rectangle2D.Double(-delta / 2, -delta, size / 2, size);
		}

		// The default is a rectangle
		return new java.awt.geom.Rectangle2D.Double(-delta, -delta, size, size);
	}

	private static int[] intArray(double a, double b, double c) {
		return new int[] { (int) a, (int) b, (int) c };
	}

	private static int[] intArray(double a, double b, double c, double d) {
		return new int[] { (int) a, (int) b, (int) c, (int) d };
	}

	private static int[] intArray(double a, double b, double c, double d, double e, double f, double g, double h, double i, double j, double k, double l) {
		return new int[] { (int) a, (int) b, (int) c, (int) d, (int) e, (int) f, (int) g, (int) h, (int) i, (int) j, (int) k, (int) l };
	}

	/*
	 * getBarRenderer
	 * 
	 * This method will return a custom bar renderer that supports custom colors
	 * for each item in a series. The custom colors are specified using the
	 * colorList attribute of the cfchartseries tag.
	 */
	private AbstractCategoryItemRenderer getBarRenderer(String seriesPlacement, boolean bShow3D, int xOffset, int yOffset, Paint[] colors) {
		AbstractCategoryItemRenderer renderer;
		if (bShow3D) {
			if (seriesPlacement.equals("stacked"))
				renderer = new CustomStackedBarRenderer3D(colors, xOffset, yOffset);
			else
				renderer = new CustomBarRenderer3D(colors, xOffset, yOffset);
		} else {
			if (seriesPlacement.equals("stacked"))
				renderer = new CustomStackedBarRenderer(colors);
			else if (seriesPlacement.equals("percent"))
				renderer = new CustomStackedBarRenderer(colors, true);
			else
				renderer = new CustomBarRenderer(colors);
		}
		return renderer;
	}

	/*
	 * getBarRenderer
	 * 
	 * This method will return a custom bar renderer that supports custom colors
	 * for each item in a series. The custom colors are specified using the
	 * colorList attribute of the cfchartseries tag.
	 */
	private XYItemRenderer getXYBarRenderer(String seriesPlacement, boolean bShow3D, int xOffset, int yOffset, Paint[] colors) {
		XYItemRenderer renderer;

		renderer = new CustomClusteredXYBarRenderer(colors);

		return renderer;
	}

	/*
	 * getColorList
	 * 
	 * returns the series colorList as an array of Paint objects.
	 */
	private Paint[] getColorList(cfCHARTSERIESData seriesData) throws cfmBadFileException {
		Paint[] colors = null;
		List<String> colorList = seriesData.getColorList();
		if (colorList != null) {
			colors = new Paint[colorList.size()];
			for (int x = 0; x < colorList.size(); x++) {
				colors[x] = convertStringToColor(colorList.get(x));
			}
		} else {
			colors = new Paint[0];
		}

		return colors;
	}

	private void addSeriesDataToDataset(cfCHARTSERIESData seriesData, DefaultCategoryDataset dataset, MinMaxData minMax) {
		int num = seriesData.getNumItems();
		for (int j = 0; j < num; j++) {
			Double value = seriesData.getItemValue(j);

			dataset.addValue(value, seriesData.getSeriesLabel(), seriesData.getItemName(j));

			if (value.doubleValue() < minMax.minValue)
				minMax.minValue = value.doubleValue();
			if (value.doubleValue() > minMax.maxValue)
				minMax.maxValue = value.doubleValue();
		}
	}

	private void addSeriesDataToDataset(cfCHARTSERIESData seriesData, XYSeriesCollection dataset, MinMaxData minMax) {
		XYSeries series = new XYSeries(seriesData.getSeriesLabel());
		int num = seriesData.getNumItems();
		for (int j = 0; j < num; j++) {
			Double yValue = seriesData.getYValue(j);

			series.add(seriesData.getXValue(j), yValue);

			if (yValue.doubleValue() < minMax.minValue)
				minMax.minValue = yValue.doubleValue();
			if (yValue.doubleValue() > minMax.maxValue)
				minMax.maxValue = yValue.doubleValue();
		}
		dataset.addSeries(series);
	}

	private byte[] generateChart(JFreeChart chart, byte _format, int width, int height, ChartRenderingInfo info) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if (_format == FORMAT_JPG)
			ChartUtilities.writeChartAsJPEG(out, chart, width, height, info);
		else
			ChartUtilities.writeChartAsPNG(out, chart, width, height, info);
		out.close();

		return out.toByteArray();
	}

	private String generateChartUrl(cfSession _Session, String filename, String path ) {
		
		if ( path == null )
			path = "";
		else{
			if ( !path.endsWith("/") )
				path += "/";
		}

		switch (storage) {
		case FILE:
		case DB:
			return path + "cfchart.cfchart?" + filename;

		case SESSION:
			String appName = _Session.getApplicationData().getAppName();
			return path + "cfchart.cfchart?chartName=" + filename + "&appName=" + appName;

		default:
			throw new IllegalStateException("Charts can only be stored to file, a session or a database");
		}
	}

	private String saveChart(cfSession _Session, JFreeChart chart, byte format, int width, int height, ChartRenderingInfo info) throws IOException, cfmRunTimeException {
		switch (storage) {
		case FILE:
			return saveChartToFile(chart, format, width, height, info);
		case SESSION:
			return saveChartToSession(_Session, chart, format, width, height, info);
		case DB:
			return saveChartToDB(chart, format, width, height, info);
		default:
			throw new IllegalStateException("Charts can only be stored to file, a session or a database");
		}
	}

	private String saveChartToFile(JFreeChart chart, byte _format, int width, int height, ChartRenderingInfo info) throws IOException {
		File tempFile;
		if (_format == FORMAT_JPG) {
			tempFile = File.createTempFile("cfchart", ".jpeg", cfchartDirectory);
			ChartUtilities.saveChartAsJPEG(tempFile, chart, width, height, info);
		} else {
			tempFile = File.createTempFile("cfchart", ".png", cfchartDirectory);
			ChartUtilities.saveChartAsPNG(tempFile, chart, width, height, info);
		}
		String filename = tempFile.getName();

		// Check if charts are being cached
		if (storageCacheSize > 0) {
			synchronized (storageCache) {
				// If we've reached the cache limit then delete the oldest cached chart.
				if (storageCache.size() == storageCacheSize) {
					String oldestChart = storageCache.remove(0);
					File fileToDelete = new File(cfchartDirectory, oldestChart);
					fileToDelete.delete();
				}

				// Add the new chart to the end of the list of cached charts
				storageCache.add(filename);
			}
		}

		return filename;
	}

	private String saveChartToSession(cfSession _Session, JFreeChart chart, byte _format, int width, int height, ChartRenderingInfo info) throws IOException, cfmRunTimeException {
		// Come up with a session name for the chart that is unique for the session
		String name = "cfchart-" + com.nary.util.UUID.generateKey();

		// Generate the chart and save the contents in the session scope
		byte[] chartBytes = generateChart(chart, _format, width, height, info);
		cfData session = _Session.getQualifiedData(variableStore.SESSION_SCOPE);
		if (session instanceof com.naryx.tagfusion.cfm.engine.cfDisabledStructData)
			throw newRunTimeException("CFCHART is configured to store charts in the SESSION scope but the SESSION scope is disabled.");

		session.setData(name, new cfBinaryData(chartBytes));

		// Check if charts are being cached
		// NOTE: with session storage each session will have its own cache of
		// charts.
		if (storageCacheSize > 0) {
			cfArrayData sessionStorageCache = (cfArrayData) session.getData("cfchart-cache");
			if (sessionStorageCache == null)
				sessionStorageCache = cfArrayData.createArray(1);

			// If we've reached the cache limit then delete the oldest cached chart.
			if (sessionStorageCache.size() == storageCacheSize) {
				cfStringData oldestChart = (cfStringData) sessionStorageCache.getElement(1);
				sessionStorageCache.removeElementAt(1);
				session.deleteData(oldestChart.toString());
			}

			// Add the new chart to the end of the list of cached charts
			sessionStorageCache.addElement(new cfStringData(name));

			session.setData("cfchart-cache", sessionStorageCache);
		}

		return name;
	}

	private String saveChartToDB(JFreeChart chart, byte format, int width, int height, ChartRenderingInfo info) throws IOException, cfmRunTimeException {
		// Come up with a session name for the chart that is unique for the DB
		String name = "cfchart-" + com.nary.util.UUID.generateKey();

		// Generate the chart and save the contents in the name variable
		byte[] chartBytes = generateChart(chart, format, width, height, info);

		Connection con = null;
		java.sql.PreparedStatement Statmt = null;
		try {
			con = storageDataSource.getConnection();
			Statmt = con.prepareStatement(SQL_INSERT);
			Statmt.setString(1, name);
			Statmt.setBytes(2, chartBytes);
			Statmt.executeUpdate();
		} catch (SQLException E) {
			cfEngine.log("Error inserting data into BDCHARTDATA table: " + E);
			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_APPLICATION);
			catchData.setDetail("Database error: " + E.getMessage());
			catchData.setMessage("Error occurred when attempting to insert data into the BDCHARTDATA table");
			throw new cfmRunTimeException(catchData);
		} finally {
			if (Statmt != null)
				try {
					Statmt.close();
				} catch (SQLException e) {
				}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
		}

		// Check if charts are being cached
		if (storageCacheSize > 0) {
			synchronized (storageCache) {
				// If we've reached the cache limit then delete the oldest cached chart.
				if (storageCache.size() == storageCacheSize) {
					String oldestChart = storageCache.remove(0);
					con = null;
					Statmt = null;
					try {
						con = cfCHART.getStorageDataSource().getConnection();
						Statmt = con.prepareStatement(cfCHART.SQL_DELETE);
						Statmt.setString(1, oldestChart);
						Statmt.executeUpdate();
					} catch (SQLException E) {
						cfEngine.log("Error deleting data from BDCHARTDATA table: " + E);
					} finally {
						if (Statmt != null)
							try {
								Statmt.close();
							} catch (SQLException e) {
							}
						if (con != null)
							try {
								con.close();
							} catch (SQLException ignore) {
							}
					}
				}

				// Add the new chart to the end of the list of cached charts
				storageCache.add(name);
			}
		}

		return name;
	}

	private static void clearChartStorage() {
		switch (storage) {
		case FILE:
			clearFileStorage();
			break;
		case SESSION:
			clearSessionStorage();
			break;
		case DB:
			clearDBStorage();
			break;
		}
	}

	private static void clearFileStorage() {
		File[] charts = cfchartDirectory.listFiles();
		if (charts != null) {
			for (int i = 0; i < charts.length; i++)
				charts[i].delete();
		}
	}

	private static void clearSessionStorage() {
		// We can't clear session storage at init time because we don't have access
		// to sessions.
		// This means that sessions that survive a restart will contain old charts
		// in them.
		// This is OK though since eventually the session will timeout freeing up
		// the memory.
	}

	private static void clearDBStorage() {
		// We can't clear DB storage because multiple instances of BD may all be
		// pointing to
		// the same database.
	}

	/*
	 * setPaintStyle
	 * 
	 * This method sets the paint style for a category chart.
	 * 
	 * Note that CFMX does not have consistent behavior for this attribute. For
	 * example, with a bar chart the style for all items will be based on the top
	 * and bottom of the plot area while with a horizontal bar chart the style for
	 * an item will be based on the top and bottom of that item.
	 * 
	 * Unfortunately with JFreeChart we get inconsistent behavior too. With 2D
	 * category bar and horizontal bar charts the style for an item will be based
	 * on the top and bottom of that item. With all other charts (including 3D bar
	 * and horizontal bar charts and scale bar and horizontal bar charts) we are
	 * unable to determine the plot area so we have to set the style for all items
	 * based on the top and bottom of the chart. This is done by setting the start
	 * point as (0,0) and the end point as (0,height) where height is the height
	 * of the chart.
	 */
	private void setPaintStyle(String paintStyle, CategoryItemRenderer renderer, int seriesNum, int height) {
		if (paintStyle.equals("shade")) {
			Paint paint = renderer.getSeriesPaint(seriesNum);
			if (paint instanceof java.awt.Color) {
				java.awt.Color orig = (java.awt.Color) paint;
				java.awt.Color darker = getDarkerColor(orig);
				renderer.setSeriesPaint(seriesNum, new java.awt.GradientPaint(0, 0, darker, 0, height, orig));
			}
		} else if (paintStyle.equals("light")) {
			Paint paint = renderer.getSeriesPaint(seriesNum);
			if (paint instanceof java.awt.Color) {
				java.awt.Color orig = (java.awt.Color) paint;
				java.awt.Color lighter = getLighterColor(orig);
				renderer.setSeriesPaint(seriesNum, new java.awt.GradientPaint(0, 0, orig, 0, height, lighter));
			}
		}

		// With CFMX 7.0, the raise value appears to behave the same as the plain
		// value.
	}

	/*
	 * setPaintStyle
	 * 
	 * This method sets the paint style for a scale(xy) chart. Refer to the
	 * comments of the above method for more details.
	 */
	private void setPaintStyle(String paintStyle, XYItemRenderer renderer, int seriesNum, int height) {
		if (paintStyle.equals("shade")) {
			Paint paint = renderer.getSeriesPaint(seriesNum);
			if (paint instanceof java.awt.Color) {
				java.awt.Color orig = (java.awt.Color) paint;
				java.awt.Color darker = getDarkerColor(orig);
				renderer.setSeriesPaint(seriesNum, new java.awt.GradientPaint(0, 0, darker, 0, height, orig));
			}
		} else if (paintStyle.equals("light")) {
			Paint paint = renderer.getSeriesPaint(seriesNum);
			if (paint instanceof java.awt.Color) {
				java.awt.Color orig = (java.awt.Color) paint;
				java.awt.Color lighter = getLighterColor(orig);
				renderer.setSeriesPaint(seriesNum, new java.awt.GradientPaint(0, 0, orig, 0, height, lighter));
			}
		}

		// With CFMX 7.0, the raise value appears to behave the same as the plain
		// value.
	}

	public static java.awt.Color getDarkerColor(java.awt.Color orig) {
		int darkerRed = (int) (orig.getRed() * DARKER_FACTOR);
		int darkerGreen = (int) (orig.getGreen() * DARKER_FACTOR);
		int darkerBlue = (int) (orig.getBlue() * DARKER_FACTOR);
		return new java.awt.Color(darkerRed, darkerGreen, darkerBlue);
	}

	public static java.awt.Color getLighterColor(java.awt.Color orig) {
		int lighterRed = 255 - (int) ((255 - orig.getRed()) * LIGHTER_FACTOR);
		int lighterGreen = 255 - (int) ((255 - orig.getGreen()) * LIGHTER_FACTOR);
		int lighterBlue = 255 - (int) ((255 - orig.getBlue()) * LIGHTER_FACTOR);
		return new java.awt.Color(lighterRed, lighterGreen, lighterBlue);
	}

	private void setCategoryItemLabelsData(AbstractCategoryItemRenderer renderer, cfCHARTSERIESData seriesData) throws cfmRunTimeException {
		// Set them as visible
		renderer.setItemLabelsVisible(true);

		// Set their color
		renderer.setItemLabelPaint(convertStringToColor(seriesData.getDataLabelColor()));

		// Set their font
		renderer.setItemLabelFont(getFont(seriesData.getDataLabelFont(), seriesData.getDataLabelFontBold(), seriesData.getDataLabelFontItalic(), seriesData.getDataLabelFontSize()));

		// Set the item label position for negative data values
		double degrees = seriesData.getDataLabelAngle();
		double radians = Math.toRadians(degrees);
		String negPosition = seriesData.getNegativeDataLabelPosition();
		if (negPosition.equals("top"))
			renderer.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER, TextAnchor.CENTER, radians));
		else if (negPosition.equals("top_inside"))
			renderer.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE12, TextAnchor.TOP_CENTER, TextAnchor.CENTER, radians));
		else if (negPosition.equals("left"))
			renderer.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE9, TextAnchor.CENTER_RIGHT, TextAnchor.CENTER, radians));
		else if (negPosition.equals("left_inside"))
			renderer.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE9, TextAnchor.CENTER_LEFT, TextAnchor.CENTER, radians));
		else if (negPosition.equals("center"))
			renderer.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, radians));
		else if (negPosition.equals("right"))
			renderer.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE3, TextAnchor.CENTER_LEFT, TextAnchor.CENTER, radians));
		else if (negPosition.equals("right_inside"))
			renderer.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE3, TextAnchor.CENTER_RIGHT, TextAnchor.CENTER, radians));
		else if (negPosition.equals("bottom_inside"))
			renderer.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE6, TextAnchor.BOTTOM_CENTER, TextAnchor.CENTER, radians));
		else if (negPosition.equals("bottom"))
			renderer.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER, TextAnchor.CENTER, radians));

		// Set the item label position for positive data values
		String posPosition = seriesData.getPositiveDataLabelPosition();
		if (posPosition.equals("top"))
			renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER, TextAnchor.CENTER, radians));
		else if (posPosition.equals("top_inside"))
			renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE12, TextAnchor.TOP_CENTER, TextAnchor.CENTER, radians));
		else if (posPosition.equals("left"))
			renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE9, TextAnchor.CENTER_RIGHT, TextAnchor.CENTER, radians));
		else if (posPosition.equals("left_inside"))
			renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE9, TextAnchor.CENTER_LEFT, TextAnchor.CENTER, radians));
		else if (posPosition.equals("center"))
			renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, radians));
		else if (posPosition.equals("right"))
			renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE3, TextAnchor.CENTER_LEFT, TextAnchor.CENTER, radians));
		else if (posPosition.equals("right_inside"))
			renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE3, TextAnchor.CENTER_RIGHT, TextAnchor.CENTER, radians));
		else if (posPosition.equals("bottom_inside"))
			renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE6, TextAnchor.BOTTOM_CENTER, TextAnchor.CENTER, radians));
		else if (posPosition.equals("bottom"))
			renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER, TextAnchor.CENTER, radians));

		// With Bar graphs and an inside position, we want to force the item labels
		// to be drawn even if they don't fit inside the bar graph.
		if (renderer instanceof BarRenderer) {
			((BarRenderer) renderer).setNegativeItemLabelPositionFallback(renderer.getNegativeItemLabelPosition());
			((BarRenderer) renderer).setPositiveItemLabelPositionFallback(renderer.getPositiveItemLabelPosition());
		}
	}

	private void setXYItemLabelsData(XYItemRenderer renderer, cfCHARTSERIESData seriesData) throws cfmRunTimeException {
		// Set them as visible
		renderer.setItemLabelsVisible(true);

		// Set their color
		renderer.setItemLabelPaint(convertStringToColor(seriesData.getDataLabelColor()));

		// Set their font
		renderer.setItemLabelFont(getFont(seriesData.getDataLabelFont(), seriesData.getDataLabelFontBold(), seriesData.getDataLabelFontItalic(), seriesData.getDataLabelFontSize()));

		// Set the item label position for negative data values
		double degrees = seriesData.getDataLabelAngle();
		double radians = Math.toRadians(degrees);
		String negPosition = seriesData.getNegativeDataLabelPosition();
		if (negPosition.equals("top"))
			renderer.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER, TextAnchor.CENTER, radians));
		else if (negPosition.equals("top_inside"))
			renderer.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE12, TextAnchor.TOP_CENTER, TextAnchor.CENTER, radians));
		else if (negPosition.equals("left"))
			renderer.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE9, TextAnchor.CENTER_RIGHT, TextAnchor.CENTER, radians));
		else if (negPosition.equals("left_inside"))
			renderer.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE9, TextAnchor.CENTER_LEFT, TextAnchor.CENTER, radians));
		else if (negPosition.equals("center"))
			renderer.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, radians));
		else if (negPosition.equals("right"))
			renderer.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE3, TextAnchor.CENTER_LEFT, TextAnchor.CENTER, radians));
		else if (negPosition.equals("right_inside"))
			renderer.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE3, TextAnchor.CENTER_RIGHT, TextAnchor.CENTER, radians));
		else if (negPosition.equals("bottom_inside"))
			renderer.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE6, TextAnchor.BOTTOM_CENTER, TextAnchor.CENTER, radians));
		else if (negPosition.equals("bottom"))
			renderer.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER, TextAnchor.CENTER, radians));

		// Set the item label position for positive data values
		String posPosition = seriesData.getPositiveDataLabelPosition();
		if (posPosition.equals("top"))
			renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER, TextAnchor.CENTER, radians));
		else if (posPosition.equals("top_inside"))
			renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE12, TextAnchor.TOP_CENTER, TextAnchor.CENTER, radians));
		else if (posPosition.equals("left"))
			renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE9, TextAnchor.CENTER_RIGHT, TextAnchor.CENTER, radians));
		else if (posPosition.equals("left_inside"))
			renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE9, TextAnchor.CENTER_LEFT, TextAnchor.CENTER, radians));
		else if (posPosition.equals("center"))
			renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, radians));
		else if (posPosition.equals("right"))
			renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE3, TextAnchor.CENTER_LEFT, TextAnchor.CENTER, radians));
		else if (posPosition.equals("right_inside"))
			renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE3, TextAnchor.CENTER_RIGHT, TextAnchor.CENTER, radians));
		else if (posPosition.equals("bottom_inside"))
			renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE6, TextAnchor.BOTTOM_CENTER, TextAnchor.CENTER, radians));
		else if (posPosition.equals("bottom"))
			renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER, TextAnchor.CENTER, radians));
	}

	public void addRangeMarker(CategoryPlot plot, cfCHARTRANGEMARKERData rmData) throws cfmRunTimeException {
		IntervalMarker rangeMarker = new IntervalMarker(rmData.getStart(), rmData.getEnd());
		rangeMarker.setPaint(convertStringToColor(rmData.getColor()));
		if (rmData.getLabel() != null) {
			rangeMarker.setLabel(rmData.getLabel());
			rangeMarker.setLabelPaint(convertStringToColor(rmData.getLabelColor()));
			String labelPos = rmData.getLabelPosition();
			if (labelPos.equals("top_left")) {
				rangeMarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
				rangeMarker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
			} else if (labelPos.equals("top")) {
				rangeMarker.setLabelAnchor(RectangleAnchor.TOP);
				rangeMarker.setLabelTextAnchor(TextAnchor.TOP_CENTER);
			} else if (labelPos.equals("top_right")) {
				rangeMarker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
				rangeMarker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
			} else if (labelPos.equals("left")) {
				rangeMarker.setLabelAnchor(RectangleAnchor.LEFT);
				rangeMarker.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
			} else if (labelPos.equals("center")) {
				rangeMarker.setLabelAnchor(RectangleAnchor.CENTER);
				rangeMarker.setLabelTextAnchor(TextAnchor.CENTER);
			} else if (labelPos.equals("right")) {
				rangeMarker.setLabelAnchor(RectangleAnchor.RIGHT);
				rangeMarker.setLabelTextAnchor(TextAnchor.CENTER_RIGHT);
			} else if (labelPos.equals("bottom_left")) {
				rangeMarker.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
				rangeMarker.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
			} else if (labelPos.equals("bottom")) {
				rangeMarker.setLabelAnchor(RectangleAnchor.BOTTOM);
				rangeMarker.setLabelTextAnchor(TextAnchor.BOTTOM_CENTER);
			} else if (labelPos.equals("bottom_right")) {
				rangeMarker.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
				rangeMarker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
			}
			rangeMarker.setLabelOffsetType(LengthAdjustmentType.NO_CHANGE);
			rangeMarker.setLabelFont(getFont(rmData.getFont(), rmData.getFontBold(), rmData.getFontItalic(), rmData.getFontSize()));
		}
		plot.addRangeMarker(rangeMarker, Layer.BACKGROUND);
	}

	public void addRangeMarker(XYPlot plot, cfCHARTRANGEMARKERData rmData) throws cfmRunTimeException {
		IntervalMarker rangeMarker = new IntervalMarker(rmData.getStart(), rmData.getEnd());
		rangeMarker.setPaint(convertStringToColor(rmData.getColor()));
		if (rmData.getLabel() != null) {
			rangeMarker.setLabel(rmData.getLabel());
			rangeMarker.setLabelPaint(convertStringToColor(rmData.getLabelColor()));
			String labelPos = rmData.getLabelPosition();
			if (labelPos.equals("top_left")) {
				rangeMarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
				rangeMarker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
			} else if (labelPos.equals("top")) {
				rangeMarker.setLabelAnchor(RectangleAnchor.TOP);
				rangeMarker.setLabelTextAnchor(TextAnchor.TOP_CENTER);
			} else if (labelPos.equals("top_right")) {
				rangeMarker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
				rangeMarker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
			} else if (labelPos.equals("left")) {
				rangeMarker.setLabelAnchor(RectangleAnchor.LEFT);
				rangeMarker.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
			} else if (labelPos.equals("center")) {
				rangeMarker.setLabelAnchor(RectangleAnchor.CENTER);
				rangeMarker.setLabelTextAnchor(TextAnchor.CENTER);
			} else if (labelPos.equals("right")) {
				rangeMarker.setLabelAnchor(RectangleAnchor.RIGHT);
				rangeMarker.setLabelTextAnchor(TextAnchor.CENTER_RIGHT);
			} else if (labelPos.equals("bottom_left")) {
				rangeMarker.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
				rangeMarker.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
			} else if (labelPos.equals("bottom")) {
				rangeMarker.setLabelAnchor(RectangleAnchor.BOTTOM);
				rangeMarker.setLabelTextAnchor(TextAnchor.BOTTOM_CENTER);
			} else if (labelPos.equals("bottom_right")) {
				rangeMarker.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
				rangeMarker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
			}
			rangeMarker.setLabelOffsetType(LengthAdjustmentType.NO_CHANGE);
			rangeMarker.setLabelFont(getFont(rmData.getFont(), rmData.getFontBold(), rmData.getFontItalic(), rmData.getFontSize()));
		}
		plot.addRangeMarker(rangeMarker, org.jfree.ui.Layer.BACKGROUND);
	}

	public void addDomainMarker(CategoryPlot plot, cfCHARTDOMAINMARKERData dmData) throws cfmRunTimeException {
		CategoryMarker domainMarker = new CategoryMarker(dmData.getValue());
		boolean drawAsLine = false;
		if (dmData.getShape().equals("line"))
			drawAsLine = true;
		domainMarker.setDrawAsLine(drawAsLine);
		domainMarker.setPaint(convertStringToColor(dmData.getColor()));
		if (dmData.getLabel() != null) {
			domainMarker.setLabel(dmData.getLabel());
			domainMarker.setLabelPaint(convertStringToColor(dmData.getLabelColor()));
			String labelPos = dmData.getLabelPosition();
			if (labelPos.equals("top_left")) {
				domainMarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
				if (drawAsLine)
					domainMarker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
				else
					domainMarker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
			} else if (labelPos.equals("top")) {
				domainMarker.setLabelAnchor(RectangleAnchor.TOP);
				domainMarker.setLabelTextAnchor(TextAnchor.TOP_CENTER);
			} else if (labelPos.equals("top_right")) {
				domainMarker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
				if (drawAsLine)
					domainMarker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
				else
					domainMarker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
			} else if (labelPos.equals("left")) {
				domainMarker.setLabelAnchor(RectangleAnchor.LEFT);
				if (drawAsLine)
					domainMarker.setLabelTextAnchor(TextAnchor.CENTER_RIGHT);
				else
					domainMarker.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
			} else if (labelPos.equals("center")) {
				domainMarker.setLabelAnchor(RectangleAnchor.CENTER);
				domainMarker.setLabelTextAnchor(TextAnchor.CENTER);
			} else if (labelPos.equals("right")) {
				domainMarker.setLabelAnchor(RectangleAnchor.RIGHT);
				if (drawAsLine)
					domainMarker.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
				else
					domainMarker.setLabelTextAnchor(TextAnchor.CENTER_RIGHT);
			} else if (labelPos.equals("bottom_left")) {
				domainMarker.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
				if (drawAsLine)
					domainMarker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
				else
					domainMarker.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
			} else if (labelPos.equals("bottom")) {
				domainMarker.setLabelAnchor(RectangleAnchor.BOTTOM);
				domainMarker.setLabelTextAnchor(TextAnchor.BOTTOM_CENTER);
			} else if (labelPos.equals("bottom_right")) {
				domainMarker.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
				if (drawAsLine)
					domainMarker.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
				else
					domainMarker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
			}
			domainMarker.setLabelOffsetType(LengthAdjustmentType.NO_CHANGE);
			domainMarker.setLabelFont(getFont(dmData.getFont(), dmData.getFontBold(), dmData.getFontItalic(), dmData.getFontSize()));
		}
		plot.addDomainMarker(domainMarker, Layer.BACKGROUND);
	}

	public void addDomainMarker(XYPlot plot, cfCHARTDOMAINMARKERData dmData) throws cfmRunTimeException {
		double dbl;
		try {
			dbl = Double.parseDouble(dmData.getValue());
		} catch (NumberFormatException nfe) {
			throw newRunTimeException("the CFCHARTDOMAINMARKER value attribute must be numeric for scale charts");
		}
		ValueMarker domainMarker = new ValueMarker(dbl);
		boolean drawAsLine = true; // XY charts currently only support drawing
																// domain markers as lines
		domainMarker.setPaint(convertStringToColor(dmData.getColor()));
		if (dmData.getLabel() != null) {
			domainMarker.setLabel(dmData.getLabel());
			domainMarker.setLabelPaint(convertStringToColor(dmData.getLabelColor()));
			String labelPos = dmData.getLabelPosition();
			if (labelPos.equals("top_left")) {
				domainMarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
				if (drawAsLine)
					domainMarker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
				else
					domainMarker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
			} else if (labelPos.equals("top")) {
				domainMarker.setLabelAnchor(RectangleAnchor.TOP);
				domainMarker.setLabelTextAnchor(TextAnchor.TOP_CENTER);
			} else if (labelPos.equals("top_right")) {
				domainMarker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
				if (drawAsLine)
					domainMarker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
				else
					domainMarker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
			} else if (labelPos.equals("left")) {
				domainMarker.setLabelAnchor(RectangleAnchor.LEFT);
				if (drawAsLine)
					domainMarker.setLabelTextAnchor(TextAnchor.CENTER_RIGHT);
				else
					domainMarker.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
			} else if (labelPos.equals("center")) {
				domainMarker.setLabelAnchor(RectangleAnchor.CENTER);
				domainMarker.setLabelTextAnchor(TextAnchor.CENTER);
			} else if (labelPos.equals("right")) {
				domainMarker.setLabelAnchor(RectangleAnchor.RIGHT);
				if (drawAsLine)
					domainMarker.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
				else
					domainMarker.setLabelTextAnchor(TextAnchor.CENTER_RIGHT);
			} else if (labelPos.equals("bottom_left")) {
				domainMarker.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
				if (drawAsLine)
					domainMarker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
				else
					domainMarker.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
			} else if (labelPos.equals("bottom")) {
				domainMarker.setLabelAnchor(RectangleAnchor.BOTTOM);
				domainMarker.setLabelTextAnchor(TextAnchor.BOTTOM_CENTER);
			} else if (labelPos.equals("bottom_right")) {
				domainMarker.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
				if (drawAsLine)
					domainMarker.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
				else
					domainMarker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
			}
			domainMarker.setLabelOffsetType(LengthAdjustmentType.NO_CHANGE);
			domainMarker.setLabelFont(getFont(dmData.getFont(), dmData.getFontBold(), dmData.getFontItalic(), dmData.getFontSize()));
		}
		plot.addDomainMarker(domainMarker, Layer.BACKGROUND);
	}

	/*
	 * MinMaxData
	 * 
	 * This class is used to store the minimum and maximum y-value of a chart. The
	 * is used when the GRIDLINES attribute is specified to determine what the
	 * yAxisUnits value should be set to.
	 */
	class MinMaxData {
		public double minValue;

		public double maxValue;

		public MinMaxData() {
			minValue = 0;
			maxValue = 0;
		}
	}
}
