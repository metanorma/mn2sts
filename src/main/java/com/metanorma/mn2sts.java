package com.metanorma;

import static com.metanorma.Constants.*;
import com.metanorma.utils.LoggerHelper;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * This application for the conversion of an XML file from Metanorma to NISO/ISO STS format
 */
public class mn2sts {

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



    

    /*private void checkSTS(File xml) throws SAXParseException {
            
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
    }*/
    
    /**
     * Main method.
     *
     * @param args command-line arguments
     * @throws org.apache.commons.cli.ParseException
     */
    public static void main(String[] args) throws ParseException {

        LoggerHelper.setupLogger();
        
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
                printVersion(cmdCheckOnly.hasOption("version"));
                System.out.println("\n");
                
                STSValidator validator = new STSValidator(cmdCheckOnly.getOptionValue("xml-file-in"), cmdCheckOnly.getOptionValue("check-type"));
                
                /*final String argXMLin = cmdCheckOnly.getOptionValue("xml-file-in");
                File fXMLin = new File(argXMLin);
                if (!fXMLin.exists()) {
                    System.out.println(String.format(INPUT_NOT_FOUND, XML_INPUT, fXMLin));
                    System.exit(ERROR_EXIT_CODE);
                }*/

                
                /*if (cmdCheckOnly.hasOption("check-type")) {
                    String ctype = cmdCheckOnly.getOptionValue("check-type");  
                    ctype = ctype.replace("-", "_").toUpperCase();
                    if (CheckAgainstEnum.valueOf(ctype) != null) {
                        checkAgainst = CheckAgainstEnum.valueOf(ctype);
                    } else {
                        System.out.println("Unknown option value: " + cmdCheckOnly.getOptionValue("check-type"));
                        System.exit(ERROR_EXIT_CODE);
                    }
                }*/
                
                /*System.out.println(String.format(INPUT_LOG, XML_INPUT, fXMLin));                
                System.out.println();*/

                
                if (!validator.check()) {
                    System.exit(ERROR_EXIT_CODE);
                }
                
                
                
                /*try {
                    mn2sts app = new mn2sts();

                    app.checkSTS(fXMLin);
                    
                    System.out.println("End!");
                
                } catch (SAXParseException e) {
                    System.err.println(e.toString());
                    System.exit(ERROR_EXIT_CODE);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    System.exit(ERROR_EXIT_CODE);
                }*/
                
                
                
                cmdFail = false;
            } catch (ParseException exp) {
                cmdFail = true;
            }
        }        
        
        if(cmdFail) {            
            try {             
                CommandLine cmd = parser.parse(options, args);

                System.out.print("mn2sts ");
                printVersion(cmd.hasOption("version"));
                System.out.println("\n");

                XsltConverter converter = new XsltConverter();
                converter.setInputFilePath(cmd.getOptionValue("xml-file-in"));
                
                /*final String argXMLin = cmd.getOptionValue("xml-file-in");
                File fXMLin = new File(argXMLin);
                if (!fXMLin.exists()) {
                    System.out.println(String.format(INPUT_NOT_FOUND, XML_INPUT, fXMLin));
                    System.exit(ERROR_EXIT_CODE);
                }*/

                /*final String argXSL = cmd.getOptionValue("xsl-file");
                File fXSL = null;
                if (argXSL != null) {
                    fXSL = new File(argXSL);
                    if (!fXSL.exists()) {
                        System.out.println(String.format(INPUT_NOT_FOUND, XSL_INPUT, fXSL));
                        System.exit(ERROR_EXIT_CODE);
                    }
                }*/
                
                converter.setInputXslPath(cmd.getOptionValue("xsl-file"));
                

                /*final String argXMLout = cmd.getOptionValue("xml-file-out");
                File fXMLout = new File(argXMLout);*/
                
                converter.setOutputFilePath(cmd.getOptionValue("xml-file-out"));
                

                //DEBUG = cmd.hasOption("debug");
                
                converter.setDebugMode(cmd.hasOption("debug"));

                /*if (cmd.hasOption("check-type")) {
                    String ctype = cmd.getOptionValue("check-type");  
                    ctype = ctype.replace("-", "_").toUpperCase();                    
                    try {
                        checkAgainst = CheckAgainstEnum.valueOf(ctype);
                    } catch (Exception ex) {
                        System.out.println("Unknown option value: " + cmd.getOptionValue("check-type"));
                        System.exit(ERROR_EXIT_CODE);
                    }
                }*/
                
                converter.setCheckType(cmd.getOptionValue("check-type"));
                
                

                /*if (cmd.hasOption("output-format")) {
                    String outputtype = cmd.getOptionValue("output-format");  
                    outputtype = outputtype.toUpperCase();                    
                    try {
                        outputFormat = OutputFormatEnum.valueOf(outputtype);
                    } catch (Exception ex) {
                        System.out.println("Unknown option value: " + cmd.getOptionValue("output-format"));
                        System.exit(ERROR_EXIT_CODE);
                    }
                }*/
                
                converter.setOutputFormat(cmd.getOptionValue("output-format"));
                //System.out.println(String.format(INPUT_LOG, XML_INPUT, fXMLin));
                
                /*if (fXSL != null) {
                    System.out.println(String.format(INPUT_LOG, XSL_INPUT, fXSL));
                }
                System.out.println(String.format(OUTPUT_LOG, XML_OUTPUT, fXMLout, outputFormat));
                System.out.println();*/

                /*try {
                    mn2sts app = new mn2sts();

                    app.convertmn2sts(fXMLin, fXSL, fXMLout);
                    System.out.println("End!");
                } catch (SAXParseException e) {
                    System.err.println(e.toString());
                    System.exit(ERROR_EXIT_CODE);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    System.exit(ERROR_EXIT_CODE);
                }*/
                
                boolean result = converter.process();
                
                if (!result) {
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


    private static void printVersion(boolean print) {
        if (print) {            
            System.out.println(VER);
        }
    }       

}