package io.jenkins.plugins.sample.util;

import com.sun.jersey.api.client.ClientResponse;
import javax.ws.rs.core.MediaType;

public interface RestClient {
    public static class RemoteException extends Exception {
        private static final long serialVersionUID = 0;
        Object excObj = null;
        ClientResponse response = null;
        RestClient restClient = null;

        RemoteException(Object o, RestClient restClient, ClientResponse resp) {
            excObj = o;
            this.response = resp;
            this.restClient = restClient;
        }

        public Object getExceptionObject() {
            return excObj;
        }

        public String toString() {
            return String.format("Exception occured on remote host: RestClient: %s, Response: %s", restClient,
                    response.toString());
        }
    }

    public static class RestClientException extends Exception {
        private static final long serialVersionUID = 0;
        Exception cause = null;
        RestClient restClient = null;

        RestClientException(Exception cause, RestClient restClient) {
            this.cause = cause;
            this.restClient = restClient;
        }

        public Exception getCause() {
            return cause;
        }

        public String toString() {
            return String.format("Exception occured RestClient: RestClient: %s, Exception: %s", restClient,
                    cause.toString());
        }
    }

    /**
     * Set API Base URL
     *
     * @param  apiBaseUrl  Base URL of an API.
     *                     if it is set to null, methods can only
     *                     be invoked using absolute URL
     */
    public void setApiBaseUrl(String apiBaseUrl);

    /**
     * Set HTTP Accept header
     *
     * @param  accept   accept type, application/json if null
     */
    public void setAccept(MediaType accept);

    /**
     * Set API contentType
     *
     * @param  contentType  the auth string passed to contentType header field
     */
    public void setContentType(MediaType contentType);

    /**
     * Set API authorization
     *
     * @param  authStr  the auth string passed to Authorization header field
     */
    public void setAuth(String authStr);

    public void setOAuth2(String url, String clientId, String clientSecret);

    /**
     * Set API authorization
     *
     * @param  appId    if appId is null, no authorization is used
     * @param  appToken appToken
     */
    public void setAuth(String appId, String appToken);

    /**
     * Set connect interval
     *
     * @param  interval  connection interval in milliseconds. infinite if 0
     */
    public void setTimeout(int interval);

    /**
     * Set exception type returned by server when error
     *
     * @param  excType  exception type, should be POJO
     */
    public void setExcType(Class<?> excType);

    /**
     * Restful GET
     *
     * @param  path     absolute URL or relative path if apiBaseUrl is specified
     * @param  retcls   expected type of return object, should be a POJO
     *
     * @return object returned by the API
     * @throws Exception
     * @throws RemoteException
     */
    public <T> T get(String path, Class<T> retcls) throws RemoteException, RestClientException;

    /**
     * Restful POST
     *
     * @param  path     absolute URL or relative path if apiBaseUrl is specified
     * @body   entity   body entity to pass to the API, should be a POJO
     * @param  retcls   expected type of return object, should be a POJO
     *
     * @return object returned by the API
     * @throws Exception
     * @throws RemoteException
     */
    public <T_R, T_B> T_R post(String path, T_B entity, Class<T_R> retcls) throws RemoteException, RestClientException;

    /**
     * Restful PUT
     *
     * @param  path     absolute URL or relative path if apiBaseUrl is specified
     * @body   entity   body entity to pass to the API, should be a POJO
     * @param  retcls   expected type of return object, should be a POJO
     *
     * @return object returned by the API
     * @throws Exception
     * @throws RemoteException
     */
    public <T_R, T_B> T_R put(String path, T_B entity, Class<T_R> retcls) throws RemoteException, RestClientException;

    /**
     * Restful DELETE
     *
     * @param  path     absolute URL or relative path if apiBaseUrl is specified
     * @param  retcls   expected type of return object, should be a POJO
     *
     * @return object returned by the API
     * @throws Exception
     * @throws RemoteException
     */
    public <T> T delete(String path, Class<T> retcls) throws RemoteException, RestClientException;

}
