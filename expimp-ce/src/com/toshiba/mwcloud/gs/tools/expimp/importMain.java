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
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toshiba.mwcloud.gs.tools.expimp.GSConstants.CMD_NAME;
import com.toshiba.mwcloud.gs.tools.expimp.GSConstants.TARGET_TYPE;
import com.toshiba.mwcloud.gs.tools.expimp.util.Utility;


/**
 * Import command MAIN class
 *
 *
 */
public class importMain {
	/**
	 * Internationalized message resource
	 */
	private static ResourceBundle messageResource;
	/**
	 * Logger class settings
	 */
	private static final Logger log = LoggerFactory.getLogger(importMain.class);

	/**
	 * MAIN method
	 *
	 * @param args Input parameter string
	 */
	public static void main(String[] args) {
		boolean status = false;
		boolean silent = false;

		try {
			long startTime = System.currentTimeMillis();

			// Message resource initialization
			messageResource = Utility.getResource();

			// Start importing
			log.info(messageResource.getString("MESS_IMPORT_PROC_IMPORTMAIN_1") +" :Version "+
					messageResource.getString("MESS_TOOL_VERSION"));

			//------------------------------
			// Check options
			//------------------------------
			cmdAnalyze ca = new cmdAnalyze();
			commandLineInfo cli = ca.analyzeParameter(CMD_NAME.GS_IMPORT, args);
			if ( cli == null ){
				System.exit(1);	// Message output has already been performed in the lower layer.
			}
			commandProgressStatus.setCommandLineInfo(cli);

			if (cli.getAppendFlag()) {
				// Append Mode	Start importing (addition mode)
				cli.sysoutString(messageResource.getString("MESS_IMPORT_PROC_IMPORTMAIN_2"));
			} else if (cli.getReplaceFlag()) {
				// Replace Mode	Start import (relocation mode)
				cli.sysoutString(messageResource.getString("MESS_IMPORT_PROC_IMPORTMAIN_3"));
			} else if (cli.getListFlag()) {
				// List Mode	Display a container list of export data on the local disk
				cli.sysoutString(messageResource.getString("MESS_IMPORT_PROC_IMPORTMAIN_5"));
			} else {
				// Normal Mode	Start importing
				cli.sysoutString(messageResource.getString("MESS_IMPORT_PROC_IMPORTMAIN_1"));
			}
			if ( cli.getTestFlag() ){
				cli.sysoutString(messageResource.getString("MESS_TEST_MODE"));
			}

			//------------------------------
			// Start import process
			//------------------------------
			// Separate processing from export files and from external data sources
			if ( cli.getTargetType() != TARGET_TYPE.DATASOURCE ){
				importProcess ip = new importProcess(cli);
				status = ip.start();

				if (cli.getListFlag() == false) {
					// Display of the number of results
					commandProgressStatus.printStatus();

					long endTime = System.currentTimeMillis();

					// Import finished
					cli.sysoutString(messageResource.getString("MESS_IMPORT_PROC_IMPORTMAIN_4"));
					log.info(messageResource.getString("MESS_IMPORT_PROC_IMPORTMAIN_4")+": time=["+(endTime-startTime)+"]");
				}
			}

		} catch ( GSEIException e ){
			if (!silent) {
				System.out.println(e.getMessage());
			}
			log.error(e.getMessage(), e);
			status = false;

		} catch (Exception e) {
			if (!silent) {
				// An error occurred during the import process
				System.out.println(messageResource.getString("MESS_IMPORT_ERR_IMPORTMAIN_1")+ ":"+ e.getMessage());
			}
			log.error(messageResource.getString("MESS_IMPORT_ERR_IMPORTMAIN_1")+ ":" + e.getMessage(), e);
			status = false;

		} catch ( NoClassDefFoundError error){
			String errMsg = messageResource.getString("MESS_IMPORT_ERR_IMPORTMAIN_2");
			if ( error.getMessage().indexOf("com/toshiba/mwcloud/gs/") < 0 ){
				errMsg = messageResource.getString("MESS_IMPORT_ERR_IMPORTMAIN_3")+ ": className=["+ error.getMessage()+"]";
			}
			if (!silent) System.out.println(errMsg);
			log.error(errMsg, error);
			status = false;
		} catch (Error error) {
			if (!silent) {
				// An error occurred during the import process
				System.out.println(messageResource.getString("MESS_IMPORT_ERR_IMPORTMAIN_4"));
			}
			log.error(messageResource.getString("MESS_IMPORT_ERR_IMPORTMAIN_4"), error);
			status = false;
		}

		if ( status ){
			System.exit(0);
		} else {
			System.exit(1);
		}

	}

}
