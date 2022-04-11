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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toshiba.mwcloud.gs.ContainerInfo;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GSTimeoutException;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.PartitionController;
import com.toshiba.mwcloud.gs.experimental.DatabaseInfo;
import com.toshiba.mwcloud.gs.experimental.ExperimentalTool;
import com.toshiba.mwcloud.gs.tools.common.data.MetaContainerFileIO;
import com.toshiba.mwcloud.gs.tools.common.data.ToolConstants;
import com.toshiba.mwcloud.gs.tools.common.data.ToolConstants.RowFileType;
import com.toshiba.mwcloud.gs.tools.common.data.ToolContainerInfo;
import com.toshiba.mwcloud.gs.tools.expimp.GSConstants.TARGET_TYPE;
import com.toshiba.mwcloud.gs.tools.expimp.util.Utility;

public class TargetContainerManager {

	/** Instance of this class */
	private static final TargetContainerManager instance = new TargetContainerManager();

	/**
	 * Internationalized message resource
	 */
	private static ResourceBundle messageResource;

	/**
	 * Logger class settings
	 */
	private static final Logger log = LoggerFactory.getLogger(TargetContainerManager.class);

	//==============================================
	//  For Export
	//==============================================
	/** Container list  ( Node name - DbPartition[Database name, partition ID, container group] ) */
	private Map<String, List<DbPartition>> m_containerMap;

	/** Assigning nodes per thread  ( Thread ID-node name ) */
	private Map<Long, String> m_threadAssignmentMap;

	/** Iterator for assigning nodes to each thread */
	private Iterator<String> m_nodeIterator;

	/** Iterator list showing the progress of processing ( Node name - DbPartitionのIterator )
	 *   (Indicates to which DbPartition the process has progressed in m_containerMap.
	 *   If null, the process for the node's container has been completed）
	 */
	private Map<String, Iterator<DbPartition>> m_progressMap;


	//==============================================
	//  For Import
	//==============================================
	/**
	 * Import processing target container information list
	 */
	private Map<String, List<GSEIContInfo>> m_nameFileMap;

	/**
	 * Database name to be imported
	 */
	private Set<String> m_databaseList;

	/**
	 * Returns the instance. (Singleton)
	 * @return instance
	 */
	public static TargetContainerManager getInstance(){
		return instance;
	}

	/**
	 * Constructor
	 */
	private TargetContainerManager(){
		messageResource = Utility.getResource();
	}

	/**
	 * Create an array for threads.
	 * @param nThreads Degree of parallelism
	 */
	public void setThreadNum(int nThreads){
		m_threadAssignmentMap = new HashMap<Long, String>(nThreads, 1.0F);
		m_containerMap = null;
		m_nodeIterator = null;
		m_progressMap = null;
	}


	/**
	 * Register the thread ID.
	 *
	 * @param threadId Thread ID
	 */
	public synchronized void registThread(long threadId){
		if ( m_nodeIterator == null || !m_nodeIterator.hasNext() ){
			m_nodeIterator = m_containerMap.keySet().iterator();
		}
		String node = m_nodeIterator.next();
		m_threadAssignmentMap.put(threadId, node);
	}



	/**
	 * Returns a container list.
	 * At the time of export, It is executed from each thread.
	 *
	 * @return
	 */
	public DbPartition getContainerList(){
		long threadId = Thread.currentThread().getId();
		String node = m_threadAssignmentMap.get(threadId);

		synchronized (m_containerMap.get(node)) {
			Iterator<DbPartition> itr = m_progressMap.get(node);
			if ( (itr == null) || !itr.hasNext() ){
				// Since it is gone, move to the next node
				m_progressMap.put(node, null);
			} else {
				return itr.next();
			}
		}

		for ( String nodeName : m_progressMap.keySet() ){
			synchronized (m_containerMap.get(nodeName)) {
				Iterator<DbPartition> itr = m_progressMap.get(nodeName);
				if ( (itr == null) || !itr.hasNext() ){
					// Since it is gone, move to the next node
					m_progressMap.put(nodeName, null);
				} else {
					m_threadAssignmentMap.put(threadId, nodeName);
					return itr.next();
				}
			}
		}
		return null;
	}


	/**
	 * Dump the container list. (For debug)
	 */
	public void dumpContainerList(){
		for (Map.Entry<String, List<DbPartition>> nodeEntry : m_containerMap.entrySet()){
			System.out.println("["+nodeEntry.getKey()+"]");

			for ( DbPartition dbPartition : nodeEntry.getValue() ){
				System.out.print("\t-["+dbPartition.getDbName()+"]");
				System.out.print("-["+dbPartition.getPartitionId()+"]:");

				for ( String contName : dbPartition.getContainerList() ){
					System.out.print(contName+",");
				}
				System.out.println();
			}
		}
	}

	/**
	 * Returns the number of containers in the container list.
	 * @return
	 */
	public int getContainerCount(){
		int count = 0;
		for (Map.Entry<String, List<DbPartition>> nodeEntry : m_containerMap.entrySet()){
			for ( DbPartition dbPartition : nodeEntry.getValue() ){
				count += dbPartition.getContainerList().size();
			}
		}
		return count;
	}


	/**
	 * Select the container to be imported.
	 *
	 * @param comLineInfo
	 * @return
	 * @throws GSEIException
	 */
	public Map<String, List<GSEIContInfo>> selectImportContainerList(commandLineInfo comLineInfo) throws GSEIException {
		// (Create a list of container name, meta information file)

		JsonReader jr = null;
		String currentFile = "";
		List<GSEIContInfo> metaMap = null;
		try {
			// No -f option
			if ( comLineInfo.getFileNameList() == null ){
				// Export Import the management file (gs_export.json)
				metaInformationFileIO mio = new metaInformationFileIO(comLineInfo);
				metaMap = mio.readExportManagerFile();
				m_databaseList = mio.getDatabaseList();

			} else {
				metaMap = new ArrayList<GSEIContInfo>();
				m_databaseList = new HashSet<String>();

				// Read the specified file
				for ( String fileName : comLineInfo.getFileNameList() ){
					// Read meta information file
					File file = new File(comLineInfo.getDirectoryPath(), fileName);
					currentFile = file.getCanonicalPath();
					jr = Json.createReader(new InputStreamReader(MetaContainerFileIO.skipBOM(new FileInputStream(file)), ToolConstants.ENCODING_JSON));

					// Get Json object
					JsonStructure st = jr.read();
					JsonObject[] jo = null;
					if ( st.getValueType() == JsonValue.ValueType.ARRAY ){
						// For arrays              (Multi-container format)
						JsonArray ja = (JsonArray)st;
						jo = ja.toArray(new JsonObject[0]);
					} else {
						// For objects   (Single container format)
						jo = new JsonObject[1];
						jo[0] = (JsonObject)st;
					}

					for (JsonObject obj : jo) {
						// Get container information
						String name = null;
						String dbName = ToolConstants.PUBLIC_DB;

						// V3.5 or later The default container attribute is SINGLE
						// For compatibility with V3.2 or earlier files, read as a character string and branch the process.
						String attribute = ToolConstants.EXP_TOOL_ATTRIBUTE_SINGLE;

						for (Entry<String, JsonValue> entry : obj.entrySet()) {
							if (entry.getKey().equalsIgnoreCase("container")) {
								name = entry.getValue().toString().trim();
								if ( name == null || name.isEmpty() ){
									// The container name value is empty in the meta information file
									throw new GSEIException(messageResource.getString("MESS_COMM_ERR_METAINFO_34")
											+": path=["+currentFile+"]");
								}

							} else if ( entry.getKey().equalsIgnoreCase(ToolConstants.JSON_META_DBNAME)){
								dbName = entry.getValue().toString().trim();

							} else if ( entry.getKey().equalsIgnoreCase(ToolConstants.JSON_META_CONTAINER_ATTRIBUTE)){
								String jsonAttr = entry.getValue().toString();

								if (jsonAttr != null) {
									attribute = jsonAttr;
								}
							}
						}
						GSEIContInfo info = new GSEIContInfo(dbName, name, fileName);
						if ( metaMap.contains(info) ){
							// The meta information file contains information with the same container name
							throw new GSEIException(messageResource.getString("MESS_COMM_ERR_METAINFO_35")
									+": containerName=["+name+"] path=["+currentFile+"]");
						}

						// Compatibility with V3.2 and earlier files
						// Partition containers exported in V3.5 V2.9 or earlier are not supported
						if (attribute.equalsIgnoreCase(ToolConstants.EXP_TOOL_ATTRIBUTE_LARGE)) {
							// D00946: The partitioned table exported in format before V4 will be skipped.
							String skipMsg = messageResource.getString("MESS_COMM_ERR_METAINFO_46") +" db=["+dbName+"] container=["+name+"]";
							comLineInfo.sysoutString(skipMsg);
							log.warn(skipMsg);
							commandProgressStatus.addSkipCount(1);
							commandProgressStatus.addSkipMessage(skipMsg);
							continue;
						} else if (attribute.equalsIgnoreCase(ToolConstants.EXP_TOOL_ATTRIBUTE_SUB) || attribute.equalsIgnoreCase(ToolConstants.EXP_TOOL_ATTRIBUTE_SINGLE_SYSTEM)) {
							// Skip system tables (index tables) exported in V3.5 V3.2 or earlier and SUBs exported in V2.9 or earlier
							// [Design Note] Since these are containers that are not shown to the user, no warning is displayed.
							continue;
						}


						metaMap.add(info);
						m_databaseList.add(dbName);
						dbName = null;
					}
				} // for (fileName)
			}

			//---------------------------------------------------------
			// Narrow down according to the designation
			//---------------------------------------------------------
			// When specifying a database name
			Set<GSEIContInfo> resultList = null;

			if ( comLineInfo.getTargetType() == TARGET_TYPE.ALL){
				resultList = new LinkedHashSet<GSEIContInfo>(metaMap);

			} else if ( comLineInfo.getTargetType() == TARGET_TYPE.DB ){
				resultList = new LinkedHashSet<GSEIContInfo>();

				for ( String dbName : comLineInfo.getDbNamelist() ){
					for ( GSEIContInfo info : metaMap ){
						if ( ((dbName==null)&&(info.getDbName()==null)) || (dbName!=null)&&(dbName.equalsIgnoreCase(info.getDbName()))){
							resultList.add(info);
						}
					}
				}

				m_databaseList = new HashSet<String>(comLineInfo.getDbNamelist());

			} else {
				resultList = new LinkedHashSet<GSEIContInfo>();
				m_databaseList = new HashSet<String>();
				String prefixDbName = (comLineInfo.getDbNamelist()==null ? null:comLineInfo.getDbNamelist().get(0));

				// When specifying the container name
				if ( comLineInfo.getContainerNameList() != null ){

					for ( String name : comLineInfo.getContainerNameList() ){
						//String[] tmp = name.split("\\"+ToolConstants.DB_DELIMITER);
						String dbName = prefixDbName;
						//if ( tmp.length == 2 ){
						//	dbName = tmp[0];
						//	name = tmp[1];
						//}
						//if ( (dbName != null) && dbName.equalsIgnoreCase(ToolConstants.PUBLIC_DB) ){
						//	dbName = null;
						//}
						m_databaseList.add(dbName);

						GSEIContInfo info = new GSEIContInfo(dbName, name, null);
						int index = metaMap.indexOf(info);
						if ( index < 0 ){
							// "The specified container is not included in the file."
							String errMsg = messageResource.getString("MESS_COMM_ERR_METAINFO_36")+": db=["+info.getDbName()+"] containerName=["+info.getContainerName()+"]";
							throw new GSEIException(errMsg);
						}
						GSEIContInfo targetInfo = metaMap.get(index);

						resultList.add(targetInfo);
					}
				}

				// When specifying a regular expression
				if ( comLineInfo.getRegexContainerNameList() != null ){
					for ( String regex : comLineInfo.getRegexContainerNameList()){
						/*
						String[] tmp = regex.split("\\\\"+ToolConstants.DB_DELIMITER);//正規表現の場合.はエスケープされている
						String dbName = prefixDbName;
						if ( tmp.length == 1 ){
							regex = (dbName==null ?
										"$[^\\.]*"+regex : dbName+"\\"+ToolConstants.DB_DELIMITER+regex);
						} else {
							dbName = tmp[0];
						}
						if ( (dbName != null) && dbName.equalsIgnoreCase(ToolConstants.PUBLIC_DB) ){
							dbName = null;
						}
						*/

						Pattern singleMatch = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
						for ( GSEIContInfo info : metaMap ) {
							if ( (prefixDbName == info.getDbName()) ||
									((prefixDbName!=null) && prefixDbName.equalsIgnoreCase(info.getDbName()))){
								Matcher m1 = singleMatch.matcher(info.getContainerName());
								if (m1.find()) {
									resultList.add(info);
									m_databaseList.add(info.getDbName());
								}
							}
						}
					}


				}
			}

			//---------------------------------------------------------
			// Version check
			//---------------------------------------------------------
			// Read only one meta information file and check the version.
			MetaContainerFileIO mio = new MetaContainerFileIO();
			for ( GSEIContInfo info : metaMap ){
				ToolContainerInfo tInfo = null;
				try {
					File file = new File(comLineInfo.getDirectoryFullPath(), info.getMetaFileName() );
					tInfo = mio.readMetaInfo(file, info.getContainerName(), info.getDbName());
				} catch ( Exception e ){
					// Ignore the error in this case. You can read any one container.
					continue;
				}
				String version = tInfo.getVersion();
				RowFileType type = tInfo.getContainerFileType();
				if ( (type == RowFileType.BINARY) && (version.compareToIgnoreCase(GSConstants.EXPORT_MNG_FILE_VERSION_1) < 0) ){
					throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_40"));
				}
				break;
			}

			//---------------------------------------------------------
			// Divide the list according to the degree of parallelism
			//---------------------------------------------------------
			//  Map (key, Container list)
			//
			//    In parallel
			//       Divide the container list according to the degree of parallelism using the metafile name as a key.
			//        (If exported in parallel, the meta information file is output as many as the number of parallel degrees)
			//    When not in parallel
			//       Create one container list using an empty string as a key

			m_nameFileMap = new HashMap<String, List<GSEIContInfo>>();
			if ( comLineInfo.getParallelCount() == 1 ){
				m_nameFileMap.put("", new ArrayList<GSEIContInfo>(resultList));

			} else {
				for ( GSEIContInfo info : resultList ){
					String metaName = info.getMetaFileName();
					List<GSEIContInfo> list = m_nameFileMap.get(metaName);
					// Split by metafile name
					if ( list == null ){
						list = new ArrayList<GSEIContInfo>();
						m_nameFileMap.put(metaName, list);
					}
					list.add(info);
				}
			}

			return m_nameFileMap;

		} catch ( GSEIException e ){
			throw e;
		} catch ( Exception e ){
			// An error occurred while reading the meta information file
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_METAINFO_37")
					+": msg=["+e.getMessage()+"] metaFile=["+currentFile+"]", e);
		}

	}

	/**
	 * Select the container to be exported.
	 *
	 * @param operationInfo
	 * @throws GSEIException
	 */
	public void selectExportTargetContainer(commandLineInfo operationInfo) throws GSEIException{
		try {
			//----------------------------------------------------------------
			// (1) Acquisition of target database name list
			//----------------------------------------------------------------
			List<String> dbNameList = null;

			if ( operationInfo.getTargetType() == TARGET_TYPE.ALL ){
				// When --all is specified => Get DB name list from server
				GridStore store = gridStoreServerIO.getConnection(operationInfo);
				Map<String, DatabaseInfo> dbInfoList = ExperimentalTool.getDatabases(store);
				dbNameList = new ArrayList<String>();
				dbNameList.add(ToolConstants.PUBLIC_DB);
				for ( String dbName : dbInfoList.keySet() ){
					dbNameList.add(dbName);
				}
				store.close();

				// 4.2- Hold the target database list. Used for view export.
				operationInfo.setDbNameList(dbNameList);
			} else {
				// For --db, --container, --container regex
				dbNameList = operationInfo.getDbNamelist();
			}

			//----------------------------------------------------------------
			// (2) Obtaining the target container name from each database
			//----------------------------------------------------------------
			m_containerMap = new HashMap<String, List<DbPartition>>();
			Set<String> containerMap = new HashSet<String>();// A set of container names for duplicate checking

			// If a container name is specified, search by container name
			if ( operationInfo.getTargetType()==TARGET_TYPE.CONTAINER_NAME ){
				if ( operationInfo.getContainerNameList() != null ){
					String dbName = dbNameList==null? ToolConstants.PUBLIC_DB: dbNameList.get(0);
					GridStore gstore = gridStoreServerIO.getConnection(operationInfo, dbName);
					PartitionController controller = gstore.getPartitionController();

					for ( String contName : operationInfo.getContainerNameList()){
						ContainerInfo contInfo = gstore.getContainerInfo(contName);
						if ( contInfo == null ){
							// D00512: No specified container exists.
							throw new GSEIException(messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_12")+" db=["+dbName+"] container=["+contName+"]");
						}
						if ( !containerMap.contains(contInfo.getName()) ){
							int i = controller.getPartitionIndexOfContainer(contInfo.getName());
							setContainerList(contInfo.getName(), operationInfo.getParallelCount()>1?controller.getOwnerHost(i).toString():"AllNode", dbName, i);
							containerMap.add(contInfo.getName());
						}
					}
					gstore.close();
					gstore = null;
				}
			}

			// If --all, --db, --containerregex is specified, get the container name list from DB    (Refine if a regular expression is specified)
			if ( operationInfo.getTargetType()==TARGET_TYPE.ALL || operationInfo.getTargetType()==TARGET_TYPE.DB || operationInfo.getRegexContainerNameList()!= null ){

				Set<Integer> failPartition = null;

				for ( String dbName : dbNameList ){
					GridStore gstore = gridStoreServerIO.getConnection(operationInfo, dbName);
					PartitionController controller = gstore.getPartitionController();
					int partitionCount = controller.getPartitionCount();

					for (int i = 0; i < partitionCount; i++) {
						try {
							if ( (failPartition != null) && failPartition.contains(i) ){
								// In case of a partition error, the partition will not be accessed again.
								continue;
							}
							List<String> containerNames = controller.getContainerNames(i, 0L, null); // Without limit

							// Narrow down if there is a regular expression
							if ( operationInfo.getRegexContainerNameList() != null ) {
								containerNames = checkRegexContainer(operationInfo, containerNames, containerMap);
							}
							if ( containerNames.size() > 0 ){
								setContainerList(containerNames, operationInfo.getParallelCount()>1?controller.getOwnerHost(i).toString():"AllNode", dbName, i);
							}

						} catch ( GSException e ){
							if ( operationInfo.getForceFlag() ){
								if ( failPartition == null ) failPartition = new HashSet<Integer>();
								failPartition.add(i);
								String errMsg = messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_26_1")+i+messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_26_2");
								operationInfo.sysoutString(errMsg);
								commandProgressStatus.setContainerStatus(null, false, errMsg);
								continue;
							} else {
								throw e;
							}
						}
					}
					gstore.close();
					gstore = null;
				}
			}

			if ( getContainerCount() == 0 ){
				// The corresponding container does not exist for export.
				throw new GSEIException(messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_18"));
			}

			m_progressMap = new HashMap<String, Iterator<DbPartition>>(m_containerMap.size(), 1.0F);
			for ( Map.Entry<String, List<DbPartition>> entry : m_containerMap.entrySet() ){
				m_progressMap.put(entry.getKey(), entry.getValue().iterator());
			}

		} catch (GSTimeoutException e){
			// The connection process to the Grid Store has timed out.
			throw new GSEIException(messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_21")+":"+e.getMessage(), e);

		} catch (GSEIException e ){
			throw e;

		} catch (Exception e) {
			// "D00502: The container name list could not be acquired."
			String errMsg = messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_2")+ ":"+ e.getMessage();
			throw new GSEIException(errMsg, e);
		}
	}

	private void setContainerList(List<String> contList, String addressStr, String dbName, int partitionNo){
		// [memo]I can't get the port number. There is no problem
		// because multiple GridStore nodes on the same machine cannot be used in normal operation.

		// Map<String, List<DBPartition>> m_containerMap  Node address - List <DB partition information (DB name, partition ID, container group)>
		List<DbPartition> dbList = m_containerMap.get(addressStr);
		if ( dbList == null ){
			dbList = new ArrayList<DbPartition>();
			m_containerMap.put(addressStr, dbList);
		}

		DbPartition dbPartition = new DbPartition(dbName, partitionNo, contList);
		int idx = dbList.indexOf(dbPartition);
		if ( idx > -1 ){
			DbPartition part = dbList.get(idx);
			part.addContainerList(contList);

		} else {
			dbList.add(dbPartition);
		}
	}

	private void setContainerList(String contName, String addressStr, String dbName, int partitionNo ){
		List<String> contList = new ArrayList<String>();
		contList.add(contName);
		setContainerList(contList, addressStr, dbName, partitionNo);
	}

	/**
	 * Checks if the container name corresponds to a regular expression and returns a list of applicable container names.
	 *
	 * @param operationInfo
	 * @param containerList
	 */
	private List<String> checkRegexContainer(commandLineInfo operationInfo, List<String> containerList, Set<String> containerMap){

		List<String> list2 = operationInfo.getRegexContainerNameList();
		List<String> returnList = new ArrayList<String>();

		if ( (list2 != null) && !list2.isEmpty() ){

			// Find the container name that matches the regular expression
			for ( int i = 0; i < list2.size(); i++ ){
				Pattern singleMatch = Pattern.compile(list2.get(i), Pattern.CASE_INSENSITIVE);
				for (String contName : containerList) {
					Matcher m1 = singleMatch.matcher(contName);
					if (m1.find()) {
						if (!containerMap.contains(contName)){
							returnList.add(contName);
							containerMap.add(contName);
						}
					}
				}
			}
		}
		return returnList;
	}


	public Set<String> getDatabaseList(){
		return m_databaseList;
	}


	/*
	public List<String> getSubContainerList(String largeContainerName){
		return m_subContainerMap.get(largeContainerName);
	}
	*/

}

/**
 * A class that manages the container list.
 * Manage the container list for each combination of DB name and partition ID.
 */
class DbPartition{
	private String m_dbName;
	private int m_partitionId;
	private List<String> m_containerList;

	public DbPartition(String dbName, int partitionId, List<String> containerList){
		m_dbName = dbName;
		m_partitionId = partitionId;
		m_containerList = containerList;
	}

	public String getDbName(){
		return m_dbName;
	}
	public void setDbName(String dbName){
		m_dbName = dbName;
	}


	public int getPartitionId(){
		return m_partitionId;
	}
	public void setPartitionId(int partitionId){
		m_partitionId = partitionId;
	}

	public List<String> getContainerList(){
		return m_containerList;
	}
	public void setContainerList(List<String> containerList){
		m_containerList = containerList;
	}
	public void addContainerList(List<String> containerList){
		if ( m_containerList == null ){
			m_containerList = new ArrayList<String>();
		}
		m_containerList.addAll(containerList);
	}
	public void addContainer(String containerName){
		if ( m_containerList == null ){
			m_containerList = new ArrayList<String>();
		}
		m_containerList.add(containerName);
	}


	/**
	 *If the DB name and partition ID match, it is considered as a match. Do not include the container list.
	 */
    public boolean equals(Object anObject) {
		if (this == anObject) {
		    return true;
		}
		if (anObject instanceof DbPartition) {
			DbPartition value = (DbPartition)anObject;

			if ( m_dbName == null ){
				if ( value.m_dbName == null ){
					if ( m_partitionId == value.m_partitionId ){
						return true;
					}
				} else {
					return false;
				}
			} else {
				if ( m_dbName.equals(value.m_dbName) ){
					if ( m_partitionId == value.m_partitionId ){
						return true;
					}
				}
			}
		}
		return false;
    }

    public int hashCode(){
    	int hashCode = 0;
    	if ( m_dbName != null ) hashCode += m_dbName.hashCode();
    	if ( m_partitionId != -1 ) hashCode += m_partitionId;
    	return hashCode;
    }
}