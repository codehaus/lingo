package org.agilasoft.lingo.jms;

import org.agilasoft.lingo.jms.marshall.XStreamMarshaller;

import javax.jms.JMSException;

/**
 * @version $Revision$
 */
public class JmsXStreamTest extends JmsMultiplexingRemotingTest {

    protected void configure(JmsServiceExporter exporter) throws Exception {
        exporter.setMarshaller(new XStreamMarshaller());
        super.configure(exporter);
    }

    protected void configure(JmsProxyFactoryBean pfb) throws JMSException {
        pfb.setMarshaller(new XStreamMarshaller());
        super.configure(pfb);
    }
}
