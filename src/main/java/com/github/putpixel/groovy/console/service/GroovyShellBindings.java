package com.github.putpixel.groovy.console.service;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.ImmutableMap;

import groovy.lang.Closure;

// Add here only methods that are intended to use in Groovy console
public class GroovyShellBindings {

	private final EntityManager em;

	private GroovyTxManager txManager;

	private final ApplicationContext context;

	public GroovyShellBindings(EntityManager em, GroovyTxManager txManager, ApplicationContext context) {
		this.em = em;
		this.txManager = txManager;
		this.context = context;
	}

	public Object autowire(Class<?> clazz) throws Exception {
		return doLookupInternal(clazz);
	}

	public Object lookup(Class<?> clazz) throws Exception {
		return doLookupInternal(clazz);
	}

	public int executeUpdate(String hql) {
		return executeUpdate(hql, ImmutableMap.of());
	}

	public int executeUpdate(String hql, Map<String, Object> params) {
		Query query = em.createQuery(hql);
		setParamsToQuery(params, query);
		return query.executeUpdate();
	}

	public int executeUpdateSql(String hql) {
		return executeUpdateSql(hql, ImmutableMap.of());
	}

	public int executeUpdateSql(String hql, Map<String, Object> params) {
		Query query = em.createNativeQuery(hql);
		setParamsToQuery(params, query);
		return query.executeUpdate();
	}

	public List query(String hql) {
		return query(hql, ImmutableMap.of());
	}

	public List query(String hql, Map<String, Object> params) {
		Query query = em.createQuery(hql);
		setParamsToQuery(params, query);
		return query.getResultList();
	}

	public List queryLimited(String hql, Map<String, Object> params, int maxResults) {
		Query query = em.createQuery(hql);
		query.setMaxResults(maxResults);
		setParamsToQuery(params, query);
		return query.getResultList();
	}

	public List querySql(String sql) {
		return querySql(sql, ImmutableMap.of());
	}

	public List querySql(String sql, Map<String, Object> params) {
		Query query = em.createNativeQuery(sql);
		setParamsToQuery(params, query);
		return query.getResultList();
	}

	public Object inNewTx(Closure<?> c) {
		return txManager.inNewTx(c);
	}

	public Object inNewReadOnlyTx(Closure<?> c) {
		return txManager.inNewReadOnlyTx(c);
	}

	private Object doLookupInternal(Class<?> clazz) throws Exception {
		try {
			return context.getBean(clazz);
		} catch (BeansException beanException) {
			// If an interface we can't instance it other way
			if (clazz.isInterface()) {
				throw beanException;
			}

			try {
				// Fail over in case we wan't initialize class directly
				Object newInstance = clazz.newInstance();
				AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();
				beanFactory.autowireBean(newInstance);
				return newInstance;
			} catch (Exception e) {
				throw new RuntimeException(
						"Instance of class " + clazz.getCanonicalName() + " can't been found in spring context or instantiated directly");
			}
		}
	}

	private void setParamsToQuery(Map<String, Object> params, Query query) {
		params.forEach((name, value) -> query.setParameter(name, value));
	}

}