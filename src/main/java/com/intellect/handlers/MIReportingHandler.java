package com.intellect.handlers;

import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;

import org.apache.axiom.soap.*;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class MIReportingHandler extends AbstractSynapseHandler {

	private static Logger logger = LoggerFactory.getLogger(MIReportingHandler.class);

	@Override
	public boolean handleRequestInFlow(MessageContext synContext) {

		try {

			SOAPEnvelope envelope = synContext.getEnvelope();
			SOAPHeader header = envelope.getHeader();
			SOAPBody soapBody = envelope.getBody();

			logger.debug("\n\n header and body is  " + header + soapBody + " \n\n");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public boolean handleRequestOutFlow(MessageContext synContext) {

		logger.debug("\n\n MESSAGECONTEXT >>>> " + synContext + "\n\n");

		logger.debug(" \n\n MessageContext >>>  " + synContext.getEnvelope().toString());

		return true;
	}

	@Override
	public boolean handleResponseInFlow(MessageContext synContext) {

		logger.debug("\n\n MessageContext >>>> " + synContext + "\n\n");

		logger.debug(" \n\n MessageContext >>>  " + synContext.getEnvelope().toString());

		return true;
	}

	@Override
	public boolean handleResponseOutFlow(MessageContext synContext) {

		logger.debug("\n\n MessageContext >>>> " + synContext + "\n\n");

		logger.debug(" \n\n MessageContext >>>  " + synContext.getEnvelope().toString());

		return true;
	}

}
