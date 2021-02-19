/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.tracing;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.opentracing.contrib.reporter.LogLevel;
import io.opentracing.contrib.reporter.Reporter;
import io.opentracing.contrib.reporter.SpanData;
import org.apache.commons.logging.Log;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;

/**
 * This class used to generate log tracer related logs.
 */
public class TracingReporter implements Reporter {

    private final Log log;
    private final JsonFactory jsonFactory = new JsonFactory();

    public TracingReporter(Log log) {
        this.log = log;
    }

    public void start(Instant timeStamp, SpanData span) {
    }

    public void finish(Instant timeStamp, SpanData span) {
        if (this.log.isTraceEnabled()) {
            this.log.trace(this.toStructuredMessage(timeStamp, span));
        }
    }

    public void log(Instant timeStamp, SpanData span, Map<String, ?> fields) {
        LogLevel level = LogLevel.INFO;
            LogLevel logLevel = (LogLevel) fields.get(LogLevel.FIELD_NAME);
            if (logLevel != null) {
                level = logLevel;
                fields.remove(LogLevel.FIELD_NAME);
            }
        switch (level) {
            case TRACE:
                if (log.isTraceEnabled()) {
                    log.trace(toStructuredMessage(timeStamp, span));
                }
                break;
            case DEBUG:
                if (log.isDebugEnabled()) {
                    log.debug(toStructuredMessage(timeStamp, span));
                }
                break;
            case WARN:
                if (log.isWarnEnabled()) {
                    log.warn(toStructuredMessage(timeStamp, span));
                }
                break;
            case ERROR:
                if (log.isErrorEnabled()) {
                    log.error(toStructuredMessage(timeStamp, span));
                }
                break;
            default:
                if (log.isInfoEnabled()) {
                    log.info(toStructuredMessage(timeStamp, span));
                }
        }
    }

    /**
     * Get the structured log message format
     *
     * @param timeStamp timeStamp Instant
     * @param span      opentracing SpanData
     * @return structured log message format String
     * */
    private String toStructuredMessage(Instant timeStamp, SpanData span) {
        try {
            StringWriter writer = new StringWriter();
            JsonGenerator generator = this.jsonFactory.createGenerator(writer);
            generator.writeStartObject();
            generator.writeNumberField(TracingConstants.LATENCY, Duration.between(span.startAt, timeStamp).toMillis());
            generator.writeStringField(TracingConstants.OPERATION_NAME, span.operationName);
            generator.writeObjectFieldStart(TracingConstants.TAGS);
            Iterator itr = span.tags.entrySet().iterator();

            Map.Entry map;
            Object value;
            while (itr.hasNext()) {
                map = (Map.Entry) itr.next();
                value = map.getValue();
                if (value instanceof String) {
                    generator.writeStringField((String) map.getKey(), (String) value);
                } else if (value instanceof Number) {
                    generator.writeNumberField((String) map.getKey(), ((Number) value).doubleValue());
                } else if (value instanceof Boolean) {
                    generator.writeBooleanField((String) map.getKey(), (Boolean) value);
                }
            }
            generator.writeEndObject();
            generator.close();
            writer.close();
            return writer.toString();
        } catch (IOException e) {
            log.error("Error in structured message" , e);
            return null;
        }
    }

}
