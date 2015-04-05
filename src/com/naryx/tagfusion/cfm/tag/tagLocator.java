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

package com.naryx.tagfusion.cfm.tag;

import java.util.Stack;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.file.cfmlFileCache;


/**
  * This class is used mostly by the CUSTOM tags feature of tagServlet.
  * Due to the fact that a TAG can have an optional end-tag, without
  * any prior indication, we have to look through the rest of the file
  * to see if we can see an end tag.  If we find one, we pass that back
  * to the core parsing engine.  This class should only be used by the
  * CFMODULE and all its subclasses.
  */
 
public class tagLocator extends cfTag {

	private static final long serialVersionUID = 1L;
	
  tagReader in;
  String    name, endname;
  
  private static FastMap tagCache = new FastMap();
  private Stack tagStack;
  
  public tagLocator( String tagName, tagReader inFile ){
    in      = inFile;
    name    = tagName.toUpperCase();
    endname = "/" + name;
    in.mark();
    tagStack = new Stack();
  }

  public String findEndMarker() {
    String tagname = null;
    int character;
    int peekindx = 0;

    while ((character = in.peekRead()) != -1) {

      if (character == cfParseTag.TAG_START_MARKER) {
        peekindx = in.getPeekIndx();
        tagname = readTagName().toUpperCase();
        boolean isEndTag = false;
        if ( tagname.length() > 1 && tagname.charAt(0) == '/' ){
          isEndTag = true;
          tagname = tagname.substring(1);
        }
        
        //-- comment tag
        if ( tagname.equals("!---") ) {
          readCommentTag(in);
        
        //-- cfml tag, custom tag or imported tag
        }else if ( tagname.startsWith("CF") || cfmlFileCache.isCustomTag(tagname) || tagname.indexOf(':') != -1 ){
          String restOfTag = readRestOfCFTag(); 

          //-- self closing
          if ( restOfTag.length() > 1 && restOfTag.charAt( restOfTag.length() - 2) == '/' ) { 
            in.setPeekIndx(peekindx);
            
          //-- start tag
          } else if ( !isEndTag ){  
            pushTagStack(tagname);
          
          //-- end tag
          }else{ 
            if (endname.equalsIgnoreCase( "/" + tagname) && ( tagStack.size() == 0 || !tagStack.contains( tagname ) ) ){
              // this has the same tag name and either the tagStack is empty 
              // or we aren't looking for another closing tag of the same name
              return "<" + endname + ">";
            // must have started the search in the body of another tag
            } else if (tagStack.size() == 0) {
              return null;
            } else {
              popTagStack(tagname);
            }
          }
        
        
        }else{
          in.setPeekIndx( peekindx );
        }
      }
    }
    
    return null;
  }

  private String readTagName(){
  	StringBuilder tagname = new StringBuilder();
    int nextChar = in.peekRead();
    while( nextChar != -1 && !cfParseTag.isTagNameTerminatingChar( nextChar ) && nextChar != cfParseTag.TAG_END_MARKER ){
      tagname.append( (char) nextChar );
      nextChar = in.peekRead();
    }
    in.setPeekIndx( in.getPeekIndx()-1 ); // leave it at the tagname terminating char
    return tagname.toString();
  }

  
  private String readRestOfCFTag(){
  	StringBuilder restoftag = new StringBuilder();
    int nextChar = in.peekRead();
    StringBuilder blockChars = new StringBuilder();
    
    while( nextChar != -1 ){
      restoftag.append( (char) nextChar );
      
			//--[ Check for the end marker if none has been specified
			if ( nextChar == cfParseTag.CHAR_DOUBLEQUOTE || nextChar == cfParseTag.CHAR_SINGLEQUOTE || nextChar == cfParseTag.CHAR_HASH ){
				if ( blockChars.length() > 0 ){
					char lastChar = blockChars.charAt( blockChars.length()-1 );
					if ( blockChars.length() > 0 && nextChar == lastChar ){
						blockChars.deleteCharAt( blockChars.length()-1 );
					}else if ( !( ( nextChar == cfParseTag.CHAR_DOUBLEQUOTE && lastChar == cfParseTag.CHAR_SINGLEQUOTE ) 
							|| ( nextChar == cfParseTag.CHAR_SINGLEQUOTE && lastChar == cfParseTag.CHAR_DOUBLEQUOTE ) ) ){
						blockChars.append( (char) nextChar );
					}
				}else{
					blockChars.append( (char) nextChar );
				}
			}else if ( nextChar == cfParseTag.TAG_END_MARKER && blockChars.length() == 0  )
				break;
		
      nextChar = in.peekRead();
    }
    
    return restoftag.toString();
  }


  private void popTagStack( String _tagname ){
    if ( tagStack.size() > 0 ){
      // note that the end tag may not be at the top of the stack due to tags
      // that have optional bodies. It's not til this point that we know there's
    // no body
      int popIndex = -1;
      for ( int i = tagStack.size()-1; i >= 0 ; i-- ){
        if ( ( (String) tagStack.elementAt(i) ).equals( _tagname ) ){
          popIndex = i;
          break;
        }
      }
      
      if ( popIndex != -1 ){
        for ( int i = tagStack.size()-1; i >= popIndex ; i-- ){
          tagStack.pop();
        }
      }
    }
    
  }
  
  private void pushTagStack( String _tagname ){
    
    if ( cfmlFileCache.isCustomTag( _tagname ) || _tagname.indexOf(':') != -1 ){
      tagStack.push( _tagname );
      
    }else{
      cfTag tagClass = (cfTag) tagCache.get( _tagname );
      if ( tagClass == null ){
        try{
          tagClass = (cfTag) Class.forName( cfEngine.thisInstance.TagChecker.getClass( _tagname.toUpperCase() ) ).newInstance();
        }catch(Exception ignored){} // let cfParseTag find this
        
      }
  
      // if we're expecting an end tag then push this on to the stack
      if ( tagClass != null ){
        // if optional end tag assume there is one
        // note that cfPROCESSINGDIRECTIVE is included here as it's the only tag where the existance of
        // an end tag is determined in defaultParameters() - a method that we can't invoke at this point
        if ( tagClass instanceof cfOptionalBodyTag || tagClass.getEndMarker() != null || tagClass instanceof cfPROCESSINGDIRECTIVE ){
          tagStack.push( _tagname );
        }
      }
    }
  }
  
  private static void readCommentTag( tagReader inFile ){
  
  	StringBuilder tag = new StringBuilder(32);
    int counter = 1;
    int character;
  
    while ( (character=inFile.peekRead()) != -1 ){
      tag.append( (char)character );
      
      if ( tag.toString().indexOf( "<!---" ) != -1 ){
        counter++;
        tag = new StringBuilder(32); 
      } else if ( tag.toString().indexOf( "--->" ) != -1 ){
        --counter;
        if ( counter == 0 )
          break;

        tag = new StringBuilder(32);
      }
    }
  }
  
  
}
