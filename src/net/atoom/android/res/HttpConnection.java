/**
 *    Copyright 2009 Bram de Kruijff <bdekruijff [at] gmail [dot] com>
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
package net.atoom.android.res;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.atoom.android.l2.L2Activity;
import net.atoom.android.util.LogBridge;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public final class HttpConnection {

	private static final int HTTP_OK = 200;
	private static final String ETAG_HEADER = "ETag";

	private final DefaultHttpClient myHttpClient = new DefaultHttpClient();

	public HttpConnection() {
	}

	public boolean loadResource(ResourceEntity resourceEntity) {
		if (LogBridge.isLoggable())
			LogBridge.i("HttpConnection : loadResource : started: " + resourceEntity.toString());
		HttpGet httpUriRequest = new HttpGet(resourceEntity.getUri());
		try {
			HttpResponse httpResponse = myHttpClient.execute(httpUriRequest);

			// this happens...
			if (httpResponse.getStatusLine().getStatusCode() != HTTP_OK) {
				if (LogBridge.isLoggable())
					LogBridge.w("HttpConnection : loadResource : failed: " + httpResponse.getStatusLine().toString());
				// set status on entity?
				return false;
			}

			Header header = httpResponse.getFirstHeader(ETAG_HEADER);
			if (header != null) {
				resourceEntity.setETag(header.getValue());
				if (LogBridge.isLoggable())
					LogBridge.i("HttpConnection : loadResource : eTag : " + resourceEntity.getETag());
			}

			Header header2 = httpResponse.getFirstHeader("Expires");
			if (header2 != null) {
				SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
				Date d;
				try {
					d = format.parse(header2.getValue());
					resourceEntity.setExpires(d.getTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}

			HttpEntity httpEntity = httpResponse.getEntity();
			OutputStream os = resourceEntity.getOutputStream();
			httpEntity.writeTo(os);
			os.close();

			if (LogBridge.isLoggable())
				LogBridge.i("HttpConnection : loadResource : completed : " + resourceEntity.toString());
			return true;
		} catch (ClientProtocolException e) {
			Log.w(L2Activity.LOGGING_TAG, "HttpConnection : loadResource : failed: " + resourceEntity.toString(), e);
		} catch (IOException e) {
			Log.w(L2Activity.LOGGING_TAG, "HttpConnection : loadResource : failed: " + resourceEntity.toString(), e);
		}
		return false;
	}

	public boolean isModified(ResourceEntity resourceEntity) {
		if (LogBridge.isLoggable())
			LogBridge.i("HttpConnection : isModified : started: " + resourceEntity.toString());
		HttpHead httpUriRequest = new HttpHead(resourceEntity.getUri());
		try {
			HttpResponse httpResponse = myHttpClient.execute(httpUriRequest);
			if (httpResponse.getStatusLine().getStatusCode() != HTTP_OK) {
				if (LogBridge.isLoggable())
					LogBridge.i("HttpConnection : isModified : failed: " + httpResponse.getStatusLine().toString()
							+ " : " + resourceEntity.toString());
				return false;
			}
			Header eTagHeader = httpResponse.getFirstHeader(ETAG_HEADER);
			if (eTagHeader != null && eTagHeader.getValue().equals(resourceEntity.getETag())) {
				if (LogBridge.isLoggable())
					LogBridge
							.i("HttpConnection : isModified : completed : NOT MODIFIED : " + resourceEntity.toString());
				return true;
			}
			if (LogBridge.isLoggable())
				LogBridge.i("HttpConnection : isModified : completed : MODIFIED : " + resourceEntity.toString());
		} catch (ClientProtocolException e) {
			Log.w(L2Activity.LOGGING_TAG, "HttpConnection : isModified : failed: " + resourceEntity.toString(), e);
		} catch (IOException e) {
			Log.w(L2Activity.LOGGING_TAG, "HttpConnection : isModified : failed: " + resourceEntity.toString(), e);
		}
		return false;
	}
}