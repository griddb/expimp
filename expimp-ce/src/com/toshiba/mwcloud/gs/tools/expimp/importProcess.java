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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParsingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.ContainerInfo;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GSTimeoutException;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.IndexInfo;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.TriggerInfo;
import com.toshiba.mwcloud.gs.experimental.DatabaseInfo;
import com.toshiba.mwcloud.gs.experimental.ExperimentalTool;
import com.toshiba.mwcloud.gs.experimental.ExtendedContainerInfo;
import com.toshiba.mwcloud.gs.experimental.PrivilegeInfo;
import com.toshiba.mwcloud.gs.experimental.PrivilegeInfo.RoleType;
import com.toshiba.mwcloud.gs.experimental.UserInfo;
import com.toshiba.mwcloud.gs.tools.common.GridDBJdbcUtils;
import com.toshiba.mwcloud.gs.tools.common.GridStoreCommandException;
import com.toshiba.mwcloud.gs.tools.common.data.ExpirationInfo;
import com.toshiba.mwcloud.gs.tools.common.data.MetaContainerFileIO;
import com.toshiba.mwcloud.gs.tools.common.data.TablePartitionProperty;
import com.toshiba.mwcloud.gs.tools.common.data.TimeIntervalInfo;
import com.toshiba.mwcloud.gs.tools.common.data.ToolConstants;
import com.toshiba.mwcloud.gs.tools.common.data.ToolConstants.RowFileType;
import com.toshiba.mwcloud.gs.tools.common.data.ToolContainerInfo;
import com.toshiba.mwcloud.gs.tools.expimp.GSConstants.TARGET_TYPE;
import com.toshiba.mwcloud.gs.tools.expimp.util.Report;
import com.toshiba.mwcloud.gs.tools.expimp.util.Utility;

/**
 * Import command processing class
 *
 *
 */
public class importProcess {
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
	private static final Logger log = LoggerFactory.getLogger(importProcess.class);

	/**
	 * Put processing time (container unit)
	 */
	private long m_timePut;

	/**
	 * Raw file read object
	 */
	private GSEIFileIO m_fileIO;

	/**
	 * Thread stop flag
	 */
	private static volatile boolean m_stopFlag;

	private Set<String> m_partitionTableSet;

	/**
	 * Constructor
	 *
	 * @param cli Command parameter information class
	 */
	public importProcess(commandLineInfo cli) {
		this.comLineInfo = cli;
		messageResource = Utility.getResource();
	}


	/**
	 * Checks the existence of import target directories and files.
	 *
	 * @throws GSEIException
	 */
	private void checkExistsDirFile() throws GSEIException{
		File dir = new File(comLineInfo.getDirectoryPath());	// Current directory if -d is not specified
		if ( !dir.exists() ){
			// The directory specified by the -d option does not exist
			throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_25")+": dir=["+dir.getAbsolutePath()+"]");
		}
		if ( !dir.isDirectory() ){
			// The path specified by the -d option is not a directory
			throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_26")+": dir=["+dir.getAbsolutePath()+"]");
		}

		if ( comLineInfo.getFileNameList() != null ){
			for ( String fileName: comLineInfo.getFileNameList() ){
				if ( !fileName.toLowerCase().endsWith(GSConstants.FILE_EXT_JSON.toLowerCase()) ){
					// The specified file is not a meta information file (* .json)
					throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_27")
							+ ": fileName=["+fileName+"]");
				}

				try {
					File file = new File(fileName);
					if ( file.getParentFile() != null ){
						// Only file names that do not include directories can be specified in the -f option.
						throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_30")
								+": fileName=["+fileName+"]");
					}

					file = new File(dir, fileName);
					if ( !file.exists() ){
						// The file specified by the -f option does not exist
						throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_28")+": file=["+file.getCanonicalPath()+"]");
					}
					if ( !file.isFile() ){
						// The name specified with the -f option is not a file
						throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_29")+": file=["+file.getCanonicalPath()+"]");
					}

				} catch ( IOException e ){
					// The file specified by the -f option is invalid
					throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_31")+": fileName=["+fileName+"]", e);
				}
			}
		}
	}


	/**
	 * Import processing method
	 *
	 * @return true:success  false:Failure
	 * @throws GSEIException
	 */
	public boolean start() throws GSEIException {
		boolean status = true;
		m_stopFlag = false;

		try {
			long startReadTime = System.currentTimeMillis();
			// ----------------------------------------------------
			// Check for the existence of the directory file specified by the option
			// ----------------------------------------------------
			checkExistsDirFile();

			// ----------------------------------------------------
			// Import or list Get the list of containers to be displayed from a local file
			// ----------------------------------------------------
			// Export Import management file
			TargetContainerManager mng = TargetContainerManager.getInstance();
			Map<String, List<GSEIContInfo>> map = mng.selectImportContainerList(comLineInfo);
			Set<String> dbNameList = mng.getDatabaseList();

			if ( comLineInfo.getListFlag() ){
				// List display
				listFiles(map);
				return true;
			}

			// Number of target containers
			int containerCount = 0;
			for ( Map.Entry<String, List<GSEIContInfo>> entry : map.entrySet() ){
				containerCount += entry.getValue().size();
			}
			if ( containerCount == 0 ){
				// Target container does not exist in the file
				throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_32"));
			}
			long endReadTime = System.currentTimeMillis();
			commandProgressStatus.setContainerCount(containerCount);
			if ( commandProgressStatus.getContainerSkipCount() > 0 ) {
				comLineInfo.sysoutString(String.format(messageResource.getString("MESS_EXPORT_PROC_EXPORTPROC_23"), containerCount, commandProgressStatus.getContainerSkipCount()));
			} else {
				comLineInfo.sysoutString(messageResource.getString("MESS_EXPORT_PROC_EXPORTPROC_20")+containerCount);
			}

			log.info("Get Container Information from metaInfoFile. : containerCount=["+containerCount+"] time=["+(endReadTime-startReadTime)+"]");

			// ----------------------------------------------------
			// 4.2- Read view definition file
			// [Memo] Import the view after importing the container.
			// ----------------------------------------------------

			// Read the list of views of the database to be imported from the file.
			// [Memo] It also serves as a preliminary confirmation that the file is not damaged.
			List<GSEIViewInfo> targetViews = null;
			if ( comLineInfo.isJdbcEnabled() ) {
				// Import views only if option is all or db
				if (comLineInfo.getTargetType() == TARGET_TYPE.ALL || comLineInfo.getTargetType() == TARGET_TYPE.DB) {
					targetViews = readViewJson(dbNameList);
					if (targetViews != null) {
						// Number of target views : n
						//comLineInfo.sysoutString(String.format(messageResource.getString("MESS_EXPORT_PROC_EXPORTPROC_24"), targetViews.size()));
						log.info("Get definitions from view definition file. : viewCount=[" + targetViews.size() + "]");
					}
				}
			}

			// ----------------------------------------------------
			// Database - ACL - User
			// ----------------------------------------------------
			Set<String> skipDbList = null;
			if ( comLineInfo.getAclFlag() ){
				// For all migrations
				importACL();
			} else {
				// Check if the DB of the container to be imported exists.
				// If it does not exist, create it if you are an administrator
				skipDbList = checkAndCreateDb(dbNameList);
			}
			comLineInfo.sysoutString("");

			// ----------------------------------------------------
			// Execution of container import process
			// ----------------------------------------------------
			int nThreads = comLineInfo.getParallelCount();		// Degree of parallelism
			//RowSerialize.prepare(nThreads);						// Preparing for deserialization

			if ( nThreads > 1 ){
				// Parallel processing



				ImportThread[] threadList = new ImportThread[nThreads];
				int n = 0;
				
				// 並列数分のリスト作成
				List<List<GSEIContInfo>> listGCIs = new ArrayList<List<GSEIContInfo>>();
				for ( int i = 0; i < nThreads; i++ ){
					listGCIs.add(new ArrayList<GSEIContInfo>());
				}
				// 並列数分にimport対象コンテナを振り分け
				for ( List<GSEIContInfo> list : map.values() ){
					List<GSEIContInfo> tmp = new ArrayList<GSEIContInfo>(listGCIs.get(n%nThreads));
					tmp.addAll(list);
					listGCIs.set(n%nThreads, tmp);
					n++;
				}
				int nt = 0;
				// 並列数分のスレッド生成
				for ( List<GSEIContInfo> list : listGCIs ){
					threadList[nt] = new ImportThread(nt, comLineInfo);
					threadList[nt].setContainerList(list, skipDbList);
					nt++;
				}
				
				for ( int i = 0; i < nThreads; i++ ){
					threadList[i].start();
				}
				for ( int i = 0; i < nThreads; i++ ){
					threadList[i].join();

					// Get results
					int successCount = threadList[i].getSuccessCount();
					commandProgressStatus.addContainerSuccessCount(successCount);
				}

			} else {
				// Sequential processing
				for ( List<GSEIContInfo> list : map.values() ){
					int successCount = import1(list, skipDbList);
					commandProgressStatus.addContainerSuccessCount(successCount);
				}
			}

			if ( containerCount != commandProgressStatus.getContainerSuccessCount() ){
				status = false;
			}

			// ----------------------------------------------------
			// View import process execution
			// ----------------------------------------------------
			if (targetViews != null) {
				int viewCount = importViews(targetViews,skipDbList);
				commandProgressStatus.addViewSuccessCount(viewCount);
			}

			// ----------------------------------------------------
			// Check for invalid views
			// ----------------------------------------------------
			if (targetViews != null) {
				int invalidCount = checkViews(targetViews,skipDbList);
			}

		} catch (GSEIException e)  {
			comLineInfo.sysoutString(e.getMessage());
			log.error(e.getMessage(), e);
			commandProgressStatus.setContainerStatus(null, false, e.getMessage());
			status = false;

		} catch (Exception ee) {
			// An error occurred during the import process.
			comLineInfo.sysoutString(messageResource.getString("MESS_IMPORT_ERR_IMPORTMAIN_1") + ee.getMessage());
			log.error(messageResource.getString("MESS_IMPORT_ERR_IMPORTMAIN_1"), ee );
			commandProgressStatus.setContainerStatus(null, false, messageResource.getString("MESS_IMPORT_ERR_IMPORTMAIN_1") + ee.getMessage());
			status = false;
		}

		return status;

	}


	/**
	 * Check if the database exists and if not:
	 *   Administrator user: Create, General user: Error
	 * will be occurs
	 *
	 * @param dbNameList Database list of import destination
	 * @throws Exception
	 */
	private Set<String> checkAndCreateDb(Set<String> dbNameList) throws Exception {

		GridStore store = null;
		Set<String> skipDbList = null;
		try {
			store = gridStoreServerIO.getConnection(comLineInfo);
			Map<String, DatabaseInfo> gsDatabaseMap = ExperimentalTool.getDatabases(store);
			UserInfo userInfo = ExperimentalTool.getCurrentUser(store);

			for ( String dbName : dbNameList ){
				if ( dbName == null || dbName.equalsIgnoreCase(ToolConstants.PUBLIC_DB)){
					// Skip because it is the default DB
					continue;
				}

				// Check if it exists
				if ( !gsDatabaseMap.containsKey(dbName) ){

					// If it does not exist, create it if you are an administrator. Error for general users.
					if ( !userInfo.isSuperUser() ){
						String msg = messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_39")+" database=["+dbName+"]";
						if ( comLineInfo.getForceFlag() ){
							comLineInfo.sysoutString(msg);
							commandProgressStatus.setContainerStatus(dbName+".*", false, msg);
							if ( skipDbList == null ) skipDbList = new HashSet<String>();
							skipDbList.add(dbName.toLowerCase());

							continue;
						} else {
							throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_39")+" database=["+dbName+"]");
						}
					}

					DatabaseInfo dbInfo = new DatabaseInfo();
					dbInfo.setName(dbName);
					ExperimentalTool.putDatabase(store, dbName, dbInfo, false);
				}
			}

			return skipDbList;

		} catch ( GSEIException e ){
			throw e;
		} catch ( Exception e ){
			throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_3A"), e);
		} finally {
			if ( store != null ){
				try {
					store.close();
				} catch (Exception e){}
			}
		}
	}


	/**
	 * Database - User - Import the ACL.
	 */
	private void importACL() throws Exception{

		GridStore store = null;
		try {
			// Connection
			store = gridStoreServerIO.getConnection(comLineInfo);

			// Make sure you are an admin user.
			UserInfo myUser = ExperimentalTool.getCurrentUser(store);
			if ( !myUser.isSuperUser() ){
				throw new GSEIException(messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_25"));
			}

			List<UserInfo> userInfoList = null;
			Map<String, Map<String, PrivilegeInfo>> dbInfoMap = null;
			try {
				// Read ACL file
				File file = new File(comLineInfo.getDirectoryFullPath(), GSConstants.FILE_GS_EXPORT_ACL_JSON);
				if ( !file.exists() ){
					throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_35"));
				}
				JsonParser jp = Json.createParser(new InputStreamReader(MetaContainerFileIO.skipBOM(new FileInputStream(file)), ToolConstants.ENCODING_JSON));
				if ( !jp.hasNext() ) throw new GridStoreCommandException("json invalid");
				Event e = jp.next();
				if ( e != Event.START_OBJECT ){
					throw new Exception();
				}
				while(jp.hasNext()){
					Event event = jp.next();
					if ( event == Event.KEY_NAME ){
						String key = jp.getString();
						if ( key.equalsIgnoreCase("user") ){
							userInfoList = readUser(jp);
						} else if ( key.equalsIgnoreCase("database") ){
							dbInfoMap = readDatabase(jp);
						}

					} else if ( event == Event.END_OBJECT ){
						break;
					}
				}
			} catch ( JsonParsingException e ){
				throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_3B")+" msg=["+e.getMessage()+"]", e);
			}

			// User created
			Map<String, UserInfo> gsUserMap = ExperimentalTool.getUsers(store);
			for ( UserInfo userInfo : userInfoList ){
				// V4.5 Only users can check if it matches an existing user
				UserInfo gsUserInfo = null;
				if ( !userInfo.isRole() ) {
					gsUserInfo = gsUserMap.get(userInfo.getName());
				}
				if ( gsUserInfo != null ){
					// Check if it match
					if ( !userInfo.getHashPassword().equals(gsUserInfo.getHashPassword()) ){
						throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_36")+" userName=["+userInfo.getName()+"]");
					}

				} else {
					if ( userInfo.isRole() ) {
						// V4.5 Creating a role
						try {
							ExperimentalTool.putRole(store, userInfo.getRoleName());
						} catch ( GSException e ){
							throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_3C")
									+" roleName=["+userInfo.getRoleName()+"] msg=["+e.getMessage()+"]", e);
						}
						log.info("create role name=["+userInfo.getRoleName()+"]");
					} else {
						try {
							ExperimentalTool.putUser(store, userInfo.getName(), userInfo, false);
						} catch ( GSException e ){
							throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_3C")
									+" userName=["+userInfo.getName()+"] msg=["+e.getMessage()+"]", e);
						}
						log.info("create user name=["+userInfo.getName()+"]");
					}
				}
			}
			comLineInfo.sysoutString(messageResource.getString("MESS_EXPORT_PROC_EXPORTPROC_21")+userInfoList.size());

			// Database creation / ACL
			Map<String, DatabaseInfo> gsDbMap = ExperimentalTool.getDatabases(store);
			for ( Map.Entry<String, Map<String, PrivilegeInfo>> entry : dbInfoMap.entrySet() ){
				DatabaseInfo gsDbInfo = gsDbMap.get(entry.getKey());
				if ( gsDbInfo != null ){
					// Check if it match
					for ( Map.Entry<String, PrivilegeInfo> entryPri : entry.getValue().entrySet() ){
						if ( gsDbInfo.getPrivileges().get(entryPri.getKey()) == null ){
							throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_37")+" dbName=["+entry.getKey()+"]");
						}
					}

				} else {
					DatabaseInfo dbInfo = new DatabaseInfo(entry.getKey(), entry.getValue());
					try {
						ExperimentalTool.putDatabase(store, entry.getKey(), dbInfo, false);
					} catch ( GSException e ){
						throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_3D")
								+" dbName=["+entry.getKey()+"] msg=["+e.getMessage()+"]", e);
					}
					if ( entry.getValue().size() > 0 ){
						for ( Map.Entry<String, PrivilegeInfo> privilegeEntry : entry.getValue().entrySet() ){
							try {
								ExperimentalTool.putPrivilege(store, entry.getKey(), privilegeEntry.getKey(), privilegeEntry.getValue());
							} catch ( GSException e ){
								throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_3E")
										+" dbName=["+entry.getKey()+"] user=["+entry.getKey()+"] msg=["+e.getMessage()+"]", e);
							}
						}
					}
					log.info("create database name=["+entry.getKey()+"]");
				}
			}
			comLineInfo.sysoutString(messageResource.getString("MESS_EXPORT_PROC_EXPORTPROC_22")+dbInfoMap.size());


		} catch ( GSEIException e ){
			throw e;

		} catch ( Exception e ){
			throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_38")+" msg=["+e.getMessage()+"]", e);

		} finally {
			try {
				if ( store != null ) store.close();
			} catch ( Exception e ){}
		}
	}

	/**
	 * Read the database information from the ACL file.
	 *
	 * @param jp
	 */
	Map<String, Map<String, PrivilegeInfo>> readDatabase(JsonParser jp) throws Exception{

		if (!jp.hasNext()) {
			throw new GridStoreCommandException("'"+ToolConstants.JSON_META_COLUMN_SET+"' is invalid."
					+": line(about)=["+jp.getLocation().getLineNumber()+"]");
		}
		if ( jp.next() != Event.START_ARRAY ){
			throw new GridStoreCommandException("'"+ToolConstants.JSON_META_COLUMN_SET+"' must be array."
					+": line(about)=["+jp.getLocation().getLineNumber()+"]");
		}

		Map<String, Map<String, PrivilegeInfo>> dbList = new HashMap<String, Map<String, PrivilegeInfo>>();
		Map<String, PrivilegeInfo> privilegeList = null;
		String dbName = null;
		String userName = null;
		PrivilegeInfo privilege = null;
		// V4.3 User permission import / database [] / acl [] / role detection flag
		boolean foundAclRole = false;
		Event nextEvent = null;
		String key = "database";
		String obj = key;

		while(jp.hasNext()){
			Event event = jp.next();
			//if ( (nextEvent != null) && (event != nextEvent) ){
			//	throw new Exception();
			//}

			switch(event){
			case KEY_NAME:
				key = jp.getString();
				if ( key.equalsIgnoreCase("name") ){
					nextEvent = Event.VALUE_STRING;
				} else if ( key.equalsIgnoreCase("acl") ){
					nextEvent = Event.START_ARRAY;
				}
				break;
			case VALUE_STRING:
				String value = jp.getString();
				value = value.trim();
				if ( key.equalsIgnoreCase("name") ){
					dbName = value;
					nextEvent = Event.KEY_NAME;
				} else if ( key.equalsIgnoreCase("acl") ){
					nextEvent = Event.END_OBJECT;
				} else if ( key.equalsIgnoreCase("username") ){
					userName = value;
					// V4.3 User permission import
					// privilege = new PrivilegeInfo();	// There is no type of authority in V2.7
					if (privilege == null) {
						privilege = new PrivilegeInfo();
					}
					nextEvent = Event.KEY_NAME;
					// V4.3 User Privilege Import / Read the value of database [] / acl [] / role
				} else if ( key.equalsIgnoreCase("role") ) {
					// V4.3 User permission import / database [] / acl [] / role Turn on detection flag
					foundAclRole = true;
					privilege = new PrivilegeInfo();
					// V4.3 User permission import Set permission information according to the value of / database [] / acl [] / role
					if (value.equalsIgnoreCase("ALL")) {
						privilege.setRole(RoleType.ALL);
					} else if (value.equalsIgnoreCase("READ")) {
						privilege.setRole(RoleType.READ);
					} else {
						// V4.3 Error if role value is other than ALL / READ
						throw new GridStoreCommandException("'"+ "role" +"' is invalid."
								+": line(about)=["+jp.getLocation().getLineNumber()+"]");
					}
				}
				break;
			case START_ARRAY:
				if ( key.equalsIgnoreCase("acl") ){
					privilegeList = new HashMap<String, PrivilegeInfo>();
				}
				obj = key;
				nextEvent = Event.START_OBJECT;
				break;
			case END_ARRAY:
				if (obj.equalsIgnoreCase("acl")){
					obj = "database";
				} else if (obj.equalsIgnoreCase("database")){
					obj = null;
				}
				nextEvent = null;
				break;
			case START_OBJECT:
				nextEvent = Event.KEY_NAME;
				break;
			case END_OBJECT:
				if (obj.equalsIgnoreCase("acl")){
					// V4.3 If user authority import / database [] / acl [] / role does not exist, it will be treated as ALL.
					if ( !foundAclRole ) {
						privilege = new PrivilegeInfo();
						privilege.setRole(RoleType.ALL);
					}
					// V4.3 User permission import / database [] / acl [] / role Initialize detection flag
					foundAclRole = false;

					privilegeList.put(userName, privilege);
					userName = null;
					privilege = null;
				} else if (obj.equalsIgnoreCase("database")){
					dbList.put(dbName, privilegeList);
					dbName = null;
					privilegeList = null;
				}
				nextEvent = null;
				break;
			default:
				break;
			}

			if ( (event == Event.END_ARRAY) && (obj == null) ) break;
		}

		return dbList;
	}

	List<UserInfo> readUser(JsonParser jp) throws Exception{
		List<UserInfo> userInfoList = new ArrayList<UserInfo>();
		UserInfo userInfo = null;
		Event nextEvent = null;
		String key = "";
		// V4.5 isRole value for temporary storage
		boolean isRoleVal = false;

		if (!jp.hasNext()) {
			throw new GridStoreCommandException("'"+ToolConstants.JSON_META_COLUMN_SET+"' is invalid."
					+": line(about)=["+jp.getLocation().getLineNumber()+"]");
		}
		if ( jp.next() != Event.START_ARRAY ){
			throw new GridStoreCommandException("'"+ToolConstants.JSON_META_COLUMN_SET+"' must be array."
					+": line(about)=["+jp.getLocation().getLineNumber()+"]");
		}

		while(jp.hasNext()){
			Event event = jp.next();
			//if ( (nextEvent != null) && (event != nextEvent) ){
			//	throw new Exception();
			//}

			switch(event){
			case KEY_NAME:
				key = jp.getString();
				if ( key.equalsIgnoreCase("username") ){
					nextEvent = Event.VALUE_STRING;
				} else if ( key.equalsIgnoreCase("password") ){
					nextEvent = Event.VALUE_STRING;
				} else if ( key.equalsIgnoreCase("isRole") ){
					// V4.5 nextEvent Is not used in particular, so do nothing
				}
				break;
			case VALUE_STRING:
				String value = jp.getString();
				value = value.trim();
				if ( key.equalsIgnoreCase("username") ){
					// V4.5 Regardless of whether the username indicates
					// the user name or role name, setName () is used here.
					userInfo.setName(value);
					nextEvent = Event.KEY_NAME;
				} else if ( key.equalsIgnoreCase("password") ){
					userInfo.setHashPassword(value);
					nextEvent = Event.END_OBJECT;
				}
				break;
			case VALUE_TRUE:
				if ( key.equalsIgnoreCase("isRole") ){
					// V4.5 isRole The value for temporary storage is true
					isRoleVal = true;
				}
				break;
			case VALUE_FALSE:
				if ( key.equalsIgnoreCase("isRole") ){
					// V4.5 isRole Temporary save value is false
					isRoleVal = false;
				}
				break;
			case START_OBJECT:
				nextEvent = Event.KEY_NAME;
				userInfo = new UserInfo();
				// V4.5 isRole Initialize with false value for temporary storage
				isRoleVal = false;
				break;
			case END_OBJECT:
				nextEvent = null;
				if ( isRoleVal ) {
					// V4.5 If isRole is true, recreate userInfo as role information
					// Since UserInfo uses only UserInfo.roleName when creating a role in the import process,
					// the value set here other than roleName is not very meaningful.
					userInfo = new UserInfo(null, userInfo.getHashPassword(), userInfo.isSuperUser(), userInfo.isGroupMapping(), userInfo.getName());
				}
				userInfoList.add(userInfo);
				break;
			default:
				break;
			}

			if ( event == Event.END_ARRAY ) break;
		}

		return userInfoList;
	}

	/**
	 * Parse the JSON in the view definition file.
	 * <p>
	 * Returns null if no view definition is listed.
	 *
	 * @return List<GSEIViewInfo>
	 * @throws GSEIException
	 */
	private List<GSEIViewInfo> readViewJson(Set<String> dbList) throws GSEIException {
		List<GSEIViewInfo> views = new ArrayList<GSEIViewInfo>();
		try {
			// View definition file read
			File file = new File(comLineInfo.getDirectoryFullPath(), GSConstants.FILE_GS_EXPORT_VIEW_JSON);
			if ( !file.exists() ){
				return null;
			}
			JsonParser jp = Json.createParser(new InputStreamReader(MetaContainerFileIO.skipBOM(new FileInputStream(file)), ToolConstants.ENCODING_JSON));
			if ( !jp.hasNext() ) {
				// D0024D: An unexpected error occurred while reading the view definition file.
				throw new GSEIException("MESS_IMPORT_ERR_IMPORTPROC_4D");
			}
			Event e = jp.next();
			if ( e != Event.START_OBJECT ){
				// D0024D: An unexpected error occurred while reading the view definition file.
				throw new GSEIException("MESS_IMPORT_ERR_IMPORTPROC_4D");
			}
			while(jp.hasNext()){
				Event event = jp.next();
				if ( event == Event.KEY_NAME ){
					String key = jp.getString();
					// Read view array
					if ( key.equalsIgnoreCase(ToolConstants.JSON_VIEW_VIEW) ){
						views = readViewArray(jp, dbList);
					}
				} else if ( event == Event.END_OBJECT ){
					break;
				}
			}
		} catch ( JsonParsingException e ){
			// D0024D: An unexpected error occurred while reading the view definition file.
			throw new GSEIException("MESS_IMPORT_ERR_IMPORTPROC_4D", e);
		} catch ( Exception e ) {
			// D0024D: An unexpected error occurred while reading the view definition file.
			throw new GSEIException("MESS_IMPORT_ERR_IMPORTPROC_4D", e);
		}

		if (views.isEmpty()) {
			// If the file exists but no view definition is listed
			return null;
		} else {
			// Returns the view definition if it exists
			return views;
		}
	}

	/**
	 * Reads the view array in the view definition file and returns it as a list of GSEIViewInfo
	 * <p>
	 * <pre>
	 * [
	 *   {
	 *     "database": "public",
	 *     "name": "myview",
	 *     "definition": "select * from mytable"
	 *   }
	 *               :
	 * ]
	 * </pre>
	 *
	 * @param jp
	 * @return List<GSEIViewInfo>
	 * @throws GSEIException
	 */
	private List<GSEIViewInfo> readViewArray(JsonParser jp, Set<String> dbList) throws GSEIException {
		List<GSEIViewInfo> views = new ArrayList<GSEIViewInfo>();
		String dbName = null;
		String viewName = null;
		String definition = null;
		String key = "";

		if (!jp.hasNext()) {
			throw new GSEIException("'"+ToolConstants.JSON_VIEW_VIEW+"' is invalid."
					+": line(about)=["+jp.getLocation().getLineNumber()+"]");
		}
		if ( jp.next() != Event.START_ARRAY ){
			throw new GSEIException("'"+ToolConstants.JSON_VIEW_VIEW+"' must be array."
					+": line(about)=["+jp.getLocation().getLineNumber()+"]");
		}

		while(jp.hasNext()){
			Event event = jp.next();

			switch(event){
			case KEY_NAME:
				key = jp.getString();
				break;
			case VALUE_STRING:
				String value = jp.getString();
				value = value.trim();
				if ( key.equalsIgnoreCase(ToolConstants.JSON_VIEW_VIEW_DATABASE) ){
					dbName = value;
				} else if ( key.equalsIgnoreCase(ToolConstants.JSON_VIEW_VIEW_NAME) ){
					viewName = value;
				} else if ( key.equalsIgnoreCase(ToolConstants.JSON_VIEW_VIEW_DEFINITION) ){
					definition = value;
				}
				break;
			case START_OBJECT:
				dbName = null;
				viewName = null;
				definition = null;
				break;
			case END_OBJECT:
				// Add if you have all the elements you need for your view definition
				if (dbName != null && viewName != null && definition != null) {
					// Add if the database name is included in the database to be imported
					if (dbList.contains(dbName)) {
						views.add(new GSEIViewInfo(dbName, viewName, definition));
					}
				}
				break;
			default:
				break;
			}

			if ( event == Event.END_ARRAY ) break;
		}

		return views;
	}

	/**
	 * Perform the import process.
	 *
	 * @param dataList Container list
	 * @return Number of containers successfully processed
	 * @throws Exception
	 */
	public int import1(List<GSEIContInfo> dataList, Set<String> skipDbList) {

		String prevDbName = null;
		GridStore store = null;
		Connection conn = null;
		int successCount = 0;
		MetaContainerFileIO metaFileIO = new MetaContainerFileIO();

		if ( comLineInfo.getTestFlag() ){
			return -1;
		}

		//RowSerialize.registerTheradId(Thread.currentThread().getId());
		TargetContainerManager control = TargetContainerManager.getInstance();

		// Container loop
		for ( GSEIContInfo data : dataList ){
			long startTime = System.currentTimeMillis();
			String containerName = data.getContainerName();

			try {
				if ( (skipDbList != null) && (data.getDbName()!=null) && skipDbList.contains(data.getDbName().toLowerCase()) ){
					continue;
				}


				// ---------------------------
				// Reading meta information file
				// ---------------------------
				long startMeta = System.currentTimeMillis();
				ToolContainerInfo contInfo = metaFileIO.readMetaInfo(new File(comLineInfo.getDirectoryPath(), data.getMetaFileName()), containerName, data.getDbName());
				checkContainerInfo(contInfo);
				long endMeta = System.currentTimeMillis();
				if ( m_fileIO == null ){
					// Raw file read object (Since the file output type csv / binray is required,
					// it is executed at this timing when the metafile is read)
					m_fileIO = GSEIFileIOFactory.createFileIO(contInfo.getContainerFileType(), comLineInfo);
					// (V5.2)SimpleDateFormatのフォーマットを変更したため、下位互換を持たせる
					if ((contInfo.getContainerFileType().equals(RowFileType.CSV) || contInfo.getContainerFileType().equals(RowFileType.ARCHIVE_CSV)) 
							&& contInfo.getVersion().compareTo(GSConstants.EXPORT_MNG_FILE_VERSION_3) < 0) {
						m_fileIO.changeDateFormat();
					}
				}

				containerName = contInfo.getFullName();

				// --------------------------------------
				// GridStore connection (reconnect if DB is different)
				// --------------------------------------
				if ( (((prevDbName == null)&&(prevDbName != contInfo.getDbName()))
						|| ((prevDbName != null)&&!prevDbName.equalsIgnoreCase(contInfo.getDbName())))
						|| (store==null) ){
					if ( store != null ) {
						store.close();
						store = null;
					}
					store = gridStoreServerIO.getConnection(comLineInfo, contInfo.getDbName());

					if ( comLineInfo.isJdbcEnabled() ) {
						if (conn != null) {
							conn.close();
							conn = null;
						}
						conn = gridStoreServerIO.getJdbcConnection(comLineInfo, contInfo.getDbName());
						// Get a list of partition table names when connecting to JDBC
						m_partitionTableSet = gridStoreServerIO.getPartitionTableNames(conn);

					}
					prevDbName = contInfo.getDbName();
				}

				// ---------------------------
				// Search container information
				// ---------------------------
				long startSearch = System.currentTimeMillis();
				ContainerInfo cInfo = store.getContainerInfo(contInfo.getName());
				long endSearch = System.currentTimeMillis();

				// ---------------------------
				// Create or get a container
				// ---------------------------
				long startCreate = System.currentTimeMillis();
				Container<?, Row> targetContainer = null;
				if (contInfo.isPartitioned()) {
					// Partition table created via JDBC
					targetContainer = createPartitionTable(conn, store, cInfo, contInfo);
				} else {
					// Regular container created with Java API
					targetContainer = createContainer(store, cInfo, contInfo);
				}
				long endCreate = System.currentTimeMillis();

				// ---------------------------
				// Raw data registration
				// ---------------------------
				int count = importData(targetContainer, contInfo, 0, -1);

				// ---------------------------
				// Indexing
				// ---------------------------
				createIndex(conn, store, cInfo, contInfo, targetContainer);

				// success
				long endTime = System.currentTimeMillis();
				comLineInfo.sysoutString( containerName +" : " + count);
				successCount++;

				log.info("import: db,"+contInfo.getDbName()+",name,"+contInfo.getName()
					+",rowCount,"+count
					+",Time all,"+(endTime-startTime)			// Total processing time
					+",put,"+m_timePut							// Row registration time
					+",readRow,"+m_fileIO.m_timeFileIO			// Raw file read time
					+",readMeta,"+(endMeta-startMeta)			// Metafile loading time
					+",createDrop,"+(endCreate-startCreate)		// Updater creation or deletion time
					+",search,"+(endSearch-startSearch)			// Container search time
					+",other,"+((endTime-startTime)-m_timePut-m_fileIO.m_timeFileIO-(endMeta-startMeta)-(endCreate-startCreate)-(endSearch-startSearch)));

				// Thread stop check
				if ( importProcess.m_stopFlag ){
					break;
				}

			} catch ( GSEIException e ){
				comLineInfo.sysoutString(e.getMessage());
				log.error(e.getMessage(), e);
				commandProgressStatus.setContainerStatus(containerName, false, e.getMessage());
				//status = false;
				if ( !comLineInfo.getForceFlag() ) {
					importProcess.m_stopFlag = true;
					break;
				}

			} catch (GSTimeoutException e){
				// "D00101: An unexpected error occurred while importing data."
				String errMsg = messageResource.getString("MESS_IMPORT_ERR_IMPORTMAIN_1") + ":"+ e.getMessage();
				comLineInfo.sysoutString(errMsg);
				log.error(errMsg, e);
				commandProgressStatus.setContainerStatus(containerName, false, e.getMessage());
				// When the connection times out, terminate without looping
				importProcess.m_stopFlag = true;
				break;

			} catch (Exception e) {
				// "D00101: An unexpected error occurred while importing data."
				String errMsg = messageResource.getString("MESS_IMPORT_ERR_IMPORTMAIN_1") + ":"+ e.getMessage();
				comLineInfo.sysoutString(errMsg);
				log.error(errMsg, e);
				commandProgressStatus.setContainerStatus(containerName, false, e.getMessage());
				//status = false;
				if ( !comLineInfo.getForceFlag() ) {
					importProcess.m_stopFlag = true;
					break;
				}
			}

		} // Loop per container  for(container)


		// Disconnect
		try {
			if ( store != null ) store.close();
			if ( conn != null ) conn.close();
		} catch (Exception e) {}


		return successCount;
	}


	/**
	 * Read the row file and register the row.
	 *
	 * @param store GridStore object
	 * @param container Container object
	 * @param contInfo Local container information object
	 * @return Number of registered rows
	 * @throws GSEIException
	 */
	public int importData(Container<?, Row> container, ToolContainerInfo contInfo, int firstIndex, int lastIndex) throws GSEIException{

		long startMultiPut = 0;
		long endMultiPut = 0;
		m_timePut = 0;
		int rowIndex = 0;
		int addRowCount = 0;

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(GSConstants.DATE_FORMAT_NOT_TIMEZONE);
		final int INTERVAL_UNIT = Calendar.DATE;
		final int INTERVAL_VALUE = 1;
		List<String> containerFileList = new ArrayList<String>();

		try {
			// メタデータファイルにtimeIntervalInfoオブジェクトがあり　かつ　--intervalsが指定された場合
			if ( contInfo.getTimeIntervalInfos() != null && contInfo.getTimeIntervalInfos().size() > 0
					&& comLineInfo.getIntervals() != null && comLineInfo.getIntervals().length > 1) {
				List<String> contFileList = new ArrayList<String>();// import対象のファイル一覧
				Date intervalFrom = comLineInfo.getIntervals()[0];// --intervals 始点
				Date intervalTo = comLineInfo.getIntervals()[1];// --intervals 終点
				for (TimeIntervalInfo timeIntervalInfo : contInfo.getTimeIntervalInfos()) {
					Date from = sdf.parse(timeIntervalInfo.getBoundaryValue());
					cal.setTime(from);
					cal.add(INTERVAL_UNIT, INTERVAL_VALUE);
					Date to = cal.getTime();
					// import対象ファイルの期間の終点 < --intervalsの始点　importしない
					if (to.getTime() < intervalFrom.getTime()) {
						//何もしない intervalsの条件と一致しない						
					}
					// import対象ファイルの期間の始点 > --intervalsの終点　importしない
					else if (from.getTime() > intervalTo.getTime()) {
						//何もしない intervalsの条件と一致しない
					} 
					else {
						// import対象のロウデータファイルとして追加する
						contFileList.add(timeIntervalInfo.getContainerFile());
					}
				}
				
				// intervalsの条件と一致するロウデータファイルが存在しなかった
				if (contFileList == null || contFileList.size() == 0) {
					String warnMsg = messageResource.getString("MESS_IMPORT_PROC_IMPORTPROC_5") 
							+ comLineInfo.getIntervals()[0] + " - " + comLineInfo.getIntervals()[1];
					comLineInfo.sysoutString(warnMsg);
					log.warn(warnMsg);
				}
				
				// 読み込み対象のファイル一覧設定
				containerFileList = contFileList;
			} else {
				// 既存の処理
				if ( contInfo.getContainerFileList() == null ){
					return 0;
				}
				// 読み込み対象のファイル一覧設定
				containerFileList = contInfo.getContainerFileList();
			}

			if (contInfo.getContainerFileType().equals(RowFileType.CSV) || 
					contInfo.getContainerFileType().equals(RowFileType.ARCHIVE_CSV)) {
				// csv形式は元々1ロウデータファイルのみだったため、複数ロウデータファイルに対応
				for (String containerFile:containerFileList) {
					List<String> containerFiles = new ArrayList<String>();
					containerFiles.add(containerFile);
				
					// ロウファイル読み込み開始
					m_fileIO.readContainer(contInfo, containerFiles);
					
					// ROWの読み込みと登録
					int commitCount = comLineInfo.getCommitCount();
					List<Row> rowList = new ArrayList<Row>(commitCount);
					while(m_fileIO.hasNextRow()){
						rowIndex++;
						if ( firstIndex > (rowIndex-1) ) continue;
						if ( (lastIndex!=-1) && lastIndex < rowIndex ) {
							rowIndex--;
							break;
						}

						Row row = m_fileIO.readRow(container);
						rowList.add(row);
						addRowCount++;

						if ( ((rowIndex) % commitCount) == 0 ){
							startMultiPut = System.currentTimeMillis();
							container.put(rowList);
							endMultiPut = System.currentTimeMillis();
							m_timePut += (endMultiPut - startMultiPut);
							rowList = new ArrayList<Row>(commitCount);
						}
					}
					if ( rowList.size() > 0 ){
						startMultiPut = System.currentTimeMillis();
						container.put(rowList);
						endMultiPut = System.currentTimeMillis();
						m_timePut += (endMultiPut - startMultiPut);
					}
			
				}
			} else {
				// ロウファイル読み込み開始
				m_fileIO.readContainer(contInfo, containerFileList);

				// ROWの読み込みと登録
				int commitCount = comLineInfo.getCommitCount();
				List<Row> rowList = new ArrayList<Row>(commitCount);
				while(m_fileIO.hasNextRow()){
					rowIndex++;
					if ( firstIndex > (rowIndex-1) ) continue;
					if ( (lastIndex!=-1) && lastIndex < rowIndex ) {
						rowIndex--;
						break;
					}

					Row row = m_fileIO.readRow(container);
					rowList.add(row);
					addRowCount++;

					if ( ((rowIndex) % commitCount) == 0 ){
						startMultiPut = System.currentTimeMillis();
						container.put(rowList);
						endMultiPut = System.currentTimeMillis();
						m_timePut += (endMultiPut - startMultiPut);
						rowList = new ArrayList<Row>(commitCount);
					}
				}
				if ( rowList.size() > 0 ){
					startMultiPut = System.currentTimeMillis();
					container.put(rowList);
					endMultiPut = System.currentTimeMillis();
					m_timePut += (endMultiPut - startMultiPut);
				}
			}

			return addRowCount;

		} catch ( GSEIException e ){
			if ( rowIndex != 0 ){
				throw new GSEIException(e.getMessage() + " rowNum=["+rowIndex+"]", e);
			} else {
				throw e;
			}
		} catch ( Exception e ){
			// An error occurred during the raw data import process
			throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_33")+": containerName=["+
					contInfo.getContainerInfo().getName() +"] msg=["+e.getMessage()+"]", e);
		}
	}


	/**
	 * If the container does not exist, create it.
	 * If the container exists, the meta information match (append) / container
	 * is recreated (replace) according to the option specification.
	 *
	 * @param store GridStore object
	 * @param cInfo Container information(GridStore)
	 * @param contInfo Container information (Tool)
	 * @return Container to be imported
	 * @throws Exception
	 */
	private Container<?, Row> createContainer(GridStore store, ContainerInfo cInfo, ToolContainerInfo contInfo) throws Exception {
		return createContainer(store, cInfo, contInfo, true, false);
	}
	private Container<?, Row> createContainer(GridStore store, ContainerInfo cInfo, ToolContainerInfo contInfo, boolean jobChainFirst, boolean isSplitContainer) throws Exception {
		Container<?, Row> targetContainer = null;

		if (cInfo == null ) {
			// Create a new container because it does not exist
			//targetContainer = ExperimentalTool.putContainer(store, contInfo.getName(), contInfo.getContainerInfo(), false);
			targetContainer = store.putContainer(contInfo.getName(), contInfo.getContainerInfo(), false);
			// [memo] putContainer gets the type from ContainerInfo, so you don't have to use putCollection / putTimeSeries.

		} else {

			// Even if the option is specified, it should not be continued (case of record number division / column value division.
			// Because you do not know which container it is correct to delete the range)
			if ( isSplitContainer && jobChainFirst ){
				throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_45")+": db=["+contInfo.getDbName()+"] containerName=["+contInfo.getName()+"]");
			}

			// --replace (delete existing container / create new one)
			if ( (jobChainFirst && comLineInfo.getReplaceFlag()) ){
				// Delete container
				//ExperimentalTool.dropContainer(store, contInfo.getName());
				store.dropContainer(contInfo.getName());

				// Create a new container
				//targetContainer = ExperimentalTool.putContainer(store, contInfo.getName(), contInfo.getContainerInfo(), false);
				targetContainer = store.putContainer(contInfo.getName(), contInfo.getContainerInfo(), false);

			// If it is a continuation Jov, proceed as it is
			} else if ( !jobChainFirst ){
				targetContainer = store.getContainer(contInfo.getName());

			// --append (addition / update to existing container)
			} else if (comLineInfo.getAppendFlag()) {
				// Make sure the schema information matches to overwrite.
				// However, if the option to uncheck the schema information is specified, the confirmation is not performed.
				boolean isSchemaCheckSkip = comLineInfo.getSchemaCheckSkipFlag();
				if ( isSchemaCheckSkip == false && comLineInfo.isJdbcEnabled() ){
					if ( m_partitionTableSet != null && m_partitionTableSet.contains(cInfo.getName()) ){
						log.error("[Unmatch] self=[Not Partitioned] another=[Partitioned]");
						throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_10")+": db=["+contInfo.getDbName()+"] containerName=["+contInfo.getName()+"]");
					}
				}
				if ( isSchemaCheckSkip == false && !contInfo.compareContainerInfo(cInfo) ){
					log.error(contInfo.getMessage());
					throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_10")+": db=["+contInfo.getDbName()+"] containerName=["+contInfo.getName()+"]");
				}

				// Get container object
				targetContainer = store.getContainer(contInfo.getName());

			} else { // Other than --append / --replace
				// The specified container already exists. The [--append] or [--replace] parameter is required to perform the operation
				throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_24")
						+": db=["+contInfo.getDbName()+"] containerName=["+contInfo.getName()+"]");
			}
		}

		return targetContainer;
	}

	/**
	 * Create a partition table via JDBC, get it as a container with Java API and return it.
	 * <p>
	 * If the table exists, the meta information match (append) / table is recreated (replace) according to the option specification.
	 * <p>
	 *
	 * @param conn JDBC connection
	 * @param store GridDB connection
	 * @param cInfo Container information obtained from GridDB
	 * @param contInfo Meta information
	 * @return Container
	 * @throws GSEIException
	 */
	private Container<?, Row> createPartitionTable(Connection conn, GridStore store,
			ContainerInfo cInfo, ToolContainerInfo metaInfo) throws GSEIException {

		if ( cInfo == null ) {
			// Create a new table because it does not exist
			createPartitionTableFromMetaInfo(conn, metaInfo);

		} else {
			// --replace (delete existing table / create new)
			if ( comLineInfo.getReplaceFlag() ) {
				// Delete table (partitioning table or container)
				dropTable(conn, metaInfo.getName());

				// Create a new table based on the meta table
				createPartitionTableFromMetaInfo(conn, metaInfo);

			// --append (addition / update to existing container)
			} else if ( comLineInfo.getAppendFlag() ) {
				List<TablePartitionProperty> gsProperties = null;
				ExpirationInfo gsExpInfo = null;
				try {
					gsProperties = GridDBJdbcUtils.getTablePartitionProperties(conn, cInfo.getName());
					gsExpInfo = GridDBJdbcUtils.getExpirationInfo(conn, cInfo.getName());
				} catch (Exception e) {
					throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_4B")+": db=["+metaInfo.getDbName()+"] containerName=["+metaInfo.getName()+"]");
				}
				ToolContainerInfo gsToolInfo = new ToolContainerInfo();
				gsToolInfo.setContainerInfo(cInfo);
				gsToolInfo.setTablePartitionProperties(gsProperties);
				try {
					gsToolInfo.setExpirationInfo(gsExpInfo);
				} catch (GridStoreCommandException e) {
					// Not reached if unexpected information is not returned from the server
					throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_4C")+": db=["+metaInfo.getDbName()+"] containerName=["+metaInfo.getName()+"]", e);
				}

				// Make sure the schema information matches to overwrite.
				// However, if the option to uncheck the schema information is specified, the confirmation is not performed.
				boolean isSchemaCheckSkip = comLineInfo.getSchemaCheckSkipFlag();
				if ( isSchemaCheckSkip == false && !metaInfo.compareContainerInfo(gsToolInfo) ){
					log.error(metaInfo.getMessage());
					throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_10")+": db=["+metaInfo.getDbName()+"] containerName=["+metaInfo.getName()+"]");
				}

			} else { // Other than --append/--replace
				// The specified container already exists. The [--append] or [--replace] parameter is required to perform the operation
				throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_24")
						+": db=["+metaInfo.getDbName()+"] containerName=["+metaInfo.getName()+"]");
			}
		}

		Container<?,Row> cont = null;
		try {
			cont = store.getContainer(metaInfo.getName());
		} catch (Exception e) {
			throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_4A")+":  name=["+metaInfo.getName()+"] msg=["+e.getMessage()+"]", e);
		}

		// Get the created table or the existing table as a container and return it
		return cont;
	}

	/**
	 * (JDBC)Create a partitioning table from the meta information.
	 * @param conn
	 * @param metaInfo
	 * @throws GSEIException
	 */
	private void createPartitionTableFromMetaInfo(Connection conn, ToolContainerInfo metaInfo) throws GSEIException {
		String sql = metaInfo.buildCreateTableStatement();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_48")+":  sql=["+sql+"] msg=["+e.getMessage()+"]", e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {}
		}
	}

	/**
	 * (JDBC)Drops the specified table.
	 * @param conn
	 * @param name
	 * @throws GSEIException
	 */
	private void dropTable(Connection conn, String name) throws GSEIException {
		PreparedStatement pstmt = null;
		try {
			// Use String.format because the table name cannot be specified in PreparedStatement
			pstmt = conn.prepareStatement(String.format(ToolConstants.PSTMT_DROP_TABLE, name));
			pstmt.executeUpdate();
		} catch (Exception e) {
			throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_49")+": name=[" + name + "] msg=["+e.getMessage()+"]", e);
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {}
		}
	}


	/**
	 * Create an index.
	 *   ・Created by Native API
	 *   ・Created by SQL
	 *
	 * @param conn
	 * @param gridstore
	 * @param contInfo
	 * @param toolInfo
	 */
	private void createIndex(Connection conn, GridStore gridstore, ContainerInfo contInfo, ToolContainerInfo toolInfo, Container<?, Row> targetContainer) throws Exception{
		if (toolInfo.isPartitioned()){
			// Indexing in SQL
			// [memo] In the case of SQL, an error will occur if the index has already been created.
			//        It is necessary to check if the index is already attached in case of additional registration with the --append option.
			Map<IndexInfo, String> sqlMap = toolInfo.buildCreateIndexStatements();
			List<IndexInfo> gsIndexList = new ArrayList<IndexInfo>();
			if ( comLineInfo.getAppendFlag() && ( contInfo != null )){
				gsIndexList = contInfo.getIndexInfoList();
			}

			Statement stmt = conn.createStatement();
			try {
				for(Map.Entry<IndexInfo, String> e : sqlMap.entrySet()) {
					boolean alreadyExists = false;
					for ( IndexInfo gsIndexInfo : gsIndexList ){
						IndexInfo indexInfo = e.getKey();
						// Check if index type, column and name match
						// V4.3 Do not get information with getColumnName () Get information with getColumnNameList ()
						//if ( indexInfo.getType()==gsIndexInfo.getType() && gsIndexInfo.getColumnName().equals(indexInfo.getColumnName())
						if ( indexInfo.getType()==gsIndexInfo.getType() && gsIndexInfo.getColumnNameList().equals(indexInfo.getColumnNameList())
								&& indexInfo.getName().equals(gsIndexInfo.getName())){
							// Skip because it has already been created
							alreadyExists = true;
							break;
						}
					}
					if ( alreadyExists ){
						break;
					}
					stmt.executeUpdate(e.getValue());
				}
			} finally {
				stmt.close();
			}

		} else {
			// Indexing with Native API
			for (IndexInfo index : toolInfo.getIndexInfoList()) {
				targetContainer.createIndex(index);
			}
		}
	}


	/**
	 * Performs the import process of the view to be imported.
	 *
	 * @return Number of successful views
	 */
	private int importViews(List<GSEIViewInfo> views, Set<String> skipDbList) throws GSEIException {
		int viewCount = 0;

		// Import process
		GridStore store = null;
		Connection conn = null;
		String prevDbName = null;
		long startTime = System.currentTimeMillis();
		// View loop
		for ( GSEIViewInfo view : views ){
			String dbName = view.getDbName();
			String viewName = view.getViewName();

			// If the database has not been created, skip it as an import target
			if ( (skipDbList != null) && (dbName!=null) && skipDbList.contains(dbName.toLowerCase()) ){
				continue;
			}

			try {
				// --------------------------------------
				// GridStore connection (reconnect if DB is different)
				// --------------------------------------
				if ( (((prevDbName == null)&&(prevDbName != dbName))
						|| ((prevDbName != null)&&!prevDbName.equalsIgnoreCase(dbName)))
						|| (store==null) ){
					if ( store != null ) {
						store.close();
						store = null;
					}
					store = gridStoreServerIO.getConnection(comLineInfo, dbName);

					if ( comLineInfo.isJdbcEnabled() ) {
						if (conn != null) {
							conn.close();
							conn = null;
						}
						conn = gridStoreServerIO.getJdbcConnection(comLineInfo, dbName);
					}
					prevDbName = dbName;
				}

				// ---------------------------
				// Search container information
				// ---------------------------
				if (store.getContainerInfo(viewName) != null) {
					// D0024E: The container with same name has already existed. Importing of this view is skipped.
					comLineInfo.sysoutString(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_4E") + ": dbName=[" + dbName + "], name=[" + viewName + "]");
					continue;
				}

				// ---------------------------
				// Create or replace view
				// ---------------------------
				int successCount = createOrReplaceView(conn, view);
				viewCount += successCount;

				// Thread stop check
				if ( importProcess.m_stopFlag ){
					break;
				}
			} catch (GSEIException e) {
				comLineInfo.sysoutString(e.getMessage());
				log.error(e.getMessage(), e);
				if ( !comLineInfo.getForceFlag() ) {
					importProcess.m_stopFlag = true;
					break;
				}
			} catch (GSTimeoutException e) {
				// "D00101: An unexpected error occurred while importing data."
				String errMsg = messageResource.getString("MESS_IMPORT_ERR_IMPORTMAIN_1") + ":"+ e.getMessage();
				comLineInfo.sysoutString(errMsg);
				log.error(errMsg, e);

				// When the connection times out, terminate without looping
				importProcess.m_stopFlag = true;
				break;
			} catch (Exception e) {
				// "D00101: An unexpected error occurred while importing data."
				String errMsg = messageResource.getString("MESS_IMPORT_ERR_IMPORTMAIN_1") + ":"+ e.getMessage();
				comLineInfo.sysoutString(errMsg);
				log.error(errMsg, e);

				if ( !comLineInfo.getForceFlag() ) {
					importProcess.m_stopFlag = true;
					break;
				}
			}
		} // View loop
		long endTime = System.currentTimeMillis();

		// Output processing time to log
		log.info("import: importViews," + (endTime - startTime));

		// Disconnect
		try {
			if ( store != null ) store.close();
			if ( conn != null ) conn.close();
		} catch (Exception e) {}


		// Returns the number
		return viewCount;
	}

	/**
	 * Creates the specified view.
	 * <p>
	 * The operation is as follows.
	 * <ul>
	 * <li>Same name container exists: Skip import process</li>
	 * <li>View with the same name exists: Error, continue processing with forced option</li>
	 * </ul>
	 * <p>
	 * In replace mode, the operation is as follows.
	 * <ul>
	 * <li>Same name container exists: Skip import process</li>
	 * <li>View with same name exists: Replace operation</li>
	 * </ul>
	 * @param conn
	 * @param view
	 * @return
	 * @throws GSEIException
	 */
	private int createOrReplaceView(Connection conn, GSEIViewInfo view) throws GSEIException {
		String dbName = view.getDbName();
		String name = view.getViewName();
		try {
			GSEIViewInfo exists = getView(conn, name);
			if (exists != null) {
				// --replace Delete existing view
				if (comLineInfo.getReplaceFlag()) {
					dropView(conn, name);
				} else {
					if (comLineInfo.getForceFlag()) {
						// D0024F: The view with same name has already existed. Importing of this view is skipped.
						comLineInfo.sysoutString(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_4F") + ": dbName=[" + dbName + "], name=[" + name + "]");
						return 0;
					} else {
						// D00250: The view with same name has already existed.
						throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_50"));
					}
				}
			}

			// Create a view
			createView(conn, view);
		} catch (GSEIException e) {
			throw e;
		} catch (Exception e) {
			// D00251: An unexpected error occurred while creating or replacing view.
			throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_51"), e);
		}

		return 1;
	}


	/**
	 * (JDBC)#views Gets the view definition with the specified name from the metatable.
	 * <p>
	 * Returns null if it does not exist.
	 *
	 * @param conn
	 * @param name
	 * @return GSEIViewInfo
	 * @throws Exception
	 */
	private GSEIViewInfo getView(Connection conn, String name) throws GSEIException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		GSEIViewInfo view = null;
		try {
			// Execute query SELECT * FROM "#views" WHERE TABLE_NAME=?
			pstmt = conn.prepareStatement(String.format(ToolConstants.PSTMT_SELECT_META_VIEWS_WITH_TABLE, name));
			rs = pstmt.executeQuery();
			while (rs.next()) {
				view = new GSEIViewInfo(
						rs.getString(ToolConstants.META_VIEWS_DATABASE_NAME),
						rs.getString(ToolConstants.META_VIEWS_VIEW_NAME),
						rs.getString(ToolConstants.META_VIEWS_VIEW_DEFINITION)
					);
			}
		} catch (Exception e) {
			// D00252: An unexpected error occurred while getting view.
			throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_52") + ": name=[" + name + "]", e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {};
		}
		return view;
	}

	/**
	 * (JDBC)Deletes the specified view.
	 * @param conn
	 * @param name
	 * @throws GSEIException
	 */
	private void dropView(Connection conn, String name) throws GSEIException {
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(String.format(ToolConstants.PSTMT_DROP_VIEW, name));
			pstmt.executeUpdate();
		} catch (Exception e) {
			// D00253: An unexpected error occurred while dropping view.
			throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_53") + ": name=[" + name + "]");
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {};
		}
	}

	/**
	 * (JDBC)Creates the specified view.
	 * @param conn
	 * @param view
	 * @throws GSEIException
	 */
	private void createView(Connection conn, GSEIViewInfo view) throws GSEIException {
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(
						String.format(
							ToolConstants.PSTMT_CREATE_VIEW,
							view.getViewName(),
							view.getDefinition()
						)
					);
			pstmt.executeUpdate();
		} catch (Exception e) {
			// D00254: An unexpected error occurred while creating view.
			throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_54") + ": name=[" + view.getViewName() + "]");
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {};
		}
	}

	/**
	 * Check whether the view can be referenced.
	 *
	 * @param views
	 * @param skipDbList
	 * @return
	 * @throws GSEIException
	 */
	private int checkViews(List<GSEIViewInfo> views, Set<String> skipDbList) throws GSEIException {
		int viewCount = 0;

		Connection conn = null;
		String prevDbName = null;
		long startTime = System.currentTimeMillis();
		// View loop
		for ( GSEIViewInfo view : views ){
			String dbName = view.getDbName();
			String viewName = view.getViewName();

			// If the database has not been created, skip it as an import target
			if ( (skipDbList != null) && (dbName!=null) && skipDbList.contains(dbName.toLowerCase()) ){
				continue;
			}

			try {
				// --------------------------------------
				// GridStore connection (reconnect if DB is different)
				// --------------------------------------
				if ( (((prevDbName == null)&&(prevDbName != dbName))
						|| ((prevDbName != null)&&!prevDbName.equalsIgnoreCase(dbName))) ){

					if ( comLineInfo.isJdbcEnabled() ) {
						if (conn != null) {
							conn.close();
							conn = null;
						}
						conn = gridStoreServerIO.getJdbcConnection(comLineInfo, dbName);
					}
					prevDbName = dbName;
				}

				// ---------------------------
				// View reference availability check
				// ---------------------------
				try {
					checkView(conn, viewName);
				} catch (Exception e) {
					String msg = "dbName=" + dbName + ", viewName=" + viewName + ", msg=" + e.getMessage();
					commandProgressStatus.addInvalidViewMessage(msg);
					viewCount++;
				}

				// Thread stop check
				if ( importProcess.m_stopFlag ){
					break;
				}
			} catch (GSEIException e) {
				comLineInfo.sysoutString(e.getMessage());
				log.error(e.getMessage(), e);
				break;
			} catch (Exception e) {
				comLineInfo.sysoutString(e.getMessage());
				log.error(e.getMessage(), e);
				break;
			}
		} // View loop
		long endTime = System.currentTimeMillis();

		// Output processing time to log
		log.info("import: checkViews," + (endTime - startTime));

		// Returns the number
		return viewCount;
	}

	/**
	 * (JDBC)Make sure the specified view is visible.
	 * @param conn
	 * @param viewName
	 * @return boolean
	 * @throws Exception
	 */
	private void checkView(Connection conn, String viewName) throws Exception {
		PreparedStatement pstmt = null;
		try {
			// Execute query SELECT * FROM CREATE VIEW ? AS ?
			pstmt = conn.prepareStatement(
						String.format(
								ToolConstants.PSTMT_CHECK_VIEW,
								viewName
						)
					);
			pstmt.executeQuery();
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {};
		}
	}

	/**
	 * List list display
	 *
	 * @param map MAP of container information, meta information file name set
	 */
	private void listFiles(Map<String, List<GSEIContInfo>> map) {
		try {
			comLineInfo.sysoutString(String.format("%-10s %-20s %-12s %-25s","DB", "Name", "Type", "FileName" ));
			MetaContainerFileIO mio = new MetaContainerFileIO();

			for (Map.Entry<String, List<GSEIContInfo>> entry : map.entrySet()) {
				for ( GSEIContInfo data : entry.getValue() ){

					// Reading meta information file
					File metaFile = new File(comLineInfo.getDirectoryPath(), data.getMetaFileName());
					ToolContainerInfo contInfo = mio.readMetaInfo(metaFile, data.getContainerName(), data.getDbName());
					checkContainerInfo(contInfo);
					// DB ContName ContType RowFileName
					comLineInfo.sysoutString(String.format("%-10s %-20s %-12s %-25s",
							(data.getDbName()==null?ToolConstants.PUBLIC_DB:data.getDbName()),
							contInfo.getContainerInfo().getName().toString(),
							(contInfo.getType().toString()),
							(contInfo.getContainerFile()==null?"-":contInfo.getContainerFile())
							));
				}
			}

		} catch (Exception e) {
			//List<String> file = comLineInfo.getFileName();
			// An error occurred during the container information list acquisition process
			comLineInfo.sysoutString(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_22")+ ":"+ e.getMessage());
			log.error(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_22")+ ": msg=["+ e.getMessage()
					+"]", e );
		}
		return;
	}

	/**
	 * Check the meta definition file for Export / Import.
	 *
	 */
	private void checkContainerInfo(ToolContainerInfo info) throws Exception{

		RowFileType containerFileType = info.getContainerFileType();
		List<String> containerFileList = info.getContainerFileList();
		if ( containerFileType == null ){
			throw new GSEIException(Utility.getResource().getString("MESS_IMPORT_ERR_IMPORTPROC_46")+ ": db=["+info.getDbName()+"] containerName=["+info.getName()+"] msg=['"+ToolConstants.JSON_META_CONTAINER_FILE_TYPE+"' is required.]");
		}
		//if ( (containerFileList==null) || containerFileList.isEmpty() ){
		//	throw new GSEIException(Utility.getResource().getString("MESS_IMPORT_ERR_IMPORTPROC_46")+ ": containerName=["+info.getFullName()+"] msg=['"+ToolConstants.JSON_META_CONTAINER_FILE+"' is required.]");
		//}
		// File extension check (for Exp / Imp tool)
		if ( containerFileType.equals(ToolConstants.RowFileType.BINARY)){
			// The extension is not particularly checked.
		} else if (containerFileType.equals(ToolConstants.RowFileType.BINARY)){
			String ext = null;
			for (String fileName : containerFileList ){
				if ( (ext != null) && !fileName.toLowerCase().endsWith(ext) ){
					// File list extensions do not match
					throw new GSEIException(Utility.getResource().getString("MESS_IMPORT_ERR_IMPORTPROC_46")+ ": db=["+info.getDbName()+"] containerName=["+info.getName()+"] msg=['"+ToolConstants.JSON_META_CONTAINER_FILE+"' is required.]");
				}
				if ( fileName.toLowerCase().endsWith(GSConstants.FILE_EXT_BINARY_MULTI) ){
					ext = GSConstants.FILE_EXT_BINARY_MULTI;
				} else if (fileName.toLowerCase().endsWith(GSConstants.FILE_EXT_BINARY_SINGLE) ){
					ext = GSConstants.FILE_EXT_BINARY_SINGLE;
				} else {
					// The file extension is incorrect
					throw new GSEIException(Utility.getResource().getString("MESS_IMPORT_ERR_IMPORTPROC_46")+ ": db=["+info.getDbName()+"] containerName=["+info.getName()+"] msg=['"+ToolConstants.JSON_META_CONTAINER_FILE+"' is required.]");
				}
			}
		}


	}


}

/**
 * Thread class for Import processing.
 *
 */
class ImportThread extends Thread {

	/**
	 * Thread number
	 */
	int m_threadNo;

	/**
	 * Setting information object
	 */
	commandLineInfo m_operationInfo;

	/**
	 * Container information to be imported
	 */
	private List<GSEIContInfo> m_containerList;

	/**
	 * Number of containers successfully processed
	 */
	private int m_successCount;

	/**
	 * List of Dbs to skip processing
	 */
	private Set<String> m_skipDbList;

	/**
	 * Constructor
	 *
	 * @param threadNo Thread number
	 * @param opertationInfo Setting information object
	 */
	ImportThread(int threadNo, commandLineInfo opertationInfo){
		m_threadNo = threadNo;
		m_operationInfo = opertationInfo;
	}


	/**
	 * Returns the thread number
	 * 。
	 * @return Thread number
	 */
	public int getThreadNo(){
		return m_threadNo;
	}

	/**
	 * Returns the number of successful containers.
	 *
	 * @return Number of successful containers
	 */
	public int getSuccessCount(){
		return m_successCount;
	}
	/**
	 * Set the container information to be imported.
	 *
	 * @param containerList Container information list
	 */
	public void setContainerList(List<GSEIContInfo> containerList, Set<String> skipDbList){
		m_containerList = containerList;
		m_skipDbList = skipDbList;
	}

	/**
	 * Import processing is performed in the thread.
	 */
	public void run(){

		importProcess proc = new importProcess(m_operationInfo);
		m_successCount = proc.import1(m_containerList, m_skipDbList);

	}
}
