package org.defaultextensions.checkstyle;

import static java.nio.file.Files.notExists;
import static java.util.Map.entry;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.defaultextensions.DefaultExtensionsClient.getFileProperties;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
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

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;

public class ValidateCheckstyeChecks {

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

    private static final String HEAD_FILE = """
    		<?xml version="1.0"?>
    		<!DOCTYPE module PUBLIC
    		          "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
    		          "https://checkstyle.org/dtds/configuration_1_3.dtd">
    		      <module name="Checker">

    		         <property name="charset" value="UTF-8"/>

    		         <property name="severity" value="error"/>

    		         <property name="fileExtensions" value="java, properties, xml"/>

    		        """;
    private static final String FOOT_FILE = "</module>	";

    private record Check(String group, String name, String value, String parent) {
    };
    
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

    private Map<String, List<Check>> fetchOnlineChecks(final String version) {
	
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

	final var mapChecks = new HashMap<String, List<Check>>();
	
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

		final var listChecks = new ArrayList<Check>();

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
		    
		    listChecks.add(new Check(key, name, source, parent));
		}
		
		LOGGER.debug("Key {}, qtChecks {}", key, listChecks.size());

		mapChecks.put(key, listChecks);
	    }

	} catch (final IOException | URISyntaxException ex) {
	    throw new IllegalStateException(getRootCauseMessage(ex));
	}

	watch.stop();
	LOGGER.info("Finished fetch check with {} ms", watch.getTime(MILLISECONDS));
	watch.reset();
	
	return mapChecks;
    }
    
    private List<Check> readChecksFile(final String pathFile) {
	
	LOGGER.info("Start to read rules from file {}", pathFile);
	
	final var certificationPath = Paths.get(pathFile);
	
	if (notExists(certificationPath)) {
	    return List.of();
	}
	
	final var result = new ArrayList<Check>();
	
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
	    
	    final var rootModules = xmlDocument.nodes("/module[@name='Checker']/module[@name!='TreeWalker']");
	    
	    final var treeWalkModules = xmlDocument.nodes("/module[@name='Checker']/module[@name='TreeWalker']/module");
	    
	    
	
	    watch.stop();
	    LOGGER.info("Finished read checks from file with {} ms", watch.getTime(MILLISECONDS));
	    watch.reset();
	    
 
	} catch (final IOException ex) {
	    throw new IllegalStateException(ex);
	}
	
	return result;
    }
    
    private void writeChecks(final Map<String, List<Check>> onlineChecks, final List<Check> fileChecks, final String version, final String pathFolder) {
	
    }
    
    public void execute() {
	
	final var properties = readCheckStyleProperties();
	
	final var version = properties.get("version");
	
	final var pathFile = properties.get("pathFile");
	
	final var pathFolder = properties.get("pathFolder");
	
	final var onlineChecks = fetchOnlineChecks(version);
	
	final var fileChecks = readChecksFile(pathFile);
	
	writeChecks(onlineChecks, fileChecks, version, pathFolder);
    }

    public static void main(final String... args) {
	
	final var validateCheckstyeChecks = new ValidateCheckstyeChecks();
	validateCheckstyeChecks.execute();
    }
}
