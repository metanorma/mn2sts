package com.metanorma;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.cli.ParseException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class mn2stsTests {

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
    
    @Test
    public void notEnoughArguments() throws ParseException {
        exitRule.expectSystemExitWithStatus(-1);
        String[] args = new String[]{"1"};
        mn2sts.main(args);

        assertTrue(systemOutRule.getLog().contains(mn2sts.USAGE));
    }

    
    @Test
    public void xmlNotExists() throws ParseException {
        exitRule.expectSystemExitWithStatus(-1);

        String[] args = new String[]{"--xml-file-in", "test.xml", "--xml-file-out", "out.xml"};
        mn2sts.main(args);

        assertTrue(systemOutRule.getLog().contains(
                String.format(mn2sts.INPUT_NOT_FOUND, mn2sts.XML_INPUT, args[1])));
    }

    @Test
    public void xslNotExists() throws ParseException {
        exitRule.expectSystemExitWithStatus(-1);

        ClassLoader classLoader = getClass().getClassLoader();        
        //String xml = classLoader.getResource(XMLFILE_MN).getFile();
        
        String[] args = new String[]{"--xml-file-in", XMLFILE_MN, "--xsl-file", "alternate.xsl", "--xml-file-out", "out.xml"};
        mn2sts.main(args);

        assertTrue(systemOutRule.getLog().contains(
                String.format(mn2sts.INPUT_NOT_FOUND, mn2sts.XSL_INPUT, args[2])));
    }

    @Test
    public void successConvertAndCheckXSD() throws ParseException {
        ClassLoader classLoader = getClass().getClassLoader();        
        //String xml = classLoader.getResource(XMLFILE_MN).getFile();
        
        Path xmlout = Paths.get(System.getProperty("buildDirectory"), "out.xml");

        String[] args = new String[]{"--xml-file-in",  XMLFILE_MN, "--xml-file-out", xmlout.toAbsolutePath().toString()};
        mn2sts.main(args);

        assertTrue(Files.exists(xmlout));
        assertTrue(systemOutRule.getLog().contains("is valid"));
    }
    
    @Test
    public void successConvertAndCheckDTD_NISO() throws ParseException {
        ClassLoader classLoader = getClass().getClassLoader();        
        //String xml = classLoader.getResource(XMLFILE_MN).getFile();        
        Path xmlout = Paths.get(System.getProperty("buildDirectory"), "out.xml");

        String[] args = new String[]{"--xml-file-in",  XMLFILE_MN, "--xml-file-out", xmlout.toAbsolutePath().toString(), "--check-type", "dtd-niso"};
        mn2sts.main(args);

        assertTrue(Files.exists(xmlout));
        assertTrue(systemOutRule.getLog().contains("is valid"));
    }
    
    @Test
    // Element type "code" must be declared. etc...
    public void NoSuccessConvertAndCheckDTD_ISO() throws ParseException {
        exitRule.expectSystemExitWithStatus(-1);
        
        ClassLoader classLoader = getClass().getClassLoader();        
        //String xml = classLoader.getResource(XMLFILE_MN).getFile();        
        Path xmlout = Paths.get(System.getProperty("buildDirectory"), "out.xml");

        String[] args = new String[]{"--xml-file-in",  XMLFILE_MN, "--xml-file-out", xmlout.toAbsolutePath().toString(), "--check-type", "dtd-iso"};
        mn2sts.main(args);

        assertTrue(Files.exists(xmlout));
        assertTrue(systemOutRule.getLog().contains("is NOT valid"));
    }
    
    @Test
    // Element type "code" must be declared. etc...
    public void successConvertAndCheckDTD_ISO() throws ParseException {        
        ClassLoader classLoader = getClass().getClassLoader();        
        //String xml = classLoader.getResource(XMLFILE_MN).getFile();        
        Path xmlout = Paths.get(System.getProperty("buildDirectory"), "out.xml");

        String[] args = new String[]{"--xml-file-in",  XMLFILE_MN, "--xml-file-out", xmlout.toAbsolutePath().toString(), "--check-type", "dtd-iso", "--output-format", "iso"};
        mn2sts.main(args);

        assertTrue(Files.exists(xmlout));
        assertTrue(systemOutRule.getLog().contains("is valid"));
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
