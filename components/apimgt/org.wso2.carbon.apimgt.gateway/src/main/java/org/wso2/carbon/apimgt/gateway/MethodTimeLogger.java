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

package org.wso2.carbon.apimgt.gateway;

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
    private static final Log log = LogFactory.getLog("timing");

    /**
     * This is an AspectJ pointcut defined to apply to all methods within the package,
     * org.wso2.carbon.apimgt.gateway.handlers and consist of the annotation, @MethodStats.
     * Also, the pointcut looks for the configuration to enable/ disable timing logs
     *
     * @return true if the configuration value is given as true
     */
    @Pointcut("execution(* org.wso2.carbon.apimgt.gateway.handlers..*(..)) && @annotation(MethodStats) && if()")
    public static boolean pointCut() {
        boolean enabled = false;
        String config = CarbonUtils.getServerConfiguration().getFirstProperty("EnableTimingLogs");
        if (config != null && !config.equals("")) {
            enabled = Boolean.parseBoolean(config);
        }
        return enabled;
    }

    /**
     * If the pointcut returns true, this method is invoked every time a method satisfies the
     * criteria given in the pointcut.
     *
     * @param point The JoinPoint before method execution
     * @return result of method execution
     * @throws Throwable
     */
    @Around("pointCut()")
    public Object log(ProceedingJoinPoint point) throws Throwable
    {
        long start = System.currentTimeMillis();
        Object result = point.proceed();
        log.info("className="+MethodSignature.class.cast(point.getSignature()).getDeclaringTypeName()+
                ", methodName="+MethodSignature.class.cast(point.getSignature()).getMethod().getName()+
                ",threadId="+Thread.currentThread().getId() + ", timeMs="+ (System.currentTimeMillis() - start) );
        return result;
    }
}
