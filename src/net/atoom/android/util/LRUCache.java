/**
 *    Copyright 2010 Bram de Kruijff <bdekruijff [at] gmail [dot] com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.atoom.android.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> {

	private static final float HASHTABLE_LOADFACTOR = 0.75f;

	private final LinkedHashMap<K, V> myMap;
	private final int myCacheSize;

	public LRUCache(final int cacheSize) {
		myCacheSize = cacheSize;
		int hashTableCapacity = (int) Math.ceil(cacheSize
				/ HASHTABLE_LOADFACTOR) + 1;
		myMap = new LinkedHashMap<K, V>(hashTableCapacity,
				HASHTABLE_LOADFACTOR, true) {

			private static final long serialVersionUID = -6745644057056287453L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
				return size() > LRUCache.this.myCacheSize;
			}
		};
	}

	public synchronized V get(K key) {
		return myMap.get(key);
	}

	public synchronized void put(K key, V value) {
		myMap.put(key, value);
	}

	public synchronized void remove(K key) {
		myMap.remove(key);
	}
}