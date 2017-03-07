package org.wso2.carbon.apimgt.impl.clients;

/**
 * Created by mushthaq on 11/1/16.
 */
import org.apache.axis2.AxisFault;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.AuthenticationException;
import org.wso2.carbon.apimgt.impl.internal.ServiceAuthenticator;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityExceptionException;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceStub;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionIdsDTO;
import org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO;
import org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO;

import java.rmi.RemoteException;

public class UserInformationRecoveryClient {

    private UserInformationRecoveryServiceStub stub;

    public UserInformationRecoveryClient() throws APIManagementException {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String serverUrl = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
        String username = config.getFirstProperty("AuthManager.Username");
        String password = config.getFirstProperty("AuthManager.Password");

        if (serverUrl == null) {
            throw new APIManagementException("Required connection details for the key management server not provided");
        }

        try {
            stub = new UserInformationRecoveryServiceStub(serverUrl + APIConstants.USER_INFO_RECOVERY_SERVICE);

            ServiceAuthenticator authenticator = ServiceAuthenticator.getInstance();
            authenticator.setAccessUsername(username);
            authenticator.setAccessPassword(password);
            authenticator.authenticate(stub._getServiceClient());
        } catch (AxisFault axisFault) {
            throw new APIManagementException("Error while initializing the User Information Recovery "
                    + "admin service stub", axisFault);
        } catch (AuthenticationException authEx) {
            throw new APIManagementException("Error while authenticating admin service client", authEx);
        }

    }


    public CaptchaInfoBean generateCaptcha() throws AxisFault {

        CaptchaInfoBean bean = null;

        try {
            bean = stub.getCaptcha();
        } catch (RemoteException e) {
            throw new AxisFault("Error getting captcha " + e.getMessage());
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            throw new AxisFault("Error getting captcha " + e.getMessage());
        }
        return bean;
    }

    public VerificationBean VerifyUser(String username) {

        VerificationBean bean = null;
        CaptchaInfoBean captcha = new CaptchaInfoBean();

        try {
            bean = stub.verifyUser(username, captcha);
        } catch (RemoteException e) {
            bean = new VerificationBean();
            bean.setVerified(false);
            bean.setError(e.getMessage());
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            bean = new VerificationBean();
            bean.setVerified(false);
            bean.setError(e.getMessage());
        }

        return bean;
    }

    public VerificationBean sendRecoveryNotification(String username, String key, String notificationType) {

        VerificationBean bean = null;

        try {
            bean = stub.sendRecoveryNotification(username, key, notificationType);
        } catch (RemoteException e) {
            bean = new VerificationBean();
            bean.setVerified(false);
            bean.setError(e.getMessage());
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            bean = new VerificationBean();
            bean.setVerified(false);
            bean.setError(e.getMessage());
        }
        return bean;
    }

    public VerificationBean verifyConfirmationCode(String username, String code,
            CaptchaInfoBean captcha) {

        VerificationBean bean = null;

        try {
            bean = stub.verifyConfirmationCode(username, code, captcha);
        } catch (RemoteException e) {
            bean = new VerificationBean();
            bean.setVerified(false);
            bean.setError(e.getMessage());
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            bean = new VerificationBean();
            bean.setVerified(false);
            bean.setError(e.getMessage());
        }

        return bean;
    }

    public VerificationBean resetPassword(String username, String confirmationCode,
            String newPassword) {

        VerificationBean bean = null;

        try {
            bean = stub.updatePassword(username, confirmationCode, newPassword);
        } catch (RemoteException e) {
            bean = new VerificationBean();
            bean.setVerified(false);
            bean.setError(e.getMessage());
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            bean = new VerificationBean();
            bean.setVerified(false);
            bean.setError(e.getMessage());
        }

        return bean;
    }

    public ChallengeQuestionIdsDTO getChallengeQuestionIds(String username,
            String confirmationCode) {

        ChallengeQuestionIdsDTO bean = null;

        try {
            bean = stub.getUserChallengeQuestionIds(username, confirmationCode);
        } catch (RemoteException e) {
            bean = new ChallengeQuestionIdsDTO();
            bean.setError(e.getMessage());
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            bean = new ChallengeQuestionIdsDTO();
            bean.setError(e.getMessage());
        }

        return bean;
    }

    public UserChallengesDTO getChallengeQuestion(String username, String code, String id) {

        UserChallengesDTO bean = null;

        try {
            bean = stub.getUserChallengeQuestion(username, code, id);
        } catch (RemoteException e) {
            bean = new UserChallengesDTO();
            bean.setError(e.getMessage());
            bean.setVerfied(false);
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            bean = new UserChallengesDTO();
            bean.setError(e.getMessage());
            bean.setVerfied(false);
        }

        return bean;
    }

    public VerificationBean checkAnswer(String username, String code, String id, String answer) {

        VerificationBean bean = null;

        try {
            bean = stub.verifyUserChallengeAnswer(username, code, id, answer);
        } catch (RemoteException e) {
            bean = new VerificationBean();
            bean.setVerified(false);
            bean.setError(e.getMessage());
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            bean = new VerificationBean();
            bean.setVerified(false);
            bean.setError(e.getMessage());
        }

        return bean;
    }

    public UserIdentityClaimDTO[] getUserIdentitySupportedClaims(String dialect) throws AxisFault{
        UserIdentityClaimDTO[] cliams = null;
        try {
            cliams = stub.getUserIdentitySupportedClaims(dialect);
        } catch (RemoteException e) {
            throw new AxisFault("Error getting claims "  + e.getMessage());
        } catch (UserInformationRecoveryServiceIdentityExceptionException e) {
            throw new AxisFault("Error getting claims "  + e.getMessage());
        }
        return cliams;
    }

    public VerificationBean verifyAccount(UserIdentityClaimDTO[] claims, CaptchaInfoBean captcha,
            String tenantDomain) throws RemoteException {
        VerificationBean bean = null;
        try {
            bean = stub.verifyAccount(claims, captcha, tenantDomain);
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            bean = new VerificationBean();
            bean.setVerified(false);
            bean.setError(e.getMessage());
        }
        return bean;
    }

    public VerificationBean registerUser(String userName, String password,
            UserIdentityClaimDTO[] claims, String profileName,
            String tenantDomain) {
        VerificationBean bean = null;
        try {
            bean = stub.registerUser(userName, password, claims, profileName, tenantDomain);
        } catch (RemoteException e) {
            bean = new VerificationBean();
            bean.setVerified(false);
            bean.setError(e.getMessage());
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            bean = new VerificationBean();
            bean.setVerified(false);
            bean.setError(e.getMessage());
        }
        return bean;
    }

    public VerificationBean confirmUserSelfRegistration
            (String userName, String code, CaptchaInfoBean captcha, String tenantDomain)
            throws RemoteException {

        VerificationBean bean = null;
        try {
            bean = stub.confirmUserSelfRegistration(userName, code, captcha, tenantDomain);
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            bean = new VerificationBean();
            bean.setVerified(false);
            bean.setError(e.getMessage());
        }
        return bean;
    }
}
