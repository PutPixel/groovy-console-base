package com.github.putpixel.groovy.console.service;

import com.google.gson.GsonBuilder;

public class GroovyScript {

	private String body;

	private boolean startTx;

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public boolean isStartTx() {
		return startTx;
	}

	public void setStartTx(boolean startTx) {
		this.startTx = startTx;
	}

	@Override
	public String toString() {
		return "GroovyScript " + asJson();
	}

	public String asJson() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

}
