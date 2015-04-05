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

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Map;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.cfm.tag.cfOptionalBodyTag;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.cfm.tag.tagLocator;
import com.naryx.tagfusion.cfm.tag.tagReader;

public class cfCHARTSERIES extends cfTag implements cfOptionalBodyTag, Serializable {
	static final long serialVersionUID = 1;

	private static final String TAG_NAME = "CFCHARTSERIES";
	private String endMarker = null;
	public static final String DATA_BIN_KEY = "CFCHARTSERIES_DATA";
	private String type;
	private Map<String, String> nonDefaultAttributes;

	
  public java.util.Map getInfo(){
  	return createInfo("output", "The child tag of CFCHART");
  }
  
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
 			createAttInfo("QUERY", 											"Query to be used as the chart data", "", false ),
 			
 			createAttInfo("ITEMCOLUMN", 								"X Axis value. Required if QUERY is specified.", "", false ),
 			
			createAttInfo("VALUECOLUMN", 								"Y Axis value. Required if QUERY is specified.", "", false ),
			
			createAttInfo("COLORLIST", 									"Colour list used to determine the colour for each item for pie and category charts. " +
																									"This is a comma seperated list of colours.", "", false ),
																	
			createAttInfo("MARKERSTYLE", 								"Marker Style for each data point. Available options are: " +
																									"triangle, triangledown, triangleright, triangleleft, diamond, " +
																									"circle, letterx, mcross, snow, rcross, horizontalrectangle, " +
																									"verticalrectangle, rectangle", "rectangle", false ),
																		
			createAttInfo("PAINTSTYLE", 								"Allows different shading of series colours. " +
																									"Available options are: plain, light, shade", "plain", false ),
			
			createAttInfo("SERIESCOLOR", 								"The colour of the series. This can be a hexadecimal colour " +
																									"or one of the following predefined colours: " +
																									"aliceblue, antiquewhite, aqua, aquamarine, azure, beige, bisque, black, " +
																									"blanchedalmond, blue, blueviolet, brown, burlywood, cadetblue, chartreuse, " +
																									"chocolate, coral, cornflowerblue, cornsilk, crimson, cyan, darkblue, darkcyan, " +
																									"darkgoldenrod, darkgray, darkgreen, darkgrey, darkkhaki, darkmagenta, darkolivegreen, " +
																									"darkorange, darkorchid, darkred, darksalmon, darkslateblue, darkslategray, " +
																									"darkslategrey, darkturquoise, darkviolet, deeppink, deepskyblue, dimgray, dimgrey, " +
																									"dodgerblue, firebrick, floralwhite, forestgreen, fuchsia, gainsboro, ghostwhite, " +
																									"gold, goldenrod, gray, green, greenyellow, grey, honeydew, hotpink, indianred, " +
																									"indigo, ivory, khaki, lavender, lavenderblush, lawngreen, lemonchiffon, lightblue, " +
																									"lightcoral, lightcyan, lightgoldenrodyellow, lightgray, lightgreen, lightgrey, " +
																									"lightpink, lightsalmon, lightseagreen, lightskyblue, lightslategray, lightslategrey, " +
																									"lightsteelblue, lightyellow, lime, limegreen, linen, magenta, maroon, mediumaquamarine, " +
																									"mediumblue, mediumorchid, mediumpurple, mediumseagreen, mediumslateblue, mediumspringgreen, " +
																									"mediumturquoise, mediumvioletred, midnightblue, mintcream, mistyrose, moccasin, navajowhite, " +
																									"navy, oldlace, olive, olivedrab, orange, orangered, orchid, palegoldenrod, palegreen, " +
																									"paleturquoise, palevioletred, papayawhip, peachpuff, peru, pink, plum, powderblue, purple, " +
																									"red, rosybrown, royalblue, saddlebrown, salmon, sandybrown, seagreen, seashell, sienna, " +
																									"silver, skyblue, slateblue, slategray, slategrey, snow, springgreen, steelblue, tan, teal, " +
																									"thistle, tomato, turquoise, violet, violetred, wheat, white, whitesmoke, yellow, yellowgreen",
																									"", false ),
			
			createAttInfo("SERIESLABEL", 								"The label of the series.", "Series Number e.g. Series 1", false ),
			
			createAttInfo("TYPE", 											"Type of Chart to produce. Options are: area, bar, horizontalbar, " +
																									"line, pie, ring, scatter and step", "", true ),
			
			createAttInfo("DATALABELSTYLE", 						"Style of the label for the data point. Available options are: " +
																									"none, value, rowlabel, columnlabel, pattern", "none", false ),
			
			createAttInfo("DATALABELANGLE", 						"Angle of label for data point", "0", false ),
			
			createAttInfo("DATALABELCOLOR", 						"Colour of the data item labels. " +
																									"This can be a hexadecimal colour " +
																									"or one of the following predefined colours: " +
																									"aliceblue, antiquewhite, aqua, aquamarine, azure, beige, bisque, black, " +
																									"blanchedalmond, blue, blueviolet, brown, burlywood, cadetblue, chartreuse, " +
																									"chocolate, coral, cornflowerblue, cornsilk, crimson, cyan, darkblue, darkcyan, " +
																									"darkgoldenrod, darkgray, darkgreen, darkgrey, darkkhaki, darkmagenta, darkolivegreen, " +
																									"darkorange, darkorchid, darkred, darksalmon, darkslateblue, darkslategray, " +
																									"darkslategrey, darkturquoise, darkviolet, deeppink, deepskyblue, dimgray, dimgrey, " +
																									"dodgerblue, firebrick, floralwhite, forestgreen, fuchsia, gainsboro, ghostwhite, " +
																									"gold, goldenrod, gray, green, greenyellow, grey, honeydew, hotpink, indianred, " +
																									"indigo, ivory, khaki, lavender, lavenderblush, lawngreen, lemonchiffon, lightblue, " +
																									"lightcoral, lightcyan, lightgoldenrodyellow, lightgray, lightgreen, lightgrey, " +
																									"lightpink, lightsalmon, lightseagreen, lightskyblue, lightslategray, lightslategrey, " +
																									"lightsteelblue, lightyellow, lime, limegreen, linen, magenta, maroon, mediumaquamarine, " +
																									"mediumblue, mediumorchid, mediumpurple, mediumseagreen, mediumslateblue, mediumspringgreen, " +
																									"mediumturquoise, mediumvioletred, midnightblue, mintcream, mistyrose, moccasin, navajowhite, " +
																									"navy, oldlace, olive, olivedrab, orange, orangered, orchid, palegoldenrod, palegreen, " +
																									"paleturquoise, palevioletred, papayawhip, peachpuff, peru, pink, plum, powderblue, purple, " +
																									"red, rosybrown, royalblue, saddlebrown, salmon, sandybrown, seagreen, seashell, sienna, " +
																									"silver, skyblue, slateblue, slategray, slategrey, snow, springgreen, steelblue, tan, teal, " +
																									"thistle, tomato, turquoise, violet, violetred, wheat, white, whitesmoke, yellow, yellowgreen",
																									"black", false ),
			
			createAttInfo("DATALABELFONT", 							"Font of the data label. Options are: arial, times and courier", "arial", false ),
			
			createAttInfo("DATALABELFONTBOLD", 					"Whether data label font is bold : yes or no", "no", false ),
			
			createAttInfo("DATALABELFONTITALIC", 				"Whether data label font is italic : yes or no", "no", false ),
			
			createAttInfo("DATALABELFONTSIZE", 					"Size of data label font", "11", false ),
			
			createAttInfo("NEGATIVEDATALABELPOSITION",	"Position of Data label. Available options are: top, top_inside, left, left_inside, " +
																									"center, right, right_inside, bottom_inside, bottom", "bottom", false ),
			
			createAttInfo("POSITIVEDATALABELPOSITION", 	"Position of Data label. Available options are: top, top_inside, left, left_inside, " +
																									"center, right, right_inside, bottom_inside, bottom", "top", false )
			
  	};
		
  	
  }
	
	
	protected void defaultParameters(String _tag) throws cfmBadFileException {

		parseTagHeader(_tag);

		// Keep track of the attributes that don't use default values
		nonDefaultAttributes = new FastMap<String, String>(properties);

		// NOTE: the default values need to be set at render time in
		// setDefaultParameters()

		if (containsAttribute("QUERY")) {
			if (!containsAttribute("ITEMCOLUMN"))
				throw newBadFileException("Missing ITEMCOLUMN", "You need to provide an ITEMCOLUMN when the QUERY attribute is specifed.");
			if (!containsAttribute("VALUECOLUMN"))
				throw newBadFileException("Missing VALUECOLUMN", "You need to provide a VALUECOLUMN when the QUERY attribute is specifed.");
		}
	}

	private void setDefaultParameters(cfSession _Session, cfCHARTInternalData chartData) {
		unclipAttributes();
		cfTag defaultChartSeriesTag = chartData.getDefaultChartSeriesTag();

		// Set the default values for attributes that were not set
		if (!nonDefaultAttributes.containsKey("COLORLIST")) {
			if ((defaultChartSeriesTag != null) && (defaultChartSeriesTag.containsAttribute("COLORLIST")))
				defaultAttribute("COLORLIST", defaultChartSeriesTag.getConstant("COLORLIST"));
			else
				removeAttribute("COLORLIST");
		}
		if (!nonDefaultAttributes.containsKey("MARKERSTYLE")) {
			if ((defaultChartSeriesTag != null) && (defaultChartSeriesTag.containsAttribute("MARKERSTYLE")))
				defaultAttribute("MARKERSTYLE", defaultChartSeriesTag.getConstant("MARKERSTYLE"));
			else
				defaultAttribute("MARKERSTYLE", "rectangle");
		}
		if (!nonDefaultAttributes.containsKey("PAINTSTYLE")) {
			if ((defaultChartSeriesTag != null) && (defaultChartSeriesTag.containsAttribute("PAINTSTYLE")))
				defaultAttribute("PAINTSTYLE", defaultChartSeriesTag.getConstant("PAINTSTYLE"));
			else
				defaultAttribute("PAINTSTYLE", "plain");
		}
		if (!nonDefaultAttributes.containsKey("SERIESCOLOR")) {
			if ((defaultChartSeriesTag != null) && (defaultChartSeriesTag.containsAttribute("SERIESCOLOR")))
				defaultAttribute("SERIESCOLOR", defaultChartSeriesTag.getConstant("SERIESCOLOR"));
			else
				removeAttribute("SERIESCOLOR");
		}
		if (!nonDefaultAttributes.containsKey("SERIESLABEL")) {
			if ((defaultChartSeriesTag != null) && (defaultChartSeriesTag.containsAttribute("SERIESLABEL")))
				defaultAttribute("SERIESLABEL", defaultChartSeriesTag.getConstant("SERIESLABEL"));
			else
				defaultAttribute("SERIESLABEL", chartData.getDefaultSeriesLabel());
		}
		if (!nonDefaultAttributes.containsKey("TYPE")) {
			if ((defaultChartSeriesTag != null) && (defaultChartSeriesTag.containsAttribute("TYPE")))
				defaultAttribute("TYPE", defaultChartSeriesTag.getConstant("TYPE"));
			else
				removeAttribute("TYPE");
		}
		if (!nonDefaultAttributes.containsKey("DATALABELSTYLE")) {
			if ((defaultChartSeriesTag != null) && (defaultChartSeriesTag.containsAttribute("DATALABELSTYLE")))
				defaultAttribute("DATALABELSTYLE", defaultChartSeriesTag.getConstant("DATALABELSTYLE"));
			else
				removeAttribute("DATALABELSTYLE");
		}
		if (!nonDefaultAttributes.containsKey("DATALABELANGLE")) {
			if ((defaultChartSeriesTag != null) && (defaultChartSeriesTag.containsAttribute("DATALABELANGLE")))
				defaultAttribute("DATALABELANGLE", defaultChartSeriesTag.getConstant("DATALABELANGLE"));
			else
				defaultAttribute("DATALABELANGLE", "0");
		}
		if (!nonDefaultAttributes.containsKey("DATALABELCOLOR")) {
			if ((defaultChartSeriesTag != null) && (defaultChartSeriesTag.containsAttribute("DATALABELCOLOR")))
				defaultAttribute("DATALABELCOLOR", defaultChartSeriesTag.getConstant("DATALABELCOLOR"));
			else
				defaultAttribute("DATALABELCOLOR", "black");
		}
		if (!nonDefaultAttributes.containsKey("DATALABELFONT")) {
			if ((defaultChartSeriesTag != null) && (defaultChartSeriesTag.containsAttribute("DATALABELFONT")))
				defaultAttribute("DATALABELFONT", defaultChartSeriesTag.getConstant("DATALABELFONT"));
			else
				defaultAttribute("DATALABELFONT", "arial");
		}
		if (!nonDefaultAttributes.containsKey("DATALABELFONTBOLD")) {
			if ((defaultChartSeriesTag != null) && (defaultChartSeriesTag.containsAttribute("DATALABELFONTBOLD")))
				defaultAttribute("DATALABELFONTBOLD", defaultChartSeriesTag.getConstant("DATALABELFONTBOLD"));
			else
				defaultAttribute("DATALABELFONTBOLD", "no");
		}
		if (!nonDefaultAttributes.containsKey("DATALABELFONTITALIC")) {
			if ((defaultChartSeriesTag != null) && (defaultChartSeriesTag.containsAttribute("DATALABELFONTITALIC")))
				defaultAttribute("DATALABELFONTITALIC", defaultChartSeriesTag.getConstant("DATALABELFONTITALIC"));
			else
				defaultAttribute("DATALABELFONTITALIC", "no");
		}
		if (!nonDefaultAttributes.containsKey("DATALABELFONTSIZE")) {
			if ((defaultChartSeriesTag != null) && (defaultChartSeriesTag.containsAttribute("DATALABELFONTSIZE")))
				defaultAttribute("DATALABELFONTSIZE", defaultChartSeriesTag.getConstant("DATALABELFONTSIZE"));
			else
				defaultAttribute("DATALABELFONTSIZE", "11");
		}
		if (!nonDefaultAttributes.containsKey("NEGATIVEDATALABELPOSITION")) {
			if ((defaultChartSeriesTag != null) && (defaultChartSeriesTag.containsAttribute("NEGATIVEDATALABELPOSITION")))
				defaultAttribute("NEGATIVEDATALABELPOSITION", defaultChartSeriesTag.getConstant("NEGATIVEDATALABELPOSITION"));
			else
				defaultAttribute("NEGATIVEDATALABELPOSITION", "bottom");
		}
		if (!nonDefaultAttributes.containsKey("POSITIVEDATALABELPOSITION")) {
			if ((defaultChartSeriesTag != null) && (defaultChartSeriesTag.containsAttribute("POSITIVEDATALABELPOSITION")))
				defaultAttribute("POSITIVEDATALABELPOSITION", defaultChartSeriesTag.getConstant("POSITIVEDATALABELPOSITION"));
			else
				defaultAttribute("POSITIVEDATALABELPOSITION", "top");
		}
	}

	public String getEndMarker() {
		return endMarker;
	}

	public void setEndTag() {
		endMarker = null;
	}

	public void lookAheadForEndTag(tagReader inFile) {
		endMarker = (new tagLocator(TAG_NAME, inFile)).findEndMarker();
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		// --[ Get the internal chart data in which this relates to
		cfCHARTInternalData chartData = (cfCHARTInternalData) _Session.getDataBin(cfCHART.DATA_BIN_KEY);
		if (chartData == null)
			throw newRunTimeException("CFCHARTSERIES must be used inside a CFCHART tag");

		setDefaultParameters(_Session, chartData);

		if (!containsAttribute("TYPE"))
			throw newBadFileException("Missing TYPE", "You need to provide a TYPE");

		// --[ Get the TYPE property out
		type = getDynamic(_Session, "TYPE").toString().toLowerCase();
		if (!type.equals("area") && !type.equals("bar") && !type.equals("horizontalbar") && !type.equals("line") && !type.equals("pie") && !type.equals("ring") && !type.equals("scatter") && !type.equals("step")) {
			throw newBadFileException("Invalid TYPE Attribute", "Only the area, bar, horizontalbar, line, pie, ring, scatter and step chart types are supported.");
		}

		String seriesLabel = getDynamic(_Session, "SERIESLABEL").toString();
		String negDataLabelPos = getDynamic(_Session, "NEGATIVEDATALABELPOSITION").toString().toLowerCase();
		if (!negDataLabelPos.equals("top") && !negDataLabelPos.equals("top_inside") && !negDataLabelPos.equals("left") && !negDataLabelPos.equals("left_inside") && !negDataLabelPos.equals("center") && !negDataLabelPos.equals("right") && !negDataLabelPos.equals("right_inside") && !negDataLabelPos.equals("bottom_inside") && !negDataLabelPos.equals("bottom")) {
			throw newBadFileException("Invalid NEGATIVEDATALABELPOSITION Attribute", "The negativeDataLabelPosition attribute cannot have a value of '" + negDataLabelPos + "'");
		}

		String posDataLabelPos = getDynamic(_Session, "POSITIVEDATALABELPOSITION").toString().toLowerCase();
		if (!posDataLabelPos.equals("top") && !posDataLabelPos.equals("top_inside") && !posDataLabelPos.equals("left") && !posDataLabelPos.equals("left_inside") && !posDataLabelPos.equals("center") && !posDataLabelPos.equals("right") && !posDataLabelPos.equals("right_inside") && !posDataLabelPos.equals("bottom_inside") && !posDataLabelPos.equals("bottom")) {
			throw newBadFileException("Invalid POSITIVEDATALABELPOSITION Attribute", "The positiveDataLabelPosition attribute cannot have a value of '" + posDataLabelPos + "'");
		}

		double dataLabelAngle = 0;
		try {
			dataLabelAngle = getDynamic(_Session, "DATALABELANGLE").getDouble();
		} catch (dataNotSupportedException e) {
			throw newBadFileException("Invalid DATALABELANGLE Attribute", "The dataLabelAngle attribute must specify the angle in degrees.  The value '" + getDynamic(_Session, "DATALABELANGLE") + "' is not valid.");
		}

		try {
			cfCHARTSERIESData seriesData = new cfCHARTSERIESData(type, seriesLabel, getDynamic(_Session, "MARKERSTYLE").toString().toLowerCase(), getDynamic(_Session, "PAINTSTYLE").toString().toLowerCase(), getDynamic(_Session, "DATALABELSTYLE"), negDataLabelPos, posDataLabelPos, dataLabelAngle, getDynamic(_Session, "DATALABELCOLOR").toString(), getDynamic(_Session, "DATALABELFONT").toString(),
					getDynamic(_Session, "DATALABELFONTBOLD").getBoolean(), getDynamic(_Session, "DATALABELFONTITALIC").getBoolean(), getDynamic(_Session, "DATALABELFONTSIZE").getInt());
			_Session.setDataBin(cfCHARTSERIES.DATA_BIN_KEY, seriesData);

			if (containsAttribute("COLORLIST"))
				seriesData.setColorList(getDynamic(_Session, "COLORLIST").toString());

			if (containsAttribute("SERIESCOLOR"))
				seriesData.setSeriesColor(getDynamic(_Session, "SERIESCOLOR").toString());

			if (containsAttribute("QUERY")) {
				// Retrieve the series data from a query
				String queryName = getDynamic(_Session, "QUERY").toString();
				String itemColumn = getDynamic(_Session, "ITEMCOLUMN").toString();
				String valueColumn = getDynamic(_Session, "VALUECOLUMN").toString();
				cfQueryResultData qryData = null;
				cfData queryDataTmp = null; // used til we can determine the query data
																		// is the right type
				try {
					queryDataTmp = runTime.runExpression(_Session, queryName);
				} catch (cfmRunTimeException ignored) {
				} // queryDataTmp doesn't exist

				// throw exceptions if the query doesn't exist or is not the right type.
				// Cast it if it is.
				if (queryDataTmp == null) {
					throw newRunTimeException("The specified QUERY " + queryName + " does not exist.");
				} else if (queryDataTmp instanceof cfQueryResultData) {
					qryData = (cfQueryResultData) queryDataTmp;
				} else {
					throw newRunTimeException("The specified QUERY " + queryName + " is not a valid query type.");
				}

				int resetRow = qryData.getCurrentRow();

				// Going to check what type the series data is
				try {
					// Getting first row of query
					qryData.first();

					// Check data type for the itemColumn
					cfData data = qryData.getData(itemColumn);

					switch (data.getDataType()) {
					case cfData.CFDATEDATA:
						seriesData.setSeriesDataType(cfCHARTSERIESData.XY_DATE_SERIES);
						break;
					case cfData.CFNUMBERDATA:
						seriesData.setSeriesDataType(cfCHARTSERIESData.XY_NUMERIC_SERIES);
						break;
					default:
						seriesData.setSeriesDataType(cfCHARTSERIESData.CATEGORY_SERIES);
						break;
					}
				} catch (SQLException e) {
					throw newRunTimeException("Failed to retrieve data from query: " + e.getMessage());
				}

				// Make sure the cursor is before the first row
				qryData.beforeFirst();
				try {
					while (qryData.next()) {
						if (seriesData.getSeriesDataType() == cfCHARTSERIESData.CATEGORY_SERIES) {
							String item = qryData.getString(itemColumn);
							double value = qryData.getDouble(valueColumn);
							seriesData.add(item, value);
						} else {
							double x = 0;
							if (seriesData.getSeriesDataType() == cfCHARTSERIESData.XY_DATE_SERIES) {
								x = qryData.getData(itemColumn).getDateLong();
							} else {
								x = qryData.getDouble(itemColumn);
							}
							double y = qryData.getDouble(valueColumn);
							seriesData.add(x, y);
						}

					}
				} catch (SQLException e) {
					throw newRunTimeException("Failed to retrieve data from query: " + e.getMessage());
				} finally {
					// Reset the query data to the row it was at to begin with
					qryData.setCurrentRow(resetRow == 0 ? 1 : resetRow);
				}
			} else {
				// Render the CFCHARTDATA tags
				renderToString(_Session);
			}

			chartData.add(seriesData);
		} finally {
			_Session.deleteDataBin(cfCHARTSERIES.DATA_BIN_KEY);
		}

		return cfTagReturnType.NORMAL;
	}
}
