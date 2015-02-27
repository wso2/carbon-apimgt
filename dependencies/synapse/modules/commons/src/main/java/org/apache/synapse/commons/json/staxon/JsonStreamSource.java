/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.synapse.commons.json.staxon;

import de.odysseus.staxon.json.stream.JsonStreamToken;

import java.io.Closeable;
import java.io.IOException;

import javax.xml.stream.Location;

/**
 * JSON stream source.
 */
interface JsonStreamSource extends Closeable, Location {
	/**
	 * Represents a simple value.
	 */
	public static class Value {
		public final String text;
		public final Object data;

		private Value(String text, Object data) {
			this.text = text;
			this.data = data;
		}

		/**
		 * Create number value
		 * @param text
		 * @param number
		 */
		public Value(String text, Number number) {
			this(text, (Object) number);
		}

		/**
		 * Create string value
		 * @param text
		 */
		public Value(String text) {
			this(text, text);
		}

		@Override
		public String toString() {
			return text == null ? "null" : text;
		}
	}

	/**
	 * "true" value
	 */
	public static final Value TRUE = new Value("true", Boolean.TRUE);

	/**
	 * "false" value
	 */
	public static final Value FALSE = new Value("false", Boolean.FALSE);

	/**
	 * "null" value
	 */
	public static final Value NULL = new Value(null, null);

	/**
	 * Consume {@link de.odysseus.staxon.json.stream.JsonStreamToken#NAME} token.
	 * @return name
	 * @throws IOException
	 */
	public String name() throws IOException;

	/**
	 * Consume {@link de.odysseus.staxon.json.stream.JsonStreamToken#VALUE} token.
	 * @return value
	 * @throws IOException
	 */
	public Value value() throws IOException;

	/**
	 * Consume {@link de.odysseus.staxon.json.stream.JsonStreamToken#START_OBJECT} token.
	 * @throws IOException
	 */
	public void startObject() throws IOException;

	/**
	 * Consume {@link de.odysseus.staxon.json.stream.JsonStreamToken#END_OBJECT} token.
	 * @throws IOException
	 */
	public void endObject() throws IOException;

	/**
	 * Consume {@link de.odysseus.staxon.json.stream.JsonStreamToken#START_ARRAY} token.
	 * @throws IOException
	 */
	public void startArray() throws IOException;

	/**
	 * Consume {@link de.odysseus.staxon.json.stream.JsonStreamToken#END_ARRAY} token.
	 * @throws IOException
	 */
	public void endArray() throws IOException;

	/**
	 * Peek next token.
	 * @return token
	 * @throws IOException
	 */
	public JsonStreamToken peek() throws IOException;
}
