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
                
                if (!validator.check()) {
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
                printVersion(cmd.hasOption("version"));
                System.out.println("\n");

                XsltConverter converter = new XsltConverter();
                converter.setInputFilePath(cmd.getOptionValue("xml-file-in"));
                converter.setInputXslPath(cmd.getOptionValue("xsl-file"));
                converter.setOutputFilePath(cmd.getOptionValue("xml-file-out"));
                converter.setDebugMode(cmd.hasOption("debug"));
                converter.setCheckType(cmd.getOptionValue("check-type"));
                converter.setOutputFormat(cmd.getOptionValue("output-format"));

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