package org.montsuqi.client;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ValueStruct {
	public int type;
	public int attr;
	public boolean update;
    public ValueStructUnion body;

	public ValueStruct(int type) {
		update = false;
		attr = AttributeType.NULL;
		this.type = type;
		switch (type) {
		case Type.BYTE:
		case Type.CHAR:
		case Type.VARCHAR:
		case Type.DBCODE:
		case Type.TEXT:
			body.CharData = ""; //$NON-NLS-1$
			break;
		case Type.NUMBER:
			body.FixedData = new BigDecimal("0.0"); //$NON-NLS-1$
			break;
		case Type.INT:
			body.IntegerData = 0;
			break;
		case Type.BOOL:
			body.BoolData = false;
			break;
//		case Type.OBJECT:
//			body.Object.apsid = 0;
//			body.Object.oid = 0;
//		break;
		case Type.RECORD:
			body.RecordData.count = 0;
			body.RecordData.members = new HashMap();
			body.RecordData.item = null;
			body.RecordData.names = null;
			break;
		case Type.ARRAY:
			body.ArrayData.count = 0;
			body.ArrayData.item = null;
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	public String toString() {
		switch (type) {
		case Type.CHAR:
		case Type.VARCHAR:
		case Type.DBCODE:
		case Type.TEXT:
			return body.CharData;
		case Type.NUMBER:
			return body.FixedData.toString();
		case Type.INT:
			return String.valueOf(body.IntegerData);
//		case Type.OBJECT:
//			return null;
		case Type.FLOAT:
			return String.valueOf(body.FloatData);
		case Type.BOOL:
			return body.BoolData ? "TRUE" : "FALSE"; //$NON-NLS-1$ //$NON-NLS-2$
		default:
			throw new IllegalStateException(Messages.getString("ValueStruct.unknown_value_type")); //$NON-NLS-1$
		}
	}
}

class ValueStructUnion {
	public ArrayDataPart ArrayData;
	public RecordDataPart RecordData;
	public String CharData;
	public BigDecimal FixedData;
	public int IntegerData;
	public double FloatData;
	public boolean BoolData;
}

class ArrayDataPart {
	public int count;
	public ValueStruct[] item;
}

class RecordDataPart {
	public int count;
	public ValueStruct[] item;
	public String[] names;
	public Map members;
}
