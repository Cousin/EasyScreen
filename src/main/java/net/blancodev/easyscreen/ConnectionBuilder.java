package net.blancodev.easyscreen;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionBuilder {

    private final static String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";

    private String endpoint;
    private int port;

    private String data;
    private String method;

    private Proxy proxy;

    private boolean https;

    private Map<String, String> headers;

    private HttpURLConnection finalConnection;

    public boolean failed;

    public ConnectionBuilder(String endpoint) {

        this.endpoint = endpoint;
        this.port = 80;
        this.data = new String();
        this.headers = new HashMap<>();
        this.method = "GET";
        this.https = false;

    }

    public ConnectionBuilder https(boolean https) {
        this.https = https;
        return this;
    }

    public ConnectionBuilder proxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public ConnectionBuilder endpoint(String endpoint) {

        this.endpoint = endpoint;

        return this;

    }

    public ConnectionBuilder header(String key, String value) {
        this.headers.put(key, value);

        return this;
    }

    public ConnectionBuilder port(int port) {

        this.port = port;

        return this;

    }

    public ConnectionBuilder method(String method) {

        this.method = method;

        return this;

    }

    public ConnectionBuilder data(String data) {

        this.data = data;

        return this;

    }

    public ConnectionBuilder send() {

        try {

            if (method.equalsIgnoreCase("POST")) {

                HttpURLConnection connection;
                if (https) {

                    //random ass shit i took off stackoverflow that made it work.. dont ask
                    CookieHandler.setDefault(new CookieManager());
                    TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }};
                    SSLContext sc = null;
                    try {
                        sc = SSLContext.getInstance("SSL");
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    try {
                        sc.init(null, trustAllCerts, new java.security.SecureRandom());
                    } catch (KeyManagementException e) {
                        e.printStackTrace();
                    }
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                    if (proxy != null) {
                        connection = (HttpsURLConnection) new URL(endpoint).openConnection(proxy);
                    } else {
                        connection = (HttpsURLConnection) new URL(endpoint).openConnection();
                    }
                } else {
                    connection = (HttpURLConnection) new URL(endpoint).openConnection();
                }

                connection.setInstanceFollowRedirects(false);
                connection.setConnectTimeout(20_000);
                connection.setReadTimeout(20_000);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                connection.setRequestMethod("POST");
                connection.setRequestProperty("User-Agent", USER_AGENT);

                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }

                DataOutputStream output = new DataOutputStream(connection.getOutputStream());
                output.writeBytes(data);
                output.flush();
                output.close();

                finalConnection = connection;

            } else if (method.equalsIgnoreCase("GET")) {

                HttpURLConnection connection;
                if (https) {
                    CookieHandler.setDefault(new CookieManager());
                    TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }};
                    SSLContext sc = null;
                    try {
                        sc = SSLContext.getInstance("SSL");
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    try {
                        sc.init(null, trustAllCerts, new java.security.SecureRandom());
                    } catch (KeyManagementException e) {
                        e.printStackTrace();
                    }
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                    if (proxy != null) {
                        connection = (HttpsURLConnection) new URL(endpoint).openConnection(proxy);
                    } else {
                        connection = (HttpsURLConnection) new URL(endpoint).openConnection();
                    }
                } else {
                    connection = (HttpURLConnection) new URL(endpoint).openConnection();
                }

                connection.setInstanceFollowRedirects(false);

                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", USER_AGENT);

                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }

                connection.setConnectTimeout(25_000);
                connection.setDoOutput(true);

                connection.getResponseCode();
                connection.connect();

                finalConnection = connection;

            }


        } catch (Exception e) {
            System.out.println("Connection error " + (proxy != null ? "Using Proxy " + proxy : ""));
            System.out.println(e.getMessage());
            failed = true;
        }

        return this;

    }

    public String getCookiesStr() {

        if (finalConnection != null) {

            System.out.println(finalConnection.getHeaderFields());

            String cookies = "";

            for (Map.Entry<String, List<String>> entry : finalConnection.getHeaderFields().entrySet()) {

                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase("set-cookie")) {

                    for (String cookie : entry.getValue()) {

                        cookies += cookie + (cookie.endsWith(";") ? "" : ";");

                    }

                }

            }

            System.out.println("coogas " + cookies);

            return cookies;

        }

        return "";

    }

    public Map<String, String> getCookies() {

        String cookiesStr = getCookiesStr();

        if (cookiesStr.replace(" ", "").length() == 0) {
            return new HashMap<>();
        }


        Map<String, String> cookies = new HashMap<>();

        String[] split = cookiesStr.split(";");

        for (String cookie : split) {
            String[] cookieSplit = cookie.split("=");
            if (cookieSplit.length == 2) {
                cookies.put(cookieSplit[0], cookieSplit[1]);
            }
        }

        return cookies;

    }

    public String getResponse() {

        if (finalConnection != null) {

            try {
                if (finalConnection.getResponseCode() > 199 && finalConnection.getResponseCode() < 400) {

                    BufferedReader reader = new BufferedReader(new InputStreamReader(finalConnection.getInputStream()));

                    String finalRes = "";
                    String line;

                    while ((line = reader.readLine()) != null) {
                        finalRes += line;
                    }

                    reader.close();

                    return finalRes;

                } else {

                    BufferedReader reader;
                    try {
                        reader = new BufferedReader(new InputStreamReader(finalConnection.getErrorStream()));
                    } catch (NullPointerException x) {
                        failed = true;
                        return "Error grabbing response";
                    }

                    String finalRes = "";
                    String line;

                    try {
                        while ((line = reader.readLine()) != null) {
                            finalRes += line;
                        }
                    } catch (IOException e1) {
                        return "Error grabbing response";
                    }

                    try {
                        reader.close();
                    } catch (IOException e1) {
                        failed = true;
                        return "Error grabbing response";
                    }

                    return finalRes;

                }
            } catch (IOException e) {
                failed = true;
                return "Error grabbing response";
            }

        } else {
            failed = true;
            return "Error grabbing response";
        }

    }

    public String getHeader(String name) {

        return finalConnection.getHeaderField(name);

    }

    public int getResponseCode() {
        try {
            return finalConnection.getResponseCode();
        } catch (IOException | NullPointerException e) {
            return 400;
        }
    }

    public HttpURLConnection getFinalConnection() {
        return finalConnection;
    }

    public Map<String, String> getSentHeaders() {
        return headers;
    }

    private String formatGetURL(String url, String data) {

        String newURL = url;

        if (!newURL.endsWith("?")) {
            newURL = newURL + "?";
        }

        newURL = newURL + data;

        return newURL;

    }


}