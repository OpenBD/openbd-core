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

package com.nary.net.http;


/**
 * tagProcessor
 *
 * this class, given a url and port, will resolve urls in tags
 * passed to it when a tagFilterInputStream instance calls process tag.
 * Note this convert all urls. Only :
 *    - img src 
 *    - a href 
 *    - form action 
 *    - applet code 
 *    - script src 
 *    - embed src 
 *    - embed pluginspace 
 *    - body background 
 *    - frame src 
 *    - bgsound src 
 *    - object data 
 *    - object classid 
 *    - object codebase 
 *    - object usemap 
 *
 */


import java.io.ByteArrayOutputStream;

import com.nary.net.tagFilterInputStream;
import com.nary.net.tagListener;
import com.nary.util.byteArray;


public class tagProcessor implements tagListener{ 
  private ByteArrayOutputStream wordStream;

  private final static byte DEFAULT=0, BASE=1;

  private String sourceURL;
  private String baseURL = null;

  private final static int capsToSmallGap = (int)'a' - (int)'A';
  // for use in reading in and coverting tags
  private ByteArrayOutputStream temp;
  private byte [] buffer;
  private int bufferAt;

	private UrlLinkResolver urlutils;

  public tagProcessor( String _url, int _port ){
    // sourceURL must be absolute
    sourceURL = _url;
    // convert the url so that it contains all /'s as opposed to \'s,
    // and so that it ends with a '/'
    sourceURL.replace('\\', '/');
    // ensure url ends with a /
    if (sourceURL.lastIndexOf('/') <= 7){
      sourceURL += "/";
    }
    if (!sourceURL.endsWith("/")){
      sourceURL = sourceURL.substring(0, sourceURL.lastIndexOf("/") + 1);
    }

    int thirdSlashIndex = sourceURL.indexOf('/', 7);
    // if a port number isn't given in the url, add it in
    if (sourceURL.indexOf(':', sourceURL.indexOf(':') + 1) == -1){
      sourceURL = sourceURL.substring(0, thirdSlashIndex) + ":" + _port + sourceURL.substring(thirdSlashIndex);
    }

    temp 				= new ByteArrayOutputStream();
    wordStream 	= new ByteArrayOutputStream();
    urlutils		= new UrlLinkResolver();
  }//tagProcessor()
  

  public byte[] processTag( byte [] _tag ) {
    buffer = _tag;
    bufferAt = 0;

    // reset stream used for copying new tag into
    temp.reset();

    // notes: first byte should be a '<', last byte should be a '>' (might not be LATER)
    bufferAt = 0;

    // get the '<'
    temp.write(buffer[bufferAt]);
    bufferAt++;

    skipWhitespace();

    byte [] firstWord = getNextWord();

    try{
      temp.write(firstWord);
    }catch(java.io.IOException ignored){}


    // if A
    if (byteArray.equalsIgnoreCase(firstWord, new byte[]{(byte)'A'})){
      // get href
      processRestOfTag(new byte[][]{{(byte)'h',(byte)'r',(byte)'e',(byte)'f'}}, DEFAULT);
    }

    // if form
    else if (byteArray.equalsIgnoreCase(firstWord, new byte[]{(byte)'F',(byte)'O',(byte)'R',(byte)'M'})){
      // get action
      processRestOfTag(new byte[][]{{(byte)'a',(byte)'c',(byte)'t',(byte)'i',(byte)'o',(byte)'n'}}, DEFAULT);
    }

    // if embed
    else if (byteArray.equalsIgnoreCase(firstWord, new byte[]{(byte)'E',(byte)'M',(byte)'B',(byte)'E',(byte)'D'})){
      // get pluginspace, src
      processRestOfTag(new byte[][]{{(byte)'p',(byte)'l',(byte)'u',(byte)'g',(byte)'i',(byte)'n',(byte)'s',(byte)'p',(byte)'a',(byte)'c',(byte)'e'},{(byte)'s',(byte)'r',(byte)'c'}}, DEFAULT);
    }

    // if frame, bgsound, img, script, base
    else if (byteArray.equalsIgnoreCase(firstWord, new byte[]{(byte)'F',(byte)'R',(byte)'A',(byte)'M',(byte)'E'})
      || byteArray.equalsIgnoreCase(firstWord, new byte[]{(byte)'B',(byte)'G',(byte)'S',(byte)'O',(byte)'U',(byte)'N',(byte)'D'})
      || byteArray.equalsIgnoreCase(firstWord, new byte[]{(byte)'S',(byte)'C',(byte)'R',(byte)'I',(byte)'P',(byte)'T'})
      || byteArray.equalsIgnoreCase(firstWord, new byte[]{(byte)'I',(byte)'M',(byte)'G'})){
      // get src
      processRestOfTag(new byte[][]{{(byte)'s',(byte)'r',(byte)'c'}}, DEFAULT);
    }

    // if base
    else if (byteArray.equalsIgnoreCase(firstWord, new byte[]{(byte)'B',(byte)'A',(byte)'S',(byte)'E'})){
      // get src
      processRestOfTag(new byte[][]{{(byte)'s',(byte)'r',(byte)'c'}}, BASE);
    }

    // if body
    else if (byteArray.equalsIgnoreCase(firstWord, new byte[]{(byte)'B',(byte)'O',(byte)'D',(byte)'Y'})){
      // get background
      processRestOfTag(new byte[][]{{(byte)'b',(byte)'a',(byte)'c',(byte)'g',(byte)'r',(byte)'o',(byte)'u',(byte)'n',(byte)'d'}}, DEFAULT);
    }

    // NOTE: Object and Applet tags are special cases that may involve a codebase
    // if object
    else if (byteArray.equalsIgnoreCase(firstWord, new byte[] {(byte)'O',(byte)'B',(byte)'J',(byte)'E',(byte)'C',(byte)'T'})){
      processObjectTag();
    }

    // if applet
    else if (byteArray.equalsIgnoreCase(firstWord, new byte[] {(byte)'A',(byte)'P',(byte)'P',(byte)'L',(byte)'E',(byte)'T'})){
      processAppletTag();
    }

    else{
      // not a tag that has any urls that require resolving, so just
      // return the untouched buffer
      return buffer;
    }

    // get the '>'
    temp.write(buffer[bufferAt]);
    bufferAt++;

    return temp.toByteArray();
  }// checkTag()


  private void processRestOfTag(byte [][] _keywords, byte _tagType) {
    try{
      int bufferLen = buffer.length;
      // while haven't reached the '>'
      while (bufferAt < bufferLen-1){

        skipWhitespace();
  
        byte [] word = getNextWord();
        temp.write(word);

        // if it's a tag then get the value
        if (isKeyword(word, _keywords) != -1){
          skipWhitespace();
          if (buffer[bufferAt] == '='){
            temp.write(buffer[bufferAt]);
            bufferAt++;
            skipWhitespace();
            processURI(getURI(), _tagType);
          }
        }

      }// while
    }catch (java.io.IOException ignored){}
    
  }//processRestOfTag()


  private void skipWhitespace(){
    // skip LWS
    while ((bufferAt < buffer.length) && buffer[bufferAt] == ' ' ||
          buffer[bufferAt] == '\r' || buffer[bufferAt] == '\n' ||
	        buffer[bufferAt] == '\t'){
      temp.write(buffer[bufferAt]);
      bufferAt++;
    }
  }// skipWhitespace


  /**
   * returns the next word in the buffer (not the stream)
   * [used to parse the buffer]
   */
  private byte[] getNextWord(){
    wordStream.reset();

    // while haven't reached the end of the tag & current character is ok
    while((bufferAt < buffer.length-1) && (isChar(buffer[bufferAt]))){
      wordStream.write(buffer[bufferAt]);
      bufferAt++;
    }

    return wordStream.toByteArray();
  }// getNextWord


  // return if character is a legal character other than '='
  // except in case where '=' is treated as a word itself
  private boolean isChar(byte ch){
    return ((ch < 0) || (ch > 32 && ch < 256 && ch != 61) || (ch == 61 && wordStream.size() == 0));//((ch >= 65 && ch <=90) || (ch >= 97 && ch <= 122));
  }// isChar()


  /**
   * returns true if the given word is a tag keyword from the tag list 'tags'
   */

  private int isKeyword(byte[] word, byte[][] _keywords){
    int keywordIndex = 0;
    int wordIndex = 0;

    // check for each known tag
    for (int keywordNum = 0; keywordNum < _keywords.length; keywordNum++){
      keywordIndex = 0;
      int wordLen = _keywords[keywordNum].length;

      // no point comparing this tag if word lengths don't match
      if (word.length != wordLen)
        continue;

      // while the char in the word matches the char in the tag
      // AND the end of the tag hasn't been reached
      while (keywordIndex < wordLen && (toSmall(word[wordIndex]) == _keywords[keywordNum][keywordIndex])){
        wordIndex++;
        keywordIndex++;
      }

      if (keywordIndex == wordLen){
        return keywordNum;
      }
    }

    // no tags match
    return -1;
  }// isKeyword()


  /**
   * gets the next uri from the byte stream returning it as a byte[]
   */

  private byte[] getURI() {
    wordStream.reset();
    // if next char is " then get next chars up til the next "
    if (buffer[bufferAt] == '"' || buffer[bufferAt] == '\''){
      // don't write the "
      bufferAt++;

      this.skipWhitespace();
      //if the uri given is just "    "
      if (buffer[bufferAt] == '"' || buffer[bufferAt] == '\''){
        return new byte[0];
      }

      // while haven't reached the end '>' or the " for
      while ((bufferAt < buffer.length-1) && (buffer[bufferAt] != '"') &&
            (buffer[bufferAt] != '\'')){
        wordStream.write(buffer[bufferAt]);
	      bufferAt++;
      }

      // if stopped looping because " found
      if (bufferAt != buffer.length-1){
	      // don't write the "
	      bufferAt++;
      }

    }
    // else get the next chars up til the next white space or carriage return
    else{
      // fix this line to make it more efficient
      while ((bufferAt < buffer.length-1) && (buffer[bufferAt] != '"')
        && (buffer[bufferAt] != '\'') && buffer[bufferAt] != '\n' && buffer[bufferAt] != ' '){
	      wordStream.write(buffer[bufferAt]);
	      bufferAt++;
      }

      // if stopped looping because ", or ' found
      if (buffer[bufferAt] == '=' || (buffer[bufferAt] == '\'')){
	      // write the "
        wordStream.write(buffer[bufferAt]);
        bufferAt++;
      }
    }

    return wordStream.toByteArray();

  }// getURI


  /**
   *  processes the given url depending on the operation given
   *  if the op is DEFAULT, then encode the given url
   *  if the op is BASE, then set the BASE url as the given url
   *  @param in - the url to be processed
   *  @param op - the operation to be performed
   **/

  private void processURI(byte[] in, int op) {

    try{
      // if url is not an http url then
      if (!isHttpURL(in)){
        //leave the url as it is
        temp.write('"');
        temp.write(in);
        temp.write('"');
        return;
      }

      switch (op){
        case DEFAULT:

          if (baseURL == null){
            temp.write('"');
            temp.write((urlutils.encode(new String(in), sourceURL)).getBytes());
            temp.write('"');
          }else{
            temp.write('"');
            String resolved1 = urlutils.encode(baseURL, sourceURL);
            temp.write((urlutils.encode(new String(in), resolved1)).getBytes());
            temp.write('"');
          }
          break;
        case BASE:
        	// set BASE
        	temp.write('"');
          temp.write(in);
          temp.write('"');
          baseURL = (urlutils.encode(new String(in), sourceURL));
        	break;
		default:
		  throw new IllegalStateException("invalid op - " + op);
	  }//switch
    }catch (java.io.IOException ignored){}
  }// processURI()


  /**
   * resolves the code uri relative to the codebase uri if one exists
   */

  private void processAppletTag() {
    try{
      int bufferLen = buffer.length;
      // keywords - code, codebase

      byte [] codeURL = null;
      byte [] codebaseURL = null;
      String fullCodebase = null;

      // while haven't reached the '>'
      while (bufferAt < bufferLen-1){

        skipWhitespace();

        byte [] word = getNextWord();

        int wordIndex = isKeyword(word, new byte[][]{{(byte)'c',(byte)'o',(byte)'d',(byte)'e'},{(byte)'c',(byte)'o',(byte)'d',(byte)'e',(byte)'b',(byte)'a',(byte)'s',(byte)'e'}});
      
        // if code
        if (wordIndex == 0){
          skipWhitespace();
          bufferAt++; // skip the '='
          skipWhitespace();
          codeURL = getURI();
        
        // if codebase
        }else if (wordIndex == 1){
          skipWhitespace();
          bufferAt++; // skip the '='
          skipWhitespace();
          codebaseURL = getURI();
          fullCodebase = urlutils.encode(sourceURL, new String(codebaseURL));
          temp.write(word);
          temp.write('=');
          temp.write('"');
          temp.write(fullCodebase.getBytes());
          temp.write('"');
        
        }else{
          temp.write(word);
        }


      }// while

      if (codeURL != null){ // unlikely that it does equal null 
        temp.write(new byte[]{(byte)'C',(byte)'O',(byte)'D',(byte)'E',(byte)'=',(byte)'"'});

        if (fullCodebase != null){
          temp.write((urlutils.encode(new String(codeURL), fullCodebase)).getBytes());
      
        }else{
          temp.write((urlutils.encode(new String(codeURL), sourceURL)).getBytes());
        }

        temp.write('"');
      }

    }catch (java.io.IOException ignored){}  

  }// processAppletTag()


  /**
   * resolves the data uri relative to the codebase uri if one exists
   */

  private void processObjectTag() {
    try{
      // keywords - data, classid, usemap, codebase
      int bufferLen = buffer.length;

      byte [] dataURL = null;
      byte [] codebaseURL = null;
      String fullCodebase = null;

      // while haven't reached the '>'
      while (bufferAt < bufferLen-1){

        skipWhitespace();

        byte [] word = getNextWord();

        int wordIndex = isKeyword(word, new byte[][]{{(byte)'d',(byte)'a',(byte)'t',(byte)'a'},{(byte)'c',(byte)'o',(byte)'d',(byte)'e',(byte)'b',(byte)'a',(byte)'s',(byte)'e'},
          {(byte)'u',(byte)'s',(byte)'e',(byte)'m',(byte)'a',(byte)'p'}, {(byte)'c',(byte)'l',(byte)'a',(byte)'s',(byte)'s',(byte)'i',(byte)'d'}});
      
        // if code
        if (wordIndex == 0){
          skipWhitespace();
          bufferAt++; // skip the '='
          skipWhitespace();
          dataURL = getURI();
        
        // if codebase
        }else if (wordIndex == 1){
          skipWhitespace();
          bufferAt++; // skip the '='
          skipWhitespace();
          codebaseURL = getURI();
          fullCodebase = urlutils.encode(new String(codebaseURL), sourceURL);
          temp.write(word);
          temp.write('=');
          temp.write('"');
          temp.write(fullCodebase.getBytes());
          temp.write('"');
        
        }else if (wordIndex == 2 || wordIndex == 3){
          temp.write(word);
          skipWhitespace();
          bufferAt++; // skip the '='
          temp.write('=');
          skipWhitespace();
          temp.write('"');
          processURI(getURI(), DEFAULT);
          temp.write('"');

        }else{
          temp.write(word);
        }

      }// while

      if (dataURL != null){ // unlikely that it does equal null 
        temp.write(new byte[]{(byte)'D',(byte)'A',(byte)'T',(byte)'A',(byte)'=',(byte)'"'});

        if (fullCodebase != null){
          temp.write((urlutils.encode(new String(dataURL), fullCodebase)).getBytes());
        }else{
          temp.write((urlutils.encode(new String(dataURL), sourceURL)).getBytes());
        }

        temp.write('"');
      }

    }catch (java.io.IOException ignored){}
  }// processObjectTag()



  private static boolean isHttpURL(byte [] in){
    // check first if starts with http:
    // if uri put in is long enough to check that it begins with http:
    if (in.length > 5){
      // check if uri begins with "http:"
      if (in[0] == 'h' && in[1] == 't' && in[2] == 't' && in[3] == 'p' && in[4] == ':'){
        return true;
      }
    }

    // check if this is a relative url i.e. - the uri doesn't specify a protocol
    int index = 0;
    while (index < in.length){
      // if a colon is found then all chars previous to this make up the
      // protocol and hence this isn't http. Note that this colon cannot
      // be the colon preceding the port number since www.somesite.com:80 is
      // an invalid uri without the http://
      if (in[index] == ':'){
        return false;
      }

      // if the character is not a valid char for a protocol then assume
      // this is a relative http url - so return true
      if (!((in[index] >= 'A' && in[index] <= 'Z') ||
            (in[index] >= 'a' && in[index] <= 'z') ||
            (in[index] >= '0' && in[index] <= '9') ||
            in[index] == '+' || in[index] == '-' || in[index] == '.')){
        return true;
      }

      index++;

    }// while
    // if reached the end of the uri without finding the end of a protocol
    // (denoted by a ':') then must be a relative http url
    return true;

  }// isHttpURL()


  private static byte toSmall(byte in){
    if (in >= 'A' && in <= 'Z')
      return (byte)(in + capsToSmallGap);
    else
      return in;
  }

	
	public static void main( String [] args ){
		tagProcessor tp = new tagProcessor( "http://www.n-ary.com", 80 );
		tagFilterInputStream is = new tagFilterInputStream( new java.io.ByteArrayInputStream( ("<IMG SRC=\"/image/pic.gif\"><IMG SRC=\"/image/pic.gif\">").getBytes() ) );
		is.registerTagListener( tp );
		
		try{
			int ch = is.readChar();
			while( ch != -1 ){
				System.out.print( (char) ch );
				ch = is.readChar();
			}
		}catch( java.io.IOException ioe ){
			ioe.printStackTrace();
		}
		
	}

}// tagProcessor