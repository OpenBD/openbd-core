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

package com.naryx.tagfusion.util;

import java.io.InputStream;
import java.util.Hashtable;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Wrapper class to the XML tag document.
 *
 */
    
public class xmlSAXParser extends DefaultHandler{  
  
  Hashtable<String, TagElement> tagTable;
  TagElement currentTag;
  TagAttributeElement currentAttribute;
  String  tagContent;
  String revision;

  public xmlSAXParser( InputStream xmlFile ) throws Exception{
    tagTable = new Hashtable<String, TagElement>();
    SAXParserFactory factory = SAXParserFactory.newInstance();
    
    SAXParser saxParser = factory.newSAXParser();
    saxParser.parse( xmlFile, this);
  }

  public Hashtable<String, TagElement> getTags(){
    return tagTable;
  }
  
  public String getRevision(){
    return revision;
  }

  //===========================================================
  // SAX DocumentHandler methods
  //===========================================================
  
  public void startElement(String namespaceURI, String eName, String qName, Attributes attrs)throws SAXException{
    if ( qName.equalsIgnoreCase("project") ){
      revision = attrs.getValue( 1 );
    }
    
    if ( qName.equalsIgnoreCase("tag") ){
      currentTag = new TagElement();
      currentAttribute = null;
    } else if (qName.equalsIgnoreCase("attribute")) {
      currentAttribute = new TagAttributeElement();
    }
  }

  public void characters(char buf[], int offset, int len) throws SAXException{   
    tagContent = new String( buf, offset, len );
  }

  public void endElement(String namespaceURI, String sName, String qName )throws SAXException {
    try{
     if ( qName.equalsIgnoreCase("tag") ){
      tagTable.put( currentTag.getName(), currentTag );
      currentTag = null;
      currentAttribute = null;
    } else if (qName.equalsIgnoreCase("attribute")) {
      currentTag.addAttribute( currentAttribute );
      currentAttribute = null;
    } else if ( currentAttribute != null ){
      //--[ round off all the attribute stuff
      if ( qName.equalsIgnoreCase("name") )
        currentAttribute.setName( tagContent );
      else if ( qName.equalsIgnoreCase("info") )
        currentAttribute.setInfo( tagContent );
      else if ( qName.equalsIgnoreCase("required") )
        currentAttribute.setRequired( tagContent );
      else if ( qName.equalsIgnoreCase("supported") )
        currentAttribute.setSupported( tagContent );
      else if ( qName.equalsIgnoreCase("errormessage") )
        currentAttribute.setErrorMessage( tagContent );

    } else if ( currentTag != null ){
      //--[ round off all the tag stuff
      if ( qName.equalsIgnoreCase("name") )
        currentTag.setName( tagContent );
      else if ( qName.equalsIgnoreCase("tagclass") )
        currentTag.setTagClass( tagContent );
      else if ( qName.equalsIgnoreCase("supported") )
        currentTag.setSupported( tagContent );
      else if ( qName.equalsIgnoreCase("info") )
        currentTag.setInfo( tagContent );
      else if ( qName.equalsIgnoreCase("errormessage") )
        currentTag.setErrorMessage( tagContent );
    }
    }catch(Exception E){
      System.out.println( "namespaceURI="+namespaceURI );
      System.out.println( "sName="+sName );
      System.out.println( "qName="+qName );
      E.printStackTrace();
    }
  }
}