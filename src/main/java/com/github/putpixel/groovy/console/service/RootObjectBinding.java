package com.github.putpixel.groovy.console.service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.runtime.MethodClosure;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import groovy.lang.Binding;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;

/**
 * Provides syntax sugar for resolving entity by property.
 *
 * @GExample <b>1. </b> Shipment.byBarcode("1234") will return this shipment
 */
// Also binds {@link GroovyShellBindings} to Groovy console. (Internal stuff)
public class RootObjectBinding extends Binding {
	private final GroovyShellBindings root;
	private Set<String> registeredEntities;

	public RootObjectBinding(GroovyShellBindings context, Map<?, ?> params, Set<String> registeredEntities) {
		super(params);
		this.root = context;
		this.registeredEntities = registeredEntities != null ? registeredEntities : ImmutableSet.of();
	}

	/**
	 * Internal, nothing helpful for end user
	 */
	@Override
	public Object getVariable(String arg0) {
		try {
			return super.getVariable(arg0);
		} catch (MissingPropertyException e) {
			if (root == null) {
				throw e;
			}

			if (looksLikeEntity(arg0)) {
				return new EntityHandler(arg0);
			}

			String getterName = "get" + Character.toUpperCase(arg0.charAt(0)) + arg0.substring(1);
			try {
				Method method = root.getClass().getMethod(getterName);
				if (method.getReturnType() != void.class) {
					return method.invoke(root);
				}
			} catch (Exception e1) {
				// Ignore
			}

			try {
				Field field = root.getClass().getField(arg0);
				return field.get(arg0);
			} catch (Exception e1) {
				// Ignore
			}

			try {
				Method[] methods = root.getClass().getMethods();
				for (Method method : methods) {
					if (method.getName().equals(arg0)) {
						return new MethodClosure(root, arg0);
					}
				}
			} catch (Exception e1) {
				// Ignore
			}

			try {
				Method method = root.getClass().getMethod("get", String.class);
				if (method.getReturnType() != void.class) {
					return method.invoke(root, arg0);
				}
			} catch (Exception e1) {
				// Ignore
			}

			throw e;
		}
	}

	private class EntityHandler extends GroovyObjectSupport {

		private static final int MAX_ENTITIES_PER_QUERY = 1000;

		private String entityName;

		public EntityHandler(String entityName) {
			this.entityName = entityName;
		}

		@SuppressWarnings("unused")
		public Object methodMissing(String name, Object args) {
			return invokeMethod(name, (Object[]) args);
		}

		public Object invokeMethod(String name, Object[] args) {
			if (name.startsWith("by") && args.length == 1) {
				String arg0 = name.replaceFirst("by", "");
				String paramName = lowerFirstLetter(arg0);
				return query("FROM " + entityName + " WHERE " + paramName + " = :param", ImmutableMap.of("param", args[0]));
			}
			if (name.startsWith("like") && args.length == 1) {
				String arg0 = name.replaceFirst("like", "");
				String paramName = lowerFirstLetter(arg0);
				return query("FROM " + entityName + " WHERE " + paramName + " like :param", ImmutableMap.of("param", "%" + args[0] + "%"));
			} else {
				throw new UnsupportedOperationException(
						"Can't find handler for operation " + name + ", supported opeartions are: by...(single argument), like...(single argument) ");
			}
		}

		private Object query(String hql, ImmutableMap<String, Object> params) {
			List<?> queryLimited = root.queryLimited(hql, params, MAX_ENTITIES_PER_QUERY);
			if (queryLimited.size() == 1) {
				return Iterables.getOnlyElement(queryLimited);
			} else {
				return queryLimited;
			}
		}

		private String lowerFirstLetter(String arg0) {
			return Character.toLowerCase(arg0.charAt(0)) + arg0.substring(1);
		}

	}

	private boolean looksLikeEntity(String arg0) {
		return registeredEntities.parallelStream().anyMatch(entityName -> entityName.endsWith(arg0));
	}

}