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
import java.text.DateFormat;
import java.text.NumberFormat;

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.DataUtilities;
import org.jfree.data.category.CategoryDataset;
import org.jfree.util.PublicCloneable;

/**
 * A standard label generator that can be used with a
 * {@link org.jfree.chart.renderer.category.CategoryItemRenderer}.
 */
public class CategoryItemLabelGenerator extends StandardCategoryItemLabelGenerator implements org.jfree.chart.labels.CategoryItemLabelGenerator, Cloneable, PublicCloneable, Serializable {

	/** For serialization. */
	private static final long serialVersionUID = 3499701401211412882L;

	/** The default format string. */
	public static final String DEFAULT_LABEL_FORMAT_STRING = "{2}";

	private String[] yAxisSymbols = null;

	/**
	 * Creates a new generator with a default number formatter.
	 */
	public CategoryItemLabelGenerator() {
		super(DEFAULT_LABEL_FORMAT_STRING, NumberFormat.getInstance());
	}

	/**
	 * Creates a new generator with the specified number formatter.
	 * 
	 * @param labelFormat
	 *          the label format string (<code>null</code> not permitted).
	 * @param formatter
	 *          the number formatter (<code>null</code> not permitted).
	 */
	public CategoryItemLabelGenerator(String labelFormat, NumberFormat formatter) {
		super(labelFormat, formatter);
	}

	/**
	 * Creates a new generator with the specified date formatter.
	 * 
	 * @param labelFormat
	 *          the label format string (<code>null</code> not permitted).
	 * @param formatter
	 *          the date formatter (<code>null</code> not permitted).
	 */
	public CategoryItemLabelGenerator(String labelFormat, DateFormat formatter) {
		super(labelFormat, formatter);
	}

	public CategoryItemLabelGenerator(String labelFormat, NumberFormat formatter, String[] yAxisSymbols) {
		super(labelFormat, formatter);
		this.yAxisSymbols = yAxisSymbols;
	}

	public CategoryItemLabelGenerator(String labelFormat, DateFormat formatter, String[] yAxisSymbols) {
		super(labelFormat, formatter);
		this.yAxisSymbols = yAxisSymbols;
	}

	/**
	 * Generates the label for an item in a dataset. Note: in the current dataset
	 * implementation, each row is a series, and each column contains values for a
	 * particular category.
	 * 
	 * @param dataset
	 *          the dataset (<code>null</code> not permitted).
	 * @param row
	 *          the row index (zero-based).
	 * @param column
	 *          the column index (zero-based).
	 * 
	 * @return The label (possibly <code>null</code>).
	 */
	public String generateLabel(CategoryDataset dataset, int row, int column) {
		return generateLabelString(dataset, row, column);
	}

	protected Object[] createItemArray(CategoryDataset dataset, int row, int column) {
		Object[] result = new Object[5];
		result[0] = dataset.getRowKey(row).toString();
		result[1] = dataset.getColumnKey(column).toString();
		Number value = dataset.getValue(row, column);
		if (value != null) {
			if (yAxisSymbols != null) {
				int intValue = value.intValue();
				if (intValue < yAxisSymbols.length)
					result[2] = yAxisSymbols[intValue];
				else
					result[2] = "???";
			} else if (this.getNumberFormat() != null) {
				result[2] = this.getNumberFormat().format(value);
			} else if (this.getDateFormat() != null) {
				result[2] = this.getDateFormat().format(value);
			}
		} else {
			result[2] = "-"; // this.nullValueString;
		}

		if (value != null) {
			double total = DataUtilities.calculateRowTotal(dataset, row);
			double percent = value.doubleValue() / total;
			result[3] = NumberFormat.getPercentInstance().format(percent);
			if (this.getNumberFormat() != null) {
				result[4] = this.getNumberFormat().format(total);
			} else if (this.getDateFormat() != null) {
				// result[4] = this.getDateFormat().format(total);
			}
		}

		return result;
	}

}
