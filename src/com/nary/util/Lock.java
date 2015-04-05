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

package com.nary.util;

/**
 * Utility class for locking
 * 
 * We use a simple semaphore to track the number of waiting threads
 * waiting on a lock.  We only remove from the list of locks when
 * there are no more thread waitings.  Since the getLock/removeLock
 * functions are synchronized there is no need to synchronize the
 * lockSemaphore classes.
 * 
 **/

import java.util.HashMap;

public class Lock {

	transient private HashMap locks = new HashMap();

  
	public Object getLock( String key ) {
		synchronized ( locks ){
      lockSemaphore lock = (lockSemaphore)locks.get( key );
			if ( lock == null ){
				lock = new lockSemaphore();
				locks.put( key, lock );
			}
      lock.lock();

			return lock;
		}
	}
	
	public void removeLock( String key ){
		synchronized ( locks ){
      lockSemaphore lock = (lockSemaphore)locks.get( key );
      
      if ( lock == null ){
        return;
      }
      
      lock.unlock();
      if ( lock.isFree() ){
        locks.remove( key );  
      }
		}
	}

  class lockSemaphore {
    int inUse = 0;
    
    public void lock(){
      inUse = inUse + 1;
    }
    
    public void unlock(){
      inUse = inUse - 1;
    }
    
    public boolean isFree(){
      return (inUse == 0);
    }
  }
}
