package com.intellect.handlers;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.MessageContext;

public class MIReportingHandler extends AbstractHandler {

	// Some Global Variables
	private static final Logger logger = LoggerFactory.getLogger(MIReportingHandler.class);
	MIReportingUtility utility = new MIReportingUtility();
	HashMap<String, Object> elements = new HashMap<>();

	// Handling request made for consent creation
	@Override
	public boolean handleRequest(MessageContext context) {
		// Getting Axis2MessageContext
		org.apache.axis2.context.MessageContext a2mContext = utility.getAxis2MessageContext(context);

		try {

			// Decalred local variables

			HashMap<String, Object> Headers = utility.getHeaders(a2mContext);
			Long backendstarttime = Instant.now().toEpochMilli();
			Long req_exq_time = Long.parseLong(utility.getReqResRecvdTime(a2mContext).toString());
			Long timestamp = Instant.now().getEpochSecond();
			JSONObject clientdetails = null;
			String link = null;

			// Getting Additional values for db insertion

			Map TRANSPORT_HEADERS = (Map) a2mContext.getProperty("TRANSPORT_HEADERS");
			elements.put("headers_size", ((new JSONObject(TRANSPORT_HEADERS)).toString().getBytes("utf-8").length));
			logger.debug("headers size TH " + elements.get("headers_size"));
			if (Headers.get("AccessToken") != null) {
				clientdetails = utility.getClientDetails(Headers.get("AccessToken").toString());
				elements.put("client_id",
						(clientdetails.getJSONObject("data").getJSONObject("list").get("CONSUMER_KEY")));
				elements.put("tpp_id", (clientdetails.getJSONObject("data").getJSONObject("list").get("tppid")));
			}
			if (Headers.get("Context") != null)
				link = Headers.get("Context").toString();

			elements.put("request_exq_start_time", req_exq_time);
			elements.put("timestamp", timestamp);
			elements.put("backend_req_start_time", backendstarttime);
			if (Headers.get("User-Agent") != null)
				elements.put("user_agent", Headers.get("User-Agent").toString());
			if (Headers.get("http_method") != null)
				elements.put("http_method", Headers.get("http_method"));
			if (link.contains("v3.1")) {
				elements.put("api_spec_version", "v3.1");
			} else if (link.contains("v3.0")) {
				elements.put("api_spec_version", "v3.0");
			} else if (link.contains("v2.0")) {
				elements.put("api_spec_version", "v2.0");
			}

			// Inserting Payment type and sorce
			if (link.contains("pisp")) {
				if (link.contains("domestic-payment-consents")) {
					elements.put("elected_resource", "/domestic-payment-consents");
					elements.put("payment_type", "domestic-payment-consents");
				} else if (link.contains("domestic-scheduled-payment-consents")) {
					elements.put("elected_resource", "/domestic-scheduled-payment-consents");
					elements.put("payment_type", "domestic-scheduled-payment-consents");
				} else if (link.contains("domestic-standing-order-consents")) {
					elements.put("elected_resource", "/domestic-standing-order-consents");
					elements.put("payment_type", "domestic-standing-order-consents");
				} else if (link.contains("file-payment-consents")) {
					elements.put("elected_resource", "/file-payment-consents");
					elements.put("payment_type", "file-payment-consents");
				} else if (link.contains("international-payment-consents")) {
					elements.put("elected_resource", "/international-payment-consents");
					elements.put("payment_type", "international-payment-consents");
				}
				elements.put("api_name", "PaymentInitiationAPI");
				elements.put("request_type", "payments");

			} else if (link.contains("AccountsInfoAPI")) {
				if (link.contains("account-access-consents")) {
					elements.put("elected_resource", "/account-access-consents");
				}
				elements.put("request_type", "accounts");
				elements.put("api_name", "AccountsInformationAPI");
			} else if (link.contains("cbpii")) {
				if (link.contains("funds-confirmation-consents")) {
					elements.put("elected_resource", "/funds-confirmation-consents");
				}
				elements.put("api_name", "ConfirmationOfFundsAPI");
				elements.put("request_type", "confirmationoffunds");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	// Handling the response from the api
	@Override
	public boolean handleResponse(MessageContext context) {
		org.apache.axis2.context.MessageContext a2mContext = utility.getAxis2MessageContext(context);
		try {

			// local declared variables
			Long backend_end_time = Long.parseLong(utility.getReqResRecvdTime(a2mContext).toString());
			Long latency = backend_end_time - (long) elements.get("backend_req_start_time");
			JSONObject jsonBody = utility.getBody(a2mContext);
			HashMap<String, Object> Headers = utility.getHeaders(a2mContext);

			// Inserting Additional Values in the Map

			elements.put("backend_req_end_time", backend_end_time);
			elements.put("backend_latency", latency);
			elements.put("consent_id", jsonBody.getJSONObject("Data").getString("ConsentId"));
			elements.put("authorisation_status", jsonBody.getJSONObject("Data").getString("Status"));
			elements.put("response_payload_size",
					(int) elements.get("headers_size") + jsonBody.toString().getBytes("utf-8").length);
			elements.put("status_code", Headers.get("Status_Code"));
			elements.put("status_message", Headers.get("SC_Desc"));
			elements.put("multi_authorisation", 0);
			elements.put("local_instrument", null);
			elements.put("multi_auth_users", 0);

			//Getting Payment Details from PayLoad
			if (elements.get("api_name") == "PaymentInitiationAPI") {
				elements= utility.getPaymentDetails(jsonBody, elements);
			}

			JSONObject insertbody = new JSONObject(elements);
			utility.ConsentCreationInsertionAPIInvocation(insertbody);
			utility.ConsentCreationInsertionAPIAuth(insertbody);

			if (elements.get("api_name") == "AccountsInformationAPI") {
				utility.ConsentCreationInsertionAccounts(insertbody);
			} else if (elements.get("api_name") == "PaymentInitiationAPI") {
				utility.ConsentCreationInsertionPayments(insertbody);
			} else if (elements.get("api_name") == "ConfirmationOfFundsAPI") {
				utility.ConsentCreationInsertionCOF(insertbody);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}
}
