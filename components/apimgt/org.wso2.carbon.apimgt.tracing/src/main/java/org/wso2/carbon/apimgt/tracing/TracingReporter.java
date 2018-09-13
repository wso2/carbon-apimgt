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
    private final JsonFactory f = new JsonFactory();
    private final boolean includeStackTraceInStructuredLog;

    public TracingReporter(Log log, boolean includeStackTraceInStructuredLog) {
        LogFactory.getLog(this.getClass()).info("{reporter: 'init'}");
        this.log = log;
        this.includeStackTraceInStructuredLog = includeStackTraceInStructuredLog;
    }

    public void start(Instant ts, SpanData span) {
        if (this.log.isTraceEnabled()) {
            this.log.trace(this.toStructuredMessage(ts, "start", span, (Map)null));
        }

    }

    public void finish(Instant ts, SpanData span) {
        if (this.log.isTraceEnabled()) {
            this.log.trace(this.toStructuredMessage(ts, "finish", span, (Map)null));
        }

    }

    public void log(Instant ts, SpanData span, Map<String, ?> fields) {
        LogLevel level = LogLevel.INFO;

        try {
            LogLevel level0 = (LogLevel)fields.get(LogLevel.FIELD_NAME);
            if (level0 != null) {
                level = level0;
                fields.remove(LogLevel.FIELD_NAME);
            }
        } catch (Exception var6) {
//            this.log.warn("fail to read value of field {}", LogLevel.FIELD_NAME, var6);
            this.log.warn("fail to read value of field {}");
        }

        switch(level) {
            case TRACE:
                if (this.log.isTraceEnabled()) {
                    this.log.trace(this.toStructuredMessage(ts, "log", span, fields));
                }
                break;
            case DEBUG:
                if (this.log.isDebugEnabled()) {
                    this.log.debug(this.toStructuredMessage(ts, "log", span, fields));
                }
                break;
            case WARN:
                if (this.log.isWarnEnabled()) {
                    this.log.warn(this.toStructuredMessage(ts, "log", span, fields));
                }
                break;
            case ERROR:
                if (this.log.isErrorEnabled()) {
                    this.log.error(this.toStructuredMessage(ts, "log", span, fields));
                }
                break;
            default:
                if (this.log.isInfoEnabled()) {
                    this.log.info(this.toStructuredMessage(ts, "log", span, fields));
                }
        }

    }

    protected String toStructuredMessage(Instant ts, String action, SpanData span, Map<String, ?> fields) {
        try {
            StringWriter w = new StringWriter();
            JsonGenerator g = this.f.createGenerator(w);
            g.writeStartObject();
//            g.writeNumberField("ts", ts.toEpochMilli());
//            g.writeNumberField("elapsed", Duration.between(span.startAt, ts).toMillis());
            g.writeStringField("spanId", span.spanId);
            g.writeStringField("operation", span.operationName);
            g.writeStringField("action", action);
//            g.writeStringField("operation", span.operationName);
            g.writeObjectFieldStart("tags");
            Iterator var7 = span.tags.entrySet().iterator();

            Map.Entry kv;
            Object v;
            while(var7.hasNext()) {
                kv = (Map.Entry)var7.next();
                v = kv.getValue();
                if (v instanceof String) {
                    g.writeStringField((String)kv.getKey(), (String)v);
                } else if (v instanceof Number) {
                    g.writeNumberField((String)kv.getKey(), ((Number)v).doubleValue());
                } else if (v instanceof Boolean) {
                    g.writeBooleanField((String)kv.getKey(), (Boolean)v);
                }
            }

            g.writeEndObject();
            if (fields != null && !fields.isEmpty()) {
                g.writeObjectFieldStart("fields");
                var7 = fields.entrySet().iterator();

                while(var7.hasNext()) {
                    kv = (Map.Entry)var7.next();
                    v = kv.getValue();
                    if (v instanceof String) {
                        g.writeStringField((String)kv.getKey(), (String)v);
                    } else if (v instanceof Number) {
                        g.writeNumberField((String)kv.getKey(), ((Number)v).doubleValue());
                    } else if (v instanceof Boolean) {
                        g.writeBooleanField((String)kv.getKey(), (Boolean)v);
                    } else if (v instanceof Throwable) {
                        if (this.includeStackTraceInStructuredLog) {
                            try {
                                StringWriter w2 = new StringWriter();
                                Throwable var11 = null;

                                try {
                                    ((Throwable)v).printStackTrace(new PrintWriter(w2));
                                    g.writeStringField((String)kv.getKey(), w2.toString());
                                } catch (Throwable var22) {
                                    var11 = var22;
                                    throw var22;
                                } finally {
                                    if (w2 != null) {
                                        if (var11 != null) {
                                            try {
                                                w2.close();
                                            } catch (Throwable var21) {
                                                var11.addSuppressed(var21);
                                            }
                                        } else {
                                            w2.close();
                                        }
                                    }

                                }
                            } catch (Exception var24) {
                                g.writeStringField((String)kv.getKey(), String.valueOf(v));
                            }
                        } else {
                            g.writeStringField((String)kv.getKey(), String.valueOf(v));
                        }
                    } else {
                        g.writeStringField((String)kv.getKey(), String.valueOf(v));
                    }
                }

                g.writeEndObject();
            } else {
                g.writeObjectFieldStart("baggage");
                var7 = span.context().baggageItems().iterator();

                while(var7.hasNext()) {
                    kv = (Map.Entry)var7.next();
                    g.writeStringField((String)kv.getKey(), (String)kv.getValue());
                }

                g.writeEndObject();
                g.writeObjectFieldStart("references");
                var7 = span.references.entrySet().iterator();

                while(var7.hasNext()) {
                    kv = (Map.Entry)var7.next();
                    g.writeStringField((String)kv.getKey(), (String)kv.getValue());
                }

                g.writeEndObject();
            }

            g.writeEndObject();
            g.close();
            w.close();
            return w.toString();
        } catch (Exception var25) {
            var25.printStackTrace();
            return "";
        }
    }

}
