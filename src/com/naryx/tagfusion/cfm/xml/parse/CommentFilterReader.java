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

package com.naryx.tagfusion.cfm.xml.parse;

import java.io.IOException;
import java.io.Reader;

/**
 * A FilterReader that removes xml comment content from the underlying Reader
 * as it's read.
 * 
 * @author Matt Jacobsen
 *
 */
public class CommentFilterReader extends XmlFilterReader
{
	private boolean stillFilter = true;
	private String residual = null;
	private String prev = null;
	
	/**
	 * Default constructor. Takes the underlying Reader.
	 * 
	 * @param reader to filter
	 */
	public CommentFilterReader(Reader reader)
	{
		super(reader);
		this.stillFilter = true;
		this.residual = "";
		this.prev = "";
	}

	/**
	 * Returns true if comment filtering is enabled, false otherwise.
	 * 
	 * @return true if comment filtering is enabled, false otherwise
	 */
	public boolean isFiltering()
	{
		return this.stillFilter;
	}
	
	/**
	 * Sets the value of comment filtering.
	 * 
	 * @param pval value for which to set comment filtering
	 */
	public void setFiltering(boolean pval)
	{
		this.stillFilter = pval;
		if (!pval && residual.length() > 0)
		{
			// Need to flush whatever's left
			localBuffer.append(residual);			
			residual = "";
		}
	}
	
	/**
	 * Returns true if comment filtering should still continue, false otherwise.
	 * 
	 * @return true if comment filtering should still continue, false otherwise.
	 */
	protected boolean stillFiltering()
	{
		return stillFilter;
	}
	
	/**
	 * Reads from the underlying Reader instance and fills the localBuffer. 
	 * Returns true if reading from the underlying Reader is not limited. 
	 * Returns false if the end of the data stream is reached during this read.
     * 
	 * @param minCount minimum number of characters that should be read for this call
	 * @return true if more data can be read, false otherwise
	 * @throws IOException
	 */
	protected boolean readUnderlying(int minCount) throws IOException
	{
        // Read at least the minCount
        int pos = 0;
    	char[] chars = new char[512];
    	int r = -1;
        String line = null;
        boolean inComment = false;        
        minCount += localBuffer.length();
        while (localBuffer.length() < minCount || inComment)
        {
            r = in.read(chars, 0, chars.length);
            pos = 0;

            // Stop if there is no more to read
            if (r == -1 && residual.length() == 0)
                return false;
            else if (r == -1)
            	line = residual;
            else
            	line = residual + new String(chars, 0, r);
            residual = "";            

            // Examine the line and remove any comments.
            while (pos < line.length())
            {
                int ndx = -1;
                if (inComment)
                {
                	if (pos == 0 && prev.length() > 0 && (ndx = (new String(prev + line).indexOf("-->"))) != -1)
                	{
                        pos = ndx + 3 - prev.length();
                    	prev = "";
                        inComment = false;
                	}
                	else if ((ndx = line.indexOf("-->", pos)) != -1)
                    {
                        pos = ndx + 3;
                    	prev = "";
                    	inComment = false;
                    }
                    else
                    {
                    	prev = line.substring(line.length() - 2 < 0?0:line.length() - 2);
                        pos = line.length();
                    }
                }
                else
                {                    
                    if ((ndx = line.indexOf("<!--", pos)) != -1)
                    {
                        localBuffer.append(line.substring(pos, ndx));
                        pos = ndx + 4;
                    	prev = "";
                    	inComment = true;
                    }
                    else
                    {                    	
                    	int endPos = (line.length() - 3 < 0?0:line.length() - 3);
                    	endPos = (pos > endPos?pos:endPos);
                    	endPos = (r == -1?line.length():endPos);
                    	residual = line.substring(endPos);
                    	prev = "";
                    	localBuffer.append(line.substring(pos, endPos));
                        pos = line.length();
                    }
                }
            }
        }
        return true;
    }

}
