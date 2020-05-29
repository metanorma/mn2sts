package com.metanorma.validator;

import com.metanorma.ResourceResolver;
import com.metanorma.Util;
import java.io.File;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

public class XSDValidator extends Validator {

    public XSDValidator(File xml) {
        super(xml);
    }

    public List<String> validate(CheckAgainstEnum checkAgainst) {
        this.checkAgainst = checkAgainst;
        
        String checkSrc = CheckAgainstMap.getMap().get(checkAgainst);
                
        System.out.println("Validate XML againts XSD " + checkSrc + "...");
        
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        // associate the schema factory with the resource resolver, which is responsible for resolving the imported XSD's
        schemaFactory.setResourceResolver(new ResourceResolver(new File(checkSrc).getParentFile().toString() + "/"));
              
        try {
            Source schemaFile = new StreamSource(Util.getStreamFromResources(getClass().getClassLoader(), checkSrc));                
            Schema schema = schemaFactory.newSchema(schemaFile);
            javax.xml.validation.Validator validator = schema.newValidator();
            validator.setErrorHandler(errorHandler);
            validator.validate(new StreamSource(xml));            
        } catch (Exception e) { 
            exceptions.add(e.toString());            
        }        
        return exceptions;
    }
    
}
