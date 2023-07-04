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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.toshiba.mwcloud.gs.ColumnInfo;
import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.ContainerInfo;
import com.toshiba.mwcloud.gs.GSType;
import com.toshiba.mwcloud.gs.Geometry;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.TimeUnit;
import com.toshiba.mwcloud.gs.TimestampUtils;
import com.toshiba.mwcloud.gs.tools.common.data.MetaContainerFileIO;
import com.toshiba.mwcloud.gs.tools.common.data.ToolConstants;
import com.toshiba.mwcloud.gs.tools.common.data.ToolContainerInfo;
import com.toshiba.mwcloud.gs.tools.expimp.GSConstants.TARGET_TYPE;
import com.toshiba.mwcloud.gs.tools.expimp.util.Utility;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * CSV format raw data I/O processing class
 *
 *
 */
public class rowCsvFileIO extends GSEIFileIO{

	/** String, character size of the condition to output array type data to an external file */
	private final int MAX_CELL_LENGTH = 100;

	/**
	 * Internationalized message resource
	 */
	private static ResourceBundle messageResource;

	/**
	 * Hostname string
	 */
	private String hostname;

	/** CSV output file object */
	private CSVWriter m_outFile;

	/** CSV read file object */
	private CSVReader m_inFile;

	/** Number of lines read */
	private int m_lineCount;

	private String[] csvLine;

	private SimpleDateFormat dateFormatCheck;
	private SimpleDateFormat dateFormat;
	private SimpleDateFormat dateFormatNotTimezone;

	// For TimeStamp conversion(String->Date)
	private String m_timeZoneStr = "";
	private int m_diffDate = 0;

	// The last line of container name you read last time
	private String m_csvContName;

	private static char SEPARATOR = CSVParser.DEFAULT_SEPARATOR;
	private static char QUOTE = CSVParser.DEFAULT_QUOTE_CHARACTER;
	private static char ESCAPE = CSVParser.DEFAULT_ESCAPE_CHARACTER;
	private static char ESCAPE_NULL = 0;

	private String m_multiExternalFileNameBase;

	/**
	 * Constructor
	 *
	 * @param cli Command parameter information class
	 */
	public rowCsvFileIO(commandLineInfo cli) {
		super(cli);

		messageResource = Utility.getResource();

		// Date format with validation
		dateFormatCheck = new SimpleDateFormat(GSConstants.DATE_FORMAT);
		dateFormatCheck.setLenient(false);

		// Date format without validation
		dateFormat = new SimpleDateFormat(GSConstants.DATE_FORMAT);

		// Date format without timezone
		dateFormatNotTimezone = new SimpleDateFormat(GSConstants.DATE_FORMAT_NOT_TIMEZONE);
	}

	/**
	 * Timestamp型の下位互換対応
	 * V5.2以降 GSConstants.DATE_FORMAT yyyy-MM-dd'T'HH:mm:ss.SSSXXX
	 * V5.1以前 GSConstants.DATE_FORMAT_BEFORE yyyy-MM-dd'T'HH:mm:ss.SSSZ
	 */
	public void changeDateFormat() {
		// 妥当性チェックあり日付フォーマット
		dateFormatCheck = new SimpleDateFormat(GSConstants.DATE_FORMAT_BEFORE);
		dateFormatCheck.setLenient(false);
			
		// 妥当性チェックなし日付フォーマット
		dateFormat = new SimpleDateFormat(GSConstants.DATE_FORMAT_BEFORE);
	}

	//**********************************************************************
	// Start/end of file output
	//**********************************************************************
	/**
	 * Prepare for file output.
	 *
	 *   For multi-container format, create a file to output multiple container data.
	 *
	 * @param fileName File name in multi-container format   (Specify null in single mode)
	 */
	public void startWrite(){
		// No processing in particular
	}

	/**
	 * Ends file output.
	 */
	public void endWrite(){
		if ( m_outputMode == OUTPUT_MODE.MULTI ){
			try {
				m_outFile.close();
				m_outFile = null;
			} catch ( Exception e ){}
		}
	}


	//**********************************************************************
	// Start/end of container unit
	//**********************************************************************
	/**
	 * Prepare to output container data.
	 *
	 *    For single container format, create a file to output the container data.
	 *
	 * @param containerInfo Container information
	 */
	public void startWriteContainer(ToolContainerInfo containerInfo) throws GSEIException{
		m_containerInfo = containerInfo;

		// No file output in test mode
		if (cmdLineInfo.getTestFlag()) return;

		String fileName = super.createRowFileName(m_containerInfo, cmdLineInfo);
		containerInfo.setFileBaseName(fileName);

		// V4.5 Do not output to a file if export only container definition is specified
		if (cmdLineInfo.getSchemaOnlyFlag()) return;

		// For multi-container format
		if ( m_outputMode == OUTPUT_MODE.MULTI ){
			m_multiExternalFileNameBase = null;

			if ( m_file == null ){
				// File acquisition
				//String fileName = containerInfo.getFileBaseName();
				m_file = new File(m_targetPath, fileName + GSConstants.FILE_EXT_CSV);

				// File creation
				try {
					m_outFile = new CSVWriter(new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(m_file), GSConstants.ENCODING_CSV)), SEPARATOR, QUOTE, ESCAPE);

				} catch ( Exception e ){
					// An error occurred while creating the CSV file
					throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_27")+": path=["+m_file.getAbsolutePath()
							+"] msg=["+e.getMessage()+"]", e);
				}

				csvWriteHeader(fileName, cmdLineInfo.getUserName());
			}

		// For single container format
		} else if ( m_outputMode == OUTPUT_MODE.SINGLE ){
			// File creation
			//m_file = new File(m_targetPath, containerInfo.getFullName()+GSConstants.FILE_EXT_CSV);
			m_file = new File(m_targetPath, fileName + GSConstants.FILE_EXT_CSV);
			try {
				m_outFile = new CSVWriter(new BufferedWriter(new OutputStreamWriter(
								new FileOutputStream(m_file), GSConstants.ENCODING_CSV)), SEPARATOR, QUOTE, ESCAPE);

			} catch ( Exception e ){
				// An error occurred while creating the CSV file
				throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_28")+": db=["+m_containerInfo.getDbName()+"] containerName=["
						+m_containerInfo.getName()+"] path=["+m_file.getAbsolutePath()
						+"] msg=["+e.getMessage()+"]", e);
			}

			// Output of header and container information
			csvWriteHeader(m_containerInfo.getFileBaseName(), cmdLineInfo.getUserName());

		}

		csvWriteHeaderContainer();


	}

	/**
	 * Ends the output of container data.
	 */
	public void endWriteContainer(){
		// No file output in test mode
		if (cmdLineInfo.getTestFlag()) return;
		// V4.5 Do not output to a file if export only container definition is specified
		if (cmdLineInfo.getSchemaOnlyFlag()) return;

		try {
			// Save the name of the output file
			m_containerInfo.addContainerFile(m_file.getName());

			// For single container
			if ( m_outputMode == OUTPUT_MODE.SINGLE ){
				m_outFile.close();
				m_outFile = null;
			}

		} catch (Exception e) {}
	}

	//**********************************************************************
	// Processing ROW data
	//**********************************************************************
	/**
	 * Write one Row data to a file.
	 *
	 * @param row Row data
	 * @param rowNum Row number
	 */
	public void writeRow(Row row, int rowNum ) throws GSEIException{
		try {
			// Convert Row data to String   (If external file output is required, output it)
			ContainerInfo ci = row.getSchema();
			String[] data = new String[ci.getColumnCount()];
			for (int i = 0; i < ci.getColumnCount(); i++) {
				ColumnInfo col = ci.getColumnInfo(i);
				data[i] = getRowValue(row, rowNum, col.getType(), i );
			}

			// Write to file
			m_outFile.writeNext(data);
			m_outFile.flush();

		} catch ( GSEIException e ){
			throw e;
		} catch ( Exception e ){
			// An error occurred while writing Row data
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_29")
					+": db=["+m_containerInfo.getDbName()+"]containerName=["+m_containerInfo.getName()
					+"] rowNum=["+rowNum+"] path=["+m_file.getAbsolutePath()+"] msg=["+e.getMessage()+"]",e);
		}
	}



	public void readContainer(ToolContainerInfo contInfo, List<String> fileList) throws GSEIException {
		m_containerInfo = contInfo;

		long start = System.currentTimeMillis();
		try {
			m_timeFileIO = 0;

			String identifer = m_containerInfo.getFullName();
			File file = new File(m_targetPath, fileList.get(0));
			if ( (m_containerInfo.getVersion() != null) && (m_containerInfo.getVersion().startsWith("1.") || m_containerInfo.getVersion().startsWith("2.")
					|| m_containerInfo.getVersion().startsWith("3.")) ){
				if ( m_containerInfo.getDbName() == null  || m_containerInfo.getDbName().equalsIgnoreCase(ToolConstants.PUBLIC_DB) ){
					identifer = m_containerInfo.getName();
				}
			}

			boolean readNewFile = true;
			if ( (cmdLineInfo.getTargetType()==TARGET_TYPE.ALL) && (m_file != null) && file.equals(m_file)){
				// Read more for --all in multi-container format.
				if( identifer.equals(m_csvContName) ){
					// Check if the last line you read last time corresponds to this container name
					return;
				}
				readNewFile = false;
			}

			for ( int i = 0; i < 2; i++ ){
				if (readNewFile) {
					// Open CSV file (CSV file is not divided. Only one file)
					m_file = new File(m_targetPath, fileList.get(0));
					if ( m_inFile != null ) m_inFile.close();

					char escapeChar = ESCAPE;
					if ( (m_containerInfo.getVersion() != null) && (m_containerInfo.getVersion().startsWith("1.") || m_containerInfo.getVersion().startsWith("2.")) ){
						escapeChar = ESCAPE_NULL;
					}
					CSVParser parser = new CSVParserBuilder()
										.withSeparator(SEPARATOR)
										.withQuoteChar(QUOTE)
										.withEscapeChar(escapeChar)
										.withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
										.build();
					m_inFile = new CSVReaderBuilder(new BufferedReader(new InputStreamReader(
								new FileInputStream(m_file), GSConstants.ENCODING_CSV)))
								//.withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
								.withCSVParser(parser)
								.withKeepCarriageReturn(true)
								.build();
				}

				// Read line by line
				boolean container_found = false;
				m_lineCount = 0;
				while ((csvLine = m_inFile.readNext()) != null ){
					m_lineCount++;

					// Skip to the line of the specified container
					if ( csvLine[0].trim().equals("$")
							&& csvLine[1].trim().contains(identifer) ) {
						container_found = true;
						m_csvContName = identifer;
						break;
					}
				}

				if ( container_found ){
					break;
				} else {
					if ( readNewFile || (i == 1) ){
						// Data for the specified container does not exist
						throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_30")
								+": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()+"] path=["+m_file.getAbsolutePath()+"]");
					}
					readNewFile = true;
				}
			}

		} catch ( GSEIException e ){
			throw e;
		} catch ( Exception e ){
			// An error occurred while reading the CSV file
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_31")+": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()
						+"] path=["+m_file.getAbsolutePath()+"] line=["+m_lineCount+"]", e);
		} finally {
			long end = System.currentTimeMillis();
			m_timeFileIO += (end-start);
		}

	}

	public boolean hasNextRow() throws GSEIException{

		long start = System.currentTimeMillis();
		try {
			while ((csvLine = m_inFile.readNext())!=null){
				m_lineCount++;

				if (csvLine[0] != null) {
					// Skip for comment lines
					if (csvLine[0].trim().indexOf("#") == 0) continue;

					// V3.5 ~ Blank lines are interpreted as NULL of 1 column data
					//if ( (csvLine.length == 1) && csvLine[0].isEmpty()) continue;

					// Container name line check (ends with another container name)
					if ((csvLine.length == 2) && csvLine[0].trim().equals("$")
							&& !csvLine[1].trim().equalsIgnoreCase(m_csvContName)) {
						m_csvContName = csvLine[1].trim();
						break;
					}
				}
				return true;
			}

			return false;

		} catch ( Exception e ){
			// An error occurred while reading the CSV file
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_32")
					+": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()
					+"] path=["+m_file.getAbsolutePath()+"] line=["+m_lineCount+"]", e);
		} finally {
			long end = System.currentTimeMillis();
			m_timeFileIO += (end-start);
		}
	}

	public Row readRow(Container<?, Row> container) throws GSEIException {
		try {
			//List<columnInfo> colSet = m_containerInfo.getColumnInfoSet();

			Row row = container.createRow();

			// Column size check of read data
			if (m_containerInfo.getContainerInfo().getColumnCount() != csvLine.length) {
				// "We have detected raw data that does not match the number of columns in the schema."
				String msg = messageResource.getString("MESS_COMM_ERR_ROWCSV_16")
						+ ": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()+"] columnNum=["+csvLine.length
						+"] schemaColumnNum=["+m_containerInfo.getContainerInfo().getColumnCount()+"] line=["+m_lineCount+"] path=["+m_file.getAbsolutePath()+"]";
				throw new GSEIException(msg);
			}

			// Processing in column data units
			for (int columnNum = 0; columnNum < m_containerInfo.getContainerInfo().getColumnCount(); columnNum++) {
				ColumnInfo col = m_containerInfo.getContainerInfo().getColumnInfo(columnNum);

				// If empty, do not set a value in Row (initial value)
				if (csvLine[columnNum] != null && csvLine[columnNum].isEmpty()) {
					continue;
				}

				// Set the read data in the Row object
				setRowValue(container, row, col.getType(), csvLine[columnNum], columnNum);
			}

			return row;

		} catch ( GSEIException e){
			throw e;

		} catch ( Exception e ){
			// An error occurred while reading the CSV file
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_33")+": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()
					+"] path=["+m_file.getAbsolutePath()+"] line=["+m_lineCount+"] msg=["+e.getMessage()+"]", e);
		}
	}


	/**
	 * Set the String data read from CSV to the Row object.
	 *
	 * @param container GridStore container class
	 * @param row GridStore row information class
	 * @param columnType Column type
	 * @param value Column data
	 * @param columnNum Column number
	 *
	 * @return GridStore row information class
	 * @throws GSEIException
	 */
	private Row setRowValue(Container<?, Row> container, Row row, GSType columnType,
			String value, int columnNum) throws GSEIException {
		try {
			//value = value.trim();

			if (value == null) {
				row.setNull(columnNum);
			} else if (columnType.equals(GSType.BOOL)) {
				// "true" ->true,  Other character strings ->false
				row.setBool(columnNum, Boolean.parseBoolean(value));

			} else if (columnType.equals(GSType.STRING)) {
				if (isExternalFile(value, columnType)){
					// Read external file
					value = readExternalTextFile(value, columnNum);
				}
				row.setString(columnNum, value);

			} else if (columnType.equals(GSType.BYTE)) {
				row.setByte(columnNum, Byte.parseByte(value));

			} else if (columnType.equals(GSType.SHORT)) {
				row.setShort(columnNum, Short.parseShort(value));

			} else if (columnType.equals(GSType.INTEGER)) {
				row.setInteger(columnNum, Integer.parseInt(value));

			} else if (columnType.equals(GSType.LONG)) {
				row.setLong(columnNum, Long.parseLong(value));

			} else if (columnType.equals(GSType.FLOAT)) {
				row.setFloat(columnNum, Float.parseFloat(value));

			} else if (columnType.equals(GSType.DOUBLE)) {
				row.setDouble(columnNum, Double.parseDouble(value));

			} else if (columnType.equals(GSType.TIMESTAMP)) {
				if (MetaContainerFileIO.isPreciseColumn(row.getSchema().getColumnInfo(columnNum))) {
					row.setPreciseTimestamp(columnNum, convertStr2Timestamp(value));
				} else {
					// 自前の変換処理を使用    (SimpleDataFormatは、性能が悪い)
					row.setTimestamp(columnNum, convertStr2Date(value));
				}

			} else if ( columnType.equals(GSType.BLOB)) {
				// BLOB is always an external file (binary)
				if (isExternalFile(value, columnType)){
					readExternalBinaryFile(row, container.createBlob(), value, columnNum);
				} else {
					// Specify the BLOB type in an external file
					throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_34")
							+": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()
							+"] line=["+m_lineCount+"] path=["+m_file.getAbsolutePath()+"]");
				}

			} else if (columnType.equals(GSType.BOOL_ARRAY)) {
				String[] boolean_array = null;
				if (isExternalFile(value, columnType)){
					// Read external file
					boolean_array = readExternalTextArrayFile(value, columnNum);
				} else {
					boolean_array = value.split(",", -1);
				}
				boolean[] ba = new boolean[boolean_array.length];
				for (int i = 0; i < boolean_array.length; i++) {
					// "true" ->true,  Other character strings ->false
					ba[i] = Boolean.parseBoolean(boolean_array[i]);
				}
				row.setBoolArray(columnNum, ba);

			} else if (columnType.equals(GSType.STRING_ARRAY)) {
				String[] str_array = null;
				if (isExternalFile(value, columnType)){
					// Read external file
					str_array = readExternalTextArrayFile(value, columnNum);
				} else {
					str_array = value.split(",", -1);
				}
				row.setStringArray(columnNum, str_array);

			} else if (columnType.equals(GSType.BYTE_ARRAY)) {
				String[] str_array = null;
				if (isExternalFile(value, columnType)){
					// Read external file
					str_array = readExternalTextArrayFile(value, columnNum);
				} else {
					str_array = value.split(",", -1);
				}
				ByteBuffer ba = ByteBuffer.allocate(str_array.length);
				for (String str : str_array) {
					ba.put(Byte.parseByte(str));
				}
				row.setByteArray(columnNum, ba.array());

			} else if (columnType.equals(GSType.SHORT_ARRAY)) {
				String[] boolean_array = null;
				if (isExternalFile(value, columnType)){
					// Read external file
					boolean_array = readExternalTextArrayFile(value, columnNum);
				} else {
					boolean_array = value.split(",", -1);
				}
				short[] sa = new short[boolean_array.length];
				for (int i = 0; i < boolean_array.length; i++) {
					sa[i] = Short.parseShort(boolean_array[i]);
				}
				row.setShortArray(columnNum, sa);

			} else if (columnType.equals(GSType.INTEGER_ARRAY)) {
				String[] boolean_array = null;
				if (isExternalFile(value, columnType)){
					// Read external file
					boolean_array = readExternalTextArrayFile(value, columnNum);
				} else {
					boolean_array = value.split(",", -1);
				}
				int[] ia = new int[boolean_array.length];
				for (int i = 0; i < boolean_array.length; i++) {
					ia[i] = Integer.parseInt(boolean_array[i]);
				}
				row.setIntegerArray(columnNum, ia);

			} else if (columnType.equals(GSType.LONG_ARRAY)) {
				String[] boolean_array = null;
				if (isExternalFile(value, columnType)){
					// Read external file
					boolean_array = readExternalTextArrayFile(value, columnNum);
				} else {
					boolean_array = value.split(",", -1);
				}
				long[] la = new long[boolean_array.length];
				for (int i = 0; i < boolean_array.length; i++) {
					la[i] = Long.parseLong(boolean_array[i]);
				}
				row.setLongArray(columnNum, la);

			} else if (columnType.equals(GSType.FLOAT_ARRAY)) {
				String[] boolean_array = null;
				if (isExternalFile(value, columnType)){
					// Read external file
					boolean_array = readExternalTextArrayFile(value, columnNum);
				} else {
					boolean_array = value.split(",", -1);
				}
				float[] fa = new float[boolean_array.length];
				for (int i = 0; i < boolean_array.length; i++) {
					fa[i] = Float.parseFloat(boolean_array[i]);
				}
				row.setFloatArray(columnNum, fa);

			} else if (columnType.equals(GSType.DOUBLE_ARRAY)) {
				String[] boolean_array = null;
				if (isExternalFile(value, columnType)){
					// Read external file
					boolean_array = readExternalTextArrayFile(value, columnNum);
				} else {
					boolean_array = value.split(",", -1);
				}
				double[] da = new double[boolean_array.length];
				for (int i = 0; i < boolean_array.length; i++) {
					da[i] = Double.parseDouble(boolean_array[i]);
				}
				row.setDoubleArray(columnNum, da);

			} else if (columnType.equals(GSType.GEOMETRY)) {
				if (isExternalFile(value, columnType)){
					// Read external file
					value = readExternalTextFile(value, columnNum);
				}
				row.setGeometry(columnNum, Geometry.valueOf(value));

			} else if (columnType.equals(GSType.TIMESTAMP_ARRAY)) {
				String[] timestamp_array = null;
				if (isExternalFile(value, columnType)){
					// Read external file
					timestamp_array = readExternalTextArrayFile(value, columnNum);
				} else {
					timestamp_array = value.split(",", -1);
				}
				Date[] da = new Date[timestamp_array.length];
				for (int i = 0; i < timestamp_array.length; i++) {
					da[i] = convertStr2Date(timestamp_array[i]);
				}
				row.setTimestampArray(columnNum, da);

			} else {
				// Log output for unknown GS Type
				// "A data type that cannot be processed by this version has been detected"-> "MESS_COMM_ERR_ROWCSV_19"
				throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_19")+" :type=["+columnType+"]");
			}

			return row;

		} catch ( GSEIException e ){
			throw e;

		} catch (Exception e) {
			// "An error occurred while setting the raw data in the Grid Store"
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_18")
					+ ": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()+"] type=["+columnType
					+"] line=["+m_lineCount+"] msg=["+e.getMessage()+"] path=["+m_file.getAbsolutePath()+"]", e);
		}
	}

	/**
	 * Converts a date string to a Date object.
	 *
	 * @param value Date string (format yyyy-MM-ddTHH:mm:ss.SSSZ)
	 * @return Date object
	 * @throws Exception
	 */
	private Date convertStr2Date(String value) throws Exception{
		// Date format index
        // 0         1         2
		// 012345678901234567890123
		// yyyy-MM-ddTHH:mm:ss.SSSZ
		// yyyy-MM-dd HH:mm:ss.SSS
		// These two formats are OK

		int yearEnd = value.indexOf("-");
		if ( yearEnd != 4 ){
			throw new Exception("Illegal TimeStamp Format '"+GSConstants.DATE_FORMAT + "'. value=["+value+"]");
		}
		int monthEnd = value.indexOf("-", 5);
		if ( monthEnd != 7 ){
			throw new Exception("Illegal TimeStamp Format '"+GSConstants.DATE_FORMAT + "'. value=["+value+"]");
		}
		//int dayEnd = value.indexOf("T", 8);
		//if ( dayEnd != 10 ){
		//	throw new Exception("Illegal TimeStamp Format '"+GSConstants.DATE_FORMAT + "'. value=["+value+"]");
		//}
		int hourEnd = value.indexOf(":", 11);
		if ( hourEnd != 13 ){
			throw new Exception("Illegal TimeStamp Format '"+GSConstants.DATE_FORMAT + "'. value=["+value+"]");
		}
		int minEnd = value.indexOf(":", 14);
		if ( minEnd != 16 ){
			throw new Exception("Illegal TimeStamp Format '"+GSConstants.DATE_FORMAT + "'. value=["+value+"]");
		}
		int secEnd = value.indexOf(".", 17);
		if ( secEnd != 19 ){
			throw new Exception("Illegal TimeStamp Format '"+GSConstants.DATE_FORMAT + "'. value=["+value+"]");
		}
		if ( value.length() < 23 ){
			throw new Exception("Illegal TimeStamp Format '"+GSConstants.DATE_FORMAT + "'. value=["+value+"]");
		}

		try {
			int year = Integer.parseInt(value.substring(0, 4));
			int month = Integer.parseInt(value.substring(5, 7));
			int day = Integer.parseInt(value.substring(8, 10));
			int hour = Integer.parseInt(value.substring(11, 13));
			int min = Integer.parseInt(value.substring(14, 16));
			int sec = Integer.parseInt(value.substring(17, 19));
			int millsec = Integer.parseInt(value.substring(20, 23));

			if ( value.length() > 23 ){
				String timeZone = value.substring(23);
				if ( !m_timeZoneStr.equals(timeZone) ){
					// Date and time conversion including TimeZone
					Date timeZoneDate = dateFormat.parse(value);
					// Date and time conversion not including TimeZone
					Date notTimeZoneDate = dateFormatNotTimezone.parse(value);
					m_diffDate = (int)(timeZoneDate.getTime() - notTimeZoneDate.getTime());
					m_timeZoneStr = timeZone;
				}
			} else {
				m_timeZoneStr = "";
				m_diffDate = 0;
			}

			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.MONTH, month-1);	// MONTH starts from 0
			c.set(Calendar.DAY_OF_MONTH, day);
			c.set(Calendar.HOUR_OF_DAY, hour);
			c.set(Calendar.MINUTE, min);
			c.set(Calendar.SECOND, sec);
			c.set(Calendar.MILLISECOND, millsec);
			c.add(Calendar.MILLISECOND, m_diffDate);	// Reflection of Time Zone

			return c.getTime();

		} catch ( NumberFormatException e ){
			throw new Exception("Illegal TimeStamp Format '"+GSConstants.DATE_FORMAT + "'. value=["+value+"]", e);
		}
	}

	/**
	 * Convert a string into Timestamp value
	 *
	 * @param value the string to convert
	 * @return a Timestamp value
	 * @throws ParseException
	 */
	private Timestamp convertStr2Timestamp(String value) throws ParseException {
		// yyyy-MM-dd HH:mm:ss.SSS compability
		if (value.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3}$")) {
			value = value.replace(" ", "T") + "Z";
		}
		Timestamp columnTimeStamp = TimestampUtils.parsePrecise(value);
		return columnTimeStamp;
	}

	/**
	 * Check if it is a character string that specifies an external file.  Format   "@GSType:External file name"
	 *
	 * @param s String
	 * @param type Column type
	 * @return true:External file is specified
	 * @throws GSEIException
	 */
	private boolean isExternalFile(String s, GSType type) throws GSEIException{
		try {
			String[] ss = s.split(":", -1);
			if ( ss.length != 2 ){
				// It is not an external file name description.
				return false;
			}
			if (ss[0].matches("^@[A-Z_]+$")) {
				GSType t = GSType.valueOf(ss[0].substring(1).toUpperCase());
				if ( t.equals(type) ){
					return true;
				} else {
					// The column type specified for the external file name is invalid.
					throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_35")
							+ ": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()+"] line=["+m_lineCount
							+"] data=["+s+"] type=["+t+"] columnType=["+type+"] path=["+m_file.getAbsolutePath()+"]");
				}
			}

			// Not an external file name description
			return false;

		} catch (Exception e) {
			// The specification of the external file name is invalid
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_36")
					+ ": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()+"] line=["+m_lineCount
					+"] data=["+s+"] path=["+m_file.getAbsolutePath()+"]", e);
		}

	}


	/**
	 * Get the filename of an external file
	 *
	 * @param s External file string
	 * @return External file name
	 */
	private String getFilePath(String s) throws GSEIException{
		try {
			String[] ss = s.split(":", -1);
			if (ss[0].matches("^@[A-Z_]+")) {
				return s.substring(ss[0].length() + 1);
			} else {
				// "The file name of the external data file could not be obtained"
				throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_22")
						+": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()
						+"] path=["+m_file.getAbsolutePath()+"] line=["+m_lineCount+"] value=["+s+"]");
			}

		} catch (GSEIException e){
			throw e;
		} catch (Exception e) {
			// "The file name of the external data file could not be obtained"
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_22")
					+": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()
					+"] path=["+m_file.getAbsolutePath()+"] line=["+m_lineCount+"] value=["+s
					+"] msg=["+e.getMessage()+"]", e);
		}
	}

	/**
	 * Reads the text file data of an external file.
	 *
	 * @param csvValue String of "@column type: external file name"  (Format is checked)
	 * @param columnNum Number of column rows
	 * @return Read data
	 */
	private String readExternalTextFile(String csvValue, int columnNum) throws GSEIException{
		BufferedReader in = null;
		File inputFile = null;
		String fileName = null;
		try {
			// Extract file name
			int idx = csvValue.indexOf(":");
			fileName = csvValue.substring(idx+1);

			// File open
			inputFile = new File(cmdLineInfo.getDirectoryPath(), fileName);
			in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), GSConstants.ENCODING_CSV));

			// Read
			StringBuffer sb = new StringBuffer();
			int len = 0;
			char[] c = new char[GSConstants.READ_BUFFER_SIZE];
			while ( (len = in.read(c, 0, c.length)) > -1 ){
				sb.append(c, 0, len);
			}

			return sb.toString();

		} catch (Exception e) {
			// "An error occurred while reading data from an external data file"
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_23")
					+ ": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()
					+"] msg=["+e.getMessage()+"] externalFile=["+ fileName+"] csvFile=["+m_file.getAbsolutePath()
					+"] line=["+m_lineCount+"]", e);

		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Generates the file name of the raw file.
	 *
	 * @return File name
	 */
	String createRowFileName(){
		String name = m_containerInfo.getFileBaseName();
		name += GSConstants.FILE_EXT_CSV;
		return name;
	}

	/**
	 * Reads the text file data of an external file.
	 *
	 * @param csvValue String of "@column type: external file name"  (Format is checked)
	 * @param columnNum Number of column rows
	 * @return Read data
	 */
	private String[] readExternalTextArrayFile(String csvValue, int columnNum) throws GSEIException{
		CSVReader in = null;
		File inputFile = null;
		String fileName = null;
		try {
			// Extract file name
			int idx = csvValue.indexOf(":");
			fileName = csvValue.substring(idx+1);

			// File open
			inputFile = new File(cmdLineInfo.getDirectoryPath(), fileName);
			char escapeChar = ESCAPE;
			if ( (m_containerInfo.getVersion()!=null) && (m_containerInfo.getVersion().startsWith("1.") || m_containerInfo.getVersion().startsWith("2.")) ){
				escapeChar = ESCAPE_NULL;
			}
			in = new CSVReader(new InputStreamReader(new FileInputStream(inputFile), GSConstants.ENCODING_CSV), SEPARATOR, QUOTE, escapeChar);
			// Read
			return in.readNext();

		} catch (Exception e) {
			// "An error occurred while reading data from an external data file"
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_23")
					+ ": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()
					+"] msg=["+e.getMessage()+"] externalFile=["+ fileName+"] csvFile=["+m_file.getAbsolutePath()
					+"] line=["+m_lineCount+"]", e);

		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Converts the specified column of Row to String.
	 * Data that requires external file output is output and the file name is returned.
	 *
	 * @param row GridStore row information class
	 * @param rowNum Row number
	 * @param columnType Column type
	 * @param columnNum Column number
	 *
	 * @return Column data string
	 */
	private String getRowValue(Row row, int rowNum, GSType columnType, int columnNum ) throws GSEIException{
		try {
			if (row.isNull(columnNum)) {
				return null;
			} else if (columnType.equals(GSType.BOOL)) {
				return String.valueOf(row.getBool(columnNum));
			} else if (columnType.equals(GSType.STRING)) {
				String tmp = row.getString(columnNum);
				if (tmp.length() > MAX_CELL_LENGTH) {
					// If the character length limit is exceeded, output to an external file
					return writeExternalTextFile(rowNum, columnNum, columnType, new StringBuilder(tmp));
				} else {
					// Output to an external file if return (CR,'\r') is included
					if ( tmp.indexOf('\r') > -1 ){
						return writeExternalTextFile(rowNum, columnNum, columnType, new StringBuilder(tmp));
					} else {
						return tmp;
					}
				}
			} else if (columnType.equals(GSType.BYTE)) {
				return Byte.toString(row.getByte(columnNum));

			} else if (columnType.equals(GSType.SHORT)) {
				return Short.toString(row.getShort(columnNum));

			} else if (columnType.equals(GSType.INTEGER)) {
				return Integer.toString(row.getInteger(columnNum));

			} else if (columnType.equals(GSType.LONG)) {
				return Long.toString(row.getLong(columnNum));

			} else if (columnType.equals(GSType.FLOAT)) {
				return Float.toString(row.getFloat(columnNum));

			} else if (columnType.equals(GSType.DOUBLE)) {
				return Double.toString(row.getDouble(columnNum));

			} else if (columnType.equals(GSType.TIMESTAMP)) {
				ColumnInfo columnInfo = row.getSchema().getColumnInfo(columnNum);
				if (MetaContainerFileIO.isPreciseColumn(columnInfo)) {
					TimeUnit    precision  = columnInfo.getTimePrecision();
					Timestamp   timestamp  = row.getPreciseTimestamp(columnNum);
					ZonedDateTime  zdt  = ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
					return zdt.format(MetaContainerFileIO.getDateTimeFormatter(precision));
				}
				else {
					java.util.Date date = row.getTimestamp(columnNum);
					return dateFormat.format(date);
				}

			} else if (columnType.equals(GSType.GEOMETRY)) {
				StringBuilder sb = new StringBuilder();
				sb.append(row.getGeometry(columnNum).toString());
				return writeExternalTextFile(rowNum, columnNum, columnType, sb);

			} else if (columnType.equals(GSType.BLOB)) {
				return writeExternalBinaryFile(rowNum, columnNum, row);

			} else if (columnType.equals(GSType.BOOL_ARRAY)) {
				StringBuilder sb = new StringBuilder();
				for (boolean b : row.getBoolArray(columnNum)) {
					sb.append(String.valueOf(b)).append(",");
				}
				if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
				if (sb.length() > MAX_CELL_LENGTH) {
					return writeExternalTextArrayFile(rowNum, columnNum, columnType, sb.toString().split(","));
				} else {
					return sb.toString();
				}

			} else if (columnType.equals(GSType.STRING_ARRAY)) {
				StringBuilder sb = new StringBuilder();
				String[] data = row.getStringArray(columnNum);
				boolean check = false;
				for (String b : data) {
					if ( b.indexOf(",") > -1 ){
						// Since the data contains "," it is output to an external file in CSV format.
						check = true;
						break;
					}
					sb.append(b).append(",");
				}
				if ( check ){
					return writeExternalTextArrayFile(rowNum, columnNum, columnType, data);

				} else {
					if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
					if (sb.length() > MAX_CELL_LENGTH) {
						return writeExternalTextArrayFile(rowNum, columnNum, columnType, data);
					} else {
						return sb.toString();
					}
				}

			} else if (columnType.equals(GSType.BYTE_ARRAY)) {
				StringBuilder sb = new StringBuilder();
				for (byte b : row.getByteArray(columnNum)) {
					sb.append(Byte.toString(b)).append(",");
				}
				if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
				if (sb.length() > MAX_CELL_LENGTH) {
					return writeExternalTextArrayFile(rowNum, columnNum, columnType, sb.toString().split(","));
				} else {
					return sb.toString();
				}

			} else if (columnType.equals(GSType.SHORT_ARRAY)) {
				StringBuilder sb = new StringBuilder();
				for (short b : row.getShortArray(columnNum)) {
					sb.append(Short.toString(b)).append(",");
				}
				if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
				if (sb.length() > MAX_CELL_LENGTH) {
					return writeExternalTextArrayFile(rowNum, columnNum, columnType, sb.toString().split(","));
				} else {
					return sb.toString();
				}

			} else if (columnType.equals(GSType.INTEGER_ARRAY)) {
				StringBuilder sb = new StringBuilder();
				for (int b : row.getIntegerArray(columnNum)) {
					sb.append(Integer.toString(b)).append(",");
				}
				if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
				if (sb.length() > MAX_CELL_LENGTH) {
					return writeExternalTextArrayFile(rowNum, columnNum, columnType, sb.toString().split(","));
				} else {
					return sb.toString();
				}

			} else if (columnType.equals(GSType.LONG_ARRAY)) {
				StringBuilder sb = new StringBuilder();
				for (long b : row.getLongArray(columnNum)) {
					sb.append(Long.toString(b)).append(",");
				}
				if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
				if (sb.length() > MAX_CELL_LENGTH) {
					return writeExternalTextArrayFile(rowNum, columnNum, columnType, sb.toString().split(","));
				} else {
					return sb.toString();
				}

			} else if (columnType.equals(GSType.FLOAT_ARRAY)) {
				StringBuilder sb = new StringBuilder();
				for (float b : row.getFloatArray(columnNum)) {
					sb.append(Float.toString(b)).append(",");
				}
				if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
				if (sb.length() > MAX_CELL_LENGTH) {
					return writeExternalTextArrayFile(rowNum, columnNum, columnType, sb.toString().split(","));
				} else {
					return sb.toString();
				}

			} else if (columnType.equals(GSType.DOUBLE_ARRAY)) {
				StringBuilder sb = new StringBuilder();
				for (double b : row.getDoubleArray(columnNum)) {
					sb.append(Double.toString(b)).append(",");
				}
				if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
				if (sb.length() > MAX_CELL_LENGTH) {
					return writeExternalTextArrayFile(rowNum, columnNum, columnType, sb.toString().split(","));
				} else {
					return sb.toString();
				}

			} else if (columnType.equals(GSType.TIMESTAMP_ARRAY)) {
				StringBuilder sb = new StringBuilder();
				for (java.util.Date b : row.getTimestampArray(columnNum)) {
					sb.append(dateFormat.format(b)).append(",");
				}
				if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
				if (sb.length() > MAX_CELL_LENGTH) {
					return writeExternalTextArrayFile(rowNum, columnNum, columnType, sb.toString().split(","));
				} else {
					return sb.toString();
				}

			} else {
				// "A data type that cannot be processed has been detected"
				throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_4")
						+": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()
						+"] rowNum=["+rowNum+"] columnNum=["+columnNum+"] columnType=["+columnType+"]");
			}

		} catch ( GSEIException e ){
			throw e;

		} catch (Exception e) {
			// "An error occurred in the character string conversion process of column data"
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_37")
					+": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()
					+"] rowNum=["+rowNum+"] columnNum=["+columnNum+"] columnType=["+columnType
					+"] msg=["+e.getMessage()+"]", e);
		}

	}

	/**
	 * Outputs an external file as a text file.
	 *
	 * @param rowIndex Row number
	 * @param columnNum Column number
	 * @param type Column type
	 * @param sb Output string data
	 * @return Output raw data file path string
	 */
	private String writeExternalTextFile(int rowIndex, int columnNum, GSType type, StringBuilder sb) throws GSEIException{
		String fileName = "";
		BufferedWriter writer = null;
		try {
			// // Operationally, character strings and spatial information files are specifications up to 128KB (GridStore V1.5)
			// Object FIle: FileName_ContainerName_RowID_ColumnID.GSType

			// External file name
			fileName = getExternalFileNameBase() + "_" + rowIndex + "_" + columnNum + "." + type.toString().toLowerCase();

			// File writing
			File outputFile = new File(m_targetPath, fileName);
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), GSConstants.ENCODING_CSV));
			writer.write(sb.toString());
			writer.flush();

			// Format indicating an external file    @Column type: External file name
			return "@" + type.toString().toUpperCase() + ":"+ fileName;

		} catch (Exception e) {
			// D00706: An unexpected error occurred while being output to external data file.
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_6")
					+": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()+"] rowNum=["+rowIndex
					+"] columnNum=["+columnNum+"] columnType=["+type+"] fileName=["+fileName+"]", e);
		} finally {
			try {
				if ( writer != null ) writer.close();
			} catch ( Exception e ){}
		}
	}
	/**
	 * Outputs an external file as a text file.
	 *
	 * @param rowIndex Row number
	 * @param columnNum Column number
	 * @param type Column type
	 * @param sb Output string data
	 * @return Output raw data file path string
	 */
	private String writeExternalTextArrayFile(int rowIndex, int columnNum, GSType type, String[] data) throws GSEIException{
		String fileName = "";
		CSVWriter writer = null;
		try {
			// // Operationally, character strings and spatial information files are specifications up to 128KB (GridStore V1.5)
			// Object FIle: FileName_ContainerName_RowID_ColumnID.GSType

			// External file name
			fileName = getExternalFileNameBase() + "_" + rowIndex + "_" + columnNum + "." + type.toString().toLowerCase();

			// File writing
			File outputFile = new File(m_targetPath, fileName);
			writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(outputFile), GSConstants.ENCODING_CSV), SEPARATOR, QUOTE, ESCAPE);
			writer.writeNext(data);
			writer.flush();

			// Format indicating an external file    @Column type: External file name
			return "@" + type.toString().toUpperCase() + ":"+ fileName;

		} catch (Exception e) {
			// D00706: An unexpected error occurred while being output to external data file.
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_6")
					+": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()+"] rowNum=["+rowIndex
					+"] columnNum=["+columnNum+"] columnType=["+type+"] fileName=["+fileName+"]", e);
		} finally {
			try {
				if ( writer != null ) writer.close();
			} catch ( Exception e ){}
		}
	}

	private String getExternalFileNameBase() throws GSEIException {
		String fileName = m_containerInfo.getFileBaseName();

		if ( m_outputMode == OUTPUT_MODE.MULTI ){
			if ( m_multiExternalFileNameBase == null ){
				m_multiExternalFileNameBase = super.encodingDbAndContainer(m_containerInfo);
			}
			fileName += "_" + m_multiExternalFileNameBase;
		}

		return fileName;
	}


	/**
	 * Outputs an external file as a binary file.
	 *
	 * @param rowIndex Row number
	 * @param columnNum Column number
	 * @param rowData GridStore row information class
	 *
	 * @return Output raw data file path string
	 */
	private String writeExternalBinaryFile(int rowIndex, int columnNum, Row rowData) throws GSEIException {
		BufferedOutputStream output = null;
		BufferedInputStream input = null;
		String fileName = "";
		try {
			// BLOB is output as a separate file
			// Blob FIle: FileName_ContainerName_RowID_ColumnID.blob

			// External file name
			fileName = getExternalFileNameBase() + "_" + rowIndex + "_" + columnNum + ".blob";
			File blobFile = new File(m_targetPath, fileName);

			java.sql.Blob blob = rowData.getBlob(columnNum);
			if ( blob.length() == 0 ){
				return "";
			}

			// File output
			int len = 0;
			byte[] buffer = new byte[1024];
			input = new BufferedInputStream(blob.getBinaryStream());
			output = new BufferedOutputStream(new FileOutputStream(blobFile));
			while ( (len=input.read(buffer)) != -1 ) {
				output.write(buffer, 0, len);
			}
			output.flush();

			return "@BLOB:" + fileName ;

		} catch (Exception e) {
			// "An error occurred while processing the output of the external BLOB file"
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_7")
					+": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()+"] rowNum=["+rowIndex
					+"] columnType=["+GSType.BLOB+"] externalFile=["+fileName+"]", e);
		} finally {
			try {
				if ( output != null ) output.close();
				if ( input != null ) input.close();
			} catch ( Exception e ){}
		}
	}


	/**
	 * External BLOB file read processing method
	 *
	 * @param row GridStore row information class
	 * @param blob BLOB data
	 * @param csvValue BLOB filename string
	 * @param columnNum Number of column rows
	 * @return GridStore row information class
	 */
	private Row readExternalBinaryFile(Row row, java.sql.Blob blob, String csvValue, int columnNum) throws GSEIException {
		InputStream blobFile = null;
		OutputStream blobBuffer = null;
		byte[] buffer = new byte[GSConstants.READ_BUFFER_SIZE];
		File inputFile = null;
		String fileName = null;
		try {
			// Read file name
			fileName = getFilePath(csvValue);
			inputFile = new File(m_targetPath, fileName);

			int len = 0;
			blobFile = new BufferedInputStream(new FileInputStream(inputFile));
			blobBuffer = new BufferedOutputStream(blob.setBinaryStream(1));
			while ((len = blobFile.read(buffer)) > -1) {
				blobBuffer.write(buffer, 0, len);
			}
			blobBuffer.flush();

			// Blob export to row
			row.setBlob(columnNum, blob);

			return row;

		} catch (Exception e) {
			// "An error occurred while reading the BLOB"
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_25")
					+": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()+"] msg=["+e.getMessage()
					+"] externalFile=["+fileName+"] csvFile=["+m_file.getName()+"] line=["+m_lineCount+"]", e);
		} finally {
			try {
				if (blobFile != null)
					blobFile.close();
				if (blobBuffer != null)
					blobBuffer.close();
			} catch (Exception err) {
				// Ignore file close error
			}
		}
	}

	/**
	 * CSV format file header output processing method
	 *
	 * @param hostname GridStore server name string
	 * @param user Username string
	 * @param top Whether it is the beginning of the file
	 */
	private void csvWriteHeader(String fileName, String user) throws GSEIException {
		try {
			// header1
			// [#Date and time information #Host name #GridStore version]
			Calendar cal = Calendar.getInstance();
			String date = String.format("%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS.%1$tL%1$tz", cal);
			String GSVersion = "GridDB "+com.toshiba.mwcloud.gs.tools.expimp.messageResource.VERSION;
			if (hostname == null) hostname = "";
			String[] header1 = { "#" + date + " " + hostname + " " + GSVersion };
			// header2
			// [#User:username]
			String[] header2 = { "#User:" + user };
			// config
			// [%,(Meta Info FileName)_properties.json]
			String[] config = { "%", fileName + ToolConstants.FILE_EXT_METAINFO };
			m_outFile.writeNext(header1);
			m_outFile.writeNext(header2);
			m_outFile.writeNext(config);

			m_outFile.flush();

		} catch (Exception e) {
			// "An error occurred in CSV format header output processing"
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWCSV_8")
					+ ": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()+" ] path=["+m_file.getAbsolutePath()
					+"] msg=["+ e.toString()+"]", e);
		}
	}

	private void csvWriteHeaderContainer() throws GSEIException {
		String[] containerName = {"$", m_containerInfo.getFullName() };
		m_outFile.writeNext(containerName);
	}


}
