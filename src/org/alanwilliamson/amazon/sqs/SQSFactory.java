/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  http://openbd.org/
 *  $Id: SQSFactory.java 2323 2013-02-09 16:50:23Z alan $
 */

package org.alanwilliamson.amazon.sqs;

import org.alanwilliamson.amazon.AmazonKey;
import org.alanwilliamson.amazon.AmazonKeyFactory;
import org.aw20.amazon.SimpleSQS;

public class SQSFactory extends Object {

	public static SimpleSQS getDS(String name){
		String key	= name.toLowerCase().trim();
		
		AmazonKey	amz = AmazonKeyFactory.getDS( key );
		if ( amz == null )
			return null;
		
		return new SimpleSQS( amz.getKey(), amz.getSecret(), amz.getSQSHost() );
	}
}