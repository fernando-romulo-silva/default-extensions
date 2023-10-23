package org.defaultextensions;

import java.nio.file.Files;
import java.nio.file.Path;

import org.defaultextensions.pmd.ValidatePmdRuleSet;

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

    public static void main(final String... args) {
	final var validatePmdRuleSet = new ValidatePmdRuleSet();
	validatePmdRuleSet.execute();
    }
}
