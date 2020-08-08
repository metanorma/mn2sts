package com.metanorma;

import com.metanorma.validator.CheckAgainstEnum;
import com.metanorma.validator.CheckAgainstMap;
import com.metanorma.validator.DTDValidator;
import com.metanorma.validator.XSDValidator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.security.CodeSource;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xml.sax.SAXParseException;

/**
 * This class for the conversion of an XML file to PDF using FOP and JEuclid
 */
public class mn2sts {

    static final String CMD = "java -jar mn2sts.jar";
    static final String INPUT_NOT_FOUND = "Error: %s file '%s' not found!";
    static final String XML_INPUT = "XML";
    static final String XML_OUTPUT = "XML";
    static final String XSL_INPUT = "XSL";
    static final String INPUT_LOG = "Input: %s (%s)";
    static final String OUTPUT_LOG = "Output: %s (%s), format %s STS";

    static CheckAgainstEnum checkAgainst = CheckAgainstEnum.XSD_NISO;
    
    static OutputFormatEnum outputFormat = OutputFormatEnum.NISO;
    
    static boolean DEBUG = false;

    static String VER = Util.getAppVersion();
    
    static final Options optionsInfo = new Options() {
        {
            addOption(Option.builder("v")
                    .longOpt("version")
                    .desc("display application version")
                    .required(true)
                    .build());
        }
    };
    
    static final Options optionsCheckOnly = new Options() {
        {
            addOption(Option.builder("v")
                    .longOpt("version")
                    .desc("display application version")
                    .required(false)
                    .build());
            addOption(Option.builder("x")
                    .longOpt("xml-file-in")
                    .desc("path to source XML file")
                    .hasArg()
                    .argName("file")
                    .required(true)
                    .build());
            addOption(Option.builder("t")
                    .longOpt("check-type")
                    .desc("Check against XSD NISO (value xsd-niso), DTD ISO (dtd-iso), DTD NISO (dtd-niso) (Default: xsd-niso)")
                    .hasArg()
                    .argName("xsd-niso|dtd-iso|dtd-niso")
                    .required(false)
                    .build());
        }
    };

    static final Options options = new Options() {
        {
            addOption(Option.builder("x")
                    .longOpt("xml-file-in")
                    .desc("path to source XML file")
                    .hasArg()
                    .argName("file")
                    .required(true)
                    .build());
            addOption(Option.builder("s")
                    .longOpt("xsl-file")
                    .desc("path to XSL file (optional)")
                    .hasArg()
                    .argName("file")
                    .required(false)
                    .build());
            addOption(Option.builder("o")
                    .longOpt("xml-file-out")
                    .desc("path to output XML file")
                    .hasArg()
                    .argName("file")
                    .required(true)
                    .build());
            addOption(Option.builder("d")
                    .longOpt("debug")
                    .desc("print additional debug information into output XML file")
                    .required(false)
                    .build());
            addOption(Option.builder("v")
                    .longOpt("version")
                    .desc("display application version")
                    .required(false)
                    .build());
            addOption(Option.builder("t")
                    .longOpt("check-type")
                    .desc("Check against XSD NISO (value 'xsd-niso'), DTD ISO (value 'dtd-iso'), DTD NISO (value 'dtd-niso') (Default: xsd-niso)")
                    .hasArg()
                    .argName("xsd-niso|dtd-iso|dtd-niso")
                    .required(false)
                    .build());
            addOption(Option.builder("f")
                    .longOpt("output-format")
                    .desc("Output format: NISO STS (value 'niso') or ISO STS (value 'iso') (Default: niso)")
                    .hasArg()
                    .argName("niso|iso")
                    .required(false)
                    .build());
        }
    };

    static final String USAGE = getUsage();

    static final int ERROR_EXIT_CODE = -1;


    /**
     * Converts an MN XML file to NISO STS XML file
     *
     * @param xmlin the XML source file
     * @param xsl the external XSL file
     * @param xmlout the XML output file
     * @throws IOException In case of an I/O problem
     * @throws javax.xml.transform.TransformerException
     */
    private void convertmn2sts(File xmlin, File xsl, File xmlout) throws IOException, TransformerException, SAXParseException {
        
        try {
            OutputJaxpImplementationInfo();

            Source srcXSL = null;
            if (xsl != null) { //external xsl
                srcXSL = new StreamSource(xsl);
            } else { // internal xsl
                srcXSL = new StreamSource(Util.getStreamFromResources(getClass().getClassLoader(), "mn2sts.xsl"));
            }

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(srcXSL);
            transformer.setParameter("debug", DEBUG);
            transformer.setParameter("outputformat", outputFormat);
 
            Source src = new StreamSource(xmlin);
            StringWriter resultWriter = new StringWriter();
            StreamResult sr = new StreamResult(resultWriter);
            System.out.println("Transforming...");
            transformer.transform(src, sr);
            String xmlSTS = resultWriter.toString();

            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(xmlout.getAbsolutePath()))) {
                writer.write(xmlSTS);
            }
            
            // check agains XSD NISO, DTD NISO or DTD ISO
            checkSTS(xmlout);
                
        } catch (SAXParseException e) {            
            throw (e);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(ERROR_EXIT_CODE);
        }
    }

    private void checkSTS(File xml) throws SAXParseException {
            
        List<String> exceptions = new LinkedList<>(); 

        String checkSrc = CheckAgainstMap.getMap().get(checkAgainst);

        boolean isXSDcheck = checkSrc.toLowerCase().endsWith(".xsd");

        if (isXSDcheck) {
            XSDValidator xsdValidator = new XSDValidator(xml);
            exceptions = xsdValidator.validate(checkAgainst);                
        } else {                
            DTDValidator dtdValidator = new DTDValidator(xml);
            dtdValidator.setDebug(DEBUG);
            exceptions = dtdValidator.validate(checkAgainst);                
        }


        StringBuilder sbErrors = new StringBuilder();
        if (exceptions.isEmpty()) {
            System.out.println(xml.getAbsolutePath() + " is valid.");            
        } else {
            sbErrors.append(xml.getAbsolutePath());
            sbErrors.append(" is NOT valid reason:");
            sbErrors.append("\n");
            //System.out.println(xml.getAbsolutePath() + " is NOT valid reason:");
            exceptions.forEach((exception) -> {
                //System.out.println("[ERROR] " + exception);
                sbErrors.append("[ERROR] ");
                sbErrors.append(exception);
                sbErrors.append("\n");
            });
            throw new SAXParseException(sbErrors.toString(), null);
        }   
    }
    
    /**
     * Main method.
     *
     * @param args command-line arguments
     * @throws org.apache.commons.cli.ParseException
     */
    public static void main(String[] args) throws ParseException {

        CommandLineParser parser = new DefaultParser();
               
        boolean cmdFail = false;
        
        try {
            CommandLine cmdInfo = parser.parse(optionsInfo, args);
            printVersion(cmdInfo.hasOption("version"));            
        } catch (ParseException exp) {
            cmdFail = true;
        }
        
        if(cmdFail) {
            try {
                CommandLine cmdCheckOnly = parser.parse(optionsCheckOnly, args);
                System.out.print("mn2sts ");
                if(cmdCheckOnly.hasOption("version")) {                    
                    System.out.print(VER);
                }            
                System.out.println("\n");
                
                printVersion(cmdCheckOnly.hasOption("version"));
                
                final String argXMLin = cmdCheckOnly.getOptionValue("xml-file-in");
                File fXMLin = new File(argXMLin);
                if (!fXMLin.exists()) {
                    System.out.println(String.format(INPUT_NOT_FOUND, XML_INPUT, fXMLin));
                    System.exit(ERROR_EXIT_CODE);
                }
                
                if (cmdCheckOnly.hasOption("check-type")) {
                    String ctype = cmdCheckOnly.getOptionValue("check-type");  
                    ctype = ctype.replace("-", "_").toUpperCase();
                    if (CheckAgainstEnum.valueOf(ctype) != null) {
                        checkAgainst = CheckAgainstEnum.valueOf(ctype);
                    } else {
                        System.out.println("Unknown option value: " + cmdCheckOnly.getOptionValue("check-type"));
                        System.exit(ERROR_EXIT_CODE);
                    }
                }
                
                System.out.println(String.format(INPUT_LOG, XML_INPUT, fXMLin));                
                System.out.println();

                try {
                    mn2sts app = new mn2sts();

                    app.checkSTS(fXMLin);
                    
                    System.out.println("End!");
                
                } catch (SAXParseException e) {
                    System.err.println(e.toString());
                    System.exit(ERROR_EXIT_CODE);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    System.exit(ERROR_EXIT_CODE);
                }
                cmdFail = false;
            } catch (ParseException exp) {
                cmdFail = true;
            }
        }        
        
        if(cmdFail) {            
            try {             
                CommandLine cmd = parser.parse(options, args);

                System.out.print("mn2sts ");
                if(cmd.hasOption("version")) {                    
                    System.out.print(VER);
                }                
                System.out.println("\n");

                printVersion(cmd.hasOption("version"));
                
                final String argXMLin = cmd.getOptionValue("xml-file-in");
                File fXMLin = new File(argXMLin);
                if (!fXMLin.exists()) {
                    System.out.println(String.format(INPUT_NOT_FOUND, XML_INPUT, fXMLin));
                    System.exit(ERROR_EXIT_CODE);
                }

                final String argXSL = cmd.getOptionValue("xsl-file");
                File fXSL = null;
                if (argXSL != null) {
                    fXSL = new File(argXSL);
                    if (!fXSL.exists()) {
                        System.out.println(String.format(INPUT_NOT_FOUND, XSL_INPUT, fXSL));
                        System.exit(ERROR_EXIT_CODE);
                    }
                }

                final String argXMLout = cmd.getOptionValue("xml-file-out");
                File fXMLout = new File(argXMLout);

                DEBUG = cmd.hasOption("debug");

                if (cmd.hasOption("check-type")) {
                    String ctype = cmd.getOptionValue("check-type");  
                    ctype = ctype.replace("-", "_").toUpperCase();                    
                    try {
                        checkAgainst = CheckAgainstEnum.valueOf(ctype);
                    } catch (Exception ex) {
                        System.out.println("Unknown option value: " + cmd.getOptionValue("check-type"));
                        System.exit(ERROR_EXIT_CODE);
                    }
                }

                if (cmd.hasOption("output-format")) {
                    String outputtype = cmd.getOptionValue("output-format");  
                    outputtype = outputtype.toUpperCase();                    
                    try {
                        outputFormat = OutputFormatEnum.valueOf(outputtype);
                    } catch (Exception ex) {
                        System.out.println("Unknown option value: " + cmd.getOptionValue("output-format"));
                        System.exit(ERROR_EXIT_CODE);
                    }
                }
                
                
                System.out.println(String.format(INPUT_LOG, XML_INPUT, fXMLin));
                if (fXSL != null) {
                    System.out.println(String.format(INPUT_LOG, XSL_INPUT, fXSL));
                }
                System.out.println(String.format(OUTPUT_LOG, XML_OUTPUT, fXMLout, outputFormat));
                System.out.println();

                try {
                    mn2sts app = new mn2sts();

                    app.convertmn2sts(fXMLin, fXSL, fXMLout);
                    System.out.println("End!");
                } catch (SAXParseException e) {
                    System.err.println(e.toString());
                    System.exit(ERROR_EXIT_CODE);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    System.exit(ERROR_EXIT_CODE);
                }
                cmdFail = false;
            } catch (ParseException exp) {
                cmdFail = true;
            }
        }
        
        if (cmdFail) {
            System.out.println(USAGE);
            System.exit(ERROR_EXIT_CODE);
        }
    }

    private static String getUsage() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter pw = new PrintWriter(stringWriter);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(pw, 80, CMD, "", options, 0, 0, "");
        pw.flush();
        return stringWriter.toString();
    }

    private static void OutputJaxpImplementationInfo() {
        if (DEBUG) {
            System.out.println(getJaxpImplementationInfo("DocumentBuilderFactory", DocumentBuilderFactory.newInstance().getClass()));
            System.out.println(getJaxpImplementationInfo("XPathFactory", XPathFactory.newInstance().getClass()));
            System.out.println(getJaxpImplementationInfo("TransformerFactory", TransformerFactory.newInstance().getClass()));
            System.out.println(getJaxpImplementationInfo("SAXParserFactory", SAXParserFactory.newInstance().getClass()));
        }
    }

    private static String getJaxpImplementationInfo(String componentName, Class componentClass) {
        CodeSource source = componentClass.getProtectionDomain().getCodeSource();
        return MessageFormat.format(
                "{0} implementation: {1} loaded from: {2}",
                componentName,
                componentClass.getName(),
                source == null ? "Java Runtime" : source.getLocation());
    }    

    private static void printVersion(boolean print) {
        if (print) {            
            System.out.println(VER);
        }
    }       

}