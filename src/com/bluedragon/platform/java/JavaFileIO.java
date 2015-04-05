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
 *  $Id: JavaFileIO.java 1767 2011-11-04 08:08:07Z alan $
 */

package com.bluedragon.platform.java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.StandardFileSystemManager;

import com.bluedragon.platform.FileIO;
import com.nary.io.FileUtils;
import com.nary.util.Date;
import com.naryx.tagfusion.cfm.engine.cfEngine;

public class JavaFileIO implements FileIO {
	public static final String DEFAULT_RUNTIME_LOGGING = "true";
	public static final String DEFAULT_RUNTIME_LOGGING_MAX = "100";

	private File 						workingDirectory, tempDirectory, rteDirectory;
	private boolean 				bRunTimeLogging = true;
	private int 						runTimeLoggingMax = 100;
	private FileSystemManager	fsManager;

	public JavaFileIO(ServletConfig config) throws ServletException{
		
		
		try {
			fsManager = VFS.getManager();
			((StandardFileSystemManager)fsManager).addProvider( "s3", new com.intridea.io.vfs.provider.s3.S3FileProvider() );
			((StandardFileSystemManager)fsManager).addOperationProvider("s3", new com.intridea.io.vfs.provider.s3.acl.AclOperationsProvider() );
		} catch (Exception e1) {
			cfEngine.log("Failed to register the s3:// name space: " + e1.getMessage());
		}
		
		
		// Set the working directory
		try {
			if (!setWorkingDirectory(config.getInitParameter("BLUEDRAGON_WORKING_DIRECTORY"))) {
				throw new Exception(config.getInitParameter("BLUEDRAGON_WORKING_DIRECTORY") + " could not be created");
			}
		} catch (Exception E) {
			System.out.println( cfEngine.PRODUCT_NAME + ": Init Parameter BLUEDRAGON_WORKING_DIRECTORY Error: " + E);
			throw new ServletException( cfEngine.PRODUCT_NAME + ": Init Parameter BLUEDRAGON_WORKING_DIRECTORY Error: " + E);
		}

		
		// Setup the logging
		com.nary.Debug.SystemOff();
		File logFile = new File( getWorkingDirectory(), "bluedragon.log");
		rolloverLogFile(logFile);
		com.nary.Debug.setFilename(logFile.toString());

		setTempDirectory();
		setupRuntimeLogging();
		
		cfEngine.log(cfEngine.PRODUCT_NAME + " WorkingDirectory=[" + getWorkingDirectory() + "]");
		cfEngine.log(cfEngine.PRODUCT_NAME + " TempDirectory=[" + getTempDirectory() + "]");
	}
	
	public void engineAdminUpdate(){
		setupRuntimeLogging();
	}
	
	private void rolloverLogFile(File logFile) {
		// delete the oldest (10th) backup; increment the numbers of the remaining backups
		for (int backupNo = 10; backupNo > 0; backupNo--) {
			File backupFile = new File(logFile + "." + backupNo);
			if (backupFile.exists()) {
				if (backupNo == 10) {
					backupFile.delete();
				} else {
					backupFile.renameTo(new File(logFile + "." + (backupNo + 1)));
				}
			}
		}

		if (logFile.exists()) {
			// Try 3 times to rename bluedragon.log, sleeping 500ms between each attempt.
			int numAttempts = 0;
			while ((numAttempts < 3) && !logFile.renameTo(new File(logFile + ".1"))) {
				numAttempts++;
				try {
					Thread.sleep(500);
				} catch (InterruptedException ignore) {
				}
			}
		}
	}

	
	private boolean setWorkingDirectory(String directory) {
		if (directory == null) {
			directory = System.getProperty("bluedragon.workdir");
			if (directory != null) {
				workingDirectory = new File(directory);
			}
		} else {
			workingDirectory = cfEngine.getResolvedFile(directory);
		}

		if (workingDirectory == null) {
			workingDirectory = (java.io.File)cfEngine.thisServletContext.getAttribute("javax.servlet.context.tempdir");
			workingDirectory = new File(workingDirectory, "bluedragon");
		}

		// --[ Check to see if the directory exists, if not, create it
		if (!workingDirectory.isDirectory() && !workingDirectory.mkdirs())
			return false;
		else
			return true;
	}

	
	private void setTempDirectory() {
		// --[ if the property exists
		tempDirectory = cfEngine.getResolvedFile( cfEngine.thisInstance.getSystemParameters().getString("server.system.tempdirectory"));

		if (tempDirectory == null) {
			tempDirectory = new File(workingDirectory, "temp");
		}

		if (!tempDirectory.isDirectory() && !tempDirectory.mkdirs()) {
			tempDirectory = new File(workingDirectory, "temp");
		}

		// delete temporary native library files
		File[] tempFiles = tempDirectory.listFiles();
		for (int i = 0; i < tempFiles.length; i++) {
			if (tempFiles[i].isFile() && tempFiles[i].getName().startsWith("LIB")) {
				tempFiles[i].delete();
			}
		}
	}
	
	private void setupRuntimeLogging() {
		rteDirectory = new File(tempDirectory, "rtelogs");
		if (!rteDirectory.isDirectory() && !rteDirectory.mkdirs())
			rteDirectory = tempDirectory;

		bRunTimeLogging 	= cfEngine.thisInstance.getSystemParameters().getBoolean("server.system.runtimelogging", Boolean.valueOf(DEFAULT_RUNTIME_LOGGING).booleanValue());
		runTimeLoggingMax = cfEngine.thisInstance.getSystemParameters().getInt("server.system.runtimeloggingmax", Integer.valueOf(DEFAULT_RUNTIME_LOGGING_MAX).intValue() );
	    
		if (bRunTimeLogging)
			cfEngine.log("RunTimeError Directory=[" + rteDirectory + "], Max=[" + runTimeLoggingMax + "]");
	}
	
	public OutputStream getFileOutputStream(File tempFile) throws IOException {
		return new FileOutputStream( tempFile );
	}

	public Writer getFileWriter(File outFile) throws IOException{
		return new FileWriter( outFile );
	}
	
	public File getWorkingDirectory(){
		return workingDirectory;
	}
	
	public File getTempDirectory(){
		return tempDirectory;
	}
	
	
	public void writeLogFile(File _outFile, String body) {
		try {
			cfEngine.log("RTE: " + _outFile);
			FileUtils.writeFile(_outFile, body);
		} catch (Exception E) {}


		/* Write out to the constant file */
		synchronized ( rteDirectory )	{
			try {
				FileUtils.writeFile( new File( rteDirectory, "bderror-latest.html" ), body);
			} catch (Exception E) {}
		}


		// See if writing this log file caused us to go over the max limit
		if ( rteDirectory.listFiles().length >  runTimeLoggingMax )	{

			// Perform the deletion of the oldest log file within a sync block so we don't delete too many log files
			synchronized ( rteDirectory )	{
				// Re-check the number of files since another thread might have deleted one
				File[] files = rteDirectory.listFiles();
				if ( files.length > runTimeLoggingMax ){
					// Find the oldest log file
					int oldest = 0;
					for ( int i = 1; i < files.length; i++ ){
						if ( files[i].lastModified() < files[oldest].lastModified() )
							oldest = i;
					}

					// Delete the oldest log file
					files[oldest].delete();
				}
			}
		}
	}
	
	public boolean isRunTimeLoggingEnabled(){
		return bRunTimeLogging;
	}
	
	public File getRunTimeLoggingFile() {
		try {
			return File.createTempFile("bderror-" 
					+ Date.formatNow( "yyyy-MM-dd-HHmmss" )
					+ "_", 
					".html", rteDirectory);
		} catch (IOException E) {
			cfEngine.log("Failed to create error log file: " + E.getMessage());
			return null;
		}
	}

	@Override public FileSystemManager vfsManager() {
		return fsManager;
	}

	@Override public FileObject vfsGetTempDirectory() {
		try {
			return fsManager.resolveFile( getTempDirectory().getAbsolutePath() );
		} catch (FileSystemException e) {
			cfEngine.log("vfsGetTempDirectory: Failed to create temp directory: " + e.getMessage() );
			return null;
		}
	}
	
}
