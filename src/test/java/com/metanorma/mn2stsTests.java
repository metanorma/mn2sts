package com.metanorma;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.cli.ParseException;

import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class mn2stsTests {

    @Rule
    public final ExpectedSystemExit exitRule = ExpectedSystemExit.none();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Rule
    public final EnvironmentVariables envVarRule = new EnvironmentVariables();

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
        String xml = classLoader.getResource("iso-tc154-8601-1-en.xml").getFile();

        String[] args = new String[]{"--xml-file-in", xml, "--xsl-file", "alternate.xsl", "--xml-file-out", "out.xml"};
        mn2sts.main(args);

        assertTrue(systemOutRule.getLog().contains(
                String.format(mn2sts.INPUT_NOT_FOUND, mn2sts.XSL_INPUT, args[2])));
    }

    @Test
    public void successXSD() throws ParseException {
        ClassLoader classLoader = getClass().getClassLoader();        
        String xml = classLoader.getResource("iso-tc154-8601-1-en.xml").getFile();        
        Path xmlout = Paths.get(System.getProperty("buildDirectory"), "out.xml");

        String[] args = new String[]{"--xml-file-in",  xml, "--xml-file-out", xmlout.toAbsolutePath().toString()};
        mn2sts.main(args);

        assertTrue(Files.exists(xmlout));
        assertTrue(systemOutRule.getLog().contains("is valid"));
    }
    
    @Test
    public void successDTD_NISO() throws ParseException {
        ClassLoader classLoader = getClass().getClassLoader();        
        String xml = classLoader.getResource("iso-tc154-8601-1-en.xml").getFile();        
        Path xmlout = Paths.get(System.getProperty("buildDirectory"), "out.xml");

        String[] args = new String[]{"--xml-file-in",  xml, "--xml-file-out", xmlout.toAbsolutePath().toString(), "--check-type", "dtd-niso"};
        mn2sts.main(args);

        assertTrue(Files.exists(xmlout));
        assertTrue(systemOutRule.getLog().contains("is valid"));
    }
    
    @Test
    public void nosuccessDTD_ISO() throws ParseException {
        //exitRule.expectSystemExitWithStatus(-1);
        
        ClassLoader classLoader = getClass().getClassLoader();        
        String xml = classLoader.getResource("iso-tc154-8601-1-en.xml").getFile();        
        Path xmlout = Paths.get(System.getProperty("buildDirectory"), "out.xml");

        String[] args = new String[]{"--xml-file-in",  xml, "--xml-file-out", xmlout.toAbsolutePath().toString(), "--check-type", "dtd-iso"};
        mn2sts.main(args);

        assertTrue(Files.exists(xmlout));
        assertTrue(systemOutRule.getLog().contains("is valid"));
    }
    
}
