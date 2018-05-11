

import ballerina /io;


GatewayConf gatewayConf = new;
public type GatewayConf object {
    private {
        KeyManagerConf keyManagerConf;
    }
    public function getGatewayConf() returns (GatewayConf) {
        return gatewayConf;
    }

    public function setKeyManagerConf(KeyManagerConf keyManagerConf) {
        gatewayConf.keyManagerConf = keyManagerConf;
    }
    public function getKeyManagerConf() returns(KeyManagerConf) {
        return gatewayConf.keyManagerConf;
    }

};

   public function getGatewayConfInstance() returns (GatewayConf) {
       return gatewayConf;
   }
