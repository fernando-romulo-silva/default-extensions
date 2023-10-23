package org.defaultextensions.checkstyle;

import static java.io.File.separator;
import static java.nio.file.Files.notExists;
import static java.util.Map.entry;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang3.StringUtils.LF;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.defaultextensions.DefaultExtensionsClient.RULE_CATEGORY_SEPERATOR;
import static org.defaultextensions.DefaultExtensionsClient.getFileProperties;
import static org.defaultextensions.checkstyle.ElementType.MODULE;
import static org.defaultextensions.checkstyle.ElementType.PROPERTY;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.xml.XMLDocument;

/**
 * 
 * 
 * @author Fernando Romulo da Silva
 */
public class ValidateCheckstyeChecks {

    private static final String MODULE_NAME_CHECKER_BEGIN = "<module name=\"Checker\">";
    private static final String MODULE_NAME_CHECKER_END = "</module>";

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateCheckstyeChecks.class);

    private static final String WHITE_SPACES_KEY = "WHITE SPACES";
    private static final String SIZES_KEY = "SIZES";
    private static final String REGEXP_KEY = "REGEXP";
    private static final String NAMING_KEY = "NAMING";
    private static final String MODIFIER_KEY = "MODIFIER";
    private static final String MISC_KEY = "MISC";
    private static final String METRICS_KEY = "METRICS";
    private static final String JAVADOC_KEY = "JAVADOC";
    private static final String IMPORTS_KEY = "IMPORTS";
    private static final String HEADER_KEY = "HEADER";
    private static final String CODING_KEY = "CODING";
    private static final String DESIGN_KEY = "DESIGN";
    private static final String BLOCKS_KEY = "BLOCKS";
    private static final String ANNOTATIONS_KEY = "ANNOTATIONS";
    private static final String PROPERTIES_KEY = "PROPERTIES";
    

    private static final String HEAD_FILE = """
    		<?xml version="1.0"?>
    		<!DOCTYPE module PUBLIC
    		          "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
    		          "https://checkstyle.org/dtds/configuration_1_3.dtd">
    		<module name="Checker">

    		        """;
    private static final String FOOT_FILE = "</module>	";

    private Map<String, String> readCheckStyleProperties() {
	
	final var configs = new Configurations();
	
	try {
	    
	    final var propertiesFile = getFileProperties();
	    
	    final var properties = configs.properties(propertiesFile);
	    
	    return Map.of( //
			    "pathFile", properties.getString("checkstyle.checks.file.path"), //
			    "version", properties.getString("checkstyle.version"), //
			    "pathFolder", properties.getString("checkstyle.folder") //
	    );
	    
	} catch (final ConfigurationException ex) {
	    throw new IllegalStateException(getRootCauseMessage(ex));
	}
    }

    private List<Element> fetchOnlineChecks(final String version) {
	
	LOGGER.info("Start to fetch checks from online with {} version", version);

	final var url = "https://checkstyle.sourceforge.io/checks";

	final var ruleGroupsUrls = List.of( //
//			entry(ANNOTATIONS_KEY, url.concat("/annotation/index.html")), //
//			entry(BLOCKS_KEY, url.concat("/blocks/index.html")), //
//			entry(DESIGN_KEY, url.concat("/design/index.html")), //
//			entry(CODING_KEY, url.concat("/coding/index.html")), //
//			entry(HEADER_KEY, url.concat("/header/index.html")), //
//			entry(IMPORTS_KEY, url.concat("/imports/index.html")), //
//			entry(JAVADOC_KEY, url.concat("/javadoc/index.html")), //
//			entry(METRICS_KEY, url.concat("/metrics/index.html")), //
//			entry(MISC_KEY, url.concat("/misc/index.html")), //
//			entry(MODIFIER_KEY, url.concat("/modifier/index.html")), //
//			entry(NAMING_KEY, url.concat("/naming/index.html")), //
//			entry(REGEXP_KEY, url.concat("/regexp/index.html")), //
//			entry(SIZES_KEY, url.concat("/sizes/index.html")), //
			entry(WHITE_SPACES_KEY, url.concat("/whitespace/index.html")) //
	);

	final var elements = new ArrayList<Element>();
	
	final var watch = new StopWatch();

	try {
	    
	    watch.start();
	    
	    for (final var entry : ruleGroupsUrls) {

		final var key = entry.getKey();
		final var groupUrl = entry.getValue();

		final var timeoutMillis = 100000;
		
		final var document = Jsoup.parse(new URI(groupUrl).toURL(), timeoutMillis);

		final var tds = document.select("td[align=left]");

		final var hyperlinks = tds.select("a[href]");

		for (final var hyperlink : hyperlinks) {
		    final var href = hyperlink.attr("href");
		    final var name = StringUtils.substringAfter(href, "#");

		    final var internalUrl = groupUrl.replace("index.html", href);
		    final var internalDocument = Jsoup.parse(new URI(internalUrl).toURL(), timeoutMillis);

		    final var parent = internalDocument.select("section[id=Parent_Module]")
				    	.select("a")
				    	.text();
		    
		    final var source = internalDocument.selectFirst("section[id=Examples]")
				    .selectFirst("div[class=source]")
				    .select("pre").text();
		    
		    final var value = containsIgnoreCase(source, "Checker") 
				    ? substringBetween(source, MODULE_NAME_CHECKER_BEGIN, MODULE_NAME_CHECKER_END) 
			            : source;

		    
		    elements.add(new Element(key, name, value, parent, MODULE));
		}
		
		LOGGER.debug("Key {}, qtChecks {}", key, elements.size());
	    }

	} catch (final IOException | URISyntaxException ex) {
	    throw new IllegalStateException(getRootCauseMessage(ex));
	}

	watch.stop();
	LOGGER.info("Finished fetch check with {} ms", watch.getTime(MILLISECONDS));
	watch.reset();
	
	return elements;
    }
    
    private List<Element> readElementsFile(final String pathFile) {
	
	LOGGER.info("Start to read checks from file {}", pathFile);
	
	final var certificationPath = Paths.get(pathFile);
	
	if (notExists(certificationPath)) {
	    LOGGER.info("Finished read checks, no file");
	    return List.of();
	}
	
	final var result = new ArrayList<Element>();
	
	final var watch = new StopWatch();
	watch.start();
	
	try {
	    
	    final var xmlString = Files.readString(certificationPath);
	    
	    final var xml = new XMLDocument(xmlString);
	    
	    final var preChecks = xml.node()
			    .getChildNodes()
	    		    .item(1);
	    
	    final var xmlDocument = new XMLDocument(preChecks);
	    
	    final var rootProperties = xmlDocument.nodes("/module[@name='Checker']/property");
	    for (final var rootProperty : rootProperties) {
		result.add(new Element(
				substringBetween(rootProperty.toString(), "name=\"", "\""), 
				rootProperty.toString(), 
				"Checker", 
				PROPERTY
			));  
	    }
	    
	    final var rootModules = xmlDocument.nodes("/module[@name='Checker']/module[@name!='TreeWalker']");
	    for (var rootModule : rootModules) {
		
		result.add(new Element(
				substringBetween(rootModule.toString(), "name=\"", "\""), 
				rootModule.toString(), 
				"Checker", 
				MODULE
			));
	    }
	    
	    
	    final var treeWalkModules = xmlDocument.nodes("/module[@name='Checker']/module[@name='TreeWalker']/module");
	    for (var treeWalkModule : treeWalkModules) {
		
		result.add(new Element(
				substringBetween(treeWalkModule.toString(), "name=\"", "\""), 
				treeWalkModule.toString(), 
				"TreeWalker", 
				MODULE
			));
	    }
	
	    watch.stop();
	    LOGGER.info("Finished read checks from file with {} ms", watch.getTime(MILLISECONDS));
	    watch.reset();
	    
 
	} catch (final IOException ex) {
	    throw new IllegalStateException(ex);
	}
	
	return result;
    }
    
    private void writeChecksFull(final List<Element> onlineChecks, final List<Element> fileElements, final String version, final String pathFolder) {
	LOGGER.info("Start to write full checks to file");
	
	final var text = new StringBuilder(HEAD_FILE);
	
	final var watch = new StopWatch();
	watch.start();
	
	final var checkerProperties = ObjectUtils.isNotEmpty(fileElements) 
			? fileElements.stream()
					.filter(element -> element.type() == PROPERTY && equalsIgnoreCase(element.parent(), "Checker"))
					.map(Element::value)
					.toList()
	                : List.of("	<property name=\"charset\" value=\"UTF-8\"/>", 
	        		  "	<property name=\"severity\" value=\"error\"/>", 
	        		  "	<property name=\"fileExtensions\" value=\"java, properties, xml\"/>");
	
	text.append(replace(RULE_CATEGORY_SEPERATOR, "{#}", PROPERTIES_KEY));
	text.append(LF);

	for (final var checkerProperty : checkerProperties) {
	    text.append("	".concat(checkerProperty.trim())).append(LF);
	}
	
	final var checkerModules = onlineChecks.stream()
			.filter(element -> element.type() == MODULE && equalsIgnoreCase(element.parent(), "Checker"))
			.collect(groupingBy(Element::key));
	
	for (final var entry : checkerModules.entrySet()) {
	    final var key = entry.getKey();
	    final var modules = entry.getValue();
	    
	    text.append(replace(RULE_CATEGORY_SEPERATOR, "{#}", key));
	    text.append(LF);
	    
	    for (final var module : modules) {
		
		final var optionalRule = fileElements.stream()
				.filter(fileElement -> equalsIgnoreCase(fileElement.name(), module.name()))
				.findAny();
		
		if (optionalRule.isPresent()) {
		    text.append(optionalRule.get().value().trim())
	    		   .append(LF);
		    
		} else {
		    text.append(module.value().trim())
	    		   .append(LF);
		}
	    }
	}
			
	text.append(FOOT_FILE);
	
	final var file = pathFolder.concat(separator).concat("checkstyle-checks-").concat(version).concat("-full").concat(".xml");
	
	try (final var writer = new BufferedWriter(new FileWriter(file)))  {
	    
	    writer.write(text.toString());
	    
	} catch (final IOException ex) {
	    throw new IllegalStateException(ex);
	}
	
	watch.stop();
	LOGGER.info("Finished to write full checks to file with {} ms", watch.getTime(MILLISECONDS));
	watch.reset();
    }
    
    public void execute() {
	
	final var properties = readCheckStyleProperties();
	
	final var version = properties.get("version");
	final var pathFile = properties.get("pathFile");
	final var pathFolder = properties.get("pathFolder");
	
	final var onlineChecks = fetchOnlineChecks(version);
	final var fileChecks = readElementsFile(pathFile);
	writeChecksFull(onlineChecks, fileChecks, version, pathFolder);
    }

    public static void main(final String... args) {
	
	final var validateCheckstyeChecks = new ValidateCheckstyeChecks();
	validateCheckstyeChecks.execute();
    }
}
