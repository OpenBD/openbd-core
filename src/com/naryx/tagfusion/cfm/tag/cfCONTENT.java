/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

package com.naryx.tagfusion.cfm.tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfCONTENT extends cfTag implements Serializable {
	static final long serialVersionUID = 1;


  @SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
  	return createInfo("output", "Controls the MIMETYPE of the page, or sends out a file or binary object to the requester");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
   			createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
   			createAttInfo( "TYPE", "The MIME TYPE of the output page", 	"", false ),
   			createAttInfo( "RESET", "Controls whether the existing content sent is discarded", 	"true", false ),
   			createAttInfo( "DELETEFILE", "If FILE specified, controls whether the file is deleted once its sent", 	"false", false ),
   			createAttInfo( "FILE", "The path to the file", 	"", false ),
   			createAttInfo( "OUTPUT", "The variable that contains the content to send to the request", 	"", false ),
   			createAttInfo( "VARIABLE", "The variable that contains the content to send to the request", 	"", false ),
   			createAttInfo( "URIDIRECTORY", "Is the path to the file relative to the document root", 	"false", false ),
  	};
  }
	
	
	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute("URIDIRECTORY", "NO");
		defaultAttribute("RESET", "YES");
		defaultAttribute("DELETEFILE", "NO");
		parseTagHeader(_tag);
		setFlushable(false);
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);
		
		// Set the mime type
		if (containsAttribute(attributes,"TYPE")) {
			_Session.setContentType(getDynamic(attributes,_Session, "TYPE").getString());
		}

		if (containsAttribute(attributes,"FILE")) {
			_Session.resetBuffer();
			readFile(attributes,_Session);
		} else if (containsAttribute(attributes,"OUTPUT") || containsAttribute(attributes,"VARIABLE")) {
			_Session.resetBuffer();
			outputVariable(attributes,_Session);
		} else if (getDynamic(attributes,_Session, "RESET").getBoolean()) {
			_Session.resetBuffer();
		}

		return cfTagReturnType.NORMAL;
	}

	private void outputVariable(cfStructData attributes, cfSession _Session) throws cfmRunTimeException {
		// invariant: assume that either VARIABLE or OUTPUT exist if we get to this point
		cfData outputData = getDynamic(attributes,_Session, "VARIABLE");
		if (outputData == null) {
			outputData = getDynamic(attributes,_Session, "OUTPUT");
		}

		if (outputData.getDataType() == cfData.CFBINARYDATA)
			_Session.write(((cfBinaryData) outputData).getByteArray());
		else
			_Session.write(outputData.getString().getBytes());

		_Session.abortPageProcessing(); // don't want to process any more after this
	}
	

	private void readFile(cfStructData attributes, cfSession _Session) throws cfmRunTimeException {
		//Create File object
		File thisFile;
		if ( getDynamic(attributes,_Session, "URIDIRECTORY").getBoolean() )
			thisFile = FileUtils.getRealFile(_Session.REQ, getDynamic(attributes,_Session, "FILE").getString());
		else
			thisFile = new File(getDynamic(attributes,_Session, "FILE").getString());

		if (!thisFile.exists())
			throw newRunTimeException("The file does not exist [" + thisFile + "]");

		try {
			// --[ Open up the file
			FileInputStream in = new FileInputStream(thisFile);

			// --[ Work out a reasonable sized buffer
			int filesize = in.available();
			int bufferSize = 8192; // default buffer size
			if (filesize >= 256000)
				bufferSize = 65536;
			else if (filesize >= 65536)
				bufferSize = 16384;

			byte[] buffer = new byte[bufferSize];
			int readCount = 0;
			while ((readCount = in.read(buffer)) != -1) {
				_Session.write(buffer, 0, readCount);
				_Session.pageFlush();
			}

			in.close();

		} catch (IOException E) {
			throw newRunTimeException(E.toString());
		}

		// --[ Check to see if the file is to be deleted or not
		if (getDynamic(attributes,_Session, "DELETEFILE").getBoolean())
			thisFile.delete();
	}
}
