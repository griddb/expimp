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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

import com.toshiba.mwcloud.gs.GSType;
import com.toshiba.mwcloud.gs.Geometry;
import com.toshiba.mwcloud.gs.Row;

/**
 * This is a class for serialization when inputting / outputting a binary format file.
 *
 */
public class RowSerialize implements Externalizable {

	/**
	 * Serial version
	 */
	private static final long serialVersionUID = -5455175282166036744L;

	/**
	 * Thread ID and row map
	 */
	//private static Map<Long, Row> m_rowMap;

	/**
	 * Map of thread ID and column information
	 */
	//private static Map<Long, List<GSType>> m_columnListMap;


	/**
	 * List of container column types
	 */
	private static ThreadLocal<List<GSType>> m_columnTypeList = new ThreadLocal<List<GSType>>();

	/**
	 * Raw data to be processed
	 */
	private static ThreadLocal<Object> m_rowObject = new ThreadLocal<Object>();

	/**
	 * Meta information file version
	 * The default value is the latest meta information file version
	 */
	private static String m_metaFileVersion = GSConstants.EXPORT_MNG_FILE_VERSION;

	// The following static method processing is indispensable at the time of Import (file reading: deserialization).
	// (At the time of deserialization, since this class is new inside Java and readExternal is called,
	// there is no timing to set column information and ROW.
	//  Therefore, the column information and ROW are set in the static variable in advance,
	// and it is designed so that it can be used from readExternal.)
	/**
	 * Initializes the number of threads.
	 *
	 * @param threadCount Number of threads
	 */
	/*
	public static void prepare(int threadCount){
		m_rowMap = new HashMap<Long, Row>(threadCount, 1.0F);
		m_columnListMap = new HashMap<Long, List<GSType>>(threadCount, 1.0F);
		// Mapがリサイズされないように、負荷係数1.0を指定。
	}
	*/

	/**
	 * Initialize the thread ID.
	 *
	 * @param threadId Thread ID
	 */
	/*
	public synchronized static void registerTheradId(long threadId){
		m_rowMap.put(threadId, null);
		m_columnListMap.put(threadId, null);
		// スレッド数とマップの項目数が1対1になる。
		// 各スレッドでは、自分のスレッドIDの項目にしかアクセスしないので、
		// 以降の処理ではsynchronizedは不要。
	}
	*/

	/**
	 * Stores column information.
	 *
	 * @param columnTypeList List of column information
	 */
	public static void setColumnTypeList(List<GSType> columnTypeList){
		//System.out.println("["+Thread.currentThread().getId()+"][RowSerialize.setColumnTypeList]"+columnTypeList);
		m_columnTypeList.set(columnTypeList);
	}

	/**
	 * Stores rows.
	 *
	 * @param row Row object
	 */
	public static void setRow(Row row){
		//m_rowMap.put(Thread.currentThread().getId(), row);
		m_rowObject.set(row);
	}

	/**
	 * Get the row.
	 * @return
	 */
	public static Row getRow(){
		//return m_rowMap.get(Thread.currentThread().getId());
		Row row = (Row)(m_rowObject.get());
		m_rowObject.remove();
		return row;
	}

	/**
	 * Get raw data.
	 * @return
	 */
	public static List<Object> getRowData(){
		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>)(m_rowObject.get());
		m_rowObject.remove();
		return list;
	}

	/**
	 * Stores the version of the meta information file.
	 * @param version Meta information file version
	 */
	public static void setMetaFileVersion(String version) {
		m_metaFileVersion = version;
	}

	public static void removeColumnTypeList(){
		m_columnTypeList.remove();
	}

	/**
	 * Default constructor
	 */
	public RowSerialize(){
	}

	/**
	 * Perform serialization.
	 * The processing is switched depending on the version of the information file.
	 *
	 * [Design memo] Switching is for backward compatibility. Not used in V3.5.
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		// Serialize if the meta information file version is V2.7.0 (V2.7.0)
		// Otherwise serialize (V3.5.0)
		if (m_metaFileVersion != null && m_metaFileVersion.equals(GSConstants.EXPORT_MNG_FILE_VERSION_1)) {
			writeExternal1(out);
		} else {
			writeExternal2(out);
		}
	}

	/**
	 * Deserialize.
	 * The process is switched depending on the version of the meta information file.
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		// Deserialize if the meta information file version is V2.7.0 (V2.7.0)
		// Otherwise deserialize (V3.5.0)
		if (m_metaFileVersion != null && m_metaFileVersion.equals(GSConstants.EXPORT_MNG_FILE_VERSION_1)) {
			readExternal1(in);
		} else {
			readExternal2(in);
		}
	}

	/**
	 * Serialize (V2.7.0)
	 * Primitive types are serialized by values and reference types are serialized by objects.
	 */
	public void writeExternal1(ObjectOutput out) throws IOException {

		// Get row and column type from ThreadID
		long threadId = Thread.currentThread().getId();
		/*
		Row row = RowSerialize.m_rowMap.get(threadId);
		List<GSType> rowTypeList = RowSerialize.m_columnListMap.get(threadId);
		*/

		Row row = (Row)(m_rowObject.get());
		List<GSType> rowTypeList = m_columnTypeList.get();

		//System.out.print("["+threadId+"][RowSerialize.writeExternal]");

		for (int index = 0; index < rowTypeList.size(); index++) {
			GSType type = rowTypeList.get(index);

			switch(type){
			case BOOL:
				out.writeBoolean(row.getBool(index));
				break;
			case BYTE:
				out.writeByte(row.getByte(index));
				break;
			case SHORT:
				out.writeShort(row.getShort(index));
				break;
			case INTEGER:
				out.writeInt(row.getInteger(index));
				break;
			case LONG:
				out.writeLong(row.getLong(index));
				break;
			case FLOAT:
				out.writeFloat(row.getFloat(index));
				break;
			case DOUBLE:
				out.writeDouble(row.getDouble(index));
				break;
			case TIMESTAMP:
				out.writeObject(row.getTimestamp(index));
				break;
			case GEOMETRY:
				out.writeObject((row.getGeometry(index)).toString());	// GEOMETRY outputs as String
				break;
			case BLOB:
				try {
					Blob blob = row.getBlob(index);
					SerialBlob sb = null;
					if ( blob.length() == 0 ){
						sb = new SerialBlob(new byte[0]);
					} else {
						sb = new SerialBlob(blob.getBytes(1, (int)blob.length()));

						// [memo] If you use the constructor below, you will get a NotSerializableException in writeObject, so don't use it.
						//SerialBlob sb = new SerialBlob(blob);
					}
					out.writeObject(sb);

				} catch ( Exception e ){
					throw new IOException(e);
				}
				break;
			default:
				out.writeObject(row.getValue(index));
				break;
			}
			//System.out.print(" "+row.getValue(index));
		}

		//System.out.println();
	}

	/**
	 * Deserialize (V2.7.0)
	 */
	public void readExternal1(ObjectInput in) throws IOException, ClassNotFoundException {

		// Get row and column type from ThreadID
		long threadId = Thread.currentThread().getId();
		//Row row = RowSerialize.m_rowMap.get(threadId);
		//List<GSType> rowTypeList = RowSerialize.m_columnListMap.get(threadId);

		Object obj = m_rowObject.get();
		List<GSType> rowTypeList = m_columnTypeList.get();

		//System.out.println(threadId+" "+rowTypeList.get(0));

		if ( obj != null ){
			Row row = (Row)obj;

			for (int index = 0; index < rowTypeList.size(); index++) {
				GSType type = rowTypeList.get(index);

				switch(type){
				case BOOL:
					row.setBool(index, in.readBoolean());
					break;
				case STRING:
					row.setString(index, (String)in.readObject());
					break;
				case BYTE:
					row.setByte(index, in.readByte());
					break;
				case SHORT:
					row.setShort(index, in.readShort());
					break;
				case INTEGER:
					row.setInteger(index, in.readInt());
					break;
				case LONG:
					row.setLong(index, in.readLong());
					break;
				case FLOAT:
					row.setFloat(index, in.readFloat());
					break;
				case DOUBLE:
					row.setDouble(index, in.readDouble());
					break;
				case TIMESTAMP:
					row.setTimestamp(index, (Date)in.readObject());
					break;
				case GEOMETRY:
					row.setGeometry(index, Geometry.valueOf((String)in.readObject()));	// GEOMETRY outputs as String
					break;
				case BLOB:
					Blob blob = (Blob)in.readObject();
					try {
						if ( blob.length() != 0 ){
							row.setBlob(index, blob);
						}
					} catch (SQLException e) {
						throw new IOException(e);
					}
					break;
				case BOOL_ARRAY:
					row.setBoolArray(index, (boolean[])in.readObject());
					break;
				case STRING_ARRAY:
					row.setStringArray(index, (String[])in.readObject());
					break;
				case BYTE_ARRAY:
					row.setByteArray(index, (byte[])in.readObject());
					break;
				case SHORT_ARRAY:
					row.setShortArray(index, (short[])in.readObject());
					break;
				case INTEGER_ARRAY:
					row.setIntegerArray(index, (int[])in.readObject());
					break;
				case LONG_ARRAY:
					row.setLongArray(index, (long[])in.readObject());
					break;
				case FLOAT_ARRAY:
					row.setFloatArray(index, (float[])in.readObject());
					break;
				case DOUBLE_ARRAY:
					row.setDoubleArray(index, (double[])in.readObject());
					break;
				case TIMESTAMP_ARRAY:
					row.setTimestampArray(index, (Date[])in.readObject());
					break;
				}
			}

		} else {

			List<Object> rowDataList = new ArrayList<Object>();

			for (int index = 0; index < rowTypeList.size(); index++) {
				GSType type = rowTypeList.get(index);

				switch(type){
				case BOOL:
					rowDataList.add(in.readBoolean());
					break;
				case STRING:
					rowDataList.add((String)in.readObject());
					break;
				case BYTE:
					rowDataList.add(in.readByte());
					break;
				case SHORT:
					rowDataList.add(in.readShort());
					break;
				case INTEGER:
					rowDataList.add(in.readInt());
					break;
				case LONG:
					rowDataList.add(in.readLong());
					break;
				case FLOAT:
					rowDataList.add(in.readFloat());
					break;
				case DOUBLE:
					rowDataList.add(in.readDouble());
					break;
				case TIMESTAMP:
					rowDataList.add((Date)in.readObject());
					break;
				case GEOMETRY:
					rowDataList.add(Geometry.valueOf((String)in.readObject()));	// GEOMETRY outputs as String
					break;
				case BLOB:
					Blob blob = (Blob)in.readObject();
					try {
						if ( blob.length() != 0 ){
							rowDataList.add(blob);
						}
					} catch (SQLException e) {
						throw new IOException(e);
					}
					break;
				case BOOL_ARRAY:
					rowDataList.add((boolean[])in.readObject());
					break;
				case STRING_ARRAY:
					rowDataList.add((String[])in.readObject());
					break;
				case BYTE_ARRAY:
					rowDataList.add((byte[])in.readObject());
					break;
				case SHORT_ARRAY:
					rowDataList.add((short[])in.readObject());
					break;
				case INTEGER_ARRAY:
					rowDataList.add((int[])in.readObject());
					break;
				case LONG_ARRAY:
					rowDataList.add((long[])in.readObject());
					break;
				case FLOAT_ARRAY:
					rowDataList.add((float[])in.readObject());
					break;
				case DOUBLE_ARRAY:
					rowDataList.add((double[])in.readObject());
					break;
				case TIMESTAMP_ARRAY:
					rowDataList.add((Date[])in.readObject());
					break;
				}
			}
			//System.out.println("[RowSerialize.readObject] "+rowDataList);

			m_rowObject.set(rowDataList);
		}

	}

	/**
	 * シリアライズ(V3.5.0)
	 * 各カラム値の前にbooleanでNULLフラグをシリアライズします。
	 * NULLフラグがtrueのとき、後ろの値またはオブジェクトは使われません。
	 */
	public void writeExternal2(ObjectOutput out) throws IOException {

		// ThreadIDからロウ、カラムタイプを取得する
		long threadId = Thread.currentThread().getId();
		/*
		Row row = RowSerialize.m_rowMap.get(threadId);
		List<GSType> rowTypeList = RowSerialize.m_columnListMap.get(threadId);
		*/

		Row row = (Row)(m_rowObject.get());
		List<GSType> rowTypeList = m_columnTypeList.get();

		//System.out.print("["+threadId+"][RowSerialize.writeExternal]");

		for (int index = 0; index < rowTypeList.size(); index++) {
			if (row.isNull(index)) {
				out.writeByte(1);
				continue;
			} else {
				out.writeByte(0);
			}

			GSType type = rowTypeList.get(index);

			switch(type){
			case BOOL:
				out.writeBoolean(row.getBool(index));
				break;
			case BYTE:
				out.writeByte(row.getByte(index));
				break;
			case SHORT:
				out.writeShort(row.getShort(index));
				break;
			case INTEGER:
				out.writeInt(row.getInteger(index));
				break;
			case LONG:
				out.writeLong(row.getLong(index));
				break;
			case FLOAT:
				out.writeFloat(row.getFloat(index));
				break;
			case DOUBLE:
				out.writeDouble(row.getDouble(index));
				break;
			case TIMESTAMP:
				out.writeObject(row.getTimestamp(index));
				break;
			case GEOMETRY:
				out.writeObject((row.getGeometry(index)).toString());	// GEOMETRYはStringで出力する
				break;
			case BLOB:
				try {
					Blob blob = row.getBlob(index);
					SerialBlob sb = null;
					if ( blob.length() == 0 ){
						sb = new SerialBlob(new byte[0]);
					} else {
						sb = new SerialBlob(blob.getBytes(1, (int)blob.length()));

						// [memo] If you use the constructor below, you will get a NotSerializableException in writeObject, so don't use it.
						//SerialBlob sb = new SerialBlob(blob);
					}
					out.writeObject(sb);

				} catch ( Exception e ){
					throw new IOException(e);
				}
				break;
			default:
				out.writeObject(row.getValue(index));
				break;
			}
			//System.out.print(" "+row.getValue(index));
		}

		//System.out.println();
	}

	/**
	 * Deserialize(V3.5.0)
	 */
	public void readExternal2(ObjectInput in) throws IOException, ClassNotFoundException {

		// Get row and column type from ThreadID
		long threadId = Thread.currentThread().getId();
		//Row row = RowSerialize.m_rowMap.get(threadId);
		//List<GSType> rowTypeList = RowSerialize.m_columnListMap.get(threadId);

		Object obj = m_rowObject.get();
		List<GSType> rowTypeList = m_columnTypeList.get();

		//System.out.println(threadId+" "+rowTypeList.get(0));

		if ( obj != null ){
			Row row = (Row)obj;

			for (int index = 0; index < rowTypeList.size(); index++) {
				byte isNull = in.readByte();
				if (isNull == 1) {
					row.setValue(index, null);
					continue;
				}

				GSType type = rowTypeList.get(index);

				switch(type){
				case BOOL:
					row.setBool(index, in.readBoolean());
					break;
				case STRING:
					row.setString(index, (String)in.readObject());
					break;
				case BYTE:
					row.setByte(index, in.readByte());
					break;
				case SHORT:
					row.setShort(index, in.readShort());
					break;
				case INTEGER:
					row.setInteger(index, in.readInt());
					break;
				case LONG:
					row.setLong(index, in.readLong());
					break;
				case FLOAT:
					row.setFloat(index, in.readFloat());
					break;
				case DOUBLE:
					row.setDouble(index, in.readDouble());
					break;
				case TIMESTAMP:
					row.setTimestamp(index, (Date)in.readObject());
					break;
				case GEOMETRY:
					row.setGeometry(index, Geometry.valueOf((String)in.readObject()));	// GEOMETRY outputs as String
					break;
				case BLOB:
					Blob blob = (Blob)in.readObject();
					try {
						if ( blob.length() != 0 ){
							row.setBlob(index, blob);
						}
					} catch (SQLException e) {
						throw new IOException(e);
					}
					break;
				case BOOL_ARRAY:
					row.setBoolArray(index, (boolean[])in.readObject());
					break;
				case STRING_ARRAY:
					row.setStringArray(index, (String[])in.readObject());
					break;
				case BYTE_ARRAY:
					row.setByteArray(index, (byte[])in.readObject());
					break;
				case SHORT_ARRAY:
					row.setShortArray(index, (short[])in.readObject());
					break;
				case INTEGER_ARRAY:
					row.setIntegerArray(index, (int[])in.readObject());
					break;
				case LONG_ARRAY:
					row.setLongArray(index, (long[])in.readObject());
					break;
				case FLOAT_ARRAY:
					row.setFloatArray(index, (float[])in.readObject());
					break;
				case DOUBLE_ARRAY:
					row.setDoubleArray(index, (double[])in.readObject());
					break;
				case TIMESTAMP_ARRAY:
					row.setTimestampArray(index, (Date[])in.readObject());
					break;
				}
			}

		} else {

			List<Object> rowDataList = new ArrayList<Object>();

			for (int index = 0; index < rowTypeList.size(); index++) {
				byte isNull = in.readByte();
				if (isNull == 1) {
					rowDataList.add(null);
					continue;
				}

				GSType type = rowTypeList.get(index);

				switch(type){
				case BOOL:
					rowDataList.add(in.readBoolean());
					break;
				case STRING:
					rowDataList.add((String)in.readObject());
					break;
				case BYTE:
					rowDataList.add(in.readByte());
					break;
				case SHORT:
					rowDataList.add(in.readShort());
					break;
				case INTEGER:
					rowDataList.add(in.readInt());
					break;
				case LONG:
					rowDataList.add(in.readLong());
					break;
				case FLOAT:
					rowDataList.add(in.readFloat());
					break;
				case DOUBLE:
					rowDataList.add(in.readDouble());
					break;
				case TIMESTAMP:
					rowDataList.add((Date)in.readObject());
					break;
				case GEOMETRY:
					rowDataList.add(Geometry.valueOf((String)in.readObject()));	// GEOMETRY outputs as String
					break;
				case BLOB:
					Blob blob = (Blob)in.readObject();
					try {
						if ( blob.length() != 0 ){
							rowDataList.add(blob);
						}
					} catch (SQLException e) {
						throw new IOException(e);
					}
					break;
				case BOOL_ARRAY:
					rowDataList.add((boolean[])in.readObject());
					break;
				case STRING_ARRAY:
					rowDataList.add((String[])in.readObject());
					break;
				case BYTE_ARRAY:
					rowDataList.add((byte[])in.readObject());
					break;
				case SHORT_ARRAY:
					rowDataList.add((short[])in.readObject());
					break;
				case INTEGER_ARRAY:
					rowDataList.add((int[])in.readObject());
					break;
				case LONG_ARRAY:
					rowDataList.add((long[])in.readObject());
					break;
				case FLOAT_ARRAY:
					rowDataList.add((float[])in.readObject());
					break;
				case DOUBLE_ARRAY:
					rowDataList.add((double[])in.readObject());
					break;
				case TIMESTAMP_ARRAY:
					rowDataList.add((Date[])in.readObject());
					break;
				}
			}
			//System.out.println("[RowSerialize.readObject] "+rowDataList);

			m_rowObject.set(rowDataList);
		}

	}
}
