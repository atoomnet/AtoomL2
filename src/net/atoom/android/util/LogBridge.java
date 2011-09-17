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

import net.atoom.android.l2.L2Activity;
import android.util.Log;

public final class LogBridge {

	// switch to false before release
	private static final boolean doLog = true;

	public static void i(final String m) {
		if (doLog)
			Log.i(L2Activity.LOGGING_TAG, m);
	}

	public static void w(final String m) {
		if (doLog)
			Log.w(L2Activity.LOGGING_TAG, m);
	}

	public static boolean isLoggable() {
		return doLog
				&& (Log.isLoggable(L2Activity.LOGGING_TAG, Log.INFO) || Log
						.isLoggable(L2Activity.LOGGING_TAG, Log.WARN));
	}
}
