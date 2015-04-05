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

import java.io.Serializable;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.NumberFormat;

import org.jfree.data.category.CategoryDataset;
import org.jfree.util.ObjectUtilities;

/**
 * A URL generator that can be assigned to a
 * {@link org.jfree.chart.renderer.category.CategoryItemRenderer}.
 * 
 * @author Richard Atkinson
 */
public class CategoryURLGenerator implements org.jfree.chart.urls.CategoryURLGenerator, Cloneable, Serializable {

	/** For serialization. */
	private static final long serialVersionUID = 1L;

	/** The URL */
	private String url;

	private String urlLower;

	private NumberFormat numberFormat;

	private DateFormat dateFormat;

	public CategoryURLGenerator(String url, NumberFormat formatter) {
		if (url == null) {
			throw new IllegalArgumentException("Null 'url' argument.");
		}
		if (formatter == null) {
			throw new IllegalArgumentException("Null 'formatter' argument.");
		}
		this.url = url;
		this.urlLower = url.toLowerCase();

		this.numberFormat = formatter;
	}

	public CategoryURLGenerator(String url, DateFormat formatter) {
		if (url == null) {
			throw new IllegalArgumentException("Null 'url' argument.");
		}
		if (formatter == null) {
			throw new IllegalArgumentException("Null 'formatter' argument.");
		}
		this.url = url;
		this.urlLower = url.toLowerCase();

		this.dateFormat = formatter;
	}

	/**
	 * Generates a URL for a particular item within a series.
	 * 
	 * @param dataset
	 *          the dataset.
	 * @param series
	 *          the series index (zero-based).
	 * @param category
	 *          the category index (zero-based).
	 * 
	 * @return The generated URL.
	 */
	@SuppressWarnings("deprecation")
	public String generateURL(CategoryDataset dataset, int series, int category) {
		String seriesKey = dataset.getRowKey(series).toString();
		String categoryKey = dataset.getColumnKey(category).toString();
		Number numberValue = dataset.getValue(series, category);
		String value;
		if (numberValue == null)
			value = "";
		else if (dateFormat != null)
			value = this.dateFormat.format(numberValue);
		else
			value = this.numberFormat.format(numberValue);

		StringBuilder generatedURL = new StringBuilder(urlLower.length());
		for (int i = 0; i < urlLower.length();) {
			char ch = urlLower.charAt(i);
			if (ch == '$') {
				if (urlLower.regionMatches(i, "$serieslabel$", 0, "$serieslabel$".length())) {
					generatedURL.append(URLEncoder.encode(seriesKey));
					i = i + "$serieslabel$".length();
				} else if (urlLower.regionMatches(i, "$itemlabel$", 0, "$itemlabel$".length())) {
					generatedURL.append(URLEncoder.encode(categoryKey));
					i = i + "$itemlabel$".length();
				} else if (urlLower.regionMatches(i, "$value$", 0, "$value$".length())) {
					generatedURL.append(URLEncoder.encode(value));
					i = i + "$value$".length();
				} else {
					// Preserve case by retrieving char from original URL
					generatedURL.append(url.charAt(i));
					i++;
				}
			} else {
				// Preserve case by retrieving char from original URL
				generatedURL.append(url.charAt(i));
				i++;
			}
		}
		return generatedURL.toString();
	}

	/**
	 * Returns an independent copy of the URL generator.
	 * 
	 * @return A clone.
	 * 
	 * @throws CloneNotSupportedException
	 *           not thrown by this class, but subclasses (if any) might.
	 */
	public Object clone() throws CloneNotSupportedException {

		// all attributes are immutable, so we can just return the super.clone()
		return super.clone();

	}

	/**
	 * Tests the generator for equality with an arbitrary object.
	 * 
	 * @param obj
	 *          the object (<code>null</code> permitted).
	 * 
	 * @return A boolean.
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof CategoryURLGenerator)) {
			return false;
		}
		CategoryURLGenerator that = (CategoryURLGenerator) obj;
		if (!ObjectUtilities.equal(this.url, that.url)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns a hash code.
	 * 
	 * @return A hash code.
	 */
	public int hashCode() {
		int result;
		result = (this.url != null ? this.url.hashCode() : 0);
		return result;
	}

}
