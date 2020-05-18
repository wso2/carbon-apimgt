package org.wso2.carbon.apimgt.api.doc.model;

import java.util.List;
import java.util.Objects;

public class APIResource {
	
	private String path;
	
	private String description;
	
	private List<Operation> operations;

	private String verb;
	
	public APIResource(String path, String description, List<Operation> ops) {
		this.path = path;
		this.description = description;
		this.operations = ops;
	}

	public APIResource(String verb, String path) {
		this.verb = verb;
		this.path = path;
	}

	@Override
	public String toString() {
		return "{" +
				"verb='" + verb + '\'' +
				", path='" + path + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (!(o instanceof APIResource)) {
			return false;
		}

		APIResource that = (APIResource) o;
		return verb.equals(that.verb) &&
				path.equals(that.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(verb, path);
	}
}


