package org.defaultextensions.pmd;

import static com.gargoylesoftware.htmlunit.BrowserVersion.FIREFOX_ESR;
import static java.io.File.separator;
import static java.nio.file.Files.notExists;
import static java.util.Map.entry;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang3.StringUtils.LF;
import static org.apache.commons.lang3.StringUtils.containsAny;
import static org.apache.commons.lang3.StringUtils.containsAnyIgnoreCase;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringsBetween;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.defaultextensions.DefaultExtensionsClient.RULE_CATEGORY_SEPERATOR;
import static org.defaultextensions.DefaultExtensionsClient.getFileProperties;

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

import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.jcabi.xml.XMLDocument;

public class ValidatePmdRuleSet {
    
    private static final String BEST_PRACTICE_KEY = "BEST PRACTICES";
    private static final String CODE_STYLE_KEY = "CODE STYLE";
    private static final String DESIGN_KEY = "DESIGN";
    private static final String DOCUMENTATION_KEY = "DOCUMENTATION";
    private static final String ERROR_PRONE_KEY = "ERROR PRONE";
    private static final String MULTITHREADING_KEY = "MULTITHREADING";
    private static final String PERFORMANCE_KEY = "PERFORMANCE";
    private static final String SECURITY_KEY = "SECURITY";

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidatePmdRuleSet.class);

    private static final String HEAD_FILE = """
    		<?xml version="1.0" encoding="UTF-8"?>
    		<ruleset name="default-pmd-rules"
    		     xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
    		     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    		     xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0 https://pmd.sourceforge.io/ruleset_2_0_0.xsd">

    			<description>PMD Default Extension Rule Set</description>
    			 
    			""";

    private static final String FOOT_FILE = "</ruleset>	";

    private Map<String, String> readPmdProperties() {
	
	final var configs = new Configurations();
	
	try {
	    
	    final var propertiesFile = getFileProperties();
	    
	    final var properties = configs.properties(propertiesFile);
	    
	    return Map.of( //
			    "pathFile", properties.getString("pmd.rules.file.path"), //
			    "version", properties.getString("pmd.version"), //
			    "pathFolder", properties.getString("pmd.folder") //
	    );
	    
	} catch (final ConfigurationException ex) {
	    throw new IllegalStateException(getRootCauseMessage(ex));
	}
    }
    
    private Map<String, List<String>> fetchRulesOnline(final String version) {
	
	LOGGER.info("Start to fetch rules from online with {} version", version);

	final var webClient = new WebClient(FIREFOX_ESR);
	final var options = webClient.getOptions();
	options.setJavaScriptEnabled(false);
	options.setCssEnabled(false);

	final var urlBase = "https://pmd.sourceforge.io/pmd-";

	final var rulesCategories = List.of( //
			entry(BEST_PRACTICE_KEY, urlBase.concat(version).concat("/pmd_rules_java_bestpractices.html")), // best practices
			entry(CODE_STYLE_KEY, urlBase.concat(version).concat("/pmd_rules_java_codestyle.html")), // code style
			entry(DESIGN_KEY, urlBase.concat(version).concat("/pmd_rules_java_design.html")), // design
			entry(DOCUMENTATION_KEY, urlBase.concat(version).concat("/pmd_rules_java_documentation.html")), // documentation
			entry(ERROR_PRONE_KEY, urlBase.concat(version).concat("/pmd_rules_java_errorprone.html")), // error prone
			entry(MULTITHREADING_KEY, urlBase.concat(version).concat("/pmd_rules_java_multithreading.html")), // multi threading
			entry(PERFORMANCE_KEY, urlBase.concat(version).concat("/pmd_rules_java_performance.html")), // perfomance
			entry(SECURITY_KEY, urlBase.concat(version).concat("/pmd_rules_java_security.html")) // security
	);

	final var filters = new String[] { //
		"This rule is replaced".toLowerCase(), //
		"This rule is deprecated since".toLowerCase(), //
		"This rule has been deprecated".toLowerCase(), //
		"this rule is deprecated".toLowerCase() //
	};

	final var mapRules = new HashMap<String, List<String>>();

	final var watch = new StopWatch();
	
	try (webClient) {
	    
	    watch.start();

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

	watch.stop();
	LOGGER.info("Finished fetch rules on lines with {} ms", watch.getTime(MILLISECONDS));
	watch.reset();
	
	return mapRules;
    }

    private List<String> readRulesFile(final String pathFile) {
	
	LOGGER.info("Start to read rules from file {}", pathFile);
	
	final var certificationPath = Paths.get(pathFile);
	
	if (notExists(certificationPath)) {
	    return List.of();
	}
	
	final var result = new ArrayList<String>();
	
	final var watch = new StopWatch();
	watch.start();
	
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
	
	    watch.stop();
	    LOGGER.info("Finished read rules from file with {} ms", watch.getTime(MILLISECONDS));
	    watch.reset();
	    
	    return result;
	} catch (final IOException ex) {
	    throw new IllegalStateException(ex);
	}
    }
    
    private void writeRules(final Map<String, List<String>> onlineRules, final List<String> fileRules, final String version, final String pathFolder) {
	LOGGER.info("Start to write rules to file");
	
	final var text = new StringBuilder(HEAD_FILE);
	
	final var watch = new StopWatch();
	watch.start();
	
	for (final var entry : onlineRules.entrySet()) {
	    final var key = entry.getKey();
	    final var rules = entry.getValue();
	    
	    text.append(replace(RULE_CATEGORY_SEPERATOR, "{#}", key));
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
	
	final var file = pathFolder.concat(separator).concat("pmd-ruleset-").concat(version).concat("-new").concat(".xml");
	
	try (final var writer = new BufferedWriter(new FileWriter(file)))  {
	    
	    writer.write(text.toString());
	    
	} catch (final IOException ex) {
	    throw new IllegalStateException(ex);
	}
	
	watch.stop();
	LOGGER.info("Finished to write rules to file with {} ms", watch.getTime(MILLISECONDS));
	watch.reset();
    }

    public void execute() {
	
	final var properties = readPmdProperties();
	
	final var version = properties.get("version");
	
	final var pathFile = properties.get("pathFile");
	
	final var pathFolder = properties.get("pathFolder");
	
	final var onlineRules = fetchRulesOnline(version);
	
	final var fileRules = readRulesFile(pathFile);

	writeRules(onlineRules, fileRules, version, pathFolder);
    }
}
