package org.wso2.carbon.apimgt.tracing;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.opentracing.contrib.reporter.LogLevel;
import io.opentracing.contrib.reporter.Reporter;
import io.opentracing.contrib.reporter.SpanData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;

public class TracingReporter implements Reporter {

    private final Log log;
    private final JsonFactory jsonFactory = new JsonFactory();
    private final boolean includeStackTraceInStructuredLog;

    public TracingReporter(Log log, boolean includeStackTraceInStructuredLog) {
        this.log = log;
        this.includeStackTraceInStructuredLog = includeStackTraceInStructuredLog;
    }

    public void start(Instant timeStamp, SpanData span) {

    }

    public void finish(Instant timeStamp, SpanData span) {
        if (this.log.isTraceEnabled()) {
            this.log.trace(this.toStructuredMessage(timeStamp, "finish", span));
        }
    }

    public void log(Instant timeStamp, SpanData span, Map<String, ?> fields) {

        LogLevel level = LogLevel.INFO;
        try {
            LogLevel level0 = (LogLevel)fields.get(LogLevel.FIELD_NAME);
            if (level0 != null) {
                level = level0;
                fields.remove(LogLevel.FIELD_NAME);
            }
        } catch (Exception e) {
            this.log.warn("fail to read value of field {}", e);
        }

        switch(level) {
            case TRACE:
                if (this.log.isTraceEnabled()) {
                    this.log.trace(this.toStructuredMessage(timeStamp, "log", span));
                }
                break;
            case DEBUG:
                if (this.log.isDebugEnabled()) {
                    this.log.debug(this.toStructuredMessage(timeStamp, "log", span));
                }
                break;
            case WARN:
                if (this.log.isWarnEnabled()) {
                    this.log.warn(this.toStructuredMessage(timeStamp, "log", span));
                }
                break;
            case ERROR:
                if (this.log.isErrorEnabled()) {
                    this.log.error(this.toStructuredMessage(timeStamp, "log", span));
                }
                break;
            default:
                if (this.log.isInfoEnabled()) {
                    this.log.info(this.toStructuredMessage(timeStamp, "log", span));
                }
        }

    }

    protected String toStructuredMessage(Instant timeStamp, String action, SpanData span) {
        try {
            StringWriter writer = new StringWriter();
            JsonGenerator generator = this.jsonFactory.createGenerator(writer);
            generator.writeStartObject();
            generator.writeNumberField("Latency", Duration.between(span.startAt, timeStamp).toMillis());
            generator.writeStringField("Operation", span.operationName);
            generator.writeStringField("Action", action);
            generator.writeObjectFieldStart("Tags");
            Iterator itr = span.tags.entrySet().iterator();

            Map.Entry map;
            Object value;
            while(itr.hasNext()) {
                map = (Map.Entry)itr.next();
                value = map.getValue();
                if (value instanceof String) {
                    generator.writeStringField((String)map.getKey(), (String)value);
                } else if (value instanceof Number) {
                    generator.writeNumberField((String)map.getKey(), ((Number)value).doubleValue());
                } else if (value instanceof Boolean) {
                    generator.writeBooleanField((String)map.getKey(), (Boolean)value);
                }
            }
            generator.writeEndObject();
            generator.close();
            writer.close();
            return writer.toString();
        } catch (Exception e) {
            log.error("Error in structured message");
            return "";
        }
    }

}
