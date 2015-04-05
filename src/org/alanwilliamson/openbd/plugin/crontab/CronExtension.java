/*
 *  Copyright (C) 2000-2015 aw2.0 LTD
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
 *  http://www.openbd.org/
 *  $Id: CronExtension.java 2538 2015-04-05 13:50:07Z alan $
 */
package org.alanwilliamson.openbd.plugin.crontab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;

import com.bluedragon.plugin.Plugin;
import com.bluedragon.plugin.PluginManager;
import com.bluedragon.plugin.PluginManagerInterface;
import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class CronExtension extends Thread implements Plugin {

	private static File			rootCronDir;
	private static String		uriRootPath = null;
	private static boolean 	bEnabled = false;

	private static String	CRON_1MIN			= "cron.1min";
	private static String	CRON_5MIN			= "cron.5min";
	private static String	CRON_15MIN		= "cron.15min";
	private static String	CRON_HOUR			= "cron.hourly";
	private static String	CRON_DAILY		= "cron.daily";
	private static String	CRON_WEEKLY		= "cron.weekly";
	private static String	CRON_MONTHLY	= "cron.monthly";
	
	private int currentMonth = -1; 
	private int currentDay = -1;
	private int	currentDayOfWeek = -1;
	private int currentHour = -1;
	private boolean bRunning = true;
	
	public CronExtension(){
		super("CronPluginThread");
	}
	
	@Override
	public String getPluginDescription() {
		return getPluginName();
	}

	@Override
	public String getPluginName() {
		return "CronTab";
	}

	public String getPluginVersion() {
		return "1.2011.11.4";
	}
	

	public static void	setRootPath( String _uriRootPath ){
		if ( _uriRootPath == null )
			return;
		
		if ( !_uriRootPath.startsWith("/") )
			_uriRootPath = "/" + _uriRootPath;
		
		if ( !_uriRootPath.endsWith("/") )
			_uriRootPath = _uriRootPath + "/";
		
		uriRootPath = _uriRootPath;
		bEnabled		= true;
		setupUriDirectory();
		
		savePropeties();
	}
	
	
	private static void setupUriDirectory(){
		try {
			File	uriObject	= cfEngine.getResolvedFile( uriRootPath );
			
			if ( !uriObject.exists() ){
				uriObject.mkdirs();
			}
			
			createSubFolders( uriObject );
		} catch (Exception e) {
			PluginManager.getPlugInManager().log( "cf.cron : " + e.getMessage() );
		}
	}
	
	
	public static void setEnable(boolean b) {
		bEnabled	= b;
		savePropeties();
	}

	
	public void pluginStart(PluginManagerInterface manager, xmlCFML systemParameters) {
		manager.registerFunction("cronsetdirectory", 	"org.alanwilliamson.openbd.plugin.crontab.CronSetDirectory" );
		manager.registerFunction("cronenable", 				"org.alanwilliamson.openbd.plugin.crontab.CronEnable" );
		
		try {
			// Setup the times
			Calendar thisDate = new GregorianCalendar();
			currentMonth			= thisDate.get( Calendar.MONTH );
			currentHour				= thisDate.get( Calendar.HOUR_OF_DAY );
			currentDayOfWeek 	= thisDate.get( Calendar.DAY_OF_WEEK );
			currentDay				= thisDate.get( Calendar.DAY_OF_MONTH );
			
			rootCronDir = FileUtils.checkAndCreateDirectory( cfEngine.thisPlatform.getFileIO().getWorkingDirectory(), "plugin-cfcron", false );
			createSubFolders( rootCronDir );

			// Load the file
			Properties	prop	= new Properties();
			InputStream is = null;
			try {
				is = new FileInputStream( new File(rootCronDir,"cron.ini") );
				prop.load( is );
				uriRootPath	= prop.getProperty("path");
				
				if ( uriRootPath == null ){
					uriRootPath	= "/cron";
				} else {
					
					if ( !uriRootPath.startsWith("/") )
						uriRootPath = "/" + uriRootPath;
					
					if ( !uriRootPath.endsWith("/") )
						uriRootPath = uriRootPath + "/";
					
				}
				
				setupUriDirectory();
				
				String en	= prop.getProperty("enable", "0");
				if ( en.equals("1") )
					bEnabled = true;
				
			} catch (Exception e) {
				uriRootPath	= null;
			} finally{
				if ( is != null ){
					try {	is.close();	} catch (IOException e) {}
				}
			}
			
			start();
		} catch (Exception e) {
			PluginManager.getPlugInManager().log( getPluginName() + " : " + e.getMessage() );
		}
	}

	@SuppressWarnings("deprecation")
	private static void savePropeties(){
		// Save the file
		Properties	prop	= new Properties();
		prop.setProperty("path", 		uriRootPath);
		prop.setProperty("enable", 	bEnabled ? "1" : "0" );
		OutputStream os = null;
		try {
			os = new FileOutputStream( new File(rootCronDir,"cron.ini") );
			prop.save( os, "" );
		} catch (Exception e) {} 
		finally{
			if ( os != null ){
				try {	os.close();	} catch (IOException e) {}
			}
		}
	}

	
	private static void createSubFolders( File rootDir ) throws Exception {
		FileUtils.checkAndCreateDirectory( rootDir, CRON_1MIN, false );
		FileUtils.checkAndCreateDirectory( rootDir, CRON_5MIN, false );
		FileUtils.checkAndCreateDirectory( rootDir, CRON_15MIN, false );
		FileUtils.checkAndCreateDirectory( rootDir, CRON_HOUR, false );
		FileUtils.checkAndCreateDirectory( rootDir, CRON_DAILY, false );
		FileUtils.checkAndCreateDirectory( rootDir, CRON_WEEKLY, false );
		FileUtils.checkAndCreateDirectory( rootDir, CRON_MONTHLY, false );
	}
	
	
	public void pluginStop(PluginManagerInterface manager) {
		bRunning = false;
	}

	
	public void run(){
		
		while (bRunning){
			try {
				sleep( 60000 );
			} catch (InterruptedException e) {
				break;
			}
			
			if ( !bEnabled || uriRootPath == null )
				continue;
			
			Calendar thisDate = new GregorianCalendar();

			new Thread("CronPlugin_" + currentMonth + "-1min" ){
				public void run(){ try{ processCronFiles( CRON_1MIN ); }catch(Throwable ignore){} }
			}.start();

			/* Handle the 5min change */
			if ( thisDate.get( Calendar.MINUTE )%5 == 0 ){
				new Thread("CronPlugin_" + currentMonth + "-5mins" ){
					public void run(){ try{ processCronFiles( CRON_5MIN ); }catch(Throwable ignore){} }
				}.start();
			}
			
			/* Handle the 15min change */
			if ( thisDate.get( Calendar.MINUTE )%15 == 0 ){
				new Thread("CronPlugin_" + currentMonth + "-15mins" ){
					public void run(){ try{ processCronFiles( CRON_15MIN ); }catch(Throwable ignore){} }
				}.start();
			}
						
			/* Handle the Monthly change */
			if ( thisDate.get( Calendar.MONTH ) != currentMonth ){
				currentMonth = thisDate.get( Calendar.MONTH );
				
				new Thread("CronPlugin_" + currentMonth + "-" + currentMonth ){
					public void run(){ try{ processCronFiles( CRON_MONTHLY ); }catch(Throwable ignore){} }
				}.start();
				
			}
			
			/* Handle the HOURLY change */
			if ( thisDate.get( Calendar.HOUR_OF_DAY ) != currentHour ){
				currentHour = thisDate.get( Calendar.HOUR_OF_DAY );
				
				new Thread("CronPlugin_" + currentMonth + "-" + currentDay + "_" + currentHour ){
					public void run(){ try{ processCronFiles( CRON_HOUR ); }catch(Throwable ignore){} }
				}.start();
				
			}
			
			/* Handle the WEEKLY change */
			if ( thisDate.get( Calendar.DAY_OF_WEEK ) != currentDayOfWeek && thisDate.get( Calendar.DAY_OF_WEEK ) == Calendar.SUNDAY ){
				currentDayOfWeek = thisDate.get( Calendar.DAY_OF_WEEK );
				
				new Thread("CronPlugin_" + currentMonth + "-" + currentDay + "_" + currentHour + "_" + currentDayOfWeek ){
					public void run(){ try{ processCronFiles( CRON_WEEKLY ); }catch(Throwable ignore){} }
				}.start();
				
			}
			
			/* Handle the DAY change */
			if ( thisDate.get( Calendar.DAY_OF_MONTH ) != currentDay ){
				currentDay	= thisDate.get( Calendar.DAY_OF_MONTH );
				
				new Thread("CronPlugin_" + currentMonth + "-" + currentDay ){
					public void run(){ try{ processCronFiles( CRON_DAILY ); }catch(Throwable ignore){} }
				}.start();
				
			}
		}
		
		PluginManager.getPlugInManager().log( getName() + " scheduler stopped" );
	}
	
	
	
	private void processCronFiles( String directory ){
		if ( uriRootPath == null )
			return;
		
		try {
			File	uriDirObject	= cfEngine.getResolvedFile( uriRootPath + directory );
			
			String[]	childFiles = uriDirObject.list();
			if ( childFiles == null || childFiles.length == 0 )
				return;
			
			
			for ( int x=0; x < childFiles.length; x++ ){
				File fName	= new File( uriDirObject, childFiles[x] );
				String filename = fName.getName();
				
				if ( fName.isFile() && filename.endsWith(".cfm") && !filename.equals("Application.cfm") ){
					String uri	= uriRootPath + directory + "/" + filename;

					try {
						new ExecutePage( new File( rootCronDir, directory), uri ).service();
					}catch (Exception e) {}

				}
			}
			
		} catch (Exception e) {
			PluginManager.getPlugInManager().log(e.getMessage());
		}
	}

}