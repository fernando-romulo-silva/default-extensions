package org.defaultextensions.pmd;

import static org.apache.commons.lang3.StringUtils.SPACE;

import java.util.Objects;

record Rule(String name, String value, String key, Boolean deprecated) {

    public Rule(String name, String value) {
	this(name, value, SPACE, null);
    }

    @Override
    public boolean equals(final Object obj) {
	
	if (obj == null) {
	    return false;
	}
	
	if (this == obj) {
	    return true;
	}
	
	if (obj instanceof final Rule other) {
	    return Objects.equals(name, other.name);
	}
	
	return false;
    }

    @Override
    public int hashCode() {
	return Objects.hash(name);
    }
}
