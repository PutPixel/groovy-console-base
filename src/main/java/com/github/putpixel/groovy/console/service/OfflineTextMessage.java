package com.github.putpixel.groovy.console.service;

public class OfflineTextMessage {

	private String text;

	public OfflineTextMessage(String text) {
		this.text = text;
	}

	public OfflineTextMessage(Object text) {
		this.text = String.valueOf(text);
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return text;
	}

}
