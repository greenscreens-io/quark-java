/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.JsonDecoder;
import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.ext.annotations.ExtJSAction;
import io.greenscreens.quark.ext.annotations.ExtJSDirect;
import io.greenscreens.quark.ext.annotations.ExtJSDirectLiteral;
import io.greenscreens.quark.ext.annotations.ExtJSMethod;

/**
 * Singleton class used to find CDI bean and wraps it into destructable
 * instance. It is used as an internal bean finder.
 */
@ApplicationScoped
public class BeanManagerUtil {

	private ArrayNode api;

	@PostConstruct
	public void init() {
		getAPI();
	}
	
	/**
	 * Retrieve engine meta structure for web
	 * 
	 * @return
	 */
	public ArrayNode getAPI() {
		if (Objects.isNull(api)) {
			build(null);
		}
		return api;
	}

	/**
	 * Finds CDI bean by class type and defined qualifier annotations
	 * 
	 * @param type       - class type implemented in CDI bean
	 * @param qualifiers - additional bean qualifier
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> IDestructibleBeanInstance<T> getDestructibleBeanInstance(final Class<T> type,
			final Annotation... qualifiers) {

		final Set<Bean<?>> beansF = new HashSet<>();
		final Set<Bean<?>> beans = QuarkEngine.getBeanManager().getBeans(Object.class, qualifiers);
		
		final Iterator<Bean<?>> iterator = beans.iterator();

		while (iterator.hasNext()) {

			final Bean<?> bean = iterator.next();

			if (type.isInterface()) {

				final Class<?>[] intfs = bean.getBeanClass().getInterfaces();

				for (final Class<?> intf : intfs) {

					if (type.equals(intf)) {
						beansF.add(bean);
					}

				}

			} else {

				if (bean.getBeanClass().equals(type)) {
					beansF.add(bean);
				}

			}
		}

		final Bean<T> bean = (Bean<T>) QuarkEngine.getBeanManager().resolve(beansF);
		return getDestructibleBeanInstance(bean);
	}

	/** 
	 * Wraps CDI bean into custom destructible instance
	 * 
	 * @param bean
	 * @return
	 */
	public <T> IDestructibleBeanInstance<T> getDestructibleBeanInstance(final Bean<T> bean) {

		IDestructibleBeanInstance<T> result = null;

		if (Objects.nonNull(bean)) {

			final CreationalContext<T> creationalContext = QuarkEngine.getBeanManager().createCreationalContext(bean);

			if (Objects.nonNull(creationalContext)) {
				final T instance = bean.create(creationalContext);
				result = new DestructibleBeanInstance<>(instance, bean, creationalContext);
			}

		}

		return result;
	}

	/**
	 * Build meta structure for web
	 * 
	 * @return
	 */
	public ArrayNode build(final String [] pathsFilter) {

		final ArrayNode root = JsonNodeFactory.instance.arrayNode();
		ObjectNode objectNode = null;
		ArrayNode methodsNode = null;
		
		final List<String> pLsit =  Objects.isNull(pathsFilter) ? null : Arrays.asList(pathsFilter);

		final ExtJSDirectLiteral type = new ExtJSDirectLiteral(pathsFilter);
		final Set<Bean<?>> beans = QuarkEngine.getBeanManager().getBeans(Object.class, type);
		for (Bean<?> bean : beans) {

			final Class<?> clazz = bean.getBeanClass();

			final ExtJSDirect extJSDirect = clazz.getAnnotation(ExtJSDirect.class);
			final ExtJSAction extJSAction = clazz.getAnnotation(ExtJSAction.class);

			if (Objects.nonNull(pLsit)) {
				List<String> cLsit = new ArrayList<>(Arrays.asList(extJSDirect.paths()));
				cLsit.retainAll(pLsit);
				if (cLsit.isEmpty()) {
					continue;
				} 
			}
			
			final JsonNode paths = JsonDecoder.getJSONEngine().valueToTree(extJSDirect.paths());

			objectNode = JsonNodeFactory.instance.objectNode();
			objectNode.put("namespace", extJSAction.namespace());
			objectNode.put("action", extJSAction.action());
			objectNode.set("paths", paths);
			methodsNode = objectNode.putArray("methods");
			buildMethod(methodsNode, clazz);		
			root.add(objectNode);

		}

		api = root;
		return root;

	}
	
	/**
	 * Build exposed method list
	 * @param methodsNode
	 * @param clazz
	 */
	void buildMethod(final ArrayNode methodsNode, final Class<?> clazz) {
		
		ExtJSMethod extJSMethod = null;
		final Method[] methods = clazz.getMethods();

		for (Method method : methods) {
			extJSMethod = method.getAnnotation(ExtJSMethod.class);
			if (Objects.nonNull(extJSMethod)) {
				ObjectNode objNode = JsonNodeFactory.instance.objectNode();
				methodsNode.add(objNode);				
				objNode.put("name", extJSMethod.value());
				objNode.put("len", method.getParameterCount());
				if (!extJSMethod.encrypt()) {
					objNode.put("encrypt", false);
				}
				if (extJSMethod.async()) {
					objNode.put("async", extJSMethod.async());	
				}
			}
		}
	}
	
}
