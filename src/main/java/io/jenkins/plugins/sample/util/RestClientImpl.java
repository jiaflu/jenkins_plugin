package io.jenkins.plugins.sample.util;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.net.ssl.*;
import javax.ws.rs.core.MediaType;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


public class RestClientImpl implements RestClient {

    enum AuthType {
        BASIC, OAUTH2, CUSTOM
    }

    private Client restClient = null;
    private String apiBaseUrl = null;
    private String authString = null;
    private AuthType authType = AuthType.BASIC;
    private String oAuth2Url;
    private String oAuth2ClientId;
    private String oAuth2Secret;
    private String basicUsername;
    private String basicPassword;
    private long oAuth2TokenExpireTime = 0;
    private MediaType accept = MediaType.APPLICATION_JSON_TYPE;
    private MediaType contentType = MediaType.APPLICATION_JSON_TYPE;
    private Integer timeout = null;
    private Class<?> excType = String.class;
    private boolean sslDummy = true;

    public RestClientImpl() {
    }

    public RestClientImpl(String oAuth2Url, String clientId, String clientSecret) {
        setOAuth2(oAuth2Url, clientId, clientSecret);
    }

    public RestClientImpl(String oAuth2Url,String clientId, String clientSecret, String basicUser,String basicPassword){
        setOAuth2(oAuth2Url,clientId,clientSecret,basicUser,basicPassword);
    }

    private void setOAuth2(String oAuth2Url, String clientId, String clientSecret, String basicUsername, String basicPassword) {
        this.authType = AuthType.OAUTH2;
        this.oAuth2Url = oAuth2Url;
        this.oAuth2ClientId = clientId;
        this.oAuth2Secret = clientSecret;
        this.basicUsername = basicUsername;
        this.basicPassword = basicPassword;
    }

    public RestClientImpl(String auth) {
        setAuth(auth);
    }

    public RestClientImpl(String appId, String appToken) {
        setAuth(appId, appToken);
    }

    @PostConstruct
    public void initClient() {
        System.setProperty("jsse.enableSNIExtension", "false");
        ClientConfig cfg = new DefaultClientConfig();
        HTTPSProperties httpProps;
        if (sslDummy) {
            httpProps = getDummyHTTPSProperties();
        } else {
            httpProps = getDelegateHTTPSProperties();
        }

        cfg.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, httpProps);
        cfg.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        restClient = Client.create(cfg);
    }

    private HTTPSProperties getDelegateHTTPSProperties() {
        try {
            SSLContext ctx = SSLContext.getInstance("SSL");
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(this.getClass().getResourceAsStream("/certification"), "changeit".toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
            tmf.init(ks);
            ctx.init(null, tmf.getTrustManagers(), null);
            return new HTTPSProperties(null, ctx);
        } catch (Exception ex) {
            return null;
        }
    }

    private HTTPSProperties getDummyHTTPSProperties() {
        try {
            SSLContext ctx = SSLContext.getInstance("SSL");
            ctx.init(null, new TrustManager[] { new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
            } }, null);
            return new HTTPSProperties(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            }, ctx);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void setApiBaseUrl(String apiBaseUrl) {
        if (StringUtils.isNotBlank(apiBaseUrl))
            return;
        if (apiBaseUrl.charAt(apiBaseUrl.length() - 1) == '/') {
            this.apiBaseUrl = apiBaseUrl.substring(0, apiBaseUrl.length() - 1);
        } else {
            this.apiBaseUrl = apiBaseUrl;
        }
    }

    @Override
    public void setAccept(MediaType accept) {
        this.accept = accept == null ? MediaType.APPLICATION_JSON_TYPE : accept;
    }

    @Override
    public void setContentType(MediaType contentType) {
        this.contentType = contentType == null ? MediaType.APPLICATION_JSON_TYPE : contentType;
    }

    @Override
    public void setAuth(String appId, String appToken) {
        authType = AuthType.BASIC;
        if (appId != null && appToken != null) {
            authString = "Basic "
                    + Base64.encodeBase64String((appId + ":" + appToken).getBytes()).replace("\r", "")
                    .replace("\n", "");
        } else {
            authString = null;
        }
    }

    @Override
    public void setAuth(String authStr) {
        authType = AuthType.CUSTOM;
        authString = authStr;
    }

    @Override
    public void setOAuth2(String url, String clientId, String clientSecret) {
        authType = AuthType.OAUTH2;
        oAuth2Url = url;
        oAuth2ClientId = clientId;
        oAuth2Secret = clientSecret;
    }

    @Override
    public void setTimeout(int interval) {
        timeout = interval;
    }

    @Override
    public void setExcType(Class<?> excType) {
        if (excType != null)
            this.excType = excType;
    }

    private void retrieveOAuth2Token() throws RestClientException, RemoteException {
        if (System.currentTimeMillis() + 1800000 < oAuth2TokenExpireTime)
            return;
        try {
            RestClient restClient = new RestClientImpl();
            OAuth2Return ret = restClient.get(
                    oAuth2Url.replace("{client_id}", oAuth2ClientId).replace("{client_secret}", oAuth2Secret),
                    OAuth2Return.class);
            authString = "Bearer " + ret.getAccessToken();
            oAuth2TokenExpireTime = System.currentTimeMillis() + ret.getExpiresIn() * 1000;
        } catch (Exception e) {
            throw e;
        }
    }

    private String getFullPath(String path) {
        String fullPath = null;
        if (StringUtils.isBlank(apiBaseUrl) || path.startsWith("http://") || path.startsWith("https://")) {
            fullPath = path;
        } else if (StringUtils.isBlank(path)) {
            fullPath = apiBaseUrl;
        } else if (path.charAt(0) == '/') {
            fullPath = apiBaseUrl + path;
        } else {
            fullPath = apiBaseUrl + "/" + path;
        }
        return fullPath;
    }

    private <T> WebResource.Builder getBuilder(String path) throws RestClientException, RemoteException {
        String fullPath = getFullPath(path);
        if (restClient == null)
            initClient();
        restClient.setConnectTimeout(timeout);
        restClient.setReadTimeout(timeout);
        if (AuthType.OAUTH2 == authType)
            retrieveOAuth2Token();
        if (authString != null)
            return restClient.resource(fullPath).header("Authorization", authString).type(contentType).accept(accept);
        return restClient.resource(fullPath).type(contentType).accept(accept);
    }

    private <T> T processResult(ClientResponse resp, Class<T> type) throws RemoteException, RestClientException {
        if (type == null) return null;
        if (resp.getStatus() == 204)
            return null;
        String jsonString = resp.getEntity(String.class);
        try {
            if (resp.getStatus() >= 400) {
                throw new RemoteException(JsonUtil.asObject(jsonString, excType), this, resp);
            }
            T ret = JsonUtil.asObject(jsonString, type);
            if (ret != null)
                return ret;
            throw new Exception();
        } catch (Exception ex) {
            if (ex instanceof RemoteException)
                throw (RemoteException) ex;
            Object excObj = JsonUtil.asObject(jsonString, excType);
            if (excObj == null)
                throw new RestClientException(new Exception(String.format(
                        "Json string '%s' cannot map to neither '%s' or excType '%s'", jsonString,
                        type.getSimpleName(), excType.getSimpleName())), this);

            throw new RemoteException(excObj, this, resp);
        }
    }

    @Override
    public <T> T get(String path, Class<T> retcls) throws RemoteException, RestClientException {
        try {
            ClientResponse resp = getBuilder(path).get(ClientResponse.class);
            return processResult(resp, retcls);
        } catch (RemoteException | RestClientException | ClientHandlerException e) {
            throw e;
        }
    }

    @Override
    public <T_R, T_B> T_R post(String path, T_B entity, Class<T_R> retcls) throws RemoteException, RestClientException {
        try {
            ClientResponse resp = getBuilder(path).post(ClientResponse.class, entity);
            return processResult(resp, retcls);
        } catch (RemoteException | RestClientException | ClientHandlerException e) {
            throw e;
        }
    }

    @Override
    public <T_R, T_B> T_R put(String path, T_B entity, Class<T_R> retcls) throws RemoteException, RestClientException {
        try {
            ClientResponse resp = getBuilder(path).put(ClientResponse.class, entity);
            return processResult(resp, retcls);
        } catch (RemoteException | RestClientException | ClientHandlerException e) {
            throw e;
        }
    }

    @Override
    public <T> T delete(String path, Class<T> retcls) throws RemoteException, RestClientException {
        try {
            ClientResponse resp = getBuilder(path).delete(ClientResponse.class);
            return processResult(resp, retcls);
        } catch (RemoteException | RestClientException | ClientHandlerException e) {
            throw e;
        }
    }

}

