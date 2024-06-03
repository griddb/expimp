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
import java.util.ListResourceBundle;

/**
 * 国際化メッセージリソース（en）
 *
 */
public class messageResource extends ListResourceBundle {

	public static final String VERSION_NUM = "5.6.00";
	public static final String VERSION = "V"+VERSION_NUM;
	public static final String GS_EXPORT_CE = "gs_export-ce";
	public static final String GS_IMPORT_CE = "gs_import-ce";
	/**
	 * 国際化リソース
	 */
	static final Object[][] contents = {
			{ "MESS_TOOL_VERSION", VERSION_NUM },//
			{ "MESS_GS_EXPORT_CE", GS_EXPORT_CE },//
			{ "MESS_GS_IMPORT_CE", GS_IMPORT_CE },//
			{ "MESS_IMPORT_PROC_IMPORTMAIN_1", "Import Start." },//
			{ "MESS_IMPORT_PROC_IMPORTMAIN_2", "Import Start.(Append Mode)" },//
			{ "MESS_IMPORT_PROC_IMPORTMAIN_3", "Import Start.(Replace Mode)" },//
			{ "MESS_IMPORT_PROC_IMPORTMAIN_4", "Import Completed." },//
			{ "MESS_IMPORT_PROC_IMPORTMAIN_5", "Container List in local export file" },//

			{ "MESS_TEST_MODE",  "[TEST Mode]"},

			{ "MESS_TABLE_PARTITION", "[Table partition]"},
			{ "MESS_TABLE_PARTITION_SUCCESS", "Successfully completed."},
			{ "MESS_TABLE_PARTITION_ERROR", "Some data has not been processed."},

			{ "MESS_IMPORT_ERR_IMPORTMAIN_1", "D00101: An unexpected error occurred while importing data." },//
			{ "MESS_IMPORT_ERR_IMPORTMAIN_2", "D00102: The client api (gridstore.jar) version may be older."},
			{ "MESS_IMPORT_ERR_IMPORTMAIN_3", "D00103: A Required class does not found."},
			{ "MESS_IMPORT_ERR_IMPORTMAIN_4", "D00104: An unexpected error occurred while importing data."},

			{ "MESS_IMPORT_PROC_IMPORTPROC_1", "The following Container is processed." },//
			{ "MESS_IMPORT_PROC_IMPORTPROC_2", "Container[" },//
			{ "MESS_IMPORT_PROC_IMPORTPROC_3", "] has been imported successfully." },//
			{ "MESS_IMPORT_PROC_IMPORTPROC_4", "] has been imported unsuccessfully." },//
			{ "MESS_IMPORT_PROC_IMPORTPROC_5", "The row data did not exist specified intervals : " },

			{ "MESS_IMPORT_ERR_IMPORTPROC_1", "D00201: An unexpected error occurred while getting container names." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_2", "D00202: No file exists in that directory path." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_3",
					"D00203: An unexpected error occurred while getting container names from specified local files." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_4",
					"D00204: An unexpected error occurred while getting container names from all local files." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_5", "D00205: No container name is set." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_6", "D00206: Container names could not be retrieved from the local file." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_7",
					"D00207: There is no container that match the regular expression or container name." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_8", "D00208: An unexpected error occurred while importing data." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_9", "D00209: An unexpected error occurred while importing data." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_10", "D00210: The same container that the schema is different has already registered. Check the logs for the location of the schema mismatch." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_11", "D00211: This container type is not supported." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_12", "D00212: The buffer to store row data does not get." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_13", "D00213: This row data type is not supported." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_14", "D00214: This index type cannot be used in time-series data." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_15", "D00215: A file i/o error has occurred." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_16", "D00216: An unexpected error occurred while parsing the json file." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_17",
					"D00217: An unexpected error occurred while getting container names from the json file." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_18", "D00218: An unexpected error occurred while checking UTF-8 BOM." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_19",
					"D00219: An unexpected error occurred while getting container names from json object." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_20", "D00220: This file is not exists or file type is not JSON." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_21",
					"D00221: No Specified Container Name or information exists in the files." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_22", "D00222: An unexpected error occurred while getting container information list." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_23", "D00223: Indexes cannot be set to rowkey column of timeseries." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_24", "D00224: Because this container is already exists, [--append] or [--replace] option is required." },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_25", "D00225: The specified directory by -d option does not exist." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_26", "D00226: The specified path by -d option is not a directory." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_27", "D00227: The specified file is not the meta information file(*.json)." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_28", "D00228: The specified file by -f option does not exist." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_29", "D00229: The specified name by -f option is not a file." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_30", "D00230: The file name without the directory can be specified in the option." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_31", "D00231: The specified file by -f option is invalid." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_32", "D00232: There is no target container in the file." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_33", "D00233: An unexpected error occurred while importing row data." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_35", "D00235: An ACL file does not exist." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_36", "D00236: The same user that the user information is different has already existed." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_37", "D00237: The same database that the database setting is different has already existed." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_38", "D00238: An unexpected error occurred while setting database, user, or acl." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_39", "D00239: Because the target database does not exist, create the target database by administrator." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_3A", "D0023A: An unexpected error occurred while creating database or checking if database exists." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_3B", "D0023B: An unexpected error occurred while reading gs_export_acl.json." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_3C", "D0023C: An unexpected error occurred while creating user." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_3D", "D0023D: An unexpected error occurred while creating database." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_3E", "D0023E: An unexpected error occurred while setting permissions." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_3F", "D0023F: Some data has not been processed in partition table." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_40", "D00240: The Binary data file that has stored export data must be version 2.7 or later." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_41", "D00241: Because some newsql data have been stored in the target database, data in a table format cannot be imported." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_42", "D00242: The NewSQL data have already been stored in the target database. The force option is set, and import of NewSQL table is skipped." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_43", "D00243: An unexpected error occurred while checking NewSQL data." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_44", "D00244: The --container option cannot set NewSQL table." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_45", "D00245: The same name of the container exists. In the case of a container split、data cannot added to existing container or replace them." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_46", "D00246: Failed to read Meta Information file." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_47", "D00247: The partitioned table with same name has already existed." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_48", "D00248: Failed to drop partitioned table." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_49", "D00249: Failed to drop partitioned table." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_4A", "D0024A: Failed to get partitioned table." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_4B", "D0024B: Failed to compare the table partitioning information." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_4C", "D0024C: Failed to compare the expiration information." },
			{ "MESS_IMPORT_ERR_IMPORTPROC_4D", "D0024D: An unexpected error occurred while reading the view definition file."},
			{ "MESS_IMPORT_ERR_IMPORTPROC_4E", "D0024E: The container with same name has already existed. Importing of this view is skipped."},
			{ "MESS_IMPORT_ERR_IMPORTPROC_4F", "D0024F: The view with same name has already existed. Importing of this view is skipped."},
			{ "MESS_IMPORT_ERR_IMPORTPROC_50", "D00250: The view with same name has already existed."},
			{ "MESS_IMPORT_ERR_IMPORTPROC_51", "D00251: An unexpected error occurred while creating or replacing view."},
			{ "MESS_IMPORT_ERR_IMPORTPROC_52", "D00252: An unexpected error occurred while getting view."},
			{ "MESS_IMPORT_ERR_IMPORTPROC_53", "D00253: An unexpected error occurred while dropping view."},
			{ "MESS_IMPORT_ERR_IMPORTPROC_54", "D00254: An unexpected error occurred while creating view."},

			{ "MESS_IMPORT_ERR_GRIDSTORE_1", "D00301: An unexpected error occurred while creating container." },

			{ "MESS_EXPORT_PROC_EXPORTMAIN_1", "Export Start." },
			{ "MESS_EXPORT_PROC_EXPORTMAIN_2", "Export Completed." },

			{ "MESS_EXPORT_ERR_EXPORTMAIN_1", "D00401: An unexpected error occurred while exporting data." },
			{ "MESS_EXPORT_ERR_EXPORTMAIN_2", "D00402: The client api (gridstore.jar) version may be older."},
			{ "MESS_EXPORT_ERR_EXPORTMAIN_3", "D00403: A Required class does not found."},
			{ "MESS_EXPORT_ERR_EXPORTMAIN_4", "D00404: An unexpected error occurred while exporting data."},

			{ "MESS_EXPORT_PROC_EXPORTPROC_1", "Connected to the GridDB cluster" },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_2", "The container name list has been acquired." },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_3", "The processing target container name list has been acquired." },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_4", "The container information is output to meta information file(*.json)." },
			{ "MESS_EXPORT_PROC_EXPORTPROC_5", "The container information has been output to meta information file(*.json)." },
			{ "MESS_EXPORT_PROC_EXPORTPROC_6", "The row data is acquired from the container information." },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_7", "The row data has been acquired." },
			{ "MESS_EXPORT_PROC_EXPORTPROC_8", "The container name list has been acquired." },
			{ "MESS_EXPORT_PROC_EXPORTPROC_9", "The container name list has been acquired." },
			{ "MESS_EXPORT_PROC_EXPORTPROC_10", "The container name list has been acquired." },
			{ "MESS_EXPORT_PROC_EXPORTPROC_11", "The file name list that stored the container information has been Created" },
			{ "MESS_EXPORT_PROC_EXPORTPROC_12", "Container[" },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_13", "] has been exported successfully." },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_14", "] has been exported unsuccessfully." },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_15",
					"The row data is output to CSV data file. [TEST Mode]" },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_16",
					"The row data is output to binary data file. [TEST Mode]" },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_17", "The container information is output to meta information file(*.json). [TEST Mode]" },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_18", "The export management information is output to export management file. [TEST Mode]" },
			{ "MESS_EXPORT_PROC_EXPORTPROC_19", "Directory       : " },
			{ "MESS_EXPORT_PROC_EXPORTPROC_20", "Number of target containers : " },
			{ "MESS_EXPORT_PROC_EXPORTPROC_21", "Number of users : " },
			{ "MESS_EXPORT_PROC_EXPORTPROC_22", "Number of databases : " },
			{ "MESS_EXPORT_PROC_EXPORTPROC_23", "Number of target containers : %d  Number of skip containers : %d" },
			{ "MESS_EXPORT_PROC_EXPORTPROC_24", "Number of target views : %d" },
			{ "MESS_EXPORT_PROC_EXPORTPROC_25", "The row data did not exist specified intervals : " },

			{ "MESS_EXPORT_ERR_EXPORTPROC_1", "D00501: Failed to connect to the GridDB cluster." },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_2", "D00502: The container name list could not be acquired." },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_3", "D00503: No processing target container name exists." },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_4", "D00504: An unexpected error occurred while exporting data." },
			{ "MESS_EXPORT_ERR_EXPORTPROC_5", "D00505: An unexpected error occurred while operating row file." },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_6", "D00506: An unexpected error occurred while creating meta information file(*.json)." },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_7", "D00507: An unexpected error occurred while connecting to GridDB cluster." },
			{ "MESS_EXPORT_ERR_EXPORTPROC_8",
					"D00508: An unexpected error occurred while acquiring container information from all partition information." },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_9", "D00509: An unexpected error occurred while extracting container name." },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_10", "D00510: An unexpected error occurred while acquiring container information." },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_11", "D00511: No specified container exists." },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_12", "D00512: No specified container exists." },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_13", "D00513: The container information could not be acquired." },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_14",
					"D00514: An unexpected error occurred while creating json file from container information." },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_15", "D00515: An unexpected error occurred while checking duplicate file." },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_16", "D00516: An unexpected error occurred while creating output directory."},
			{ "MESS_EXPORT_ERR_EXPORTPROC_17", "D00517: An unexpected error occurred while acquiring the export target container name."},
			{ "MESS_EXPORT_ERR_EXPORTPROC_18", "D00518: The export target container does not exist."},
			{ "MESS_EXPORT_ERR_EXPORTPROC_19", "D00519: The export results in specified directory have already exist."},
			{ "MESS_EXPORT_ERR_EXPORTPROC_20", "D00520: An unexpected error occurred while checking directories and files."},
			{ "MESS_EXPORT_ERR_EXPORTPROC_21", "D00521: A timeout occurred while acquiring container list."},
			{ "MESS_EXPORT_ERR_EXPORTPROC_22", "D00522: The Query that set by the option is wrong."},
			{ "MESS_EXPORT_ERR_EXPORTPROC_23", "D00523: An unexpected error occurred while retrieving data."},
			{ "MESS_EXPORT_ERR_EXPORTPROC_24", "D00524: An unexpected error occurred while acquiring database, user, or acl."},
			{ "MESS_EXPORT_ERR_EXPORTPROC_25", "D00525: The general user cannot set the --acl option."},
			{ "MESS_EXPORT_ERR_EXPORTPROC_26_1", "D00526: An unexpected error occurred while acquiring container list of the partition (ID=\""},
			{ "MESS_EXPORT_ERR_EXPORTPROC_26_2", "\"). (All containers of the partition is out of the processing target.)"},
			{ "MESS_EXPORT_ERR_EXPORTPROC_27", "D00527: The table can only be exported with [--all] or [--db] option."},
			{ "MESS_EXPORT_ERR_EXPORTPROC_28", "D00528: The container with partitioning will be skipped."},
			{ "MESS_EXPORT_ERR_EXPORTPROC_29", "D00529: Failed to connect to the GridDB cluster using JDBC."},
			{ "MESS_EXPORT_ERR_EXPORTPROC_2A", "D0052A: An unexpected error occurred while exporting views."},
			{ "MESS_EXPORT_ERR_EXPORTPROC_2B", "D0052B: An unexpected error occurred while creating view definition file." },
			{ "MESS_EXPORT_ERR_EXPORTPROC_2C", "D0052C: An unexpected error occurred while getting views."},

			{ "MESS_COMM_ERR_PROPINFO_1", "D00601: An unexpected error occurred while reading the properties file." },
			{ "MESS_COMM_ERR_PROPINFO_2", "D00602: The properties file does not found. The properties file is created." },
			{ "MESS_COMM_ERR_PROPINFO_3", "D00603: An unexpected error occurred while writing the properties file." },

			{ "MESS_COMM_PROC_ROWCSV_1", "The output of row data file in csv format start." },
			{ "MESS_COMM_PROC_ROWCSV_2", "The following Container is processed." },
			{ "MESS_COMM_PROC_ROWCSV_3", "The following ROW is processed." },
			{ "MESS_COMM_PROC_ROWCSV_4", "The row data is output to the CSV format file." },

			{ "MESS_COMM_ERR_ROWCSV_1", "D00701: An unexpected error occurred while being output of row data to multi CSV format file." },
			{ "MESS_COMM_ERR_ROWCSV_2", "D00702: An unexpected error occurred while being output of row data to CSV format file." },
			{ "MESS_COMM_ERR_ROWCSV_3", "D00703: An unexpected error occurred while generating serialized row data." },
			{ "MESS_COMM_ERR_ROWCSV_4", "D00704: This data type is not supported." },
			{ "MESS_COMM_ERR_ROWCSV_5", "D00705: An unexpected error occurred while acquiring the column data." },
			{ "MESS_COMM_ERR_ROWCSV_6", "D00706: An unexpected error occurred while being output to external data file." },
			{ "MESS_COMM_ERR_ROWCSV_7", "D00707: An unexpected error occurred while being output to external BLOB file." },
			{ "MESS_COMM_ERR_ROWCSV_8", "D00708: An unexpected error occurred while being output to CSV format header." },
			{ "MESS_COMM_ERR_ROWCSV_9", "D00709: An unexpected error occurred while being output to CSV format meta information file(*.json).." },
			{ "MESS_COMM_ERR_ROWCSV_10", "D00710: An unexpected error occurred while being output of CSV format row data." },
			{ "MESS_COMM_ERR_ROWCSV_11",
					"D00711: An unexpected error occurred while being output of CSV format row data. The following Container is processed." },
			{ "MESS_COMM_ERR_ROWCSV_12", "D00712: The row data could not be acquired." },
			{ "MESS_COMM_ERR_ROWCSV_13",
					"D00713:  An unexpected error occurred while being output of CSV format row data. The following Container is processed." },

			{ "MESS_COMM_ERR_ROWCSV_15", "D00715: An unexpected error occurred while reading the row data from CSV format file." },
			{ "MESS_COMM_ERR_ROWCSV_16", "D00716: The row data that does not match the number of the schema column was detected" },
			{ "MESS_COMM_ERR_ROWCSV_17", "D00717: Data Tag of Specified Container information does not exist" },

			{ "MESS_COMM_ERR_ROWCSV_18", "D00718: An unexpected error occurred while setting row data." },
			{ "MESS_COMM_ERR_ROWCSV_19", "D00719: The Data type that cannot be processed in the present version has been detected." },
			{ "MESS_COMM_ERR_ROWCSV_20", "D00720: An unexpected error occurred while checking if the external data file exists." },
			{ "MESS_COMM_ERR_ROWCSV_21", "D00721: The GridDB Type from external data file could not be acquired." },
			{ "MESS_COMM_ERR_ROWCSV_22", "D00722: The external data file name could not be acquired." },
			{ "MESS_COMM_ERR_ROWCSV_23", "D00723: An unexpected error occurred while reading the external data file." },
			{ "MESS_COMM_ERR_ROWCSV_24", "D00724: An unexpected error occurred while writing the file." },
			{ "MESS_COMM_ERR_ROWCSV_25", "D00725: An unexpected error occurred while reading the BLOB data." },
			{ "MESS_COMM_ERR_ROWCSV_26", "D00726: The External file cannot be accesses." },
			{ "MESS_COMM_ERR_ROWCSV_27", "D00727: An unexpected error occurred while creating the CSV file." },
			{ "MESS_COMM_ERR_ROWCSV_28", "D00728: An unexpected error occurred while creating the CSV file." },
			{ "MESS_COMM_ERR_ROWCSV_29", "D00729: An unexpected error occurred while writing the row data." },
			{ "MESS_COMM_ERR_ROWCSV_30", "D00730: The specified container data does not exist" },
			{ "MESS_COMM_ERR_ROWCSV_31", "D00731: An unexpected error occurred while reading the CSV file." },
			{ "MESS_COMM_ERR_ROWCSV_32", "D00732: An unexpected error occurred while reading the CSV file." },
			{ "MESS_COMM_ERR_ROWCSV_33", "D00733: An unexpected error occurred while reading the CSV file." },
			{ "MESS_COMM_ERR_ROWCSV_34", "D00734: The BLOB Type is set in the external file." },
			{ "MESS_COMM_ERR_ROWCSV_35", "D00735: The column type that is specified the external file name is invalid." },
			{ "MESS_COMM_ERR_ROWCSV_36", "D00736: The specified external file name is invalid" },
			{ "MESS_COMM_ERR_ROWCSV_37", "D00737: An unexpected error occurred while converting the column data." },

			{ "MESS_COMM_ERR_ROWBNY_1", "D00801: An unexpected error occurred while being output of the row data to the multi binary format file." },
			{ "MESS_COMM_ERR_ROWBNY_2", "D00802: An unexpected error occurred while being output of the row data to the binary format file." },
			{ "MESS_COMM_ERR_ROWBNY_3", "D00803: An unexpected error occurred while checking the ZIP file." },
			{ "MESS_COMM_ERR_ROWBNY_4", "D00804: An unexpected error occurred while creating the ZIP file." },
			{ "MESS_COMM_ERR_ROWBNY_5", "D00805: An unexpected error occurred while creating the row data file." },
			{ "MESS_COMM_ERR_ROWBNY_6", "D00806: An unexpected error occurred while creating the individual row data file." },
			{ "MESS_COMM_ERR_ROWBNY_7", "D00807: An unexpected error occurred while checking the binary format file." },
			{ "MESS_COMM_ERR_ROWBNY_8",
					"D00808: An unexpected error occurred while creating the binary format row data file. The following Container is processed." },
			{ "MESS_COMM_ERR_ROWBNY_9", "D00809: An unexpected error occurred while reading the binary file." },
			{ "MESS_COMM_ERR_ROWBNY_10", "D00810: An unexpected error occurred while being output to the binary format file." },
			{ "MESS_COMM_ERR_ROWBNY_11", "D00811: The Row Data could not be acquired from GridDB cluster." },
			{ "MESS_COMM_ERR_ROWBNY_12", "D00812: This data type is not supported" },
			{ "MESS_COMM_ERR_ROWBNY_13", "D00813: An unexpected error occurred while converting to the row data." },
			{ "MESS_COMM_ERR_ROWBNY_14",
					"D00814: An unexpected error occurred while converting the row data to the object." },
			{ "MESS_COMM_ERR_ROWBNY_15", "D00815: No file to make zip file exists." },
			{ "MESS_COMM_ERR_ROWBNY_16", "D00816: An unexpected error occurred while making zip file." },
			{ "MESS_COMM_ERR_ROWBNY_17", "D00817: An unexpected error occurred while opening zip file." },
			{ "MESS_COMM_ERR_ROWBNY_18",
					"D00818: The file other than single binary format file or multi binary format file is set." },
			{ "MESS_COMM_ERR_ROWBNY_19", "D00819: The row data of specified container does not found." },
			{ "MESS_COMM_ERR_ROWBNY_20", "D00820: The binary format row data file does not found." },
			{ "MESS_COMM_ERR_ROWBNY_21", "D00821: An unexpected error occurred while deleting the files." },
			{ "MESS_COMM_ERR_ROWBNY_22", "D00822: An unexpected error occurred while being output to the binary file." },
			{ "MESS_COMM_ERR_ROWBNY_23", "D00823: An unexpected error occurred while being output to the binary file." },
			{ "MESS_COMM_ERR_ROWBNY_24", "D00824: An unexpected error occurred while reading the binary file." },
			{ "MESS_COMM_ERR_ROWBNY_25", "D00825: An unexpected error occurred while reading the binary file." },
			{ "MESS_COMM_ERR_ROWBNY_26", "D00826: An unexpected error occurred while reading the binary file." },
			{ "MESS_COMM_ERR_ROWBNY_27", "D00827: An unexpected error occurred while reading the binary file." },

			{ "MESS_COMM_PROC_ROWBNY_1", "The individual single binary format row data file is output." },
			{ "MESS_COMM_PROC_ROWBNY_2", "The combined individual Single Binary format Row Data File is output the file." },
			{ "MESS_COMM_PROC_ROWBNY_3", "The single binary format row data file is output to the buffer." },
			{ "MESS_COMM_PROC_ROWBNY_4", "The buffer size is output to binary format row data file." },
			{ "MESS_COMM_PROC_ROWBNY_5", "The buffer crc is output to binary format row data file." },
			{ "MESS_COMM_PROC_ROWBNY_6", "The file information is output to binary format row data file." },
			{ "MESS_COMM_PROC_ROWBNY_7", "The buffer data is output to binary format row data file." },
			{ "MESS_COMM_PROC_ROWBNY_8", "The binary format file name" },

			{ "MESS_COMM_PROC_METAINFO_1", "The creating meta information file of multi container start." },
			{ "MESS_COMM_PROC_METAINFO_2", "The creating meta information file of multi container completed." },
			{ "MESS_COMM_PROC_METAINFO_3", "The creating meta information file of single container start." },
			{ "MESS_COMM_PROC_METAINFO_4", "The creating meta information file of single container completed." },

			{ "MESS_COMM_ERR_METAINFO_1", "D00901: An unexpected error occurred while creating meta information file." },
			{ "MESS_COMM_ERR_METAINFO_2", "D00902: An unexpected error occurred while creating meta information file." },
			{ "MESS_COMM_ERR_METAINFO_3", "D00903: The same name file have already existed." },
			{ "MESS_COMM_ERR_METAINFO_4", "D00904: An unexpected error occurred while being output to meta information file." },
			{ "MESS_COMM_ERR_METAINFO_5", "D00905: An unexpected error occurred while converting container information to json." },
			{ "MESS_COMM_ERR_METAINFO_6", "D00906: An unexpected error occurred while converting container information to json." },
			{ "MESS_COMM_ERR_METAINFO_7", "D00907: An unexpected error occurred while checking UTF-8 BOM." },//
			{ "MESS_COMM_ERR_METAINFO_9", "D00909: An unexpected error occurred while parsing timeseries properties." },//
			{ "MESS_COMM_ERR_METAINFO_11", "D00911: An unexpected error occurred while parsing index properties." },//
			{ "MESS_COMM_ERR_METAINFO_12", "D00912: An unexpected error occurred while parsing column properties." },//
			{ "MESS_COMM_ERR_METAINFO_13", "D00913: An unexpected error occurred while parsing column type." },//
			{ "MESS_COMM_ERR_METAINFO_14", "D00914: An unexpected error occurred while parsing container properties." },//
			{ "MESS_COMM_ERR_METAINFO_15", "D00915: An unexpected error occurred while reading specified container of meta information file." },//
			{ "MESS_COMM_ERR_METAINFO_16", "D00916: An unexpected error occurred while reading all container of meta information file." },//

			{ "MESS_COMM_ERR_METAINFO_17", "D00917: The column information is not set." },//
			{ "MESS_COMM_ERR_METAINFO_18", "D00918: The column name of the column information is not set." },//
			{ "MESS_COMM_ERR_METAINFO_19", "D00919: The column type of the column information is not set." },//
			{ "MESS_COMM_ERR_METAINFO_20", "D00920: The column name of the index information is not set." },//
			{ "MESS_COMM_ERR_METAINFO_21", "D00921: The index type of the index information is not set." },//
			{ "MESS_COMM_ERR_METAINFO_22", "D00922: The column name of the index information does not exist in the column information." },//
			{ "MESS_COMM_ERR_METAINFO_24", "D00924: The column name of HI information does not exist in the column information." },//
			{ "MESS_COMM_ERR_METAINFO_25", "D00925: An unexpected error occurred while checking container information." },//

			{ "MESS_COMM_ERR_METAINFO_26", "D00926: The container information of json file could not be read." },//
			{ "MESS_COMM_ERR_METAINFO_27", "D00927: The specified container information does not exist in json file." },//
			{ "MESS_COMM_ERR_METAINFO_28", "D00928: The container information of json file could not be read." },//

			{ "MESS_COMM_ERR_METAINFO_29", "D00929: This index type is not supported in timeseries container." },//
			{ "MESS_COMM_ERR_METAINFO_30", "D00930: An unexpected error occurred while converting column type." },//
			{ "MESS_COMM_ERR_METAINFO_31", "D00931: The extension of the files do not match." },
			{ "MESS_COMM_ERR_METAINFO_32", "D00932: The extension of the file is invalid." },
			{ "MESS_COMM_ERR_METAINFO_33", "D00933: An unexpected error occurred while writing export management file." },
			{ "MESS_COMM_ERR_METAINFO_34", "D00934: The container name of meta information file is null." },
			{ "MESS_COMM_ERR_METAINFO_35", "D00935: The same container names exists in meta information file." },
			{ "MESS_COMM_ERR_METAINFO_36", "D00936: The container information does not exist in the file." },
			{ "MESS_COMM_ERR_METAINFO_37", "D00937: An unexpected error occurred while reading meta information file." },
			{ "MESS_COMM_ERR_METAINFO_38", "D00938: The export management file(gs_export.json) does not exist." },
			{ "MESS_COMM_ERR_METAINFO_39", "D00939: To import the data file created by user or the archive data file or the export file version 1.5 or earlier, use meta information file with -f option. " },
			{ "MESS_COMM_ERR_METAINFO_40", "D00940: The container data(name or file) of the export management file is invalid" },
			{ "MESS_COMM_ERR_METAINFO_41", "D00941: The container data does not exist in the export management file. " },
			{ "MESS_COMM_ERR_METAINFO_42", "D00942: An unexpected error occurred while reading the export management file." },
			{ "MESS_COMM_ERR_METAINFO_43", "D00943: The file specified by \"--filterfile\" does not exist." },
			{ "MESS_COMM_ERR_METAINFO_44", "D00944: The file specified by \"--filterfile\" is invalid." },
			{ "MESS_COMM_ERR_METAINFO_45", "D00945: An unexpected error occurred while reading the file specified by \"--filterfile\". " },
			{ "MESS_COMM_ERR_METAINFO_46", "D00946: The partitioned table exported in format before V4 will be skipped." },
			{ "MESS_COMM_ERR_METAINFO_47", "D00947: Interval partition table must be set when (interval_worker_group or interval_worker_group_position) are specified" },

			{ "MESS_COMM_PROC_CMD_1", "A command duplicate check start." },
			{ "MESS_COMM_PROC_CMD_2", "A command line parameter persing start." },
			{ "MESS_COMM_PROC_CMD_3", "A command line parameter persing start." },
			{ "MESS_COMM_PROC_CMD_4", "A command line parameter persing completed." },
			{ "MESS_COMM_PROC_CMD_5", "This string by the --container option is a regular expression." },//
			{ "MESS_COMM_PROC_CMD_6", "This string is a container name." },//

			{ "MESS_COMM_ERR_CMD_1", "D00A01: The command has not been set correctly." },//
			{ "MESS_COMM_ERR_CMD_2", "D00A02: The command parameter cannot be duplicated." },//
			{ "MESS_COMM_ERR_CMD_3", "D00A03: [-h|--help] option and --version option cannot be set at same time." },//
			{ "MESS_COMM_ERR_CMD_4", "D00A04: The valid parameter is not set." },//
			{ "MESS_COMM_ERR_CMD_5", "D00A05: The invalid parameter is set." },//
			{ "MESS_COMM_ERR_CMD_6", "D00A06: An unexpected error occurred while parsing parameter." },//
			{ "MESS_COMM_ERR_CMD_7", "D00A07: An unexpected error occurred while parsing parameter." },//
			{ "MESS_COMM_ERR_CMD_8", "D00A08: The invalid parameter is set." },//
			{ "MESS_COMM_ERR_CMD_9", "D00A09: The initializing from properties files is failed." },//
			{ "MESS_COMM_ERR_CMD_10", "D00A10: Server Information [:] is duplicated." },//
			{ "MESS_COMM_ERR_CMD_11", "D00A11: The IP Address could not be obtained because of the unknown host." },//
			{ "MESS_COMM_ERR_CMD_12", "D00A12: The Port number could not be parsed." },//
			{ "MESS_COMM_ERR_CMD_13", "D00A13: The Port number is duplicated." },//
			{ "MESS_COMM_ERR_CMD_14", "D00A14: The User Account has not been set." },//
			{ "MESS_COMM_ERR_CMD_15", "D00A15: The username and password is invalid." },//
			{ "MESS_COMM_ERR_CMD_16", "D00A16: [--all] option, [--db] option, and [--container] [--containerregex] option cannot be set at same time" },//
			{ "MESS_COMM_ERR_CMD_17", "D00A17: The fetch count or the commit count is invalid." },//
			{ "MESS_COMM_ERR_CMD_18", "D00A18: An unexpected error occurred while checking directory path." },//
			{ "MESS_COMM_ERR_CMD_19", "D00A19: An security error occurred while checking directory path." },//
			{ "MESS_COMM_ERR_CMD_20", "D00A20: The [--out] parameter cannot be set directory path." },//
			{ "MESS_COMM_ERR_CMD_21", "D00A21: The invalid container name is set" },//
			{ "MESS_COMM_ERR_CMD_22", "D00A22: An unexpected error occurred while parsing parameter." },//
			{ "MESS_COMM_ERR_CMD_23", "D00A23: An unexpected error occurred while setting export parameters." },//
			{ "MESS_COMM_ERR_CMD_24", "D00A24: An unexpected error occurred while setting import parameters." },//
			{ "MESS_COMM_ERR_CMD_25", "D00A25: The command has not been set correctly." },//
			{ "MESS_COMM_ERR_CMD_26", "D00A26: The command parameter cannot be duplicated." },//
			{ "MESS_COMM_ERR_CMD_27", "D00A27: The invalid parameter is set." },//
			{ "MESS_COMM_ERR_CMD_28", "D00A28: An unexpected error occurred while checking duplicate parameters." },//
			{ "MESS_COMM_ERR_CMD_29", "D00A29: An unexpected error occurred while matching regular expression for the cluster name." },//
			{ "MESS_COMM_ERR_CMD_30", "D00A30: An unexpected error occurred while checking the container name." },//
			{ "MESS_COMM_ERR_CMD_31",
					"D00A31: [-f|--file] option, [--append] option, and [--replace] option cannot be set at export command." },//
			{ "MESS_COMM_ERR_CMD_32",
					"D00A32: [--out] option and [-t|--test] option cannot be set at export command." },//
			{ "MESS_COMM_ERR_CMD_33",
					"D00A33: If setting [-l|--list] options, cannot set the option other than [--directory] option or [--file] option." },//
			{ "MESS_COMM_ERR_CMD_34", "D00A34: [--append] option and [--replace] option cannot be set at same time." },//
			{ "MESS_COMM_ERR_CMD_35", "D00A35: [-f|--file] option  must be set a file name." },//
			{ "MESS_COMM_ERR_CMD_36", "D00A36: The following String cannot be used as a regular expression." },//
			{ "MESS_COMM_ERR_CMD_37", "D00A37: The required argument is not set." },//
			{ "MESS_COMM_ERR_CMD_38", "D00A38: The username and password must be set." },//
			{ "MESS_COMM_ERR_CMD_39", "D00A39: The [--binary] option is invalid." },//
			{ "MESS_COMM_ERR_CMD_40", "D00A40: The [--binary] option does not require argument." },//
			{ "MESS_COMM_ERR_CMD_41", "D00A41: At least one from [--all] option, [--db] option and [--container] option must be set." },//
			{ "MESS_COMM_ERR_CMD_42", "D00A42: [--prefixdb] option and [--container] option must be set at same time." },//
			{ "MESS_COMM_ERR_CMD_44", "D00A44: [--list] option is not available at the same time as [--all] option, [--db] option, or [--container] option." },//
			{ "MESS_COMM_ERR_CMD_45", "D00A45: [--parallel] option is invalid. Set a integer value between 2-16." },//
			{ "MESS_COMM_ERR_CMD_46", "D00A46: [--acl] option and [--all] option or [--db] option must be set at same time." },//
			{ "MESS_COMM_ERR_CMD_47", "D00A47: [--parallel] option is available at same time as [--binary] option and [--out] option." },//
			{ "MESS_COMM_ERR_CMD_48", "D00A48: Property [load.input.threadNum] is invalid. Set a integer value between 1-128." },//
			{ "MESS_COMM_ERR_CMD_49", "D00A49: Property [load.output.threadNum] is invalid. Set a integer value between 1-16." },//
			{ "MESS_COMM_ERR_CMD_50", "D00A50: [--parallel] option is invalid. Specify a value between 1-16." },//
			{ "MESS_COMM_ERR_CMD_51", "D00A51: Both property [load.input.threadNum] and [load.output.threadNum] must be set." },//
			{ "MESS_COMM_ERR_CMD_54", "D00A54: The max of the length of string that can be specified as [--out] option is 20 characters." },//
			{ "MESS_COMM_ERR_CMD_55", "D00A55: [--intervals] option is invalid. Specify a value yyyyMMdd:yyyyMMdd format." },//
			{ "MESS_COMM_ERR_CMD_56", "D00A56: [--intervals] option is invalid. Specify a value yyyyMMdd(from):yyyyMMdd(to) format and specify so that from < to." },//
			{ "MESS_COMM_ERR_CMD_57", "D00A57: Property [intervalTimeZone] is invalid. Specify TimeZone or GMT+HH:mm format." },//
			{ "MESS_COMM_ERR_CMD_58", "D00A58: [--intervals] option and [--filterfile] option cannot be set at same time." },//
			{ "MESS_COMM_ERR_CMD_59", "D00A59: [--progress] option is invalid. Please set positive integer number." },//

			{ "MESS_COMM_PROC_PROCINFO_1", "The container name which did not be processed has been detected." },//
			{ "MESS_COMM_PROC_PROCINFO_2", "(%d/%d)Container %s is imported %s.(%s)" },//
			{ "MESS_COMM_PROC_PROCINFO_3", "Success" },//
			{ "MESS_COMM_PROC_PROCINFO_4", "Failure" },//
			{ "MESS_COMM_PROC_PROCINFO_5", "Number of target containers:%d ( Success:%d  Failure:%d )" },//
			{ "MESS_COMM_PROC_PROCINFO_6", "(%d/%d)container %s is exported %s.(%s)" },//
			{ "MESS_COMM_PROC_PROCINFO_7", "Number of containers:%d ( Success:%d  Failure:%d  Unprocessed:%d )" },
			{ "MESS_COMM_PROC_PROCINFO_8", "[Error List]" },
			{ "MESS_COMM_PROC_PROCINFO_A", "Number of containers : Success:%d  Failure:%d" },//
			{ "MESS_COMM_PROC_PROCINFO_B", "Number of containers : Success:%d  Failure:%d  Processing:%d" },//
			{ "MESS_COMM_PROC_PROCINFO_C", "[Skip List]" },
			{ "MESS_COMM_PROC_PROCINFO_D", "Number of views : %d" },
			{ "MESS_COMM_PROC_PROCINFO_E", "[Invalid views]" },


			{ "MESS_LOAD_ERR_INPUT_0", "D00E00: An unexpected error occurred while acquiring data."},

			{ "MESS_LOAD_ERR_OUTPUT_0", "D00F00: An unexpected error occurred while registering data  "},

			{
					"MESS_EXPORT_HELP_1",
					"gs_export --user username/password [--password password]"
							+ System.getProperty("line.separator")
							+ "--all [--acl]|--db|--container name... [--prefixdb db]|--containerregex regex... [--prefixdb db]"
							+ System.getProperty("line.separator")
							+ "[--directory directorypath]"
							+ System.getProperty("line.separator")
							+ "[--out [filename]][--binary [fileSizeLimit]]"
							+ System.getProperty("line.separator")
							+ /*"[--count maxFetchCount]*/"[--filterfile filename]"
							+ System.getProperty("line.separator")
							+ "[--intervals YYYYMMdd:YYYYMMdd]"
							+ System.getProperty("line.separator")
							+ "[--test][--force]"
							+ System.getProperty("line.separator")
							+ "[--silent][--verbose]"
							+ System.getProperty("line.separator")
			},
			{
					"MESS_EXPORT_HELP_2",
					System.getProperty("line.separator") },
			{
					"MESS_IMPORT_HELP_1",
					"gs_import --user username/password [--password password]"
							+ System.getProperty("line.separator")
							+ "--all [--acl]|--db|--container name... [--prefixdb db]|--containerregex regex... [--prefixdb db]"
							+ System.getProperty("line.separator")
							+ "[--directory directorypath]"
							+ System.getProperty("line.separator")
							+ "[--file filename...]"
							+ System.getProperty("line.separator")
							+ "[--append | --replace][--count maxCommitCount]"
							+ System.getProperty("line.separator")
							+ "[--intervals YYYYMMdd:YYYYMMdd]"
							+ System.getProperty("line.separator")
							+ "[--force]"
							+ System.getProperty("line.separator")
							+ "[--silent][--verbose]"
							+ System.getProperty("line.separator")
							+ System.getProperty("line.separator")
			},
			{
					"MESS_IMPORT_HELP_2",
					"gs_import --user username/password [--password password]"
							+ System.getProperty("line.separator")
							+ "[--append | --replace]"
							+ System.getProperty("line.separator")
							+ "[--force]"
							+ System.getProperty("line.separator")
			},
			{
					"MESS_IMPORT_HELP_3",
					"gs_import- --list"
							+ System.getProperty("line.separator")
							+ "[--directory directorypath]"
							+ System.getProperty("line.separator")
							+ "[--file filename...]"
			},
			{
					"MESS_EXPORT_VERSION",
					" ["+messageResource.VERSION+"]"
							+ System.getProperty("line.separator") },
			{ "MESSAGE_EXPORT_19", " " } };

	/**
	 * 国際化リソース取得メソッド
	 */
	public Object[][] getContents() {
		return contents;
	}
}
