package org.montsuqi.tools;

import junit.extensions.xml.XMLTestSuite;
import junit.extensions.xml.XMLUtil;
import junit.framework.Test;
import junit.textui.TestRunner;

/**
 * A class written as an example of how to use the jfcUnit
 * framework. This class is used to "test" the SwingSet demo
 * shipped with JDK 1.2.2
 *
 * @author <a href="mailto:vraravam@thoughtworks.com">Vijay Aravamudhan : ThoughtWorks Inc.</a>
 */
public class TestXMLRecording extends XMLTestSuite {

    /** Document factory key. */
    public static final String DOCUMENT_FACTORY = "javax.xml.parsers.DocumentBuilderFactory";

    /**
     * Construct a TestXMLRecording().
     * Use testSwingSet.xml as the XML test definition.
     */
    public TestXMLRecording(String[] args) {
        super("xmlrecordingtemplate.xml",
              XMLUtil.readFileFromClassContext(
                TestXMLRecording.class,
                "xmlrecordingtemplate.xml"));
	org.montsuqi.client.Client.main(args);

        try {
          Thread.sleep(3000);
        } catch (InterruptedException ex) {
          // Ignore
        }
    }

    /**
     * Create a test suite for this object.
     * @return Test Suite of tests to be executed.
     */
    public static Test suite(String[] args) {
        return new TestXMLRecording(args);
    }

    /**
     * Main method to run this class from the command line.
     * Use the internal parse if available otherwise use the
     * xerces parser.
     *
     * @param args   Command line arguments.
     */
    public static void main(final String[] args) {
        TestRunner.run(TestXMLRecording.suite(args));
    }
}
