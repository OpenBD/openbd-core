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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * FilterReader that filters xml character data. Subclasses can simply implement
 * readUnderlying to take advantage of normal xml filtering. This class provides
 * the framework to handle filtering and to disable filtering at any point.
 * 
 * @author Matt Jacobsen
 *
 */
public abstract class XmlFilterReader extends FilterReader
{
    protected StringBuilder localBuffer = null;
    protected boolean moreToRead = true;
    
	/**
	 * Default constructor. Takes the underlying Reader.
	 * 
	 * @param reader to filter
	 */
	public XmlFilterReader(Reader reader)
	{
		super(reader);
        this.localBuffer = new StringBuilder();
        this.moreToRead = true;
    }

	/**
	 * Returns true if filtering should still continue, false otherwise.
	 * Subclasses need to return true until they decided that filtering
	 * is no longer needed.
	 * 
	 * @return true if filtering should still continue, false otherwise.
	 */
	protected abstract boolean stillFiltering();
	
	/**
	 * Inheritors must implement this method. It reads from the underlying 
	 * Reader instance and fills the localBuffer. Note, implementations should 
	 * not call any public methods in this class or infinite recursion will 
	 * result. Returns true if reading from the underlying Reader is not 
	 * limited. Returns false if the end of the data stream is reached during 
	 * this read.
     * 
	 * @param minCount minimum number of characters that should be read for this call
	 * @return true if more data can be read, false otherwise
	 * @throws IOException
	 */
	protected abstract boolean readUnderlying(int minCount) throws IOException;
	
	/**
	 * Reads len chars from the underlying Reader into the specified 
	 * array using the specified offset. Returns the number of chars 
	 * read or -1 if the end of the stream has been reached.
	 * 
	 * @param cbuf array to fill
	 * @param off offset to begin filling the array at
	 * @param len maximum number of chars to read.
	 * @return number of characters read or -1 if the end has been reached
	 */
	public int read(char[] cbuf, int off, int len) throws IOException
	{
        if (off < 0 || off > cbuf.length || len < 0 || len > cbuf.length)
            throw new IOException("Offset and/or length are out of range.");
        if (off + len > cbuf.length)
            throw new IOException("Offset + length greater than the buffer size.");

        if (stillFiltering())
        {
            if (localBuffer.length() >= len || !moreToRead)
            {
                int read = (len > localBuffer.length() ? localBuffer.length() : len);
                localBuffer.getChars(0, read, cbuf, off);
                localBuffer.delete(0, read);
                if (read == 0)
                	read = -1;
                return read;
            }
            else
            {
                moreToRead = readUnderlying(len - localBuffer.length());
                return this.read(cbuf, off, len);
            }
        }
        else
        {
            int rtnCount = 0;
            int leftInBuf = localBuffer.length();
            if (leftInBuf > 0)
            {
                // Read some from the buffer
                leftInBuf = (leftInBuf > len) ? len : leftInBuf;
                if (leftInBuf > 0)
                {
                    localBuffer.getChars(0, leftInBuf, cbuf, off);
                    localBuffer.delete(0, leftInBuf);
                }
                len -= leftInBuf;
                off += leftInBuf;
                rtnCount += leftInBuf;
            }

            // Read the remaining amount directly
            if (len > 0)
            {
            	int r = in.read(cbuf, off, len);
            	if (r == -1 && rtnCount == 0)
            		rtnCount = r;
            	else if (r != -1)
            		rtnCount += r;
            }
            
            return rtnCount;
        }
	}

	/**
	 * Reads an individual char from the underlying Reader and returns 
	 * the char read or -1 to indicate the end of the stream.
	 * 
	 * @return char read, or -1
	 */
	public int read() throws IOException
	{
        if (localBuffer.length() > 0)
        {
            int rtn = (int)localBuffer.charAt(0);
            localBuffer.deleteCharAt(0);
            return rtn;
        }

        if (stillFiltering() && moreToRead)
        {
            moreToRead = readUnderlying(1);
            return this.read();
        }
        else
        {
            return in.read();
        }
	}

	/**
	 * Reads chars from the underlying Reader into the specified array. 
	 * Returns the number of chars read or -1 if the end of the stream
	 * has been reached.
	 * 
	 * @param cbuf array to fill
	 * @return number of chars read, or -1
	 */
	public int read(char[] cbuf) throws IOException
	{
		return this.read(cbuf, 0, cbuf.length);
	}
	
	/**
	 * Attempts to read characters into the specified character buffer. 
	 * The buffer is used as a repository of characters as-is: the only 
	 * changes made are the results of a put operation. No flipping or 
	 * rewinding of the buffer is performed
	 * 
	 * @param target buffer to read characters into 
	 * @return number of characters added to the buffer, or -1 if this 
	 * source of characters is at its end 
	 */
	public int read(CharBuffer target) throws IOException
	{
		char[] tmp = new char[512];
		int read = read(tmp);
		target.put(tmp, 0, read);
		return read;
	}
	
	/**
	 * Tell whether this stream supports the mark() operation. Always 
	 * returns false.
	 * 
	 * @return false, always.
	 */
	public boolean markSupported()
	{
		return false;
	}
	
	/**
	 * Mark the present position in the stream. This call is ignored.
	 * 
	 * @param readAheadLimit limit on the number of characters that may be 
	 * 			read while still preserving the mark. After reading this 
	 * 			many characters, attempting to reset the stream may fail.
	 */
	public void mark(int readAheadLimit) throws IOException
	{
		// Ignore
	}
	
	/**
	 * Skip characters.
	 * 
	 * @param n number of characters to skip
	 * @return number of characters actually skipped.
	 * @throws IOException
	 */
	public long skip(long n) throws IOException
	{
        if (stillFiltering())
        {
            if (localBuffer.length() >= n || !moreToRead)
            {
                long skip = (n > localBuffer.length() ? localBuffer.length() : n);
                localBuffer.delete(0, (int)skip);
                return skip;
            }
            else
            {
                moreToRead = readUnderlying((int)(n - localBuffer.length()));
                return this.skip(n);
            }
        }
        else
        {
            long rtnCount = 0;
            long leftInBuf = localBuffer.length();
            if (leftInBuf > 0)
            {
                // Skip some out of the buffer
                leftInBuf = (leftInBuf > n) ? n : leftInBuf;
                if (leftInBuf > 0)
                    localBuffer.delete(0, (int)leftInBuf);
                n -= leftInBuf;
                rtnCount += leftInBuf;
            }

            // Skip the remaining amount directly
            if (n > 0)
                rtnCount += in.skip(n);
            return rtnCount;
        }
    }
	
	/**
	 * Reset the stream.
	 * 
	 * @throws IOException
	 */
	public void reset() throws IOException
	{
		localBuffer.setLength(0);
		super.reset();
	}
	
	/**
	 * Close the stream.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException
	{
		localBuffer.setLength(0);
		super.close();
	}

}
