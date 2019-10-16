package io.jenkins.plugins.sample.util;

import org.apache.commons.lang.StringUtils;

public class UrlUtil {

    public static String getDomain(String instance){
        return (instance.startsWith("http") ? instance : ("https://" + instance));
    }

    public static String appendPath(String domain,String path) {
        String fullPath = null;
        if (StringUtils.isBlank(path)) {
            fullPath = domain;
        } else if (path.charAt(0) == '/') {
            fullPath = domain + path;
        } else {
            fullPath = domain + "/" + path;
        }
        return fullPath;
    }
}
