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
 * This class is used as a communications class between the inner
 * tags of CFMAIL.  It holds all the necessary information associated
 * with MailParts and MailParams
 * 
 */

package com.naryx.tagfusion.cfm.mail;

import java.util.Enumeration;
import java.util.Vector;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;

public class cfMailData extends cfData {

	private static final long serialVersionUID = 1L;

	private Vector<fileAttachment> fileAttachList;
	private Vector<fileAttachment> fileInlineList;
	private Vector<String> headerList;
	private Vector<cfMailPartData> mailParts;

	public cfMailData( cfSession _parent ){
		fileAttachList	 	= new Vector<fileAttachment>();
		fileInlineList    = new Vector<fileAttachment>();
    
    headerList	= new Vector<String>();
		mailParts		= new Vector<cfMailPartData>();
	}

	public int fileAttachmentListSize(){
		return fileAttachList.size();
	}

  public int fileInlineListSize(){
    return fileInlineList.size();
  }

	public int mailPartSize(){
		return mailParts.size();
	}

	public int headerListSize(){
		return headerList.size();
	}

  public void addFile( String _file, String _type ){
    addFile( _file, _type, "ATTACHMENT", null );
  }
  
	public void addFile( String _file, String _type, String _disposition, String _id ){
	  if ( _disposition.equals( "INLINE" ) ){
	    fileInlineList.addElement( new fileAttachment( _file, _type, _disposition.equals( "INLINE" ) ? true : false, _id ) );
    }else{
      fileAttachList.addElement( new fileAttachment( _file, _type, _disposition.equals( "INLINE" ) ? true : false, _id ) );
    }
  }

  
	public Enumeration<fileAttachment> getFileAttachmentList(){
		return fileAttachList.elements();
	}

  public Enumeration<fileAttachment> getInlineFileList(){
    return fileInlineList.elements();
  }

	public void addHeader( String Name, String Value ){
		headerList.addElement( Name + "," + Value );
	}

	public void addMailPart( cfMailPartData mailpartData ){
		mailParts.addElement( mailpartData );
	}

	public Enumeration<cfMailPartData> getMailParts(){
		return mailParts.elements();
	}

	public Enumeration<String> getHeaderList(){
		return headerList.elements();
	}
  
  public String toString(){
		return "{MAILDATA}";
	}
}
