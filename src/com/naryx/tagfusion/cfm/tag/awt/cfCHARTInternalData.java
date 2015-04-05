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

package com.naryx.tagfusion.cfm.tag.awt;

import java.util.ArrayList;
import java.util.List;

import com.naryx.tagfusion.cfm.tag.cfTag;

public class cfCHARTInternalData extends Object {
	private List<cfCHARTSERIESData> series = new ArrayList<cfCHARTSERIESData>();

	private List<cfCHARTRANGEMARKERData> rangeMarkers = new ArrayList<cfCHARTRANGEMARKERData>();
	private List<cfCHARTDOMAINMARKERData> domainMarkers = new ArrayList<cfCHARTDOMAINMARKERData>();
	private boolean isXyChart = false; // set to true if it's a scale(xy) chart

	private cfTag defaultChartTag;

	private cfCHARTLEGENDData legendData = null;
	private cfCHARTIMAGEData imageData = null;
	private List<cfCHARTTITLEData> titles = new ArrayList<cfCHARTTITLEData>();

	public cfCHARTInternalData(String axisType, cfTag defaultChartTag) {
		if (axisType.equals("scale"))
			isXyChart = true;

		this.defaultChartTag = defaultChartTag;
	}

	public void setLegendData(cfCHARTLEGENDData data) {
		legendData = data;
	}

	public cfCHARTLEGENDData getLegendData() {
		return legendData;
	}

	public void setImageData(cfCHARTIMAGEData data) {
		imageData = data;
	}

	public cfCHARTIMAGEData getImageData() {
		return imageData;
	}

	public List<cfCHARTSERIESData> getSeries() {
		return series;
	}

	public List<cfCHARTRANGEMARKERData> getRangeMarkers() {
		return rangeMarkers;
	}

	public List<cfCHARTDOMAINMARKERData> getDomainMarkers() {
		return domainMarkers;
	}

	public List<cfCHARTTITLEData> getTitles() {
		return titles;
	}

	public boolean isXyChart() {
		return isXyChart;
	}

	public void add(cfCHARTSERIESData seriesData) {
		series.add(seriesData);
	}

	public void add(cfCHARTRANGEMARKERData rangeMarkerData) {
		rangeMarkers.add(rangeMarkerData);
	}

	public void add(cfCHARTDOMAINMARKERData domainMarkerData) {
		domainMarkers.add(domainMarkerData);
	}

	public void add(cfCHARTTITLEData titleData) {
		titles.add(titleData);
	}

	public String getDefaultSeriesLabel() {
		return "Series " + series.size();
	}

	/*
	 * getDefaultChartSeriesTag
	 * 
	 * This method uses the length of the series vector to know which default
	 * CFCHARTSERIES tag to return. For example, if the series vector is empty
	 * then this method needs to return the first default CFCHARTSERIES tag.
	 */
	public cfTag getDefaultChartSeriesTag() {
		if (defaultChartTag == null)
			return null;

		cfTag[] childTags = defaultChartTag.getTagList();
		if (series.size() >= childTags.length)
			return null;

		int tagNum = 0;
		for (int i = 0; i < childTags.length; i++) {
			if (childTags[i] instanceof cfCHARTSERIES) {
				if (series.size() == tagNum)
					return childTags[i];
				tagNum++;
			}
		}

		return null;
	}

	public cfTag getDefaultChartRangeMarkerTag() {
		if (defaultChartTag == null)
			return null;

		cfTag[] childTags = defaultChartTag.getTagList();
		if (rangeMarkers.size() >= childTags.length)
			return null;

		int tagNum = 0;
		for (int i = 0; i < childTags.length; i++) {
			if (childTags[i] instanceof cfCHARTRANGEMARKER) {
				if (rangeMarkers.size() == tagNum)
					return childTags[i];
				tagNum++;
			}
		}

		return null;
	}

	public cfTag getDefaultChartDomainMarkerTag() {
		if (defaultChartTag == null)
			return null;

		cfTag[] childTags = defaultChartTag.getTagList();
		if (domainMarkers.size() >= childTags.length)
			return null;

		int tagNum = 0;
		for (int i = 0; i < childTags.length; i++) {
			if (childTags[i] instanceof cfCHARTDOMAINMARKER) {
				if (domainMarkers.size() == tagNum)
					return childTags[i];
				tagNum++;
			}
		}

		return null;
	}

	public cfTag getDefaultChartTitleTag() {
		if (defaultChartTag == null)
			return null;

		cfTag[] childTags = defaultChartTag.getTagList();
		if (titles.size() >= childTags.length)
			return null;

		int tagNum = 0;
		for (int i = 0; i < childTags.length; i++) {
			if (childTags[i] instanceof cfCHARTTITLE) {
				if (titles.size() == tagNum)
					return childTags[i];
				tagNum++;
			}
		}

		return null;
	}

	public cfTag getDefaultChartLegendTag() {
		if (defaultChartTag == null)
			return null;

		cfTag[] childTags = defaultChartTag.getTagList();

		for (int i = 0; i < childTags.length; i++) {
			if (childTags[i] instanceof cfCHARTLEGEND)
				return childTags[i];
		}

		return null;
	}

	public cfTag getDefaultChartImageTag() {
		if (defaultChartTag == null)
			return null;

		cfTag[] childTags = defaultChartTag.getTagList();

		for (int i = 0; i < childTags.length; i++) {
			if (childTags[i] instanceof cfCHARTIMAGE)
				return childTags[i];
		}

		return null;
	}
}
