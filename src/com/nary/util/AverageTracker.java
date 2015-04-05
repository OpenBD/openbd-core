/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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

import java.util.HashMap;
import java.util.Map;

public class AverageTracker extends Object {
	Map<Thread, Long> currentTrackers;

	long total = 0;
	long count = 0;
	long max = 0;
	long min = Long.MAX_VALUE;
	String name;

	public AverageTracker(String name) {
		currentTrackers = new HashMap<Thread, Long>();
	}

	public synchronized void begin() {
		currentTrackers.put(Thread.currentThread(), new Long(System.currentTimeMillis()));
	}

	public synchronized void end() {
		Long time = currentTrackers.get(Thread.currentThread());
		currentTrackers.remove(Thread.currentThread());
		if (time != null) {
			long thisTime = System.currentTimeMillis() - time.longValue();

			total += thisTime;
			count++;

			if (thisTime > max)
				max = thisTime;

			if (thisTime < min)
				min = thisTime;
		}
	}

	public synchronized String getSummary() {
		StringBuilder buffer = new StringBuilder(32);

		buffer.append("AverageTracker: ");
		buffer.append(name);
		buffer.append("\r\n");

		buffer.append("Total Hits    : ");
		buffer.append(count);
		buffer.append(" \r\n");

		if (count != 0) {
			buffer.append("Average Time  : ");
			buffer.append((total / count));
			buffer.append(" ms\r\n");
		}

		buffer.append("Maximum Time  : ");
		buffer.append(max);
		buffer.append(" ms\r\n");

		buffer.append("Minimum Time  : ");
		buffer.append(min);
		buffer.append(" ms\r\n");

		buffer.append("In Progress   : ");
		buffer.append(currentTrackers.size());
		buffer.append("\r\n");

		return buffer.toString();
	}

	public long getAverage() {
		if (count == 0)
			return 0;
		return (total / count);
	}

	public long getMax() {
		return max;
	}

	public long getMin() {
		return min;
	}

	public String getTrackerName() {
		return name;
	}

	public int getActiveCount() {
		return currentTrackers.size();
	}

	public long getCount() {
		return count;
	}

	public long getTotal() {
		return total;
	}
}
