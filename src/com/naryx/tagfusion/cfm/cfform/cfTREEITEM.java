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

package com.naryx.tagfusion.cfm.cfform;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

/**
 * Works exclusively with the cfTREE class.
 */
 
public class cfTREEITEM extends cfTag implements Serializable
{

  static final long serialVersionUID = 1;

	protected void defaultParameters( String _tag ) throws cfmBadFileException{
		defaultAttribute( "TARGET", "_self" );
		defaultAttribute( "EXPAND", "false" );
    parseTagHeader( _tag );

    if (!containsAttribute( "VALUE" ) )
      throw newBadFileException( "Missing ATTRIBUTE", "the following required attributes have not been provided: (VALUE) for the CFTREEITEM tag" );
  }

	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException{

		//--[ Determine if this tag is indeed within the necessary parent tags
    cfTreeData treeData = (cfTreeData)_Session.getDataBin( cfTREE.DATA_BIN_KEY );
    if( treeData == null )
      throw newRunTimeException( "CFTREEITEM must be nested inside tag CFTREE");
    
    String display 	= null;
    if ( containsAttribute( "DISPLAY" ) )
    	display = getDynamic( _Session, "DISPLAY" ).getString();


		//--[ Collate the parameters
    String parent = null;
		if ( containsAttribute( "PARENT" ) )
    	parent = getDynamic( _Session, "PARENT" ).getString();
      
		String href = null;		
    if ( containsAttribute( "HREF" ) ){
			href = getDynamic( _Session, "HREF" ).getString();
			if ( href.length() == 0 ) {
				href = null;
			} else if ( !href.toLowerCase().startsWith("http://") && !href.toLowerCase().startsWith("https://") ){
				//-- Need to prepend the current URI to here
				String hostURI	= (_Session.REQ.isSecure() ? "https://" : "http://") + _Session.REQ.getServerName() + ":" + _Session.REQ.getServerPort();
				if ( href.charAt(0) == '/' ){
					href	= hostURI + hostURI;
				}else{
					String thisURI = _Session.REQ.getContextPath() + _Session.REQ.getServletPath();
					href	= hostURI + thisURI.substring(0,thisURI.lastIndexOf("/")+1) + href;
				}
			}
    }

		String img = null;
		if ( containsAttribute("IMG") ){
			img	= getDynamic(_Session,"IMG").getString();
			if ( img.equalsIgnoreCase("folder") )
				img = "FolderClosed.gif";
			else if ( img.equalsIgnoreCase("document") )
				img = "Document.gif";
			else if ( img.equalsIgnoreCase("floppy") )
				img = "Floppy.gif";
			else if ( img.equalsIgnoreCase("element") )
				img = "Elements.gif";
			else if ( img.equalsIgnoreCase("computer") )
				img = "Computer.gif";
			else if ( img.equalsIgnoreCase("remote") )
				img = "NetworkDrive.gif";
			else if ( img.equalsIgnoreCase("fixed") )
				img = "HardDrive.gif";
			else if ( img.equalsIgnoreCase("dot") )
				img = "dot.gif";
		}

		treeData.setDefaultNode("dot.gif","FolderClosed.gif");

		//-- Add the node into the main tree
		treeData.addNode(	getDynamic( _Session, "VALUE" ).getString(),
                      display,
											parent,
											href,
											img,
											getDynamic( _Session, "EXPAND" ).getBoolean(),
											getDynamic( _Session, "TARGET" ).getString()
										);
		
		return cfTagReturnType.NORMAL;
	}
}
