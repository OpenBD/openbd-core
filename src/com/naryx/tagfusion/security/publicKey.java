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

package com.naryx.tagfusion.security;

import java.util.List;

import com.nary.util.string;

public class publicKey extends Object {

  publicSignature pS;
  long  options;
  long  checksum;

  public final static int HOST_LICENSE    = 55521;
  public final static int DOMAIN_LICENSE  = 17737;
  public final static int FREE_LICENSE    = 23479;

  public publicKey( String signature ) throws Exception {
    pS = new publicSignature( signature );
  }

  public publicKey( String s, String SIGN ) throws Exception {
    pS = new publicSignature( SIGN );
  
    s = s.toLowerCase();
    int len = s.length();
    byte[] signature = new byte[ len/2 ];
    for ( int x=0; x < signature.length; x++ ){
      int digit1 = s.charAt( x*2 );
      int digit2 = s.charAt( x*2 + 1 );
      if ( (digit1 >= '0') && (digit1 <= '9') ) 
        digit1 -= '0';
      else if ( (digit1 >= 'a') && (digit1 <= 'f'))
        digit1 -= 'a' - 10;

      if ( (digit2 >= '0') && (digit2 <= '9') ) 
        digit2 -= '0';
      else if ( (digit2 >= 'a') && (digit2 <= 'f'))
        digit2 -= 'a' - 10;
        
      signature[x]  = (byte)((digit1 << 4) + digit2 );
    }
    
    //--[ Decode this
    try{
    	List<String> tokens = string.split( new String( signature ), "|" );
      checksum  = com.nary.util.string.convertToLong( tokens.get(0).toString(), -1 );
      options   = com.nary.util.string.convertToLong( tokens.get(1).toString(), -1 );
    }catch(Exception E){}
  }

  public publicSignature getSignature(){
    return pS;
  }

  public void setOptions( long _options ){
    options = _options;
  }
  
  public long getOptions(){
    return options;
  }
  
  public String getHost(){
    return pS.host;
  }

  public String getDomain(){
    return pS.domain;
  }
  
  public String getEmail(){
    return pS.email;
  }
  
  public String getPublicKey(){
    return getString( new String( pS.getChecksum() + "|" + options).getBytes() );
  }

  public boolean isValid(){
    if ( pS.getChecksum() == checksum )
      return true;
    else
      return false;  
  }

  static final char[] digits = {'0','1','2','3','4','5','6','7',
                                '8','9','a','b','c','d','e','f'};
  
  private static String getString( byte[] signature ){
  	StringBuilder s = new StringBuilder();
    for ( int x=0; x < signature.length; x++ ){
      byte b = signature[x];
      s.append( digits[(b& 0xf0)>> 4]  );
      s.append( digits[(b& 0x0f)] );
    }
    
    return s.toString().toUpperCase();
  }
  
  public static void main(String args[]){
    try{
    
      publicKey pK  = new publicKey( args[0] );
      pK.setOptions( FREE_LICENSE );
      
      System.out.println( "Key= " + pK.getPublicKey() );
    
    }catch(Exception E){}
  }  
}
