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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.toshiba.mwcloud.gs.ColumnInfo;
import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.GSType;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.tools.common.data.ToolConstants;
import com.toshiba.mwcloud.gs.tools.common.data.ToolContainerInfo;
//import java.sql.Blob;
import com.toshiba.mwcloud.gs.tools.expimp.util.Utility;

/**
 * Binary format raw data I/O processing class
 *
 *
 */
public class rowDataFileIO extends GSEIFileIO{
	/**
	 * nternationalized message resource
	 */
	private static ResourceBundle messageResource;


	//-------------------------------
	// Definition
	//-------------------------------
	/** File name management area for Zip files */
	private static final int ZIP_HEADER_SIZE		= 104;

	/** Maximum entry size (MB) */
	private static final double LIMIT_ENTRY_SIZE	= 5;

	//-------------------------------
	// Common
	//-------------------------------
	/** Zip file entry */
	private ZipEntry m_entry;

	/** Number of Zip file entries    (Serial number for each container) */
	private int m_entryNum;


	//-------------------------------
	// read related
	//-------------------------------
	/** ZipInputStream object */
	private ZipInputStream m_inSingle;

	/** List of files to be read */
	private List<String> m_fileNameList;

	/** Number of Zip files read (in container) */
	private int m_currentFileNum;

	/** Raw data size after serialization */
	private long m_objSize;

	//-------------------------------
	// write related
	//-------------------------------
	/** ZipOutputStream object  */
	private ZipOutputStream m_outSingle;

	/** Zip file size limit (Byte) */
	private int m_zipLimitSize = -1;

	/** Total size of Zip files currently being output*/
	private double m_currentFileSize;

	/** Total size of entries currently being output */
	private int m_entrySize;

	/** Zip file split number */
	/** (In the case of single format, the serial number of each container, in the case of multi format, the entire serial number)*/
	private int m_zipFileNum;

	/** Serialized object */
	private RowSerialize m_serialize;

	// Row size byte size (long is 8 bytes)
	private int rowSizeLength = 8;

	// Row size buffer when writing
	private ByteBuffer bufRowSizeWrite = ByteBuffer.allocate(rowSizeLength);

	// Row size buffer when reading
	private byte[] bufRowSizeRead = new byte[rowSizeLength];

	// Buffer when reading row data
	private byte[] bufRowData = new byte[1024*100];


	/**
	 * Constructor
	 *
	 * @param cli Command parameter information class
	 */
	rowDataFileIO(commandLineInfo cli) {
		super(cli);
		messageResource = Utility.getResource();

		m_currentFileSize = 0;
		m_entrySize = 0;
		m_zipFileNum = 0;
		m_entryNum = 0;
	}

	// バイナリはSimpleDateFormatを使わないので、処理はなし
	public void changeDateFormat() {
	}

	/**
	 * Returns the entry name of the Zip file.
	 *
	 * @param containerName Container name
	 * @param rowNum Row number
	 * @return Row file name
	 */

	private String getEntryName(ToolContainerInfo contInfo){
		String name = null;
		if ( contInfo.getContainerInternalFileName() != null ) {
			name = contInfo.getContainerInternalFileName();
		} else {
			if ( contInfo.getVersion() != null && (contInfo.getVersion().startsWith("3") || contInfo.getVersion().startsWith("2"))){
				if ( contInfo.getDbName()==null || contInfo.getDbName().equalsIgnoreCase(ToolConstants.PUBLIC_DB) ){
					name = contInfo.getName();
				} else {
					name = contInfo.getFullName();
				}
			} else {
				name = contInfo.getFullName();
			}
		}
		name = name + GSConstants.FILENAME_SEPARATOR + m_entryNum
				+ GSConstants.FILE_EXT_BINARY_ROW;
		return name;
	}



	/**
	 * Returns the filename of the Zip file.
	 *
	 * @return Zip file name
	 */
	/*
	private String getZipFileName(){
		String fileName = m_containerInfo.getFileBaseName()
				+GSConstants.FILENAME_SEPARATOR_DIV + m_zipFileNum ;

		switch(m_outputMode){
		case SINGLE:
			fileName += GSConstants.FILE_EXT_BINARY_SINGLE;
			break;
		case MULTI:
			fileName += GSConstants.FILE_EXT_BINARY_MULTI;
			break;
		}

		return fileName;
	}
	*/


	//**********************************************************************
	// Start/end of file output
	//**********************************************************************
	/**
	 * Prepare for Zip file output.
	 *
	 */
	public void startWrite(){
		// Specify the maximum size of the Zip file
		m_zipLimitSize = cmdLineInfo.getFileSizeLimit()*1024*1024; // The size is specified in MB.

		// Serialized object
		m_serialize = new RowSerialize();
	}

	/**
	 * Quit Zip file output.
	 */
	public void endWrite(){
		closeZipFile();
	}


	//**********************************************************************
	// Start/end of container unit
	//**********************************************************************
	/**
	 * Prepare to output container data.
	 *
	 * @param containerInfo Container information
	 */
	public void startWriteContainer(ToolContainerInfo containerInfo) throws GSEIException {
		m_containerInfo = containerInfo;

		// No file output in test mode
		if (cmdLineInfo.getTestFlag()) return;

		// Initialize per-container variables
		m_entryNum = 0;
		m_entrySize = 0;

		// Object generation for serialization
		List<GSType> columnTypeList = new ArrayList<GSType>(containerInfo.getColumnInfoList().size());
		for ( ColumnInfo info : containerInfo.getColumnInfoList() ){
			columnTypeList.add(info.getType());
		}
		RowSerialize.setColumnTypeList(columnTypeList);
		RowSerialize.setMetaFileVersion(GSConstants.EXPORT_MNG_FILE_VERSION);

		// Create a filename base
		String fileName = super.createRowFileName(m_containerInfo, cmdLineInfo);
		// Number of threads
		if ( Thread.currentThread() instanceof ExportThread ){
			int id = ((ExportThread)Thread.currentThread()).getThreadNo();
			fileName += GSConstants.FILENAME_SEPARATOR + "t" + id;
		}
		m_containerInfo.setFileBaseName(fileName);

		// V4.5 Do not output to a file if export only container definition is specified
		if (cmdLineInfo.getSchemaOnlyFlag()) return;

		// File creation
		switch(m_outputMode){
		case SINGLE:
			m_zipFileNum = 0;
			createZipFile();
			break;

		case MULTI:
			if ( m_outSingle == null ){
				createZipFile();
			}
			String tmp = super.encodingDbAndContainer(m_containerInfo, false);
			if ( !tmp.equals(m_containerInfo.getFullName()) ){
				String n = super.encodingDbAndContainer(m_containerInfo);
				m_containerInfo.setContainerInternalFileName(n);
			}
			break;
		//case SINGLE_LARGE:
		//	break;
		}
	}

	/**
	 * Ends the output of container data.
	 */
	public void endWriteContainer() throws GSEIException {
		// No file output in test mode
		if (cmdLineInfo.getTestFlag()) return;
		// V4.5 Do not output to a file if export only container definition is specified
		if (cmdLineInfo.getSchemaOnlyFlag()) return;

		try {
			// ロウファイル名を保存する
			// 生成していないロウデータファイル名を追記しない
			if (m_containerInfo.getContainerFile() == null || !m_containerInfo.getContainerFileList().contains(m_file.getName())) {
				m_containerInfo.addContainerFile(m_file.getName());//rowCsvFileIOと同じ処理に変更
			}
			
			switch(m_outputMode){
			case SINGLE:
				// Close zip file
				closeZipFile();
				break;

			case MULTI:
				if ( m_entry != null ){
					// Close entry
					closeEntry();
				}
				break;
			}
			RowSerialize.removeColumnTypeList();

		} catch (Exception e) {
			// An error occurred in the binary format file output processing of raw data
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWBNY_2")
					+": containerName=["+m_containerInfo.getFullName()
					+"] msg=["+e.getMessage()+"]", e);
		}
	}

	private void closeEntry() throws IOException{
		m_outSingle.flush();
		m_outSingle.closeEntry();

		// File size calculation
		m_currentFileSize += m_entry.getCompressedSize();

		m_entry = null;
		m_entrySize = 0;
		m_entryNum++;
	}


	//**********************************************************************
	// Processing ROW data
	//**********************************************************************
	/**
	 * Write one Row data to a file.
	 *
	 * @param ToolContainerInfo Container information object
	 * @param rowNum Row number
	 * @param row Row data
	 */
	public void writeRow(Row row, int rowNum) throws GSEIException {
		try {
			// Entry start
			if ( m_entry == null ){
				if ( m_outSingle == null ){
					createZipFile();
				}
				String rowFileName = getEntryName(m_containerInfo);
				m_entry = new ZipEntry(rowFileName);
				m_outSingle.putNextEntry(m_entry);
				m_currentFileSize += rowFileName.getBytes("utf-8").length + ZIP_HEADER_SIZE;
				m_entrySize = 0;
			}

			// Serialize Row into a byte array
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);

			RowSerialize.setRow(row);
			oos.writeObject(m_serialize);
			byte b[] = bos.toByteArray();

			// Write size and data to file
			long rowDataSize = b.length;
			bufRowSizeWrite.clear();
			m_outSingle.write(bufRowSizeWrite.putLong(rowDataSize).array());
			m_outSingle.write(b);
			m_entrySize += rowSizeLength + rowDataSize;

			// The unit type of m_entrySize is bytes
			if ( m_entrySize > (1024 * 1024 * LIMIT_ENTRY_SIZE) ){
				// Close entry
				closeEntry();

				// If the Zip file exceeds the specified size, close the file
				if ( m_currentFileSize >= m_zipLimitSize ){
					m_containerInfo.addContainerFile(createRowFileName());
					closeZipFile();
				}
			}

		} catch ( GSEIException e ){
			throw e;
		} catch ( Exception e ){
			// An error occurred in the binary format file output processing of raw data
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWBNY_2")
					+": containerName=["+m_containerInfo.getFullName()
					+"] msg=["+e.getMessage()+"]", e);
		}

	}


	//********************************************************************
	// Zip file
	//********************************************************************
	/**
	 * Create a single container format file.
	 */
	private void createZipFile() throws GSEIException{
		try {
			m_currentFileSize = 0;

			m_file = new File(m_targetPath, createRowFileName());
			m_outSingle = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(m_file)));

		} catch ( Exception e ){
			// An error occurred in binary file output
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWBNY_23")
					+": containerName=["+m_containerInfo.getFullName()
					+"] path=["+m_file.getAbsolutePath()+"]", e);
		}
	}

	/**
	 * Read zip file
	 *
	 * @param file
	 * @throws GSEIException
	 */
	private void readZipFile(File file) throws GSEIException{
		try {
			m_file = file;
			m_inSingle = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
			m_entry = null;

		} catch ( Exception e ){
			// An error occurred while reading the binary file
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWBNY_27")
					+": containerName=["+m_containerInfo.getFullName()
					+"] path=["+m_file.getAbsolutePath()+"] msg=["+e.getMessage()+"]", e);
		}
	}

	/**
	 * Close the single container format file.
	 */
	private void closeZipFile(){
		try {
			if ( m_outSingle != null ){
				if ( m_entry != null ){
					m_outSingle.flush();
					m_outSingle.closeEntry();
					m_entry = null;
					m_entrySize = 0;
				}

				m_outSingle.flush();
				m_outSingle.close();
				m_outSingle = null;

				m_currentFileSize = 0;
				m_zipFileNum++;

			}

			if ( m_inSingle != null ){
				m_entry = null;
				m_file = null;

				m_inSingle.close();
				m_inSingle = null;
			}

		} catch ( Exception e ){}
	}

	/**
	 * Generates the file name of the raw file.
	 *
	 * @return file name
	 */
	String createRowFileName() throws GSEIException {
		String fileName = m_containerInfo.getFileBaseName();
		// Division number
		fileName += GSConstants.FILENAME_SEPARATOR_DIV + m_zipFileNum;

		switch(m_outputMode){
		case SINGLE:
			fileName += GSConstants.FILE_EXT_BINARY_SINGLE;
			break;
		case MULTI:
			fileName += GSConstants.FILE_EXT_BINARY_MULTI;
			break;
		}

		return fileName;
	}


	//***************************************************************
	// Read
	//***************************************************************
	/**
	 * Open the Zip file (multi-format) and look for the entry that corresponds to the container name.
	 *
	 * @param fileName file name
	 */
	private void readMCFile(String fileName) throws GSEIException {
		try {
			// [Memo] The container data storage order of the binary file and
			// the container specification order (--container option) to be processed are different.
			// In case, if the container data is not found in the binary file, read it again from the beginning of the file.
			for ( int i = 0; i < 2; i++ ){
				File file = new File(m_targetPath, fileName);
				if ( (m_file == null) || !m_file.equals(file) ){
					// If it is different from the loaded file, load a new file
					closeZipFile();
					readZipFile(file);
					m_entry = m_inSingle.getNextEntry();
				}

				// Find the internal entry that matches the specified container name
				String entryName = getEntryName(m_containerInfo);
				while( m_entry != null ){
					String name = m_entry.getName();
					if ( name.equals(entryName)){
						break;
					}
					m_entry = m_inSingle.getNextEntry();
				}

				if ( m_entry != null ){
					break;
				} else {
					// At the first time, once you read to the end, close once
					if ( i == 0 ) closeZipFile();
				}
			}
			if ( m_entry == null ){
				// Raw data for the specified container cannot be found
				throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWBNY_19")
						+": containerName=["+m_containerInfo.getFullName()+"] path=["+m_file.getAbsolutePath()+"]");
			}

		} catch ( GSEIException e ){
			throw e;
		} catch ( Exception e ){
			// An error occurred while reading the binary file
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWBNY_9")
					+": containerName=["+m_containerInfo.getFullName()
					+"] msg=["+e.getMessage()+"] file=["+fileName+"]", e);
		}
	}

	/**
	 * Load the export data of the container.
	 *
	 * @param contInfo Target container information object
	 * @param fileNameList File name list
	 */
	public void readContainer(ToolContainerInfo contInfo, List<String> fileNameList) throws GSEIException{
		m_containerInfo = contInfo;
		m_fileNameList = fileNameList;
		m_zipFileNum = 0;
		m_currentFileNum = 0;
		m_timeFileIO = 0;
		m_timeFileRead = 0;
		m_timeFileRead2 = 0;

		m_entryNum = 0;

		long start = System.currentTimeMillis();
		try {

			// File list extension check
			String fileName = m_fileNameList.get(0);
			if ( fileName.endsWith(GSConstants.FILE_EXT_BINARY_MULTI) ){
				m_outputMode = OUTPUT_MODE.MULTI;
			} else if ( fileName.endsWith(GSConstants.FILE_EXT_BINARY_SINGLE) ){
				m_outputMode = OUTPUT_MODE.SINGLE;
			}

			// Read the first file
			if ( m_outputMode == OUTPUT_MODE.MULTI ){
				// If an MC file is specified, look for an internal entry
				readMCFile(fileName);

			} else if ( m_outputMode == OUTPUT_MODE.SINGLE ){
				// When SC file is specified
				readZipFile(new File(m_targetPath, fileName));
			}

			m_currentFileNum++;

			// Serialization object settings
			List<GSType> columnTypeList = new ArrayList<GSType>();
			for ( ColumnInfo info : contInfo.getColumnInfoList() ){
				columnTypeList.add(info.getType());
			}
			RowSerialize.setColumnTypeList(columnTypeList);
			RowSerialize.setMetaFileVersion(contInfo.getVersion());

		} catch ( GSEIException e ){
			throw e;
		} catch ( Exception e ){
			// An error occurred while reading the binary file
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWBNY_24")
					+": containerName=["+m_containerInfo.getFullName()
					+"] msg=["+e.getMessage()+"]", e);
		} finally {
			long end = System.currentTimeMillis();
			m_timeFileIO += (end-start);
		}
	}

	/**
	 * Check for the next ROW data.
	 *
	 * @return true:If there is the next Row data / false:none
	 */
	public boolean hasNextRow() throws GSEIException {
		long start = System.currentTimeMillis();

		try{
			// Entry loop
			while ( true ){
				if ( m_entry == null ){
					m_entry = m_inSingle.getNextEntry();

					if ( m_entry == null ){
						// End of file  ⇒  If there is still a file to read, read it
						if ( m_currentFileNum < m_fileNameList.size() ){
							closeZipFile();
							readZipFile(new File(m_targetPath, m_fileNameList.get(m_currentFileNum++)));
							continue;
						} else {
							RowSerialize.removeColumnTypeList();
							return false;
						}
					}
					// Check entry name
					if ( m_outputMode == OUTPUT_MODE.MULTI ){
						String name = m_entry.getName();
						if ( name.equals(getEntryName(m_containerInfo))){
						} else {
							// Break with the next container
							RowSerialize.removeColumnTypeList();
							return false;
						}
					}
				}

				// Checking stored data
				int writtenSize = 0;
				int writeSize = rowSizeLength;
				int allSize = 0;
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				while ((writtenSize = m_inSingle.read(bufRowSizeRead, 0, writeSize)) != -1) {
					bo.write(bufRowSizeRead, 0, writtenSize);
					allSize += writtenSize;
					if ( allSize == rowSizeLength ){
						break;
					} else {
						writeSize = rowSizeLength - allSize;
					}
			    }

				if ( allSize != 0 ){
					ByteBuffer b = ByteBuffer.wrap(bo.toByteArray());
					m_objSize = b.getLong();
					return true;
				} else {
					// End of entry  ⇒  To the next entry
					m_entry = null;
					m_entryNum++;
				}
			}

		} catch (Exception e){
			// An error occurred while reading the binary file
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWBNY_25")
					+": containerName=["+m_containerInfo.getFullName()
					+"] msg=["+e.getMessage()+"]", e);
		} finally {
			long end = System.currentTimeMillis();
			m_timeFileIO += (end-start);
		}
	}

	/**
	 * Reads and returns ROW data.
	 *
	 * @param row ROW object
	 * @return ROW object that stores the read data
	 */
	public Row readRow(Container<?, Row> container) throws GSEIException{
		try {
			long start = System.currentTimeMillis();

			Row row = container.createRow();

			// Read from file
			int writeSize = (m_objSize>bufRowData.length?bufRowData.length:(int)m_objSize);
			long allSize = 0;
			int writtenSize = 0;
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			while ((writtenSize = m_inSingle.read(bufRowData, 0, writeSize)) != -1) {
				bo.write(bufRowData, 0, writtenSize);
				allSize += writtenSize;
				if ( allSize == m_objSize ){
					break;
				} else {
					writeSize = ((m_objSize-allSize)>bufRowData.length?bufRowData.length:(int)(m_objSize-allSize));
				}
		    }
			if ( allSize == 0 ){
				return row;
			}

			// Deserialize
			RowSerialize.setRow(row);
			// [memo] RowSeriarize is newly created without permission, so specify row with the above method.
			byte[] test = bo.toByteArray();
			ByteArrayInputStream bai = new ByteArrayInputStream(test);
			ObjectInputStream ois = new ObjectInputStream(bai);
			ois.readObject();

			long end = System.currentTimeMillis();
			m_timeFileIO += (end-start);

			return RowSerialize.getRow();

		} catch ( Exception e ){
			// An error occurred while reading the binary file
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWBNY_26")
					+": containerName=["+m_containerInfo.getFullName()
					+"] msg=["+e.getMessage()+"]", e);
		}

	}


	/**
	 * ロウデータ変換処理メソッド
	 *
	 * @param row GridStoreロウデータ情報クラス
	 * @param objList ローカルデータオブジェクト配列
	 * @param contInfo ローカルコンテナ情報クラス
	 * @return GridStoreロウデータ情報クラス
	 */
	/*
	private Row convertObjToRow(Row row, Object[] objList, containerInfo contInfo) throws GSEIException {
		try {
			int index = 0;
			for (Object obj : objList) {
				GSType type = contInfo.getContainerInfo().getColumnInfo(index).getType();

				switch (type){
				case TIMESTAMP:
					row.setTimestamp(index, (Date) obj);
					break;
				case INTEGER:
					Integer ii = (Integer) obj;
					row.setInteger(index, ii.intValue());
					break;
				case STRING:
					row.setString(index, (String) obj);
					break;
				case BOOL:
					Boolean b = (Boolean) obj;
					row.setBool(index, b.booleanValue());
					break;
				case LONG:
					Long l = (Long) obj;
					row.setLong(index, l.longValue());
					break;
				case FLOAT:
					Float f = (Float) obj;
					row.setFloat(index, f.floatValue());
					break;
				case DOUBLE:
					Double d = (Double) obj;
					row.setDouble(index, d.doubleValue());
					break;
				case SHORT:
					Short s = (Short) obj;
					row.setShort(index, s.shortValue());
					break;
				case BYTE:
					Byte by = (Byte) obj;
					row.setByte(index, by.byteValue());
					break;
				case BOOL_ARRAY:
					row.setBoolArray(index, (boolean[]) obj);
					break;
				case STRING_ARRAY:
					row.setStringArray(index, (String[]) obj);
					break;
				case BYTE_ARRAY:
					row.setByteArray(index, (byte[]) obj);
					break;
				case SHORT_ARRAY:
					row.setShortArray(index, (short[]) obj);
					break;
				case INTEGER_ARRAY:
					row.setIntegerArray(index, (int[]) obj);
					break;
				case LONG_ARRAY:
					row.setLongArray(index, (long[]) obj);
					break;
				case FLOAT_ARRAY:
					row.setFloatArray(index, (float[]) obj);
					break;
				case DOUBLE_ARRAY:
					row.setDoubleArray(index, (double[]) obj);
					break;
				case TIMESTAMP_ARRAY:
					row.setTimestampArray(index, (Date[]) obj);
					break;
				case GEOMETRY:
					row.setGeometry(index, Geometry.valueOf((String)obj));
					break;
				case BLOB:
					row.setBlob(index, (java.sql.Blob) obj);
					break;
				default:
					// 処理できないデータ型が検出されました
					log.warn(messageResource.getString("MESS_COMM_ERR_ROWBNY_12")+ ":"+ type.toString());
					throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWBNY_12")
							+": containerName=["+m_containerInfo.getContainerInfo().getName()
							+"] columnType=["+type+"]");
				}
				index++;
			}
			return row;

		} catch (GSEIException e){
			throw e;
		} catch (Exception e) {
			// ロウデータへの変換処理でエラーが発生しました
			log.error(messageResource.getString("MESS_COMM_ERR_ROWBNY_13")+ ":" + e.toString(), e);
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWBNY_13")
					+": containerName=["+m_containerInfo.getContainerInfo().getName()
					+"] msg=["+e.getMessage()+"]", e);
		}
	}
	*/

	/**
	 * GridStoreロウデータからオブジェクトへの変換処理メソッド
	 *
	 * @param contInfo ローカルコンテナ情報クラス
	 * @param row GridStoreロウデータ情報クラス
	 * @return オブジェクト配列
	 */
	/*
	private Object[] convertRowToObj(containerInfo contInfo, Row row) throws GSEIException {
		try {
			List<Object> objList = new ArrayList<Object>();
			for (int index = 0; index < contInfo.getContainerInfo().getColumnCount(); index++) {
				GSType type = contInfo.getContainerInfo().getColumnInfo(index).getType();
				switch(type){
				case BLOB:
					SerialBlob sb = new SerialBlob(row.getBlob(index).getBytes((long) 1, (int) row.getBlob(index).length()));
					objList.add(sb);
					break;
				case GEOMETRY:
					// GEOMETRYはStringでファイルに出力する
					objList.add((row.getGeometry(index)).toString());
					break;
				default:
					objList.add(row.getValue(index));
					break;
				}
			}
			return objList.toArray(new Object[objList.size()]);

		} catch (Exception e) {
			// ロウデータからオブジェクトへの変換処理でエラーが発生しました
			log.error(messageResource.getString("MESS_COMM_ERR_ROWBNY_14")+ ":" + e.toString(), e);
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWBNY_14")
					+": containerName=["+m_containerInfo.getContainerInfo().getName()
					+"] msg=["+e.getMessage()+"]", e);
		}
	}
	*/

}
