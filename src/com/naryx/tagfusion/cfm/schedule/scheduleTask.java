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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.http.cfHttpConnection;
import com.naryx.tagfusion.cfm.http.cfHttpData;
import com.naryx.tagfusion.util.dummyServletRequest;
import com.naryx.tagfusion.util.dummyServletResponse;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class scheduleTask extends Object implements java.io.Serializable {
	static final long serialVersionUID = 1;

	private String name;

	private String taskType;

	private long startDate, endDate;

	private long startTime, endTime;

	private int interval; // --[ expressed in seconds

	private boolean bPublish;

	private String publishFile;

	private String publishPath;

	private boolean bResolveLinks;

	private String urlToUse;

	private int portToUse;

	private String proxyServer;

	private int proxyPort;

	private String proxyUsername, proxyPassword;

	private String username, password;
	private long	nextRunTime = -1;

	private boolean bPause	= false;
	private int requestTimeout; // stored as milliseconds, but the admin UI uses
															// seconds so conversions take place between here
															// and the view.

	// ------------------------------------------------

	public scheduleTask(String _name) {
		name = _name;
		startTime = System.currentTimeMillis();
		startTime = startDate;
		endTime = -1;
		endDate = -1;
		interval = -1;

		bPublish = false;
		bResolveLinks = false;
		proxyServer = null;
		proxyUsername = null;
		username = null;
		portToUse = -1;
		proxyPort = 80;
		taskType = null;

		requestTimeout = 30000;
	}

	public String getName() {
		return name;
	}

	public void setName(String _name) {
		name = _name;
	}

	public void setUrl(String _urlToUse) {
		urlToUse = _urlToUse;
	}

	public void setPort(int _port) {
		portToUse = _port;
	}

	public void setProxy(String _proxyServer) {
		proxyServer = _proxyServer;
		if (proxyServer != null && proxyServer.length() == 0)
			proxyServer = null;
	}

	public void setProxyPort(int _port) {
		proxyPort = _port;
	}

	public void setStartTime(long _startTime) {
		startTime = _startTime;
	}

	public void setEndTime(long _endTime) {
		if (_endTime < startTime) {
			endTime = startTime;
			startTime = _endTime;
		} else
			endTime = _endTime;
	}

	public void setStartDate(long _startDate) {
		startDate = _startDate;
	}

	public void setEndDate(long _endDate) {
		endDate = _endDate;
	}

	public void setTaskType(String _taskType) {
		taskType = _taskType;
	}

	public void setInterval(int _interval) {
		interval = _interval;
	}

	public void setRequestTimeout(int _interval) {
		if (_interval <= 0)
			requestTimeout = 0;
		else
			requestTimeout = _interval * 1000;
	}

	public void setPublish() {
		bPublish = true;
	}

	public void setFilename(String _filename) {
		publishFile = _filename;
	}

	public void setPath(String _path) {
		publishPath = _path;
	}

	public void setResolveURL(boolean _url) {
		bResolveLinks = _url;
	}

	public void setUsername(String _username) {
		username = _username;
		if (username != null && username.length() == 0)
			username = null;
	}

	public void setPassword(String _password) {
		password = _password;
		if (password != null && password.length() == 0)
			password = null;
	}

	public void setProxyUsername(String _username) {
		proxyUsername = _username;
		if (proxyUsername != null && proxyUsername.length() == 0)
			proxyUsername = null;
	}

	public void setProxyPassword(String _password) {
		proxyPassword = _password;
		if (proxyPassword != null && proxyPassword.length() == 0)
			proxyPassword = null;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("taskName=" + name);

		if (taskType != null)
			sb.append("; Type=" + taskType);

		if (startTime == -1)
			sb.append("; StartTime=-1");
		else
			sb.append("; StartTime=" + com.nary.util.Date.formatDate(startTime, "HH:mm.ss"));

		if (startDate == -1)
			sb.append("; StartDate=-1");
		else
			sb.append("; StartDate=" + com.nary.util.Date.formatDate(startDate, "dd/MMM/yyyy"));

		if (endTime == -1)
			sb.append("; EndTime=-1");
		else
			sb.append("; EndTime=" + com.nary.util.Date.formatDate(endTime, "HH:mm.ss"));

		if (endDate == -1)
			sb.append("; EndDate=-1");
		else
			sb.append("; EndDate=" + com.nary.util.Date.formatDate(endDate, "dd/MMM/yyyy"));

		sb.append("; interval=" + interval);

		return sb.toString();
	}

	public void setStructureData(cfStructData taskData) {
		taskData.setData("task", new cfStringData(name));
		taskData.setData("startdate", new cfDateData(startDate));
		taskData.setData("starttime", new cfDateData(startTime));

		if (endDate != -1)
			taskData.setData("enddate", new cfDateData(endDate));

		if (endTime != -1)
			taskData.setData("endtime", new cfDateData(endTime));

		if (taskType != null)
			taskData.setData("tasktype", new cfStringData(taskType));
		else
			taskData.setData("tasktype", new cfStringData("INTERVAL"));

		taskData.setData("interval", new cfNumberData(interval)); // removed
																															// divide-by 60
																															// here to fix bug
																															// #1473

		taskData.setData("url", new cfStringData(urlToUse));
		taskData.setData("port", new cfNumberData(portToUse));

		if (username != null) {
			taskData.setData("username", new cfStringData(username));
			taskData.setData("password", new cfStringData(password));
		} else {
			taskData.setData("username", new cfStringData(""));
			taskData.setData("password", new cfStringData(""));
		}

		if (proxyServer != null) {
			taskData.setData("proxyserver", new cfStringData(proxyServer));
			taskData.setData("proxyport", new cfNumberData(proxyPort));
			if (proxyUsername != null) {
				taskData.setData("proxyusername", new cfStringData(proxyUsername));
				taskData.setData("proxypassword", new cfStringData(proxyPassword));
			} else {
				taskData.setData("proxyusername", new cfStringData(""));
				taskData.setData("proxypassword", new cfStringData(""));
			}
		} else {
			taskData.setData("proxyserver", new cfStringData(""));
			taskData.setData("proxyport", new cfNumberData(proxyPort));
			taskData.setData("proxyusername", new cfStringData(""));
			taskData.setData("proxypassword", new cfStringData(""));
		}

		taskData.setData("resolveurl", cfBooleanData.getcfBooleanData(bResolveLinks));
		taskData.setData("requesttimeout", new cfNumberData(requestTimeout / 1000));

		if (bPublish) {
			taskData.setData("file", new cfStringData(publishFile));
			taskData.setData("path", new cfStringData(publishPath));
			taskData.setData("uridirectory", cfBooleanData.FALSE);
			taskData.setData("publish", cfBooleanData.TRUE);
		} else {
			taskData.setData("file", new cfStringData(""));
			taskData.setData("path", new cfStringData(""));
			taskData.setData("uridirectory", cfBooleanData.FALSE);
			taskData.setData("publish", cfBooleanData.FALSE);
		}
	}

	// ------------------------------------------------

	public void schedule() {
		schedule(false);
	}

	public void schedule(boolean alreadyRun) {
		GregorianCalendar thisTime = new GregorianCalendar();

		// ---[ Check to see if this box is still to be used
		if (endDate != -1) {
			GregorianCalendar endDateG = com.nary.util.Date.createCalendar(endDate);

			// This is an update to the fix for bug NA#3288 to allow tasks to run on
			// the end date but not after.
			endDateG.set(Calendar.HOUR_OF_DAY, 23);
			endDateG.set(Calendar.MINUTE, 59);
			endDateG.set(Calendar.SECOND, 59);
			endDateG.set(Calendar.MILLISECOND, 999);

			// If the current time is after the end date then just return since we
			// don't need to schedule the task. This is the fix for bug NA#3288.
			if (thisTime.after(endDateG)) {
				nextRunTime = Long.MIN_VALUE;
				return;
			}
		}

		// function returns the milliseconds to the next one timeout period
		nextRunTime = getNextScheduledTime(thisTime, alreadyRun);
		nextRunTime	+= System.currentTimeMillis();
	}

	public long getNextRunTime(){
		return nextRunTime;
	}
	
	public String print(GregorianCalendar startDateG) {
		return com.nary.util.Date.formatDate(startDateG.getTime().getTime());
	}

	public long getNextScheduledTime(GregorianCalendar _thisTime, boolean _alreadyRun) {
		long scheduleTime = -1;

		GregorianCalendar startDateG = com.nary.util.Date.createCalendar(startDate);
		GregorianCalendar startTimeG = com.nary.util.Date.createCalendar(startTime);

		startDateG.set(Calendar.HOUR_OF_DAY, startTimeG.get(Calendar.HOUR_OF_DAY));
		startDateG.set(Calendar.MINUTE, startTimeG.get(Calendar.MINUTE));
		startDateG.set(Calendar.SECOND, startTimeG.get(Calendar.SECOND));
		startDateG.set(Calendar.MILLISECOND, 0);

		// Now schedule the time
		if (taskType == null) {
			/*
			 * This code handles scheduled tasks that are scheduled to run daily at specified intervals
			 * between a specified start time and end time. For example, a task could be scheduled to
			 * run every day between 1pm and 2pm at 10 minute intervals.
			 */
			
			// Set the start date day of year to the current day of year
			startDateG.set(Calendar.DAY_OF_YEAR, _thisTime.get(Calendar.DAY_OF_YEAR));

			// We want to skip up to the current time as much possible, but without going past it
			long timeDiffSecs = (long) ((_thisTime.getTime().getTime() - startDateG.getTime().getTime()) / 1000);
			if (timeDiffSecs > interval) {
				long skipTimeSecs = (long) (timeDiffSecs / interval) * interval;
				startDateG.add(Calendar.MINUTE, (int) (skipTimeSecs / 60));
			}

			// Increment the start date by the specified interval until it is after the current time
			int dayOfYear = startDateG.get(Calendar.DAY_OF_YEAR);
			while (_thisTime.after(startDateG)) {
				startDateG.add(Calendar.SECOND, interval);
			}

			// If adding the interval moved us to the next day then set the hour,
			// minutes and seconds back to the start hour, minutes and seconds.
			// NOTE: this is the fix for bug #3318.
			if (dayOfYear != startDateG.get(Calendar.DAY_OF_YEAR)) {
				startDateG.set(Calendar.HOUR_OF_DAY, startTimeG.get(Calendar.HOUR_OF_DAY));
				startDateG.set(Calendar.MINUTE, startTimeG.get(Calendar.MINUTE));
				startDateG.set(Calendar.SECOND, startTimeG.get(Calendar.SECOND));
			} else {
				// If we've gone past the end time then increment the day of year by 1
				// and set the hour, minutes and seconds back to the start hour, minutes
				// and seconds.
				if (endTime != -1) {
					GregorianCalendar eTimeG = com.nary.util.Date.createCalendar(endTime);
					int endHour = eTimeG.get(Calendar.HOUR_OF_DAY);
					int endMinute = eTimeG.get(Calendar.MINUTE);
					int endSecond = eTimeG.get(Calendar.SECOND);
					int startHour = startDateG.get(Calendar.HOUR_OF_DAY);
					int startMinute = startDateG.get(Calendar.MINUTE);
					int startSecond = startDateG.get(Calendar.SECOND);

					if ((startHour > endHour) || ((startHour == endHour) && (startMinute > endMinute)) || ((startHour == endHour) && (startMinute == endMinute) && (startSecond > endSecond))) {
						startDateG.add(Calendar.DAY_OF_YEAR, 1);
						startDateG.set(Calendar.HOUR_OF_DAY, startTimeG.get(Calendar.HOUR_OF_DAY));
						startDateG.set(Calendar.MINUTE, startTimeG.get(Calendar.MINUTE));
						startDateG.set(Calendar.SECOND, startTimeG.get(Calendar.SECOND));
					}
				}
			}

			// Get the end date and time
			if (endDate != -1) {
				GregorianCalendar endTimeG = getEndDateCalender();

				// If the next start time is after the end date then return -1 so the task will be deleted
				if (startDateG.after(endTimeG)){
					scheduleEngine.deleteTask(name);
					return Long.MIN_VALUE;
				}
			}

			scheduleTime = startDateG.getTime().getTime() - _thisTime.getTime().getTime();

		} else if (taskType.equals("ONCE")) {
			if (_thisTime.after(startDateG) || _alreadyRun) {
				scheduleEngine.deleteTask(name);
				return Long.MIN_VALUE;
			} else {
				scheduleTime = startDateG.getTime().getTime() - _thisTime.getTime().getTime();
			}

		} else if (taskType.equals("DAILY")) {

			if (endDate != -1) {
				GregorianCalendar endTimeG = getEndDateCalender();

				// If the end date has already past next start time is after the end
				// date then return -1 so the task will be deleted
				if (_thisTime.after(endTimeG)){
					scheduleEngine.deleteTask(name);
					return Long.MIN_VALUE;
				}
			}

			// note using Calendar.DAY_OF_YEAR results in the DST being taken account of automatically
			scheduleTime = updateNextStartDate(startDateG, startTimeG.get(Calendar.HOUR_OF_DAY), _thisTime, Calendar.DAY_OF_YEAR, 1, _alreadyRun);
			
		} else if (taskType.equals("WEEKLY")) {
			
			// note using Calendar.DAY_OF_YEAR results in the DST being taken account of automatically
			scheduleTime = updateNextStartDate(startDateG, startTimeG.get(Calendar.HOUR_OF_DAY), _thisTime, Calendar.DAY_OF_YEAR, 7, _alreadyRun);
			
		} else if (taskType.equals("MONTHLY")) {
			
			/*
			 * If we increment by months using Calendar.add(), it will take care of the logic for handling dates at the end of the month, or to be more specific, this logic:
			 * 
			 * If you schedule a job to run monthly on any date in the range 28-31, the scheduler does the following: If you schedule a monthly job to run on the last day of a month, the scheduled job will run on the last day of each month. For example, if you schedule a monthly job to start on January 31, it will run on January 31, February 28 or 29, March 31, April 30, and so on. If you schedule a
			 * monthly job to run on the 29th or 30th of the month, the job will run on the specified day of each month for 30 or 31-day months, and the last day of February. For example, if you schedule a monthly job to start on January 30, the job will run on January 30, February 28 or 29, March 30, April 30, and so on.
			 * 
			 * However unlike DAILY and WEEKLY, we can't do it 1 month at a time otherwise 31 Jan start date goes to 28th Feb to 28th Mar (should be 31 Mar). So we need to calculate the total number of months and do it in 1 add()
			 */
			if (startDateG.after(_thisTime)) {
				scheduleTime = startDateG.getTime().getTime() - _thisTime.getTime().getTime();
			} else {
				// we know that the date is after the start date so find the number of months after the start date that we're at
				int yearDiff = _thisTime.get(Calendar.YEAR) - startDateG.get(Calendar.YEAR);
				int monthDiff = _thisTime.get(Calendar.MONTH) - startDateG.get(Calendar.MONTH);
				int totalMonthDiff = 0;

				// there's at least a year diffence in the dates
				if (yearDiff > 0) {
					totalMonthDiff = yearDiff * 12;
					if (monthDiff < 0) {
						totalMonthDiff -= 12;
					}
				}

				if (monthDiff > 0) {
					totalMonthDiff += monthDiff;
				} else if (monthDiff < 0) {
					// note that monthDiff is negative so this will result in a numeric <
					// 12
					totalMonthDiff += (12 + monthDiff);
				}// else monthDiff == 0

				if (_alreadyRun && totalMonthDiff == 0) {
					totalMonthDiff++;
				}

				startDateG.add(Calendar.MONTH, totalMonthDiff);

				if (_thisTime.after(startDateG)) {
					startDateG.add(Calendar.MONTH, 1);
				}
				scheduleTime = startDateG.getTime().getTime() - _thisTime.getTime().getTime();
			}
		}

		if (scheduleTime < 0)
			scheduleTime = 0;

		return scheduleTime;
	}

	private GregorianCalendar getEndDateCalender() {
		GregorianCalendar endTimeG = com.nary.util.Date.createCalendar(endDate);
		if (endTime != -1) {
			GregorianCalendar eTimeG = com.nary.util.Date.createCalendar(endTime);
			endTimeG.set(Calendar.HOUR_OF_DAY, eTimeG.get(Calendar.HOUR_OF_DAY));
			endTimeG.set(Calendar.MINUTE, eTimeG.get(Calendar.MINUTE));
			endTimeG.set(Calendar.SECOND, eTimeG.get(Calendar.SECOND));
		} else {
			// This is an update to the fix for bug NA#3288 to allow tasks to run on
			// the end date but not after.
			endTimeG.set(Calendar.HOUR_OF_DAY, 23);
			endTimeG.set(Calendar.MINUTE, 59);
			endTimeG.set(Calendar.SECOND, 59);
			endTimeG.set(Calendar.MILLISECOND, 999);
		}
		return endTimeG;
	}

	private long updateNextStartDate(GregorianCalendar _startDateG, int _startHour, GregorianCalendar _thisTime, int _field, int _amount, boolean _alreadyRun) {
		if (_alreadyRun) {
			_startDateG.add(_field, _amount);

			// If the start hour is 2am and we've crossed the start or end of daylight
			// savings
			// then the hour will be set to 1am. In that case we need to set the hour
			// back to 2am.
			// Note that tasks scheduled to run at 2am will run at 3am on the day when
			// daylight
			// savings starts. On the day daylight savings ends, a 2am task will run
			// at 2am after
			// the time change has occurred.
			// NOTE: this is the fix for bug NA#3314 issue 1.
			if ((_startHour == 2) && (_startDateG.get(Calendar.HOUR_OF_DAY) != 2))
				_startDateG.set(Calendar.HOUR_OF_DAY, _startHour);
		}

		while (_thisTime.after(_startDateG)) {
			_startDateG.add(_field, _amount);

			// If the start hour is 2am and we've crossed the start or end of daylight
			// savings
			// then the hour will be set to 1am. In that case we need to set the hour
			// back to 2am.
			// Note that tasks scheduled to run at 2am will run at 3am on the day when
			// daylight
			// savings starts. On the day daylight savings ends, a 2am task will run
			// at 2am after
			// the time change has occurred.
			// NOTE: this is the fix for bug NA#3314 issue 1.
			if ((_startHour == 2) && (_startDateG.get(Calendar.HOUR_OF_DAY) != 2))
				_startDateG.set(Calendar.HOUR_OF_DAY, _startHour);
		}

		return _startDateG.getTime().getTime() - _thisTime.getTime().getTime();
	}

	// ------------------------------------------------------------
	// ------------------------------------------------------------

	public void runAndSchedule() {
		nextRunTime = Long.MIN_VALUE;	// while it is running in a schedule, let us disable it
		run();
		schedule(true);
	}

	public void run() {
		scheduleEngine.log("scheduleTask.run(" + name + ").started");

		HttpServletRequest REQ = new dummyServletRequest(cfEngine.thisPlatform.getFileIO().getTempDirectory().getAbsolutePath());
		HttpServletResponse RES = new dummyServletResponse();

		cfSession tmpSession = new cfSession(REQ, RES, cfEngine.thisServletContext);
		cfHttpConnection con = null;

		cfHttpData httpData = new cfHttpData( "UTF-8" );
		try {
			con = new cfHttpConnection(tmpSession, httpData);
		} catch (cfmRunTimeException e) {
			handleException(e);
			return;
		}
		
		con.setUserAgent("BlueDragon");

		try {
			con.setMethod("GET", false);
			con.setURL(urlToUse, portToUse); // use default port or get one set
		} catch (cfmRunTimeException t) {
			handleException(t);
			try {
				con.close();
			} catch (IOException ignored) {
			}
			
			return;
		}

		con.setGetAsBinary("auto");
		con.setThrowOnError(true);
		con.setTimeout(requestTimeout);

		// Setup the various parameters
		if (username != null)
			con.authenticate(username, password);

		con.setResolveLinks(bResolveLinks);

		if (proxyServer != null) {
			con.setProxyServer(proxyServer, proxyPort);
			if (proxyUsername != null)
				con.authenticateProxy(proxyUsername, proxyPassword);
		}

		con.setFollowRedirects(true);

		if (bPublish) {
			con.setFile(new File(publishPath, publishFile));
		}

		// Create the thread that will do the processing
		new runThread(con);

	}

	private void handleException(Throwable t) {
		String msg;
		String exceptionType = "exception";
		if (t instanceof cfmRunTimeException) {
			msg = ((cfmRunTimeException) t).getCatchData().getMessage();
		} else if (t instanceof Exception) {
			msg = t.getMessage();
		} else {
			msg = t.getMessage();
			exceptionType = "seriousError";
		}

		scheduleEngine.log("scheduleTask(" + name + ")." + exceptionType + ":" + msg);
		writeErrorToFile(msg);
	}

	private void writeErrorToFile(String _msg) {
		if (bPublish) {
			FileWriter fw = null;
			try {
				fw = new FileWriter(new File(publishPath, publishFile));
				fw.write(_msg);
			} catch (IOException e1) {
				scheduleEngine.log("scheduleTask(" + name + ").exception. Failed to publish error to file:" + e1.getMessage());
			} finally {
				if (fw != null)
					try {
						fw.close();
					} catch (IOException ignored) {
					}
			}
		}
	}

	// -------------------------------------------------

	private class runThread extends Thread {
		private cfHttpConnection con;

		public runThread(cfHttpConnection con) {
			this.setName("scheduleTask." + name);
			this.con = con;
			setPriority(Thread.MIN_PRIORITY);
			start();
		}

		public void run() {
			try {
				con.connect();
				scheduleEngine.log("scheduleTask.run(" + name + ").finished");

			} catch (Throwable t) {
				handleException(t);
			} finally {
				try {
					con.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	// -------------------------------------------------

	public void writeToConfig(xmlCFML configFile) {
		String taskKey = "server.cfschedule.task[" + name.toLowerCase() + "]";

		configFile.setData(taskKey + ".name", name);

		if (taskType != null)
			configFile.setData(taskKey + ".tasktype", taskType);

		// Store the date and time values in bluedragon.xml as strings
		configFile.setData(taskKey + ".startdate", getDateString(startDate));
		configFile.setData(taskKey + ".enddate", getDateString(endDate));
		configFile.setData(taskKey + ".starttime", getTimeString(startTime));
		configFile.setData(taskKey + ".endtime", getTimeString(endTime));

		configFile.setData(taskKey + ".interval", Integer.toString(interval));

		configFile.setData(taskKey + ".publish", (new Boolean(bPublish)).toString());
		configFile.setData(taskKey + ".pause", (new Boolean(bPause)).toString());

		if (publishFile == null)
			configFile.setData(taskKey + ".publishfile", "");
		else
			configFile.setData(taskKey + ".publishfile", publishFile);

		if (publishPath == null)
			configFile.setData(taskKey + ".publishpath", "");
		else
			configFile.setData(taskKey + ".publishpath", publishPath);

		configFile.setData(taskKey + ".resolvelinks", (new Boolean(bResolveLinks)).toString());

		if (urlToUse == null)
			configFile.setData(taskKey + ".urltouse", "");
		else
			configFile.setData(taskKey + ".urltouse", urlToUse);
		configFile.setData(taskKey + ".porttouse", Integer.toString(portToUse));

		if (proxyServer == null)
			configFile.setData(taskKey + ".proxyserver", "");
		else
			configFile.setData(taskKey + ".proxyserver", proxyServer);
		configFile.setData(taskKey + ".proxyport", Integer.toString(proxyPort));
		if (proxyUsername == null)
			configFile.setData(taskKey + ".proxyusername", "");
		else
			configFile.setData(taskKey + ".proxyusername", proxyUsername);
		if (proxyPassword == null)
			configFile.setData(taskKey + ".proxypassword", "");
		else
			configFile.setData(taskKey + ".proxypassword", proxyPassword);

		if (username == null)
			configFile.setData(taskKey + ".username", "");
		else
			configFile.setData(taskKey + ".username", username);

		if (password == null)
			configFile.setData(taskKey + ".password", "");
		else
			configFile.setData(taskKey + ".password", password);

		configFile.setData(taskKey + ".requesttimeout", Integer.toString(requestTimeout / 1000));
	}

	public static scheduleTask getTaskConfig(xmlCFML configFile, String taskKey) {
		scheduleTask task = new scheduleTask(taskKey);

		task.setName(configFile.getString(taskKey + ".name", null));
		task.setTaskType(configFile.getString(taskKey + ".tasktype", null));

		// Store the date and time values internally as long values
		String startDate = configFile.getString(taskKey + ".startdate", null);
		String endDate = configFile.getString(taskKey + ".enddate", null);
		String startTime = configFile.getString(taskKey + ".starttime", null);
		String endTime = configFile.getString(taskKey + ".endtime", null);
		task.setStartDate(getTime(startDate));
		task.setEndDate(getTime(endDate));

		// If a startDate was specified then use it when retrieving the startTime
		// to avoid problems with tasks scheduled to start at 2am. If we don't do
		// this then when we restart on days when daylight savings begins or ends,
		// tasks scheduled to start at 2am will be set to start at 1am.
		// NOTE: this is the fix for bug NA#3314 issue 2.
		if ((startDate != null) && (startDate.length() > 0) && (startTime != null) && (startTime.length() > 0))
			task.setStartTime(getTime(startDate + " " + startTime));
		else
			task.setStartTime(getTime(configFile.getString(taskKey + ".starttime", null)));

		// If a startDate was specified then use it when retrieving the startTime
		// to avoid problems with tasks scheduled to start at 2am. If we don't do
		// this then when we restart on days when daylight savings begins or ends,
		// tasks scheduled to start at 2am will be set to start at 1am.
		// NOTE: this is the fix for bug NA#3314 issue 2.
		long endTimeValue;
		if ((endDate != null) && (endDate.length() > 0) && (endTime != null) && (endTime.length() > 0))
			endTimeValue = getTime(endDate + " " + endTime);
		else
			endTimeValue = getTime(configFile.getString(taskKey + ".endtime", null));

		if (endTimeValue != -1)
			task.setEndTime(endTimeValue);

		task.setInterval(configFile.getInt(taskKey + ".interval", -1));
		if (configFile.getBoolean(taskKey + ".publish", false))
			task.setPublish();

		if (configFile.getBoolean(taskKey + ".pause", false))
			task.pause();
		else
			task.resume();

		task.setFilename(configFile.getString(taskKey + ".publishfile", null));
		task.setPath(configFile.getString(taskKey + ".publishpath", null));
		task.setResolveURL(configFile.getBoolean(taskKey + ".resolvelinks", false));
		task.setUrl(configFile.getString(taskKey + ".urltouse", null));
		task.setPort(configFile.getInt(taskKey + ".porttouse", -1));
		task.setProxy(configFile.getString(taskKey + ".proxyserver", null));
		task.setProxyPort(configFile.getInt(taskKey + ".proxyport", 80));
		task.setProxyUsername(configFile.getString(taskKey + ".proxyusername", null));
		task.setProxyPassword(configFile.getString(taskKey + ".proxypassword", null));
		task.setUsername(configFile.getString(taskKey + ".username", null));
		task.setPassword(configFile.getString(taskKey + ".password", null));
		task.setRequestTimeout(configFile.getInt(taskKey + ".requesttimeout", 30));

		return task;
	}

	private static long getTime(String dateTime) {
		if ((dateTime != null) && (dateTime.length() > 0)) {
			com.nary.util.date.dateTimeTokenizer DT = new com.nary.util.date.dateTimeTokenizer(dateTime);
			if (DT.validateStructure()) {
				java.util.Date DD = DT.getDate();
				if (DD != null)
					return DD.getTime();
				else
					cfEngine.log("-] ERROR: a scheduled task has an invalid date or time - [" + dateTime + "]");
			} else {
				cfEngine.log("-] ERROR: a scheduled task has an invalid date or time - [" + dateTime + "]");
			}
		}

		return -1;
	}

	private static String getDateString(long date) {
		if (date != -1) {
			GregorianCalendar cal = com.nary.util.Date.createCalendar(date);
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy");
			return sdf.format(cal.getTime());
		}

		return "";
	}

	private static String getTimeString(long time) {
		if (time != -1) {
			GregorianCalendar cal = com.nary.util.Date.createCalendar(time);
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
			return sdf.format(cal.getTime());
		}

		return "";
	}

	public void pause() {
		bPause = true;
	}

	public void resume() {
		bPause = false;
	}
	
	public boolean isPause(){
		return bPause;
	}
}
