/*      PANDA -- a simple transaction monitor

Copyright (C) 1998-1999 Ogochan.
              2000-2003 Ogochan & JMA (Japan Medical Association).
              2002-2006 OZAWA Sakuro.

This module is part of PANDA.

		PANDA is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY.  No author or distributor accepts responsibility
to anyone for the consequences of using it or for whether it serves
any particular purpose or works at all, unless he says so in writing.
Refer to the GNU General Public License for full details.

		Everyone is granted permission to copy, modify and redistribute
PANDA, but only under the conditions described in the GNU General
Public License.  A copy of this license is supposed to have been given
to you along with PANDA so you can know your rights and
responsibilities.  It should be in a file named COPYING.  Among other
things, the copyright notice and this notice must be preserved on all
copies.
*/

package org.montsuqi.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.Format;
import java.text.MessageFormat;

public class OptionParser {

	static final String CONFIG_TRAILER;

	static {
		if (System.getProperty("os.name").startsWith("Windows")) { //$NON-NLS-1$ //$NON-NLS-2$
			CONFIG_TRAILER = ".CFG"; //$NON-NLS-1$
		} else {
			CONFIG_TRAILER = ".conf"; //$NON-NLS-1$
		}
	}

	private static final char COMMAND_SWITCH = '-';
	private static final char RESPONSE_FILE_SWITCH = '@';

	private Map options;
	private static final Logger logger = Logger.getLogger(OptionParser.class);

    public OptionParser() {
        options = new TreeMap();
    }

    public void add(String name, String message, boolean defaultValue) {
		add(new Option(name, message, defaultValue));
    }

    public void add(String name, String message, int defaultValue) {
		add(new Option(name, message, defaultValue));
    }

    public void add(String name, String message, String defaultValue) {
		add(new Option(name, message, defaultValue));
    }

	private void add(Option option) {
		options.put(option.getName(), option);
	}

	private Object getValue(String name) {
		if (options.containsKey(name)) {
			return ((Option)options.get(name)).getValue();
		}
		Object[] args = { name };
		throw new IllegalArgumentException(MessageFormat.format("no such option: {0}", args)); //$NON-NLS-1$
	}

	public String getString(String key) {
		return (String)getValue(key);
	}

	public int getInt(String key) {
		return ((Integer)getValue(key)).intValue();
	}


	public boolean getBoolean(String key) {
		return ((Boolean)getValue(key)).booleanValue();
	}

	public String[] parse(String program, String[] args) {
		try {
			List result = new LinkedList();
			File paramFile = new File(changeSuffix(program, CONFIG_TRAILER));
			if (paramFile.canRead()) {
				result.addAll(parseFile(program, paramFile));
			}
			result.addAll(parseArray(program, args));
			return (String[])result.toArray(new String[result.size()]);
		} catch (IllegalArgumentException e) {
			logger.warn(e);
			return args;
		}
	}

	private List parseFile(String program, File file) {
		List lines = new LinkedList();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0) {
					lines.add(line);
				}
			}
		} catch (IOException e) {
			throw new OptionParserException(e);
		}
		return parseArray(program, lines.toArray(new String[lines.size()]));
	}

	private List parseArray(String program, Object[] args) {
		List files = new LinkedList();
		for (int i = 0; i < args.length; i++) {
			boolean isParam = false;
			String arg = (String)args[i];
			char c = arg.charAt(0);
			switch (c) {
			case RESPONSE_FILE_SWITCH:
				isParam = true;
				File paramFile = new File(arg.substring(1));
				if (paramFile.canRead()) {
					files.addAll(parseFile(program, paramFile));
				}
				break;
			case COMMAND_SWITCH:
				isParam = true;
				if (arg.equals("?") || arg.equals("h") || arg.equals("H")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					System.out.println(usage("USAGE:" + program + " <option(s)> files...")); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					isParam = analyzeLine(arg.substring(1));
				}
				break;
			default:
				isParam = analyzeLine(arg);
				break;
			}
			if ( ! isParam) {
				files.add(arg);
			}
		}
		return files;
	}

	private boolean analyzeLine(String line) {
		if (line.startsWith(";")) { //$NON-NLS-1$
			logger.info("skipping comment: " + line); //$NON-NLS-1$
			return false;
		}
		Iterator i = options.values().iterator();
		while (i.hasNext()) {
			Option option = (Option)i.next();
			String name = option.getName();

			if (line.substring(0, name.length()).equals(name)) {
				String arg = line.substring(name.length()).trim();
				if (arg.length() > 0 && arg.charAt(0) == '=') {
					arg = arg.substring(1);
				}
				if (arg.length() > 0 || option.getType() == Boolean.class) {
					option.setValue(arg);
					return true;
				}
			}
		}
		return false;
	}

	public String usage(String comment) {
		Iterator i = options.values().iterator();
		Format format = new MessageFormat("-{0} : {1}\n"); //$NON-NLS-1$
		// "  -%-12s : %-40s"
		StringBuffer usage = new StringBuffer();
		usage.append(comment);
		usage.append("\n"); //$NON-NLS-1$
		while (i.hasNext()) {
			Option o = (Option)i.next();
			Object[] args = { o.getName(), o.getMessage() };
			usage.append(format.format(args));
			Object value = o.getValue();
			if (value != null) {
				usage.append("\t[" + value + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				usage.append("\n"); //$NON-NLS-1$
			}
		}
		usage.append("\n"); //$NON-NLS-1$
		return usage.toString();
	}

	private String changeSuffix(String orig, String suffix) {
		if (orig.indexOf('.') >= 0) {
			return orig.substring(0, orig.lastIndexOf('.')) + suffix;
		}
		return orig + suffix;
	}

	public static void main(String[] args) {
		OptionParser parser = new OptionParser();

		parser.add("a", "aaa", "abc"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		parser.add("b", "bbb", 10); //$NON-NLS-1$ //$NON-NLS-2$
		parser.add("c", "ccc", false); //$NON-NLS-1$ //$NON-NLS-2$

		args = parser.parse("OptionParser", args); //$NON-NLS-1$

		System.out.println("*****\n"); //$NON-NLS-1$
		System.out.println(parser.usage("Usage")); //$NON-NLS-1$
		for (int i = 0; i < args.length; i++) {
			System.out.println(args[i]);
		}
	}
}

