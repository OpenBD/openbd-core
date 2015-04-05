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
 */

package com.bluedragon.platform;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;

public interface FileIO {

	public FileSystemManager vfsManager();
	
	public void engineAdminUpdate();
	
	public OutputStream getFileOutputStream(File tempFile) throws IOException;

	public Writer getFileWriter(File outFile) throws IOException;

	public File getWorkingDirectory();
	public File getTempDirectory();
	
	public boolean isRunTimeLoggingEnabled();
	public File getRunTimeLoggingFile();
	public void writeLogFile(File _outFile, String body);
	
	// vfs Helper methods
	public FileObject vfsGetTempDirectory();
}
