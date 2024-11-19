/*
    Copyright (c) 2021 TOSHIBA Digital Solutions Corporation.
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.toshiba.mwcloud.gs.tools.expimp;

import java.util.EnumSet;
import java.util.Set;

import com.toshiba.mwcloud.gs.experimental.ContainerAttribute;

public class GSConstants {

	//******************************************************************
	//  Common
	//******************************************************************
	/**
	 * Command name constant
	 */
	public static enum CMD_NAME {
		/**
		 * Export command name
		 */
		GS_EXPORT,
		/**
		 * Import command name
		 */
		GS_IMPORT;
		/**
		 * Returns the command name in lowercase
		 */
		public String toString() {
			return name().toLowerCase();
		}
	}

	/** Message resource name */
	public static final String MESSAGE_RESOURCE			= "com.toshiba.mwcloud.gs.tools.expimp.messageResource";


	/** Meta information file format version */
	public static String EXPORT_MNG_FILE_VERSION		= "5.7.0";

	// V2.7.0 is not compatible with previous files
	public static String EXPORT_MNG_FILE_VERSION_1		= "2.7.0";

	// V3.5.0 has a different binary file format from V2.7.0 format
	public static String EXPORT_MNG_FILE_VERSION_2		= "3.5.0";

	// V5.2.0はV5.1.0以前とDATE_FORMATの値が異なる
	public static String EXPORT_MNG_FILE_VERSION_3		= "5.2.0";

	public enum DataSourceType{
		Http
	}

	//******************************************************************
	//  File related
	//******************************************************************
	/** Export management file name */
	public static final String FILE_GS_EXPORT_JSON		= "gs_export.json";
	/** File name of database, ACL, etc. */
	public static final String FILE_GS_EXPORT_ACL_JSON	= "gs_export_acl.json";
	/** View definition file name */
	public static final String FILE_GS_EXPORT_VIEW_JSON	= "gs_export_view.json";

	/** File extension (CSV file) */
	public static final String FILE_EXT_CSV				= ".csv";
	/** File extension (Row in binary mode) */
	public static final String FILE_EXT_BINARY_ROW		= ".scfile";
	/** File extension (single container format in binary mode) */
	public static final String FILE_EXT_BINARY_SINGLE	= ".sc";
	/** File extension (multi-container format in binary mode) */
	public static final String FILE_EXT_BINARY_MULTI	= ".mc";
	/** File extension (JSON file) */
	public static final String FILE_EXT_JSON			= ".json";

	/** Character between file name and ROW number (.scFile) */
	public static final String FILENAME_SEPARATOR 		= "_";

	/** Character between file name and split number (binary mode split) */
	public static final String FILENAME_SEPARATOR_DIV 	= "_div";


	/** Character code of CSV file and external file */
	public static final String ENCODING_CSV				= "UTF-8";



	/** Read buffer size */
	public static final int READ_BUFFER_SIZE			= 8192;

	/** JDBC connection destination URL prefix */
	public static final String JDBC_URL_PREFIX			= "jdbc:gs://";

	//******************************************************************
	//  Export related
	//******************************************************************
	/** Maximum number of progress display */
	public static final int PROGRESS_COUNT				= 20;

	/** Range of Parallel */
	public static final int PARALLEL_MIN				= 2;
	public static final int PARALLEL_MAX				= 16;

	/** Initial size of binary file */
	public static final int BINARY_FILE_INIT_SIZE		= 100;  // 100MB

	//******************************************************************
	//  Connection properties
	//******************************************************************
	public static final String PROP_NOTIFICATION_ADDRESS	= "notificationAddress";
	public static final String PROP_NOTIFICATION_PORT		= "notificationPort";
	public static final String PROP_NOTIFICATION_MEMBER		= "notificationMember";
	public static final String PROP_PROVIDER_URL			= "notificationProvider";
	public static final String PROP_NOTIFICATION_MODE		= "mode";

	public static final String PROP_JDBC_ADDRESS				= "jdbcAddress";
	public static final String PROP_JDBC_PORT					= "jdbcPort";
	public static final String PROP_JDBC_NOTIFICATION_MEMBER	= "jdbcNotificationMember";
	public static final String PROP_JDBC_LOGIN_TIMEOUT			= "jdbcLoginTimeout";

	public static final String PROP_CLUSTER_NAME			= "clusterName";
	public static final String PROP_DATABASE				= "database";
	public static final String PROP_USER					= "user";
	public static final String PROP_PASSWORD				= "password";
	public static final String PROP_CONSISTENCY				= "consistency";
	public static final String PROP_TRANSACTION_TIMEOUT		= "transactionTimeout";
	public static final String PROP_FAILOVER_TIMEOUT		= "failoverTimeout";
	public static final String PROP_CONTAINER_CHACHE_SIZE	= "containerCacheSize";
	public static final String PROP_FETCH_BYTES_SIZE		= "internal.fetchBytesSize";

	public static final String PROP_PARALLEL_INPUT			= "load.input.threadNum";
	public static final String PROP_PARALLEL_OUTPUT			= "load.output.threadNum";

	public static final String PROP_MAX_JOB_SIZE			= "maxJobSize";
	public static final String PROP_MAX_JOB_BUFFER_SIZE		= "maxJobBufferSize";

	public static final String PROP_STORE_BLOCK_SIZE		= "storeBlockSize";

	public static final String PROP_APPLICATION_NAME		= "applicationName";
	public static final String PROP_AGING_SWAP_RATE			= "storeMemoryAgingSwapRate";

	public static final String PROP_EXPORT_AGING_SWAP_RATE	= "export.storeMemoryAgingSwapRate";
	public static final String PROP_IMPORT_AGING_SWAP_RATE	= "import.storeMemoryAgingSwapRate";

	public static final String PROP_AUTHENTICATION_METHOD	= "authenticationMethod";
	public static final String PROP_SSL_MODE					= "sslMode";
	public static final String PROP_VALUE_SSL_MODE_DISABLED		= "DISABLED";
	public static final String PROP_VALUE_SSL_MODE_PREFERRED	= "PREFERRED";
	public static final String PROP_VALUE_SSL_MODE_REQUIRED		= "REQUIRED";
	public static final String PROP_VALUE_SSL_MODE_VERIFY		= "VERIFY";
	public static final String PROP_NOTIFICATION_INTERFACE_ADDRESS	= "notificationInterfaceAddress";

	public static final String PROP_AUTHENTICATION			= "authentication";

	public static final String PROP_TRUST_STORE_TYPE = "trustStore.type";

	public static final String PROP_TRUST_STORE_FILE = "trustStore.path";

	public static final String PROP_TRUST_STORE_PASSWORD = "trustStore.password";

	public static final String PROP_INTERVAL_TIMEZONE = "intervalTimeZone";
	
	//******************************************************************
	// Item name of Export management file (gs_export.json)
	//******************************************************************
	public static final String JSON_EXP_VERSION				= "version";
	public static final String JSON_EXP_STARTTIME			= "startTime";
	public static final String JSON_EXP_ENDTTIME			= "endTime";
	public static final String JSON_EXP_ADDRESS				= "address";
	public static final String JSON_EXP_USER				= "user";
	public static final String JSON_EXP_CONTAINER_COUNT		= "containerCount";
	public static final String JSON_EXP_PARALLEL_COUNT		= "parallel";
	public static final String JSON_EXP_ROW_FILETYPE		= "rowFileType";
	public static final String JSON_EXP_CONTAINER			= "container";
	public static final String JSON_EXP_DATABASE			= "database";
	public static final String JSON_EXP_CONTAINER_NAME		= "name";
	public static final String JSON_EXP_CONTAINER_FILE		= "metafile";

	public static final String JSON_FILTER_CONDITION		= "filterCondition";


	public static final String DATE_FORMAT					= "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	public static final String DATE_FORMAT_BEFORE			= "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public static final String DATE_FORMAT_NOT_TIMEZONE		= "yyyy-MM-dd'T'HH:mm:ss.SSS";
	public static final String DATE_FORMAT_DAY			= "yyyy-MM-dd'T'00:00:00.000Z";
	public static final String DATE_FORMAT_HOUR			= "yyyy-MM-dd'T'HH:00:00.000Z";
	public static final String DATE_INTERVAL_FORMAT	= "yyyyMMdd";



	public enum TARGET_TYPE { ALL,			/* --all */
							DB, 			/* --db */
							CONTAINER_NAME,	/* --container(Name only) or --containerregex(Regular expression)*/
							DATASOURCE,		/* External data source */
							NONE};

	/**
	 * Attribute of NewSQL data
	 */
	public static final Set<ContainerAttribute> CONTAINER_ATTRIBUTE_NEWSQL = EnumSet.of(
			ContainerAttribute.LARGE,
			ContainerAttribute.SINGLE,
			ContainerAttribute.SUB,
			ContainerAttribute.SINGLE_SYSTEM);

	//******************************************************************
	//  Road related
	//******************************************************************

	/*
	 * Command line arguments
	 */

	/** Min value of the parrallel input*/
	public static final int PARALLEL_INPUT_MIN				= 1;
	/** Max value of the parrallel input*/
	public static final int PARALLEL_INPUT_MAX				= 64;
	/** Min value of the parrallel output*/
	public static final int PARALLEL_OUTPUT_MIN				= 1;
	/** Max value of the parrallel output*/
	public static final int PARALLEL_OUTPUT_MAX				= 16;

	/*
	 * Regular expression
	 */

	/** Regular expression to determine a valid container name */
	public static final String REGEXP_VALID_CONTAINER_NAME = "^[A-Z_]+[A-Z0-9_]*@?[A-Z0-9_]*$";
	/** A regular expression that determines a valid column name */
	public static final String REGEXP_VALID_COLUMN_NAME = "^[A-Z_]+[A-Z0-9_]*$";
	/** A regular expression that determines the suffix of a valid column name */
	public static final String REGEXP_VALID_COLUMN_NAME_SUFFIX = "^[A-Z0-9_]*$";

}

