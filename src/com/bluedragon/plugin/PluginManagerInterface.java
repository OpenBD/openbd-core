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

/*
 * Created on 03-Feb-2005
 *
 * This is the interface that plugin tags/functions will use to interact
 * with the core BlueDragon engine.
 * 
 */
package com.bluedragon.plugin;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.engineListener;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

/**
 * This is the main plugin manager that lets you interact with the core engine.
 *
 */
public interface PluginManagerInterface {

	
	/**
	 * Gets a handle to a CFC based on the current session.  If the 'cfdata' is a CFC reference (given from the core engine) then
	 * this reference is used.  Otherwise, the 'cfdata' is converted to a string, and a new CFC instance will be created and returned.
	 * 
	 * @param session
	 * @param cfdata
	 * @return the CFC object 
	 * @throws Exception
	 */
	public ObjectCFC createCFC( cfSession session, cfData cfdata ) throws Exception;
	
	
	/**
	 * Creates a new CFC based on the Session object passed in.  The CFC creation goes through the same rules as if you had called
	 * the "CreateObject('component', cfcName)" directly within CFML page, looking up the current directory then going to the 
	 * custom-tag directory accordingly.
	 * 
	 * @param session
	 * @param cfcName
	 * @return the CFC object 
	 * @throws Exception
	 */
	public ObjectCFC createCFC( cfSession session, String cfcName ) throws Exception;
	
	
	/**
	 * Depending on your plugin, you may not be executing in a traditional 'page' request.  Under these circumstances you require
	 * a dummy session to be created that lets the rest of the tags execute.  This dummy session can be used to create CFC's using 
	 * the methods in this class.  The root context is assumed to be the raw page.
	 * 
	 * @return a blank session
	 */
	public cfSession createBlankSession();
	
	
  /**
   * Registers a tag into the core engine.  If the tag already already exists then it is replaced with this one.
   * 
   * @param tagName  the name of the name
   * @param tagClass the full class path of the class to be registered
   */
  public void registerTag( String tagName, String tagClass );
  
  
  /**
   * Registers a new function with the core engine.  If the expression already exists then it is replaced with this one.
   * 
   * @param functionName 	the name of the function
   * @param functionClass the full class path of the class to be registered
   */
  public void registerFunction( String functionName, String functionClass );
  
  /**
   * Registers the enginer listener allowing you get a hook when the engine shuts down or an update is made to the admin
   * 
   * @param _new the listener
   */
  public void addEngineListener(engineListener _new);
  
  
  /**
   * Stops the given listener from receiving events
   * 
   * @param _new
   */
  public void removeEngineListener(engineListener _new);
  
  /**
   * Registers a request listener to the core engine.  This lets you receive events regarding incoming requests.
   * 
   * @param _new
   */
  public void addRequestListener(RequestListener _new);

  
  /**
   * Registers a new language to be invoked when CFSCRIPT lang="" property is hit
   * @param lang
   * @param cfscriptClass
   */
  public void registerLangauge( String lang, String cfscriptClass );
  
  
  /**
   * Removes the given request listener
   * 
   * @param _new
   */
  public void removeRequestListener(RequestListener _new);
 
  /**
   * Starts the core engine tracking manager to collect stats on incoming requests; total hits, average hits etc
   */
  public void startRequestStats();
  
  /**
   * Stops the core engine from registering stats
   */
  public void stopRequestStats();
  
  /**
   * Request Stats collection must be enabled; -1 returned otherwise
   * 
   * @return total requests serviced 
   */
  public long getStatsTotalRequests();
  
  /**
   * Request Stats collection must be enabled; -1 returned otherwise
   * 
   * @return average time in milliseconds for each request
   */
  public long getStatsAverageRequestTimeMS();
  
  /**
   * Request Stats collection must be enabled; -1 returned otherwise
   * 
   * @return shortest request time in milliseconds
   */
  public long getStatsShortestRequestTimeMS();
  
  /**
   * Request Stats collection must be enabled; -1 returned otherwise
   * 
   * @return longest request time in milliseconds
   */
  public long getStatsLongestRequestTimeMS();
  
  /**
   * Request Stats collection must be enabled; -1 returned otherwise
   * 
   * @return number of requests currently being processed
   */
  public long getStatsActiveRequests();  
  
  
  /**
   * Prints the given log string out to the system log for the core engine
   * 
   * @param log
   */
  public void log( String log );
  
  
  /**
   * Used for loading the plugin 
   * @param pluginClass
   * @param systemParameters
   */
  public void loadPlugIn( String pluginClass, xmlCFML systemParameters );
}
