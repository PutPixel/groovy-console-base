package com.github.putpixel.groovy.console.service;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;

public class GroovyResultException extends RuntimeException {

	private Map<String, OfflineTextMessage> result;

	public GroovyResultException(Exception cause) {
		super(cause);
	}

	public Map<String, OfflineTextMessage> getResult() {
		return result;
	}

	public void setResult(Map<String, OfflineTextMessage> results) {
		this.result = results;
	}

	@Override
	public void printStackTrace(PrintStream s) {
		getCause().printStackTrace(s);
		String resultAsString = getResultAsString();
		if (!resultAsString.isEmpty()) {
			s.println();
			s.print(resultAsString);
		}
	}

	@Override
	public void printStackTrace(PrintWriter s) {
		getCause().printStackTrace(s);
		String resultAsString = getResultAsString();
		if (!resultAsString.isEmpty()) {
			s.println();
			s.print(resultAsString);
		}
	}

	private String getResultAsString() {
		if (result != null) {
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, OfflineTextMessage> stringListEntry : result.entrySet()) {

				sb.append("\n");
				sb.append("-----------------------------------");
				sb.append("\n");
				sb.append("Variable: ").append(stringListEntry.getKey());
				sb.append("\n");
				OfflineTextMessage value = stringListEntry.getValue();
				sb.append("Value: ");
				sb.append(value.getText());
				sb.append("\n");
			}
			sb.append("-----------------------------------");
			sb.append("\n");
			return sb.toString();
		}
		return "";
	}
}