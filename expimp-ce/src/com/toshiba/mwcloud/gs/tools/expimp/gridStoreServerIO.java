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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.GridStoreFactory;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.tools.common.data.ToolConstants;
import com.toshiba.mwcloud.gs.tools.expimp.util.Utility;

/**
 * GGridStore access class
 *
 */
public class gridStoreServerIO {
	/**
	 * Command parameter information class
	 */
	//private commandLineInfo comLineInfo;
	/**
	 * Internationalized message resource
	 */
	//private static ResourceBundle messageResource;
	/**
	 * Configure for logging
	 */
	private static final Logger log = LoggerFactory.getLogger(gridStoreServerIO.class);

	/** GridStore object */
	//private GridStore m_gridStore;

	/** JDBC connection object */
	//private Connection m_connection;

	/**
	 * Constructor
	 *
	 * @param cli command parameter information class
	 */
	/*
	public gridStoreServerIO(commandLineInfo cli) {
		this.comLineInfo = cli;
		// メッセージリソースの初期化
		messageResource = Utility.getResource();
	}
	*/

	/*
	public GridStore connectServer() throws GSEIException {
		try {
			if ( m_gridStore != null ) {
				m_gridStore.close();
				m_gridStore = null;
			}
		} catch ( Exception e ){} // クローズのエラーは無視

		m_gridStore = getConnection(comLineInfo, comLineInfo.getDatabaseName());

		return m_gridStore;
	}

	public Connection jdbcConnect() throws GSEIException {
		try {
			if ( m_connection != null ) {
				m_connection.close();
				m_connection = null;
			}
		} catch ( Exception e ){} // クローズのエラーは無視

		m_connection = getJdbcConnection(comLineInfo, comLineInfo.getDatabaseName());

		return m_connection;
	}
	*/

	/**
	 * GridStore server connection method
	 *
	 * @return GridStore instance
	 */
	public static GridStore getConnection(commandLineInfo cli) throws GSEIException {
		return getConnection(cli, ToolConstants.PUBLIC_DB);
	}
	public static GridStore getConnection(commandLineInfo cli, String database) throws GSEIException {
		// Server connection
		GridStore store = null;
		String errmsg = "";
		try {
			// Connection property settings
			Properties prop = new Properties();

			// V4.2- Application name gs_export or gs_import
			prop.setProperty(GSConstants.PROP_APPLICATION_NAME, cli.getCmdName());

			switch (cli.getNotificationMode()) {
			case FIXED_LIST:
				//System.out.println("FIXED_LIST:"+cli.getTransactionMember());
				prop.setProperty(GSConstants.PROP_NOTIFICATION_MEMBER, cli.getTransactionMember());
				errmsg = GSConstants.PROP_NOTIFICATION_MEMBER+"=["+cli.getTransactionMember()+"]";
				break;
			case PROVIDER:
				//System.out.println("PROVIDER:"+cli.getProviderURL());
				prop.setProperty(GSConstants.PROP_PROVIDER_URL, cli.getProviderURL());
				errmsg = GSConstants.PROP_PROVIDER_URL+"=["+cli.getProviderURL()+"]";
				break;
			case MULTICAST:
			default:
				//System.out.println("MULTICAST:"+cli.getServerAddress());
				prop.setProperty(GSConstants.PROP_NOTIFICATION_ADDRESS, cli.getServerAddress().getHostAddress());
				prop.setProperty(GSConstants.PROP_NOTIFICATION_PORT, Integer.toString(cli.getPort()));
				errmsg = GSConstants.PROP_NOTIFICATION_ADDRESS+"=["+cli.getServerAddress().getHostAddress()+"] "+GSConstants.PROP_NOTIFICATION_PORT+"=["+cli.getPort()+"]";
				break;
			}

			prop.setProperty(GSConstants.PROP_CLUSTER_NAME, cli.getClusterName());
			// Database setting
			if ( (database != null) && !database.equalsIgnoreCase(ToolConstants.PUBLIC_DB) )
				prop.setProperty(GSConstants.PROP_DATABASE, database);
			prop.setProperty(GSConstants.PROP_USER, cli.getUserName());
			prop.setProperty(GSConstants.PROP_PASSWORD, cli.getPassword());
			// Time-out time setting
			prop.setProperty(GSConstants.PROP_TRANSACTION_TIMEOUT,Integer.toString(cli.getTransactionTimeout()));
			// Failover timeout time (time to search for a cluster by multicast)
			prop.setProperty(GSConstants.PROP_FAILOVER_TIMEOUT,Integer.toString(cli.getFailoverTimeout()));

			if ( cli.getConsistency() != null ) prop.setProperty(GSConstants.PROP_CONSISTENCY, cli.getConsistency());
			if ( cli.getContainerCacheSize() != null ) prop.setProperty(GSConstants.PROP_CONTAINER_CHACHE_SIZE, cli.getContainerCacheSize());
			if ( cli.getFetchBytesSize() != null ) prop.setProperty(GSConstants.PROP_FETCH_BYTES_SIZE, cli.getFetchBytesSize());

			// 4.2- If it is Export, set the value of export.storeMemoryAgingSwapRate to storeMemoryAgingSwapRate.
			if (cli.getCmdName() == GSConstants.CMD_NAME.GS_EXPORT.toString()) {
				prop.setProperty(GSConstants.PROP_AGING_SWAP_RATE, cli.getExportStoreMemoryAgingSwapRate());
			}

			// V4.5 If the authentication method is specified in the property file, set the authentication method.
			if ( cli.getAuthenticationMethod() != null) {
				prop.setProperty(GSConstants.PROP_AUTHENTICATION, cli.getAuthenticationMethod());
			}

			if ( cli.getSslMode() != null) {
				prop.setProperty(GSConstants.PROP_SSL_MODE, cli.getSslMode());
			}
			// V4.5 If the address of the interface that sends the multicast packet is specified, set it.
			if ( cli.getNotificationInterfaceAddress() != null) {
				prop.setProperty(GSConstants.PROP_NOTIFICATION_INTERFACE_ADDRESS, cli.getNotificationInterfaceAddress());
			}

			// V4.5 Output debug log
			if ( log.isDebugEnabled() ) {
				Properties tmpprop = new Properties();
				tmpprop.putAll(prop);
				if ( tmpprop.containsKey(GSConstants.PROP_PASSWORD) ) {
					// Overwrite password value with dummy value for log output
					tmpprop.setProperty(GSConstants.PROP_PASSWORD, "XXXXX");
				}
				log.debug("Call GridStoreFactory.getGridStore() Properties:[" + tmpprop.toString() + "]");
			}

			// Get grid store
			store = GridStoreFactory.getInstance().getGridStore(prop);

			// Check if the connection is valid by searching the dummy container
			Container<Object, Row> container = store.getContainer("DUMMY");
			if (container != null) {
				container.close();
			}
			return store;

		} catch (Exception e) {
			try {
				if ( store != null ){
					store.close();
					store = null;
				}
			} catch ( Exception ex ){}

			// "An error occurred while running the server connection process"
			String errMsg = Utility.getResource().getString("MESS_EXPORT_ERR_EXPORTPROC_7")
					+ ": " + errmsg +" msg=["+ e.getMessage()+"]";
			throw new GSEIException(errMsg, e);
		}
	}


	/**
	 * JDBC connection method
	 * @param cli
	 * @param database
	 * @return Connection
	 * @throws GSEIException
	 */
	public static Connection getJdbcConnection(commandLineInfo cli, String database) throws GSEIException {
		Connection conn = null;
		String url = null;
		try {
			Class.forName("com.toshiba.mwcloud.gs.sql.Driver");
			DriverManager.setLoginTimeout(cli.getJdbcLoginTimeout());
			url = cli.createJdbcUrl(database);

			// 4.2- Changed to props specification to specify application name
			Properties props = new Properties();
			props.setProperty(GSConstants.PROP_USER, cli.getUserName());
			props.setProperty(GSConstants.PROP_PASSWORD, cli.getPassword());
			props.setProperty(GSConstants.PROP_APPLICATION_NAME, cli.getCmdName());

			// 4.2- If it is Export, set the value of export.storeMemoryAgingSwapRate to storeMemoryAgingSwapRate.
			if (cli.getCmdName() == GSConstants.CMD_NAME.GS_EXPORT.toString()) {
				props.setProperty(GSConstants.PROP_AGING_SWAP_RATE, cli.getExportStoreMemoryAgingSwapRate());
			}

			// V4.5 If the authentication method is specified in the property file, set the authentication method.
			if ( cli.getAuthenticationMethod() != null) {
				props.setProperty(GSConstants.PROP_AUTHENTICATION, cli.getAuthenticationMethod());
			}

			if ( cli.getSslMode() != null) {
				props.setProperty(GSConstants.PROP_SSL_MODE, cli.getSslMode());
			}
			// V4.5 If the address of the interface that sends the multicast packet is specified, set it.
			if ( cli.getNotificationInterfaceAddress() != null) {
				props.setProperty(GSConstants.PROP_NOTIFICATION_INTERFACE_ADDRESS, cli.getNotificationInterfaceAddress());
			}

			// V4.5 Output debug log
			if ( log.isDebugEnabled() ) {
				Properties tmpprop = new Properties();
				tmpprop.putAll(props);
				if ( tmpprop.containsKey(GSConstants.PROP_PASSWORD) ) {
					// Overwrite password value with dummy value for log output
					tmpprop.setProperty(GSConstants.PROP_PASSWORD, "XXXXX");
				}
				log.debug("Call JDBC getConnection() url:[" + url + "] Properties:[" + tmpprop.toString() + "]");
			}

			conn = java.sql.DriverManager.getConnection(url, props);

			// Executing a pragma statement to get a hidden metatable
			// V4.1 #With the release of tables users, pragma is no longer necessary
			//GridDBJdbcUtils.executePragma(conn, ToolConstants.PRAMGMA_INTERNAL_META_TABLE_VISIBLE, "1");

		} catch (Exception e) {
			try {
				if ( conn != null ) {
					conn.close();
					conn = null;
				}
			} catch (Exception ex) {}

			String errMsg = Utility.getResource().getString("MESS_EXPORT_ERR_EXPORTPROC_29") +": url=[" + url + "] msg=["+ e.getMessage()+"]";;
			throw new GSEIException(errMsg, e);
		}
		return conn;
	}

	/**
	 * Get a list of partition table names when connecting to JDBC
	 *
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public static Set<String> getPartitionTableNames(Connection conn) throws Exception{
		Statement stmt = null;
		ResultSet rs = null;
		Set<String> setPartitionNames = new HashSet<String>();
		try {
			stmt = conn.createStatement();
			// Execute query SELECT * FROM "#tables" WHERE PARTITION_TYPE IS NOT NULL
			rs = stmt.executeQuery(ToolConstants.STMT_SELECT_META_TABLES_PATITIONNAMES);
			while (rs.next()) {
				String partName = rs.getString(ToolConstants.META_TABLES_TABLE_NAME);
				setPartitionNames.add(partName);
			}
		} catch (Exception e ){
			throw e;
		}

		return setPartitionNames;
	}


	/**
	 * インターバルパーティショニングテーブル(パーティショニングキーがTimestamp型)　テーブル名一覧取得メソッド
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public static Set<String> getIntervalPartitionTableNames(Connection conn) throws Exception{
		Statement stmt = null;
		ResultSet rs = null;
		Set<String> setPartitionNames = new HashSet<String>();
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(ToolConstants.STMT_SELECT_META_TABLES_INTERVAL_PARTITIONNAMES);
			while (rs.next()) {
				String partName = rs.getString(ToolConstants.META_TABLES_TABLE_NAME);
				setPartitionNames.add(partName);
			}
		} catch (Exception e ){
			throw e;
		}

		return setPartitionNames;
	}
	
	/**
	 * インターバルパーティショニングテーブル(パーティショニングキーがTimestamp型以外)　テーブル名一覧取得メソッド
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public static Set<String> getIntervalPartitionTableNotTimestampNames(Connection conn) throws Exception{
		Statement stmt = null;
		ResultSet rs = null;
		Set<String> setPartitionNames = new HashSet<String>();
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(ToolConstants.STMT_SELECT_META_TABLES_INTERVAL_NOT_TIMESTAMP_PARTITIONNAMES);
			while (rs.next()) {
				String partName = rs.getString(ToolConstants.META_TABLES_TABLE_NAME);
				setPartitionNames.add(partName);
			}
		} catch (Exception e ){
			throw e;
		}

		return setPartitionNames;
	}

	/**
	 * ハッシュパーティショニングテーブル(パーティショニングキーがTimestamp型以外)　テーブル名一覧取得メソッド
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public static Set<String> getHashParitionTableNotTimestampNames(Connection conn) throws Exception{
		Statement stmt = null;
		ResultSet rs = null;
		Set<String> setPartitionNames = new HashSet<String>();
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(ToolConstants.STMT_SELECT_META_TABLES_HASH_NOT_TIMESTAMP_PARTITIONNAMES);
			while (rs.next()) {
				String partName = rs.getString(ToolConstants.META_TABLES_TABLE_NAME);
				setPartitionNames.add(partName);
			}
		} catch (Exception e ){
			throw e;
		}

		return setPartitionNames;
	}
	
	/**
	 * TimeSeriesコンテナ　コンテナ名一覧取得メソッド
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public static Set<String> getTimeSeriesContainerNames(Connection conn) throws Exception{
		Statement stmt = null;
		ResultSet rs = null;
		Set<String> setContainerNames = new HashSet<String>();
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(ToolConstants.STMT_SELECT_META_TIMESERIES_NAMES);
			while (rs.next()) {
				String partName = rs.getString(ToolConstants.META_TABLES_TABLE_NAME);
				setContainerNames.add(partName);
			}
		} catch (Exception e ){
			throw e;
		}

		return setContainerNames;
	}

	/**
	 * Method to create container on GridStore
	 *
	 * @param store GridStore instance
	 * @param contInfo Container information class
	 * @return GridStore container information class
	 */
	/*
	public Container<?, Row> createContainer(GridStore store, ToolContainerInfo local_container) throws GSEIException {

		try {
			// Container creation
			Container<?,Row> gs_containerInfo =  store.putContainer(local_container.getContainerInfo().getName(), local_container.getContainerInfo(), false);

			return gs_containerInfo;

		} catch (Exception e) {
			// "An error occurred during the process of creating a container in the Gridstore"
			throw new GSEIException(messageResource.getString("MESS_IMPORT_ERR_GRIDSTORE_1")
					+ ": containerName=["+local_container.getContainerInfo().getName()+"] msg=["+e.getMessage()+"]", e);
		}
	}
	*/

	/*
	public ContainerInfo getContainerInfo(GridStore store, String contName){
		try {
			ContainerInfo contInfo = store.getContainerInfo(contName);
			return contInfo;
		} catch ( Exception e ){
			e.printStackTrace();
			return null;
		}
	}
	*/

	/*
	// Trigger creation
	public void cretatTrigger(ToolContainerInfo metaInfoCntainer,Container<?, Row> targetContainer) throws GSException , Exception{
		String triggerName = null;
		try {
			for (TriggerInfo local_trigger : metaInfoCntainer.getTriggerInfoList()) {
				targetContainer.createTrigger(local_trigger);
			}
		} catch (Exception e) {
			// "An error occurred during the trigger creation process"
			comLineInfo.sysoutString(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_34")+ ": TriggerName="
					+  triggerName + " msg=" + e.getMessage());
			log.error(messageResource.getString("MESS_IMPORT_ERR_IMPORTPROC_34")+ ": TriggerName=" +  triggerName
				+ "msg=["+ e.getMessage() + e );
			throw e;
		}
		return;
	}
	*/

	/**
	 * All container name search methods on GridStore
	 *
	 * @param store GridStore instance
	 * @return Container name list
	 * @throws GSTimeoutException
	 */
	/*
	public Map<String, Map<Integer, List<String>>> getAllContainerList(GridStore store) throws GSEIException {
		try {
			Map<String, Map<Integer, List<String>>> containerNameList = new HashMap<String, Map<Integer, List<String>>>();

			// Get partition information
			PartitionController controller = store.getPartitionController();
			int partitionCount = controller.getPartitionCount();

			// Get container information
			for (int i = 0; i < partitionCount; i++) {
				List<String> temp = controller.getContainerNames(i, 0L, null); // Without limit

				InetAddress address = controller.getOwnerHost(i);
				String addressStr = address.toString();
				// [memo] I can't get the port number.
				// There is no problem because multiple GridStore nodes on the same machine cannot be used in normal operation.

				Map<Integer, List<String>> map = containerNameList.get(addressStr);
				if ( map == null ){
					map = new HashMap<Integer, List<String>>();
					containerNameList.put(addressStr, map);
				}
				List<String> set = map.get(i);
				if ( set == null ){
					set = temp;
					map.put(i, set);
				} else {
					set.addAll(temp);
				}
			}
			return containerNameList;

		} catch (GSTimeoutException e){
			// The connection process to the GridStore has timed out.
			throw new GSEIException(messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_21")+":"+e.getMessage(), e);

		} catch (Exception e) {
			// "D00502: Can't get container name list from GridStore"
			String errMsg = messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_2")+ ":"+ e.getMessage();
			throw new GSEIException(errMsg, e);
		}
	}
	*/

	/**
	 *
	 * Method to search the specified container from GridStore
	 *
	 * @param store GridStore instance
	 * @param selectContainerList List of container names to search
	 * @return List of container information classes
	 */
	/*
	public List<ToolContainerInfo> getContainerInfo(List<ToolContainerInfo> selectContainerList) throws GSEIException{

		PartitionController controller = null;
		List<ToolContainerInfo> errorList = null;
		try {
			controller = m_gridStore.getPartitionController();
			errorList = new ArrayList<ToolContainerInfo>();
		} catch (Exception e) {
			commandProgressStatus.setContainerStatus(null, false, messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_10")+ ":"+ e.getMessage());
			// "An error occurred while executing the container information acquisition process"
			throw new GSEIException(messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_10")+ ":"+ e.getMessage(),e);
		}


		// Container information acquisition (collection / time series)
		for (ToolContainerInfo local_container : selectContainerList) {

			try {
				// Get GridStore container object from container name
				String s = local_container.getContainerInfo().getName();
				ExtendedContainerInfo store_container = ExperimentalTool.getExtendedContainerInfo(m_gridStore, s);
				if ( store_container == null ){
					errorList.add(local_container);
					// The specified container does not exist on the GridStore
					throw new GSEIException(messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_12")+": containerName=["+s+"]");
				}

				local_container.setContainerInfo(store_container);
				local_container.setPartitionNo(controller.getPartitionIndexOfContainer(s));


			} catch ( Exception e ){
				// [memo] It is better to put together the process of continuation by option in exportProcess.
				String errMsg = "";
				GSEIException ex = null;
				if ( e instanceof GSEIException ){
					errMsg = e.getMessage();
					ex = (GSEIException)e;
				} else {
					// D00510: An error occurred while executing the container information acquisition process
					errMsg = messageResource.getString("MESS_EXPORT_ERR_EXPORTPROC_10")+ ": containerName=["
							+local_container.getContainerInfo().getName()+"] msg=["+ e.getMessage()+"]";
					ex = new GSEIException(errMsg, e);
				}
				commandProgressStatus.setContainerStatus(local_container.getContainerInfo().getName(), false, errMsg);
				if ( comLineInfo.getForceFlag() ){
					// --force In the case of, log output and continue
					comLineInfo.sysoutString(errMsg);
					log.error(errMsg, e);
					errorList.add(local_container);
				} else {
					throw ex;
				}
			}
		}

		// The container in which the error occurred is excluded from the subsequent processing
		// (when the --force option is selected).
		if ( !errorList.isEmpty() ){
			selectContainerList.removeAll(errorList);
		}

		return selectContainerList;
	}
	*/
}
