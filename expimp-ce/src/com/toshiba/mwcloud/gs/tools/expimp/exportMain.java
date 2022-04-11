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
import java.util.Date;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toshiba.mwcloud.gs.tools.expimp.GSConstants.CMD_NAME;
import com.toshiba.mwcloud.gs.tools.expimp.util.Utility;


/**
 * Export command MAIN class
 *
 *
 */
public class exportMain {
	/**
	 * Internationalized message resource
	 */
	private static ResourceBundle messageResource;
	/**
	 * Logger class settings
	 */
	private static final Logger log = LoggerFactory.getLogger(exportMain.class);

	/** Execution start date and time (output to JSON file)*/
	static Date m_startTime;

	/**
	 * gs_export MAIN method
	 *
	 * @param args Input parameter string
	 */
	public static void main(String[] args) {
		boolean status = false;
		boolean silent = false;

		try {
			long startTime = System.currentTimeMillis();
			m_startTime = new Date();

			// Message resource initialization
			messageResource = Utility.getResource();

			// Start exporting
			log.info(messageResource.getString("MESS_EXPORT_PROC_EXPORTMAIN_1") +" :Version "+
					messageResource.getString("MESS_TOOL_VERSION"));

			//------------------------------
			// Check options
			//------------------------------
			// Analysis of options and arguments
			cmdAnalyze ca = new cmdAnalyze();
			commandLineInfo cli = ca.analyzeParameter(CMD_NAME.GS_EXPORT, args);
			if ( cli == null ){
				System.exit(1);	// Message output has already been performed in the lower layer.
			}
			commandProgressStatus.setCommandLineInfo(cli);

			// Start exporting
			cli.sysoutString(messageResource.getString("MESS_EXPORT_PROC_EXPORTMAIN_1"));
			if ( cli.getTestFlag() ){
				cli.sysoutString(messageResource.getString("MESS_TEST_MODE"));
			}

			//------------------------------
			// Start export process
			//------------------------------
			exportProcess ep = new exportProcess(cli);
			status = ep.start();

			// Display the number of results
			commandProgressStatus.printStatus();

			long endTime = System.currentTimeMillis();

			// Export finished
			cli.sysoutString(messageResource.getString("MESS_EXPORT_PROC_EXPORTMAIN_2"));
			log.info(messageResource.getString("MESS_EXPORT_PROC_EXPORTMAIN_2")+": time=["+(endTime-startTime)+"]");


		} catch (Exception e) {
			if (!silent) {
				// An error occurred during the export process
				System.out.println(messageResource.getString("MESS_EXPORT_ERR_EXPORTMAIN_1")+ ":"+ e.getMessage());
			}
			log.error(messageResource.getString("MESS_EXPORT_ERR_EXPORTMAIN_1"), e);
			status = false;

		} catch ( NoClassDefFoundError error){
			// The client api (gridstore.jar) version may be older
			String errMsg = messageResource.getString("MESS_EXPORT_ERR_EXPORTMAIN_2");
			if ( error.getMessage().indexOf("com/toshiba/mwcloud/gs/") < 0 ){
				// A Required class does not found
				errMsg = messageResource.getString("MESS_EXPORT_ERR_EXPORTMAIN_3")+ ": className=["+ error.getMessage()+"]";
			}
			if (!silent) System.out.println(errMsg);
			log.error(errMsg, error);
			status = false;
		}

		if ( status ){
			System.exit(0);
		} else {
			System.exit(1);
		}

	}

}
