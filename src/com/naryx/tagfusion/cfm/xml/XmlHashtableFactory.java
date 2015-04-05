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
 *  
 *  $Id: $
 */

package com.naryx.tagfusion.cfm.xml;

import org.w3c.dom.Node;

public class XmlHashtableFactory {

	protected static XmlHashtable newXmlHashtable(Node n, boolean caseSensitive) {
		if (n.getNodeType() == Node.DOCUMENT_NODE)
			return new XmlDocumentHashtable(n, caseSensitive);
		else if (n.getNodeType() == Node.ELEMENT_NODE)
			return new XmlElementHashtable(n, caseSensitive);
		else
			return new XmlNodeHashtable(n, caseSensitive);
	}

}
