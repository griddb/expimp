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
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toshiba.mwcloud.gs.tools.common.data.MetaContainerFileIO;
import com.toshiba.mwcloud.gs.tools.common.data.ToolConstants;
import com.toshiba.mwcloud.gs.tools.expimp.util.Utility;

/**
 * Export Management file I/O class.
 *
 *
 */
public class metaInformationFileIO {
	/**
	 * Internationalized message resource
	 */
	private static ResourceBundle messageResource;

	/**
	 * Command parameter information class
	 */
	private commandLineInfo comLineInfo;

	/**
	 * Logger class settings
	 */
	private static final Logger log = LoggerFactory.getLogger(metaInformationFileIO.class);


	/**
	 * Database name to be imported
	 */
	private Set<String> m_databaseList;

	/**
	 * Constructor
	 *
	 * @param cli Command parameter information class
	 */
	public metaInformationFileIO(commandLineInfo cli) {
		this.comLineInfo = cli;
		messageResource = Utility.getResource();
	}


	//********************************************************
	// Writing
	//********************************************************
	/**
	 * Export Output the management file (gs_export.json).
	 *
	 * @param containerInfoList Container information object list
	 */
	public void writeExportManagerFile(List<GSEIContInfo> containerInfoList) throws GSEIException {

		// Exit if there is no target container
		if ( containerInfoList == null || containerInfoList.isEmpty() ){
			return;
		}

		String filePath = "";
		try {
			// JSON file output
			File file = new File(comLineInfo.getDirectoryPath(), GSConstants.FILE_GS_EXPORT_JSON);
			filePath = file.getAbsolutePath();

			if (comLineInfo.getTestFlag()) {
				// Outputs the Export management information in the file [test mode]
				String msg = messageResource.getString("MESS_EXPORT_PROC_EXPORTPROC_18")+ ":" + file.getCanonicalPath();
				comLineInfo.sysoutVerboseString(msg);
				log.info(msg);
				return;
			}

			Map<String, Object> properties = new HashMap<String, Object>(1);
			properties.put(JsonGenerator.PRETTY_PRINTING, true);
			JsonGeneratorFactory factory = Json.createGeneratorFactory(properties);
			JsonGenerator gen = factory.createGenerator(new OutputStreamWriter(
					new FileOutputStream(file), ToolConstants.ENCODING_JSON));
			SimpleDateFormat df = new SimpleDateFormat(GSConstants.DATE_FORMAT);

			gen.writeStartObject();

			gen.write(GSConstants.JSON_EXP_VERSION, GSConstants.EXPORT_MNG_FILE_VERSION);
			gen.write(GSConstants.JSON_EXP_STARTTIME, df.format(exportMain.m_startTime));
			gen.write(GSConstants.JSON_EXP_ENDTTIME, df.format(new Date()));
			gen.write(GSConstants.JSON_EXP_ADDRESS, comLineInfo.getServerAddress().getHostAddress()+":"+comLineInfo.getPort());
			gen.write(GSConstants.JSON_EXP_USER, comLineInfo.getUserName());
			gen.write(GSConstants.JSON_EXP_CONTAINER_COUNT, containerInfoList.size());
			gen.write(GSConstants.JSON_EXP_ROW_FILETYPE, comLineInfo.getRowFileType().toString().toLowerCase());
			gen.write(GSConstants.JSON_EXP_PARALLEL_COUNT, comLineInfo.getParallelCount());

			// Container information output
			gen.writeStartArray(GSConstants.JSON_EXP_CONTAINER);
			for ( int i = 0; i < containerInfoList.size(); i++ ){
				GSEIContInfo contInfo = containerInfoList.get(i);
				gen.writeStartObject();
				if ( contInfo.getDbName() != null ) gen.write(GSConstants.JSON_EXP_DATABASE, contInfo.getDbName());
				gen.write(GSConstants.JSON_EXP_CONTAINER_NAME, contInfo.getContainerName());
				gen.write(GSConstants.JSON_EXP_CONTAINER_FILE, contInfo.getMetaFileName());

				if( contInfo.getFilterCondition()!=null ){
					gen.write(GSConstants.JSON_FILTER_CONDITION, contInfo.getFilterCondition());
				}
				gen.writeEnd();
				gen.flush();
			}
			gen.writeEnd();

			gen.writeEnd();
			gen.flush();
			gen.close();

		} catch (Exception e){
			// An error occurred while writing the Export management file
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_METAINFO_33")
					+": path=["+filePath+"]", e);
		}
	}


	//********************************************************
	// Read
	//********************************************************
	/**
	 * Export Load the management file (gs_export.json).
	 *
	 * @return Container information list
	 * @throws GSEIException
	 */
	public List<GSEIContInfo> readExportManagerFile() throws GSEIException {
		JsonReader jr = null;
		FileInputStream f = null;
		String filePath = "";
		try {
			// Confirmation of existence of Export management file
			File fileObj = new File(comLineInfo.getDirectoryPath(), GSConstants.FILE_GS_EXPORT_JSON);
			filePath = fileObj.getCanonicalPath();
			if ( !fileObj.exists() ){
				String msg = messageResource.getString("MESS_COMM_ERR_METAINFO_38")+": path=["+filePath+"]";
				// If the meta information management file exists in the same directory, a supplementary message is displayed.
				File dir = new File(comLineInfo.getDirectoryPath());
				for ( File tmp : dir.listFiles() ){
					if ( tmp.getName().toLowerCase().endsWith(GSConstants.FILE_EXT_JSON) ){
						// When importing V1.5 or earlier export data or user-created file,
						// specify the meta information file with option -f and execute.
						msg += System.getProperty("line.separator")
								+messageResource.getString("MESS_COMM_ERR_METAINFO_39");
						break;
					}
				}
				throw new GSEIException(msg);
			}

			m_databaseList = new HashSet<String>();

			jr = Json.createReader(new InputStreamReader(MetaContainerFileIO.skipBOM(new FileInputStream(fileObj)), ToolConstants.ENCODING_JSON));
			f = new FileInputStream(fileObj);
			JsonObject jo = jr.readObject();

			// Degree of parallelism  (Does not exist in V2.5 or earlier files)
			int parallel = 1;
			if ( jo.containsKey(GSConstants.JSON_EXP_PARALLEL_COUNT) ){
				parallel = jo.getInt(GSConstants.JSON_EXP_PARALLEL_COUNT);
				comLineInfo.setParallelCount(parallel);
			}

			String version = jo.getString(GSConstants.JSON_EXP_VERSION);
			boolean versionNewerThanV4 = true;
			if ( version!=null && (version.startsWith("1.") || version.startsWith("2.") || version.startsWith("3.")) ){
				versionNewerThanV4 = false;
			}

			// Container list
			List<GSEIContInfo> contList = new ArrayList<GSEIContInfo>();

			JsonArray ja = jo.getJsonArray(GSConstants.JSON_EXP_CONTAINER);
			for (int i = 0; i < ja.size(); i++ ) {
				JsonObject obj = ja.getJsonObject(i);
				String dbName = ToolConstants.PUBLIC_DB;
				String contName = null;
				String file = null;

				// V3.5 or later The default container attribute is SINGLE
				// For compatibility with V3.2 or earlier files, read as a character string and branch the process.
				String attribute = ToolConstants.EXP_TOOL_ATTRIBUTE_SINGLE;

				for (Entry<String, JsonValue> entry : obj.entrySet()) {
					if (entry.getKey().equalsIgnoreCase(GSConstants.JSON_EXP_DATABASE)){
						dbName = entry.getValue().toString();
						if ( ToolConstants.PUBLIC_DB.equalsIgnoreCase(dbName) ){
							dbName = ToolConstants.PUBLIC_DB;
						}
						m_databaseList.add(dbName);

					} else if (entry.getKey().equalsIgnoreCase(GSConstants.JSON_EXP_CONTAINER_NAME)) {

						if ( versionNewerThanV4 ){
							contName = entry.getValue().toString();

						} else {
							// V3.5.0
							// Full container name "database name.container name"
							String fullContName = entry.getValue().toString();
							String[] tmp2 = fullContName.split("\\"+ToolConstants.DB_DELIMITER);
							if ( tmp2.length == 1 ){
								dbName = ToolConstants.PUBLIC_DB;
								contName = fullContName;
							} else if ( tmp2.length == 2 ){
								dbName = tmp2[0];
								contName = tmp2[1];
								if ( ToolConstants.PUBLIC_DB.equalsIgnoreCase(dbName) ){
									dbName = ToolConstants.PUBLIC_DB;
								}
							} else {
								throw new GSEIException(messageResource.getString("MESS_COMM_ERR_METAINFO_40")
										+": path=["+filePath+"] value=["+fullContName+"]");
							}
							m_databaseList.add(dbName);
						}

					} else if (entry.getKey().equalsIgnoreCase(GSConstants.JSON_EXP_CONTAINER_FILE)) {
						file = entry.getValue().toString();
					} else if ( entry.getKey().equalsIgnoreCase("attribute") ){
						String jsonAttr = entry.getValue().toString();

						if (jsonAttr != null) {
							attribute = jsonAttr;
						}
					}
				}

				if ( (contName == null) || (contName.length() == 0) || (file == null) || (file.length() == 0)
						|| ( (dbName != null) && (dbName.length() == 0) ) ){
					// Export managed file container (name, file) data is not set correctly
					throw new GSEIException(messageResource.getString("MESS_COMM_ERR_METAINFO_40")
							+": path=["+filePath+"]");
				}

				// Compatibility with V3.2 and earlier files
				// V3.5 V2.9 Previously exported partition containers are not supported
				if (attribute.equalsIgnoreCase(ToolConstants.EXP_TOOL_ATTRIBUTE_LARGE)) {
					// D00946: The partitioned table exported in format before V4 will be skipped.
					String skipMsg = messageResource.getString("MESS_COMM_ERR_METAINFO_46") +" db=["+dbName+"] container=["+contName+"]";
					comLineInfo.sysoutString(skipMsg);
					log.warn(skipMsg);
					commandProgressStatus.addSkipCount(1);
					commandProgressStatus.addSkipMessage(skipMsg);
					continue;
				} else if (attribute.equalsIgnoreCase(ToolConstants.EXP_TOOL_ATTRIBUTE_SUB) || attribute.equalsIgnoreCase(ToolConstants.EXP_TOOL_ATTRIBUTE_SINGLE_SYSTEM)) {
					// V3.5 V3.2 Skip system tables (index tables) previously exported and SUBs exported before V2.9
					// [Design Note] Since these are containers that are not shown to the user, no warning is displayed.
					continue;
				}


				contList.add(new GSEIContInfo(dbName, contName, file));
			}

			if( contList.size() == 0 ){
				// Container information is not stored in the Export management file
				throw new GSEIException(messageResource.getString("MESS_COMM_ERR_METAINFO_41")
						+": path=["+filePath+"]");
			}

			return contList;

		} catch ( GSEIException e ){
			throw e;
		} catch ( Exception e ) {
			// An error occurred while reading the Export management file
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_METAINFO_42")
					+": path=["+filePath+"] msg=["+e.getMessage()+"]", e);
		} finally {
			try {
				if ( jr != null ) jr.close();
				if ( f != null ) f.close();
			} catch ( Exception e ){}
		}
	}


	public Set<String> getDatabaseList(){
		return m_databaseList;
	}
}

