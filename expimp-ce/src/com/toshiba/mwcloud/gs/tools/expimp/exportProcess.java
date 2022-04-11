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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toshiba.mwcloud.gs.AggregationResult;
import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.ContainerInfo;
import com.toshiba.mwcloud.gs.FetchOption;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.RowSet;
import com.toshiba.mwcloud.gs.experimental.DatabaseInfo;
import com.toshiba.mwcloud.gs.experimental.ExperimentalTool;
import com.toshiba.mwcloud.gs.experimental.PrivilegeInfo;
import com.toshiba.mwcloud.gs.experimental.PrivilegeInfo.RoleType;
import com.toshiba.mwcloud.gs.experimental.UserInfo;
import com.toshiba.mwcloud.gs.tools.common.GridDBJdbcUtils;
import com.toshiba.mwcloud.gs.tools.common.data.MetaContainerFileIO;
import com.toshiba.mwcloud.gs.tools.common.data.ToolConstants;
import com.toshiba.mwcloud.gs.tools.common.data.ToolContainerInfo;
import com.toshiba.mwcloud.gs.tools.expimp.GSConstants.TARGET_TYPE;
import com.toshiba.mwcloud.gs.tools.expimp.util.Utility;

/**
 * Export command processing class
 *
 *
 */
public class exportProcess {
	/**
	 * Command parameter information class
	 */
	private commandLineInfo comLineInfo;
	/**
	 * Internationalized message resource
	 */
	private static ResourceBundle messageResource;
	/**
	 * Logger class settings
	 */
	private static final Logger log = LoggerFactory.getLogger(exportProcess.class);
	/**
	 * Query filter file read data
	 */
	private static Map<String, String> m_filterQueryMap;

	/**
	 * Thread stop flag
	 */
	private static volatile boolean m_stopFlag;

	/**
	 * Constructor
	 *
	 * @param cli
	 *            Command parameter information class
	 */
	exportProcess(commandLineInfo cli) {
		this.comLineInfo = cli;
		messageResource = Utility.getResource();
	}

	/**
	 * Export processing method
	 */
	public boolean start() {
		m_stopFlag = false;
		try {
			// ------------------------------
			// Check the output directory and gs_export.json file
			// ------------------------------
			checkExistsDirFile();

			// --------------------------------
			// Get the list of container names to be exported
			// --------------------------------
			long startTime = System.currentTimeMillis();
			TargetContainerManager control = TargetContainerManager.getInstance();
			// Setting objects for parallel (must be executed before selectExportTargetContainer)
			int nThreads = comLineInfo.getParallelCount();
			control.setThreadNum(nThreads);

			control.selectExportTargetContainer(comLineInfo);
			long endTime = System.currentTimeMillis();

			int containerCount = control.getContainerCount();
			comLineInfo.sysoutString(messageResource.getString("MESS_EXPORT_PROC_EXPORTPROC_19")+comLineInfo.getDirectoryFullPath());
			if ( commandProgressStatus.getContainerSkipCount() > 0 ) {
				comLineInfo.sysoutString(String.format(messageResource.getString("MESS_EXPORT_PROC_EXPORTPROC_23"), containerCount, commandProgressStatus.getContainerSkipCount()));
			} else {
				comLineInfo.sysoutString(messageResource.getString("MESS_EXPORT_PROC_EXPORTPROC_20")+containerCount);
			}
			log.info("selectContainerNameList containerCount=["+containerCount+"] time=["+(endTime-startTime)+"]");
			commandProgressStatus.setContainerCount(containerCount);


			// ----------------------------------
			// Advance preparation
			// ----------------------------------
			// Date and time setting
			GSEIFileIO.createCalendarFileName();

			// Container information for management file generation
			List<GSEIContInfo> contInfoList = null;

			// Read query filter file
			readFilterfile();


			// ----------------------------------
			// Export database user ACL
			// ----------------------------------
			if ( comLineInfo.getAclFlag() ){
				exportACL();
			}
			comLineInfo.sysoutString("");

			// ----------------------------------
			// 4.2-Export View
			// ----------------------------------
			int viewCount = -1;

			if ( comLineInfo.isJdbcEnabled() ) {
				// Export view only if option is all or db
				if (comLineInfo.getTargetType() == TARGET_TYPE.ALL || comLineInfo.getTargetType() == TARGET_TYPE.DB) {
					viewCount = exportViews(comLineInfo.getDbNamelist());
				}
			}

			// Keep view export count for result display
			if (viewCount >= 0) {
				commandProgressStatus.addViewSuccessCount(viewCount);
			}

			if (comLineInfo.getTestFlag()) {
				// In test mode, the header for test mode result output is output.
				comLineInfo.sysoutString("Name                                      PartitionId Row");
				comLineInfo.sysoutString("------------------------------------------------------------------");
			}

			// -----------------------------------
			// Export
			// -----------------------------------
			startTime = System.currentTimeMillis();
			if ( nThreads > 1 ){
				contInfoList = new ArrayList<GSEIContInfo>();

				// Parallel processing
				// Creating a thread object
				ExportThread[] threadList = new ExportThread[nThreads];
				for ( int i = 0; i < nThreads; i++ ){
					threadList[i] = new ExportThread(i, comLineInfo);
				}

				// Thread start
				for ( int i = 0; i < nThreads; i++ ){
					threadList[i].start();
				}

				// Waiting for thread end
				for ( int i = 0; i < nThreads; i++ ){
					threadList[i].join();

					// Get results
					List<GSEIContInfo> resultList = threadList[i].getContList();
					contInfoList.addAll(resultList);
				}

			} else {
				// Sequential processing
				contInfoList = export();
			}
			commandProgressStatus.addContainerSuccessCount(contInfoList.size());

			endTime = System.currentTimeMillis();


			// -----------------------------------
			// Creating an Export management file
			// -----------------------------------
			// Container name, metafile, database name
			metaInformationFileIO fileIO = new metaInformationFileIO(comLineInfo);
			fileIO.writeExportManagerFile(contInfoList);

			// Export finished
			String msg = messageResource.getString("MESS_EXPORT_PROC_EXPORTPROC_7") + ": time=["+(endTime-startTime)+"]";
			comLineInfo.sysoutVerboseString(msg);
			log.info(msg);

			return true;

		} catch ( GSEIException e ){
			comLineInfo.sysoutString(e.getMessage());
			log.error(e.getMessage(), e);
			return false;

		} catch (Exception e) {
			// "An error occurred while executing the export process"
			String errMsg = messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_4") + ":"+ e.getMessage();
			commandProgressStatus.setContainerStatus("", false, errMsg);
			comLineInfo.sysoutString(errMsg);
			log.error(errMsg, e);
			return false;
		}
	}

	/**
	 * Outputs database user ACL information.
	 *
	 * @throws Exception
	 */
	public void exportACL() throws Exception {

		GridStore store = null;
		try {
			store = gridStoreServerIO.getConnection(comLineInfo);

			// Make sure you are an admin user.
			UserInfo myUser = ExperimentalTool.getCurrentUser(store);
			if ( !myUser.isSuperUser() ){
				throw new GSEIException(messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_25"));
			}

			// Acquisition of DB / user information
			Map<String, UserInfo> userList = ExperimentalTool.getUsers(store);
			Map<String, DatabaseInfo> dbList = ExperimentalTool.getDatabases(store);
			comLineInfo.sysoutString(messageResource.getString("MESS_EXPORT_PROC_EXPORTPROC_21") + userList.size());
			comLineInfo.sysoutString(messageResource.getString("MESS_EXPORT_PROC_EXPORTPROC_22") + dbList.size());
			if ( comLineInfo.getTestFlag() ){
				return;
			}

			// JSON file creation
			StringWriter sw = new StringWriter();
			Map<String, Object> properties = new HashMap<String, Object>(1);
			properties.put(JsonGenerator.PRETTY_PRINTING, true);	// Settings to format and output
			JsonGeneratorFactory factory = Json.createGeneratorFactory(properties);
			JsonGenerator gen = factory.createGenerator(sw);

			gen.writeStartObject();
			gen.write("version", ToolConstants.META_FILE_VERSION); // For future compatibility (meta information file version)

			// User list output
			gen.writeStartArray("user");
			for ( Map.Entry<String, UserInfo> entry : userList.entrySet() ){
				gen.writeStartObject();
				// V4.5 Change the output contents depending on the user and role
				if ( entry.getValue().isRole() ) {
					// Output for V4.5 roll
					gen.write("isRole", true);
					gen.write("username", entry.getValue().getRoleName());
				} else {
					// Output for V4.5 users
					gen.write("isRole", false);
					gen.write("username", entry.getKey());
					gen.write("password", entry.getValue().getHashPassword());
				}
				gen.writeEnd();
			}
			gen.writeEnd();

			// Database list, ACL list output
			gen.writeStartArray("database");
			for(Map.Entry<String, DatabaseInfo> e : dbList.entrySet()) {
				gen.writeStartObject();
				gen.write("name", e.getKey());

				gen.writeStartArray("acl");
				for (Map.Entry<String, PrivilegeInfo> aclEntry : e.getValue().getPrivileges().entrySet()){
					gen.writeStartObject();
					// The contents of V4.5 acl are the same for both users and roles.
					gen.write("username", aclEntry.getKey());
					// V4.3 User Privilege Export / write database [] / acl [] / role values
					PrivilegeInfo userPrivilegeInfo = aclEntry.getValue();
					if (userPrivilegeInfo != null) {
						RoleType userRole = userPrivilegeInfo.getRole();
						if (RoleType.ALL.equals(userRole)) {
							gen.write("role", "ALL");
						} else if (RoleType.READ.equals(userRole)) {
							gen.write("role", "READ");
						}
					}
					gen.writeEnd();
				}
				gen.writeEnd();
				gen.writeEnd();
			}
			gen.writeEnd();

			gen.writeEnd();
			gen.close();

			// 5. Write file
			File file = new File(comLineInfo.getDirectoryFullPath(), GSConstants.FILE_GS_EXPORT_ACL_JSON);
			PrintWriter outACLFile = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(file),ToolConstants.ENCODING_JSON)));
			outACLFile.write(sw.toString());
			outACLFile.flush();
			outACLFile.close();

		} catch ( GSEIException e ){
			throw e;

		} catch ( Exception e ){
			// An unexpected error occurred while acquiring database, user, or acl
			throw new GSEIException( messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_24") +" msg=["+e.getMessage()+"]", e);

		} finally {
			try {
				if ( store != null ) store.close();
			} catch ( Exception e ){}
		}
	}


	/**
	 * Exports the view to the database to be exported.
	 *
	 * @return Number of successful views
	 */
	private int exportViews(List<String> dbList) throws GSEIException {
		Connection conn = null;

		List<GSEIViewInfo> views = new ArrayList<GSEIViewInfo>();

		// View definition acquisition process
		long startTime = System.currentTimeMillis();
		try {
			for (String dbName : dbList) {
				conn = gridStoreServerIO.getJdbcConnection(comLineInfo, dbName);
				views.addAll(getViews(conn));
			}
		} catch ( GSEIException e ){
			throw e;
		} catch ( Exception e ) {
			// D0052A: There was an error in the view export process.
			throw new GSEIException(messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_2A"), e);
		} finally {
			try {
				if ( conn != null ) conn.close();
			} catch ( Exception e ){}
		}

		long endTime = System.currentTimeMillis();

		// Output processing time to log
		log.info("export: exportViews," + (endTime - startTime));

		// For testing, no file output
		if ( comLineInfo.getTestFlag() ){
			return views.size();
		}

		// View definition file output
		if (!views.isEmpty()) {
			writeViewJson(views);
		}

		return views.size();
	}

	/**
	 * Exports the view definition file.
	 *
	 * @param views
	 * @throws GSEIException
	 */
	private void writeViewJson(List<GSEIViewInfo> views) throws GSEIException {
		try {
			long startTime = System.currentTimeMillis();
			// JSON file creation
			StringWriter sw = new StringWriter();
			Map<String, Object> properties = new HashMap<String, Object>(1);
			properties.put(JsonGenerator.PRETTY_PRINTING, true);	// Settings to format and output
			JsonGeneratorFactory factory = Json.createGeneratorFactory(properties);
			JsonGenerator gen = factory.createGenerator(sw);

			gen.writeStartObject();
			gen.write("version", ToolConstants.META_FILE_VERSION); // For future compatibility (meta information file version)

			// View definition list output
			gen.writeStartArray("view");
			for ( GSEIViewInfo view : views ){
				gen.writeStartObject();
				gen.write("database", view.getDbName());
				gen.write("name", view.getViewName());
				gen.write("definition", view.getDefinition());
				gen.writeEnd();
			}
			gen.writeEnd();

			gen.writeEnd();
			gen.close();

			// File writing
			File file = new File(comLineInfo.getDirectoryFullPath(), GSConstants.FILE_GS_EXPORT_VIEW_JSON);
			PrintWriter outViewFile = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(file),ToolConstants.ENCODING_JSON)));
			outViewFile.write(sw.toString());
			outViewFile.flush();
			outViewFile.close();
			long endTime = System.currentTimeMillis();

			// Output processing time to log
			log.info("export: writeViewJson," + (endTime - startTime));

		} catch ( Exception e ) {
			// D0052B: An error occurred while creating the view definition file
			throw new GSEIException(messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_2B"), e);
		}
	}

	/**
	 * Export processing is performed.
	 *
	 * @return List of successful containers
	 */
	public List<GSEIContInfo> export() {

		long timeWrite = 0;
		MetaContainerFileIO metaFileIO = null;
		GSEIFileIO rowFile = null;
		String prevDbName = null;
		GridStore store = null;
		Connection conn = null;
		List<GSEIContInfo> contInfoList = new ArrayList<GSEIContInfo>();
		Set<String> setPartitionTable = new HashSet<String>();
		//@SuppressWarnings("deprecation")
		//final FetchOption fetchOpt = FetchOption.SIZE;
		final FetchOption fetchOptPARTIAL = FetchOption.PARTIAL_EXECUTION;


		// The object that manages the container list
		TargetContainerManager control = TargetContainerManager.getInstance();
		TargetContainerManager.getInstance().registThread(Thread.currentThread().getId());
		//RowSerialize.registerTheradId(Thread.currentThread().getId());

		// File IO object generation
		if ( !comLineInfo.getTestFlag() ){
			metaFileIO = new MetaContainerFileIO();
			rowFile = GSEIFileIOFactory.createFileIO(comLineInfo.getRowFileType(), comLineInfo);
			rowFile.startWrite();
		}


		// Request loop
		while ( true ){

			// Request acquisition
			DbPartition dbPartition = control.getContainerList();
			if (dbPartition == null){
				// Ends when there are no more acquisition containers
				break;
			}
			String dbName = dbPartition.getDbName();

			try {
				// GridStore connection (reconnect if DB is different)
				//if ( ((prevDbName == null)) || ((!prevDbName.equalsIgnoreCase(dbName))) ){
				if ( ((prevDbName == null)&&(dbName!=null)) || ((prevDbName!=null)&&(!prevDbName.equalsIgnoreCase(dbName))) || (store==null) ){
					if ( store != null ) {
						store.close();
						store = null;
					}
					store = gridStoreServerIO.getConnection(comLineInfo, dbName);	// When connecting, it is necessary to specify the DB name in a case-sensitive manner.
					prevDbName = dbName;

					if ( comLineInfo.isJdbcEnabled() ) {
						if (conn != null) {
							conn.close();
							conn = null;
						}
						conn = gridStoreServerIO.getJdbcConnection(comLineInfo, dbName);	// When connecting, it is necessary to specify the DB name in a case-sensitive manner.

						// Get a list of partition table names when connecting to JDBC
						setPartitionTable = gridStoreServerIO.getPartitionTableNames(conn);
					}
				}

				// Container loop
				for ( String contName: dbPartition.getContainerList() ){
					timeWrite = 0;
					long startTimeCont = System.currentTimeMillis();

					Container<?, Row> container = null;
					Query<Row> query = null;
					RowSet<Row> rs = null;
					ToolContainerInfo toolContInfo = null;
					long rsCount = 0;
					try{
						// Object for meta information output
						toolContInfo = new ToolContainerInfo();
						toolContInfo.setDbName(dbName);
						toolContInfo.setPartitionNo(dbPartition.getPartitionId());
						toolContInfo.setContainerFileType(comLineInfo.getRowFileType());

						// Get container information
						ContainerInfo contInfo = store.getContainerInfo(contName);
						if ( contInfo == null ) {
							throw new GSEIException(messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_18"));
						}
						toolContInfo.setContainerInfo(contInfo);
						contName = contInfo.getName();

						if ( setPartitionTable.contains(contInfo.getName()) ){
							// For a partitioned table, get it via JDBC.
							toolContInfo.setTablePartitionProperties(GridDBJdbcUtils.getTablePartitionProperties(conn, contInfo.getName()));
							toolContInfo.setExpirationInfo(GridDBJdbcUtils.getExpirationInfo(conn, contInfo.getName()));
						}

						// Search
						container = store.getContainer(contName);
						String queryString = getQueryStr(toolContInfo);
						try {
							if (comLineInfo.getSchemaOnlyFlag()) {
								// If export is specified only for V4.5 container definition, raw data will not be acquired.
							} else if (comLineInfo.getTestFlag()){
								// Test mode
								rsCount = getRowCount(store, conn, setPartitionTable, contName, container);
							} else {
								query = container.query(queryString);
								query.setFetchOption(fetchOptPARTIAL, true);
								rs = query.fetch();
							}

						} catch ( GSException ex ){
							// There was an error in the search.
							String errMsg = messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_23") + queryString;
							throw new GSEIException(errMsg, ex);
						}

						if ( comLineInfo.getTestFlag() ){
							// In test mode, just output the number. Do not take out ROW.
							// V4.2 Container name Partition ID Output the number of rows
							String outputLine = String.format("%-41s", toolContInfo.getFullName()) + " "
									+ String.format("%11s", toolContInfo.getPartitionNo())
									+ " " + String.format("%11s", rsCount);
							comLineInfo.sysoutString(outputLine);
							GSEIContInfo eiContInfo = new GSEIContInfo(toolContInfo.getDbName(), toolContInfo.getName(),
									toolContInfo.getFileBaseName() + ToolConstants.FILE_EXT_METAINFO);
							contInfoList.add(eiContInfo);
							continue;
						}

						// Raw data acquisition & output
						int rowNum = 0;
						long startTimeWrite = System.currentTimeMillis();
						rowFile.startWriteContainer(toolContInfo);
						timeWrite += ( System.currentTimeMillis()-startTimeWrite );
						if ( comLineInfo.getSchemaOnlyFlag() ) {
							// If export is specified only for V4.5 container definition, raw data will not be acquired.
							// However, to determine the filename of the metadata file
							// startWriteContainer()Needs to be called
							// startWriteContainer()Implemented a branch that skips raw data acquisition after calling.
						} else {
							while (rs.hasNext()) {
								Row row = rs.next();
								startTimeWrite = System.currentTimeMillis();
								rowFile.writeRow(row, rowNum++);
								timeWrite += ( System.currentTimeMillis()-startTimeWrite );
								rsCount++;
							}
						}
						startTimeWrite = System.currentTimeMillis();
						rowFile.endWriteContainer();
						timeWrite += ( System.currentTimeMillis()-startTimeWrite );

						if ( rowNum == 0 ){
							toolContInfo.setContainerFile(null);
						}

						// Meta information output
						metaFileIO.writeMetaFile(toolContInfo, comLineInfo.getDirectoryPath(), comLineInfo.getOutFlag());

						// Information for output management information
						GSEIContInfo eiContInfo = new GSEIContInfo(toolContInfo.getDbName(), toolContInfo.getName(),
								toolContInfo.getFileBaseName() + ToolConstants.FILE_EXT_METAINFO);
						contInfoList.add(eiContInfo);

						long endTimeCont = System.currentTimeMillis();

						if ( comLineInfo.getSchemaOnlyFlag() ) {
							// If export is specified only for V4.5 container definition, the number of raw data is not output.
							comLineInfo.sysoutString(toolContInfo.getFullName());
						} else {
							comLineInfo.sysoutString(toolContInfo.getFullName()+" : "+rsCount);
						}
						log.info("export: db,"+toolContInfo.getDbName()+",name,"+toolContInfo.getName()+",count,"+rsCount+",Time all,"
								+(endTimeCont-startTimeCont)+",export,"+(endTimeCont-startTimeCont-timeWrite)
								+",write,"+timeWrite);


					} catch ( Exception e ){
						// An unexpected error occurred while operating row file
						String errMsg = messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_5")
								+ ": containerName=["+contName+"] msg=["+ e.getMessage()+"]";
						commandProgressStatus.setContainerStatus(toolContInfo.getFullName(), false, errMsg);
						if ( !comLineInfo.getForceFlag() ){
							if ( !(e instanceof GSEIException) ){
								e = new GSEIException(errMsg, e);
							}
							exportProcess.m_stopFlag = true;
							throw e;
						} else {
							// If the --force option is specified, output to the log and continue
							comLineInfo.sysoutString(errMsg);
							log.error(errMsg, e);
						}

					} finally {
						if ( rs != null ) rs.close();
						if ( query != null ) query.close();
						if ( container != null ) container.close();
					}

					// Thread stop check
					if ( exportProcess.m_stopFlag ){
						break;
					}

				} // Container loop

			} catch ( Exception e ){
				String errMsg = null;
				if ( e instanceof GSEIException ){
					errMsg = e.getMessage();
				} else {
					errMsg = messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_5") + " msg=["+e.getMessage()+"]";
					commandProgressStatus.setContainerStatus(null, false, errMsg);
				}
				log.error(errMsg, e);
				exportProcess.m_stopFlag = true;
				break;	// The error here exits without continuing
			}

			// Thread stop check
			if ( exportProcess.m_stopFlag ){
				break;
			}

		} // Request acquisition loop

		if ( !comLineInfo.getTestFlag() ){
			metaFileIO.writeEnd();
			rowFile.endWrite();
		}

		if ( store != null ) {
			try {
				store.close();
			} catch (GSException e) {}
		}

		return contInfoList;
	}


	/**
	 * Gets the number of rows in the container for the --test option.
	 *
	 * @param store
	 * @param conn
	 * @param setPartitionTable
	 * @param contName
	 * @return Number of rows in the container
	 * @throws Exception
	 */
	private long getRowCount(GridStore store, Connection conn, Set<String> setPartitionTable, String contName, Container<?, Row> container) throws Exception {
		long rowCount = 0;
		if ( setPartitionTable.contains(contName) ){
			String queryString = "select count(*) from \""+contName+"\"";
			Statement stmt = conn.createStatement();
			ResultSet resultSet = stmt.executeQuery(queryString);
			if ( resultSet.next() ){
				rowCount = resultSet.getInt(1);
			}
			resultSet.close();
			stmt.close();
		} else {
			String queryString = "select count(*)";
			Query<AggregationResult> queryCount = container.query(queryString, null);
			RowSet<AggregationResult> rowSetCount = queryCount.fetch();
			if ( rowSetCount.hasNext() ){
				AggregationResult agg = (AggregationResult)rowSetCount.next();
				rowCount = agg.getLong();
			}
			rowSetCount.close();
		}
		return rowCount;
	}


	/**
	 * Check the existence of the output destination directory.
	 *
	 * @throws GSEIException
	 */
	private void checkExistsDirFile() throws GSEIException {
		try {
			File dir = null;
			if ( comLineInfo.getDirectoryPath() != null ){
				// If you do not check the directory path, create it
				dir = new File(comLineInfo.getDirectoryPath());
				if (!dir.exists()) {
					if ( !dir.mkdirs() ){
						// An error occurred during the output directory creation process.
						throw new GSEIException(messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_16")+": path=["+dir.getAbsolutePath()+"]");
					}
				}
			}
			File exportJson = new File(dir, GSConstants.FILE_GS_EXPORT_JSON);
			if ( exportJson.exists() ){
				// The Export result already exists in the specified directory.
				throw new GSEIException(messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_19")
						+": path=["+dir.getAbsolutePath()+"]");
			}

		} catch ( GSEIException e ){
			throw e;
		} catch ( Exception e ){
			// An error occurred in the pre-check of the directory file.
			throw new GSEIException(messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_20")+":"+e.getMessage(), e);
		}
	}


	/**
	 * Returns a search query for the container.
	 *
	 * @param contInfo Container information object
	 * @return Container information object
	 * @throws GSEIException
	 */
	private String getQueryStr(ToolContainerInfo contInfo) {

		String queryString = "select *";

		if ( m_filterQueryMap != null ){
			for(Map.Entry<String, String> entry : m_filterQueryMap.entrySet()){
				String containerName = entry.getKey();

				// Matching with regular expressions. Case insensitive
				Pattern p = Pattern.compile(containerName.trim(),Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(contInfo.getName());

				if( m.find() ){
					queryString = entry.getValue();
					contInfo.setFilterCondition(queryString); // gs_export.json For output
					break;
				}
			}
		}
		return queryString;
	}

	/**
	 * Read the query file for extraction search.
	 *
	 * @throws GSEIException
	 */
	private void readFilterfile() throws GSEIException {

		if ( !comLineInfo.getFilterfileFlag() ){
			m_filterQueryMap = null;
			return;
		}

		JsonReader jr = null;
		BufferedReader br = null;
		String filterFileName = comLineInfo.getFilterFileName();
		try {
			// Check for the existence of the filterfile file
			File file = new File(filterFileName);
			if ( !file.exists() ){
				String msg = messageResource.getString("MESS_COMM_ERR_METAINFO_43")+": "+filterFileName;
				throw new GSEIException(msg);
			}

			FileInputStream fis = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(fis,"UTF-8"));

			Map<String, String> map = new LinkedHashMap<String, String>();
			String line;
			int lineNum = 0;
			while ( ( line = br.readLine()) != null ) {
				lineNum++;
				// Skip blank lines
				if ( line.isEmpty() ) continue;
				// Since ":" cannot be used in the container name, the first ":" is judged as a delimiter.
				int pos = line.indexOf(':') ;
				if( pos == -1){ // ":"Error if none. No other error checking is done.
					// D00944: There is an error in the definition of the file specified by --filterfile.
					String msg = messageResource.getString("MESS_COMM_ERR_METAINFO_44")+": lineNum=" + lineNum + ":" + line;
					throw new GSEIException(msg);
				}
				map.put(line.substring(0, pos ), line.substring(pos+1, line.length()));
			}

			// If there is no valid definition, skip it. If null is returned at the upper level, skip it.
			if( map.size() == 0 ){
				map = null;
			}

			m_filterQueryMap = map;

		} catch ( GSEIException e ){
			throw e;

		} catch ( Exception e ) {
			// An unexpected error occurred while reading the file specified by \"--filterfile\"
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_METAINFO_45")
					+": file=["+filterFileName+"] msg=["+e.getMessage()+"]", e);
		} finally {
			try {
				if ( jr != null ) jr.close();
				if ( br != null ) br.close();
			} catch ( Exception e ){}
		}
	}

	/**
	 * (JDBC)#views Gets the view definition from the metatable.
	 * @param conn
	 * @return List<GSEIViewInfo>
	 * @throws Exception
	 */
	public static List<GSEIViewInfo> getViews(Connection conn) throws GSEIException {
		Statement stmt = null;
		ResultSet rs = null;
		List<GSEIViewInfo> views = new ArrayList<GSEIViewInfo>();
		try {
			stmt = conn.createStatement();
			// Execute query SELECT * FROM "#views"
			rs = stmt.executeQuery(ToolConstants.STMT_SELECT_META_VIEWS);
			while (rs.next()) {
				views.add(
					new GSEIViewInfo(
						rs.getString(ToolConstants.META_VIEWS_DATABASE_NAME),
						rs.getString(ToolConstants.META_VIEWS_VIEW_NAME),
						rs.getString(ToolConstants.META_VIEWS_VIEW_DEFINITION)
					)
				);
			}
		} catch (Exception e) {
			// D0052C: An error occurred during the view acquisition process.
			throw new GSEIException(messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_2C"), e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {};
		}
		return views;
	}

}

/**
 * Thread class for Export processing.
 */
class ExportThread extends Thread {

	/**
	 * Thread number
	 */
	int m_threadNo;

	/**
	 * Setting information object
	 */
	commandLineInfo m_operationInfo;


	/**
	 * Information for gs_export.json output
	 */
	List<GSEIContInfo> m_contInfoList;


	/**
	 * constructor
	 *
	 * @param threadNo Thread number
	 * @param opertationInfo Setting information object
	 */
	ExportThread(int threadNo, commandLineInfo opertationInfo){
		m_threadNo = threadNo;
		m_operationInfo = opertationInfo;
	}


	/**
	 * Returns the thread number
	 *
	 * @return Thread number
	 */
	public int getThreadNo(){
		return m_threadNo;
	}

	public List<GSEIContInfo> getContList(){
		return m_contInfoList;
	}

	/**
	 * Export processing is performed in the thread.
	 */
	public void run(){

		exportProcess proc = new exportProcess(m_operationInfo);
		m_contInfoList = proc.export();

	}
}

