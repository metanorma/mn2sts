package com.metanorma;

import static com.metanorma.Constants.*;
import com.metanorma.utils.LoggerHelper;
import com.metanorma.validator.CheckAgainstEnum;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathFactory;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Alexander Dyuzhev
 */

/**
 * This class for the conversion of an Metanorma XML ro NISO/ISO XML file
 */
public class XsltConverter {

    private static final Logger logger = Logger.getLogger(LoggerHelper.LOGGER_NAME);
    
    private String inputFilePath = "document.xml"; // default input file name
    //private boolean isInputFileRemote = false; // true, if inputFilePath starts from http, https, www.
    
    private String inputXslPath = null; // default xsl is null (will be used from jar)
    private File fileXSL = null;
    
    
    private String outputFilePath = ""; // default output file name
    
    private String checkType = "xsd_niso";
    private CheckAgainstEnum checkTypeEnumValue = CheckAgainstEnum.XSD_NISO; 
    
    private String outputFormat = "NISO";
    private OutputFormatEnum outputFormatEnumValue = OutputFormatEnum.NISO;
    
    private boolean isDebugMode = false; // default no debug
    
    public XsltConverter() {
        
    }

    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }


    public void setInputXslPath(String inputXslPath) {
        this.inputXslPath = inputXslPath;
    }

    public void setOutputFilePath(String outputFilePath) {
        if (outputFilePath != null) {
            this.outputFilePath = outputFilePath;
        }
    }
    
    public void setDebugMode(boolean isDebugMode) {
        this.isDebugMode = isDebugMode;
    }

    
    public void setCheckType(String checkType) {
        if (checkType != null) {
            this.checkType = checkType;
        }
    }

    public void setOutputFormat(String outputFormat) {
        if (outputFormat != null) {
            this.outputFormat = outputFormat;
        }
    }

   
    
    
    public boolean process() {
        try {
            
        
            /*if (inputFilePath.toLowerCase().startsWith("http") || inputFilePath.toLowerCase().startsWith("www.")) {
                isInputFileRemote = true;
                inputFilePath = Util.downloadFile(inputFilePath, tmpfilepath);
                if (inputFilePath.isEmpty()) {
                    return false;
                }
            }*/
            
            if (!new File(inputFilePath).exists()) {
                //System.out.println(String.format(INPUT_NOT_FOUND, XML_INPUT, fXMLin));
                logger.severe(String.format(INPUT_NOT_FOUND, XML_INPUT, inputFilePath));
                return false;
            }
            
            if (inputXslPath != null) {
                fileXSL = new File(inputXslPath);
                if (!fileXSL.exists()) {
                    //System.out.println(String.format(INPUT_NOT_FOUND, XSL_INPUT, fXSL));
                    logger.severe(String.format(INPUT_NOT_FOUND, XSL_INPUT, fileXSL));
                    return false;
                }
            }
            
            
            String ctype = checkType.replace("-", "_").toUpperCase();
            if (CheckAgainstEnum.valueOf(ctype) != null) {
                checkTypeEnumValue = CheckAgainstEnum.valueOf(ctype);
            } else {
                //System.out.println("Unknown option value: " + checkType);
                logger.log(Level.SEVERE, "Unknown option value:  {0}", checkType);
                return false;
            }
            
            
            try {
                outputFormatEnumValue = OutputFormatEnum.valueOf(outputFormat.toUpperCase());
            } catch (Exception ex) {
                //System.out.println("Unknown option value: " + outputFormat);
                logger.log(Level.SEVERE, "Unknown option value:  {0}", outputFormat);
                return false;
            }
            
            logger.info(String.format(INPUT_LOG, XML_INPUT, inputFilePath));
            if (fileXSL != null) {
                logger.info(String.format(INPUT_LOG, XSL_INPUT, fileXSL));
            }
            logger.info(String.format(OUTPUT_LOG, XML_OUTPUT, outputFilePath, outputFormat.toUpperCase()));
            logger.info("");
            
            convertmn2sts();
            
            // check agains XSD NISO, DTD NISO or DTD ISO
            STSValidator validator = new STSValidator(outputFilePath, checkType);
            if (!validator.check()) {
                return false;
            }
            
            logger.info("End!");
        
        
        } catch (Exception e) {
            //e.printStackTrace(System.err);
            logger.severe(e.toString());
            return false;
        }
        return true;
    }

    /**
     * Converts an MN XML file to NISO STS XML file
     *
     * @param xmlin the XML source file
     * @param xsl the external XSL file
     * @param xmlout the XML output file
     * @throws IOException In case of an I/O problem
     * @throws javax.xml.transform.TransformerException
     */
    private void convertmn2sts() throws IOException, TransformerException, SAXParseException, Exception {
       
        OutputJaxpImplementationInfo();

        Source srcXSL;
        if (fileXSL != null) { //external xsl
            srcXSL = new StreamSource(fileXSL);
        } else { // internal xsl
            srcXSL = new StreamSource(Util.getStreamFromResources(getClass().getClassLoader(), "mn2sts.xsl"));
        }

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(srcXSL);
        transformer.setParameter("debug", isDebugMode);
        transformer.setParameter("outputformat", outputFormat.toUpperCase());

        File fXmlIn = new File(inputFilePath);
        Source src = new StreamSource(fXmlIn);
        StringWriter resultWriter = new StringWriter();
        StreamResult sr = new StreamResult(resultWriter);
        //System.out.println("Transforming...");
        logger.info("Transforming...");
        transformer.transform(src, sr);
        String xmlSTS = resultWriter.toString();

        File fXmlOut = new File(outputFilePath);
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fXmlOut.getAbsolutePath()))) {
            writer.write(xmlSTS);
        }

    }
    
    private void OutputJaxpImplementationInfo() {
        if (isDebugMode) {
            logger.info(getJaxpImplementationInfo("DocumentBuilderFactory", DocumentBuilderFactory.newInstance().getClass()));
            logger.info(getJaxpImplementationInfo("XPathFactory", XPathFactory.newInstance().getClass()));
            logger.info(getJaxpImplementationInfo("TransformerFactory", TransformerFactory.newInstance().getClass()));
            logger.info(getJaxpImplementationInfo("SAXParserFactory", SAXParserFactory.newInstance().getClass()));
        }
    }
    
    private String getJaxpImplementationInfo(String componentName, Class componentClass) {
        CodeSource source = componentClass.getProtectionDomain().getCodeSource();
        return MessageFormat.format(
                "{0} implementation: {1} loaded from: {2}",
                componentName,
                componentClass.getName(),
                source == null ? "Java Runtime" : source.getLocation());
    }    
}
