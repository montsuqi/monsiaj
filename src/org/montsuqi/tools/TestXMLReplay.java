package org.montsuqi.tools;

import junit.extensions.xml.XMLTestSuite;
import junit.extensions.jfcunit.xml.XMLRecorder;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

import java.io.File;
import java.io.FilenameFilter;

class XMLFileFilter implements FilenameFilter {
	public boolean accept(File dir, String name) {
		return name.endsWith(".xml");
	}
}
/**
 * This class starts the XML Test scripts "saved.xml"
 * It also sets the XMLRecorder.setReplay(true) which
 * causes the Record statements to be ignored.
 *
 * @author <a href="mailto:kwilson227@users.sourceforge.net">Kevin Wilson</a>
 * @author <a href="mailto:vraravam@thoughtworks.com">Vijay Aravamudhan : ThoughtWorks Inc.</a>
 */
public class TestXMLReplay extends XMLTestSuite {

    /** Document factory key. */
    public static final String DOCUMENT_FACTORY = "javax.xml.parsers.DocumentBuilderFactory";

    /**
     * Construct a TestXMLReplay().
     * Use testSwingSet.xml as the XML test definition.
     */
    public TestXMLReplay(String foldername, String filename, String[] args) {
	    super(filename,  openFile(foldername + File.separator + filename));
	    XMLRecorder.setReplay(true);
//	    jp.or.med.jma_receipt.Launcher.main(new String[] {});
//	    String args[] = {"-host=192.168.1.177", "-user=ormaster", "panda:orca00"};
	    org.montsuqi.client.Client.main(args);

	    try {
		    Thread.currentThread().sleep(3000);
//		    Thread.currentThread().sleep(30);
	    } catch (Exception e) {
		    // Ignore
	    }
    }

    /**
     * Open the file name given.
     * @param fileName File name to be opened
     * @return InputStrem to be processed.
     */
    private static InputStream openFile(final String fileName) {
      try {
        return new FileInputStream(fileName);
      } catch (IOException ioe) {
        return null;
      }
    }

    /**
     * Create a test suite for this object.
     * @return Test Suite of tests to be executed.
     */
    public static Test suite(String[] args) {
	    TestSuite suite = new TestSuite("XMLRecordTest");
	    FilenameFilter namefilter = new XMLFileFilter();;
	    String foldername = "testrecords";
	    File fp = new File(foldername);
	    namefilter.accept(fp, ".xml");
	    String[] fileList = fp.list(namefilter );
	    for( int i = 0 ; i < fileList.length ; i ++ ){
		    suite.addTest( new TestXMLReplay(foldername, fileList[i], args));
	    }

	    return suite;
    }

    /**
     * Main method to run this class from the command line.
     * Use the internal parse if available otherwise use the
     * xerces parser.
     *
     * @param args   Command line arguments.
     */
    public static void main(final String[] args) {
        TestRunner.run((Test) TestXMLReplay.suite(args));
    }
}
