package org.wso2.carbon.apimgt.jms.listener.utils;

import org.junit.Assert;
import org.junit.Test;

public class JMSMessageListenerTest {

    @Test
    public void getSignatureIfJWT() {
        final String opaqueTokenWithNoDots = "aaaabbbcccdddeeefff";
        final String opaqueTokenWithTwoDots = "aaaa.bbbcccdddeee.fff";
        final String opaqueTokenWithMoreDots = "aaaa.bbb.ccc.ddd.eee.fff";
        final String jwtTokenWithoutSignature = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0";
        final String signature = "iOiJIUzI1NiIs3erfRd";
        final String jwtTokenWithSignature = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0." + signature;

        JMSMessageListener jmsMessageListener = new JMSMessageListener();

        //the same token should be returned
        Assert.assertEquals(opaqueTokenWithNoDots, jmsMessageListener.getSignatureIfJWT(opaqueTokenWithNoDots));
        Assert.assertEquals(opaqueTokenWithTwoDots, jmsMessageListener.getSignatureIfJWT(opaqueTokenWithTwoDots));
        Assert.assertEquals(opaqueTokenWithMoreDots, jmsMessageListener.getSignatureIfJWT(opaqueTokenWithMoreDots));
        Assert.assertEquals(jwtTokenWithoutSignature, jmsMessageListener.getSignatureIfJWT(jwtTokenWithoutSignature));

        //signature should be returned
        Assert.assertEquals(signature, jmsMessageListener.getSignatureIfJWT(jwtTokenWithSignature));
    }
}