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

/**
 * 
 */
package com.naryx.tagfusion.util;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/**
 * @author Matt Jacobsen
 * A PrintWriter that uses the decorator pattern. It tracks instances
 * that it is told it is printing in a List. It is simply a way for
 * printing routines to avoid circular (and therefore infinite)
 * printing when printing values and following references. 
 */
public class InstanceTrackingPrintWriter extends PrintWriter 
{
	private List printed = null;
	
	/**
	 * Default constructor.
	 * 
	 * @param out Writer to write to.
	 */
	public InstanceTrackingPrintWriter(Writer out) 
	{
		super(out);
		init();
	}

	/**
	 * Alternate constructor.
	 * 
	 * @param out Writer to write to.
	 * @param autoFlush true if we autoflush, false otherwise.
	 */
	public InstanceTrackingPrintWriter(Writer out, boolean autoFlush) 
	{
		super(out, autoFlush);
		init();
	}

	/**
	 * Alternate constructor.
	 * 
	 * @param out OutputStream to write to.
	 * @param autoFlush true if we autoflush, false otherwise.
	 */
	public InstanceTrackingPrintWriter(OutputStream out) 
	{
		super(out);
		init();
	}

	/**
	 * Alternate constructor.
	 * 
	 * @param out OutputStream to write to.
	 * @param autoFlush true if we autoflush, false otherwise.
	 */
	public InstanceTrackingPrintWriter(OutputStream out, boolean autoFlush) 
	{
		super(out, autoFlush);
		init();
	}
	
	/**
	 * Initialize this InstanceTrackingPrintWriter.
	 *
	 */
	private void init()
	{
		this.printed = new LinkedList();
	}

	/**
	 * Returns true if the specified instance has already
	 * been printed (as recorded by instancePrinted). 
	 * Returns false otherwise.
	 * 
	 * @param o Object instance to check.
	 * @return true if already printed, false otherwise.
	 */
	public boolean isInstancePrinted(Object o)
	{
		if (o == null)
			return false;
		try
		{
			if (o instanceof cfJavaObjectData && ((cfJavaObjectData)o).getInstance() != null)
				o = ((cfJavaObjectData)o).getInstance();
		}
		catch (cfmRunTimeException ex)
		{
			com.nary.Debug.printStackTrace(ex);
		}
		return this.printed.contains(o);
	}
	
	/**
	 * Adds the specified Object to the List of instances
	 * this InstanceTrackingPrintWriter has printed.
	 * 
	 * @param o Object instance to keep track of.
	 */
	public void instancePrinted(Object o)
	{
		if (o == null)
			return;
		try
		{
			if (o instanceof cfJavaObjectData && ((cfJavaObjectData)o).getInstance() != null)
				o = ((cfJavaObjectData)o).getInstance();
		}
		catch (cfmRunTimeException ex)
		{
			com.nary.Debug.printStackTrace(ex);
		}
		this.printed.add(o);
	}
}
