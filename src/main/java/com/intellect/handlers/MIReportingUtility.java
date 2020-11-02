package com.intellect.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import org.json.*;
import org.apache.commons.io.IOUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.synapse.transport.passthru.SourceRequest;
import org.apache.synapse.transport.passthru.util.RelayUtils;

//utility for MI Reporting Handler

public class MIReportingUtility {

    private static final Logger logger = LoggerFactory.getLogger(MIReportingUtility.class);

    public String getProps(String property) {
        Properties props = new Properties();
        String url = "";
        try {
            ClassLoader classLoader = getClass().getClassLoader();

            InputStream inputStream = classLoader.getResourceAsStream("handler.properties");
            props.load(inputStream);

            logger.debug("props are : " + props.getProperty(property));

            url = props.getProperty(property);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }

    // Getting Axis2MessageContext
    public org.apache.axis2.context.MessageContext getAxis2MessageContext(MessageContext context) {
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) context)
                .getAxis2MessageContext();
        return axis2MessageContext;
    }

    // Getting Headers from MessageContext
    public HashMap<String, Object> getHeaders(org.apache.axis2.context.MessageContext a2mContext)
            throws UnsupportedEncodingException {

        HashMap<String, Object> Headers = new HashMap<>();

        Map TRANSPORT_HEADERS = (Map) a2mContext.getProperty("TRANSPORT_HEADERS");

        SourceRequest sRequest = (SourceRequest) a2mContext.getProperty("pass-through.Source-Request");

        if (TRANSPORT_HEADERS.get("Access-Control-Request-Method") != null) {
            Headers.put("http_method", TRANSPORT_HEADERS.get("Access-Control-Request-Method"));
        } else if (sRequest != null) {
            logger.debug("headers : " + ((new JSONObject(sRequest.getHeaders())).toString().getBytes("utf-8")).length);
            Headers.put("http_method", sRequest.getMethod());
        }

        Headers.put("User-Agent", TRANSPORT_HEADERS.get("User-Agent"));
        if (a2mContext.getProperty("HTTP_SC") != null)
            Headers.put("Status_Code", a2mContext.getProperty("HTTP_SC"));
        if (a2mContext.getProperty("HTTP_SC_DESC") != null)
            Headers.put("SC_Desc", a2mContext.getProperty("HTTP_SC_DESC"));

        Headers.put("Context", a2mContext.getProperty("TransportInURL"));

        if (TRANSPORT_HEADERS.get("Authorization") != null)
            Headers.put("AccessToken", (TRANSPORT_HEADERS.get("Authorization").toString()).substring(7));

        return Headers;

    }

    // Getting request and response payload body
    public JSONObject getBody(org.apache.axis2.context.MessageContext a2mContext) {

        try {
            RelayUtils.buildMessage(a2mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        InputStream jsonPaylodStream = (InputStream) a2mContext
                .getProperty("org.apache.synapse.commons.json.JsonInputStream");
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(jsonPaylodStream, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String payloadMessage = writer.toString();
        JSONObject jsonPayload = new JSONObject(payloadMessage);
        return jsonPayload;
    }

    // Getting Request and Response excecution Time
    public String getReqResRecvdTime(org.apache.axis2.context.MessageContext a2mContext) {
        return (String) a2mContext.getProperty("wso2statistics.request.received.time");
    }

    // DB Insertion for Api Invocation
    public void ConsentCreationInsertionAPIInvocation(JSONObject insertJsonObject) {

        logger.debug("Request of insertion is in ConsentCreationInsertionAPIInvocation : " + insertJsonObject);

        try {
            URL posturl = new URL(getProps("API_INVOCATION_RAW_DATA"));
            HttpURLConnection postConnection = (HttpURLConnection) posturl.openConnection();
            postConnection.setRequestMethod("POST");
            postConnection.setRequestProperty("Content-Type", "application/json");
            postConnection.setDoOutput(true);
            OutputStream outputStream = postConnection.getOutputStream();
            outputStream.write(insertJsonObject.toString().getBytes());
            outputStream.flush();
            outputStream.close();
            int responsecode = postConnection.getResponseCode();
            logger.debug("insetion response code" + responsecode);
            if (responsecode == 200 || responsecode == 202 || responsecode == 201)
                logger.debug("insertion was successfull : API_INVOCATION_RAW_DATA");
            else
                logger.debug("insertion was unsuccessfull : API_INVOCATION_RAW_DATA");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // DB Insertion for Api Authorisation
    public void ConsentCreationInsertionAPIAuth(JSONObject insertJsonObject) {

        insertJsonObject.put("user_id", "null");
        logger.debug("Request of insertion is ConsentCreationInsertionAPIAuth : " + insertJsonObject);

        try {
            URL posturl = new URL(getProps("AUTHORIZATION_RAW_DATA"));
            HttpURLConnection postConnection = (HttpURLConnection) posturl.openConnection();
            postConnection.setRequestMethod("POST");
            postConnection.setRequestProperty("Content-Type", "application/json");
            postConnection.setDoOutput(true);
            OutputStream outputStream = postConnection.getOutputStream();
            outputStream.write(insertJsonObject.toString().getBytes());
            outputStream.flush();
            outputStream.close();
            int responsecode = postConnection.getResponseCode();
            logger.debug("insetion response code" + responsecode);
            if (responsecode == 200 || responsecode == 202 || responsecode == 201)
                logger.debug("insertion was successfull : AUTHORIZATION_RAW_DATA");
            else
                logger.debug("insertion was unsuccessfull : AUTHORIZATION_RAW_DATA");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // DB Insertion for Api Accounts
    public void ConsentCreationInsertionAccounts(JSONObject insertJsonObject) throws UnsupportedEncodingException {

        insertJsonObject.put("account_id", "null");
        insertJsonObject.put("re_authorisation", '0');
        logger.debug("Request of insertion is ConsentCreationInsertionAccounts : " + insertJsonObject);

        try {
            URL posturl = new URL(getProps("ACCOUNTS_RAW_DATA"));
            HttpURLConnection postConnection = (HttpURLConnection) posturl.openConnection();
            postConnection.setRequestMethod("POST");
            postConnection.setRequestProperty("Content-Type", "application/json");
            postConnection.setDoOutput(true);
            OutputStream outputStream = postConnection.getOutputStream();
            outputStream.write(insertJsonObject.toString().getBytes());
            outputStream.flush();
            outputStream.close();
            int responsecode = postConnection.getResponseCode();
            logger.debug("insetion response code" + responsecode);
            if (responsecode == 200 || responsecode == 202 || responsecode == 201)
                logger.debug("insertion was successfull : ACCOUNTS_RAW_DATA");
            else
                logger.debug("insertion was unsuccessfull : ACCOUNTS_RAW_DATA");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DB Insertion for Api Payments
    public void ConsentCreationInsertionPayments(JSONObject insertJsonObject) {

        try {
            URL posturl = new URL(getProps("PAYMENTS_RAW_DATA"));
            HttpURLConnection postConnection = (HttpURLConnection) posturl.openConnection();
            postConnection.setRequestMethod("POST");
            postConnection.setRequestProperty("Content-Type", "application/json");
            postConnection.setDoOutput(true);
            OutputStream outputStream = postConnection.getOutputStream();
            outputStream.write(insertJsonObject.toString().getBytes());
            outputStream.flush();
            outputStream.close();
            int responsecode = postConnection.getResponseCode();
            logger.debug("insetion response code" + responsecode);
            if (responsecode == 200 || responsecode == 202 || responsecode == 201)
                logger.debug("insertion was successfull : PAYMENTS_RAW_DATA");
            else
                logger.debug("insertion was unsuccessfull : PAYMENTS_RAW_DATA");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DB Insertion for Api COF
    public void ConsentCreationInsertionCOF(JSONObject insertJsonObject) {

        try {
            URL posturl = new URL(getProps("FUNDS_CONFIRMATION_RAW_DATA"));
            HttpURLConnection postConnection = (HttpURLConnection) posturl.openConnection();
            postConnection.setRequestMethod("POST");
            postConnection.setRequestProperty("Content-Type", "application/json");
            postConnection.setDoOutput(true);
            OutputStream outputStream = postConnection.getOutputStream();
            outputStream.write(insertJsonObject.toString().getBytes());
            outputStream.flush();
            outputStream.close();
            int responsecode = postConnection.getResponseCode();
            logger.debug("insetion response code" + responsecode);
            if (responsecode == 200 || responsecode == 202 || responsecode == 201)
                logger.debug("insertion was successfull : FUNDS_CONFIRMATION_RAW_DATA");
            else
                logger.debug("insertion was unsuccessfull : FUNDS_CONFIRMATION_RAW_DATA");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Getting Client Details from DSS
    public JSONObject getClientDetails(String AccessToken) {

        String url = getProps("GET_CLIENTID_URL");

        url = url + "?accesstoken=" + AccessToken;

        logger.debug("URL to get client details " + url);
        JSONObject clientdetailsjObject = null;

        try {
            URL getClientDetailsURL = new URL(url);
            String readline = null;
            HttpURLConnection connection = (HttpURLConnection) getClientDetailsURL.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            int resCode = connection.getResponseCode();

            logger.debug("response code from client : " + resCode);

            if (resCode == 200 || resCode == 202) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer responseBuffer = new StringBuffer();

                while ((readline = reader.readLine()) != null) {
                    responseBuffer.append(readline);
                }
                reader.close();
                connection.disconnect();

                logger.debug("response buffer values is : " + responseBuffer);

                clientdetailsjObject = new JSONObject(responseBuffer.toString());

                logger.debug("clientdetails are : " + clientdetailsjObject);
            } else
                logger.debug("getclientid is not working");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return clientdetailsjObject;
    }

    // Getting Payment payload details

    public HashMap<String, Object> getPaymentDetails(JSONObject jsonBody, HashMap<String, Object> elements) {

        String debaccno = null;
        String credaccno = null;

        try {
            // Getting Debtors Account Number

            if (jsonBody.getJSONObject("Data").getJSONObject("Initiation").has("DebtorAccount")) {
                if (jsonBody.getJSONObject("Data").getJSONObject("Initiation").getJSONObject("DebtorAccount")
                        .has("SchemeName")) {
                    if (jsonBody.getJSONObject("Data").getJSONObject("Initiation").getJSONObject("DebtorAccount")
                            .getString("SchemeName").contains("SortCodeAccountNumber")) {
                        debaccno = jsonBody.getJSONObject("Data").getJSONObject("DebtorAccount")
                                .getString("Identification");
                        elements.put("debtor_account_id", debaccno);
                    } else
                        logger.debug("No Debit Account Found");
                }
            }

            // Getting Creditors account number

            if (jsonBody.getJSONObject("Data").getJSONObject("Initiation").has("CreditorAccount")) {
                if (jsonBody.getJSONObject("Data").getJSONObject("Initiation").getJSONObject("CreditorAccount")
                        .getString("SchemeName").contains("SortCodeAccountNumber")) {
                    credaccno = jsonBody.getJSONObject("Data").getJSONObject("Initiation")
                            .getJSONObject("CreditorAccount").getString("Identification");
                    elements.put("creditor_account_id", credaccno);
                } else
                    logger.debug("No Credit Account Found");
            }

            // Getting No of Transactions

            if ((jsonBody.getJSONObject("Data").getJSONObject("Initiation").has("NumberOfTransactions")))
                elements.put("no_of_transactions",
                        jsonBody.getJSONObject("Data").getJSONObject("Initiation").get("NumberOfTransactions"));

            // Getting Amount and currency

            if (jsonBody.getJSONObject("Data").getJSONObject("Initiation").has("InstructedAmount")) {

                if (jsonBody.getJSONObject("Data").getJSONObject("Initiation").getJSONObject("InstructedAmount")
                        .has("Amount"))
                    elements.put("amount", jsonBody.getJSONObject("Data").getJSONObject("Initiation")
                            .getJSONObject("InstructedAmount").get("Amount"));

                if (jsonBody.getJSONObject("Data").getJSONObject("Initiation").getJSONObject("InstructedAmount")
                        .has("Currency")) {
                    elements.put("currency", jsonBody.getJSONObject("Data").getJSONObject("Initiation")
                            .getJSONObject("InstructedAmount").get("Currency"));
                } else {
                    elements.put("currency", "GBP");
                }
            } else if (jsonBody.getJSONObject("Data").has("Initiation")) {
                elements.put("amount", jsonBody.getJSONObject("Data").getJSONObject("Initiation").get("ControlSum"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return elements;
    }

}
