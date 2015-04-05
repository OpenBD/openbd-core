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
import java.util.Date;

import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.PublicCloneable;

/**
 * A standard item label generator for plots that use data from an 
 * {@link org.jfree.data.xy.XYDataset}.
 */
public class XYItemLabelGenerator extends StandardXYItemLabelGenerator  
                                          implements org.jfree.chart.labels.XYItemLabelGenerator, 
                                                     Cloneable, 
                                                     PublicCloneable,
                                                     Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 7807668053171837925L;
    
    /** The default item label format. */
    public static final String DEFAULT_ITEM_LABEL_FORMAT = "{2}";

    /**
     * Creates an item label generator using default number formatters.
     */
    public XYItemLabelGenerator() {
        this(
            DEFAULT_ITEM_LABEL_FORMAT, 
            NumberFormat.getNumberInstance(), NumberFormat.getNumberInstance()
        );
    }


    /**
     * Creates an item label generator using the specified number formatters.
     *
     * @param formatString  the item label format string (<code>null</code> not 
     *                      permitted).
     * @param xFormat  the format object for the x values (<code>null</code> 
     *                 not permitted).
     * @param yFormat  the format object for the y values (<code>null</code> 
     *                 not permitted).
     */
    public XYItemLabelGenerator(String formatString,
                                        NumberFormat xFormat, 
                                        NumberFormat yFormat) {
        
        super(formatString, xFormat, yFormat);
    
    }

    /**
     * Creates an item label generator using the specified number formatters.
     *
     * @param formatString  the item label format string (<code>null</code> 
     *                      not permitted).
     * @param xFormat  the format object for the x values (<code>null</code> 
     *                 not permitted).
     * @param yFormat  the format object for the y values (<code>null</code> 
     *                 not permitted).
     */
    public XYItemLabelGenerator(String formatString,
                                        DateFormat xFormat, 
                                        NumberFormat yFormat) {
        
        super(formatString, xFormat, yFormat);
    
    }

    /**
     * Creates a label generator using the specified date formatters.
     *
     * @param formatString  the label format string (<code>null</code> not 
     *                      permitted).
     * @param xFormat  the format object for the x values (<code>null</code> 
     *                 not permitted).
     * @param yFormat  the format object for the y values (<code>null</code> 
     *                 not permitted).
     */
    public XYItemLabelGenerator(String formatString,
                                        DateFormat xFormat, 
                                        DateFormat yFormat) {
        
        super(formatString, xFormat, yFormat);
    
    }

    /**
     * Generates the item label text for an item in a dataset.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The label text (possibly <code>null</code>).
     */
    public String generateLabel(XYDataset dataset, int series, int item) {
        return generateLabelString(dataset, series, item);
    }

    /**
     * Returns an independent copy of the generator.
     * 
     * @return A clone.
     * 
     * @throws CloneNotSupportedException if cloning is not supported.
     */
    public Object clone() throws CloneNotSupportedException { 
        return super.clone();
    }
    
    /**
     * Tests this object for equality with an arbitrary object.
     *
     * @param obj  the other object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof StandardXYItemLabelGenerator) {
            return super.equals(obj);
        }
        return false;
    }

    protected Object[] createItemArray(XYDataset dataset, int series, int item) {
		Object[] result = new Object[5];
		result[0] = dataset.getSeriesKey(series).toString();
		
		double x = dataset.getXValue(series, item);
		if (Double.isNaN(x) && dataset.getX(series, item) == null) {
			result[1] = "null";	//this.nullXString;
		}
		else {
			if (this.getXDateFormat() != null) {
				result[1] = this.getXDateFormat().format(new Date((long) x));   
			}
			else {
				result[1] = this.getXFormat().format(x);
			}
		}
		
		double y = dataset.getYValue(series, item);
		if (Double.isNaN(y) && dataset.getY(series, item) == null) {
			result[2] = "null";	//this.nullYString;
		}
		else {
			if (this.getYDateFormat() != null) {
				result[2] = this.getYDateFormat().format(new Date((long) y));   
			}
			else {
				result[2] = this.getYFormat().format(y);
			}
			double total = calculateYTotal(dataset, series);
			double percent = y / total;
			result[3] = NumberFormat.getPercentInstance().format(percent);
			if (this.getYFormat() != null) {
				result[4] = this.getYFormat().format(total);
			}
			else if (this.getYDateFormat() != null) {
				//result[4] = this.getDateFormat().format(total);
			}
		}
		return result;
	}
    
    private static double calculateYTotal(XYDataset dataset, int series)
    {
    	double total = 0;
    	int numItems = dataset.getItemCount(series);
    	for ( int i = 0; i < numItems; i++ )
    		total += dataset.getYValue(series,i);
    	return total;
    }
}
