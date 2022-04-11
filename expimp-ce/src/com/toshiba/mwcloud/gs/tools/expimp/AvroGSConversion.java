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

import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;

import org.apache.avro.Conversion;
import org.apache.avro.LogicalType;
import org.apache.avro.Schema;

import com.toshiba.mwcloud.gs.Geometry;

/**
 * The definition of the Conversion of Avro
 *
 */
public class AvroGSConversion {

	/**
	 * Conversion for LogicalType of STRING
	 *
	 */
	public static class GSStringConversion extends Conversion<String> {

		/*
		 * @see org.apache.avro.Conversion#getConvertedType()
		 */
		@Override
		public Class<String> getConvertedType() {
			return String.class;
		}

		/*
		 * @see org.apache.avro.Conversion#getLogicalTypeName()
		 */
		@Override
		public String getLogicalTypeName() {
			return AvroGSLogicalType.LOGICALTYPE_STRING;
		}

		/**
		 * Returns the converted String value
		 *
		 * @return String value
		 */
		@Override
		public String fromCharSequence(CharSequence seq, Schema schema, LogicalType type) {
			String retVal = null;
			if (seq != null) {
				retVal = seq.toString();
			}
			return retVal;
		}

	}

	/**
	 * Conversion for LogicalType of BOOL
	 *
	 */
	public static class GSBoolConversion extends Conversion<Boolean> {

		/*
		 * @see org.apache.avro.Conversion#getConvertedType()
		 */
		@Override
		public Class<Boolean> getConvertedType() {
			return Boolean.class;
		}

		/*
		 * @see org.apache.avro.Conversion#getLogicalTypeName()
		 */
		@Override
		public String getLogicalTypeName() {
			return AvroGSLogicalType.LOGICALTYPE_BOOL;
		}

		/**
		 * Returns a Boolean value
		 *
		 * @return Boolean value
		 */
		@Override
		public Boolean fromBoolean(Boolean value, Schema schema, LogicalType type) {
			return value;
		}
	}

	/**
	 * Conversion for LogicalType of Byte
	 *
	 */
	public static class GSByteConversion extends Conversion<Byte> {

		/*
		 * @see org.apache.avro.Conversion#getConvertedType()
		 */
		@Override
		public Class<Byte> getConvertedType() {
			return Byte.class;
		}

		/*
		 * @see org.apache.avro.Conversion#getLogicalTypeName()
		 */
		@Override
		public String getLogicalTypeName() {
			return AvroGSLogicalType.LOGICALTYPE_BYTE;
		}

		/**
		 * Returns a Byte value
		 *
		 * @return Byte value
		 */
		@Override
		public Byte fromInt(Integer value, Schema schema, LogicalType type) {
			Byte retVal = null;
			if (value != null) {
				retVal = Byte.valueOf(value.toString());
			}
			return retVal;
		}

	}

	/**
	 * Conversion for LogicalType of Short
	 *
	 */
	public static class GSShortConversion extends Conversion<Short> {

		/*
		 * @see org.apache.avro.Conversion#getConvertedType()
		 */
		@Override
		public Class<Short> getConvertedType() {
			return Short.class;
		}

		/*
		 * @see org.apache.avro.Conversion#getLogicalTypeName()
		 */
		@Override
		public String getLogicalTypeName() {
			return AvroGSLogicalType.LOGICALTYPE_SHORT;
		}

		/**
		 * Returns a short value
		 *
		 * @return Short value
		 */
		@Override
		public Short fromInt(Integer value, Schema schema, LogicalType type) {
			Short retVal = null;
			if (value != null) {
				retVal = Short.valueOf(value.toString());
			}
			return retVal;
		}

	}

	/**
	 * Conversion for LogicalType of INTEGER
	 *
	 */
	public static class GSIntegerConversion extends Conversion<Integer> {

		/*
		 * @see org.apache.avro.Conversion#getConvertedType()
		 */
		@Override
		public Class<Integer> getConvertedType() {
			return Integer.class;
		}

		/*
		 * @see org.apache.avro.Conversion#getLogicalTypeName()
		 */
		@Override
		public String getLogicalTypeName() {
			return AvroGSLogicalType.LOGICALTYPE_INTEGER;
		}

		/**
		 * Returns an Integer value
		 *
		 * @return Integer value
		 */
		@Override
		public Integer fromInt(Integer value, Schema schema, LogicalType type) {
			return value;
		}

	}

	/**
	 * Conversion for LogicalType of Long
	 *
	 */
	public static class GSLongConversion extends Conversion<Long> {

		/*
		 * @see org.apache.avro.Conversion#getConvertedType()
		 */
		@Override
		public Class<Long> getConvertedType() {
			return Long.class;
		}

		/*
		 * @see org.apache.avro.Conversion#getLogicalTypeName()
		 */
		@Override
		public String getLogicalTypeName() {
			return AvroGSLogicalType.LOGICALTYPE_LONG;
		}

		/**
		 * Returns a Long value
		 *
		 * @return Long value
		 */
		@Override
		public Long fromLong(Long value, Schema schema, LogicalType type) {
			return value;
		}

	}

	/**
	 * Conversion for LogicalType of Float
	 *
	 */
	public static class GSFloatConversion extends Conversion<Float> {

		/*
		 * @see org.apache.avro.Conversion#getConvertedType()
		 */
		@Override
		public Class<Float> getConvertedType() {
			return Float.class;
		}

		/*
		 * @see org.apache.avro.Conversion#getLogicalTypeName()
		 */
		@Override
		public String getLogicalTypeName() {
			return AvroGSLogicalType.LOGICALTYPE_FLOAT;
		}

		/**
		 * Returns a Float value
		 *
		 * @return Float value
		 */
		@Override
		public Float fromFloat(Float value, Schema schema, LogicalType type) {
			return value;
		}

	}

	/**
	 * Conversion for LogicalType of Double
	 *
	 */
	public static class GSDoubleConversion extends Conversion<Double> {

		/*
		 * @see org.apache.avro.Conversion#getConvertedType()
		 */
		@Override
		public Class<Double> getConvertedType() {
			return Double.class;
		}

		/*
		 * @see org.apache.avro.Conversion#getLogicalTypeName()
		 */
		@Override
		public String getLogicalTypeName() {
			return AvroGSLogicalType.LOGICALTYPE_DOUBLE;
		}

		/**
		 * Returns a Double value
		 *
		 * @return Double value
		 */
		@Override
		public Double fromDouble(Double value, Schema schema, LogicalType type) {
			return value;
		}
	}

	/**
	 * Conversion for LogicalType of TIMESTAMP
	 *
	 */
	public static class GSTimestampConversion extends Conversion<Date> {

		/*
		 * @see org.apache.avro.Conversion#getConvertedType()
		 */
		@Override
		public Class<Date> getConvertedType() {
			return Date.class;
		}

		/*
		 * @see org.apache.avro.Conversion#getLogicalTypeName()
		 */
		@Override
		public String getLogicalTypeName() {
			return AvroGSLogicalType.LOGICALTYPE_TIMESTAMP;
		}

		/**
		 * Returns a Date value
		 *
		 * @return Date value
		 */
		@Override
		public Date fromLong(Long value, Schema schema, LogicalType type) {
			Date retVal = null;
			if (value != null) {
				retVal = new Date(value.longValue());
			}
			return retVal;
		}
	}

	/**
	 * Conversion for LogicalType of Geometry
	 *
	 */
	public static class GSGeometryConversion extends Conversion<Geometry> {

		/*
		 * @see org.apache.avro.Conversion#getConvertedType()
		 */
		@Override
		public Class<Geometry> getConvertedType() {
			return Geometry.class;
		}

		/*
		 * @see org.apache.avro.Conversion#getLogicalTypeName()
		 */
		@Override
		public String getLogicalTypeName() {
			return AvroGSLogicalType.LOGICALTYPE_GEOMETRY;
		}

		/**
		 * Returns a Geometry value
		 *
		 * @return Geometry value
		 */
		@Override
		public Geometry fromCharSequence(CharSequence seq, Schema schema, LogicalType type) {
			Geometry retVal = null;
			if (seq != null) {
				retVal = Geometry.valueOf(seq.toString());
			}
			return retVal;
		}


	}

	/**
	 * Conversion for LogicalType of Blob
	 *
	 */
	public static class GSBlobConversion extends Conversion<Blob> {

		/*
		 * @see org.apache.avro.Conversion#getConvertedType()
		 */
		@Override
		public Class<Blob> getConvertedType() {
			return Blob.class;
		}

		/*
		 * @see org.apache.avro.Conversion#getLogicalTypeName()
		 */
		@Override
		public String getLogicalTypeName() {
			return AvroGSLogicalType.LOGICALTYPE_BLOB;
		}

		/**
		 * Returns a Blob value
		 *
		 * @return Blob value
		 */
		@Override
		public Blob fromBytes(ByteBuffer value, Schema schema, LogicalType type) {
			Blob retVal = null;
			if (value != null) {
				try {
					retVal = new javax.sql.rowset.serial.SerialBlob(((ByteBuffer) value).array());
				} catch (SQLException e) {
					throw new RuntimeException("Cannot convert " + value.getClass().getSimpleName() + " to Blob");
				}
			}
			return retVal;
		}

	}
}
