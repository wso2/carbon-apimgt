package org.wso2.carbon.apimgt.persistence.mongodb;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

/**
 * This class provides AspectJ configurations
 */
@Aspect
public class MethodTimeLogger {

    private static final Log log = LogFactory.getLog(MongoDBConstants.CORRELATION_LOGGER);
    private static boolean isEnabled = false;
    private static boolean logAllMethods = false;
    private static boolean isSet = false;
    private static boolean isLogAllSet = false;

    /**
     * This is an AspectJ pointcut defined to apply to all methods within the package,
     * org.wso2.carbon.apimgt.gateway.handlers and consist of the annotation, @MethodStats.
     * <p>
     * Note: The annotation can be given to a class too, to enable logging for all methods of the class
     */
    @Pointcut("execution(* org.wso2.carbon.apimgt.persistence.mongodb..*(..)) " +
            "&& (@annotation(MethodStats) || @target(MethodStats))")
    public static void pointCut() {
    }

    /**
     * This is an AspectJ pointcut defined to apply to all methods within the package,
     * org.wso2.carbon.apimgt.gateway. Also, the pointcut looks for the  system property to enable
     * method time logging for all methods in this package
     *
     * @return true if the property value matches this package name
     */
    @Pointcut("execution(* *(..)) && if()")
    public static boolean pointCutAll() {
        if (!isLogAllSet) {
            String config = System.getProperty(MongoDBConstants.LOG_ALL_METHODS);
            if (StringUtils.isNotEmpty(config)) {
                logAllMethods = config.contains("org.wso2.carbon.apimgt.persistence.mongodb");
                isLogAllSet = true;
            }
        }
        return logAllMethods;
    }

    /**
     * This pointcut looks for the system property to enable/ disable timing logs
     *
     * @return true if the property value is given as true
     */
    @Pointcut("if()")
    public static boolean isConfigEnabled() {
        if (!isSet) {
            String config = System.getProperty(MongoDBConstants.ENABLE_CORRELATION_LOGS);
            if (StringUtils.isNotEmpty(config)) {
                isEnabled = Boolean.parseBoolean(config);
                isSet = true;
            }
        }
        return isEnabled;
    }

    /**
     * If the pointcuts results true, this method is invoked every time a method satisfies the
     * criteria given in the pointcut.
     *
     * @param point The JoinPoint before method execution
     * @return result of method execution
     * @throws Throwable
     */
    @Around("isConfigEnabled() && (pointCut() || pointCutAll())")
    public Object log(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) point.getSignature();
        Object result = point.proceed();
        String[] args = signature.getParameterNames();

        String argString;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        if (args != null && args.length != 0) {
            String delimiter = "";
            for (String arg : args) {
                stringBuilder.append(delimiter);
                delimiter = ", ";
                stringBuilder.append(arg);
            }
        }
        stringBuilder.append("]");
        argString = stringBuilder.toString();
        MessageContext messageContext = MessageContext.getCurrentMessageContext();
        if (MDC.get(MongoDBConstants.CORRELATION_ID) == null) {
            if (messageContext != null) {
                Map headers =
                        (Map) messageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
                if (headers != null) {
                    String correlationId = (String) headers.get(MongoDBConstants.AM_ACTIVITY_ID);
                    if (StringUtils.isNotEmpty(correlationId)) {
                        MDC.put(MongoDBConstants.CORRELATION_ID, correlationId);
                    }
                    if (StringUtils.isEmpty(MDC.get(MongoDBConstants.CORRELATION_ID))) {
                        correlationId = UUID.randomUUID().toString();
                        MDC.put(MongoDBConstants.CORRELATION_ID, correlationId);
                        headers.put(MongoDBConstants.AM_ACTIVITY_ID, correlationId);
                    }
                }
            }
        }
        log.info((System.currentTimeMillis() - start) + "|MONGODB|" +
                MethodSignature.class.cast(point.getSignature()).getDeclaringTypeName() + "|" +
                MethodSignature.class.cast(point.getSignature()).getMethod().getName() + "|" + argString);
        return result;
    }
}
