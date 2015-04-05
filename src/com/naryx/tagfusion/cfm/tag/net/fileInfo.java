/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  $Id: fileInfo.java 2214 2012-07-27 01:49:42Z alan $
 */

package com.naryx.tagfusion.cfm.tag.net;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nary.util.string;

/**
 * This class stores File information. For each of the file, FTPProtocol class needs to store 
 * additional information like size, date and time. This information has to be sent to any User 
 * Interface components registered with the FTPProtocol class. This class has getter and setter 
 * methods for time, date,size,name and file type.
 */

public class fileInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static Pattern regex = Pattern.compile(
			"([bcdlfmpSs-])"
		       +"(((r|-)(w|-)([xsStTL-]))((r|-)(w|-)([xsStTL-]))((r|-)(w|-)([xsStTL-])))\\+?\\s+"
		       + "(\\d+)\\s+"
		       + "(\\S+)\\s+"
		       + "(?:(\\S+)\\s+)?"
		       + "(\\d+)\\s+"
		       + "((?:\\d+[-/]\\d+[-/]\\d+)|(?:\\S+\\s+\\S+))\\s+"
		       + "(\\d+(?::\\d+)?)\\s+"
		       + "(\\S*)(\\s*.*)",
				    Pattern.MULTILINE);

	private String mode = "";
	private String fileName = "";
	private String filepath = "";
	private String size = "";
	private String date = "";
	private String time = "";

	private boolean file; // If file is true then it is regular file else directory.

	/*
	 * 
	 * In some cases (e.g. ftp.sunet.se or ftp.ludd.luth.se) processing the directory list is shifted by one. When we encounter such a situation, we shift back with one position in method setRemoteDescriptionUNIX().
	 * 
	 * Look at the following dir listings:
	 * 
	 * funet.fi (standard unix reply): lrwxrwxrwx 1 root guru 32 Oct 28 1999 README -> /pub/files/staff-docs/README.FTP lrwxrwxrwx 1 root guru 38 Oct 28 1999 README.FILETYPES -> /pub/files/staff-docs/README.FILETYPES lrwxrwxrwx 1 root guru 34 Apr 30 07:04 README.IP-REVERSAL -> /ftp/staff-docs/README.IP-REVERSAL lrwxrwxrwx 1 root guru 37 Oct 28 1999 README.UPLOADER ->
	 * /pub/files/staff-docs/README.UPLOADER lrwxrwxrwx 1 root guru 10 Oct 22 1999 files -> /pub/files lrwxrwxrwx 1 root guru 1 Oct 30 1999 ftp -> . drwxr-xr-x 56 root metadb 8192 Aug 13 13:32 incoming lrwxrwxrwx 1 root guru 16 Oct 22 1999 index -> /pub/files/index lrwxrwxrwx 1 root guru 40 Jul 24 2000 internet-drafts -> /pub/mirrors/ftp.isi.edu/internet-drafts lrwxrwxrwx 1 root guru 23 Dec 4 1999
	 * ls-lR.gz -> /pub/files/all/ls-lR.gz drwxrwxr-x 45 hks maints 8192 Aug 28 09:32 pub lrwxrwxrwx 1 root guru 33 Mar 27 14:06 rfc -> /pub/mirrors/ftp.isi.edu/in-notes
	 * 
	 * ftp.ludd.luth.se: note that the group info is missing from this:
	 * 
	 * drwx------ 4 other 512 Feb 15 2001 .00-NOPUBLIC drwxr-xr-x 4 root 512 Nov 23 2000 .1 -rw-r--r-- 1 other 2890 Aug 1 1999 FtpUsersApplet.class -rw-r--r-- 1 other 564 Aug 1 1999 FtpUsersApplet.html -rw-r--r-- 1 other 810 Mar 7 2000 README -rw-r--r-- 1 other 869 Apr 4 2000 README.MIRRORS-ftp.luth.se.txt -rw-r--r-- 1 other 151 Mar 7 2000 README.access_methods -rw-r--r-- 1 other 3036 Aug 1 1999
	 * README.mirrors.old drwxr-xr-x 2 other 512 May 9 22:44 bin lrwxrwxrwx 1 other 26 Feb 11 2001 debian -> pub/OS/Linux/Debian/debian lrwxrwxrwx 1 other 34 Feb 11 2001 debian-non-US -> pub/OS/Linux/Debian/debian-non-US/ drwxr-xr-x 2 other 512 Feb 11 2001 dev drwxr-xr-x 4 other 512 Feb 14 2001 etc drwxr-xr-x 15 other 512 Feb 15 2001 pub drwxr-xr-x 5 other 512 Feb 11 2001 usr
	 * 
	 * incidentally, serv-u under Win uses the standard unix format as above:
	 * 
	 * drwxrwxrwx 1 user group 0 Sep 7 15:02 wstk-2.3 drwxrwxrwx 1 user group 0 Sep 10 11:35 xmas drwxrwxrwx 1 user group 0 Sep 10 14:25 xml -rwxrwxrwx 1 user group 1645 Jul 31 11:03 xml!!!!1 -rwxrwxrwx 1 user group 3107273 Jul 20 14:36 XML4J-J-bin.3.2.0.zip -rwxrwxrwx 1 user group 910 Apr 17 13:54 XMLDocument.jad drwxrwxrwx 1 user group 0 Jun 6 13:47 xplorer -rwxrwxrwx 1 user group 75264 Jul 11
	 * 10:46 Yo.doc -rwxrwxrwx 1 user group 18037 Jul 11 10:38 yotekstit.txt
	 */


	/**
	 * Creates an object of FileInfo class. The fields of the FileInfo object are initially null, they must be initialized using appropriate setter methods.
	 */
	public fileInfo() {
	}

	public long getLastModified() {
		com.nary.util.date.dateTimeTokenizer dateTime = new com.nary.util.date.dateTimeTokenizer(date + " " + time);
		dateTime.validateStructure();
		return dateTime.getDate().getTime();
	}

	public java.lang.String getName() {
		return fileName;
	}

	public String getPath() {
		return filepath;
	}

	public long getLength() {
		if (file) {
			return com.nary.util.string.convertToLong(size, 0);
		}
		return 0;
	}

	public boolean isDirectory() {
		return !file;
	}// isFile()

	public String getAttributes() {
		return (file ? "Normal" : "Directory");
	}// getAttributes()

	public String getMode() {
		return mode;
	}// getMode()

	private static int getMonthIndex(String text) {
		String mon = text.toUpperCase();
		if (mon.equals("JAN"))
			return 0;
		else if (mon.equals("FEB"))
			return 1;
		else if (mon.equals("MAR"))
			return 2;
		else if (mon.equals("APR"))
			return 3;
		else if (mon.equals("MAY"))
			return 4;
		else if (mon.equals("JUN"))
			return 5;
		else if (mon.equals("JUL"))
			return 6;
		else if (mon.equals("AUG"))
			return 7;
		else if (mon.equals("SEP"))
			return 8;
		else if (mon.equals("OCT"))
			return 9;
		else if (mon.equals("NOV"))
			return 10;
		else if (mon.equals("DEC"))
			return 11;

		return -1;
	}// isMonth()

	/**
	 * Sets the name of the file represented by this FileInfo object.
	 * 
	 * @param fileName
	 *          name of the file represented by this FileInfo object.
	 */
	public void setName(String _fileName) {
		fileName = _fileName;
	}

	public void setFilepath(String _filepath) {
		filepath = _filepath;
	}

	/**
	 * This method will set attributes for a remote file. Given a line of reply received as the result of "LIST" command, this method will set all the attributes(name, size,time and date) of the named file. This method requires the reply to be in MVS (??) format. For example, "SMS015 3390   1999/07/13  1    2  FB      80 23440  PO  ISPF.ISPPROF"
	 * 
	 * And this can be a problem, because the Used column can run into the Ext column:
	 * 
	 * "SMSX09 3390   1999/07/15  117069  FB    2048 26624  PS  #TESTDAT.#1234"
	 * 
	 * Note for the record that MVS may also migrate a dataset, i.e.: "Migrated                                                LOG.MISC"
	 * 
	 * is a possible response to the system. Of course, the system also writes files directly to tape, a la:
	 * 
	 * "P01479 Tape                                             THISN.THAT"
	 * 
	 * @param reply
	 *          reply of FTP server for "dir" command.
	 */

	public boolean setRemoteDescriptionMVS(String reply) {

		// reply represents each line read while listing.
		List<String> tokens;
		try {
			// tokenize the reply using the space character.
			tokens = string.split(reply);
			// Set whether file or directory
			file = true;
			// First Token is the Device (Volume)
			int tokenIndx = 1;

			if ((reply.toUpperCase()).trim().startsWith("MIGRATED")) {
				file = false;
				fileName = tokens.get(tokenIndx++).toString();
				return true;
			}
			// Next Token is the Device Type (Unit)
			String Unit = tokens.get(tokenIndx++).toString();
			if (Unit.equals("3380")) {

			} else if (Unit.equals("3390")) {

			} else if (!Unit.equals("Tape")) {
				file = false;
				size = "TAPE";
				fileName = tokens.get(tokenIndx++).toString();
				return true;
				// Unknown Unit
				// Therefore we will return 0 as a size, since we can't
				// Calculate it
			}

			if ((tokens.size() == 3) && (Unit.equals("Tape"))) {
				fileName = tokens.get(tokenIndx++).toString();
				return true;
			} else if (tokens.size() == 3) {
				file = false;
				size = "DIR";
				fileName = tokens.get(tokenIndx++).toString();
				return true;
				// 3 Tokens Not on Tape?
			} else if (Unit.equals("Tape")) {
				// Unit is Tape but != 3 Tokens
			}

			// next to token represents the date
			if (tokens.size() == 2) {
				tokenIndx++;
				file = false;
				fileName = tokens.get(tokenIndx++).toString();
				return true;
			}

			date = tokens.get(tokenIndx++).toString();
			// Time isn't returned by MVS, so we'll fake it
			time = "00:00";

			if (tokens.size() == 7) {
				// Ext
				tokenIndx++;
				// Used
				tokens.get(tokenIndx++).toString();
			} else if (tokens.size() == 6) {
				String usedString = tokens.get(tokenIndx++).toString();
				usedString.substring(usedString.length() - 5);
			} else {
				// Got here but not 6 or 7 tokens?!? We missed something!!
				return true;
			}
			size = "0000";
			// skip Recfm, Lrecl, BlkSz, Dsorg
			tokenIndx += 4;
			// DsName
			fileName = tokens.get(tokenIndx++).toString();
		} catch (Throwable e) {
			System.out.println(e.getMessage());
			this.fileName = null;
			this.size = null;
			this.date = null;
			this.time = null;
		}
		return true;

	}

	
	
	/**
	 * This method will set attributes for a remote file. Given a line of reply received as the result of "LIST" command, this method will set all the attributes(name,size,time,date and file type) of the named file. This method requires the reply to be in UNIX (FTP server) format. For example, " drwxr-xr-x	  2	 guest	other  1536  Jan 31 15:15  run.bat"
	 * 
	 * -rw-r--r-- 1 ftp ftp     2148338485 Apr 23 14:46 IBOG_2012.zip
	 * 
	 * @param reply
	 *          reply of FTP server for "dir" command.
	 */

	public boolean setRemoteDescriptionUNIX(String reply) {
		if (reply == null)
			return false;

		Matcher matcher = regex.matcher(reply);
		if ( matcher.matches() ){
			
			// 	Look at the directory flags
			if ( matcher.group(1).charAt(0) == 'd')
				file = false;
			else if ((matcher.group(1).charAt(0) == '-') || (matcher.group(1).charAt(0) == 'l'))
				file = true;
			else
				return false;
			
			mode 	= convertPermissionsString( matcher.group(2) );

			// Determine the size
			if (isDirectory())
				size = "DIR";
			else
				size	=	matcher.group(18);	
			
			// Determine the filename
			fileName	= matcher.group(21);
			if ( matcher.group(22) != null )
				fileName	+= matcher.group(22);
			
			date	 = matcher.group(19) + " " + matcher.group(20);

		}else{
			fileName 	= "";
			size 			= "";
			date 			= "";
			time 			= "";
		}
				
		return true;
	}

	
	
	/**
	 * This method will set attributes for a remote file. Given a line of reply received as the result 
	 * of "LIST" command, this method will set all the attributes(name, size,time and date) of the named 
	 * file. This method requires the reply to be in VM (FTP server) format. For example, 
	 * " GATEWAY  RULES  V  76  86  1  1/17/98  22:30:59  ADISK"
	 * 
	 * @param reply
	 *          reply of FTP server for "dir" command.
	 */
	public boolean setRemoteDescriptionVM(String reply) {
		// reply represents each line read while listing.
		if (reply == null) {
			return false;
		}	else {
			try {
				// tokenize the reply using the space character.
				List<String> tokens = string.split(reply);

				// Set whether file or directory
				file = true;

				// First token is the file name and next is the extension append them
				fileName = tokens.get(0) + "." + tokens.get(1);

				// next token is the size which we need to set
				size = tokens.get(3);

				// next to token represent the month and date which together
				// will be represented as date
				date = tokens.get(6);
				
				// next token is either date or time
				time = tokens.get(7);

			} catch (Throwable e) {
				fileName = null;
				size = null;
				date = null;
				time = null;
			}
		}
		return true;
	}

	
	/*
	 * author of the following method is Jan C. Zawadzki (jcz@geniesystems.com). IIS servers, 
	 * unfortunately, unlike Serv-U, return DOS-like dirlists so they must be handled differently 
	 * from Unix servers
	 * 
	 * WIN2K: SYST = 215 Windows_NT version 5.0
	 * 
	 * 09-18-01 03:34PM 45386 activation.jar 09-18-01 04:22PM 
	 * <DIR> foo 09-18-01 03:34PM 49061 FTPProtocol.jar 09-18-01 03:34PM 233406259 someBigFile.dat
	 */
	public boolean setRemoteDescriptionWinNT(String reply) {
		/* Is it actually in UNIX format? */
		if (reply.startsWith("-") || reply.startsWith("l") || reply.startsWith("d")) {
			return setRemoteDescriptionUNIX(reply);
		}

		if (reply.length() < 40) {
			// well, I'd like to ignore it...
			fileName = "BAD FILE: " + reply;
			return true;
		}

		// we don't use a tokenizer because these are fixed length...
		date = reply.substring(0, 9);
		time = reply.substring(11, 18);
		String dir = reply.substring(24, 30);
		size = reply.substring(19, 38).trim();
		fileName = reply.substring(39).trim();

		if (dir.indexOf("<DIR>") >= 0) {
			file = false;
		} else {
			file = true;
		}

		return true;
	}


	
	/*
	 * added by Werner Zsolt, 10/12/2000. The bean wasn't able to display OS/400 directories. 
	 * An OS/400 directory entry looks like one of the following: QSYS *MEM QAFCXMPOV1.MPCS_XMP_2 
	 * QSYS 233472 07/11/00 09:56:45 *FILE QAFCXM That is, we have to check the 2nd token. 
	 * When it's a number (Integer.parseInt()), it'll be a file.
	 */
	public boolean setRemoteDescriptionOS400(String reply) {
		List<String> tokens = string.split(reply);
		int tokenIndx = 1; // ignore the first token (QSYS)

		String tempToken = tokens.get(tokenIndx++).toString();
		if (tempToken.equals("*MEM")) {
			fileName = tokens.get(tokenIndx++).toString();
			return true;
		} // it was a *MEM entry - that was all
		size = tempToken; // 2nd token is size
		date = tokens.get(tokenIndx++).toString();
		time = tokens.get(tokenIndx++).toString();
		tokenIndx++; // ignore the next token (*FILE)
		fileName = tokens.get(tokenIndx).toString();
		return true;
	}

	
	
	/**
	 * This method will set attributes for a remote file. Given a line of reply received as the 
	 * result of "LIST" command, this method will set all the attributes(name,size,time,date and file type) 
	 * of the named file. This method requires the reply to be in Netware (FTP server) format. 
	 * For example, "d [-W--AFM--] ITS-GN-BLG5-SYS         512 May 16 00:22 /SYS"
	 * "- [RWCEAFMS] E01734                    23 Sep 12 2006 /file/path/globals.060912"
	 * 
	 * @param reply
	 *          reply of FTP server for "dir" command.
	 */
	public boolean setRemoteDescriptionNetware(String reply) {

		// reply represents each line read while listing.
		if (reply == null)
			return false;

		try {
			// tokenize the reply using the space character.
			java.util.StringTokenizer strToken = new java.util.StringTokenizer(reply);

			// read first token which indicates if it is a file or directory.
			String tempString = strToken.nextToken();
			if (tempString.charAt(0) == 'd') {
				file = false;
			} else if (tempString.charAt(0) == '-') {
				file = true;
			} else {
				return false;
			}

			// read second token which is access rights
			tempString = strToken.nextToken();

			// Don't know how to convert a Netware permission string
			// so skip this for now.
			// mode = convertPermissionsString( tempString.substring( 1 ) );

			// skip the third token
			strToken.nextToken(); // the owner???

			// read the fourth token which is the size
			size = strToken.nextToken();

			if (isDirectory()) {
				size = "DIR";
			}

			// read the fifth token which is the month
			String month = strToken.nextToken();

			// read the sixth token which is the day
			String day = strToken.nextToken();

			// next token is either year or time
			List<String> timeTokens = string.split(strToken.nextToken(), ":");
			if (timeTokens.size() == 2) {
				time = timeTokens.get(0).toString() + ":" + timeTokens.get(1).toString();
				Calendar rightNow = Calendar.getInstance();
				int monthIndx = getMonthIndex(month);
				int year = rightNow.get(Calendar.YEAR);
				// set the year depending on the current date - resultant date shouldn't be in the future
				if (monthIndx > rightNow.get(Calendar.MONTH)) {
					year--;
				} else if (monthIndx == rightNow.get(Calendar.MONTH)) {
					int dayInt = Integer.parseInt(day);
					if (dayInt > rightNow.get(Calendar.DAY_OF_MONTH))
						year--;
				}
				date = (year + " " + month + " " + day);
			} else {
				time = "00:00";
				int year = Integer.parseInt(timeTokens.get(0).toString());
				date = year + " " + " " + month + " " + day;
			}

			// next token is the file name
			fileName = strToken.nextToken("").substring(1).trim();
			int lastSlash = fileName.lastIndexOf('/');
			if (lastSlash >= 0)
				fileName = fileName.substring(lastSlash + 1);
		} catch (Exception e) {
			com.nary.Debug.printStackTrace(e); // REMOVE
			fileName = "";
			size = "";
			date = "";
			time = "";
		}

		return true;
	}

	
	
	// converts the likes of rwxrwxrwx to 777
	private static String convertPermissionsString(String _permStr) {
		if (_permStr.length() != 9)
			return "ERROR: invalid permission string - " + _permStr;

		String octalStr = "";
		int octal;
		String groupStr;
		for (int i = 0; i < 3; i++) {
			octal = 0;
			if (i == 2)
				groupStr = _permStr.substring(i * 3);
			else
				groupStr = _permStr.substring(i * 3, (i * 3) + 3);
			
			octal += (groupStr.charAt(0) == 'r') ? 4 : 0;
			octal += (groupStr.charAt(1) == 'w') ? 2 : 0;
			octal += ((groupStr.charAt(2) == 'x') || (groupStr.charAt(2) == 's')) ? 1 : 0;
			octalStr += octal;
		}

		return octalStr;
	}

}