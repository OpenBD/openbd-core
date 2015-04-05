/* 
 *  Copyright (C) 2000 - 2015 aw2.0 Ltd
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
 *  $Id: OutgoingMailServer.java 2524 2015-02-22 23:09:07Z alan $
 */

package com.bluedragon.platform.java.smtp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import com.bluedragon.platform.SmtpInterface;
import com.bluedragon.plugin.ObjectCFC;
import com.bluedragon.plugin.PluginManager;
import com.nary.io.FileUtils;
import com.nary.util.HashMapTimed;
import com.nary.util.LogFile;
import com.nary.util.stringtokenizer;
import com.naryx.tagfusion.cfm.application.cfAPPLICATION;
import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.engineListener;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.mail.cfMAIL;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

/**
 * Mail Server. This class monitors all the mail and controls the transmission
 * 
 */
public class OutgoingMailServer implements engineListener, SmtpInterface {
	public static final String DEFAULT_MAILTHREADS = "1";

	private static enum SendType {
		NONE, SSL, TLS
	};

	File spoolDirectory, failedDirectory;

	private AtomicLong uniqueID = new AtomicLong(System.currentTimeMillis());

	private int mailOut = 0;
	private long mailSize = 0;
	private String smtpServer, smtpPort;
	private boolean useSSL, useTLS;
	private volatile boolean stayAlive = true;
	private String domain;
	private Object semaphore = new Object();
	private fileFilter emailFileFilter = new fileFilter();
	private int mailThreads;
	private int timeout;
	private InternetAddress[]	catchEmails = null;
	private String catchEmailList;

	private long lastMailSent = 0;
	private long delaySendMS = 200;
	private Object lastSemaphoreSent = new Object();

	private List<Thread> mailThreadList;

	// needed in order to create a MimeMessage for reading in the message.
	// Its properties should not be populated because it is not used in the
	// sending
	private Session dummySession = Session.getInstance(new Properties());

	private HashMapTimed sessions = new HashMapTimed();

	public OutgoingMailServer(xmlCFML config) {

		File mainDirectory = new File(cfEngine.thisPlatform.getFileIO().getWorkingDirectory(), "cfmail");

		try {
			mainDirectory 	= FileUtils.checkAndCreateDirectory(cfEngine.thisPlatform.getFileIO().getWorkingDirectory(), "cfmail", false);
			spoolDirectory 	= FileUtils.checkAndCreateDirectory(mainDirectory, "spool", false);
			failedDirectory = FileUtils.checkAndCreateDirectory(mainDirectory, "undelivered", false);
		} catch (Exception E) {
			cfEngine.log("OutgoingMailServer failed to create all the CFMAIL spooling directorys: " + mainDirectory);
		}

		LogFile.open("MAIL", new File(mainDirectory, "mail.log").toString());
		LogFile.println("MAIL", "OutgoingMailServer started");
		cfEngine.log("OutgoingMailServer started mainDirectory=" + mainDirectory.toString());

		mailThreadList = new ArrayList<Thread>();
		setMailSettings(config);

		if ( catchEmails != null && catchEmails.length > 0 )
			cfEngine.log("OutgoingMailServer catchemail=" + catchEmailList );

		cfEngine.registerEngineListener(this);
	}

	private void setMailSettings(xmlCFML config) {
		// default value is loopback IP address
		smtpServer 	= config.getString("server.cfmail.smtpserver", DEFAULT_SMTP_SERVER);
		domain 			= config.getString("server.cfmail.domain", "");
		smtpPort 		= config.getString("server.cfmail.smtpport", DEFAULT_SMTP_PORT);
		timeout 		= config.getInt("server.cfmail.timeout", Integer.parseInt(DEFAULT_TIMEOUT));
		delaySendMS	= config.getLong("server.cfmail.ratedelay", -1);
		mailThreads = config.getInt("server.cfmail.threads", Integer.parseInt(DEFAULT_MAILTHREADS));
		useSSL 			= config.getBoolean("server.cfmail.usessl", false);
		useTLS 			= config.getBoolean("server.cfmail.usetls", false);

		catchEmailList	= config.getString("server.cfmail.catchemail", "");
		catchEmails			= cfMAIL.getAddresses( catchEmailList, SmtpInterface.DEFAULT_CHARSET );
		
		
		// check if mailThreads has changed
		if (mailThreads > mailThreadList.size()) { // new threads need to be created
			int newThreads = mailThreads - mailThreadList.size();
			for (int i = 0; i < newThreads; i++) {
				Thread t = new MailSender();
				mailThreadList.add(t);
				t.start();
			}
		} else if (mailThreads < mailThreadList.size()) { // need to shutdown threads
			int shutdownThreads = mailThreadList.size() - mailThreads;
			for (int i = 0; i < shutdownThreads; i++) {
				MailSender ms = (MailSender) mailThreadList.remove(0);
				ms.shutdown();
			}
		}
	}

	public int getDefaultTimeout() {
		return timeout;
	}

	public int getTotalMails() {
		return mailOut;
	}

	public long getTotalMailSize() {
		return mailSize;
	}

	public String getDomain() {
		return domain;
	}

	public void engineAdminUpdate(xmlCFML config) {
		setMailSettings(config);

		cfEngine.log("OutgoingMailServer Configuration Updated");
	}

	public void engineShutdown() {
		stayAlive = false;

		for (int i = 0; i < this.mailThreadList.size(); i++) {
			((MailSender) mailThreadList.get(i)).shutdown();
		}

		sessions.destroy();
		cfEngine.log("OutgoingMailServer: Shutdown");
	}
	
	private void rateLimitSend(){
		if ( delaySendMS < 0 )
			return;

		for (;;){
		
			synchronized(lastSemaphoreSent){
				if ( (System.currentTimeMillis() - lastMailSent) >= delaySendMS ){
					lastMailSent = System.currentTimeMillis();
					return;
				}
			}
			
			try {
				Thread.sleep( delaySendMS >> 2 );
			} catch (InterruptedException e) {
				return;
			}
		}
		
	}
	
	
	public void send(MimeMessage msg) {
		if (msg == null)
			return;

		long msgID = uniqueID.getAndIncrement(); // atomic increment

		/*
		 * Write out the email to a temp file so it won't be picked up by a
		 * MailSender thread until we are done writing it out. This is the fix for
		 * bug #3285.
		 */
		BufferedOutputStream out = null;
		FileOutputStream fileOut = null;
		File filename = new File(spoolDirectory, msgID + ".spool");
		
		if ( !filename.getParentFile().exists() )
			filename.getParentFile().mkdirs();

		try {
			fileOut = new FileOutputStream(filename);
			out = new BufferedOutputStream(fileOut);
			msg.writeTo(out);
			out.flush();
		} catch (Exception E) {
			LogFile.println("MAIL", E);
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException ignoreE) {}

			try {
				if (fileOut != null)
					fileOut.close();
			} catch (IOException ignoreE) {}

			// Now rename to a file with a .email extension so it will be picked up by
			// a MailSender thread. This is the fix for bug #3285.
			try {
				if (!filename.renameTo(new File(spoolDirectory, msgID + ".email")))
					LogFile.println("MAIL", "ERROR - Failed to rename [" + filename.getAbsolutePath() + "]");
			} catch (Exception exc) {
				LogFile.println("MAIL", "ERROR - Exception occurred while trying to rename [" + filename.getAbsolutePath() + "], exc=" + exc.toString());
			}
		}

		notifySenders();
	}

	public void notifySenders() {
		synchronized (semaphore) {
			semaphore.notify();
		}
	}

	class MailSender extends Thread {

		private boolean shutdown;

		public MailSender() {
			super("OpenBD MailSender");
			setDaemon(true);
			setPriority(Thread.MIN_PRIORITY);
		}

		public void shutdown() {
			shutdown = true;
			this.interrupt();
		}

		private File getMail() {
			synchronized (semaphore) {

				// Get the files from the spool directory that end with .email
				String[] fileArr = spoolDirectory.list(emailFileFilter);

				// Keep waiting until we find a file that ends with .email
				while ((fileArr == null) || (fileArr.length == 0)) {
					try {
						semaphore.wait(60000);
						fileArr = spoolDirectory.list(emailFileFilter);
					} catch (InterruptedException ignored) {
						if (shutdown) {
							return null;
						}
					}
				}

				// Rename from xxx.email to xxx.tmpsend so no other MailSender threads will pick up the file
				String oldName = fileArr[0];
				String newName = oldName.substring(0, oldName.length() - ".email".length()) + ".tmpsend";
				File oldMailFile = new File(spoolDirectory, oldName);
				File newMailFile = new File(spoolDirectory, newName);
				oldMailFile.renameTo(newMailFile);

				return newMailFile;
			}
		}

		public void run() {
			File file;
			try {
				while (stayAlive && !shutdown && (file = getMail()) != null) {
					sendMail(file);
				}
			} catch (Throwable t) {
				// handle unexpected exceptions
				// e.g. running BD with gcj can result in
				// java.lang.NoClassDefFoundError: javax/security/sasl/SaslException
				cfEngine.log("mailSender failed due to unexpected exception: " + t.getClass().getName());
			}
			cfEngine.log("mailSender: thread stop.");
		}

	}

	private Session getSession(String _host, String _port, String _timeout, boolean _auth, String _returnPath, SendType _sendType) {
		Properties props = new Properties();
		String propPrefix = "mail.smtp";

		if (_sendType == SendType.SSL) {
			propPrefix += "s";
		}

		props.put(propPrefix + ".host", _host);
		props.put(propPrefix + ".port", _port);
		props.put(propPrefix + ".timeout", _timeout);

		if (_sendType == SendType.TLS) {
			props.put(propPrefix + ".starttls.enable", "true");
			props.put(propPrefix + ".starttls.required", "true");
		}

		if (domain.length() != 0) {
			props.put(propPrefix + ".localhost", domain);
		}

		if (_auth) {
			props.put(propPrefix + ".auth", "true");
		}

		if (_returnPath != null) { // fix for bug #2986
			props.put(propPrefix + ".from", _returnPath);
		}

		String key = props.toString();
		Session session = (Session) sessions.get(key);

		// -- if session doesn't already exist, create one
		if (session == null) {
			session = Session.getInstance(props);
			sessions.put(key, session);
		}

		return session;
	}

	private boolean sendMail(File filename) {
		String To = "", From = "", Subject = "";
		String callbackCFC = null, appname = null, customData = null;
		CustomMessage msg = null;
		FileInputStream fileIn = null;
		BufferedInputStream in = null;

		boolean deleteFile = false;

		String origFilename = filename.getName();
		origFilename = origFilename.substring(0, origFilename.lastIndexOf(".")) + ".email";

		try {

			// Load in the file
			fileIn = new FileInputStream(filename);
			in = new BufferedInputStream(fileIn);
			msg = new CustomMessage(dummySession, in);

			// Message is now in; run through the servers and attempt to deliver it
			String servers[] = msg.getHeader("X-BlueDragon-server");
			msg.removeHeader("X-BlueDragon-server");

			String timeout = msg.getHeader("X-BlueDragon-timeout", ",");
			msg.removeHeader("X-BlueDragon-timeout");

			// Callback details
			callbackCFC = msg.getHeader("X-BlueDragon-callback", ",");
			appname 		= msg.getHeader("X-BlueDragon-appname", ",");
			customData 	= msg.getHeader("X-BlueDragon-callbackdata", ",");
			msg.removeHeader("X-BlueDragon-callback");
			msg.removeHeader("X-BlueDragon-appname");
			msg.removeHeader("X-BlueDragon-callbackdata");

			// SSL details
			String ssl = msg.getHeader("X-BlueDragon-SSL", ",");
			msg.removeHeader("X-BlueDragon-SSL");
			SendType sendType = SendType.NONE;
			if ("ssl".equals(ssl)) {
				sendType = SendType.SSL;
			} else if ("tls".equals(ssl)) {
				sendType = SendType.TLS;
			}

			boolean gotAtLeastOneGoodServerAndPortCombo = false;

			for (int x = 0; x < servers.length; x++) {
				Transport transport = null;
				String catchLog = "";
				
				try {
					stringtokenizer st = new stringtokenizer(servers[x], ";");
					String username = st.nextToken();
					String password = st.nextToken();
					String server = st.nextToken();
					String port = st.nextToken();

					// here is part of the fix for bug #2088
					if (server == null || server.trim().equals("") || port == null || port.trim().equals("")) {
						continue;
					} else
						gotAtLeastOneGoodServerAndPortCombo = true;

					// Now we need to deliver it
					boolean auth = (username.length() != 0 && password.length() != 0);
					String[] returnPath = msg.getHeader("Return-Path");
					Session realSession = getSession(server, port, timeout, auth, (returnPath == null || returnPath.length == 0 ? null : returnPath[0]), sendType);

					// Set the To, From
					To = msg.getHeader("To", ",");
					From = msg.getHeader("From", ",");
					Subject = msg.getSubject();

					// Is this catching emails
					if ( catchEmails != null ){
						redirectEmails( msg );
						catchLog = "[CatchEmails:" + catchEmailList + "] ";
					}else{
						catchLog = "";
					}
					
					// Send the message
					mailOut++;
					mailSize += filename.length();

					/*
					 * previously just used the static method Transport.send() to send
					 * messages that don't require authentication however we need to use
					 * the realSession as opposed to the dummySession associated with the
					 * MimeMessage
					 */
					if (sendType == SendType.SSL) {
						transport = realSession.getTransport("smtps");
					} else {
						transport = realSession.getTransport("smtp");
					}

					if (auth) {
						transport.connect(server, username, password);
					} else {
						transport.connect();
					}

					msg.saveChanges();
					
					rateLimitSend();
					transport.sendMessage(msg, msg.getAllRecipients());

					LogFile.println("MAIL", catchLog + "MailOut: To=" + To + "; From=" + From + "; Subject=" + Subject + "; Server=" + servers[x] + "; Size=" + filename.length() + " bytes");

					if (callbackCFC != null)
						onMailSend(callbackCFC, appname, servers[x], customData, msg);

					deleteFile = true;
					return true;
				} catch (Exception E) {
					LogFile.println("MAIL", catchLog + "MailOutFail: To=" + To + "; From=" + From + "; Subject=" + Subject + "; Server=" + servers[x] + "; Size=" + filename.length() + " bytes:" + E);

					if (callbackCFC != null)
						onMailFailed(callbackCFC, appname, servers[x], E.getMessage(), new File(failedDirectory, origFilename), customData, msg);

				} finally {
					if (transport != null)
						transport.close();
				}
			}// end for-loop

			if (!gotAtLeastOneGoodServerAndPortCombo) {
				String problemMsg = "Unable to send (" + filename.length() + " byte) message since both SMTP Server & SMTP Port are NOT specified.";
				LogFile.println("MAIL", problemMsg);
				throw new IllegalStateException(problemMsg);
			}
		} catch (IllegalStateException e) { // here is part of the fix for bug #2088
			throw e;
		} catch (Exception E) {
			LogFile.println("MAIL", "MailOutFail: Failed to parse " + filename);
		} finally {

			try {
				if (in != null)
					in.close();
			} catch (IOException ignoreE) {
			}

			try {
				if (fileIn != null)
					fileIn.close();
			} catch (IOException ignoreE) {
			}

			if (deleteFile) {
				if (!filename.delete())
					LogFile.println("MAIL", "MailOutFail: Failed to delete " + filename);
			} else {
				// sending failed so move file to failed directory
				File failedFilename = new File(failedDirectory, origFilename);
				if ( !failedFilename.getParentFile().exists() )
					failedFilename.getParentFile().mkdirs();
				
				if (!filename.renameTo(failedFilename))
					LogFile.println("MAIL", "MailOutFail: Failed to move " + filename + " to " + failedFilename);
			}
		}

		return false;
	}

	
	/**
	 * This is for the main catch email feature to redirect emails to a given box
	 * 
	 * @param msg
	 * @throws MessagingException 
	 */
	private void redirectEmails(CustomMessage msg) throws MessagingException {

		Address[] emails = msg.getRecipients( RecipientType.TO );
		if ( emails != null && emails.length > 0 ){
			StringBuilder sb = new StringBuilder();
			for ( int x=0; x < emails.length; x++ )
				sb.append( emails[x].toString() + ";" );
			
			msg.setHeader("x-openbd-to", sb.toString() );
			msg.setRecipients( RecipientType.TO, new Address[0] );
		}
	
		emails = msg.getRecipients( RecipientType.CC );
		if ( emails != null && emails.length > 0 ){
			StringBuilder sb = new StringBuilder();
			for ( int x=0; x < emails.length; x++ )
				sb.append( emails[x].toString() + ";" );
			
			msg.setHeader("x-openbd-cc", sb.toString() );
			msg.setRecipients( RecipientType.CC, new Address[0] );
		}
		
		emails = msg.getRecipients( RecipientType.BCC );
		if ( emails != null && emails.length > 0 ){
			StringBuilder sb = new StringBuilder();
			for ( int x=0; x < emails.length; x++ )
				sb.append( emails[x].toString() + ";" );
			
			msg.setHeader("x-openbd-bcc", sb.toString() );
			msg.setRecipients( RecipientType.BCC, new Address[0] );
		}
		
		// finally set the email to where we want it to go
		msg.setRecipients( RecipientType.TO, catchEmails );
	}
	
	/*
	 * fileFilter
	 * 
	 * This FilenameFilter returns true for all files that end with ".email".
	 */
	class fileFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return name.endsWith(".email");
		}
	}

	public String getSmtpPort() {
		return smtpPort;
	}

	public String getSmtpServer() {
		return smtpServer;
	}

	public boolean isUseSSL() {
		return useSSL;
	}

	public boolean isUseTLS() {
		return useTLS;
	}

	private class CustomMessage extends MimeMessage {

		public CustomMessage(Session session, InputStream is) throws MessagingException {
			super(session, is);
		}

		protected void updateMessageID() throws MessagingException {
		}
	}

	private void onMailFailed(String callbackCFC, String appname, String server, String exception, File filename, String customData, MimeMessage msg) {
		onCallback(callbackCFC, "onmailfail", appname, server, exception, filename, customData, msg);
	}

	private void onMailSend(String callbackCFC, String appname, String server, String customData, MimeMessage msg) {
		onCallback(callbackCFC, "onmailsend", appname, server, null, null, customData, msg);
	}

	private void onCallback(String cfcFilter, final String cfcFilterMethod, String appname, String server, String exception, File file, String customData, MimeMessage msg) {
		final cfSession tmpSession = PluginManager.getPlugInManager().createBlankSession();
		if (appname != null) {
			cfApplicationData appData = cfAPPLICATION.getAppManager().getAppData(tmpSession, appname);
			tmpSession.setQualifiedData(variableStore.APPLICATION_SCOPE, appData);
		}

		try {
			final Address[] to = msg.getAllRecipients();
			final cfArrayData array = cfArrayData.createArray(1);
			for (int x = 0; x < to.length; x++)
				array.addElement(new cfStringData(to[x].toString()));

			// Create the CFC we want to call
			final ObjectCFC cfc = PluginManager.getPlugInManager().createCFC(tmpSession, cfcFilter);
			cfc.addArgument("to", 				array);
			cfc.addArgument("from", 			msg.getHeader("From", ","));
			cfc.addArgument("subject", 		msg.getSubject());
			cfc.addArgument("messageid", 	msg.getMessageID());
			cfc.addArgument("mailserver", server);
			cfc.addArgument("customdata", customData);

			if (exception != null)
				cfc.addArgument("error", exception);

			if (file != null)
				cfc.addArgument("file", file.getAbsolutePath());

			new Thread() {
				public void run() {
					try {
						cfc.runMethod(tmpSession, cfcFilterMethod);
					} catch (cfmRunTimeException rte) {
						rte.handleException(tmpSession);
					} catch (Exception e) {
						cfEngine.log("OutgoingMailServer.onCallback.thread():" + e.getMessage());
					} finally {
						tmpSession.pageEnd();
						tmpSession.close();
					}
				}
			}.start();

		} catch (Exception e) {
			cfEngine.log("OutgoingMailServer.onCallback():" + e.getMessage());
		}
	}

	public String getSpoolDirectory() {
		return spoolDirectory.getAbsolutePath();
	}

	public String getUndeliveredDirectory() {
		return failedDirectory.getAbsolutePath();
	}
}