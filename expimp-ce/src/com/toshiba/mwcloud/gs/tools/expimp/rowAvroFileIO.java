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
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.avro.LogicalType;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;

import com.toshiba.mwcloud.gs.ColumnInfo;
import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.GSType;
import com.toshiba.mwcloud.gs.Geometry;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.tools.common.data.ToolContainerInfo;
import com.toshiba.mwcloud.gs.tools.expimp.util.Utility;

/**
 * Avro format raw data I/O processing class
 *
 *
 */
public class rowAvroFileIO extends GSEIFileIO{

	private static final String ARRAY = "_ARRAY";

	/**
	 * Internationalized message resource
	 */
	private static ResourceBundle messageResource;

	/** Avro read file object */
	private DataFileReader<GenericRecord> m_inDataFileReader;
	/** List of files to be read */
	private List<String> m_fileNameList;
	/** Number of data files read */
	private int m_currentFileNum;
	/** List of Avro schema fields */
	private List<Field> m_avroFieldList;

	/**
	 * Constructor
	 *
	 * @param cli Command parameter information class
	 */
	public rowAvroFileIO(commandLineInfo cli) {
		super(cli);

		messageResource = Utility.getResource();

		// Register the Logical Type used by Avro format raw data
		AvroGSLogicalType.registerGSLogicalType();
	}


	//**********************************************************************
	// Start/end of file output
	//**********************************************************************
	/**
	 * Exporting in Avro format is not supported and does nothing.
	 */
	public void startWrite(){
	}

	/**
	 * Exporting in Avro format is not supported and does nothing.
	 */
	public void endWrite(){
	}


	//**********************************************************************
	// Start/end of container unit
	//**********************************************************************
	/**
	 * Exporting in Avro format is not supported and does nothing.
	 *
	 * @param containerInfo Container information
	 */
	public void startWriteContainer(ToolContainerInfo containerInfo) throws GSEIException{
	}

	/**
	 * Exporting in Avro format is not supported and does nothing.
	 */
	public void endWriteContainer(){
	}

	//**********************************************************************
	// Processing ROW data
	//**********************************************************************
	/**
	 * Exporting in Avro format is not supported and does nothing.
	 *
	 * @param row Row data
	 * @param rowNum Row number
	 */
	public void writeRow(Row row, int rowNum ) throws GSEIException{
	}



	/**
	 * Read the container data file.
	 *
	 * @param contInfo Container information
	 * @param fileList List of data file names
	 * @throws GSEIException
	 */
	public void readContainer(ToolContainerInfo contInfo, List<String> fileList) throws GSEIException {
		m_containerInfo = contInfo;
		m_fileNameList = fileList;
		m_currentFileNum = 0;

		long start = System.currentTimeMillis();
		try {
			m_timeFileIO = 0;

			createDataFileReader(new File(m_targetPath, m_fileNameList.get(m_currentFileNum)));
			m_currentFileNum++;
			createFieldList();

			// Check if the container information matches the Avro schema
			checkAvroSchema();

		} catch ( GSEIException e ){
			throw e;
		} catch ( Exception e ){
			// There was an error reading the Avro file
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWAVRO_2")+": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()+"] msg=["+e.getMessage()+"]", e);
		} finally {
			long end = System.currentTimeMillis();
			m_timeFileIO += (end-start);
		}

	}

	/**
	 * Create a Reader for the Avro file.
	 *
	 * @param file Avro file
	 * @throws GSEIException
	 */
	private void createDataFileReader(File file) throws GSEIException {

		try {
			m_file = file;
			DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>();
			m_inDataFileReader = new DataFileReader<GenericRecord>(file, datumReader);
		} catch (Exception e) {
			// There was an error reading the Avro file
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWAVRO_1")+": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()
						+"] path=["+m_file.getAbsolutePath()+"] msg=["+e.getMessage()+"]", e);
		}
	}

	/**
	 *  Create a list of Fields from Avro's schema information.
	 */
	private void createFieldList() {
		// Get schema information from Avro
		Schema dataFileReaderSchema = m_inDataFileReader.getSchema();
		// Get field from schema
		m_avroFieldList = dataFileReaderSchema.getFields();
	}

	/**
	 * Close the Reader for the Avro file.
	 *
	 */
	private void closeDataFileReader() {
		if (m_inDataFileReader != null) {
			try {
				m_inDataFileReader.close();
			} catch (Exception e) {}
		}
	}

	/**
	 * Check if there is a next row in Avro file
	 *
	 * @return True if there is the next Row data
	 */
	public boolean hasNextRow() throws GSEIException{

		long start = System.currentTimeMillis();

		try {
			boolean returnVal = m_inDataFileReader.hasNext();

			if (returnVal == false) {
				closeDataFileReader();
				// If there is the next data file, read the next data file
				if ( m_currentFileNum < m_fileNameList.size() ){
					createDataFileReader(new File(m_targetPath, m_fileNameList.get(m_currentFileNum)));
					m_currentFileNum++;
					createFieldList();

					// Check if the container information matches the Avro schema
					checkAvroSchema();

					return hasNextRow();
				}
			}

			return returnVal;

		} catch ( GSEIException e ){
			throw e;
		} catch ( Exception e ){
			// There was an error reading the Avro file
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWAVRO_3")+": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()
						+"] path=["+m_file.getAbsolutePath()+"] msg=["+e.getMessage()+"]", e);
		} finally {
			long end = System.currentTimeMillis();
			m_timeFileIO += (end-start);
		}
	}

	/**
	 * Reads one line of information from the Avro file and creates a row.
	 *
	 * @param container GridStore container class
	 * @rerutn Row of the result of reading one line
	 * @throws GSEIException
	 */
	public Row readRow(Container<?, Row> container) throws GSEIException {

		try {
			// Read one line of information from the file
			GenericRecord genericRecord = null;
			genericRecord = m_inDataFileReader.next(genericRecord);

			Row row = container.createRow();

			// Processing in column data units
			for (int columnNum = 0; columnNum < m_containerInfo.getContainerInfo().getColumnCount(); columnNum++) {
				ColumnInfo col = m_containerInfo.getContainerInfo().getColumnInfo(columnNum);

				setRowValue(container, row, col.getType(), genericRecord, columnNum);
			}

			return row;

		} catch (GSEIException e) {
			throw e;
		} catch (Exception e) {
			// There was an error reading the Avro file
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWAVRO_4") + ": db=["
					+ m_containerInfo.getDbName() + "] containerName=[" + m_containerInfo.getName()
					+ "] path=[" + m_file.getAbsolutePath() + "] msg=[" + e.getMessage()
					+ "]", e);
		}
	}


	/**
	 * Set the data read from the Avro file in the Row object.
	 *
	 * @param container GridStore container class
	 * @param row GridStore row information class
	 * @param columnType Column type
	 * @param record GenericRecord class read from Avro
	 * @param columnNum Column number
	 *
	 * @return GridStore row information class
	 * @throws GSEIException
	 */
	private Row setRowValue(Container<?, Row> container, Row row, GSType columnType,
			GenericRecord record, int columnNum) throws GSEIException {
		try {
			String fieldName = m_avroFieldList.get(columnNum).name();
			Object value = record.get(fieldName);

			if (value == null) {
				row.setNull(columnNum);
			} else if (columnType.equals(GSType.BOOL)) {
				row.setBool(columnNum, ((Boolean)value).booleanValue());
			} else if (columnType.equals(GSType.STRING)) {
				row.setString(columnNum, ((String)value));
			} else if (columnType.equals(GSType.BYTE)) {
				row.setByte(columnNum, ((Byte)value).byteValue());
			} else if (columnType.equals(GSType.SHORT)) {
				row.setShort(columnNum, ((Short)value).shortValue());
			} else if (columnType.equals(GSType.INTEGER)) {
				row.setInteger(columnNum, ((Integer)value).intValue());
			} else if (columnType.equals(GSType.LONG)) {
				row.setLong(columnNum, ((Long)value).longValue());
			} else if (columnType.equals(GSType.FLOAT)) {
				row.setFloat(columnNum, ((Float)value).floatValue());
			} else if (columnType.equals(GSType.DOUBLE)) {
				row.setDouble(columnNum, ((Double)value).doubleValue());
			} else if (columnType.equals(GSType.TIMESTAMP)) {
				row.setTimestamp(columnNum, (Date)value);
			} else if ( columnType.equals(GSType.BLOB)) {
				row.setBlob(columnNum, (Blob)value);
			} else if (columnType.equals(GSType.BOOL_ARRAY)) {
				@SuppressWarnings("unchecked")
				List<Boolean> list = (List<Boolean>) value;
				boolean[] ba = new boolean[list.size()];
				for (int i = 0;i < list.size(); i++) {
					ba[i] = list.get(i).booleanValue();
				}
				row.setBoolArray(columnNum, ba);
			} else if (columnType.equals(GSType.STRING_ARRAY)) {
				@SuppressWarnings("unchecked")
				List<String> list = (List<String>) value;
				String[] strArray = new String[list.size()];
				for (int i = 0;i < list.size(); i++) {
					strArray[i] = list.get(i);
				}
				row.setStringArray(columnNum, strArray);
			} else if (columnType.equals(GSType.BYTE_ARRAY)) {
				@SuppressWarnings("unchecked")
				List<Byte> list = (List<Byte>) value;
				byte[] ba = new byte[list.size()];
				for (int i = 0;i < list.size(); i++) {
					ba[i] = list.get(i).byteValue();
				}
				row.setByteArray(columnNum, ba);
			} else if (columnType.equals(GSType.SHORT_ARRAY)) {
				@SuppressWarnings("unchecked")
				List<Short> list = (List<Short>) value;
				short[] sa = new short[list.size()];
				for (int i = 0;i < list.size(); i++) {
					sa[i] = list.get(i).shortValue();
				}
				row.setShortArray(columnNum, sa);
			} else if (columnType.equals(GSType.INTEGER_ARRAY)) {
				@SuppressWarnings("unchecked")
				List<Integer> list = (List<Integer>) value;
				int[] ia = new int[list.size()];
				for (int i = 0;i < list.size(); i++) {
					ia[i] = list.get(i).intValue();
				}
				row.setIntegerArray(columnNum, ia);
			} else if (columnType.equals(GSType.LONG_ARRAY)) {
				@SuppressWarnings("unchecked")
				List<Long> list = (List<Long>) value;
				long[] la = new long[list.size()];
				for (int i = 0;i < list.size(); i++) {
					la[i] = list.get(i).longValue();
				}
				row.setLongArray(columnNum, la);
			} else if (columnType.equals(GSType.FLOAT_ARRAY)) {
				@SuppressWarnings("unchecked")
				List<Float> list = (List<Float>) value;
				float[] fa = new float[list.size()];
				for (int i = 0;i < list.size(); i++) {
					fa[i] = list.get(i).floatValue();
				}
				row.setFloatArray(columnNum, fa);
			} else if (columnType.equals(GSType.DOUBLE_ARRAY)) {
				@SuppressWarnings("unchecked")
				List<Double> list = (List<Double>) value;
				double[] da = new double[list.size()];
				for (int i = 0;i < list.size(); i++) {
					da[i] = list.get(i).doubleValue();
				}
				row.setDoubleArray(columnNum, da);
			} else if (columnType.equals(GSType.GEOMETRY)) {
				row.setGeometry(columnNum, (Geometry)value);
			} else if (columnType.equals(GSType.TIMESTAMP_ARRAY)) {
				@SuppressWarnings("unchecked")
				List<Date> list = (List<Date>) value;
				Date[] da = new Date[list.size()];
				for (int i = 0;i < list.size(); i++) {
					da[i] = list.get(i);
				}
				row.setTimestampArray(columnNum, da);
			} else {
				// Log output for unknown GS Type
				// A data type that cannot be processed by this version has been detected.
				throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWAVRO_6")+" :type=["+columnType+"]");
			}

			return row;

		} catch (GSEIException e) {
			throw e;
		} catch (Exception e) {
			// An error occurred while setting raw data to GridStore
			throw new GSEIException(messageResource.getString("MESS_COMM_ERR_ROWAVRO_5")
					+ ": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()+"] type=["+columnType
					+"] msg=["+e.getMessage()+"] path=["+m_file.getAbsolutePath()+"] msg=["+e.getMessage()+"]", e);
		}
	}

	/**
	 * Generates the file name of the raw file.
	 *
	 * @return File name
	 */
	String createRowFileName(){
		String name = m_containerInfo.getFileBaseName();
		name += GSConstants.FILE_EXT_AVRO;
		return name;
	}

	/**
	 * Check if the container information and the Avro schema information match.
	 *
	 * @throws GSEIException
	 */
	private void checkAvroSchema() throws GSEIException {
		if (cmdLineInfo.getSchemaCheckSkipFlag()) {
			// If the option to uncheck the schema information is specified, it will not be checked.
			return;
		}
		// Get schema information from Avro
		Schema dataFileReaderSchema = m_inDataFileReader.getSchema();

		checkDbName(dataFileReaderSchema);
		checkContName(dataFileReaderSchema);
		checkColumnCount(dataFileReaderSchema);
		checkColumnNameType(dataFileReaderSchema);
		checkNotNullConstraint(dataFileReaderSchema);
	}

	/**
	 * Check if the database name of the container information and the Avro schema information match.
	 *
	 * @param dataFileReaderSchema Avro schema
	 * @throws GSEIException
	 */
	private void checkDbName(Schema dataFileReaderSchema) throws GSEIException {
		String dbName = m_containerInfo.getDbName();
		String avroNamespace = dataFileReaderSchema.getNamespace();

		if (!dbName.equals(avroNamespace)) {
			// Database name mismatch
			// A raw data file with a mismatched database name was detected
			String msg = messageResource.getString("MESS_COMM_ERR_ROWAVRO_7")
					+ ": db=["+avroNamespace+"] schemaDbName=["+m_containerInfo.getDbName()+"] path=["+m_file.getAbsolutePath()+"]";
			throw new GSEIException(msg);
		}
	}

	/**
	 * Check if the container name in the container information and the Avro schema information match.
	 *
	 * @param dataFileReaderSchema Avro schema
	 * @throws GSEIException
	 */
	private void checkContName(Schema dataFileReaderSchema) throws GSEIException {
		// Since it is possible to import with a different name from the container name at the time of creating the archive,
		// the match check between the container name and the Avro schema information is not performed.
	}

	/**
	 * Check if the number of columns in the container information and Avro schema information match.
	 *
	 * @param dataFileReaderSchema Avro schema
	 * @throws GSEIException
	 */
	private void checkColumnCount(Schema dataFileReaderSchema) throws GSEIException {
		int columnCount = m_containerInfo.getContainerInfo().getColumnCount();
		int avroFieldCount = m_avroFieldList.size();

		if (columnCount != avroFieldCount) {
			// The number of columns does not match
			// A raw data file with an inconsistent number of columns was detected.
			String msg = messageResource.getString("MESS_COMM_ERR_ROWAVRO_9")
					+ ": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()+"] columnNum=["+avroFieldCount+"] schemaColumnNum=["+m_containerInfo.getContainerInfo().getColumnCount()+"] path=["+m_file.getAbsolutePath()+"]";
			throw new GSEIException(msg);
		}
	}

	/**
	 * Check if the column name data type of the container information and Avro schema information match.
	 *
	 * @param dataFileReaderSchema Avro schema
	 * @throws GSEIException
	 */
	private void checkColumnNameType(Schema dataFileReaderSchema) throws GSEIException {
		int columnCount = m_containerInfo.getContainerInfo().getColumnCount();

		// Performed after checking that the number of columns in the container information
		// and the number of fields in avro match.
		for (int i = 0; i < columnCount; i++) {
			ColumnInfo columnInfo = m_containerInfo.getContainerInfo().getColumnInfo(i);
			Field avroField = m_avroFieldList.get(i);

			// Check if the column name of the container information and the column name information of avro match
			String columnName = columnInfo.getName();
			// If doc is defined in Avro schema, use doc setting value as column name
			String avroColumnName = avroField.doc();
			if (avroColumnName == null) {
				avroColumnName = avroField.name();
			}

			if (!columnName.equals(avroColumnName)) {
				// Column names do not match
				// A raw data file with mismatched column names was detected
				String msg = messageResource.getString("MESS_COMM_ERR_ROWAVRO_10")
						+ ": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()+"] columnName=["+avroColumnName+"] schemaColumnName=["+columnName+"] path=["+m_file.getAbsolutePath()+"]";
				throw new GSEIException(msg);
			}

			// Check if the data type of the container information and the data type information defined in the logicalType of avro match.
			GSType columnType = columnInfo.getType();
			Schema avroFieldSchema = avroField.schema();
			// Get the logicalType described in the Avro schema definition
			ArrayList<String> avroLogicalTypeNameList = getLogicalTypeName(avroFieldSchema, true);

			int avroLogicalTypeNameListSize = avroLogicalTypeNameList.size();
			if (avroLogicalTypeNameListSize == 0) {
				// When logicalType information could not be obtained
				// Could not get logicalType information from Avro file
				String msg = messageResource.getString("MESS_COMM_ERR_ROWAVRO_11")
						+ ": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()+"] columnName=["+columnName+"] path=["+m_file.getAbsolutePath()+"]";
				throw new GSEIException(msg);
			}

			for (int j = 0; j < avroLogicalTypeNameListSize; j++) {
				String avroLogicalTypeName = avroLogicalTypeNameList.get(j);
				if (!columnType.name().equals(avroLogicalTypeName)) {
					// If the data types do not match
					// A raw data file whose data type does not match was detected.
					String msg = messageResource.getString("MESS_COMM_ERR_ROWAVRO_12")
							+ ": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()+"] columnName=["+columnName+"] columnType=["+avroLogicalTypeName+"] schemaColumnType=["+columnType.name()+"] path=["+m_file.getAbsolutePath()+"]";
					throw new GSEIException(msg);
				}
			}
		}
	}

	/**
	 * Returns the value of logicalType as described in the Avro schema.
	 *
	 * @param fieldSchema Avro schema
	 * @param isAppendArraychar "type":"array" の "items"に定義した情報が入っているSchemaである場合、返す値に"_ARRAY"を付与するか
	 *         trueにすると、"STRING_ARRAY"のような値が返る
	 * @return logicalType
	 */
	private ArrayList<String> getLogicalTypeName (Schema fieldSchema, boolean isAppendArraychar) {
		ArrayList<String> retLogicalTypeNameList = new ArrayList<String>();
		if (Schema.Type.UNION.equals(fieldSchema.getType())) {
			// UNION is like "type": [{"type": "string", "logicalType": "STRING"}, "null"]
			// Applicable when the definition enclosed in [] is made.
			// Get Schema containing UNION defined information
			List<Schema> schemaList = fieldSchema.getTypes();
			Iterator<Schema> ite = schemaList.iterator();
			while (ite.hasNext()) {
				Schema schema = ite.next();
				// Get the logicalType from the obtained Schema
				retLogicalTypeNameList.addAll(getLogicalTypeName(schema, isAppendArraychar));
			}
		} else if (Schema.Type.ARRAY.equals(fieldSchema.getType())) {
			// ARRAY is like "type": {"type":  "array", "items": {"type": "string", "logicalType": "STRING"}}
			// Applicable when array is defined
			// Get Schema containing the information defined in items of array
			Schema arrayItemsSchema = fieldSchema.getElementType();
			// Get the logicalType from the obtained Schema
			ArrayList<String> logicalTypeNameList = getLogicalTypeName(arrayItemsSchema, isAppendArraychar);
			Iterator<String> logicalTypeNameIte = logicalTypeNameList.iterator();
			while (logicalTypeNameIte.hasNext()) {
				String logicalType = logicalTypeNameIte.next();
				if (isAppendArraychar) {
					// Set the acquired logicalType value to the value with "_ARRAY" (Example: "STRING" → "STRING_ARRAY")
					logicalType += ARRAY;
				}
				retLogicalTypeNameList.add(logicalType);
			}
		}  else {
			// Get the logicalType from the argument Schema
			String logicalTypeName = fieldSchema.getProp(LogicalType.LOGICAL_TYPE_PROP);
			if (logicalTypeName != null) {
				retLogicalTypeNameList.add(logicalTypeName);
			}
		}

		return retLogicalTypeNameList;
	}

	/**
	 * Check if the container information and the Not Null constraint of the Avro schema match.
	 *
	 * @param dataFileReaderSchema Avro schema
	 * @throws GSEIException
	 */
	private void checkNotNullConstraint(Schema dataFileReaderSchema) throws GSEIException {
		int columnCount = m_containerInfo.getContainerInfo().getColumnCount();

		// Performed after checking that the number of columns in the container information and the number of fields in avro match.
		for (int i = 0; i < columnCount; i++) {
			ColumnInfo columnInfo = m_containerInfo.getContainerInfo().getColumnInfo(i);
			Field avroField = m_avroFieldList.get(i);

			// Is it nullable on the DB side?
			boolean columnNullable = columnInfo.getNullable();

			Schema avroFieldSchema = avroField.schema();
			// Avro schema nullable?
			boolean avroNullable = getAvroSchemaNullable(avroFieldSchema);
			if (columnNullable != avroNullable) {
				// If the Not Null constraint does not match
				// A raw data file whose Not Null constraint does not match was detected.
				String msg = messageResource.getString("MESS_COMM_ERR_ROWAVRO_13")
						+ ": db=["+m_containerInfo.getDbName()+"] containerName=["+m_containerInfo.getName()+"] columnName=["+columnInfo.getName()+"] path=["+m_file.getAbsolutePath()+"]";
				throw new GSEIException(msg);
			}
		}
	}

	/**
	 * Returns whether the Avro schema is nullable.
	 *
	 * @param fieldSchema Avro schema
	 * @return Is it possible to set a null value?
	 */
	private boolean getAvroSchemaNullable(Schema fieldSchema) {
		boolean ret = false;
		if (Schema.Type.UNION.equals(fieldSchema.getType())) {
			// UNION is like "type": [{"type": "string", "logicalType": "STRING"}, "null"]
			// Applicable when the definition enclosed in [] is made.
			// Get Schema containing UNION defined information
			List<Schema> schemaList = fieldSchema.getTypes();
			Iterator<Schema> ite = schemaList.iterator();
			while (ite.hasNext()) {
				Schema schema = ite.next();
				// Get whether null value can be set from the acquired Schema
				ret |= getAvroSchemaNullable(schema);
			}
		} else if (Schema.Type.NULL.equals(fieldSchema.getType())) {
			// If the result of fieldSchema.getType () is Schema.Type.NULL, it is judged that null value can be set.
			ret = true;
		}
		// {"name": "str_ary", "type": {"type":  "array", "items": [{"type": "string", "logicalType": "STRING"}, "null"]}}
		// のような定義がされていたとしても、
		// "str_ary"にセットする値に{"AAA", null,"CCC"}のように、nullを含む配列を許容する定義であり、
		// "str_ary"にnullをセットすることを許容する定義ではない。
		// よって、fieldSchema.getType()の結果がSchema.Type.ARRAYであっても、
		// "items"に"null"が含まれているかはチェックしない。

		return ret;
	}
}
