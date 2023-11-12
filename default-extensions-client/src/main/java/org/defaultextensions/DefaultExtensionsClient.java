package org.defaultextensions;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.defaultextensions.checkstyle.ValidateCheckstyeChecks;
import org.defaultextensions.pmd.ValidatePmdRuleSet;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class DefaultExtensionsClient {
    
    public static final String RULE_CATEGORY_SEPERATOR = """
		          <!-- ========================================================================================================================= -->
		          <!-- === {#} === -->
		          <!-- ========================================================================================================================= -->
		   				""";

    public static String getFileProperties() {
	
	final var path = DefaultExtensionsClient.class
					.getProtectionDomain()
					.getCodeSource()
					.getLocation().getPath();
	    
	    final var pathJarFolder = Path.of(path).getParent();
	    
	    final var pathFile = pathJarFolder.resolve("application.properties");
	    
	    return Files.exists(pathFile) 
			    ? pathFile.toString() 
                            : "application.properties";
	
    }

    public static String prettyPrintByDom4j(final String xmlString) {
	
	final var format = OutputFormat.createPrettyPrint();
	format.setIndentSize(5);
	format.setSuppressDeclaration(false);
	format.setEncoding("UTF-8");
	
	try {
	    final var document = DocumentHelper.parseText(xmlString);

	    final var sw = new StringWriter();
	    
	    try (sw) {
		final var writer = new XMLWriter(sw, format);
		writer.write(document);
		return sw.toString();
	    }
	    
	} catch (final DocumentException | IOException ex) {
	    throw new IllegalStateException("Error occurs when pretty-printing xml:\n" + xmlString, ex);
	}
    }

    public static void main(final String... args) {
	final var validatePmdRuleSet = new ValidatePmdRuleSet();
	validatePmdRuleSet.execute();
	
	final var validateCheckstyeChecks = new ValidateCheckstyeChecks();
	validateCheckstyeChecks.execute();
    }
}
