/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import io.greenscreens.quark.annotations.ExtJSAsync;
import io.greenscreens.quark.annotations.ExtJSDirect;
import io.greenscreens.quark.annotations.ExtJSMethod;
import io.greenscreens.quark.async.QuarkAsyncContext;
import io.greenscreens.quark.cdi.Required;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.inject.Inject;

/**
 * Java Reflection helper class
 */
public enum ReflectionUtil {
;

	@SuppressWarnings("unchecked")
	public static <T> Collection<T> createListOfType(final Class<?> collection)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

		if (collection.isInterface()) {
			return new ArrayList<>();
		}

		if (Modifier.isAbstract(collection.getModifiers())) {
			return new ArrayList<>();
		}

		return (Collection<T>) collection.getDeclaredConstructor().newInstance();
	}

	/**
	 * Check for @Required annotation,in such case, parameter can't be null
	 * @param paramList
	 * @param params
	 * @return
	 */
	public static boolean isParametersInvalid(final List<AnnotatedParameter<AnnotatedParameter<?>>> paramList, final Object[] params) {

		boolean sts = false;
		Required req = null;
		int i = 0;

		for (AnnotatedParameter<?> param : paramList) {
			req = param.getAnnotation(Required.class);
			if (Objects.nonNull(req) && Objects.isNull(params[i])) {
				sts = true;
				break;
			}
			i++;
		}

		return sts;
	}
	
	public static boolean isJsonNode(final Object o) {
		return o instanceof JsonNode;
	}
	
	public static boolean isParameterized(final Type type) {
		return type instanceof ParameterizedType;
	}
	
	public static boolean isCollection(final Type type) {		
		return Collection.class.isAssignableFrom((Class<?>) type);
	}

	public static boolean isAsync(final Class<?> clazz) {
		return Optional.ofNullable(clazz).map(m -> m.getAnnotation(ExtJSAsync.class)).isPresent();
	}
	
	public static boolean isVirtual(final Class<?> clazz) {
		return Optional.ofNullable(clazz).map(m -> m.getAnnotation(ExtJSAsync.class)).map(a -> a.virtual()).orElse(false);
	}
	
	public static boolean isAsync(final Method method) {
		return Optional.ofNullable(method).map(m -> m.getAnnotation(ExtJSAsync.class)).isPresent();
	}
	
	public static boolean isVirtual(final Method method) {
		return Optional.ofNullable(method).map(m -> m.getAnnotation(ExtJSAsync.class)).map(a -> a.virtual()).orElse(false);
	}

	public static boolean isValidate(final Method method) {
		return extAnnotation(method).map(a -> a.validate()).orElse(false);
	}
	
	public static String mappedName(final Method method) {
		return extAnnotation(method).map(a -> a.value()).orElse(null);
	}
	
	public static String[] paths(final Class<?> clazz) {
		return extAnnotation(clazz).map(c -> c.paths()).orElse(new String[] {});
	}
	
	public static Optional<ExtJSMethod> extAnnotation(final Method method) {
		return Optional.ofNullable(method).map(m -> m.getAnnotation(ExtJSMethod.class));
	}
	
	public static Optional<ExtJSDirect> extAnnotation(final Class<?> clazz) {
		return Optional.ofNullable(clazz).map(c -> c.getAnnotation(ExtJSDirect.class));
	}
	
	public static boolean isVoid(final Method method) {
		final Class<?> clz = method.getReturnType();
		return (clz == void.class || clz == Void.class);		
	}
	
	public static boolean hasRetVal(final Method method) {
		return !isVoid(method);
	}

	public static boolean isAsyncResponder(final Class<?> clazz) {
		if (Objects.isNull(clazz)) return false;
		return isAsyncResponder(clazz.getDeclaredFields());
	}
	
	public static boolean isAsyncResponder(final Field [] fields ) {
		if (Objects.isNull(fields)) return false;
		boolean sts = false;
		for (Field field : fields) {
			sts = ReflectionUtil.isAsyncResponder(field);
			if (sts) break;
		}
		return sts;		
	}
	
	public static boolean isAsyncResponder(final Field field) {
		return field.isAnnotationPresent(Inject.class) && field.getType() == QuarkAsyncContext.class;		
	}
}
