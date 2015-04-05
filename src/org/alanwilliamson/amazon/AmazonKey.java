/*
 *  Copyright (C) 2000 - 2014 TagServlet Ltd
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
 *  $Id: AmazonKey.java 2487 2015-01-22 22:40:49Z alan $
 */

package org.alanwilliamson.amazon;

import java.io.Serializable;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.StorageClass;

public class AmazonKey extends Object implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String amzKey;
	private final String amzSecret;
	private final String amzRegion;
	private String datasource;

	public AmazonKey(String _amzKey, String _amzSecret) {
		this(_amzKey, _amzSecret, "us-standard");
	}

	public AmazonKey(String _amzKey, String _amzSecret, String _amzRegion) {
		amzKey = _amzKey;
		amzSecret = _amzSecret;
		amzRegion = _amzRegion;
	}

	public void setDataSource(String ds) {
		datasource = ds.toLowerCase().trim();
	}

	public String getDataSource() {
		return datasource;
	}

	public String getKey() {
		return amzKey;
	}

	public String getSecret() {
		return amzSecret;
	}

	public String getRegion() {
		return amzRegion;
	}

	public com.amazonaws.services.s3.model.Region getAmazonRegion() {
		if (amzRegion.equalsIgnoreCase("us-east") || amzRegion.equalsIgnoreCase("us-standard"))
			return com.amazonaws.services.s3.model.Region.US_Standard;
		else if (amzRegion.equalsIgnoreCase("us-west-1"))
			return com.amazonaws.services.s3.model.Region.US_West;
		else if (amzRegion.equalsIgnoreCase("us-west-2"))
			return com.amazonaws.services.s3.model.Region.US_West_2;
		else if (amzRegion.equalsIgnoreCase("cn-beijing"))
			return com.amazonaws.services.s3.model.Region.CN_Beijing;
		else if (amzRegion.equalsIgnoreCase("eu") || amzRegion.equalsIgnoreCase("eu_ireland"))
			return com.amazonaws.services.s3.model.Region.EU_Ireland;
		else if (amzRegion.equalsIgnoreCase("eu_frankfurt"))
			return com.amazonaws.services.s3.model.Region.EU_Frankfurt;
		else if (amzRegion.equalsIgnoreCase("ap-southeast-1") || amzRegion.equalsIgnoreCase("ap-singapore"))
			return com.amazonaws.services.s3.model.Region.AP_Singapore;
		else if (amzRegion.equalsIgnoreCase("ap-sydney"))
			return com.amazonaws.services.s3.model.Region.AP_Sydney;
		else if (amzRegion.equalsIgnoreCase("ap-northeast-1") || amzRegion.equalsIgnoreCase("ap-tokyo"))
			return com.amazonaws.services.s3.model.Region.AP_Tokyo;
		else if (amzRegion.equalsIgnoreCase("sa-east-1") || amzRegion.equalsIgnoreCase("sa-saopaulo"))
			return com.amazonaws.services.s3.model.Region.SA_SaoPaulo;
		else
			return com.amazonaws.services.s3.model.Region.US_Standard;
	}

	public String getS3Host() {
		if (amzRegion.equals("us-east") || amzRegion.equals("us-standard"))
			return "https://s3.amazonaws.com/";
		else if (amzRegion.equals("us-west-1"))
			return "https://s3-us-west-1.amazonaws.com/";
		else if (amzRegion.equals("us-west-2"))
			return "https://s3-us-west-2.amazonaws.com/";
		else if (amzRegion.equals("eu"))
			return "https://s3-eu-west-1.amazonaws.com/";
		else if (amzRegion.equals("ap-southeast-1"))
			return "https://s3-ap-southeast-1.amazonaws.com/";
		else if (amzRegion.equals("ap-southeast-2"))
			return "https://s3-ap-southeast-2.amazonaws.com/";
		else if (amzRegion.equals("ap-northeast-1"))
			return "https://s3-ap-northeast-1.amazonaws.com/";
		else if (amzRegion.equals("sa-east-1"))
			return "https://s3-sa-east-1.amazonaws.com/";
		else
			return amzRegion;
	}

	public String getSimpleDBHost() {
		if (amzRegion.equals("us-east") || amzRegion.equals("us-standard"))
			return "https://sdb.amazonaws.com";
		else if (amzRegion.equals("us-west-1"))
			return "https://sdb.us-west-1.amazonaws.com";
		else if (amzRegion.equals("us-west-2"))
			return "https://sdb.us-west-2.amazonaws.com";
		else if (amzRegion.equals("eu"))
			return "https://sdb.eu-west-1.amazonaws.com";
		else if (amzRegion.equals("ap-southeast-1"))
			return "https://sdb.ap-southeast-1.amazonaws.com";
		else if (amzRegion.equals("ap-southeast-2"))
			return "https://sdb.ap-southeast-2.amazonaws.com";
		else if (amzRegion.equals("ap-northeast-1"))
			return "https://sdb.ap-northeast-1.amazonaws.com";
		else if (amzRegion.equals("sa-east-1"))
			return "https://sdb.sa-east-1.amazonaws.com";
		else
			return amzRegion;
	}

	public String getSQSHost() {
		if (amzRegion.equalsIgnoreCase("us-east") || amzRegion.equalsIgnoreCase("us-standard"))
			return "https://sqs.us-east-1.amazonaws.com";
		else if (amzRegion.equalsIgnoreCase("us-west-1"))
			return "https://sqs.us-west-1.amazonaws.com";
		else if (amzRegion.equalsIgnoreCase("us-west-2"))
			return "https://sqs.us-west-2.amazonaws.com";
		else if (amzRegion.equalsIgnoreCase("eu"))
			return "https://sqs.eu-west-1.amazonaws.com";
		else if (amzRegion.equalsIgnoreCase("ap-southeast-1"))
			return "https://sqs.ap-southeast-1.amazonaws.com";
		else if (amzRegion.equalsIgnoreCase("ap-southeast-2"))
			return "https://sqs.ap-southeast-2.amazonaws.com";
		else if (amzRegion.equalsIgnoreCase("ap-northeast-1"))
			return "https://sqs.ap-northeast-1.amazonaws.com";
		else if (amzRegion.equalsIgnoreCase("sa-east-1"))
			return "https://sqs.sa-east-1.amazonaws.com";
		else
			return amzRegion;
	}

	public StorageClass getAmazonStorageClass(String storage) {
		if (storage == null)
			return StorageClass.Standard;
		else if (storage.equalsIgnoreCase("standard"))
			return StorageClass.Standard;
		else if (storage.toLowerCase().startsWith("reduced"))
			return StorageClass.ReducedRedundancy;
		else
			return StorageClass.Standard;
	}

	/**
	 * private | public-read | public-read-write | authenticated-read | bucket-owner-read | bucket-owner-full-control | log-delivery-write
	 *
	 * @param acl
	 * @return
	 */
	public CannedAccessControlList getAmazonCannedAcl(String acl) {
		if (acl.equalsIgnoreCase("private"))
			return CannedAccessControlList.Private;
		else if (acl.equalsIgnoreCase("public-read") || acl.equalsIgnoreCase("publicread"))
			return CannedAccessControlList.PublicRead;
		else if (acl.equalsIgnoreCase("public-read-write") || acl.equalsIgnoreCase("publicreadwrite"))
			return CannedAccessControlList.PublicReadWrite;
		else if (acl.equalsIgnoreCase("authenticated-read") || acl.equalsIgnoreCase("authenticatedread"))
			return CannedAccessControlList.AuthenticatedRead;
		else if (acl.equalsIgnoreCase("bucket-owner-read") || acl.equalsIgnoreCase("bucketownerread"))
			return CannedAccessControlList.BucketOwnerRead;
		else if (acl.equalsIgnoreCase("bucket-owner-full-control") || acl.equalsIgnoreCase("bucketownerfullcontrol"))
			return CannedAccessControlList.BucketOwnerFullControl;
		else if (acl.equalsIgnoreCase("log-delivery-write") || acl.equalsIgnoreCase("logdeliverywrite"))
			return CannedAccessControlList.LogDeliveryWrite;
		else
			return CannedAccessControlList.Private;
	}

}