package utils;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HTTPUtils {

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }



    public static void ignoreHttpsChecking() throws Exception {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {
            }
        }};

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    public static String getDataFromServer(String urlData, String requestedMethod)
            throws MalformedURLException, IOException, ProtocolException {

        return getDataFromServer(urlData, requestedMethod, null);
    }

    public static String getDataFromServer(String urlData, String requestedMethod, String content)
            throws MalformedURLException, IOException, ProtocolException {
        URL url = new URL(urlData);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(requestedMethod);
        con.setConnectTimeout(60000);
        con.setReadTimeout(60000);
        if (content != null) {
            byte[] postDataBytes = content.getBytes("UTF-8");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setDoOutput(true);
            con.setDoInput(true);
            OutputStream outputStream = con.getOutputStream();
            outputStream.write(postDataBytes);
            outputStream.flush();
            outputStream.close();
        }
        con.connect();
        StringBuffer response = new StringBuffer();
        if (con.getResponseCode() != 200 && con.getResponseCode() != 201) {
            readContent(response, con.getErrorStream());
        } else {
            readContent(response, con.getInputStream());
        }
        System.out.println("HTTP Response received for " + urlData + "\n" + response.toString());
        return response.toString();
    }

    public static void readContent(StringBuffer response, InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String inputLine;
        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine).append("\n");
        }
        reader.close();
    }


}
