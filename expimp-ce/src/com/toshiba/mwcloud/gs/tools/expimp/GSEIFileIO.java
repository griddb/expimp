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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;

import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.tools.common.data.ToolConstants;
import com.toshiba.mwcloud.gs.tools.common.data.ToolContainerInfo;


public abstract class GSEIFileIO {


	/** Output mode (single container format (SC), multi-container format (MC)) */
	static enum OUTPUT_MODE { SINGLE, MULTI };

	// Number assigned when the file name exceeds the upper limit
	// (in the case of parallel execution, it may be unique within each thread)
	private static ThreadLocal<Integer> m_threadLocalFileNameToolLongNumber = new ThreadLocal<Integer>(){
		@Override
		protected Integer initialValue(){
			return 0;
		}
	};

	public static ThreadLocal<Integer> getThreadLocalFilenameToolLongNumber() {
		return m_threadLocalFileNameToolLongNumber;
	}
	
	public static void setThreadLocalFileNameToolLongNumber(Integer threadLocalFileNameToolLongNumber) {
		m_threadLocalFileNameToolLongNumber.set(threadLocalFileNameToolLongNumber);
	}

	//-------------------------------
	// Common
	//-------------------------------
	/** Command parameter information class  */
	commandLineInfo cmdLineInfo;

	/** Container information object */
	ToolContainerInfo m_containerInfo;

	/** Processing target directory  */
	String m_targetPath;

	/** Output mode (single container / multi-container) */
	OUTPUT_MODE m_outputMode = OUTPUT_MODE.SINGLE;

	/** File object currently being processed */
	File m_file;


	//---------------------------------------
	// File write method
	//---------------------------------------
	/** Start writing file */
	abstract void startWrite();
	/** End writing file */
	abstract void endWrite();
	/** Start output for each container */
	abstract void startWriteContainer(ToolContainerInfo contInfo) throws GSEIException;
	/** End of output for each container */
	abstract void endWriteContainer() throws GSEIException;
	/** Row data output */
	abstract void writeRow(Row row, int rowIndex) throws GSEIException;

	abstract String createRowFileName() throws GSEIException;

	//---------------------------------------
	// File read method
	//---------------------------------------
	/** DATE_FORMATの下位互換変換 */
	abstract void changeDateFormat();
	/** Reading in container units */
	abstract void readContainer(ToolContainerInfo contInfo, List<String> fileNameList) throws GSEIException;
	/** Confirmation of existence of next Row data */
	abstract boolean hasNextRow() throws GSEIException;
	/** Get Row data */
	abstract Row readRow(Container<?, Row> container) throws GSEIException;

	long m_timeFileIO;

	long m_timeFileRead;
	long m_timeFileRead2;

	GSEIFileIO(commandLineInfo cli){
		cmdLineInfo = cli;

		m_targetPath = cli.getDirectoryPath();

		// The --out option is specified only for Export
		if ( cli.getOutFlag() ){
			m_outputMode = OUTPUT_MODE.MULTI;	// Multi-container format
		} else {
			m_outputMode = OUTPUT_MODE.SINGLE;	// Single container format
		}

		m_timeFileIO = 0;
		m_timeFileRead = 0;
		m_timeFileRead2 = 0;

		m_threadLocalFileNameToolLongNumber.set(0);
	}

	public static String createRowFileName(ToolContainerInfo toolContInfo, commandLineInfo comLineInfo ) throws GSEIException {

		String fileBase = "";
		if ( comLineInfo.getOutFlag() ){
			// Multi-container format
			if ( comLineInfo.getOutFileName() == null ){
				// Date
				fileBase = GSEIFileIO.createCalendarFileName();
			} else {
				// User specified
				fileBase = comLineInfo.getOutFileName();
			}

		} else {
			// Single container format
			fileBase = encodingDbAndContainer(toolContInfo);
		}
		toolContInfo.setFileBaseName(fileBase);

		return fileBase;
	}

	public static String encodingDbAndContainer(ToolContainerInfo toolContInfo) {
		return encodingDbAndContainer(toolContInfo, true);
	}
	public static String encodingDbAndContainer(ToolContainerInfo toolContInfo, boolean isNo) {
		// Single container format     "DB name.Container name"
		String dbName = toolContInfo.getDbName();
		String contName = toolContInfo.getName();
		String encodingName = null;

		// URL encoding process
		try {
			if ( (dbName == null) || (dbName.length() == 0) ){
				dbName = ToolConstants.PUBLIC_DB;
			}
			encodingName = URLEncoder.encode(dbName, "UTF-8") + ToolConstants.DB_DELIMITER + URLEncoder.encode(contName, "UTF-8");

		} catch ( UnsupportedEncodingException e ){
			// do nothing
		}

		// Character limit
		if ( encodingName.length() > 140 ){
			encodingName = encodingName.substring(0, 140);
			if ( isNo ) {
				int no = m_threadLocalFileNameToolLongNumber.get();
				m_threadLocalFileNameToolLongNumber.set(no+1);
				encodingName += "_n" + no;
			}
			// TimeIntervalコンテナのロウデータファイル名は コンテナ名_ + YYYYMMdd-YYYYMMdd
			if (toolContInfo.getIntervals() != null && toolContInfo.getIntervals().length() > 0) {
				encodingName += "_" + toolContInfo.getIntervals(); 
			} else {
				// マルチコンテナの場合はスレッド番号を追加する
				if ( Thread.currentThread() instanceof ExportThread ){
					int id = ((ExportThread)Thread.currentThread()).getThreadNo();
					if (toolContInfo.getFileBaseName() != null && toolContInfo.getFileBaseName().endsWith(GSConstants.FILENAME_SEPARATOR + "t" + id)) {
						encodingName += GSConstants.FILENAME_SEPARATOR + "t" + id;
					}
				}
			}
		}

		return encodingName;
	}

	/**
	 * Date and time file name creation method
	 *
	 * @return Date and time file name
	 */
	static String createCalendarFileName() {

		if ( m_fileName == null ){
			try {
				Calendar cal = Calendar.getInstance();
				m_fileName = String.format("%1$tY%1$tm%1$td_%1$tH%1$tM%1$tS_%1$tL", cal);

			} catch (Exception e) {
				// Returns the file name even if an error occurs.
				//log.error("createCalendarFileName():[" + "20130101_000000_000"+ "]", e);
				m_fileName = "20130101_000000_000";
			}
		}
		return m_fileName;
	}

	private static String m_fileName;

}
