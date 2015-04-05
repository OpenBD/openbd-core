/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  http://openbd.org/
 *  $Id: Platform.java 2327 2013-02-10 22:26:44Z alan $
 */

package com.bluedragon.platform;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.aw20.util.SystemClockEvent;

import com.naryx.tagfusion.cfm.sql.cfQueryImplInterface;
import com.naryx.tagfusion.util.TagElement;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public interface Platform {

	public void init(ServletConfig config) throws ServletException;
	
	public void destroy();
	
	public void engineAdminUpdate();
	
	public void log(String l);
	public FileIO	getFileIO();
	
	public SmtpInterface getSmtp();
	
	public boolean	hasNetworkAccess();
	
	public void registerTags(Hashtable<String, TagElement> tagElements);
	public void registerFunctions( Map<String, String> functions );
	public void initialiseTagSystem(xmlCFML configFile);
	public void initialiseQuerySystem(xmlCFML configFile, Map<String, cfQueryImplInterface> queryImplementations);
	public cfQueryImplInterface getDefaultQuerySystem(Map<String, cfQueryImplInterface> queryImplementations);
	
	public void registerScriptExtensions();

	public Object loadClass(String classpath);
	
	public boolean compileOutput(String outDir, ByteArrayOutputStream javacOut) throws IOException;

	// Alarm functions
	public void timerRunOnce( SystemClockEvent handler, int minuteleap );
	public void timerSetListenerMinute( SystemClockEvent handler, int minuteleap );
	public void timerSetListenerMinute( SystemClockEvent handler );
	public void timerSetListenerHourly( SystemClockEvent handler );
	public void timerCancel( SystemClockEvent handler );
}
