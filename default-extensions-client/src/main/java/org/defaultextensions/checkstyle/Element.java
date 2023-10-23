package org.defaultextensions.checkstyle;

import static org.apache.commons.lang3.StringUtils.EMPTY;

record Element(String key, String name, String value, String parent, ElementType type) {

    public Element(final String name, final String value, final String parent, final ElementType element) {
	this(EMPTY, name, value, parent, element);
    }
}
