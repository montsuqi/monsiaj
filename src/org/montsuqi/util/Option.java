package org.montsuqi.util;

class Option {
	String name;
	String message;
	Object value;
	
	private Option(String name, String message, Object value) {
		this.name = name;
		this.message = message;
		this.value = value;
	}
	
	public Option(String name, String message, boolean defaultValue) {
		this(name, message, new Boolean(defaultValue));
	}

	public Option(String name, String message, int defaultValue) {
		this(name, message, new Integer(defaultValue));
	}

	public Option(String name, String message, String defaultValue) {
		this(name, message, (Object)defaultValue);
	}

	public String getName() {
		return name;
	}

	public String getMessage() {
		return message;
	}

	public Object getValue() {
		return value;
	}

	public Class getType() {
		return value.getClass();
	}

	public String toString() {
		Class type = value.getClass();
		if (type == Boolean.class) {
			if (((Boolean)value).booleanValue()) {
				return "ON"; //$NON-NLS-1$
			} else {
				return "OFF"; //$NON-NLS-1$
			}
		} else {
			return value.toString();
		}
	}

	public void setValue(String arg) {
		arg = arg.trim();
		Class type = value.getClass();
		if (type == Boolean.class) {
			if (arg.length() > 0) {
				if (arg.charAt(0) == '+') {
					setValue(false);
				} else {
					if (arg.charAt(0) == '-') {
						setValue(true);
					} else {
						setValue(!((Boolean)value).booleanValue()); /* toggle */
					}
				}
			} else {
				setValue(!((Boolean)value).booleanValue()); /* toggle */
			}
		} else if (type == Integer.class) {
			setValue(Integer.parseInt(arg));
		} else if (type == String.class) {
			if (arg.length() != 0) {
				value = arg;
			} else {
				value = null;
			}
		}
	}

	public void setValue(boolean arg) {
		if (value.getClass() == Boolean.class) {
			value = new Boolean(arg);
		} else {
			throw new IllegalArgumentException(Messages.getString("Option.type_mismatch")); //$NON-NLS-1$
		}
		
	}

	public void setValue(int value) {
		if (this.value.getClass() == Integer.class) {
			this.value = new Integer(value);
		} else {
			throw new IllegalArgumentException(Messages.getString("Option.type_mismatch")); //$NON-NLS-1$
		}
	}
}
			
