package org.defaultextensions.checkstyle;

import static java.util.Map.entry;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    		      <!--
    			   Default checkstyle configuration
    		       -->
    		      <module name="Checker">

    		   <property name="charset" value="UTF-8"/>

    		   <property name="severity" value="error"/>

    		   <property name="fileExtensions" value="java, properties, xml"/>

    		        """;
    private static final String FOOT_FILE = "</module>	";

    private static final String FILE_LOCATION = "/home/fernando/Development/workspaces/eclipse-workspace/default-extensions/default-extensions-files/src/main/resources/default-extensions/checkstyle/java";

    private record Check(String group, String name, String parent) {
    };

    public static Map<String, List<Check>> fetchOnlineChecks() {
	
	LOGGER.info("Start to fetch checks from online");

	final var url = "https://checkstyle.sourceforge.io/checks";

	final var ruleGroupsUrls = List.of( //
			entry(ANNOTATIONS_KEY, url.concat("/annotation/index.html")), //
			entry(BLOCKS_KEY, url.concat("/blocks/index.html")), //
			entry(DESIGN_KEY, url.concat("/design/index.html")), //
			entry(CODING_KEY, url.concat("/coding/index.html")), //
			entry(HEADER_KEY, url.concat("/header/index.html")), //
			entry(IMPORTS_KEY, url.concat("/imports/index.html")), //
			entry(JAVADOC_KEY, url.concat("/javadoc/index.html")), //
			entry(METRICS_KEY, url.concat("/metrics/index.html")), //
			entry(MISC_KEY, url.concat("/misc/index.html")), //
			entry(MODIFIER_KEY, url.concat("/modifier/index.html")), //
			entry(NAMING_KEY, url.concat("/naming/index.html")), //
			entry(REGEXP_KEY, url.concat("/regexp/index.html")), //
			entry(SIZES_KEY, url.concat("/sizes/index.html")), //
			entry(WHITE_SPACES_KEY, url.concat("/whitespace/index.html")) //
	);

	final var mapChecks = new HashMap<String, List<Check>>();

	try {
	    for (Entry<String, String> entry : ruleGroupsUrls) {

		final var key = entry.getKey();
		final var groupUrl = entry.getValue();

		final var document = Jsoup.parse(new URL(groupUrl), 10000);

		final var tds = document.select("td[align=left]");

		final var hyperlinks = tds.select("a[href]");

		final var listChecks = new ArrayList<Check>();

		for (final var hyperlink : hyperlinks) {
		    final var href = hyperlink.attr("href");

		    final var internalUrl = groupUrl.replace("index.html", href);
		    final var internalDocument = Jsoup.parse(new URL(internalUrl), 10000);

		    final var parent = internalDocument.select("section[id=Parent_Module]").select("a").text();

		    listChecks.add(new Check(key, StringUtils.substringAfter(href, "#"), parent));
		}
		
		LOGGER.debug("Key {}, qtRules {}", key, listChecks.size());

		mapChecks.put(key, listChecks);
	    }

	} catch (final IOException ex) {
	    throw new IllegalStateException(getRootCauseMessage(ex));
	}

	LOGGER.info("Finished fetch check on lines");
	
	return mapChecks;
    }
    
    public static List<Check> readChecksFile() {
	return List.of();
    }
    
    public static void writeChecks(final Map<String, List<Check>> onlineChecks, final List<Check> fileChecks) {
	
    }

    public static void main(String... args) {
	final var onlineChecks = fetchOnlineChecks();
	
	final var checkRules = readChecksFile();

	writeChecks(onlineChecks, checkRules);
    }
}
