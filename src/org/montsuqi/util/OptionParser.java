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
		if (System.getProperty("os.name").startsWith("Windows")) {
			CONFIG_TRAILER = ".CFG";
		} else {
			CONFIG_TRAILER = ".conf";
		}
	}

	private static final char COMMAND_SWITCH = '-';
	private static final char RESPONSE_FILE_SWITCH = '@';

	private Map options;
	private Logger logger;

    public OptionParser() {
        options = new TreeMap();
		logger = Logger.getLogger(OptionParser.class);
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

	public Object getValue(String name) {
		return ((Option)options.get(name)).getValue();
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
			logger.info(e);
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
				if (arg.equals("?") || arg.equals("h") || arg.equals("H")) {
					System.out.println(usage("USAGE:" + program + " <option(s)> files..."));
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
		if (line.startsWith(";")) {
			logger.info("skipping comment: " + line);
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
		Format format = new MessageFormat("-{0} : {1}\n");
		// "  -%-12s : %-40s"
		StringBuffer usage = new StringBuffer();
		usage.append(comment);
		usage.append("\n");
		while (i.hasNext()) {
			Option o = (Option)i.next();
			usage.append(format.format(new Object[] { o.getName(), o.getMessage() }));
			Object value = o.getValue();
			if (value != null) {
				usage.append("\t[" + value + "]");
				usage.append("\n");
			}
		}
		usage.append("\n");
		return usage.toString();
	}

	private String changeSuffix(String orig, String suffix) {
		if (orig.indexOf('.') >= 0) {
			return orig.substring(0, orig.lastIndexOf('.')) + suffix;
		} else {
			return orig + suffix;
		}
	}

	public static void main(String[] args) {
		OptionParser parser = new OptionParser();

		parser.add("a", "aaa", "abc");
		parser.add("b", "bbb", 10);
		parser.add("c", "ccc", false);

		args = parser.parse("OptionParser", args);

		System.out.println("*****\n");
		System.out.println(parser.usage("Usage"));
		for (int i = 0; i < args.length; i++) {
			System.out.println(args[i]);
		}
	}
}

