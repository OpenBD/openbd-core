/* 
 *  Copyright (C) 2000 - 2011 AW2.0 Ltd
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
 *  
 *  $Id: LoginExtension.java 1765 2011-11-04 07:55:52Z alan $
 */

package org.aw20.plugin.login;

import java.io.File;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;

import com.bluedragon.plugin.Plugin;
import com.bluedragon.plugin.PluginManager;
import com.bluedragon.plugin.PluginManagerInterface;
import com.bluedragon.plugin.RequestListener;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.xmlConfig.xmlCFML;


public class LoginExtension implements Plugin, RequestListener {

	@Override	public void pluginStop(PluginManagerInterface manager) {}
	@Override public void requestEnd(cfSession session) {}
	@Override public void requestBadFileException(cfmBadFileException bfException, cfSession session) {}
	@Override	public void requestRuntimeException(cfmRunTimeException cfException, cfSession session) {}

	@Override	public String getPluginDescription() {
		return getPluginName();
	}

	@Override public String getPluginName() {
		return "LoginExtension";
	}

	@Override public String getPluginVersion() {
		return "1.2011.11.4";
	}

	private static Factory<org.apache.shiro.mgt.SecurityManager> factory;
	private static String userFormField = null, passwordFormField = null, returnFormField = null;
	
	@Override	public void pluginStart(PluginManagerInterface manager, xmlCFML systemParameters) {
		manager.addRequestListener( this );
		
		userFormField 		= systemParameters.getString("server.plugin.login.userfield");
		if ( userFormField == null || userFormField.length() == 0 ){
			manager.log("[LoginPlugin] <server>.<plugin>.<login>.<userfield>: no userfield specified");
			userFormField = null;
			return;	
		}

		passwordFormField = systemParameters.getString("server.plugin.login.passwordfield");
		if ( passwordFormField == null || passwordFormField.length() == 0 ){
			manager.log("[LoginPlugin] <server>.<plugin>.<login>.<passwordfield>: no passwordfield specified");
			userFormField = null;
			return;	
		}
		
		returnFormField = systemParameters.getString("server.plugin.login.returnfield");
		if ( returnFormField == null || returnFormField.length() == 0 ){
			manager.log("[LoginPlugin] <server>.<plugin>.<login>.<returnfield>: no returnfield specified");
			userFormField = null;
			return;	
		}
		
		manager.log("[LoginPlugin] userfield=" + userFormField + "; passwordfield=" + passwordFormField + "; returnfield=" + returnFormField );
		
		String iniFile = systemParameters.getString("server.plugin.login.ini");
		if ( iniFile == null || iniFile.length() == 0 ){
			manager.log("[LoginPlugin] <server>.<plugin>.<login>.<ini>: No INI path given");
			userFormField = null;
			return;
		}
		
		File iniF = new File(iniFile);
		if ( !iniF.exists() ){
			manager.log("[LoginPlugin] <server>.<plugin>.<login>.<ini>: " + iniF + " was not found");
			userFormField = null;
			return;
		}
		
		manager.log("[LoginPlugin] iniFile=" + iniFile );
		
		// Setup the factory
		factory = new IniSecurityManagerFactory(iniFile);
    SecurityUtils.setSecurityManager(factory.getInstance());
	}

	@Override public void requestStart(cfSession session) {
		
		// the plugin may be disabled
		if ( userFormField == null )
			return;
		
		cfStructData formData = (cfStructData)session.getData("form");
		
		if ( formData.containsKey(userFormField) && formData.containsKey(passwordFormField) ){
			try {
				String username = formData.getData(userFormField).getString();
				String password = formData.getData(passwordFormField).getString();
				
				// Remove the fields from the form post
				formData.setData(passwordFormField, new cfStringData("******") );
				
				// Attempt to login them in
				Subject user = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
				
        user.login(token);
        
        if ( user.isAuthenticated() ){
        	PrincipalCollection pc = user.getPrincipals();
        	formData.setData(returnFormField, new cfStringData( pc.asSet().toString() ) );
        	user.logout();
        }else{
        	formData.setData(returnFormField, new cfStringData("[failed]") );
        }
        
			} catch (Exception e) {
				formData.setData(returnFormField, new cfStringData("[exception]" + e.getMessage()) );
				PluginManager.getPlugInManager().log("[LoginExtension] " + e.getMessage() );
			}
			
		}
		
	}
}
