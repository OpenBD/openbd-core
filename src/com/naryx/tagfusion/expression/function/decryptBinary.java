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

package com.naryx.tagfusion.expression.function;

import com.nary.security.Cryptography;

public class decryptBinary extends encryptBinary {

	private static final long serialVersionUID = 1L;

  
	public java.util.Map getInfo(){
		return makeInfo(
				"security", 
				"Decrypts the given binary with the optional parameters", 
				ReturnType.BINARY );
	}
	

	public String[] getParamInfo() {
		return new String[] {
				"binary to decrypt",
				"decryption key",
				"decryption algorithm to be applied. If not specified, a default of BD_DEFAULT will be used. The CFMX_COMPAT algorithm option is not supported.",
				"the encoding - uu (default), hex or base64 are valid options",
				"the salt to be applied in decryption",
				"the number of iterations"
		};
	}

	
	public decryptBinary(){
		super( Cryptography.DECRYPT_MODE );
	}
}
