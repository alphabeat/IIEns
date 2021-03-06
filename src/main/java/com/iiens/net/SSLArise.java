package com.iiens.net;

import android.content.Context;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

class SSLArise {

    public SchemeRegistry init(Context context) {

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory
                .getSocketFactory(), 80));
        SSLSocketFactory sslSocketFactory = getSSLSocketFactory(context);
        schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));

        return schemeRegistry;
    }

    SSLSocketFactory getSSLSocketFactory(Context context) {
        KeyStore keyStore = getKeyStore(context);

        SSLSocketFactory sslSocketFactory = null;
        try {
            sslSocketFactory = new SSLSocketFactory(keyStore);
        } catch (KeyManagementException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return sslSocketFactory;

    }

    public SSLContext getSSLContext(Context context) {
        KeyStore keyStore = getKeyStore(context);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = null;
        try {
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }


        // Create an SSLContext that uses our TrustManager
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        return sslContext;
    }


    KeyStore getKeyStore(Context context) {
        // Load CA from an InputStream (CA would be saved in Raw file,
        // and loaded as a raw resource)
        CertificateFactory cf;
        InputStream in = context.getResources().openRawResource(R.raw.cacert);
        Certificate ca = null;
        try {
            cf = CertificateFactory.getInstance("X.509");
            ca = cf.generateCertificate(in);
        } catch (CertificateException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Create a KeyStore containing our trusted CAs
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            e.printStackTrace();
        }

        return keyStore;
    }

}