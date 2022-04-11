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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Command progress class
 *
 *
 */
public class commandProgressStatus {

	/**
	 * Command parameter information class
	 */
	private static commandLineInfo cmdLineInfo = null;

	/**
	 * Internationalized message resource
	 */
	private static ResourceBundle messageResource;

	/**
	 * Logger class settings
	 */
	private static final Logger log = LoggerFactory.getLogger(commandProgressStatus.class);

	/** Number of target containers */
	private static int m_containerCount;
	/** Number of successes */
	private static int m_successCount;
	/** Number of failures */
	private static int m_errorCount;
	/** Number in process */
	private static int m_inProgressCount;
	/** Number of skips */
	private static int m_skipCount;

	/** Error message list */
	private static List<String> m_errorMsgList;

	/** List of skip messages */
	private static List<String> m_skipMsgList;

	/** Number of target views */
	private static int m_viewCount;
	/** Number of successful views */
	private static int m_successViewCount;
	/** Number of view failures */
	private static int m_errorViewCount;
	/** Number of view skips */
	private static int m_skipViewCount;

	/** List of invalid view message */
	private static List<String> m_invalidViewMsgList;

	/**
	 * Constructor
	 */
	public static void setCommandLineInfo(commandLineInfo cli) {
		cmdLineInfo = cli;
		// Message resource initialization
		messageResource = ResourceBundle.getBundle(GSConstants.MESSAGE_RESOURCE);

		m_containerCount = 0;
		m_successCount = 0;
		m_errorCount = 0;
		m_skipCount = 0;
		m_errorMsgList = new ArrayList<String>();
		m_skipMsgList = new ArrayList<String>();

		m_viewCount = 0;
		m_successViewCount = 0;
		m_errorViewCount = 0;
		m_skipViewCount = 0;
		m_invalidViewMsgList = new ArrayList<String>();
	}


	/**
	 * Processing summary display
	 *
	 */
	public static void printStatus() {

		if (!cmdLineInfo.getSilentFlag()) {
			if ( m_containerCount != 0 ){
				// Unprocessed number
				int notExecute = m_containerCount-(m_successCount+m_errorCount);
				String countMsg = "";
				if ( m_containerCount == -1 ){
					if ( m_inProgressCount == 0 ){
						// Number of containers: Success:%d Failure:%d
						countMsg = String.format(messageResource.getString("MESS_COMM_PROC_PROCINFO_A"),m_successCount, m_errorCount);
					} else {
						// Number of containers: Success:%d Failure:%d In progress:%d
						countMsg = String.format(messageResource.getString("MESS_COMM_PROC_PROCINFO_B"),m_successCount, m_errorCount, m_inProgressCount);
					}
				} else if ( notExecute == 0 ){
					// Number of target containers:%d (success:%d failure:%d)
					countMsg = String.format(messageResource.getString("MESS_COMM_PROC_PROCINFO_5"),m_containerCount,m_successCount, m_errorCount);
				} else {
					// Number of target containers:%d (success:%d failure:%d unprocessed:%d)
					countMsg = String.format(messageResource.getString("MESS_COMM_PROC_PROCINFO_7"),m_containerCount, m_successCount, m_errorCount, notExecute);
				}
				System.out.println();
				System.out.println(countMsg);
				log.info(countMsg);

				if ( !m_skipMsgList.isEmpty() ){
					// [Skip list]
					System.out.println(messageResource.getString("MESS_COMM_PROC_PROCINFO_C"));
					for ( String msg : m_skipMsgList ) {
						System.out.println(msg);
					}
				}
				if ( !m_errorMsgList.isEmpty() ){
					// [Error list]
					System.out.println(messageResource.getString("MESS_COMM_PROC_PROCINFO_8"));
					for ( String msg : m_errorMsgList ){
						System.out.println(msg);
					}
				}

				if ( m_successViewCount > 0 ) {
					System.out.println();
					// Number of target views : %d
					System.out.println(String.format(messageResource.getString("MESS_COMM_PROC_PROCINFO_D"), m_successViewCount));

					if ( !m_invalidViewMsgList.isEmpty() ) {
						// [Invalid view list]
						System.out.println(messageResource.getString("MESS_COMM_PROC_PROCINFO_E"));
						for ( String msg : m_invalidViewMsgList ){
							System.out.println(msg);
						}
					}
				}
			}
		}
	}

	/**
	 * Set the number of target containers.
	 *
	 * @param containerCount Number of containers
	 */
	public static void setContainerCount(int containerCount) {
		m_containerCount = containerCount;
	}


	/**
	 * Sets the processing status of the container. (Parallel access possible)
	 * <p>
	 * If resultStatus is false, a message is set, and the container name is null,
	 * it is treated as an unprocessed container.
	 *
	 * @param containerName Container name
	 * @param resultStatus true: success / false: failure
	 * @param errMsg Error message
	 */
	public static synchronized void setContainerStatus(String containerName, boolean resultStatus, String errMsg ){
		if ( resultStatus ){
			m_successCount++;
		} else {

			if ( errMsg == null ){
				m_errorMsgList.add(containerName);
				m_errorCount++;
			} else {
				if ( containerName == null ){
					m_errorMsgList.add(errMsg);
				} else {
					m_errorMsgList.add(containerName+" : " +errMsg);
					m_errorCount++;
				}
			}
		}
	}


	/**
	 * Add the number of successfully processed containers. (Parallel access not possible)
	 *
	 * @param containerCount Number of successful containers
	 */
	public static void addContainerSuccessCount(int containerCount){
		m_successCount += containerCount;
	}
	/**
	 * Add the number of successfully processed containers. (Parallel access possible)
	 *
	 * @param containerCount Number of successful containers
	 */
	public static synchronized void addContainerSuccessCountThreadSafe(int containerCount){
		m_successCount += containerCount;
	}

	public static void addContainerInProgressCount(int containerCount){
		m_inProgressCount += containerCount;
	}

	/**
	 * Returns the number of containers that have been successfully processed.
	 *
	 * @return Number of successful containers
	 */
	public static int getContainerSuccessCount(){
		return m_successCount;
	}

	public static int getContainerErrorCount(){
		return m_errorCount;
	}

	/**
	 * Returns command line information.
	 *
	 * @return Command line information
	 */
	public static commandLineInfo getCommandLineInfo(){
		return cmdLineInfo;
	}


	/**
	 * Add the number of skipped containers. (Parallel access is not possible)
	 * <p>
	 * V3.5 Used only for skipping partitioned containers.
	 *
	 * @param Number of containers skipped
	 */
	public static void addSkipCount(int containerCount) {
		m_skipCount += containerCount;
	}

	/**
	 * Returns the number of skipped containers.
	 * <p>
	 * V3.5 Used only for skipping partitioned containers.
	 *
	 * @return Number of containers skipped
	 */
	public static int getContainerSkipCount() {
		return m_skipCount;
	}

	/**
	 * Add a skip message. (Parallel access is not possible)
	 * <p>
	 * V3.5 Used only for skipping partitioned containers.
	 */
	public static void addSkipMessage(String message) {
		m_skipMsgList.add(message);
	}

	/**
	 * Adds the number of views that have been successfully processed. (Parallel access not possible)
	 *
	 * @param viewCount Number of successful views
	 */
	public static void addViewSuccessCount(int viewCount){
		m_successViewCount += viewCount;
	}

	/**
	 * Add a view unreferenceable message. (Parallel access is not possible)
	 */
	public static void addInvalidViewMessage(String message) {
		m_invalidViewMsgList.add(message);
	}

}
