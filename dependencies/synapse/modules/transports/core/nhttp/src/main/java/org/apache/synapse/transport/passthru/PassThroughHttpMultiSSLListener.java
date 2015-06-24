package org.apache.synapse.transport.passthru;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.TransportInDescription;
import org.apache.http.HttpHost;
import org.apache.synapse.transport.http.conn.Scheme;
import org.apache.synapse.transport.nhttp.config.ServerConnFactoryBuilder;

public class PassThroughHttpMultiSSLListener extends PassThroughHttpListener {

    @Override
    protected Scheme initScheme() {
        return new Scheme("https", 443, true);
    }

    @Override
    protected ServerConnFactoryBuilder initConnFactoryBuilder(
            final TransportInDescription transportIn,
            final HttpHost host) throws AxisFault {
        return new ServerConnFactoryBuilder(transportIn, host)
            .parseSSL()
            .parseMultiProfileSSL();
    }

}
