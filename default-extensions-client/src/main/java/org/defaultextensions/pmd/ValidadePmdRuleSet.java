package org.defaultextensions.pmd;

import static com.gargoylesoftware.htmlunit.BrowserVersion.FIREFOX_ESR;
import static java.io.File.separator;
import static org.apache.commons.lang3.StringUtils.LF;
import static org.apache.commons.lang3.StringUtils.containsAny;
import static org.apache.commons.lang3.StringUtils.containsAnyIgnoreCase;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringsBetween;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.jcabi.xml.XMLDocument;

public class ValidadePmdRuleSet {

    private static final String BEST_PRACTICE_KEY = "BEST PRACTICES";
    private static final String CODE_STYLE_KEY = "CODE STYLE";
    private static final String DESIGN_KEY = "DESIGN";
    private static final String DOCUMENTATION_KEY = "DOCUMENTATION";
    private static final String ERROR_PRONE_KEY = "ERROR PRONE";
    private static final String MULTITHREADING_KEY = "MULTITHREADING";
    private static final String PERFORMANCE_KEY = "PERFORMANCE";
    private static final String SECURITY_KEY = "SECURITY";

    private static final String PMD_VERSION = "6.52.0";

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidadePmdRuleSet.class);

    private static final String HEAD_FILE = """
    		<?xml version="1.0" encoding="UTF-8"?>
    		<ruleset name="default-pmd-rules"
    		     xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
    		     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    		     xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0 https://pmd.sourceforge.io/ruleset_2_0_0.xsd">

    			<description>PMD Default Extension Rule Set</description>
    			 
    			""";

    private static final String FOOT_FILE = "</ruleset>	";

    private static final String FILE_LOCATION = "/home/fernando/Development/workspaces/eclipse-workspace/default-extensions/default-extensions-files/src/main/resources/default-extensions/pmd/java";

    private static final String RULE_CATEGORY_SEPERATOR = """
    		          <!-- ================================================================================================================================================== -->
    		          <!-- ======== {#} ========================================================================================================================== -->
    		          <!-- ================================================================================================================================================== -->
    		   				""";

    public static Map<String, List<String>> fetchRulesOnline() {

	LOGGER.info("Start to fetch rules from online");

	final var webClient = new WebClient(FIREFOX_ESR);
	final var options = webClient.getOptions();
	options.setJavaScriptEnabled(false);
	options.setCssEnabled(false);

	final var urlBase = "https://pmd.sourceforge.io/pmd-";

	final var rulesCategories = List.of( //
			Map.entry(BEST_PRACTICE_KEY, urlBase.concat(PMD_VERSION).concat("/pmd_rules_java_bestpractices.html")), // best practices
			Map.entry(CODE_STYLE_KEY, urlBase.concat(PMD_VERSION).concat("/pmd_rules_java_codestyle.html")), // code style
			Map.entry(DESIGN_KEY, urlBase.concat(PMD_VERSION).concat("/pmd_rules_java_design.html")), // design
			Map.entry(DOCUMENTATION_KEY, urlBase.concat(PMD_VERSION).concat("/pmd_rules_java_documentation.html")), // documentation
			Map.entry(ERROR_PRONE_KEY, urlBase.concat(PMD_VERSION).concat("/pmd_rules_java_errorprone.html")), // error prone
			Map.entry(MULTITHREADING_KEY, urlBase.concat(PMD_VERSION).concat("/pmd_rules_java_multithreading.html")), // multi threading
			Map.entry(PERFORMANCE_KEY, urlBase.concat(PMD_VERSION).concat("/pmd_rules_java_performance.html")), // perfomance
			Map.entry(SECURITY_KEY, urlBase.concat(PMD_VERSION).concat("/pmd_rules_java_security.html")) // security
	);

	final var filters = new String[] { //
		"This rule is replaced".toLowerCase(), //
		"This rule is deprecated since".toLowerCase(), //
		"This rule has been deprecated".toLowerCase(), //
		"this rule is deprecated".toLowerCase() //
	};

	final var mapRules = new HashMap<String, List<String>>();

	try (webClient) {

	    for (final var rule : rulesCategories) {

		final var rules = new ArrayList<String>();

		final var text = webClient //
				.<HtmlPage>getPage(rule.getValue()) //
				.asNormalizedText();

		Stream.of(substringsBetween(text, "Since: PMD", "/>")) //
				.filter(s -> !containsAny(s.toLowerCase(), filters)) //
				.map(s -> "<rule " + trim(substringAfter(s, "<rule")) + " />") //
				.forEach(rules::add);

		LOGGER.debug("Key {}, qtRules {}", rule.getKey(), rules.size());
		
		mapRules.put(rule.getKey(), rules);
	    }

	} catch (final FailingHttpStatusCodeException | IOException ex) {
	    throw new IllegalStateException(getRootCauseMessage(ex));
	}

	LOGGER.info("Finished fetch rules on lines");

	return mapRules;
    }

    public static List<String> readRulesFile() {
	
	LOGGER.info("Start to read rules from file");
	
	final var file = FILE_LOCATION.concat(separator).concat("pmd-ruleset-").concat(PMD_VERSION).concat(".xml");
	
	final var certificationPath = Paths.get(file);
	
	final var result = new ArrayList<String>();
	
	try  {
	    final var xmlString = Files.readString(certificationPath);
	    
	    final var xml = new XMLDocument(xmlString);
	    
	    final var rules = xml.node()
			    .getChildNodes()
			    .item(0)
			    .getChildNodes();

	    for (var i=0; i < rules.getLength(); i++) {
		
		final var element = rules.item(i);
		final var str = new XMLDocument(element).toString().trim();
		
		if (containsIgnoreCase(str, "<rule")) {
		    final var strFinal = str.replace("xmlns=\"http://pmd.sourceforge.net/ruleset/2.0.0\"", "");
		    result.add(strFinal);
		    LOGGER.debug("{}", strFinal);
		}
	    }
	
	    LOGGER.info("Finished read rules from file");

	    return result;
	} catch (final IOException ex) {
	    throw new IllegalStateException(ex);
	}
    }
    
    public static void writeRules(final Map<String, List<String>> onlineRules, final List<String> fileRules) {
	LOGGER.info("Start to write rules to file");
	
	final var text = new StringBuilder(HEAD_FILE);
	
	for (final var entry : onlineRules.entrySet()) {
	    final var key = entry.getKey();
	    final var rules = entry.getValue();
	    
	    text.append(RULE_CATEGORY_SEPERATOR.replace("{#}", key));
	    text.append(LF);
	    
	    for (final var rule : rules) {
		
		final var ruleFinal = StringUtils.substringBetween(rule, "rule ref=\"", "\"");
		
		final var optionalRule = fileRules.stream()
						.filter(fileRule -> containsAnyIgnoreCase(fileRule, ruleFinal))
						.findAny();
		
		if (optionalRule.isPresent()) { 
		    final var valueFile = optionalRule.get().trim();
		    
		    text.append(valueFile.replaceAll("(?m)^[ \t]*\r?\n", ""))
				    .append(LF)
				    .append(LF);
		    
		} else {
		    text.append(rule.trim())
		    		   .append(LF)
		    		   .append(LF);
		}
	    }
	    
	    text.append(LF);
	}
	
	text.append(FOOT_FILE);
	
	final var file = FILE_LOCATION.concat(separator).concat("pmd-ruleset-").concat(PMD_VERSION).concat("-new").concat(".xml");
	
	try (final var writer = new BufferedWriter(new FileWriter(file)))  {
	    
	    writer.write(text.toString());
	    
	} catch (final IOException ex) {
	    throw new IllegalStateException(ex);
	}
	
	LOGGER.info("Finished to write rules to file");
    }

    public static void main(String... args) {
	final var onlineRules = fetchRulesOnline();
	
	final var fileRules = readRulesFile();

	writeRules(onlineRules, fileRules);
    }
}
