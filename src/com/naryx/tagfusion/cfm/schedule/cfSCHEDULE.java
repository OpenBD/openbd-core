/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  $Id: cfSCHEDULE.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.schedule;

import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;

import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class cfSCHEDULE extends cfTag implements Serializable {

	static final long serialVersionUID = 1;

	public static void init(xmlCFML configFile) {
		try {
			scheduleEngine.init(configFile);
		} catch (NoClassDefFoundError ignore) {
			// this will happen for FREE server edition
		}
	}

	public java.util.Map getInfo() {
		return createInfo("admin", "Allows you to schedule a background page request to be made at a given date/time, running once or at some regular interval. The result of which can be saved to file.");
	}

	public java.util.Map[] getAttInfo() {
		return new java.util.Map[] { 
			createAttInfo("ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
			createAttInfo("ACTION", "DELETE|PAUSE|RESUME|RUN|UPDATE", "", true), 
			createAttInfo("ACTION=PAUSE", "If you specify a TASK then that task will be paused, otherwise the whole scheduler will be paused", "", false), 
			createAttInfo("ACTION=RESUME", "If you specify a TASK then that task will be resumed, otherwise the whole scheduler will resume", "", false), 
			createAttInfo("TASK", "The name of the task", "", false), 
			createAttInfo("ENDDATE", "The date the task will run until", "", false), 
			createAttInfo("ENDTIME", "The time the task will run until", "", false), 
			createAttInfo("FILE", "The name of the file to save the result in", "", false),
			createAttInfo("INTERVAL", "DAILY|MONTHLY|ONCE|WEEKLY", "", false), 
			createAttInfo("PASSWORD", "Password, if url requires authentication", "", false), 
			createAttInfo("PATH", "The file path where the published file will be ", "", false), 
			createAttInfo("PORT", "The port number", "", false), 
			createAttInfo("PROXYPASSWORD", "The password for the proxy if it requires authentication", "", false),
			createAttInfo("PROXYPORT", "The proxy port if the request needs to be made via a proxy", "", false), 
			createAttInfo("PROXYSERVER", "The proxy host if the request needs to be made via a proxy", "", false), 
			createAttInfo("PROXYUSER", "The username for the proxy if it requires authentication", "", false),
			createAttInfo("PUBLISH", "YES|NO, whether the result should be saved to file", "NO", false), 
			createAttInfo("REQUESTTIMEOUT", "The timeout to apply to the request", "", false), 
			createAttInfo("RESOLVEURL", "Whether relative urls in the published result file should be resolved to absolute urls", "NO", false), 
			createAttInfo("STARTDATE", "The date the task will run from", "", false),
			createAttInfo("STARTTIME", "The time the task will run from", "", false), 
			createAttInfo("URL", "The URL that will be requirest", "", false), 
			createAttInfo("USERNAME", "Username, if url requires authentication", "", false),
		};
	}

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute("PUBLISH", "NO");
		defaultAttribute("URIDIRECTORY", "NO");
		defaultAttribute("INTERVAL", -1);
		parseTagHeader(_tag);
	}

	/**
	 * This method was added when fixing bug #1653
	 * 
	 * @param date
	 *          - must be of the form: "mm/dd/yyyy" or "m/d/yyyy" or "mm/d/yyyy" or "m/dd/yyyy"
	 * 
	 *          in order to be valid. Specifically a 4 digit year.
	 * @return true if valid, else false.
	 */
	private static boolean has4digitYear(String date) {
		boolean has4digitYear = false;
		if (date != null) {
			date = date.trim();
			int pos = date.lastIndexOf("/");
			if (pos >= 3 // "m/d/yyyy" is the shortest valid form, so the last would be in position 3 or after.
					&& pos + 5 == date.length())
				has4digitYear = true;
		}

		return has4digitYear;
	}

	/**
	 * 
	 * This method was added when fixing bug #1653
	 */
	private boolean isValidStartDate(cfStructData attributes, cfSession _Session) throws cfmRunTimeException {
		boolean valid = false;

		if (containsAttribute(attributes, "STARTDATE")) {
			String startDate = getDynamic(attributes, _Session, "STARTDATE").getString();
			valid = has4digitYear(startDate);
		}
		return valid;
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);

		if (!containsAttribute(attributes, "ACTION"))
			throw newRunTimeException("You need to provide an ACTION");
		
		String action = getDynamic(attributes,_Session, "ACTION").getString();

		if (action.equalsIgnoreCase("LISTALL")) {
			listAllTasks(attributes,_Session);
		} else if (action.equalsIgnoreCase("PAUSE")) {
			
			if ( !containsAttribute(attributes,"TASK") )
				scheduleEngine.pause(true);
			else
				scheduleEngine.pauseTask( getDynamic(attributes,_Session, "TASK").getString() );
			
		} else if (action.equalsIgnoreCase("RESUME")) {
			
			if ( !containsAttribute(attributes,"TASK") )
				scheduleEngine.pause(false);
			else
				scheduleEngine.resumeTask( getDynamic(attributes,_Session, "TASK").getString() );
			
		} else {
			// TASK required when action isn't LISTALL
			if (!containsAttribute(attributes,"TASK"))
				throw newRunTimeException("Missing TASK attribute. You need to provide a TASK when ACTION=" + action);

			String taskName = getDynamic(attributes,_Session, "TASK").getString();

			if (action.equalsIgnoreCase("DELETE")) {
				scheduleEngine.deleteTask(taskName);
			} else if (action.equalsIgnoreCase("RUN")) {
				scheduleEngine.runTask(taskName);
			} else if (action.equalsIgnoreCase("READ") || action.equalsIgnoreCase("EDIT")) {
				editTask(attributes,_Session, taskName);
			} else if (action.equalsIgnoreCase("UPDATE")) { // modifying an existing task or creating a brand new one.
				scheduleTask task = new scheduleTask(taskName);

				if (!isValidStartDate(attributes,_Session))
					throw newRunTimeException("The specified startdate needs to be in the mm/dd/yyyy format.");

				// Sort out the interval type
				String interval = getDynamic(attributes,_Session, "INTERVAL").getString().toUpperCase();
				if (interval.equals("ONCE") || interval.equals("DAILY") || interval.equals("WEEKLY") || interval.equals("MONTHLY")) {

					task.setTaskType(interval);
					task.setInterval(-1);

					if (!containsAttribute(attributes,"STARTTIME"))
						throw newRunTimeException("You need to specify a STARTTIME when the INTERVAL=ONCE");

					// Use the startDate when retrieving the startTime to avoid problems with tasks
					// scheduled to start at 2am. If we don't do this then when we add a task on days
					// when daylight savings begins or ends, tasks scheduled to start at 2am will be
					// set to start at 1am since 2am doesn't exist on those days.
					// NOTE: this is the fix for bug NA#3314 issue 3.
					com.nary.util.date.dateTimeTokenizer DT = new com.nary.util.date.dateTimeTokenizer(getDynamic(attributes,_Session, "STARTDATE").getString() + " " + getDynamic(attributes,_Session, "STARTTIME").getString());
					DT.validateStructure();
					Date DD = DT.getDate();
					if (DD == null)
						throw newRunTimeException("You need to specify a valid STARTTIME when the INTERVAL=ONCE");

					task.setStartTime(DD.getTime());

				} else {

					task.setInterval(com.nary.util.string.convertToInteger(interval, 60));
					if (!containsAttribute(attributes,"STARTTIME") && !containsAttribute(attributes,"ENDTIME"))
						throw newRunTimeException("You need to specify a STARTTIME/ENDTIME when the INTERVAL is a number");

					// Use the startDate when retrieving the startTime to avoid problems with tasks
					// scheduled to start at 2am. If we don't do this then when we add a task on days
					// when daylight savings begins or ends, tasks scheduled to start at 2am will be
					// set to start at 1am since 2am doesn't exist on those days.
					// NOTE: this is the fix for bug NA#3314 issue 3.
					com.nary.util.date.dateTimeTokenizer DT = new com.nary.util.date.dateTimeTokenizer(getDynamic(attributes,_Session, "STARTDATE").getString() + " " + getDynamic(attributes,_Session, "STARTTIME").getString());
					DT.validateStructure();
					Date DD = DT.getDate();
					if (DD == null)
						throw newRunTimeException("You need to specify a valid STARTTIME when the INTERVAL is a number");

					task.setStartTime(DD.getTime());

					if (containsAttribute(attributes,"ENDTIME")) {
						// Use the endDate when retrieving the endTime to avoid problems with tasks
						// scheduled to end at 2am. If we don't do this then when we add a task on days
						// when daylight savings begins or ends, tasks scheduled to end at 2am will be
						// set to end at 1am since 2am doesn't exist on those days.
						// NOTE: this is the fix for bug NA#3314 issue 3.
						if (containsAttribute(attributes,"ENDDATE")) {
							DT = new com.nary.util.date.dateTimeTokenizer(getDynamic(attributes,_Session, "ENDDATE").getString() + " " + getDynamic(attributes,_Session, "ENDTIME").getString());
						} else {
							DT = new com.nary.util.date.dateTimeTokenizer(getDynamic(attributes,_Session, "ENDTIME").getString());
						}
						DT.validateStructure();
						DD = DT.getDate();
						if (DD == null)
							throw newRunTimeException("You need to specify a valid ENDTIME when the INTERVAL is a number");

						task.setEndTime(DD.getTime());
					}
				}

				com.nary.util.date.dateTimeTokenizer DT = new com.nary.util.date.dateTimeTokenizer(getDynamic(attributes,_Session, "STARTDATE").getString());
				DT.validateStructure();
				Date DD = DT.getDate();
				if (DD == null)
					throw newRunTimeException("You need to specify a valid STARTDATE");

				task.setStartDate(DD.getTime());

				// ---------------------------
				// --[ Get the End date
				if (containsAttribute(attributes,"ENDDATE")) {
					String endDate = getDynamic(attributes,_Session, "ENDDATE").getString();
					if (endDate.length() > 0) {
						if (!has4digitYear(endDate)) // part of the fix for bug #1653
							throw newRunTimeException("The specified enddate needs to be in the mm/dd/yyyy format");

						DT = new com.nary.util.date.dateTimeTokenizer(endDate);
						DT.validateStructure();
						DD = DT.getDate();
						if (DD == null)
							throw newRunTimeException("You need to specify a valid (mm/dd/yyyy) ENDDATE");

						task.setEndDate(DD.getTime());
					}
				}

				// ------------------------------------------------------------------
				// --[ Get the URL
				if (!containsAttribute("URL"))
					throw newRunTimeException("You need to specify a URL when ACTION=UPDATE");

				task.setUrl(getDynamic(attributes,_Session, "URL").getString());

				if (containsAttribute("PORT"))
					task.setPort(getDynamic(attributes,_Session, "PORT").getInt());

				// ---------------------------
				// --[ Get the Publish information
				boolean bPublish = getDynamic(attributes,_Session, "PUBLISH").getBoolean();
				if (bPublish) {
					task.setPublish();
					if (!properties.containsKey("FILE") || !properties.containsKey("PATH"))
						throw newRunTimeException("You need to specify a PATH/FILE when ACTION=UPDATE & PUBLISH=YES");

					task.setFilename(getDynamic(attributes,_Session, "FILE").getString());

					if (getDynamic(_Session, "URIDIRECTORY").getBoolean())
						task.setPath(FileUtils.getRealPath(_Session.REQ, getDynamic(_Session, "PATH").getString()));
					else
						task.setPath(getDynamic(_Session, "PATH").getString());
				}

				// ---------------------------
				// --[ Get the username/password
				if (containsAttribute(attributes,"USERNAME"))
					task.setUsername(getDynamic(attributes,_Session, "USERNAME").getString());

				if (containsAttribute(attributes,"PASSWORD"))
					task.setPassword(getDynamic(attributes,_Session, "PASSWORD").getString());

				// ---------------------------
				// --[ Get the Proxy information
				if (containsAttribute(attributes,"PROXYSERVER"))
					task.setProxy(getDynamic(attributes,_Session, "PROXYSERVER").getString());

				if (containsAttribute(attributes,"PROXYPORT"))
					task.setProxyPort(getDynamic(attributes,_Session, "PROXYPORT").getInt());

				if (containsAttribute(attributes,"PROXYUSER"))
					task.setProxyUsername(getDynamic(attributes,_Session, "PROXYUSER").getString());

				if (containsAttribute(attributes,"PROXYPASSWORD"))
					task.setProxyPassword(getDynamic(attributes,_Session, "PROXYPASSWORD").getString());

				// ---------------------------
				// --[ Get the resolveURL
				if (containsAttribute(attributes,"RESOLVEURL"))
					task.setResolveURL(getDynamic(attributes,_Session, "RESOLVEURL").getBoolean());

				if (containsAttribute(attributes,"REQUESTTIMEOUT"))
					task.setRequestTimeout(getDynamic(attributes,_Session, "REQUESTTIMEOUT").getInt());

				scheduleEngine.updateTask(taskName, task);

			} else {
				throw this.newRunTimeException("Invalid ACTION attribute. Possible valid values are DELETE, LISTALL, RUN, READ, & UPDATE");
			}
		}

		return cfTagReturnType.NORMAL;
	}

	private void listAllTasks(cfStructData attributes, cfSession _Session) throws cfmRunTimeException {
		cfArrayData taskArray = cfArrayData.createArray(1);

		Enumeration<String> E = scheduleEngine.getTasks();
		while (E.hasMoreElements())
			taskArray.addElement(new cfStringData(E.nextElement()));

		if (containsAttribute(attributes,"RESULT")) {
			_Session.setData(getDynamic(attributes,_Session, "RESULT").getString(), taskArray);
		} else {
			_Session.setData("schedule", taskArray);
		}
	}

	private void editTask(cfStructData attributes, cfSession _Session, String taskName) throws cfmRunTimeException {
		scheduleTask task = scheduleEngine.getTask(taskName);
		if (task == null)
			return;

		cfStructData taskData = new cfStructData();
		task.setStructureData(taskData);
		if (containsAttribute(attributes,"RESULT")) {
			_Session.setData(getDynamic(attributes,_Session, "RESULT").getString(), taskData);
		} else {
			_Session.setData("task", taskData);
		}
	}

}
