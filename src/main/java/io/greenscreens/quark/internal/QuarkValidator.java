/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.internal;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.annotations.ExtName;
import io.greenscreens.quark.reflection.IQuarkHandle;
import io.greenscreens.quark.utils.QuarkUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ElementKind;
import jakarta.validation.Path.Node;
import jakarta.validation.Path.ParameterNode;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.executable.ExecutableValidator;

/**
 * Internal reflection util that handle JSON t oControlelr mappings.  
 */
public enum QuarkValidator {
;
	
	private static final Logger LOG = LoggerFactory.getLogger(QuarkValidator.class);

	private static ValidatorFactory factory = null;	
	
	/**
	 * Helper method to validate calling method arguments annotated with JSR-380
	 * 
	 * @param instance
	 * @param method
	 * @param params
	 * @throws Exception
	 */
	public static void validateParameters(final IQuarkHandle handle, final Object instance, final Object[] params) throws IOException {

		if (!handle.isValidate()) {
			return;
		}

		if (Objects.isNull(factory)) {
			LOG.warn("Validation factory not initialized! Unable to validate Quark Engine call parameters.");
			return;
		}
		
		final ExecutableValidator validator = factory.getValidator().forExecutables();
		final Set<ConstraintViolation<Object>> violations = validator.validateParameters(instance, handle.method(), params);

		if (!violations.isEmpty()) {
			final String message = describeValidations(handle, violations);
			throw new IOException(message);
		}

	}
	
	/**
	 * Describe validation errors
	 * @param method
	 * @param violations
	 * @return
	 */
	public static String describeValidations(final IQuarkHandle handle, final Set<ConstraintViolation<Object>> violations) {
		
		final StringBuilder builder = new StringBuilder();
		
		for (ConstraintViolation<Object> violation : violations) {

			final Iterator<Node> it = violation.getPropertyPath().iterator();

			while (it.hasNext()) {
				final Node node = it.next();
				describeNode(handle, builder, node);
			}

			builder.append(violation.getMessage());
			builder.append("\n");
		}
		
		return builder.toString().trim();
	}
	
	/**
	 * Describe validation error node
	 * @param method
	 * @param builder
	 * @param node
	 */
	public static void describeNode(final IQuarkHandle handle, final StringBuilder builder, final Node node) {
		final Parameter [] parameters = handle.method().getParameters();
		if (node.getKind() == ElementKind.PARAMETER) {						
			final ParameterNode pNode = (ParameterNode) node;
			final int index = pNode.getParameterIndex();
			final Parameter par = parameters[(int) index];
			final ExtName name = par.getAnnotation(ExtName.class);
			
			if (Objects.nonNull(name)) {
				builder.append(par.getAnnotation(ExtName.class).value());
				builder.append(" - ");
			}
		}

	}

	/**
	 * Close data validatior engine
	 */
	public static void releaseValidator() {
		if (Objects.nonNull(factory)) {
			factory.close();
			factory = null;
		}
	}
	
	/**
	 * Initialize data validation engine
	 */
	public static void initValidator() {
		if (Objects.nonNull(factory)) return;
		try {
			factory = Validation.buildDefaultValidatorFactory();
		} catch (Exception e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.warn(msg);
			LOG.debug(msg, e);
		}
	}

}
