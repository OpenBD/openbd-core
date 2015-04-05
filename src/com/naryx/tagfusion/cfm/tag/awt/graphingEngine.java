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

package com.naryx.tagfusion.cfm.tag.awt;

import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.file.cfmlFileCache;
import com.naryx.tagfusion.cfm.file.cfmlURI;

/*
 * This class was used to handle the IMG tag produced from the CFGRAPH tag.
 * The CFGRAPH tag was removed in BD 7.0 so now this class is only needed for
 * the CFCAPTCHA tag.
 */

public class graphingEngine extends cfFile {
 	private static final long serialVersionUID = 1L;

	public graphingEngine(){
  	super();
  }

  public graphingEngine(cfSession _Session){
    super();

    cfmlFileCache.insertCfmlFile( this, new cfmlURI( _Session.REQ, "/CFGraphEngine.cfm" ), _Session.REQ );
    cfEngine.log( "-] Graphics Engine Started $Revision: 1323 $" );
  }
  
  public String getURL(cfSession Session){ 
		return Session.REQ.getContextPath() + "/CFGraphEngine.cfm"; 
	}

}
