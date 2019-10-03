package org.internetresources.util;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UnsafeSSLHelper {
    private static Log log = LogFactory.getLog(UnsafeSSLHelper.class.getName());
    /**
     * This method could be called before asking "https://" uri to accept any invalid ssl certificate.
     */

    public static SSLContext createUnsecureSSLContext() {
        SSLContext sc = null;
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{getPassiveTrustManager()};
            sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustAllCerts, new SecureRandom());
        } catch (Exception e) {
            String msg = "error while creating unsecure SSLContext";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
        return sc;
    }

    public static X509TrustManager getPassiveTrustManager() {
        return new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                    throws CertificateException {
            }

            public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                    throws CertificateException {
            }
        };
    }
}
