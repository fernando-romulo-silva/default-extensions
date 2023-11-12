package org.defaultextensions.checkstyle;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Objects;

record Element(String key, String name, String value, String parent, ElementType type) {

    public Element(final String name, final String value, final String parent, final ElementType element) {
	this(EMPTY, name, value, parent, element);
    }

    @Override
    public boolean equals(final Object obj) {
	
	if (obj == null) {
	    return false;
	}

	if (this == obj) {
	    return true;
	}

	if (obj instanceof final Element other) {
	    return Objects.equals(name, other.name);
	}

	return false;
    }

    @Override
    public int hashCode() {
	return Objects.hash(name);
    }
}
