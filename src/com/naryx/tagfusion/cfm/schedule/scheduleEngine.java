/*
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  $Id: $
 */

package com.naryx.tagfusion.cfm.schedule;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.nary.io.FileUtils;
import com.nary.util.LogFile;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class scheduleEngine extends Thread {

	private static scheduleEngine engine = null;
	private File taskDirectory;
	private Hashtable<String, scheduleTask> taskList;
	private File outFile;
	private boolean bPauseSchedule = false;

	// -------------------------

	private scheduleEngine(xmlCFML configFile) {
		super("scheduleEngine");

		try {
			taskDirectory = FileUtils.checkAndCreateDirectory( cfEngine.thisPlatform.getFileIO().getWorkingDirectory(), "cfschedule", false );
		} catch (Exception e) {
			cfEngine.log("Failed to create the ScheduleEngine: " + e.getMessage() );
			return;
		}

		taskList 	= new Hashtable<String, scheduleTask>(10);
		outFile 	= new File(taskDirectory, "schedule.log");
		setDaemon(false);
		setPriority(MIN_PRIORITY);
	}

	public static void pause( boolean bPause ){
		engine.bPauseSchedule	= bPause;
	}
	
	public void run(){
		cfEngine.log("ScheduleEngine: Started. Initial Tasks=" + taskList.size() );
		
		while (cfEngine.bEngineActive){
			try {
				sleep(30000);
			} catch (InterruptedException e) {continue;}

			if ( bPauseSchedule )
				continue;
			
			// Run around the tasks to see who is waiting for running
			try{
				
				Enumeration<scheduleTask>	e	= taskList.elements();
				while ( e.hasMoreElements() ){
					scheduleTask	sT	= e.nextElement();
					long cT	= System.currentTimeMillis();
					long nT	= sT.getNextRunTime();
					
					if ( !sT.isPause() && nT > 0 && nT <= cT ){
						sT.runAndSchedule();
					}
				}
				
			}catch(Exception e){
				LogFile.println(engine.outFile, "scheduleEngine.run(): " + e.getMessage() );
			}
		}
	
		cfEngine.log("ScheduleEngine: Shutdown");
	}
	
	public static Enumeration<String> getTasks() {
		return engine.taskList.keys();
	}

	public static synchronized void init(xmlCFML configFile) {
		if ( engine == null ){
			engine = new scheduleEngine(configFile);

			// The loadTasks() method relies on the engine member being initialized so it cannot
			// be called from within the scheduleEngine constructor.
			LogFile.println(engine.outFile, "scheduleEngine started");

			engine.loadTasks(configFile);
			engine.start();
			
			LogFile.println(engine.outFile, "Active Tasks=" + engine.taskList.size() + "; Directory=" + engine.taskDirectory);
			cfEngine.log("scheduleEngine started; logfile=" + engine.outFile.toString() );
		}
	}

	public static void updateTask(String taskName, scheduleTask task) {
		engine._updateTask(taskName.toLowerCase(), task);
	}

	public static void deleteTask(String taskName) {
		engine._deleteTask(taskName.toLowerCase());
	}

	public static void runTask(String taskName) {
		engine._runTask(taskName.toLowerCase());
	}

	public static void log(String _line) {
		LogFile.println(engine.outFile, _line);
	}

	public static scheduleTask getTask(String taskName) {
		return engine._getTask(taskName.toLowerCase());
	}

	// -------------------------

	private void _updateTask(String taskName, scheduleTask task) {
		if (taskList.containsKey(taskName))
			taskList.remove(taskName);

		task.setName(taskName.toLowerCase());
		taskList.put(taskName, task);
		saveTask(task);

		LogFile.println(outFile, "scheduleEngine: UpdateTask: " + taskName );
		LogFile.println(outFile, task.toString());
		task.schedule();
	}

	private void _deleteTask(String taskName) {
		if (taskList.containsKey(taskName)) {
			// Remove task from task list
			taskList.remove(taskName);

			// Remove task from XML config data
			String taskKey = "server.cfschedule.task[" + taskName.toLowerCase() + "]";
			cfEngine.getConfig().removeData(taskKey);
			try {
				cfEngine.writeXmlFile(cfEngine.getConfig(), false);
			} catch (Exception e) {
				cfEngine.log("Delete task error: " + e.getMessage());
			}

			LogFile.println(outFile, "scheduleEngine: TaskRemoved: " + taskName );
		}
	}

	private scheduleTask _getTask(String taskName) {
		return taskList.get(taskName);
	}

	private void _runTask(String taskName) {
		if (taskList.containsKey(taskName))
			(taskList.get(taskName)).run();
		else
			LogFile.println(outFile, "scheduleEngine: RunTask: " + taskName );
	}

	private void loadTasks(xmlCFML configFile) {

		scheduleTask task;

		// Read in the tasks from bluedragon.xml
		Vector elements = configFile.getKeys("server.cfschedule.task[]");
		if (elements != null) {
			Enumeration E = elements.elements();
			while (E.hasMoreElements()) {
				String taskKey = (String) E.nextElement();
				task = scheduleTask.getTaskConfig(configFile, taskKey);
				_updateTask(task.getName(), task);
			}
		}

		// Now read in the old tasks from disk and copy them to the XML config object
		String listOfFile[] = taskDirectory.list(new fileFilter());

		boolean writeToFile = false;
		for (int x = 0; x < listOfFile.length; x++) {
			File thisFile = new File(taskDirectory, listOfFile[x]);

			task = (scheduleTask) com.nary.Debug.loadClass(thisFile.toString());
			if (task != null) {
				_updateTask(task.getName(), task);

				// write the task to the XML config object and delete the file
				task.writeToConfig(configFile);
				thisFile.delete();
				writeToFile = true;
			}
		}

		// If we converted some old tasks then save them to bluedragon.xml
		if (writeToFile) {
			try {
				cfEngine.writeXmlFile(configFile, false);
			} catch (Exception e) {
				cfEngine.log("Save tasks to new format error: " + e.getMessage());
			}
		}
	}

	private void saveTask(scheduleTask task) {
		// Save the task to the XML config data
		task.writeToConfig(cfEngine.getConfig());
		try {
			cfEngine.writeXmlFile(cfEngine.getConfig(), false);
		} catch (Exception e) {
			cfEngine.log("Save task error: " + e.getMessage());
		}
	}

	// -------------------------

	class fileFilter implements FilenameFilter {
		public fileFilter() {
		}

		public boolean accept(File dir, String name) {
			if (name.indexOf(".task") != -1)
				return true;
			else
				return false;
		}
	}

	
	public static void pauseTask(String taskname ) {
		taskname	= taskname.toLowerCase();
		scheduleTask	task	= engine._getTask(taskname);
		if ( task != null ){
			task.pause();
			engine.saveTask( task );
		}
	}

	public static void resumeTask(String taskname) {
		taskname	= taskname.toLowerCase();
		scheduleTask	task	= engine._getTask(taskname);
		if ( task != null ){
			task.resume();
			engine.saveTask( task );
		}
	}
}
