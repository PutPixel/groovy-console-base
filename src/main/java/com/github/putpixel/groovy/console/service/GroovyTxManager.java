package com.github.putpixel.groovy.console.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import groovy.lang.Closure;

@Component
public class GroovyTxManager {

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public <T> T inNewTx(Closure<T> c) {
		return c.call();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
	public <T> T inNewReadOnlyTx(Closure<T> c) {
		return c.call();
	}
}
