package com.github.putpixel.groovy.console.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;

import groovy.lang.Closure;
import groovy.lang.GroovyShell;

@Component
public class GroovyScriptService {

	private static final Logger LOG = LoggerFactory.getLogger(GroovyScriptService.class);

	private final ApplicationContext context;

	private final GroovyTxManager txManager;

	private final EntityManager em;

	private final Set<String> allRegisteredEntities;

	public GroovyScriptService(@Autowired ApplicationContext context, @Autowired GroovyTxManager txManager, @Autowired(required = false) EntityManager em) {
		this.context = context;
		this.txManager = txManager;
		this.em = em;
		this.allRegisteredEntities = getAllRegisteredEntities();
	}

	private RootObjectBinding createBindings() {
		HashMap<String, Object> bindingValues = new HashMap<>();
		bindingValues.put("LOG", LOG);
		bindingValues.put("em", em);
		bindingValues.put("entityManager", em);
		bindingValues.put("result", new ArrayList<>());
		return new RootObjectBinding(new GroovyShellBindings(em, txManager, context), bindingValues, allRegisteredEntities);
	}

	private GroovyShell createShell() {
		return new GroovyShell(this.getClass().getClassLoader(), createBindings());
	}

	@SuppressWarnings("unchecked")
	private Set<String> getAllRegisteredEntities() {
		try {
			Set<String> evaluate = (Set<String>) createShell().evaluate("em.getDelegate().getSessionFactory().getAllClassMetadata().keySet()");
			if (evaluate != null) {
				return evaluate;
			}
		} catch (Exception e) {
			// No entity manager - no entities
		}
		return ImmutableSet.of();
	}

	public Map<String, OfflineTextMessage> executeAndGetResult(GroovyScript script) {
		Map<String, OfflineTextMessage> scriptResult;
		if (script.isStartTx()) {
			scriptResult = txManager.inNewTx(new Closure<Map<String, OfflineTextMessage>>(null) {
				private static final long serialVersionUID = 2679771929572641544L;

				public Map<String, OfflineTextMessage> doCall() {
					return doExecuteScript(script);
				}

			});
		} else {
			scriptResult = doExecuteScript(script);
		}
		return scriptResult;
	}

	private Map<String, OfflineTextMessage> doExecuteScript(GroovyScript message) {
		Map<String, OfflineTextMessage> result = new HashMap<>();
		GroovyShell shell = createShell();
		addVariablesToResult(result, shell);
		LocalDateTime startTime = LocalDateTime.now();
		result.put("Start time", new OfflineTextMessage(startTime));
		try {
			Object evaluate = shell.evaluate(message.getBody());
			result.put("Result", new OfflineTextMessage(evaluate));
		} catch (Exception e) {
			addVariablesToResult(result, shell);
			GroovyResultException groovyResultException = new GroovyResultException(e);
			groovyResultException.setResult(result);
			throw groovyResultException;
		} finally {
			LocalDateTime endTime = LocalDateTime.now();
			result.put("End time", new OfflineTextMessage(endTime));
			result.put("Execution time (seconds)", new OfflineTextMessage(ChronoUnit.SECONDS.between(endTime, startTime)));
		}
		return result;
	}

	private void addVariablesToResult(Map<String, OfflineTextMessage> result, GroovyShell shell) {
		Map<?, ?> variables = shell.getContext().getVariables();
		for (Map.Entry<?, ?> entry : variables.entrySet()) {
			String variableName = String.valueOf(entry.getKey());
			result.put(variableName, new OfflineTextMessage(String.valueOf(entry.getValue())));
		}
	}

}
