package org.wso2.carbon.apimgt.keymgt;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.keymgt.issuers.AbstractScopesIssuer;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {APIKeyMgtDataHolder.class})
public class ScopesIssuerTest {
    @Test
    public void setScopes() throws Exception {
        PowerMockito.mockStatic(APIKeyMgtDataHolder.class);
        AbstractScopesIssuer mockIssuer = Mockito.mock(AbstractScopesIssuer.class);
        Map<String, AbstractScopesIssuer> scopesIssuerMap = new HashMap<String, AbstractScopesIssuer>();
        scopesIssuerMap.put("wso2", mockIssuer);
        BDDMockito.given(APIKeyMgtDataHolder.getScopesIssuers()).willReturn(scopesIssuerMap);
        ScopesIssuer.loadInstance(Collections.<String>emptyList());
        ScopesIssuer scopesIssuer = ScopesIssuer.getInstance();
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(new OAuth2AccessTokenReqDTO());
        tokReqMsgCtx.setScope(new String[] {"wso2:a"});
        scopesIssuer.setScopes(tokReqMsgCtx);
    }
    @Test
    public void setDefaultScopes() throws Exception {
        PowerMockito.mockStatic(APIKeyMgtDataHolder.class);
        AbstractScopesIssuer mockIssuer = Mockito.mock(AbstractScopesIssuer.class);
        Map<String, AbstractScopesIssuer> scopesIssuerMap = new HashMap<String, AbstractScopesIssuer>();
        scopesIssuerMap.put("wso2", mockIssuer);
        BDDMockito.given(APIKeyMgtDataHolder.getScopesIssuers()).willReturn(scopesIssuerMap);
        ScopesIssuer.loadInstance(Collections.<String>emptyList());
        ScopesIssuer scopesIssuer = ScopesIssuer.getInstance();
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(new OAuth2AccessTokenReqDTO());
        scopesIssuer.setScopes(tokReqMsgCtx);
    }
    @Test
    public void testNoScopeIssuers() throws Exception {
        PowerMockito.mockStatic(APIKeyMgtDataHolder.class);
        Map<String, AbstractScopesIssuer> scopesIssuerMap = new HashMap<String, AbstractScopesIssuer>();
        BDDMockito.given(APIKeyMgtDataHolder.getScopesIssuers()).willReturn(scopesIssuerMap);
        ScopesIssuer.loadInstance(Collections.<String>emptyList());
        ScopesIssuer scopesIssuer = ScopesIssuer.getInstance();
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(new OAuth2AccessTokenReqDTO());
        scopesIssuer.setScopes(tokReqMsgCtx);
    }
    @Test
    public void testNoScopeAssigned() throws Exception {
        PowerMockito.mockStatic(APIKeyMgtDataHolder.class);
        AbstractScopesIssuer mockIssuer = Mockito.mock(AbstractScopesIssuer.class);
        Map<String, AbstractScopesIssuer> scopesIssuerMap = new HashMap<String, AbstractScopesIssuer>();
        scopesIssuerMap.put("default", mockIssuer);
        BDDMockito.given(APIKeyMgtDataHolder.getScopesIssuers()).willReturn(scopesIssuerMap);
        ScopesIssuer.loadInstance(Collections.<String>emptyList());
        ScopesIssuer scopesIssuer = ScopesIssuer.getInstance();
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(new OAuth2AccessTokenReqDTO());
        tokReqMsgCtx.setScope(new String[] {"wso2:a"});
        scopesIssuer.setScopes(tokReqMsgCtx);
    }

}