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

package com.naryx.tagfusion.cfm.file;

import java.io.File;

import org.apache.oro.io.Perl5FilenameFilter;
import org.apache.oro.text.regex.Perl5Compiler;


//------------------------------------------------------------

//TODO: convert other fileFilters to use this e.g. com.nary.io.filelist

public class filePatternFilter extends Perl5FilenameFilter{

  boolean             includeDirs = true;

  /*
   * This supports patterns use '*' and '?' as wildcards for many and single 
   * characters respectively. 
   * Example valid filters include *.cfm, *.cf?
   * If _inclDirs is true then directories will result in accept() returning
   * true regardless of the pattern 
   */
		
	public filePatternFilter( String _filter, boolean _inclDirs ){ 
		super( escapeFilter(_filter), Perl5Compiler.CASE_INSENSITIVE_MASK );
    includeDirs = _inclDirs;
	}
	
	public boolean accept( File _file, String _name ){
    if ( (new File( _file, _name )).isDirectory() && includeDirs ){
      return true;
    }else{
      return super.accept( _file, _name );
    }
	}
	
  private static String escapeFilter(String _filter){ 
    String filter = _filter;
    filter = com.nary.util.string.replaceString(filter,"?","\\?");
    filter = com.nary.util.string.replaceString(filter,"+","\\+");
    filter = com.nary.util.string.replaceString(filter,".","\\.");

    filter = com.nary.util.string.replaceString(filter,"\\?",".");
    return com.nary.util.string.replaceString(filter,"*",".*");
  }
	
}
