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

package org.wso2.carbon.apimgt.keymgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * This class provides AspectJ configurations
 */
@Aspect
public class MethodTimeLogger
{
    private static final Log log = LogFactory.getLog("correlation");
    private static boolean isEnabled = false;
    private static boolean logAllMethods = false;
    private static boolean isSet = false;
    private static boolean isLogAllSet = false;

    /**
     * This is an AspectJ pointcut defined to apply to all methods within the package,
     * org.wso2.carbon.apimgt.impl and consist of the annotation, @MethodStats.
     *
     * Note:
     *   1. The annotation can be given to a class too, to enable logging for all methods of the class
     *   2. Here, the org.wso2.carbon.apimgt.keymgt.service method is explicitly given because annotations cannot be
     *      added to the service classes
     */
    @Pointcut("execution(* org.wso2.carbon.apimgt.keymgt.service..*(..)) || (execution(* *(..)) &&" +
            " (@annotation(MethodStats) || @target(MethodStats)))")
    public static void pointCut() {
    }

    /**
     * This is an AspectJ pointcut defined to apply to all methods within the package,
     * org.wso2.carbon.apimgt.keymgt. Also, the pointcut looks for the system property to enable
     * method time logging for all methods in this package
     *
     * @return true if the property value matches this package name
     */
    @Pointcut("execution(* *(..)) && if()")
    public static boolean pointCutAll() {
        if (!isLogAllSet) {
            String config = System.getProperty("logAllMethods");
            if (config != null && !config.equals("")) {
                logAllMethods = config.contains("org.wso2.carbon.apimgt.keymgt");
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
            String config = System.getProperty("enableCorrelationLogs");
            if (config != null && !config.equals("")) {
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
    public Object log(ProceedingJoinPoint point) throws Throwable
    {
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
        log.info((System.currentTimeMillis() - start) + "|METHOD|" + MethodSignature.class.cast(point.getSignature()).getDeclaringTypeName() +
                "|" + MethodSignature.class.cast(point.getSignature()).getMethod().getName()+ "|" + argString);
        return result;
    }
}
