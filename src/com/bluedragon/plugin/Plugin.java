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
 * Implementations wishing to add functionality to BlueDragon must override
 * this class.
 */
package com.bluedragon.plugin;

import com.naryx.tagfusion.xmlConfig.xmlCFML;

/**
 * All plugins must implement this interface.  A plugin can register 0, 1 or more tags and functions.  This is performed through the PluginManagerInterface
 * 
 * @author alan
 */
public interface Plugin {

  /**
   * This is called when the plugin manager has created your plugin.  This lets you register tags and functions and set up your functionality
   * 
   * @param manager
   * @param systemParameters
   */
  public void pluginStart( PluginManagerInterface manager, xmlCFML systemParameters );
  
  /**
   * When the engine is being shutdown in an orderly fashion this method will be called. Do not assume it will always be called, as the engine
   * can be stopped in many different ways that would result in everything disappearing (eg power off, operating system kill)
   * 
   * @param manager
   */
  public void pluginStop( PluginManagerInterface manager );
  
  /**
   * @return short name for your plugin
   */
  public String	getPluginName();
  
  
  /**
   * @return the description of your pluing
   */
  public String	getPluginDescription();
  
  
  /**
   * @return version of the plugin
   */
  public String getPluginVersion();
  
}
