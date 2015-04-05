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

package com.naryx.tagfusion.servlet.jsp;

import java.io.IOException;

import com.naryx.tagfusion.cfm.engine.cfSession;

/**
 * This class is modelled to be a subclass of javax.servlet.jsp.JspWriter,
 * but it's not because we don't really want to support all of the abstract
 * methods defined by JspWriter.
 */

public class CfmlJspWriter //extends JspWriter
{
	private static final String NEWLINE = System.getProperty("line.separator");
	
	private cfSession _session;
	
	public CfmlJspWriter( cfSession session ) {
		_session = session;
	}
	
	/*
	 * clear
	 * 
	 * Clear the contents of the buffer. If the buffer has been already been flushed then
	 * the clear operation shall throw an IOException to signal the fact that some data
	 * has already been irrevocably written to the client response stream. 
	 */
	public void clear() throws IOException
	{
		// Need to call resetBuffer() instead of reset() on the response object since
		// reset() will also clear the status code and headers.
		try
		{
			_session.RES.resetBuffer();
		}
		catch ( IllegalStateException e )
		{
			// resetBuffer() will throw an IllegalStateException if the buffer has
			// already been flushed so rethrow it as an IOException
			throw new IOException( e.getMessage() );
		}
	}
	
	/*
	 * clearBuffer
	 * 
	 * Clears the current contents of the buffer. Unlike clear(), this method will not
	 * throw an IOException if the buffer has already been flushed. It merely clears the
	 * current content of the buffer and returns. 
	 */
	public void clearBuffer()
	{
		// Need to call resetBuffer() instead of reset() on the response object since
		// reset() will also clear the status code and headers.
		try
		{
			_session.RES.resetBuffer();
		}
		catch ( IllegalStateException e )
		{
			// resetBuffer() will throw an IllegalStateException if the buffer has
			// already been flushed so just ignore it
		}
	}
	
	/*
	 * close
	 */
//	public void close()
//	{
//	}
	
	/*
	 * flush
	 */
	public void flush()
	{
		_session.RES.flush();
	}
	
	/*
	 * newLine
	 * 
	 * Write a line separator. The line separator string is defined by the system
	 * property line.separator, and is not necessarily a single newline ('\n') character.
	 */
	public void newLine()
	{
		_session.write(NEWLINE);
	}
	
	/*
	 * print
	 * 
	 * Print a boolean value. The string produced by String.valueOf(boolean) is written
	 * to the JspWriter's buffer or, if no buffer is used, directly to the underlying
	 * writer. 
	 */
	public void print(boolean b)
	{
		_session.write(String.valueOf(b));
	}
	
	/*
	 * print
	 * 
	 * Print a character. The character is written to the JspWriter's buffer or, if no
	 * buffer is used, directly to the underlying writer.
	 */
	public void print(char c)
	{
		_session.write(new char[]{c});
	}
	
	/*
	 * print
	 * 
	 * Print an integer. The string produced by String.valueOf(int) is written
	 * to the JspWriter's buffer or, if no buffer is used, directly to the underlying
	 * writer. 
	 */
	public void print(int i)
	{
		_session.write(String.valueOf(i));
	}
	
	/*
	 * print
	 * 
	 * Print a long integer. The string produced by String.valueOf(long) is written
	 * to the JspWriter's buffer or, if no buffer is used, directly to the underlying
	 * writer. 
	 */
	public void print(long l)
	{
		_session.write(String.valueOf(l));
	}
	
	/*
	 * print
	 * 
	 * Print a floating-point number. The string produced by String.valueOf(float) is written
	 * to the JspWriter's buffer or, if no buffer is used, directly to the underlying
	 * writer. 
	 */
	public void print(float f)
	{
		_session.write(String.valueOf(f));
	}
	
	/*
	 * print
	 * 
	 * Print a double-precision floating-point number. The string produced by String.valueOf(double) is written
	 * to the JspWriter's buffer or, if no buffer is used, directly to the underlying
	 * writer. 
	 */
	public void print(double d)
	{
		_session.write(String.valueOf(d));
	}
	
	/*
	 * print
	 * 
	 * Print an array of characters. The characters are written to the JspWriter's buffer
	 * or, if no buffer is used, directly to the underlying writer. 
	 */
	public void print(char[] s)
	{
		if ( s == null )
			throw new NullPointerException();
		
		_session.write(s);
	}
	
	/*
	 * print
	 * 
	 * Print a string. If the argument is null then the string "null" is printed.
	 * Otherwise, the string's characters are written to the JspWriter's buffer or,
	 * if no buffer is used, directly to the underlying writer. 
	 */
	public void print(String s)
	{
		if ( s == null )
			_session.write("null");
		else
			_session.write(s);
	}
	
	/*
	 * print
	 * 
	 * Print an object. The string produced by the String.valueOf(Object) method is
	 * written to the JspWriter's buffer or, if no buffer is used, directly to the
	 * underlying writer. 
	 */
	public void print(Object obj)
	{
		_session.write(String.valueOf(obj));
	}
	
	/*
	 * println
	 * 
	 * Terminate the current line by writing the line separator string. The line
	 * separator string is defined by the system property line.separator, and is not
	 * necessarily a single newline character ('\n'). 
	 */
	public void println()
	{
		newLine();
	}
	
	/*
	 * println
	 * 
	 * Print a boolean value and then terminate the line. This method behaves as though
	 * it invokes print(boolean) and then println().
	 */
	public void println(boolean b)
	{
		print(b);
		newLine();
	}
	
	/*
	 * println
	 */
	public void println(char c)
	{
		print(c);
		newLine();
	}
	
	/*
	 * println
	 */
	public void println(int i)
	{
		print(i);
		newLine();
	}
	
	/*
	 * println
	 */
	public void println(long l)
	{
		print(l);
		newLine();
	}
	
	/*
	 * println
	 */
	public void println(float f)
	{
		print(f);
		newLine();
	}
	
	/*
	 * println
	 */
	public void println(double d)
	{
		print(d);
		newLine();
	}
	
	/*
	 * println
	 */
	public void println(char[] s)
	{
		print(s);
		newLine();
	}
	
	/*
	 * println
	 */
	public void println(String s)
	{
		print(s);
		newLine();
	}
	
	/*
	 * println
	 */
	public void println(Object obj)
	{
		print(obj);
		newLine();
	}

	/*
	 * getString
	 * 
	 * This method returns all of the current output as a string.
	 */
	public String getString()
	{
		return _session.RES.getString();
	}
}


