package org.wso2.carbon.apimgt.impl;

import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.mockito.Mockito;

public class AxisConfigurationMockCreator {
    private AxisConfiguration configuration;
    private TransportInDescription transportInDescription;

    public AxisConfigurationMockCreator() {
        configuration = Mockito.mock(AxisConfiguration.class);
        transportInDescription = Mockito.mock(TransportInDescription.class);
        Mockito.when(configuration.getTransportIn(Mockito.anyString())).thenReturn(transportInDescription);
    }

    public AxisConfiguration getMock() {
           return configuration;
    }

    public TransportInDescription getTransportInDescription() {
        return transportInDescription;
    }
}
