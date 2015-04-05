/* 
 *  Copyright (C) 2000 - 2009 TagServlet Ltd
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

package com.naryx.tagfusion.cfm.document;

import java.util.ArrayList;
import java.util.List;

public class DocumentContainer {

	private String mainHeader;
	private String mainHeaderAlign;
	private String mainFooter;
	private String mainFooterAlign;
	
	private List<DocumentSection> sections;
	
	private boolean inSection;
	
	public DocumentContainer(){
		sections = new ArrayList<DocumentSection>();
	}
	
	public void addSection( DocumentSection _section ){
		sections.add( _section );
	}
	
	public List<DocumentSection> getSections(){
		return sections;
	}
	
	public String getMainHeader(){
		return mainHeader;
	}

	public String getMainHeaderAlign(){
		return mainHeaderAlign;
	}

	public String getMainFooter(){
		return mainFooter;
	}

	public String getMainFooterAlign(){
		return mainFooterAlign;
	}

	public void setHeader( String _header, String alignment ){
		if ( inSection ){
			DocumentSection currentSection = sections.get( sections.size()-1 );
			currentSection.setHeader( _header, alignment );
		}else{
			mainHeader = _header;
			mainHeaderAlign = alignment;
		}
	}
	
	public void setFooter( String _footer, String alignment ){
		if ( inSection ){
			DocumentSection currentSection = sections.get( sections.size()-1 );
			currentSection.setFooter( _footer, alignment );
		}else{
			mainFooter = _footer;
			mainFooterAlign = alignment;
		}
		
	}

	public String getCurrentSectionMimeType() {
		if ( sections.size() > 0 ){
			DocumentSection currentSection = sections.get( sections.size()-1 );
			return currentSection.getMimeType();
		}else{
			return null;
		}
  }
	
	public void setInSection( boolean _inSect ){
		this.inSection = _inSect;
	}
	
	/*
	 * usesTotalPageCounters
	 * 
	 * Returns true if one of the headers or footers uses a total page counter that
	 * needs special processing. Detects this by looking for: BD:TOTALPAGECOUNT
	 * and BD:TOTALSECTIONPAGECOUNT.
	 */
	public boolean usesTotalPageCounters(){
		if ((mainHeader != null) && (mainHeader.indexOf("BD:TOTAL") != -1))
			return true;
		
		if ((mainFooter != null) && (mainFooter.indexOf("BD:TOTAL") != -1))
			return true;
		
		for (int i = 0; i < sections.size(); i++){
			if ((sections.get(i).getHeader() != null) && (sections.get(i).getHeader().indexOf("BD:TOTAL") != -1))
				return true;
			
			if ((sections.get(i).getFooter() != null) && (sections.get(i).getFooter().indexOf("BD:TOTAL") != -1))
				return true;
		}

		return false;
	}
}
