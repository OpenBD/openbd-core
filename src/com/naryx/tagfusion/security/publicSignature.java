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

public class publicSignature extends Object {

  public String  domain;
  public String  host;
  public String  email;
  private long    dateStarted;
  
  private byte[] signature;
  static final char[] digits = {'0','1','2','3','4','5','6','7',
                                '8','9','a','b','c','d','e','f'};
  
  public publicSignature( String _domain, String _host, String _email ){
    domain      = _domain;
    host        = _host;
    email       = _email;
    dateStarted = System.currentTimeMillis();

    //--[ Package up the data
    signature = new String( domain + "|" + host + "|" + email + "|" + dateStarted ).getBytes();
  }

  public publicSignature( String s ) throws Exception {
    s = s.toLowerCase();
    int len = s.length();
    signature = new byte[ len/2 ];
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
    List<String> tokens = string.split( new String( signature ), "|" );
    if ( tokens.size() != 4 )
      throw new Exception( "invalid signature" );
    
    domain      = tokens.get(0).toString();
    host        = tokens.get(1).toString();
    email       = tokens.get(2).toString();
    dateStarted = com.nary.util.string.convertToLong( tokens.get(3).toString(), -1 );
  }
  
  public String getString(){
  	StringBuilder s = new StringBuilder();
    for ( int x=0; x < signature.length; x++ ){
      byte b = signature[x];
      s.append( digits[(b& 0xf0)>> 4]  );
      s.append( digits[(b& 0x0f)] );
    }
    
    return s.toString().toUpperCase();
  }
  
  public long getChecksum(){
    long checksum = 0;
    for ( int x=0; x < signature.length; x++ )
      checksum = (checksum*x) + signature[x];
    
    return checksum;
  }
  
  public String toString(){
    return "Domain=[" + domain + "] Host=[" + host + "] Email=[" + email + "] Date=[" + dateStarted + "]";
  }
  
  public static void main(String args[]){
    try{
  
    publicSignature pS  = new publicSignature( "n-ary.com", "host50", "alan@n-ary.com" );
    
    String key = pS.getString();
    System.out.println( key );
    
    publicKey pK  = new publicKey( pS.getString() );
    pK.setOptions( publicKey.FREE_LICENSE );
    System.out.println( "PK=" + pK.getPublicKey() );
    
    publicKey PPPP  = new publicKey( pK.getPublicKey(), pS.getString() );
    
    System.out.println( PPPP.getSignature().getString() );
    System.out.println( PPPP.getPublicKey() );
    System.out.println( PPPP.isValid() + "" );
    }catch(Exception E){ System.out.println(E);}
  }
    
}
