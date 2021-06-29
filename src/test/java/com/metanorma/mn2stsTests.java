package com.metanorma;

import static com.metanorma.Constants.*;
import com.metanorma.utils.LoggerHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import org.apache.commons.cli.ParseException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.junit.Before;

import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class mn2stsTests {

    private static final Logger logger = Logger.getLogger(LoggerHelper.LOGGER_NAME);

    private static OutputStream logCapturingStream;
    private static StreamHandler customLogHandler;
    
    static String XMLFILE_MN;// = "test.mn.xml";
    //final String XMLFILE_STS = "test.sts.xml";
    
    @Rule
    public final ExpectedSystemExit exitRule = ExpectedSystemExit.none();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Rule
    public final EnvironmentVariables envVarRule = new EnvironmentVariables();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        XMLFILE_MN = System.getProperty("inputXML");        
    }
    
    
    @Before
    public void attachLogCapturer()
    {
        logCapturingStream = new ByteArrayOutputStream();
        Handler[] handlers = logger.getParent().getHandlers();
        customLogHandler = new StreamHandler(logCapturingStream, handlers[0].getFormatter());
        logger.addHandler(customLogHandler);
    }
    
    public String getTestCapturedLog() throws IOException
    {
        customLogHandler.flush();
        return logCapturingStream.toString();
    }
    
    @Test
    public void notEnoughArguments() throws ParseException {
        exitRule.expectSystemExitWithStatus(-1);
        String[] args = new String[]{"1"};
        mn2sts.main(args);

        //assertTrue(systemOutRule.getLog().contains(mn2sts.USAGE));
    }

    
    @Test
    public void xmlNotExists() throws ParseException {
        exitRule.expectSystemExitWithStatus(-1);

        String[] args = new String[]{"--xml-file-in", "test.xml", "--xml-file-out", "out.xml"};
        mn2sts.main(args);

        /*assertTrue(systemOutRule.getLog().contains(
                String.format(INPUT_NOT_FOUND, XML_INPUT, args[1])));*/
    }

    @Test
    public void xslNotExists() throws ParseException {
        exitRule.expectSystemExitWithStatus(-1);

        // ClassLoader classLoader = getClass().getClassLoader();        
        //String xml = classLoader.getResource(XMLFILE_MN).getFile();
        
        String[] args = new String[]{"--xml-file-in", XMLFILE_MN, "--xsl-file", "alternate.xsl", "--xml-file-out", "out.xml"};
        mn2sts.main(args);

        /*assertTrue(systemOutRule.getLog().contains(
                String.format(INPUT_NOT_FOUND, XSL_INPUT, args[2])));*/
    }

    @Test
    public void successConvertAndCheckXSD() throws ParseException, Exception {
        // ClassLoader classLoader = getClass().getClassLoader();        
        //String xml = classLoader.getResource(XMLFILE_MN).getFile();
        
        Path xmlout = Paths.get(System.getProperty("buildDirectory"), "out.xml");

        String[] args = new String[]{"--xml-file-in",  XMLFILE_MN, "--xml-file-out", xmlout.toAbsolutePath().toString()};
        mn2sts.main(args);

        assertTrue(Files.exists(xmlout));
        String capturedLog = getTestCapturedLog();
        //assertTrue(systemOutRule.getLog().contains("is valid"));
        assertTrue(capturedLog.contains("is valid"));
    }
    
    @Test
    public void successConvertAndCheckDTD_NISO() throws ParseException, Exception {
        // ClassLoader classLoader = getClass().getClassLoader();        
        //String xml = classLoader.getResource(XMLFILE_MN).getFile();        
        
        Path xmlout = Paths.get(System.getProperty("buildDirectory"), "out.xml");

        String[] args = new String[]{"--xml-file-in",  XMLFILE_MN, "--xml-file-out", xmlout.toAbsolutePath().toString(), "--check-type", "dtd-niso"};
        mn2sts.main(args);

        assertTrue(Files.exists(xmlout));
        String capturedLog = getTestCapturedLog();
        assertTrue(capturedLog.contains("is valid"));
        
    }
    
    @Test
    // Element type "code" must be declared. etc...
    public void NoSuccessConvertAndCheckDTD_ISO() throws ParseException, Exception {
        exitRule.expectSystemExitWithStatus(-1);
        
        //ClassLoader classLoader = getClass().getClassLoader();        
        //String xml = classLoader.getResource(XMLFILE_MN).getFile();        
        Path xmlout = Paths.get(System.getProperty("buildDirectory"), "out.xml");

        String[] args = new String[]{"--xml-file-in",  XMLFILE_MN, "--xml-file-out", xmlout.toAbsolutePath().toString(), "--check-type", "dtd-iso"};
        mn2sts.main(args);

        /*assertTrue(Files.exists(xmlout));
        assertTrue(systemOutRule.getLog().contains("is NOT valid"));*/
    }
    
    @Test
    // Element type "code" must be declared. etc...
    public void successConvertAndCheckDTD_ISO() throws ParseException, Exception {        
        // ClassLoader classLoader = getClass().getClassLoader();        
        //String xml = classLoader.getResource(XMLFILE_MN).getFile();        
        Path xmlout = Paths.get(System.getProperty("buildDirectory"), "out.xml");

        String[] args = new String[]{"--xml-file-in",  XMLFILE_MN, "--xml-file-out", xmlout.toAbsolutePath().toString(), "--check-type", "dtd-iso", "--output-format", "iso"};
        mn2sts.main(args);

        assertTrue(Files.exists(xmlout));
        String capturedLog = getTestCapturedLog();
        //assertTrue(systemOutRule.getLog().contains("is valid"));
        assertTrue(capturedLog.contains("is valid"));
    }
    
    @Test
    public void regexHelperText() {
        String res = RegExHelper.matches("^.*:\\d{4}\\D.*$", "PD 6079-4:2006 (Book)");
        assertTrue(res.equals("true"));
    }
    /*@Test
    public void successCheckXSD() throws ParseException {
        ClassLoader classLoader = getClass().getClassLoader();        
        String xml = classLoader.getResource(XMLFILE_STS).getFile();                

        String[] args = new String[]{"--xml-file-in",  xml};
        mn2sts.main(args);

        assertTrue(systemOutRule.getLog().contains("is valid"));
    }
    
    @Test
    // Element type "code" must be declared. etc...
    public void NoSuccessCheckDTD_ISO() throws ParseException {
        exitRule.expectSystemExitWithStatus(-1);
        
        ClassLoader classLoader = getClass().getClassLoader();        
        String xml = classLoader.getResource(XMLFILE_STS).getFile();                

        String[] args = new String[]{"--xml-file-in",  xml, "--check-type", "dtd-iso"};
        mn2sts.main(args);

        assertTrue(systemOutRule.getLog().contains("is NOT valid"));
    }
    
    @Test
    public void successCheckDTD_NISO() throws ParseException {
        ClassLoader classLoader = getClass().getClassLoader();        
        String xml = classLoader.getResource(XMLFILE_STS).getFile();                

        String[] args = new String[]{"--xml-file-in",  xml, "--check-type", "dtd-niso"};
        mn2sts.main(args);

        assertTrue(systemOutRule.getLog().contains("is valid"));
    }*/
    
   /* @Test
    public void nosuccessDTD_ISO() throws ParseException {
        //exitRule.expectSystemExitWithStatus(-1);

        ClassLoader classLoader = getClass().getClassLoader();
        String xml = classLoader.getResource(XMLFILE).getFile();
        Path xmlout = Paths.get(System.getProperty("buildDirectory"), "out.xml");

        String[] args = new String[]{"--xml-file-in",  xml, "--xml-file-out", xmlout.toAbsolutePath().toString(), "--check-type", "dtd-iso", "-d"};
        mn2sts.main(args);

        assertTrue(Files.exists(xmlout));
        assertTrue(systemOutRule.getLog().contains("is valid"));
    }*/
    
}
