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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import com.toshiba.mwcloud.gs.tools.common.NotificationMode;
import com.toshiba.mwcloud.gs.tools.common.data.ToolConstants;
import com.toshiba.mwcloud.gs.tools.common.data.ToolConstants.RowFileType;
import com.toshiba.mwcloud.gs.tools.expimp.GSConstants.TARGET_TYPE;

/**
 * Command line information class
 *
 *  Manages the properties file (gs_expimp.properties)
 *  and the values specified by command line arguments and options.
 *
 * [Note]
 *  Define the default value for each parameter as the initial
 *  value for the member variables of this class.
 *
 */
public class commandLineInfo /*implements commandLineInfo_Reader */{
	/**
	 * Command name
	 */
	private String cmdArg;

	//=========================================
	// GridStore(No SQL) Connection parameters
	//=========================================
	/**
	 * Connection method    [propertyFile]
	 */
	private NotificationMode notificationMode = NotificationMode.MULTICAST;

	/**
	 * NoSQL Multicast address  (Set to default value "239.0.0.1" in constructor)   [propertyFile]
	 */
	private InetAddress serverAddress;

	/**
	 * NoSQL Multicast port number    [propertyFile]
	 */
	private int port = 31999;

	/**
	 * NewSQL Multicast address  (Set to default value "239.0.0.1" in constructor)   [propertyFile]
	 */
	private InetAddress m_jdbcAddress;

	/**
	 * NewSQL Multicast port number    [propertyFile]
	 */
	private int m_jdbcPort = 41999;

	/**
	 * NoSQL Fixed list method address    [propertyFile]
	 */
	private String transactionMember;

	/**
	 * NewSQL Fixed list method address    [propertyFile]
	 */
	private String m_jdbcTransactionMember;

	/**
	 * NoSQL/NewSQL Provider method address    [propertyFile]
	 */
	private String providerURL;

	/**
	 * Node address (for extension)     [propertyFile]
	 */
	private String m_restAddress = "127.0.0.1";

	/**
	 * Node address (for extension)    [propertyFile]
	 */
	private int m_restPort = 10040;

	/**
	 * JDBC URL format    [propertyFile]
	 * <p>
	 * (Design Note)
	 * When the DBC connection destination URL assembly method createJdbcUrl is executed for the first time,
	 * it is set according to each parameter value. It cannot be set directly to this value.
	 */
	private String m_jdbcUrlPrefix;
	private String m_jdbcUrlPostfix;

	/**
	 * Whether JDBC is enabled
	 * <p>
	 * (Design Note)
	 * Default true. Set to false when an exception of JDBC driver read failure occurs.
	 * If enabled, Export processes the partitioning table.
	 */
	private boolean m_jdbcEnabled = true;

	/**
	 * Cluster name    [propertyFile]
	 */
	private String clusterName = "defaultCluster";

	/**
	 * Username when accessing the server    [option]
	 */
	private String userName = "";

	/**
	 * Password for server access    [option]
	 */
	private String password = "";

	/**
	 * NoSQL timeout setting    [propertyFile]
	 */
	private int transactionTimeout = 2147483647;

	/**
	 * NoSQL Failover timeout time (time to search for a cluster by multicast)    [propertyFile]
	 */
	private int failoverTimeout = 10;

	/**
	 * NewSQL JDBC login timeout    [propertyFile]
	 */
	private int m_jdbcLoginTimeout = 10;

	/**
	 * NoSQL Transactional consistency    [propertyFile]
	 */
	private String consistency;

	/**
	 * NoSQL Maximum number of container information to be stored in the container cache    [propertyFile]
	 */
	private String containerCacheSize;

	/**
	 * Export fetch size    [propertyFile]
	 */
	private String m_fetchBytesSize;

	/**
	 * export.storeMemoryAgingSwapRate		[propertyFile]
	 */
	private String m_exportStoreMemoryAgingSwapRate = "0";


	/**
	 * Authentication method (internal authentication)    [propertyFile]
	 */
	private String authenticationMethod;

	/**
	 * SSL接続設定    [propertyFile]
	 */
	private String sslMode;

	/**
	 * Address of the interface that sends the multicast packet    [propertyFile]
	 */
	private String notificationInterfaceAddress;

	/**
	 * コマンドintervalsオプションのTimeZone設定　[propertyFile]
	 */
	private String m_intervalTimeZoneId = "GMT+09:00";
	
	//=====================================================
	// Setting parameters for Exp/Imp
	//=====================================================
	/**
	 * Type of processing target
	 */
	private TARGET_TYPE targetType = TARGET_TYPE.NONE;

	/**
	 * Connection destination DB name list
	 */
	private List<String> dbNameList;

	/**
	 * Container name list
	 */
	private List<String> containerNameList;

	/**
	 * Container name regular expression list
	 */
	private List<String> regexContainerNameList;

	/**
	 * Directory path string
	 */
	private String directoryPath;

	/**
	 * Import file name list [Import]
	 */
	private List<String> fileNameList;

	/**
	 * Export file name (for multi-container format) [Export]
	 */
	private String outFileName;

	/**
	 * Filter file name [Export]
	 */
	private String filterFileName;

	/**
	 * Raw file format (CSV format / binary format) [Export]
	 */
	private RowFileType m_rowFileType = RowFileType.CSV;

	/**
	 * Import (number of commits)    [propertyFile or option] The priority is option > propertyFile > Default value
	 */
	private int m_commitCount = 1000;

	/**
	 * Maximum size of output file (MB) [Export]
	 */
	private int fileSizeLimit = GSConstants.BINARY_FILE_INIT_SIZE;	// Default 100MB

	/**
	 * Parallel number [Export]
	 */
	private int m_parallelCount = 1;

	/**
	 * Multi-container format flag [Export]
	 */
	private boolean out_flag;

	/**
	 * Database user ACL configuration flags
	 */
	private boolean acl_flag;

	/**
	 * List display mode flag [Import]
	 */
	private boolean list_flag;

	/**
	 * Additional registration mode flag [Import]
	 */
	private boolean append_flag;

	/**
	 * Replacement registration mode flag [Import]
	 */
	private boolean replace_flag;

	/**
	 * Test mode flag
	 */
	private boolean test_flag;

	/**
	 * Presence or absence of query file [Export]
	 */
	private boolean filterfile_flag;

	/**
	 * Silent mode flag
	 */
	private boolean silent_flag;

	/**
	 * Forced execution mode flag
	 */
	private boolean force_flag;

	/**
	 * Advanced output mode flag
	 */
	private boolean verbose_flag;

	/**
	 * Schema check skip mode flag [Import]
	 */
	private boolean schema_check_skip_flag;

	/**
	 * Container definition only export flag[Export]
	 */
	private boolean schema_only_flag;

	/**
	 * インターバル指定
	 */
	private Date[] intervals;
	
	private int m_parallelInputCount = -1;
	private int m_parallelOutputCount = -1;

	private int m_maxJobSize = 2;
	private int m_maxJobBufferSize = 512;

	private String m_storeBlockSize = "64KB";



	/**
	 * Constructor
	 */
	public commandLineInfo() {
		try {
			serverAddress = InetAddress.getByName("239.0.0.1");
			m_jdbcAddress = InetAddress.getByName("239.0.0.1");

		} catch ( UnknownHostException e ){
		}
	}

	//---------------------------------------------------------
	// Setting value used for connection processing to GridDB cluster
	//---------------------------------------------------------
	/**
	 * Stores the connection method.
	 *
	 * @param mode Connection method
	 */
	public void setNotificationMode(NotificationMode mode){
		notificationMode = mode;
	}
	/**
	 * Returns the connection method.
	 *
	 * @return Connection method
	 */
	public NotificationMode getNotificationMode(){
		return notificationMode;
	}

	/**
	 * Stores the NoSQL multicast address.
	 *
	 * @param arg InetAddress information
	 */
	public void setServerAddress(InetAddress arg) {
		serverAddress = arg;
	}
	/**
	 * NoSQL Returns the multicast address.
	 *
	 * @return NoSQL multicast address
	 */
	public InetAddress getServerAddress() {
		return serverAddress;
	}

	/**
	 * Stores the NoSQL multicast port number.
	 *
	 * @param arg int port number
	 */
	public void setPort(int arg) {
		port = arg;
	}
	/**
	 * NoSQL Returns the multicast port number.
	 *
	 * @return int Number of port numbers
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Stores NoSQL fixed list addresses.
	 *
	 * @param member
	 */
	public void setTransactionMember(String member){
		transactionMember = member;
	}
	/**
	 * NoSQL Returns a fixed list address.
	 *
	 * @return Fixed list address
	 */
	public String getTransactionMember(){
		return transactionMember;
	}

	/**
	 * Stores the NewSQL multicast address.
	 *
	 * @param address
	 */
	public void setJdbcAddress(InetAddress address){
		m_jdbcAddress = address;
	}
	/**
	 * Returns the NewSQL multicast address.
	 *
	 * @return
	 */
	public InetAddress getJdbcAddress(){
		return m_jdbcAddress;
	}

	/**
	 * Stores the NewSQL multicast port number.
	 *
	 * @param port
	 */
	public void setJdbcPort(int port){
		m_jdbcPort = port;
	}
	/**
	 * Returns the NewSQL multicast port number.
	 *
	 * @return
	 */
	public int getJdbcPort(){
		return m_jdbcPort;
	}

	/**
	 * NewSQL Stores fixed list addresses.
	 *
	 * @param member
	 */
	public void setJdbcTransactionMember(String member){
		m_jdbcTransactionMember = member;
	}
	/**
	 * NewSQL Returns a fixed list address.
	 *
	 * @return Fixed list address
	 */
	public String getJdbcTransactionMember(){
		return m_jdbcTransactionMember;
	}

	/**
	 * Stores the NoSQL/NewSQL provider address.
	 *
	 * @param url
	 */
	public void setProviderURL(String url){
		providerURL = url;
	}
	/**
	 * Returns the NoSQL/NewSQL provider address.
	 * @return
	 */
	public String getProviderURL(){
		return providerURL;
	}

	/**
	 * Stores the NoSQL node address. (For expansion)
	 *
	 * @param arg InetAddress information
	 */
	public void setRestAddress(String arg) {
		m_restAddress = arg;
	}
	/**
	 * Returns the NoSQL node address. (For expansion)
	 *
	 * @return NoSQL multicast address
	 */
	public String getRestAddress() {
		return m_restAddress;
	}

	/**
	 * Stores the NoSQL node port number. (For expansion)
	 *
	 * @param arg int port number
	 */
	public void setRestPort(int arg) {
		m_restPort = arg;
	}
	/**
	 * NoSQL Returns the node port number. (For expansion)
	 *
	 * @return int Number of port numbers
	 */
	public int getRestPort() {
		return m_restPort;
	}

	/**
	 * Stores the user name.
	 *
	 * @param arg Username string
	 */
	public void setUserName(String arg) {
		userName = arg;
	}
	/**
	 * Returns the username.
	 *
	 * @return Username string
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Store password.
	 *
	 * @param arg Password string
	 */
	public void setPassword(String arg) {
		password = arg;
	}
	/**
	 * Returns the password.
	 *
	 * @return Password string
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set the cluster name.
	 *
	 * @param clusterName
	 */
	public void setClusterName(String clusterName){
		this.clusterName = clusterName;
	}
	/**
	 * Returns the cluster name.
	 *
	 * @return
	 */
	public String getClusterName(){
		return clusterName;
	}

	/**
	 * Stores the transaction timeout period.
	 *
	 * @param arg int Timeout value
	 */
	public void setTransactionTimeout(int arg) {
		transactionTimeout = arg;
	}
	/**
	 * Returns the transaction timeout period.
	 *
	 * @return int Time-out time setting (seconds)
	 */
	public int getTransactionTimeout() {
		return transactionTimeout;
	}

	/**
	 * Stores the failover timeout period.
	 *
	 * @param arg
	 */
	public void setFailoverTimeout(int arg) {
		failoverTimeout = arg;
	}
	/**
	 * Returns the failover timeout period (the time it takes to explore the cluster by multicast).
	 *
	* @return int Failover timeout time (time to explore the cluster by multicast) (seconds)
	 */
	public int getFailoverTimeout() {
		return failoverTimeout;
	}

	/**
	 * Set the Consistency.
	 *
	 * @param consistency
	 */
	public void setConsistency(String consistency){
		this.consistency = consistency;
	}
	/**
	 * Returns Consistency.
	 *
	 * @return
	 */
	public String getConsistency(){
		return consistency;
	}

	/**
	 * Set containerCacheSize.
	 *
	 * @param containerCacheSize
	 */
	public void setContainerCacheSize(String containerCacheSize){
		this.containerCacheSize = containerCacheSize;
	}
	/**
	 * Returns containerCacheSize.
	 *
	 * @return
	 */
	public String getContainerCacheSize(){
		return containerCacheSize;
	}

	/**
	 * Stores the export fetch size.
	 *
	 * @param fetchBytesSize
	 */
	public void setFetchBytesSize(String fetchBytesSize) {
		m_fetchBytesSize = fetchBytesSize;
	}
	/**
	 * Returns the fetch size at the time of export.
	 *
	 * @return
	 */
	public String getFetchBytesSize(){
		return m_fetchBytesSize;
	}

	/**
	 * Stores the JDBC login timeout period.
	 *
	 * @param jdbcLoginTimeout
	 */
	public void setJdbcLoginTimeout(int jdbcLoginTimeout) {
		m_jdbcLoginTimeout = jdbcLoginTimeout;
	}
	/**
	 * Returns the JDBC login timeout period.
	 *
	 * @return
	 */
	public int getJdbcLoginTimeout() {
		return m_jdbcLoginTimeout;
	}

	/**
	 * Stores export.storeMemoryUsingSwapRate
	 */
	public void setExportStoreMemoryAgingSwapRate(String exportStoreMemoryAgingSwapRate) {
		m_exportStoreMemoryAgingSwapRate = exportStoreMemoryAgingSwapRate;
	}

	/**
	 * Returns export.storeMemoryUsingSwapRate
	 */
	public String getExportStoreMemoryAgingSwapRate() {
		return m_exportStoreMemoryAgingSwapRate;
	}

	/**
	 * Generate a JDBC connection URL that contains the database name.
	 * <p>
	 * Uses the format generated according to the cluster configuration method when reading the properties.
	 *
	 * @param dbName Database name
	 * @return JDBC connection URL
	 */
	public String createJdbcUrl(String dbName) {
		String url = null;
		try {
			if ( m_jdbcUrlPrefix == null ){
				switch(notificationMode){
				case MULTICAST:
					m_jdbcUrlPrefix = GSConstants.JDBC_URL_PREFIX + m_jdbcAddress.getHostAddress() + ":" + m_jdbcPort + "/" + URLEncoder.encode(clusterName, "UTF-8") + "/";
					m_jdbcUrlPostfix = "";
					break;
				case FIXED_LIST:
					m_jdbcUrlPrefix = GSConstants.JDBC_URL_PREFIX + "/" + URLEncoder.encode(clusterName, "UTF-8") + "/";
					m_jdbcUrlPostfix = "?notificationMember=" + m_jdbcTransactionMember;
					break;
				case PROVIDER:
					m_jdbcUrlPrefix = GSConstants.JDBC_URL_PREFIX + "/" + URLEncoder.encode(clusterName, "UTF-8") + "/";
					m_jdbcUrlPostfix = "?notificationProvider=" + providerURL;
					break;
				}
			}
			if (dbName == null || dbName.isEmpty()) {
				dbName = ToolConstants.PUBLIC_DB;
			}
			url = m_jdbcUrlPrefix + URLEncoder.encode(dbName, "UTF-8") + m_jdbcUrlPostfix;

		} catch (UnsupportedEncodingException e) {
		}
		return url;
	}


	/**
	 * Stores the enable / disable of the New SQL interface (JDBC).
	 *
	 * @param jdbcEnabled
	 */
	public void setJdbcEnabled(boolean jdbcEnabled) {
		m_jdbcEnabled = jdbcEnabled;
	}
	/**
	 * Returns the enable / disable of the New SQL interface (JDBC).
	 *
	 * @return
	 */
	public boolean isJdbcEnabled() {
		return m_jdbcEnabled;
	}


	//---------------------------------------------------------
	// Setting value used in the process on the Export/Import tool side
	//---------------------------------------------------------
	/**
	 * Set the command name.
	 *
	 * @param arg Command name string
	 */
	public void setCmdName(String arg) {
		cmdArg = arg;
	}
	/**
	 * Returns the command name.
	 *
	 * @return Command name string
	 */
	public String getCmdName() {
		return cmdArg;
	}

	/**
	 * Stores a list of database names to be processed.
	 *
	 * @param dbNameList
	 */
	public void setDbNameList(List<String> dbNameList){
		this.dbNameList = dbNameList;
	}
	/**
	 * Returns a list of database names to process.
	 *
	 * @return
	 */
	public List<String> getDbNamelist(){
		return dbNameList;
	}

	/**
	 * Stores a list of container names to be processed.
	 *
	 * @param arg Container name string list
	 */
	public void setContainerNameList(List<String> arg) {
		containerNameList = arg;
	}
	/**
	 * Returns a list of container names to process.
	 *
	 * @return Container name string list
	 */
	public List<String> getContainerNameList() {
		return containerNameList;
	}

	/**
	 * Stores the container name regular expression list to be processed.
	 *
	 * @param arg Container name regular expression string list
	 */
	public void setRegexContainerNameList(List<String> arg) {
		regexContainerNameList = arg;
	}
	/**
	 * Container name regular expression acquisition method
	 *
	 * @return Container name regular expression string
	 */
	public List<String> getRegexContainerNameList() {
		return regexContainerNameList;
	}

	/**
	 * Stores the directory path.
	 *
	 * @param arg Directory path string
	 */
	public void setDirectoryPath(String arg) {
		directoryPath = arg;
	}
	/**
	 * Returns the directory path
	 *
	 * @return Directory path string
	 */
	public String getDirectoryPath() {
		return directoryPath;
	}
	/**
	 * Returns the directory path (full path).
	 * @return
	 * @throws IOException
	 */
	public String getDirectoryFullPath() throws IOException{
		if ( directoryPath == null ){
			return new File("").getAbsolutePath();
		} else {
			return new File(directoryPath).getCanonicalPath();
		}
	}

	/**
	 * Stores a list of import file names.
	 *
	 * @param arg Import list of file name
	 */
	public void setFileNameList(List<String> arg) {
		fileNameList = arg;
	}
	/**
	 * Returns a list of import filenames.
	 *
	 * @return List of file name
	 */
	public List<String> getFileNameList() {
		return fileNameList;
	}

	/**
	 * Stores the export file name.
	 *
	 * @param arg Export file name string
	 */
	public void setOutFileName(String arg) {
		outFileName = arg;
	}
	/**
	 * Returns the export file name.
	 *
	 * @return File name string
	 */
	public String getOutFileName() {
		return outFileName;
	}

	/**
	 * Stores the commit count.
	 *
	 * @param arg int Number of commit count
	 */
	public void setCommitCount(int arg) {
		m_commitCount = arg;
	}
	/**
	 * Returns the commit count.
	 *
	 * @return int Number of commit count
	 */
	public int getCommitCount() {
		return m_commitCount;
	}

	/**
	 * Set the type of processing target.
	 *
	 * @param targetType of Processing object
	 */
	public void setTargetType(TARGET_TYPE targetType) {
		this.targetType = targetType;
	}
	/**
	 * Returns the type to be processed.
	 *
	 * @return Type of processing target
	 */
	public TARGET_TYPE getTargetType() {
		return targetType;
	}

	/**
	 * Stores the raw data file format (CSV / Binary).
	 *
	 * @param arg Raw data file format
	 */
	public void setRowFileType(RowFileType arg) {
		m_rowFileType = arg;
	}
	/**
	 * Returns the raw data file format (CSV / Binary).
	 *
	 * @return Raw data file format
	 */
	public RowFileType getRowFileType() {
		return m_rowFileType;
	}

	/**
	 * Stores filter file settings.
	 * @return
	 */
	public boolean getFilterfileFlag() {
		return filterfile_flag;
	}
	/**
	 * Returns the settings in the filter file.
	 * @param arg
	 */
	public void setFilterFileFlag(boolean arg) {
		filterfile_flag = arg;
	}

	/**
	 * Stores the filter file name.
	 *
	 * @param arg
	 */
	public void setFilterFileName(String arg) {
		filterFileName = arg;
	}
	/**
	 * Returns the filter file name.
	 *
	 * @return
	 */
	public String getFilterFileName() {
		return filterFileName;
	}

	/**
	 * Sets the maximum size of the output file.
	 *
	 * @param arg Maximum file size (MB)
	 */
	public void setFileSizeLimit(int arg){
		fileSizeLimit = arg;
	}
	/**
	 * Returns the maximum size of the output file.
	 *
	 * @return Maximum file size (MB)
	 */
	public int getFileSizeLimit(){
		return fileSizeLimit;
	}

	/**
	 * Stores the number of parallels.
	 *
	 * @param arg
	 */
	public void setParallelCount(int arg){
		m_parallelCount = arg;
	}
	/**
	 * Returns the number of parallels.
	 *
	 * @return
	 */
	public int getParallelCount(){
		return m_parallelCount;
	}

	/**
	 * Stores the [--acl] flag.
	 *
	 * @param arg
	 */
	public void setAclFlag(boolean arg){
		this.acl_flag = arg;
	}
	/**
	 * Returns the [--acl] flag.
	 *
	 * @return
	 */
	public boolean getAclFlag(){
		return acl_flag;
	}

	/**
	 * Stores the [--out] flag.
	 *
	 * @param arg boolean Flag value
	 */
	public void setOutFlag(boolean arg) {
		out_flag = arg;
	}
	/**
	 * Returns the [--out] flag.
	 *
	 * @return true-with settings, false-without settings
	 */
	public boolean getOutFlag() {
		return out_flag;
	}

	/**
	 * [--force] flag setting method
	 *
	 * @param arg boolean Flag value
	 */
	public void setForceFlag(boolean arg) {
		force_flag = arg;
	}
	/**
	 * [--force] Flag setting acquisition method
	 *
	 * @return true-with settings, false-without settings
	 */
	public boolean getForceFlag() {
		return force_flag;
	}

	/**
	 * [-t | --test] flag setting method
	 *
	 * @param arg booleanFlag value
	 */
	public void setTestFlag(boolean arg) {
		test_flag = arg;
	}
	/**
	 * [-t | --test] Flag setting acquisition method
	 *
	 * @return true-with settings, false-without settings
	 */
	public boolean getTestFlag() {
		return test_flag;
	}

	/**
	 * [-l | --list] flag setting method
	 *
	 * @param arg boolean Flag value
	 */
	public void setListFlag(boolean arg) {
		list_flag = arg;
	}
	/**
	 * [-l | --list] Flag setting acquisition method
	 *
	 * @return true-with settings, false-without settings
	 */
	public boolean getListFlag() {
		return list_flag;
	}

	/**
	 * [--append] Flag setting method
	 *
	 * @param arg boolean Flag value
	 */
	public void setAppendFlag(boolean arg) {
		append_flag = arg;
	}
	/**
	 * [--append] Flag setting acquisition method
	 *
	 * @return true-with settings, false-without settings
	 */
	public boolean getAppendFlag() {
		return append_flag;
	}

	/**
	 * [--replace] flag setting method
	 *
	 * @param arg boolean Flag value
	 */
	public void setReplaceFlag(boolean arg) {
		replace_flag = arg;
	}
	/**
	 * [--replace] Flag setting acquisition method
	 *
	 * @return true-with settings, false-without settings
	 */
	public boolean getReplaceFlag() {
		return replace_flag;
	}

	/**
	 * [--silent] flag setting method
	 *
	 * @param arg boolean Flag value
	 */
	public void setSilentFlag(boolean arg) {
		silent_flag = arg;
	}
	/**
	 * [--silent] Flag setting acquisition method
	 *
	 * @return true-with settings, false-without settings
	 */
	public boolean getSilentFlag() {
		return silent_flag;
	}

	/**
	 * [--verbose] flag setting method
	 *
	 * @param arg boolean Flag value
	 */
	public void setVerboseFlag(boolean arg) {
		verbose_flag = arg;
	}
	/**
	 * [--verbose] Flag setting acquisition method
	 *
	 * @return true-with settings, false-without settings
	 */
	public boolean getVerboseFlag() {
		return verbose_flag;
	}

	/**
	 * [--schemaCheckSkip] Flag setting method
	 *
	 * @param arg boolean Flag value
	 */
	public void setSchemaCheckSkipFlag(boolean arg) {
		schema_check_skip_flag = arg;
	}
	/**
	 * [--schemaCheckSkip] Flag setting acquisition method
	 *
	 * @return true-with settings, false-without settings
	 */
	public boolean getSchemaCheckSkipFlag() {
		return schema_check_skip_flag;
	}

	/**
	 * [--schemaOnly] flag setting method
	 *
	 * @param arg boolean Flag value
	 */
	public void setSchemaOnlyFlag(boolean arg) {
		this.schema_only_flag = arg;
	}

	/**
	 * [--schemaOnly] Flag setting acquisition method
	 *
	 * @return true-with settings, false-without settings
	 */
	public boolean getSchemaOnlyFlag() {
		return schema_only_flag;
	}

	/**
	 * インターバルを設定する。
	 *
	 * @param interval
	 */
	public void setIntervals(Date[] intervals) {
		this.intervals = intervals;
	}

	/**
	 * インターバルを返す。
	 *
	 * @return interval
	 */
	public Date[] getIntervals() {
		return intervals;
	}
	
	/**
	 * [--parallel] Set the input number of parallel execution
	 *
	 * @param count The input number of parallel execution
	 */
	public void setParallelInputCount(int count){
		m_parallelInputCount = count;
	}

	/**
	 *
	 * [--parallel] Get the input number of parallel execution
	 *
	 * @return The input number of parallel execution
	 */
	public int getParallelInputCount(){
		return m_parallelInputCount;
	}

	/**
	 * [--parallel] Set the output number of parallel execution
	 *
	 * @param count The input number of parallel execution
	 */
	public void setParallelOutputCount(int count){
		m_parallelOutputCount = count;
	}

	/**
	 * [--parallel] Get the output number of parallel execution
	 *
	 * @return The output number of parallel execution
	 */
	public int getParallelOutputCount(){
		return m_parallelOutputCount;
	}

	/**
	 * Get number max of job size
	 *
	 * @return Number max of job size
	 */
	public int getMaxJobSize() {
		return m_maxJobSize;
	}

	/**
	 * Set the number max ogf job size
	 *
	 * @param maxJobSize Number max of job size
	 */
	public void setMaxJobSize(int maxJobSize) {
		m_maxJobSize = maxJobSize;
	}

	/**
	 * Get the number max of job buffer size
	 *
	 * @return The number max of job buffer size
	 */
	public int getMaxJobBufferSize() {
		return m_maxJobBufferSize;
	}

	/**
	 * Set the number max of job buffer size
	 *
	 * @param maxJobBufferSize The number max of job buffer size
	 */
	public void setMaxJobBufferSize(int maxJobBufferSize) {
		m_maxJobBufferSize = maxJobBufferSize;
	}

	/**
	 * Get the store block size
	 *
	 * @return The store block size
	 */
	public String getStoreBlockSize() {
		return m_storeBlockSize;
	}

	/**
	 * Set the store block size
	 *
	 * @param storeBlockSize The store block size
	 */
	public void setStoreBlockSize(String storeBlockSize) {
		m_storeBlockSize = storeBlockSize;
	}

	/**
	 * Get the authentication method
	 *
	 * @return The authentication method
	 */
	public String getAuthenticationMethod() {
		return authenticationMethod;
	}

	/**
	 * Set the authentication method
	 *
	 * @param authenticationMethod The authentication method
	 */
	public void setAuthenticationMethod(String authenticationMethod) {
		this.authenticationMethod = authenticationMethod;
	}

	public String getSslMode() {
		return sslMode;
	}

	public void setSslMode(String sslMode) {
		this.sslMode = sslMode;
	}

	/**
	 * Get the notification interface address
	 *
	 * @return The notification interface address
	 */
	public String getNotificationInterfaceAddress() {
		return notificationInterfaceAddress;
	}

	/**
	 * Set the notification interface address
	 *
	 * @param notificationInterfaceAddress The notification interface address
	 */
	public void setNotificationInterfaceAddress(String notificationInterfaceAddress) {
		this.notificationInterfaceAddress = notificationInterfaceAddress;
	}

	/**
	 * コマンドintervalsオプションのTimeZoneを返す。
	 * @return
	 */
	public String getIntervalTimeZoneId() {
		return m_intervalTimeZoneId;
	}
	
	/**
	 * コマンドintervalsオプションのTimeZoneを設定する。
	 * @param intervalTimeZoneId
	 */
	public void setIntervalTimeZoneId(String intervalTimeZoneId) {
		m_intervalTimeZoneId = intervalTimeZoneId;
	}


	/**
	 * Detailed log display output (when --verbose is set)
	 *
	 * @param message
	 *            Log output string
	 */
	public void sysoutVerboseString(String message) {
		try {
			if (!silent_flag) {
				if (verbose_flag) {
					System.out.println(message);
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Normal log display output
	 *
	 * @param message
	 *            Log output string
	 */
	public void sysoutString(String message) {
		try {
			if (!silent_flag) {
				System.out.println(message);
			}
		} catch (Exception e) {
		}
	}

	public void sysoutStringNoLn(String message) {
		if (!silent_flag) {
			System.out.print(message);
		}
	}
}
