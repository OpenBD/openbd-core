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

/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2005, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
 * USA.  
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * ----------------------------
 * StandardPieURLGenerator.java
 * ----------------------------
 * (C) Copyright 2002-2005, by Richard Atkinson and Contributors.
 *
 * Original Author:  Richard Atkinson;
 * Contributors:     David Gilbert (for Object Refinery Limited);
 *
 * $Id: PieURLGenerator.java,v 1.1 2008/05/09 09:49:24 openbd Exp $
 *
 * Changes:
 * --------
 * 05-Aug-2002 : Version 1, contributed by Richard Atkinson;
 * 09-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 07-Mar-2003 : Modified to use KeyedValuesDataset and added pieIndex 
 *               parameter (DG);
 * 21-Mar-2003 : Implemented Serializable (DG);
 * 24-Apr-2003 : Switched around PieDataset and KeyedValuesDataset (DG);
 * 31-Mar-2004 : Added an optional 'pieIndex' parameter (DG);
 * 13-Jan-2005 : Fixed for compliance with XHTML 1.0 (DG):
 *
 */
 
package com.newatlanta.bluedragon;

import java.io.Serializable;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.NumberFormat;

import org.jfree.data.general.PieDataset;

/**
 * A URL generator for pie charts.
 *
 * @author Richard Atkinson
 */
public class PieURLGenerator implements org.jfree.chart.urls.PieURLGenerator, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 1L;
    
    /** The URL */
    private String url;
    private String urlLower;
    
    private NumberFormat numberFormat;
    private DateFormat dateFormat;

    /**
     * Creates a new generator.
     *
     * @param prefix  the prefix.
     */
    public PieURLGenerator(String url, NumberFormat formatter) {
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

    public PieURLGenerator(String url, DateFormat formatter) {
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
     * Generates a URL.
     *
     * @param data  the dataset.
     * @param key  the item key.
     * @param pieIndex  the pie index (ignored).
     *
     * @return A string containing the generated URL.
     */
    @SuppressWarnings("deprecation")
		public String generateURL(PieDataset data, Comparable key, int pieIndex) {

        String categoryKey = key.toString();
        Number numberValue = data.getValue(key);
        String value;
        if ( numberValue == null )
        	value = "";
        else if ( dateFormat != null )
        	value = this.dateFormat.format(numberValue);
        else
        	value = this.numberFormat.format(numberValue);

        StringBuilder generatedURL = new StringBuilder(urlLower.length());
        for ( int i = 0; i < urlLower.length(); )
        {
        	char ch = urlLower.charAt(i);
        	if ( ch == '$' )
        	{
        		if ( urlLower.regionMatches(i,"$serieslabel$",0, "$serieslabel$".length() ) )
        		{
        			generatedURL.append("");
        			i = i + "$serieslabel$".length();
        		}
        		else if ( urlLower.regionMatches(i,"$itemlabel$",0, "$itemlabel$".length() ) )
        		{
        			generatedURL.append(URLEncoder.encode(categoryKey));
        			i = i + "$itemlabel$".length();
        		}
        		else if ( urlLower.regionMatches(i,"$value$",0, "$value$".length() ) )
        		{
        			generatedURL.append(URLEncoder.encode(value));
        			i = i + "$value$".length();
        		}
            	else
            	{
            		// Preserve case by retrieving char from original URL
            		generatedURL.append(url.charAt(i));
            		i++;
            	}
        	}
        	else
        	{
        		// Preserve case by retrieving char from original URL
        		generatedURL.append(url.charAt(i));
        		i++;
        	}
        }
        return generatedURL.toString();
    }

    /**
     * Tests if this object is equal to another.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }
        
        if (obj == this) {
            return true;
        }

        if ((obj instanceof PieURLGenerator) == false) {
            return false;
        }

        PieURLGenerator generator = (PieURLGenerator) obj;
        return this.url.equals(generator.url);
    }
}
