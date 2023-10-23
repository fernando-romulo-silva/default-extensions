package org.defaultextensions.pmd;

import static org.apache.commons.lang3.StringUtils.SPACE;

record Rule(String name, String value, String key, Boolean deprecated) {
    
    public Rule(String name, String value) {
	this(name, value, SPACE, null);
    }
}
