/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark.metric;

import javax.enterprise.inject.Vetoed;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.GlobalMeterProvider;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;

/**
 * Code to be injected into loaded class
 */
@Vetoed
public class WebSocketMetric {

	public static final String CLASS_NAME = "io.greenscreens.quark.websocket.WebSocketEndpoint";
	public static final String ATTR_GROUP = "websocket";
	
	private Attributes attributes;
	private LongCounter requestCounter;
	private LongCounter sessionCounter;
	private LongUpDownCounter activeSessionCounter;
	private final boolean isInit;
	
	public WebSocketMetric() {
		super();
		// attributesBuilder.put("service", "service.apm").build();
	    isInit = init(CLASS_NAME, ATTR_GROUP);
	}

	public WebSocketMetric(final String name, final String group) {
		super();
		isInit = init(name, group);
	}
	
	private boolean init (final String name, final String group) {
		boolean sts = false;
		try {
			final MeterProvider provider = GlobalMeterProvider.get();
			final Meter meter = provider.get(name);
			attributes = Attributes.builder().put("group", group).build();
			activeSessionCounter = meter.upDownCounterBuilder("gs.sessions.active").setDescription("Number of active WebSocket sessions").build();
			sessionCounter = meter.counterBuilder("gs.sessions.total").setDescription("Total number of WebSocket sessions created").build();
			requestCounter = meter.counterBuilder("gs.requests.total").setDescription("Total number of WebSocket requests created").build();
			sts = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sts;
	}
	
	/**
	 * Called when connection open
	 */
	public void onOpen() {
		if (!isInit) return;
		sessionCounter.add(1, attributes);
		activeSessionCounter.add(1, attributes);
	}
	
	/**
	 * Called when connection closed
	 */
	public void onClose() {
		if (!isInit) return;
		activeSessionCounter.add(-1, attributes);	
	}
	
	/**
	 * Called when message arrived
	 */
	public void onMessage() {
		if (!isInit) return;
		requestCounter.add(1, attributes);
	}

}
