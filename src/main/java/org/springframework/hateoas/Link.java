/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Value object for links.
 * 
 * @author Oliver Gierke
 */
@XmlType(name = "link", namespace = Link.ATOM_NAMESPACE)
@JsonIgnoreProperties("templated")
public class Link implements Serializable {

	private static final long serialVersionUID = -9037755944661782121L;

	public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";

	public static final String REL_SELF = "self";
	public static final String REL_FIRST = "first";
	public static final String REL_PREVIOUS = "prev";
	public static final String REL_NEXT = "next";
	public static final String REL_LAST = "last";

    /**
     * Whether rel types with a single Link instance should be serialized as a collection
     */
    private boolean preferCollections;

	@XmlAttribute private String rel;
	@XmlAttribute private String href;
    @JsonInclude(value = Include.NON_EMPTY)
    @XmlAttribute
    private String name;
    @JsonInclude(value = Include.NON_EMPTY)
    @XmlAttribute
    private String title;
	@JsonInclude(value = Include.NON_EMPTY)
	@XmlAttribute
	private String profile;
	@XmlTransient @JsonIgnore private UriTemplate template;

	/**
	 * Creates a new link to the given URI with the self rel.
	 * 
	 * @see #REL_SELF
	 * @param href must not be {@literal null} or empty.
	 */
	public Link(String href) {
		this(href, REL_SELF);
	}

	/**
	 * Creates a new {@link Link} to the given URI with the given rel.
	 * 
	 * @param href must not be {@literal null} or empty.
	 * @param rel must not be {@literal null} or empty.
	 */
	public Link(String href, String rel) {
		this(new UriTemplate(href), rel);
	}

	/**
	 * Creates a new Link from the given {@link UriTemplate} and rel.
	 * 
	 * @param template must not be {@literal null}.
	 * @param rel must not be {@literal null} or empty.
	 */
	public Link(UriTemplate template, String rel) {

		Assert.notNull(template, "UriTempalte must not be null!");
		Assert.hasText(rel, "Rel must not be null or empty!");

		this.template = template;
		this.href = template.toString();
		this.rel = rel;
	}

	/**
	 * Empty constructor required by the marshalling framework.
	 */
	protected Link() {

	}

    /**
     * Returns whether collections should be preferred when there is only a single Link of a give rel type
     */
    @JsonIgnore
    public boolean getPreferCollections() {
        return preferCollections;
    }

	/**
	 * Returns the actual URI the link is pointing to.
	 * 
	 * @return
	 */
	public String getHref() {
		return href;
	}

	/**
	 * Returns the rel of the link.
	 * 
	 * @return
	 */
	public String getRel() {
		return rel;
	}

    /**
     * Returns the name of the link.
     * 
     * @return
     */
    public String getName() {
        return name;
    }

	/**
	 * Returns the profile of the link.
	 *
	 * @return
	 */
	public String getProfile() {
		return profile;
	}

	/**
     * Returns the title of the link
     * 
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns a {@link Link} pointing to the same URI but with the given relation.
     * 
     * @param rel must not be {@literal null} or empty.
     * @return
     */
	public Link withRel(String rel) {
		return new Link(href, rel);
	}

	/**
	 * Returns a {@link Link} pointing to the same URI but with the {@code self} relation.
	 * 
	 * @return
	 */
	public Link withSelfRel() {
		return withRel(Link.REL_SELF);
	}

	/**
	 * Returns the variable names contained in the template.
	 * 
	 * @return
	 */
	@JsonIgnore
	public List<String> getVariableNames() {
		return getUriTemplate().getVariableNames();
	}

	/**
	 * Returns all {@link TemplateVariables} contained in the {@link Link}.
	 * 
	 * @return
	 */
	@JsonIgnore
	public List<TemplateVariable> getVariables() {
		return getUriTemplate().getVariables();
	}

	/**
	 * Returns whether the link is templated.
	 * 
	 * @return
	 */
	public boolean isTemplated() {
		return !getUriTemplate().getVariables().isEmpty();
	}

	/**
	 * Turns the current template into a {@link Link} by expanding it using the given parameters.
	 * 
	 * @param arguments
	 * @return
	 */
	public Link expand(Object... arguments) {
		return new Link(getUriTemplate().expand(arguments).toString(), getRel());
	}

	/**
	 * Turns the current template into a {@link Link} by expanding it using the given parameters.
	 * 
	 * @param arguments must not be {@literal null}.
	 * @return
	 */
	public Link expand(Map<String, ? extends Object> arguments) {
		return new Link(getUriTemplate().expand(arguments).toString(), getRel());
	}

	private UriTemplate getUriTemplate() {

		if (template == null) {
			this.template = new UriTemplate(href);
		}

		return template;
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Link)) {
			return false;
		}

		Link that = (Link) obj;

		return this.href.equals(that.href) && this.rel.equals(that.rel);
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		int result = 17;
		result += 31 * href.hashCode();
		result += 31 * rel.hashCode();
		return result;
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("<%s>;rel=\"%s\"", href, rel);
	}

	/**
	 * Factory method to easily create {@link Link} instances from RFC-5988 compatible {@link String} representations of a
	 * link. Will return {@literal null} if an empty or {@literal null} {@link String} is given.
	 * 
	 * @param element an RFC-5899 compatible representation of a link.
	 * @throws IllegalArgumentException if a non-empty {@link String} was given that does not adhere to RFC-5899.
	 * @throws IllegalArgumentException if no {@code rel} attribute could be found.
	 * @return
	 */
	public static Link valueOf(String element) {

		if (!StringUtils.hasText(element)) {
			return null;
		}

		Pattern uriAndAttributes = Pattern.compile("<(.*)>;(.*)");
		Matcher matcher = uriAndAttributes.matcher(element);

		if (matcher.find()) {

			Map<String, String> attributes = getAttributeMap(matcher.group(2));

			if (!attributes.containsKey("rel")) {
				throw new IllegalArgumentException("Link does not provide a rel attribute!");
			}

			return new Link(matcher.group(1), attributes.get("rel"));

		} else {
			throw new IllegalArgumentException(String.format("Given link header %s is not RFC5988 compliant!", element));
		}
	}

	/**
	 * Parses the links attributes from the given source {@link String}.
	 * 
	 * @param source
	 * @return
	 */
	private static Map<String, String> getAttributeMap(String source) {

		if (!StringUtils.hasText(source)) {
			return Collections.emptyMap();
		}

		Map<String, String> attributes = new HashMap<String, String>();
		Pattern keyAndValue = Pattern.compile("(\\w+)=\\\"(\\p{Alnum}*)\"");
		Matcher matcher = keyAndValue.matcher(source);

		while (matcher.find()) {
			attributes.put(matcher.group(1), matcher.group(2));
		}

		return attributes;
	}

    public static class Builder {

        private String rel;
        private String href;
        private String name;
        private String title;
		private String profile;
        private UriTemplate template;
        private boolean preferCollections;

        public Builder rel(String rel) {
            this.rel = rel;
            return this;
        }

        public Builder href(String href) {
            this.href = href;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

		public Builder profile(String profile) {
			this.profile = profile;
			return this;
		}

		public Builder template(UriTemplate template) {
            this.template = template;
            return this;
        }

        public Builder preferCollections() {
            this.preferCollections = true;
            return this;
        }

        public Link build() {
            Link link = null;

            if (template != null) {
                link = new Link(template, rel);
            } else if (rel != null) {
                link = new Link(href, rel);
            } else {
                link = new Link(href);
            }

            link.title = title;
            link.name = name;
			link.profile = profile;
            link.preferCollections = preferCollections;

            return link;
        }
    }
}
