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

package com.nary.io;

/**
 * This class returns a list of files from a given directory
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class fileList extends Object {

  java.io.File    rootDir;
  String  				wildCard;

	//------------------------------------------------------------
	
  public fileList( String TopDirectory ) throws FileNotFoundException {
		this( new java.io.File(TopDirectory), "*.*" );
	}
	
  public fileList(  java.io.File TopDirectory ) throws FileNotFoundException {
		this( TopDirectory, "*.*" );
	}
	
  public fileList( String TopDirectory, String WildCard ) throws FileNotFoundException {
    this( new java.io.File(TopDirectory), WildCard );
  }

  public fileList( java.io.File TopDirectory, String WildCard ) throws FileNotFoundException {
    if ( !TopDirectory.isDirectory() )
      throw new FileNotFoundException( TopDirectory.toString() + " is not a valid directory" );

    rootDir   = TopDirectory;
    wildCard  = WildCard;
    if ( wildCard.indexOf(".") != -1 )
      wildCard  = wildCard.substring( wildCard.indexOf(".")+1 ); 
  }

	//------------------------------------------------------------
		
	public List<File> list(){
		  List<File> fileList = new ArrayList<File>();
		  List<cDirInfo> toBeDone = new ArrayList<cDirInfo>();
		
		String dir[] 		= rootDir.list( new fileFilter( wildCard ) );
		cDirInfo tX;
		
		if ( dir != null ){
			tX  = new cDirInfo( rootDir, dir );
			toBeDone.add( tX );
		}
		
		while ( !toBeDone.isEmpty() ){
			tX = toBeDone.get( 0 );
			try{
				int x = 0;
				
				for (;;){
					java.io.File newFile = new java.io.File( tX.rootDir, tX.dirList[x] );
					if ( newFile.isDirectory() ){
						java.io.File t = new java.io.File( tX.rootDir, tX.dirList[x] );
						String a[] = newFile.list( new fileFilter( wildCard ) ); 
						if ( a != null )
							toBeDone.add( new cDirInfo( t, a ) );
							
					}	else {
						fileList.add( newFile );
					}

					x++;
				}
			}	catch( ArrayIndexOutOfBoundsException E ){}
			toBeDone.remove(0);
			dir	= null;
		}
		return fileList;	
  }

	//------------------------------------------------------------
	
  public Vector<File> listDirectories(){
		Vector<File> fileList	= new Vector<File>();
		Vector<cDirInfo> toBeDone = new Vector<cDirInfo>();

		String dir[] 		= rootDir.list( new fileFilter( "*.*" ) );
		cDirInfo tX;
		
		if ( dir != null ){
			tX  = new cDirInfo( rootDir, dir );
			toBeDone.addElement( tX );
		}
		
		while ( !toBeDone.isEmpty() ){
			tX = toBeDone.firstElement();
			try{
				int x = 0;
				
				for (;;){
					java.io.File newFile = new java.io.File( tX.rootDir, tX.dirList[x] );
					if ( newFile.isDirectory() ){
						java.io.File t = new java.io.File( tX.rootDir, tX.dirList[x] );
						String a[] = newFile.list( new fileFilter( wildCard ) ); 
						if ( a != null )
							toBeDone.addElement( new cDirInfo( t, a ) );
						
						fileList.addElement( newFile );
					}

					x++;
				}
			}	catch( ArrayIndexOutOfBoundsException E ){}
			toBeDone.removeElementAt(0);
			dir	= null;
		}
		
		return fileList;	
	}

}

class cDirInfo {
	public java.io.File	rootDir;
	public String[] dirList;

	public cDirInfo( java.io.File _r, String[] _d ){
		rootDir	= _r;
		dirList = _d;
	}
}

class fileFilter implements FilenameFilter {

  String wildC;
  boolean acceptAll;

  public fileFilter( String wildCard ){
    wildC = wildCard;
    if ( wildCard.equals( "*" ) ){
      acceptAll = true;
    }else{
      acceptAll = false;
    }
    
  }

	public boolean accept(java.io.File dir, String name){
    if ( acceptAll ) return true;
    
    java.io.File tF	= new java.io.File( dir, name );

		if ( tF.isDirectory() )
			return true;

		int indx = name.lastIndexOf( "." );
		if ( indx == -1 )
			return false;

		String Ext = name.substring( indx+1, name.length() ).toLowerCase();
		if ( Ext.equals( wildC ) )
			 return true;

		return false;
	}
}
