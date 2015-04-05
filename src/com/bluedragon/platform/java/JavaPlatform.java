/*
 *  Copyright (C) 2000 - 2015 TagServlet Ltd
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
 *  $Id: JavaPlatform.java 2507 2015-02-09 01:20:34Z alan $
 */

package com.bluedragon.platform.java;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.alanwilliamson.amazon.s3.BackgroundUploader;
import org.alanwilliamson.lang.java.JavaClassFactory;
import org.alanwilliamson.lang.java.cfSCRIPTJava;
import org.aw20.util.SystemClock;
import org.aw20.util.SystemClockEvent;

import com.bluedragon.net.socket.SocketServerDataFactory;
import com.bluedragon.platform.FileIO;
import com.bluedragon.platform.Platform;
import com.bluedragon.platform.SmtpInterface;
import com.bluedragon.platform.java.smtp.OutgoingMailServer;
import com.bluedragon.plugin.PluginManager;
import com.bluedragon.search.collection.CollectionFactory;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfWebServices;
import com.naryx.tagfusion.cfm.sql.cfQueryImplInterface;
import com.naryx.tagfusion.cfm.tag.cfSCRIPT;
import com.naryx.tagfusion.cfm.xml.ws.dynws.DynamicWebServiceTypeGenerator;
import com.naryx.tagfusion.util.TagElement;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class JavaPlatform implements Platform {

	private FileIO									javaFileIO;
	private OutgoingMailServer			spoolingMailServer;

	@Override
	public void init(ServletConfig config) throws ServletException {
		com.nary.Debug.SystemOff();

		javaFileIO					= new JavaFileIO(config);

		spoolingMailServer	= new OutgoingMailServer( cfEngine.thisInstance.getSystemParameters() );
		CollectionFactory.init( cfEngine.getConfig() );

		BackgroundUploader.onStart();
		
		// Install Sun JCE provider
		try {
			Class<?> jceClass = Class.forName("com.sun.crypto.provider.SunJCE");
			Object jceInstance = jceClass.newInstance();
			java.security.Security.addProvider((java.security.Provider) jceInstance);
		} catch (Exception e) {
			cfEngine.log("Failed to add Sun JCE provider");
		}
	}


	/**
	 * The Servlet engine is going done
	 */
	@Override
	public void destroy(){
		SystemClock.shutdown();
		spoolingMailServer.engineShutdown();
		CollectionFactory.close();
		SocketServerDataFactory.thisInst.close();
		BackgroundUploader.onShutdown();
	}

	@Override
	public SmtpInterface getSmtp(){
		return spoolingMailServer;
	}

	@Override
	public void engineAdminUpdate(){
		javaFileIO.engineAdminUpdate();
	}

	@Override
	public void log(String l){
		com.nary.Debug.println( l );
	}

	@Override
	public boolean	hasNetworkAccess(){return true;}



	@Override
	public void registerScriptExtensions(){
  	cfSCRIPT.registerLanguage("java", 				"org.alanwilliamson.lang.java.cfSCRIPTJava" );
  	cfSCRIPT.registerLanguage("javascript", 	"org.alanwilliamson.lang.javascript.cfSCRIPTJavascript" );

  	try {
			cfSCRIPTJava.javaClassFactory	= new JavaClassFactory();
		} catch (Exception e) {
			cfEngine.log( "Failed to initialize CFSCRIPT lang=java: " + e.getMessage() );
		}
	}

	@Override
	public Object loadClass(String classpath){
		try{
			return Class.forName(classpath).newInstance();
		}catch(Exception e){
			return null;
		}
	}

	@Override
	public void initialiseQuerySystem(xmlCFML configFile, Map<String, cfQueryImplInterface> queryImplementations){
		cfQueryImplInterface dq	= new com.naryx.tagfusion.cfm.sql.platform.java.cfQueryImplSQL();
		dq.init(configFile);

		queryImplementations.put("dynamic", dq );
		queryImplementations.put("amazon", 	new org.alanwilliamson.amazon.simpledb.cfQueryImplSimpleDB() );
	}

	@Override
	public cfQueryImplInterface getDefaultQuerySystem(Map<String, cfQueryImplInterface> queryImplementations){
		return queryImplementations.get("dynamic");
	}


	@Override
	public void initialiseTagSystem(xmlCFML configFile){
		try {
			com.naryx.tagfusion.cfm.mail.cfIMAP.init(configFile);
		} catch (java.lang.NoClassDefFoundError e) {

			// javamail is not installed
			cfEngine.thisInstance.TagChecker.replaceTag("CFMAIL", "com.naryx.tagfusion.cfm.tag.cfDisabledMailTag" );
			cfEngine.thisInstance.TagChecker.replaceTag("CFIMAP", "com.naryx.tagfusion.cfm.tag.cfDisabledMailTag" );
			cfEngine.thisInstance.TagChecker.replaceTag("CFPOP", "com.naryx.tagfusion.cfm.tag.cfDisabledMailTag" );

			cfEngine.log("CFMAIL, CFIMAP, and CFPOP disabled: JavaMail not found on classpath");
		}

		if (cfEngine.WINDOWS)
			com.naryx.tagfusion.cfm.registry.cfREGISTRY.init(configFile);

		com.naryx.tagfusion.cfm.tag.awt.cfCHART.init(configFile);
		com.naryx.tagfusion.cfm.schedule.cfSCHEDULE.init(configFile);
		com.naryx.tagfusion.cfm.tag.net.cfMULTICAST.init(configFile);


    try {
  	  com.naryx.tagfusion.cfm.document.cfDOCUMENT.init( configFile );
    } catch ( NoClassDefFoundError e ) {
  	  cfEngine.log( "CFDOCUMENT tag disabled: " + e.toString() );
  	  cfEngine.thisInstance.TagChecker.replaceTag( "CFDOCUMENT", "com.naryx.tagfusion.cfm.document.cfDisabledDocumentTag" );
    }

    // Load in the plugins
    PluginManager.getPlugInManager().loadPlugIn("org.alanwilliamson.openbd.plugin.spreadsheet.SpreadSheetExtension", configFile);
    PluginManager.getPlugInManager().loadPlugIn("org.alanwilliamson.openbd.plugin.cfsmtp.SmtpExtension", configFile);
    PluginManager.getPlugInManager().loadPlugIn("org.aw20.plugin.login.LoginExtension", configFile);
    PluginManager.getPlugInManager().loadPlugIn("org.alanwilliamson.openbd.plugin.crontab.CronExtension", configFile);
    PluginManager.getPlugInManager().loadPlugIn("com.bluedragon.mongo.MongoExtension", configFile);
    PluginManager.getPlugInManager().loadPlugIn("org.alanwilliamson.openbd.plugin.salesforce.SalesForceExtension", configFile);
    PluginManager.getPlugInManager().loadPlugIn("com.bluedragon.vision.VisionExtension", configFile);
    PluginManager.getPlugInManager().loadPlugIn("com.bluedragon.profiler.ProfilerExtension", configFile);
	}

	@Override
	public FileIO getFileIO() {
		return javaFileIO;
	}


	@Override
	public boolean compileOutput(String outDir, ByteArrayOutputStream javacOut) throws IOException{
		String ps = System.getProperty("path.separator");
		String fs = System.getProperty("file.separator");

		// Create a java compiler object
		// NOTE: Using the newer com.sun.tools.javac.Main class causes an
		// IncompatibleClassChangeError to
		// be thrown by the compiler when BD is compiled with JDK 1.3 and run with
		// JDK 1.4. So for
		// now we'll use the older sun.tools.javac.Main class.
		@SuppressWarnings("deprecation")
		sun.tools.javac.Main javac = new sun.tools.javac.Main(javacOut, "BD Web Services Client Compiler");

		// Determine the classpath needed by the java compiler
		String javacClasspath = "";
		String dir = null;
		String libDir = null;

		String cp = System.getProperty("java.class.path");
		String bcp = System.getProperty("sun.boot.class.path");

		// This is BD J2EE (or BD J2EE WLS) so the JARs are in the WEB-INF lib folder.
		dir = cfWebServices.getDocRootDir();
		if (dir != null && !dir.endsWith(fs))
			dir += fs;
		dir = dir + "WEB-INF" + fs;
		libDir = dir + "lib" + fs;

		String altLibDir	= cfEngine.getAltLibPath();

		if (!bcp.contains("webservices.jar") && !cp.contains("webservices.jar")) {
			javacClasspath += ps + DynamicWebServiceTypeGenerator.getJarPath(libDir, altLibDir, "webservices.jar");
		}

		if (!bcp.contains("wsdl4j.jar") && !cp.contains("wsdl4j.jar")) {
			javacClasspath += ps + DynamicWebServiceTypeGenerator.getJarPath(libDir, altLibDir, "wsdl4j.jar");
		}

		if (!bcp.contains("saaj.jar") && !cp.contains("saaj.jar")) {
			javacClasspath += ps + DynamicWebServiceTypeGenerator.getJarPath(libDir, altLibDir, "saaj.jar");
		}

		if (!bcp.contains("jaxrpc.jar") && !cp.contains("jaxrpc.jar")) {
			javacClasspath += ps + DynamicWebServiceTypeGenerator.getJarPath(libDir, altLibDir, "jaxrpc.jar");
		}

		javacClasspath += ps + dir + "classes";

		// Add the J2EE specific jars (not required as classes, may be in the classes dir)
		javacClasspath += ps + DynamicWebServiceTypeGenerator.getJarPath(libDir, altLibDir, "OpenBlueDragon.jar");

		// Now add the boot classpath to the java compiler classpath
		if (bcp != null) {
			javacClasspath += ps + bcp;
		}

		// Now add the classpath to the java compiler classpath
		if (cp != null) {
			javacClasspath += ps + cp;
		}

		List<String> list = new ArrayList<String>();
		if (javacClasspath != null) {
			list.add("-classpath");
			list.add(javacClasspath);
		}
		compileFileList(new java.io.File(outDir), list);
		String[] args = list.toArray(new String[list.size()]);

		@SuppressWarnings("deprecation")
		boolean rtn = javac.compile(args);
		if (!rtn)
			System.err.println("Could not compile client web service stub classes with classpath: " + javacClasspath);
		return rtn;

	}


	private void compileFileList(File dir, List<String> l) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory())
					compileFileList(files[i], l);
				else
					l.add(files[i].getAbsolutePath());
			}
		}
	}


	@Override
	public void registerFunctions( Map<String, String> functions ){

		// Image functions
		functions.put("imagenew",													"com.naryx.tagfusion.expression.function.image.ImageNew");
		functions.put("imageread",												"com.naryx.tagfusion.expression.function.image.ImageRead");
		functions.put("imagewrite",												"com.naryx.tagfusion.expression.function.image.ImageWrite");
		functions.put("imagewritebase64",									"com.naryx.tagfusion.expression.function.image.ImageWriteBase64");
		functions.put("imagereadbase64",									"com.naryx.tagfusion.expression.function.image.ImageReadBase64");
		functions.put("isimage",													"com.naryx.tagfusion.expression.function.image.IsImage");
		functions.put("imagegetexifmetadata",							"com.naryx.tagfusion.expression.function.image.ImageGetExifMetaData");
		functions.put("imagegetexiftag",									"com.naryx.tagfusion.expression.function.image.ImageGetExifTag");

		functions.put("imageinfo",												"com.naryx.tagfusion.expression.function.image.ImageInfo");
		functions.put("imagegetwidth",										"com.naryx.tagfusion.expression.function.image.ImageGetWidth");
		functions.put("imagegetheight",										"com.naryx.tagfusion.expression.function.image.ImageGetHeight");
		functions.put("imagegetbufferedimage",						"com.naryx.tagfusion.expression.function.image.ImageGetBufferedImage");
		functions.put("imagegetblob",											"com.naryx.tagfusion.expression.function.image.ImageGetBlob");

		functions.put("imagecrop",												"com.naryx.tagfusion.expression.function.image.ImageCrop");
		functions.put("imagecopy",												"com.naryx.tagfusion.expression.function.image.ImageCopy");
		functions.put("imageblur",												"com.naryx.tagfusion.expression.function.image.ImageBlur");
		functions.put("imagesharpen",											"com.naryx.tagfusion.expression.function.image.ImageSharpen");
		functions.put("imagenegative",										"com.naryx.tagfusion.expression.function.image.ImageNegative");
		functions.put("imagegrayscale",										"com.naryx.tagfusion.expression.function.image.ImageGrayScale");
		functions.put("imageflip",												"com.naryx.tagfusion.expression.function.image.ImageFlip");
		functions.put("imagepaste",												"com.naryx.tagfusion.expression.function.image.ImagePaste");
		functions.put("imageresize",											"com.naryx.tagfusion.expression.function.image.ImageResize");
		functions.put("imagerotate",											"com.naryx.tagfusion.expression.function.image.ImageRotate");

		functions.put("imageaddborder",										"com.naryx.tagfusion.expression.function.image.ImageAddBorder");
		functions.put("imageclearrect",										"com.naryx.tagfusion.expression.function.image.ImageClearRect");
		functions.put("imagedrawrect",										"com.naryx.tagfusion.expression.function.image.ImageDrawRect");
		functions.put("imagedrawroundrect",								"com.naryx.tagfusion.expression.function.image.ImageDrawRoundRect");
		functions.put("imagedrawarc",											"com.naryx.tagfusion.expression.function.image.ImageDrawArc");
		functions.put("imagedrawbeveledrect",							"com.naryx.tagfusion.expression.function.image.ImageDrawBeveledRect");
		functions.put("imagedrawline",										"com.naryx.tagfusion.expression.function.image.ImageDrawLine");
		functions.put("imagedrawlines",										"com.naryx.tagfusion.expression.function.image.ImageDrawLines");
		functions.put("imagedrawpoint",										"com.naryx.tagfusion.expression.function.image.ImageDrawPoint");
		functions.put("imagedrawoval",										"com.naryx.tagfusion.expression.function.image.ImageDrawOval");
		functions.put("imagedrawtext",										"com.naryx.tagfusion.expression.function.image.ImageDrawText");

		functions.put("imagesetdrawingcolor",							"com.naryx.tagfusion.expression.function.image.ImageSetActiveColor");
		functions.put("imagesetbackgroundcolor",					"com.naryx.tagfusion.expression.function.image.ImageSetBackgroundColor");
		functions.put("imagesetantialiasing",							"com.naryx.tagfusion.expression.function.image.ImageSetAntialiasing");
		functions.put("imagexordrawingmode",							"com.naryx.tagfusion.expression.function.image.ImageXORDrawingMode");
		functions.put("imagereflection",									"com.naryx.tagfusion.expression.function.image.ImageReflection");
		functions.put("imagecontrastbrightness",					"com.naryx.tagfusion.expression.function.image.ImageContrastBrightness");

		functions.put("todatauri",												"com.naryx.tagfusion.expression.function.image.ToDataURI");

		/* BlueDragon only: Amazon SimpleDB functions */
		functions.put("amazonregisterdatasource", 				"org.alanwilliamson.amazon.AmazonRegisterDatasource");
		functions.put("amazonremovedatasource", 					"org.alanwilliamson.amazon.AmazonRemoveDatasource");

		functions.put("amazonsimpledbcreatedomain", 			"org.alanwilliamson.amazon.simpledb.CreateSDBDomain");
		functions.put("amazonsimpledbdeletedomain", 			"org.alanwilliamson.amazon.simpledb.DeleteSDBDomain");
		functions.put("amazonsimpledblistdomains", 				"org.alanwilliamson.amazon.simpledb.ListSDBDomains");
		functions.put("amazonsimpledbgetattributes", 			"org.alanwilliamson.amazon.simpledb.GetSDBAttributes");
		functions.put("amazonsimpledbsetattribute", 			"org.alanwilliamson.amazon.simpledb.SetSDBAttribute");
		functions.put("amazonsimpledbdeleteattribute", 		"org.alanwilliamson.amazon.simpledb.DeleteSDBAttribute");
		functions.put("amazonsimpledbsetstruct", 					"org.alanwilliamson.amazon.simpledb.SetSDBStruct");

		functions.put("amazonsqslistqueues",							"org.alanwilliamson.amazon.sqs.ListQueues" );
		functions.put("amazonsqscreatequeue",							"org.alanwilliamson.amazon.sqs.CreateQueue" );
		functions.put("amazonsqsdeletequeue",							"org.alanwilliamson.amazon.sqs.DeleteQueue" );
		functions.put("amazonsqsgetattributes",						"org.alanwilliamson.amazon.sqs.GetAttributes" );

		functions.put("amazonsqsdeletemessage", 					"org.alanwilliamson.amazon.sqs.DeleteMessage" );
		functions.put("amazonsqschangemessagevisibility", "org.alanwilliamson.amazon.sqs.ChangeMessageVisibility" );
		functions.put("amazonsqsreceivemessage", 					"org.alanwilliamson.amazon.sqs.ReceiveMessage" );
		functions.put("amazonsqssendmessage", 						"org.alanwilliamson.amazon.sqs.SendMessage" );

		functions.put("amazonsqsremovepermission", 				"org.alanwilliamson.amazon.sqs.RemovePermission" );
		functions.put("amazonsqsaddpermission", 					"org.alanwilliamson.amazon.sqs.AddPermission");

		functions.put("amazons3listbuckets", 							"org.alanwilliamson.amazon.s3.ListBuckets");
		functions.put("amazons3createbucket", 						"org.alanwilliamson.amazon.s3.CreateBucket");
		functions.put("amazons3bucketexists", 						"org.alanwilliamson.amazon.s3.ExistsBucket");
		functions.put("amazons3deletebucket", 						"org.alanwilliamson.amazon.s3.DeleteBucket");
		functions.put("amazons3bucketsetacl", 						"org.alanwilliamson.amazon.s3.SetBucketAcl");
		functions.put("amazons3bucketrequesterpays", 			"org.alanwilliamson.amazon.s3.RequesterPays");

		functions.put("amazons3changestorageclass", 			"org.alanwilliamson.amazon.s3.ChangeObjectStorageClass");
		functions.put("amazons3list", 										"org.alanwilliamson.amazon.s3.List");
		functions.put("amazons3copy", 										"org.alanwilliamson.amazon.s3.Copy");
		functions.put("amazons3geturl", 									"org.alanwilliamson.amazon.s3.GetUrl");
		functions.put("amazons3write", 										"org.alanwilliamson.amazon.s3.Write");
		functions.put("amazons3read", 										"org.alanwilliamson.amazon.s3.Read");
		functions.put("amazons3rename", 									"org.alanwilliamson.amazon.s3.Rename");
		functions.put("amazons3delete", 									"org.alanwilliamson.amazon.s3.Delete");
		functions.put("amazons3setacl", 									"org.alanwilliamson.amazon.s3.SetAcl");
		functions.put("amazons3getinfo", 									"org.alanwilliamson.amazon.s3.GetInfo");

		functions.put("amazonetpipelinedelete", 					"org.alanwilliamson.amazon.transcoder.pipeline.Delete");
		functions.put("amazonetpipelinecreate", 					"org.alanwilliamson.amazon.transcoder.pipeline.Create");
		functions.put("amazonetpipelinelist", 						"org.alanwilliamson.amazon.transcoder.pipeline.List");
		functions.put("amazonetpipelineread", 						"org.alanwilliamson.amazon.transcoder.pipeline.Read");
		functions.put("amazonetpipelineupdatestatus", 		"org.alanwilliamson.amazon.transcoder.pipeline.UpdateStatus");

		functions.put("amazonetpresetslist", 							"org.alanwilliamson.amazon.transcoder.presets.List");

		functions.put("amazonetjobcreate", 								"org.alanwilliamson.amazon.transcoder.job.Create");
		functions.put("amazonetjobcancel", 								"org.alanwilliamson.amazon.transcoder.job.Cancel");
		functions.put("amazonetjobread", 									"org.alanwilliamson.amazon.transcoder.job.Read");
		functions.put("amazonetjoblist", 									"org.alanwilliamson.amazon.transcoder.job.List");
	
		
		// IP related functions
		functions.put("ipcount",													"com.naryx.tagfusion.expression.function.ext.ip.ipGetCount" );
		functions.put("ipbroadcastmask",									"com.naryx.tagfusion.expression.function.ext.ip.ipBroadcastMask" );
		functions.put("iptocidr",													"com.naryx.tagfusion.expression.function.ext.ip.ipToCidr" );
		functions.put("ipgethighaddress",									"com.naryx.tagfusion.expression.function.ext.ip.ipGetHighAddress" );
		functions.put("ipgetlowaddress",									"com.naryx.tagfusion.expression.function.ext.ip.ipGetLowAddress" );
		functions.put("ipnetworkaddress",									"com.naryx.tagfusion.expression.function.ext.ip.ipNetworkAddress" );
		functions.put("ipinrange",												"com.naryx.tagfusion.expression.function.ext.ip.ipInRange" );
		functions.put("ipasinteger",											"com.naryx.tagfusion.expression.function.ext.ip.ipAsInteger" );
		functions.put("ipresolvedomain",									"com.naryx.tagfusion.expression.function.ext.ip.ipResolveDomain" );
		functions.put("ipgetmxrecords",										"com.naryx.tagfusion.expression.function.ext.ip.ipGetMXRecords" );
		functions.put("ipreverselookup",									"com.naryx.tagfusion.expression.function.ext.ip.ipReverseLookup" );

		// Register the expressions for Nirvanix
		functions.put( "jmxgetdomainlist",  			"com.naryx.tagfusion.cfm.engine.jmx.JmxGetDomainList" );
		functions.put( "jmxgetmbeans",   					"com.naryx.tagfusion.cfm.engine.jmx.JmxGetMBeans" );
		functions.put( "jmxgetmbeanattributes",  	"com.naryx.tagfusion.cfm.engine.jmx.JmxGetMBeanAttributes" );

		functions.put( "xmlsearch", 									"com.naryx.tagfusion.expression.function.xml.XmlSearch" );
		functions.put( "getmailspooldirectory", 			"com.naryx.tagfusion.expression.function.getMailSpoolDirectory" );
		functions.put( "getmailundelivereddirectory", "com.naryx.tagfusion.expression.function.getMailUndeliveredDirectory" );

		functions.put( "dbinfo", 											"com.naryx.tagfusion.cfm.sql.platform.java.dbInfo" );

		// Search functions
		functions.put( "collectionsearch",						"com.bluedragon.search.search.SearchFunction" );
		functions.put( "collectioncreate",						"com.bluedragon.search.collection.CollectionCreateFunction" );
		functions.put( "collectiondelete",						"com.bluedragon.search.collection.CollectionDeleteFunction" );
		functions.put( "collectionlist",							"com.bluedragon.search.collection.CollectionListFunction" );
		functions.put( "collectionstatus",						"com.bluedragon.search.collection.CollectionStatus" );
		functions.put( "collectionlistcategory",			"com.bluedragon.search.collection.CollectionListCategoryFunction" );

		functions.put( "collectionindexcustom",				"com.bluedragon.search.index.custom.CustomFunction" );
		functions.put( "collectionindexfile",					"com.bluedragon.search.index.file.FileFunction" );
		functions.put( "collectionindexpath",					"com.bluedragon.search.index.path.PathFunction" );
		functions.put( "collectionindexweb",					"com.bluedragon.search.index.web.WebFunction" );

		functions.put( "collectionindexdelete",				"com.bluedragon.search.index.custom.CustomDeleteFunction" );
		functions.put( "collectionindexpurge",				"com.bluedragon.search.index.PurgeFunction" );

		// Socket functions
		functions.put( "socketconnect",								"com.bluedragon.net.socket.SocketConnectionFunction" );
		functions.put( "socketserverstart",						"com.bluedragon.net.socket.SocketServerStartFunction" );
		functions.put( "socketserverstop",						"com.bluedragon.net.socket.SocketServerStopFunction" );
		functions.put( "socketservergetclients",			"com.bluedragon.net.socket.SocketServerAllClientsFunction" );
		
		// Journal functions
		functions.put( "journalread",									"com.bluedragon.journal.function.JournalRead" );
		functions.put( "journalreadtodatasource",			"com.bluedragon.journal.function.JournalReadToDataSource" );
		functions.put( "journalreadsession",					"com.bluedragon.journal.function.JournalReadSession" );
	}

	@Override
	public void registerTags(Hashtable<String, TagElement> tagElements){
		tagElements.put("CFIMAGE", 			new TagElement("CFIMAGE", true, "com.naryx.tagfusion.cfm.tag.awt.cfIMAGE"));
		tagElements.put("CFCAPTCHA", 		new TagElement("CFCAPTCHA", true, "com.naryx.tagfusion.cfm.tag.awt.cfCAPTCHA"));

		tagElements.put("CFSLIDER", 		new TagElement("CFSLIDER", true, "com.naryx.tagfusion.cfm.cfform.cfSLIDER"));
		tagElements.put("CFTREEITEM",	 	new TagElement("CFTREEITEM", true, "com.naryx.tagfusion.cfm.cfform.cfTREEITEM"));
		tagElements.put("CFTREE", 			new TagElement("CFTREE", true, "com.naryx.tagfusion.cfm.cfform.cfTREE"));
		tagElements.put("CFTEXTAREA", 	new TagElement("CFTEXTAREA", true, "com.naryx.tagfusion.cfm.cfform.cfTEXTAREA"));

		tagElements.put("CFCHART", 				new TagElement("CFCHART", true, "com.naryx.tagfusion.cfm.tag.awt.cfCHART"));
		tagElements.put("CFCHARTDATA", 		new TagElement("CFCHARTDATA", true, "com.naryx.tagfusion.cfm.tag.awt.cfCHARTDATA"));
		tagElements.put("CFCHARTSERIES", 	new TagElement("CFCHARTSERIES", true, "com.naryx.tagfusion.cfm.tag.awt.cfCHARTSERIES"));

		// -- BlueDragon Only CFCHART specific tags
		tagElements.put("CFCHARTRANGEMARKER", 	new TagElement("CFCHARTRANGEMARKER", true, "com.naryx.tagfusion.cfm.tag.awt.cfCHARTRANGEMARKER"));
		tagElements.put("CFCHARTDOMAINMARKER", 	new TagElement("CFCHARTDOMAINMARKER", true, "com.naryx.tagfusion.cfm.tag.awt.cfCHARTDOMAINMARKER"));
		tagElements.put("CFCHARTLEGEND", 				new TagElement("CFCHARTLEGEND", true, "com.naryx.tagfusion.cfm.tag.awt.cfCHARTLEGEND"));
		tagElements.put("CFCHARTTITLE", 				new TagElement("CFCHARTTITLE", true, "com.naryx.tagfusion.cfm.tag.awt.cfCHARTTITLE"));
		tagElements.put("CFCHARTIMAGE", 				new TagElement("CFCHARTIMAGE", true, "com.naryx.tagfusion.cfm.tag.awt.cfCHARTIMAGE"));

		tagElements.put("CFPOP", 				new TagElement("CFPOP", true, "com.naryx.tagfusion.cfm.mail.cfPOP3"));
		tagElements.put("CFIMAP", 			new TagElement("CFIMAP", true, "com.naryx.tagfusion.cfm.mail.cfIMAP"));
		tagElements.put("CFMAIL", 			new TagElement("CFMAIL", true, "com.naryx.tagfusion.cfm.mail.cfMAIL"));
		tagElements.put("CFMAILPARAM", 	new TagElement("CFMAILPARAM", true, "com.naryx.tagfusion.cfm.mail.cfMAILPARAM"));
		tagElements.put("CFMAILPART", 	new TagElement("CFMAILPART", true, "com.naryx.tagfusion.cfm.mail.cfMAILPART"));

		tagElements.put("CFLDAP", 			new TagElement("CFLDAP", true, "com.naryx.tagfusion.cfm.tag.net.cfLDAP"));
		tagElements.put("CFFTP", 				new TagElement("CFFTP", true, "com.naryx.tagfusion.cfm.tag.net.ftp.cfFTPTag"));

		tagElements.put("CFSCHEDULE", 	new TagElement("CFSCHEDULE", true, "com.naryx.tagfusion.cfm.schedule.cfSCHEDULE"));
		tagElements.put("CFXMLRPC", 		new TagElement("CFXMLRPC", true, "com.naryx.tagfusion.cfm.tag.net.cfXMLRPC"));
		tagElements.put("CFMULTICAST", 	new TagElement("CFMULTICAST", true, "com.naryx.tagfusion.cfm.tag.net.cfMULTICAST"));

		tagElements.put("CFREGISTRY", 	new TagElement("CFREGISTRY", cfEngine.WINDOWS, "com.naryx.tagfusion.cfm.registry.cfREGISTRY"));

		tagElements.put("CFEXECUTE", 		new TagElement("CFEXECUTE", true, "com.naryx.tagfusion.cfm.tag.io.cfEXECUTE"));

		tagElements.put("CFDOCUMENT", 				new TagElement("CFDOCUMENT", true, "com.naryx.tagfusion.cfm.document.cfDOCUMENT"));
		tagElements.put("CFDOCUMENTITEM", 		new TagElement("CFDOCUMENTITEM", true, "com.naryx.tagfusion.cfm.document.CFDOCUMENTITEM"));
		tagElements.put("CFDOCUMENTSECTION", 	new TagElement("CFDOCUMENTSECTION", true, "com.naryx.tagfusion.cfm.document.CFDOCUMENTSECTION"));

		tagElements.put("CFDBINFO", 		new TagElement("CFDBINFO", true, "com.naryx.tagfusion.cfm.sql.platform.java.cfDBINFO"));

		// the search elements
		tagElements.put("CFSEARCH", 		new TagElement("CFSEARCH",		true,	"com.bluedragon.search.search.SearchTag"));
		tagElements.put("CFCOLLECTION", new TagElement("CFCOLLECTION",true,	"com.bluedragon.search.collection.CollectionTag"));
		tagElements.put("CFINDEX", 			new TagElement("CFINDEX",			true,	"com.bluedragon.search.index.IndexTag"));
	}


	@Override
	public void timerRunOnce(SystemClockEvent handler, int minuteleap) {
		SystemClock.setListenerMinute(handler, minuteleap, true);
	}


	@Override
	public void timerSetListenerMinute(SystemClockEvent handler, int minuteleap) {
		SystemClock.setListenerMinute(handler, minuteleap);
	}


	@Override
	public void timerSetListenerMinute(SystemClockEvent handler) {
		SystemClock.setListenerMinute(handler);
	}


	@Override
	public void timerSetListenerHourly(SystemClockEvent handler) {
		SystemClock.setListenerHour(handler);
	}


	@Override
	public void timerCancel(SystemClockEvent handler) {
		SystemClock.removeListenerMinute(handler);
		SystemClock.removeListenerMonth(handler);
		SystemClock.removeListenerDay(handler);
		SystemClock.removeListenerHour(handler);
	}

}