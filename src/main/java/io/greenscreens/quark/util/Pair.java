/*
 * Copyright (C) 2015, 2024 Green Screens Ltd.
 */
package io.greenscreens.quark.util;

public class Pair<K, V> {

	K first;
	V second;
	
	public Pair(final K first, final V second) {
		super();
		this.first = first;
		this.second = second;
	}

	public K getFirst() {
		return first;
	}

	public V getSecond() {
		return second;
	}
	
	static public <K, V> Pair<K, V> create(final K first, final V second) {
		return new Pair<K, V>(first, second);
	}
	
}
