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

import org.apache.avro.LogicalType;
import org.apache.avro.LogicalTypes;
import org.apache.avro.LogicalTypes.LogicalTypeFactory;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;

import com.toshiba.mwcloud.gs.tools.expimp.AvroGSConversion.GSBlobConversion;
import com.toshiba.mwcloud.gs.tools.expimp.AvroGSConversion.GSBoolConversion;
import com.toshiba.mwcloud.gs.tools.expimp.AvroGSConversion.GSByteConversion;
import com.toshiba.mwcloud.gs.tools.expimp.AvroGSConversion.GSDoubleConversion;
import com.toshiba.mwcloud.gs.tools.expimp.AvroGSConversion.GSFloatConversion;
import com.toshiba.mwcloud.gs.tools.expimp.AvroGSConversion.GSGeometryConversion;
import com.toshiba.mwcloud.gs.tools.expimp.AvroGSConversion.GSIntegerConversion;
import com.toshiba.mwcloud.gs.tools.expimp.AvroGSConversion.GSLongConversion;
import com.toshiba.mwcloud.gs.tools.expimp.AvroGSConversion.GSShortConversion;
import com.toshiba.mwcloud.gs.tools.expimp.AvroGSConversion.GSStringConversion;
import com.toshiba.mwcloud.gs.tools.expimp.AvroGSConversion.GSTimestampConversion;

/**
 * The definition of LogicalType of Avro
 *
 */
public class AvroGSLogicalType {

	/**
	 * Registration status of LogicalType
	 */
	private static boolean registeredLogicalType = false;

	/**
	 * logicalType STRING
	 */
	public static final String LOGICALTYPE_STRING = "STRING";
	/**
	 * logicalType BOOL
	 */
	public static final String LOGICALTYPE_BOOL = "BOOL";
	/**
	 * logicalType BYTE
	 */
	public static final String LOGICALTYPE_BYTE = "BYTE";
	/**
	 * logicalType SHORT
	 */
	public static final String LOGICALTYPE_SHORT = "SHORT";
	/**
	 * logicalType INTEGER
	 */
	public static final String LOGICALTYPE_INTEGER = "INTEGER";
	/**
	 * logicalType LONG
	 */
	public static final String LOGICALTYPE_LONG = "LONG";
	/**
	 * logicalType FLOAT
	 */
	public static final String LOGICALTYPE_FLOAT = "FLOAT";
	/**
	 * logicalType DOUBLE
	 */
	public static final String LOGICALTYPE_DOUBLE = "DOUBLE";
	/**
	 * logicalType TIMESTAMP
	 */
	public static final String LOGICALTYPE_TIMESTAMP = "TIMESTAMP";
	/**
	 * logicalType GEOMETRY
	 */
	public static final String LOGICALTYPE_GEOMETRY = "GEOMETRY";
	/**
	 * logicalType BLOB
	 */
	public static final String LOGICALTYPE_BLOB = "BLOB";

	private static final GSLogicalTypeSTRING logicalTypeString = new GSLogicalTypeSTRING();
	private static final GSLogicalTypeBOOL logicalTypeBool = new GSLogicalTypeBOOL();
	private static final GSLogicalTypeBYTE logicalTypeByte = new GSLogicalTypeBYTE();
	private static final GSLogicalTypeSHORT logicalTypeShort = new GSLogicalTypeSHORT();
	private static final GSLogicalTypeINTEGER logicalTypeInteger = new GSLogicalTypeINTEGER();
	private static final GSLogicalTypeLONG logicalTypeLong = new GSLogicalTypeLONG();
	private static final GSLogicalTypeFLOAT logicalTypeFloat = new GSLogicalTypeFLOAT();
	private static final GSLogicalTypeDOUBLE logicalTypeDouble = new GSLogicalTypeDOUBLE();
	private static final GSLogicalTypeTIMESTAMP logicalTypeTimestamp = new GSLogicalTypeTIMESTAMP();
	private static final GSLogicalTypeGEOMETRY logicalTypeGeometry = new GSLogicalTypeGEOMETRY();
	private static final GSLogicalTypeBLOB logicalTypeBlob = new GSLogicalTypeBLOB();

	/**
	 * Register LogicalType
	 * Register Conversion for LogicalType
	 *
	 */
	public static void registerGSLogicalType() {
		if (registeredLogicalType == false) {
			registeredLogicalType = true;
			// Register Logical Type
			LogicalTypes.register(LOGICALTYPE_STRING, logicalTypeString());
			LogicalTypes.register(LOGICALTYPE_BOOL, logicalTypeBool());
			LogicalTypes.register(LOGICALTYPE_BYTE, logicalTypeByte());
			LogicalTypes.register(LOGICALTYPE_SHORT, logicalTypeShort());
			LogicalTypes.register(LOGICALTYPE_INTEGER, logicalTypeInteger());
			LogicalTypes.register(LOGICALTYPE_LONG, logicalTypeLong());
			LogicalTypes.register(LOGICALTYPE_FLOAT, logicalTypeFloat());
			LogicalTypes.register(LOGICALTYPE_DOUBLE, logicalTypeDouble());
			LogicalTypes.register(LOGICALTYPE_TIMESTAMP, logicalTypeTimestamp());
			LogicalTypes.register(LOGICALTYPE_GEOMETRY, logicalTypeGeometry());
			LogicalTypes.register(LOGICALTYPE_BLOB, logicalTypeBlob());

			// Register the conversion for the registered Logical Type
			GenericData.get().addLogicalTypeConversion(new GSStringConversion());
			GenericData.get().addLogicalTypeConversion(new GSBoolConversion());
			GenericData.get().addLogicalTypeConversion(new GSByteConversion());
			GenericData.get().addLogicalTypeConversion(new GSShortConversion());
			GenericData.get().addLogicalTypeConversion(new GSIntegerConversion());
			GenericData.get().addLogicalTypeConversion(new GSLongConversion());
			GenericData.get().addLogicalTypeConversion(new GSFloatConversion());
			GenericData.get().addLogicalTypeConversion(new GSDoubleConversion());
			GenericData.get().addLogicalTypeConversion(new GSTimestampConversion());
			GenericData.get().addLogicalTypeConversion(new GSGeometryConversion());
			GenericData.get().addLogicalTypeConversion(new GSBlobConversion());
		}
	}

	/**
	 * Returns the LogicalType of STRING
	 *
	 * @return Logical Type of STRING
	 */
	public static GSLogicalTypeSTRING logicalTypeString() {
		return logicalTypeString;
	}

	/**
	 * Returns the LogicalType of BOOL
	 *
	 * @return Logical Type of BOOL
	 */
	public static GSLogicalTypeBOOL logicalTypeBool() {
		return logicalTypeBool;
	}

	/**
	 * Returns the LogicalType of BYTE
	 *
	 * @return Logical Type of BYTE
	 */
	public static GSLogicalTypeBYTE logicalTypeByte() {
		return logicalTypeByte;
	}

	/**
	 * Returns the LogicalType of SHORT
	 *
	 * @return Logical Type of SHORT
	 */
	public static GSLogicalTypeSHORT logicalTypeShort() {
		return logicalTypeShort;
	}

	/**
	 * Returns the LogicalType of INTEGER
	 *
	 * @return Logical Type of INTEGER
	 */
	public static GSLogicalTypeINTEGER logicalTypeInteger() {
		return logicalTypeInteger;
	}

	/**
	 * Returns the LogicalType of LONG
	 *
	 * @return Logical Type of LONG
	 */
	public static GSLogicalTypeLONG logicalTypeLong() {
		return logicalTypeLong;
	}

	/**
	 * Returns the LogicalType of FLOAT
	 *
	 * @return Logical Type of FLOAT
	 */
	public static GSLogicalTypeFLOAT logicalTypeFloat() {
		return logicalTypeFloat;
	}

	/**
	 * Returns the LogicalType of DOUBLE
	 *
	 * @return Logical Type of DOUBLE
	 */
	public static GSLogicalTypeDOUBLE logicalTypeDouble() {
		return logicalTypeDouble;
	}

	/**
	 * Returns the LogicalType of TIMESTAMP
	 *
	 * @return TIMESTAMP of Logical Type
	 */
	public static GSLogicalTypeTIMESTAMP logicalTypeTimestamp() {
		return logicalTypeTimestamp;
	}

	/**
	 * Returns the LogicalType of GEOMETRY
	 *
	 * @return Logical Type of GEOMETRY
	 */
	public static GSLogicalTypeGEOMETRY logicalTypeGeometry() {
		return logicalTypeGeometry;
	}

	/**
	 * Returns the LogicalType of BLOB
	 *
	 * @return Logical Type of BLOB
	 */
	public static GSLogicalTypeBLOB logicalTypeBlob() {
		return logicalTypeBlob;
	}

	/**
	 * LogicalType of STRING
	 *
	 */
	public static class GSLogicalTypeSTRING extends LogicalType implements LogicalTypeFactory {
		private GSLogicalTypeSTRING() {
			super(LOGICALTYPE_STRING);
		}

		/* 
		 * @see org.apache.avro.LogicalType#validate(org.apache.avro.Schema)
		 */
		@Override
		public void validate(Schema schema) {
			super.validate(schema);
			if (schema.getType() != Schema.Type.STRING) {
				throw new IllegalArgumentException(
						"STRING can only be used with an underlying string type");
			}
		}

		/**
		 * Returns the LogicalType of STRING
		 *
		 * @return LogicalType of STRING
		 */
		@Override
		public LogicalType fromSchema(Schema schema) {
			return this;
		}
	}

	/**
	 * LogicalType of BOOL
	 */
	public static class GSLogicalTypeBOOL extends LogicalType implements LogicalTypeFactory {
		private GSLogicalTypeBOOL() {
			super(LOGICALTYPE_BOOL);
		}

		/* 
		 * @see org.apache.avro.LogicalType#validate(org.apache.avro.Schema)
		 */
		@Override
		public void validate(Schema schema) {
			super.validate(schema);
			if (schema.getType() != Schema.Type.BOOLEAN) {
				throw new IllegalArgumentException(
						"BOOL can only be used with an underlying boolean type");
			}
		}

		/**
		 * Returns a BOOL LogicalType
		 *
		 * @return LogicalType of BOOL
		 */
		@Override
		public LogicalType fromSchema(Schema schema) {
			return this;
		}
	}

	/**
	 * LogicalType of BYTE
	 *
	 */
	public static class GSLogicalTypeBYTE extends LogicalType implements LogicalTypeFactory {
		private GSLogicalTypeBYTE() {
			super(LOGICALTYPE_BYTE);
		}

		/* 
		 * @see org.apache.avro.LogicalType#validate(org.apache.avro.Schema)
		 */
		@Override
		public void validate(Schema schema) {
			super.validate(schema);
			if (schema.getType() != Schema.Type.INT) {
				throw new IllegalArgumentException(
						"BYTE can only be used with an underlying int type");
			}
		}

		/**
		 * Returns the LogicalType of BYTE
		 *
		 * @return LogicalType of BYTE
		 */
		@Override
		public LogicalType fromSchema(Schema schema) {
			return this;
		}
	}

	/**
	 * LogicalType of SHORT
	 *
	 */
	public static class GSLogicalTypeSHORT extends LogicalType implements LogicalTypeFactory {
		private GSLogicalTypeSHORT() {
			super(LOGICALTYPE_SHORT);
		}

		/* 
		 * @see org.apache.avro.LogicalType#validate(org.apache.avro.Schema)
		 */
		@Override
		public void validate(Schema schema) {
			super.validate(schema);
			if (schema.getType() != Schema.Type.INT) {
				throw new IllegalArgumentException(
						"SHORT can only be used with an underlying int type");
			}
		}

		/**
		 * Returns the LogicalType of SHORT
		 *
		 * @return LogicalType of SHORT
		 */
		@Override
		public LogicalType fromSchema(Schema schema) {
			return this;
		}
	}

	/**
	 * LogicalType of INTEGER
	 *
	 */
	public static class GSLogicalTypeINTEGER extends LogicalType implements LogicalTypeFactory {
		private GSLogicalTypeINTEGER() {
			super(LOGICALTYPE_INTEGER);
		}

		/* 
		 * @see org.apache.avro.LogicalType#validate(org.apache.avro.Schema)
		 */
		@Override
		public void validate(Schema schema) {
			super.validate(schema);
			if (schema.getType() != Schema.Type.INT) {
				throw new IllegalArgumentException(
						"INTEGER can only be used with an underlying int type");
			}
		}

		/**
		 * Returns the LogicalType of INTEGER
		 *
		 * @return LogicalType of INTEGER
		 */
		@Override
		public LogicalType fromSchema(Schema schema) {
			return this;
		}
	}

	/**
	 * LogicalType of LONG
	 *
	 */
	public static class GSLogicalTypeLONG extends LogicalType implements LogicalTypeFactory {
		private GSLogicalTypeLONG() {
			super(LOGICALTYPE_LONG);
		}

		/* 
		 * @see org.apache.avro.LogicalType#validate(org.apache.avro.Schema)
		 */
		@Override
		public void validate(Schema schema) {
			super.validate(schema);
			if (schema.getType() != Schema.Type.LONG) {
				throw new IllegalArgumentException(
						"LONG can only be used with an underlying long type");
			}
		}

		/**
		 * Returns the LogicalType of LONG
		 *
		 * @return LogicalType of LONG
		 */
		@Override
		public LogicalType fromSchema(Schema schema) {
			return this;
		}
	}

	/**
	 * LogicalType of FLOAT
	 *
	 */
	public static class GSLogicalTypeFLOAT extends LogicalType implements LogicalTypeFactory {
		private GSLogicalTypeFLOAT() {
			super(LOGICALTYPE_FLOAT);
		}

		/* 
		 * @see org.apache.avro.LogicalType#validate(org.apache.avro.Schema)
		 */
		@Override
		public void validate(Schema schema) {
			super.validate(schema);
			if (schema.getType() != Schema.Type.FLOAT) {
				throw new IllegalArgumentException(
						"FLOAT can only be used with an underlying float type");
			}
		}

		/**
		 * Returns the LogicalType of FLOAT
		 *
		 * @return LogicalType FLOAT
		 */
		@Override
		public LogicalType fromSchema(Schema schema) {
			return this;
		}
	}

	/**
	 * LogicalType of DOUBLE
	 *
	 */
	public static class GSLogicalTypeDOUBLE extends LogicalType implements LogicalTypeFactory {
		private GSLogicalTypeDOUBLE() {
			super(LOGICALTYPE_DOUBLE);
		}

		/* 
		 * @see org.apache.avro.LogicalType#validate(org.apache.avro.Schema)
		 */
		@Override
		public void validate(Schema schema) {
			super.validate(schema);
			if (schema.getType() != Schema.Type.DOUBLE) {
				throw new IllegalArgumentException(
						"DOUBLE can only be used with an underlying double type");
			}
		}

		/**
		 * Returns the LogicalType of DOUBLE
		 *
		 * @return LogicalType of DOUBLE
		 */
		@Override
		public LogicalType fromSchema(Schema schema) {
			return this;
		}
	}

	/**
	 * LogicalType of TIMESTAMP
	 *
	 */
	public static class GSLogicalTypeTIMESTAMP extends LogicalType implements LogicalTypeFactory {
		private GSLogicalTypeTIMESTAMP() {
			super(LOGICALTYPE_TIMESTAMP);
		}

		/* 
		 * @see org.apache.avro.LogicalType#validate(org.apache.avro.Schema)
		 */
		@Override
		public void validate(Schema schema) {
			super.validate(schema);
			if (schema.getType() != Schema.Type.LONG) {
				throw new IllegalArgumentException(
						"TIMESTAMP can only be used with an underlying long type");
			}
		}

		/**
		 * Returns the LogicalType of TIMESTAMP
		 *
		 * @return LogicalType of TIMESTAMP
		 */
		@Override
		public LogicalType fromSchema(Schema schema) {
			return this;
		}
	}

	/**
	 * LogicalType of GEOMETRY
	 *
	 */
	public static class GSLogicalTypeGEOMETRY extends LogicalType implements LogicalTypeFactory {
		private GSLogicalTypeGEOMETRY() {
			super(LOGICALTYPE_GEOMETRY);
		}

		/* 
		 * @see org.apache.avro.LogicalType#validate(org.apache.avro.Schema)
		 */
		@Override
		public void validate(Schema schema) {
			super.validate(schema);
			if (schema.getType() != Schema.Type.STRING) {
				throw new IllegalArgumentException(
						"GEOMETRY can only be used with an underlying string type");
			}
		}

		/**
		 * Returns the LogicalType of GEOMETRY
		 *
		 * @return LogicalType of GEOMETRY
		 */
		@Override
		public LogicalType fromSchema(Schema schema) {
			return this;
		}
	}

	/**
	 * LogicalType of BLOB
	 *
	 */
	public static class GSLogicalTypeBLOB extends LogicalType implements LogicalTypeFactory {
		private GSLogicalTypeBLOB() {
			super(LOGICALTYPE_BLOB);
		}

		/* 
		 * @see org.apache.avro.LogicalType#validate(org.apache.avro.Schema)
		 */
		@Override
		public void validate(Schema schema) {
			super.validate(schema);
			if (schema.getType() != Schema.Type.BYTES) {
				throw new IllegalArgumentException(
						"BLOB can only be used with an underlying bytes type");
			}
		}

		/**
		 * Returns the LogicalType of the blob
		 *
		 * @return LogicalType of BLOB
		 */
		@Override
		public LogicalType fromSchema(Schema schema) {
			return this;
		}
	}
}
