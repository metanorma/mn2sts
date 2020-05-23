package com.metanorma;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.text.MessageFormat;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xml.sax.SAXException;

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
    static final String OUTPUT_LOG = "Output: %s (%s)";

    static boolean DEBUG = false;

    static final Options optionsInfo = new Options() {
        {
            addOption(Option.builder("v")
                    .longOpt("version")
                    .desc("display application version")
                    .required(true)
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
                    .desc("path to XSL file")
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
        }
    };

    static final String USAGE = getUsage();

    static final int ERROR_EXIT_CODE = -1;

    static final String TMPDIR = System.getProperty("java.io.tmpdir");

    final Path tmpfilepath = Paths.get(TMPDIR, UUID.randomUUID().toString());

    /**
     * Converts an MN XML file to ISO STS XML file
     *
     * @param xmlin the XML source file
     * @param xsl the external XSL file
     * @param xmlout the XML output file
     * @throws IOException In case of an I/O problem
     */
    public void convertmn2sts(File xmlin, File xsl, File xmlout) throws IOException, TransformerException {

        try {
            OutputJaxpImplementationInfo();

            Source srcXSL = null;
            if (xsl != null) { //external xsl
                srcXSL = new StreamSource(xsl);
            } else { // internal xsl
                srcXSL = new StreamSource(getStreamFromResources("mn2sts.xsl"));
            }

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(srcXSL);
            transformer.setParameter("debug", DEBUG);

            Source src = new StreamSource(xmlin);
            StringWriter resultWriter = new StringWriter();
            StreamResult sr = new StreamResult(resultWriter);
            System.out.println("Transforming...");
            transformer.transform(src, sr);
            String xmlSTS = resultWriter.toString();

            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(xmlout.getAbsolutePath()))) {
                writer.write(xmlSTS);
            }

            System.out.println("Validate XML againts XSD...");
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // associate the schema factory with the resource resolver, which is responsible for resolving the imported XSD's
            schemaFactory.setResourceResolver(new ResourceResolver("xsd/"));
            
            try {
                Source schemaFile = new StreamSource(getStreamFromResources("xsd/NISO-STS-extended-1-mathml3.xsd"));
                
                Schema schema = schemaFactory.newSchema(schemaFile);
                
                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(xmlout));                
                System.out.println(xmlout.getAbsolutePath() + " is valid.");
            } catch (SAXException | IOException e) {                
                System.out.println(xmlout.getAbsolutePath() + " is NOT valid reason:" + e);                
            }

        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(ERROR_EXIT_CODE);
        }
    }

    /**
     * Main method.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) throws ParseException {

        CommandLineParser parser = new DefaultParser();

        String ver = Util.getAppVersion();
        
        try {

            try {
                CommandLine cmdInfo = parser.parse(optionsInfo, args);
                if (cmdInfo.hasOption("version")) {
                    System.out.println(ver);
                }
            } catch (ParseException exp) {

                CommandLine cmd = parser.parse(options, args);

                System.out.println("mn2sts ");
                if (cmd.hasOption("version")) {
                    System.out.println(ver);
                }
                System.out.println("\n");

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

                System.out.println(String.format(INPUT_LOG, XML_INPUT, fXMLin));
                if (fXSL != null) {
                    System.out.println(String.format(INPUT_LOG, XSL_INPUT, fXSL));
                }
                System.out.println(String.format(OUTPUT_LOG, XML_OUTPUT, fXMLout));
                System.out.println();

                try {
                    mn2sts app = new mn2sts();

                    app.convertmn2sts(fXMLin, fXSL, fXMLout);
                    System.out.println("End!");
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    System.exit(ERROR_EXIT_CODE);
                }
            }
        } catch (ParseException exp) {
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

    // get file from classpath, resources folder
    private InputStream getStreamFromResources(String fileName) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream stream = classLoader.getResourceAsStream(fileName);
        if (stream == null) {
            throw new Exception("Cannot get resource \"" + fileName + "\" from Jar file.");
        }
        return stream;
    }
}
