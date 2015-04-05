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

/*
 * This is a wrapper to the Message Part
 * 
 */
package org.alanwilliamson.openbd.plugin.cfsmtp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.activation.MimeType;
import javax.mail.Multipart;
import javax.mail.Part;

import com.nary.util.UTF7Converter;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;

public class BlueDragonPartWrapper extends cfStructData {
	private static final long serialVersionUID = 1L;
	
  private Part part;
  
  public BlueDragonPartWrapper(Part part){
    this.part = part;
  }

  public cfStringData getContent(){
    try{
      /*
       * Make sure we only get the content for a type we can handle
       */
      if ( part.getFileName() == null && !part.isMimeType("multipart/*") ){
        
        String contentType  = part.getContentType().toLowerCase();
        if ( contentType.indexOf(";") != -1 )
          contentType = contentType.substring( 0, contentType.indexOf(";") );
        
        String dispos   = part.getDisposition();
        MimeType messtype = new MimeType( contentType ); 
        if ( ( dispos == null || dispos.equalsIgnoreCase(Part.INLINE) ) && messtype.match("text/*") ) {
          
          Object content;
          if ( contentType.indexOf( "utf-7") != -1 || contentType.indexOf( "utf7") != -1){
            InputStream ins = part.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            copyFromInToOut(ins,bos);
            content = new String( UTF7Converter.convert( bos.toByteArray() ) );
          }else{
            try{
              content = part.getContent();
            }catch( UnsupportedEncodingException e ){
              content = part.getInputStream();
            }
          }
          
          if ( content instanceof InputStream ){
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            copyFromInToOut((InputStream) content,bos);
            return new cfStringData( new String( bos.toByteArray() ) );
          }else{
            return new cfStringData( content.toString() );
          }
        }
      }

      return cfStringData.EMPTY_STRING;
    }catch(Exception e){
      return cfStringData.EMPTY_STRING;
    }
  }
  
  public cfStringData getContentType(){
    try{
      String tmp  = part.getContentType();
      if ( tmp.indexOf(";") != -1 )
        tmp = tmp.substring( 0, tmp.indexOf(";") );
      
      return new cfStringData( tmp );
    }catch(Exception e){
      return cfStringData.EMPTY_STRING;
    }
  }
  
  public cfBooleanData isFile(){
    try{
      return ( part.getFileName() == null ) ? cfBooleanData.FALSE : cfBooleanData.TRUE;
    }catch(Exception e){
      return cfBooleanData.FALSE;
    }
  }
  
  public cfStringData getFilename(){
    try{
    	return new cfStringData( com.naryx.tagfusion.cfm.mail.cfMailMessageData.getFilename(part) );
    }catch(Exception e){
      return cfStringData.EMPTY_STRING;
    }
  }
  
  
  public cfBooleanData isMultiPart(){
    try{
      return ( part.isMimeType("multipart/*") ) ? cfBooleanData.TRUE : cfBooleanData.FALSE;
    }catch(Exception e){
      return cfBooleanData.FALSE;
    }
  }
  
  
  public cfArrayData getBodyParts(){
    try{
      cfArrayData arr = cfArrayData.createArray( 1 );

      if ( part.isMimeType("multipart/*") ){

        Multipart mp  = (Multipart)part.getContent();
        int count     = mp.getCount();
        for (int i = 0; i < count; i++)
          arr.addElement( new BlueDragonPartWrapper(mp.getBodyPart(i)) );

      }else{
        arr.addElement( this );
      }

      return arr;
    }catch(Exception e){
      return cfArrayData.createArray( 1 );
    }
  }
  
  
  public cfStringData saveFile( String directory ){
    BufferedInputStream in = null;
    BufferedOutputStream out = null;
    FileOutputStream fileOut = null;
    
    try{ 
      File saveDirectory = new File( directory );
      if ( !saveDirectory.isDirectory() )
        saveDirectory.mkdirs();
      
      String filename = com.naryx.tagfusion.cfm.mail.cfMailMessageData.getFilename(part); 
      if ( filename == null || filename.length() == 0 )
        filename = "unknownfile";
        
      File savedFile = new File( saveDirectory, getAttachedFilename( directory, filename ) );
      
      fileOut = new FileOutputStream( savedFile );
      in      = new BufferedInputStream( part.getInputStream() );
      out     = new BufferedOutputStream( fileOut );
      copyFromInToOut(in,out);

      out.flush();
      
      return new cfStringData( savedFile.getAbsolutePath() );
      
    }catch(Exception e){
      return cfStringData.EMPTY_STRING;
    } finally {
      try { out.close(); } catch (IOException e) {}
      try { in.close(); } catch (IOException e) {}
      try { fileOut.close(); } catch (IOException e) {}
    }
  }
  
  private static String getAttachedFilename( String attachDIR, String filename ){
    filename  = filename.replace(' ', '_').replace('/','_');
    File fileN  = new File( attachDIR, filename );
    
    int x = 1;
    while ( fileN.exists() )
      fileN  = new File( attachDIR, (x++) + "_" + filename );
    
    return fileN.getName();
  }

  private void copyFromInToOut(InputStream in, OutputStream out ) throws IOException {
		byte[] bytes = new byte[4096];
		int r = in.read(bytes);
		while (r > 0){
		  out.write(bytes, 0, r);
		  r = in.read(bytes);
		}
  }
  
  public void dumpLong( java.io.PrintWriter out, String _lbl, int _top ){
    dump( out, _lbl, _top );
  }

  public void dump( java.io.PrintWriter out, String _lbl, int _top ) {
    out.write( "<table class='cfdump_table_struct'><tr><th>mailpart</th></tr><tr><td><ul>" );
    out.write( "<li><b>saveFile( directory )</b> if a file saves it to the directory, returning back the full path to the saved file</li>");
    out.write( "<li><b>getBodyParts()</b> returns Array of body parts if this is a multi-part</li>");
    out.write( "<li><b>isMultiPart()</b> returns true or false depending on if its a multi-part</li>");
    out.write( "<li><b>isFile()</b> returns true or false if this is a file</li>");
    out.write( "<li><b>getContentType()</b> returns the content type of the message</li>");
    out.write( "<li><b>getContent()</b> returns the content of the email if text</li>");
    out.write( "<li><b>getFilename()</b> returns the filename if available</li>");
    out.write( "</ul></td><tr><table>" );
  }

}