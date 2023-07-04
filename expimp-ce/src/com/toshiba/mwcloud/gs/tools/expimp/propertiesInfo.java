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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toshiba.mwcloud.gs.tools.common.AuthenticationMethod;
import com.toshiba.mwcloud.gs.tools.common.NotificationMode;
import com.toshiba.mwcloud.gs.tools.common.SslMode;
import com.toshiba.mwcloud.gs.tools.expimp.util.Utility;

/**
 * Property file read processing method
 *
 *
 */
public class propertiesInfo {

	private int m_parallelInput;
	private int m_parallelOutput;

	private int m_maxJobSize;
	private int m_maxJobBufferSize;

	private String m_storeBlockSize;


	/**
	 * Internationalized message resource
	 */
	private static ResourceBundle messageResource;

	/**
	 * Property file name
	 */
	private final String propertiesFile = "gs_expimp.properties";

	/**
	 * Property
	 */
	private Properties configuration;

	/**
	 * Logger class settings
	 */
	private static final Logger log = LoggerFactory.getLogger(propertiesInfo.class);



	/**
	 * Constructor
	 */
	public propertiesInfo() {
	}

	public void loadConfig(commandLineInfo ci) throws Exception {

		InputStream inputStream = null;
		SimpleDateFormat sdf = new SimpleDateFormat(GSConstants.DATE_INTERVAL_FORMAT);//インターバルのデータフォーマット
		try {
			configuration = new Properties();
			messageResource = Utility.getResource();

			// Find the properties file in the classpath
			inputStream = propertiesInfo.class.getClassLoader().getResourceAsStream(propertiesFile);

			if ( inputStream == null ){
				// If the file does not exist, it will be created currently with the default value.
				// Proceed with the default value as it is
				writeProperties(ci);

			} else {
				// Property file reading
				configuration.load(inputStream);

				// Connection method
				NotificationMode notificationMode = ci.getNotificationMode();
				String notificationModeStr = configuration.getProperty(GSConstants.PROP_NOTIFICATION_MODE);
				if ( notificationModeStr != null && !notificationModeStr.isEmpty() ){
					try {
						notificationMode = NotificationMode.valueOf(notificationModeStr.toUpperCase());
						ci.setNotificationMode(notificationMode);
					} catch ( Exception e ){
						throw new GSEIException(messageResource.getString("MESS_COMM_ERR_PROPINFO_1")+" ('mode' is invalid.)", null);
					}
				}

				// NoSQL multicast
				String hostAddress = configuration.getProperty("hostAddress", ci.getServerAddress().toString());
				String strHostPort = configuration.getProperty("hostPort");
				int hostPort = ci.getPort();
				if ( (strHostPort != null) && !strHostPort.isEmpty() ){
					hostPort = Integer.parseInt(strHostPort);
				}

				// NewSQL Multicast
				String jdbcAddress = configuration.getProperty(GSConstants.PROP_JDBC_ADDRESS, ci.getJdbcAddress().toString());
				String strJdbcPort = configuration.getProperty(GSConstants.PROP_JDBC_PORT);
				int jdbcPort = ci.getJdbcPort();
				if ( (strJdbcPort != null) && !strJdbcPort.isEmpty() ){
					jdbcPort = Integer.parseInt(strJdbcPort);
				}

				// NoSQL node address (for extension)
				String restHostname = configuration.getProperty("restAddress", ci.getRestAddress().toString());
				String strRestPort = configuration.getProperty("restPort");
				int restPort = ci.getRestPort();
				if ( (strRestPort != null) && !strRestPort.isEmpty() ){
					restPort = Integer.parseInt(strRestPort);
				}
				ci.setRestAddress(restHostname);
				ci.setRestPort(restPort);

				// NoSQL fixed list
				String transactionMember = configuration.getProperty("notificationMember", ci.getTransactionMember());

				// NewSQL fixed list
				String jdbcNotificationMember = configuration.getProperty(GSConstants.PROP_JDBC_NOTIFICATION_MEMBER, ci.getJdbcTransactionMember());

				// NoSQL / NewSQL provider
				String providerURL = configuration.getProperty("notificationProvider.url", ci.getProviderURL());

				// Cluster name
				String clusterName = configuration.getProperty("clusterName", ci.getClusterName());
				ci.setClusterName(clusterName);

				// Check connection information
				switch(notificationMode){
				case MULTICAST:
					if ( hostAddress == null || hostAddress.length() == 0 || hostPort == 0 ){
						throw new GSEIException(messageResource.getString("MESS_COMM_ERR_PROPINFO_1")+" ('hostAddress' and 'hostPort' are required if mode is MULTICAST.)", null);
					}
					ci.setServerAddress(InetAddress.getByName(hostAddress));
					ci.setPort(hostPort);

					if ( ci.isJdbcEnabled() ) {
						if ( jdbcAddress == null || jdbcAddress.isEmpty() || jdbcPort == 0 ) {
							throw new GSEIException(messageResource.getString("MESS_COMM_ERR_PROPINFO_1")+" ('jdbcAddress' and 'jdbcPort' are required if mode is MULTICAST.)", null);
						}
						ci.setJdbcAddress(InetAddress.getByName(jdbcAddress));
						ci.setJdbcPort(jdbcPort);
					}
					break;

				case FIXED_LIST:
					if ( transactionMember == null || transactionMember.length() == 0 ){
						throw new GSEIException(messageResource.getString("MESS_COMM_ERR_PROPINFO_1")+"('notificationMember' is required if mode is FIXED_LIST.)", null);
					}
					ci.setTransactionMember(transactionMember);

					if ( ci.isJdbcEnabled() ) {
						if ( jdbcNotificationMember == null || jdbcNotificationMember.isEmpty()) {
							throw new GSEIException(messageResource.getString("MESS_COMM_ERR_PROPINFO_1")+"('jdbcNotificationMember' is required if mode is FIXED_LIST.)", null);
						}
						ci.setJdbcTransactionMember(jdbcNotificationMember);
					}
					break;

				case PROVIDER:
					if ( providerURL == null || providerURL.length() == 0 ){
						throw new GSEIException(messageResource.getString("MESS_COMM_ERR_PROPINFO_1")+"('notificationProvider.url' is required if mode is PROVIDER.)", null);
					}
					ci.setProviderURL(providerURL);
					break;
				}

				// Transaction timeout
				String transactionTimeoutStr = configuration.getProperty("transactionTimeout");
				if ( (transactionTimeoutStr != null) && !transactionTimeoutStr.isEmpty() ){
					try {
						int transactionTimeout = Integer.parseInt(transactionTimeoutStr);
						ci.setTransactionTimeout(transactionTimeout);
					} catch ( NumberFormatException e ){
						throw new GSEIException(messageResource.getString("MESS_COMM_ERR_PROPINFO_1")+"('transactionTimeout' must be an integer.)", e);
					}
				}

				// Failover timeout
				String failoverTimeoutStr = configuration.getProperty("failoverTimeout");
				if ( (failoverTimeoutStr != null) && !failoverTimeoutStr.isEmpty() ){
					try {
						int failoverTimeout = Integer.parseInt(failoverTimeoutStr);
						ci.setFailoverTimeout(failoverTimeout);
					} catch ( NumberFormatException e ){
						throw new GSEIException(messageResource.getString("MESS_COMM_ERR_PROPINFO_1")+"('failoverTimeout' must be an integer.)", e);
					}
				}

				// JDBC login timeout
				String strLoginTimeout = configuration.getProperty(GSConstants.PROP_JDBC_LOGIN_TIMEOUT);
				if ( (strLoginTimeout != null) && !strLoginTimeout.isEmpty() ){
					try {
						int jdbcLoginTimeout = Integer.parseInt(strLoginTimeout);
						ci.setJdbcLoginTimeout(jdbcLoginTimeout);
					} catch ( NumberFormatException e ){
						throw new GSEIException(messageResource.getString("MESS_COMM_ERR_PROPINFO_1")+"('jdbcLoginTimeout' must be an integer.)", e);
					}
				}

				// Consistency
				String consistency = configuration.getProperty(GSConstants.PROP_CONSISTENCY, ci.getConsistency());
				ci.setConsistency(consistency);

				// Container cache size
				String cacheSize = configuration.getProperty(GSConstants.PROP_CONTAINER_CHACHE_SIZE);
				if ( (cacheSize != null) && !cacheSize.isEmpty()){
					try {
						Integer.parseInt(cacheSize);
					} catch ( NumberFormatException e ){
						throw new GSEIException(messageResource.getString("MESS_COMM_ERR_PROPINFO_1")+"('containerCacheSize' must be an integer.)", e);
					}
					ci.setContainerCacheSize(cacheSize);
				}

				// Fetch size
				String strFetchBytesSize = configuration.getProperty("fetchBytesSize");
				if ( (strFetchBytesSize != null) && !strFetchBytesSize.isEmpty()){
					try {
						Integer.parseInt(strFetchBytesSize);
					} catch ( NumberFormatException e ){
						throw new GSEIException(messageResource.getString("MESS_COMM_ERR_PROPINFO_1")+"('fetchBytesSize' must be an integer.)", e);
					}
					ci.setFetchBytesSize(strFetchBytesSize);
				}

				// Commit count
				String commitCountStr = configuration.getProperty("commitCount");
				if ( (commitCountStr != null) && !commitCountStr.isEmpty() ){
					try {
						int commitCount = Integer.parseInt(commitCountStr);
						ci.setCommitCount(commitCount);
					} catch ( NumberFormatException e ){
						throw new GSEIException(messageResource.getString("MESS_COMM_ERR_PROPINFO_1")+"('commitCount' must be an integer.)", e);
					}
				}

				// export.storeMemoryAgingSwapRate
				String exportStoreMemoryAgingSwapRate = configuration.getProperty(GSConstants.PROP_EXPORT_AGING_SWAP_RATE);
				if ( (exportStoreMemoryAgingSwapRate != null) && !exportStoreMemoryAgingSwapRate.isEmpty()){
					try {
						Double.parseDouble(exportStoreMemoryAgingSwapRate);
					} catch ( NumberFormatException e ){
						throw new GSEIException(messageResource.getString("MESS_COMM_ERR_PROPINFO_1")+"('export.storeMemoryAgingSwapRate' must be a double value.)", e);
					}
					ci.setExportStoreMemoryAgingSwapRate(exportStoreMemoryAgingSwapRate);
				}

//				m_jdbcDriverPath = configuration.getProperty(GSConstants.PROP_JDBC_DRIVER_PATH);


				m_parallelInput = Integer.parseInt(configuration.getProperty(GSConstants.PROP_PARALLEL_INPUT, String.valueOf(ci.getParallelInputCount())));
				m_parallelOutput = Integer.parseInt(configuration.getProperty(GSConstants.PROP_PARALLEL_OUTPUT, String.valueOf(ci.getParallelOutputCount())));

				m_maxJobSize = Integer.parseInt(configuration.getProperty(GSConstants.PROP_MAX_JOB_SIZE, String.valueOf(ci.getMaxJobSize())));
				m_maxJobBufferSize = Integer.parseInt(configuration.getProperty(GSConstants.PROP_MAX_JOB_BUFFER_SIZE, String.valueOf(ci.getMaxJobBufferSize())));

				m_storeBlockSize = configuration.getProperty(GSConstants.PROP_STORE_BLOCK_SIZE, ci.getStoreBlockSize());

				// Set

				ci.setParallelInputCount(m_parallelInput);
				ci.setParallelOutputCount(m_parallelOutput);
				ci.setMaxJobSize(m_maxJobSize);
				ci.setMaxJobBufferSize(m_maxJobBufferSize);
				ci.setStoreBlockSize(m_storeBlockSize);

				// Authentication method (internal authentication)
				String authenticationMethod = configuration.getProperty(GSConstants.PROP_AUTHENTICATION_METHOD);
				if (authenticationMethod != null) {
					// Error if the authentication method setting value is invalid
					try {
						AuthenticationMethod.valueOf(authenticationMethod);
					} catch (IllegalArgumentException e) {
						throw new GSEIException(messageResource.getString("MESS_COMM_ERR_PROPINFO_1")+" ('authenticationMethod' is invalid.)");
					}
					ci.setAuthenticationMethod(authenticationMethod);
				}

				// SSL接続設定
				String sslMode = configuration.getProperty(GSConstants.PROP_SSL_MODE);
				if (sslMode != null) {
					// V4.6 プロパティファイルの設定値がREQUIREDの場合、DB接続時に指定するsslModeの設定はPREFERREDにする
					if (sslMode.equals(GSConstants.PROP_VALUE_SSL_MODE_REQUIRED)) {
						sslMode = GSConstants.PROP_VALUE_SSL_MODE_PREFERRED;
					}
					// SSL接続設定の設定値が不正値の場合エラー
					try {
						SslMode.valueOf(sslMode);
					} catch (IllegalArgumentException e) {
						throw new GSEIException(messageResource.getString("MESS_COMM_ERR_PROPINFO_1")+" ('sslMode' is invalid.)");
					}
					ci.setSslMode(sslMode);
				} else {
					// V4.6 デフォルト値はDISABLED
					/* CE version: 145005:JC_ILLEGAL_PROPERTY_ENTRY] Unacceptable property specified because of lack of extra library (key=sslMode) */
					//ci.setSslMode(GSConstants.PROP_VALUE_SSL_MODE_DISABLED);
				}

				// 複数NIC I/F指定対応
				String notificationInterfaceAddress = configuration.getProperty(GSConstants.PROP_NOTIFICATION_INTERFACE_ADDRESS);
				if (notificationInterfaceAddress != null) {
					ci.setNotificationInterfaceAddress(notificationInterfaceAddress);
				}

				// Interval TimeZone設定
				String intervalTimeZoneId = configuration.getProperty(GSConstants.PROP_INTERVAL_TIMEZONE);
				if (intervalTimeZoneId != null && intervalTimeZoneId.length() > 0) {
					String timeZoneId = intervalTimeZoneId;
					// TimeZoneのバリデーション
					if (!timeZoneId.equals(TimeZone.getTimeZone(timeZoneId).getID())) {
						throw new GSEIException(messageResource.getString("MESS_COMM_ERR_CMD_57")+ ":[" + intervalTimeZoneId + "]");// "プロパティ[intervalTimeZone]の値が不正です (タイムゾーン名またはGMT+HH:mm形式で指定してください)"						
					}
					try {
						// TimeZoneのバリデーション
						sdf.setTimeZone(TimeZone.getTimeZone(timeZoneId));
					} catch (Exception e) {
						throw new GSEIException(messageResource.getString("MESS_COMM_ERR_CMD_57")+ ":[" + intervalTimeZoneId + "]", e);// "プロパティ[intervalTimeZone]の値が不正です (タイムゾーン名またはGMT+HH:mm形式で指定してください)"
					}
					ci.setIntervalTimeZoneId(timeZoneId);
				}				
			}

			String msg = "Property :";
			switch(ci.getNotificationMode()){
			case MULTICAST:
				msg += " hostAddress=["+ci.getServerAddress().getHostAddress()+"] hostPort=["+ci.getPort()+"]";
				if ( ci.isJdbcEnabled() ) msg += " jdbcAddress=["+ci.getJdbcAddress().getHostAddress()+"] jdbcPort=["+ci.getJdbcPort()+"]";
				break;
			case FIXED_LIST:
				msg += " notificationMember=[" + ci.getTransactionMember() + "]";
				if ( ci.isJdbcEnabled() ) msg += " jdbcNotificationMember=["+ci.getJdbcTransactionMember()+"]";
				break;
			case PROVIDER:
				msg += " notificationProvider.url=[" + ci.getProviderURL() + "]";
				break;
			}
			msg += " clusterName=["+ci.getClusterName()
					+"] commitCount=["+ci.getCommitCount()
					+"] transactionTimeout=["+ci.getTransactionTimeout()
					+"] failoverTimeout=["+ci.getFailoverTimeout()+"]";

			if ( ci.isJdbcEnabled() ) msg += " jdbcLoginTimeout=[" + ci.getJdbcLoginTimeout() + "]";
			if ( ci.getConsistency() != null ) msg += " consistency=["+ci.getConsistency()+"]";
			if ( ci.getContainerCacheSize() != null ) msg += " containerCacheSize=["+ci.getContainerCacheSize()+"]";
			if ( ci.getFetchBytesSize() != null ) msg += " fetchBytesSize=["+ci.getFetchBytesSize()+"]";

			if (ci.getCmdName().equals(GSConstants.CMD_NAME.GS_EXPORT.toString())) {
				msg += " export.storeMemoryAgingSwapRate=[" + ci.getExportStoreMemoryAgingSwapRate() + "]";
			}

			log.info(msg);

		} catch ( GSEIException e ){
			throw e;
		} catch (Exception e) {
			// "An error occurred in the property reading process"
			log.error(messageResource.getString("MESS_COMM_ERR_PROPINFO_1")+ ":"+ e.getMessage(), e);
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_PROPINFO_1"), e);

		}finally{
			try{
				if(inputStream != null){
					inputStream.close();
				}
			}catch(Exception e){}
		}
	};


	private void writeProperties(commandLineInfo ci){
		// "The property file was not found. Create a property file."
		log.error(messageResource.getString("MESS_COMM_ERR_PROPINFO_2"));

		try {
			OutputStream outputStream = new FileOutputStream(new File(".", propertiesFile));
			configuration.setProperty("mode", ci.getNotificationMode().toString());
			configuration.setProperty("hostAddress", ci.getServerAddress().toString());
			configuration.setProperty("hostPort", Integer.toString(ci.getPort()));
			configuration.setProperty("jdbcAddress", ci.getJdbcAddress().toString());
			configuration.setProperty("jdbcPort", Integer.toString(ci.getJdbcPort()));
			configuration.setProperty("clusterName", ci.getClusterName());
			configuration.setProperty("commitCount",Integer.toString(ci.getCommitCount()));
			configuration.setProperty("transactionTimeout",Integer.toString(ci.getTransactionTimeout()));
			configuration.setProperty("failoverTimeout",Integer.toString(ci.getFailoverTimeout()));
			configuration.setProperty("jdbcLoginTimeout",Integer.toString(ci.getJdbcLoginTimeout()));
			configuration.setProperty("intervalTimeZone", ci.getIntervalTimeZoneId());

			configuration.store(outputStream, "propertiesInfo constructer");
			outputStream.flush();
			outputStream.close();

		} catch (Exception e) {
			// "There was an error writing the properties file."
			log.error(messageResource.getString("MESS_COMM_ERR_PROPINFO_3"), e);
		}

	}
}
