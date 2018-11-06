package com.microsoft.azure.management.samples;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class KeyCert {

    X509Certificate certificate;
    PrivateKey key;
    byte[] certValue;

    public KeyCert(X509Certificate certificate, PrivateKey key, byte[] certValue) {
        this.certificate = certificate;
        this.key = key;
        this.certValue = certValue;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public PrivateKey getKey() {
        return key;
    }

    public void setKey(PrivateKey key) {
        this.key = key;
    }

    public byte[] getCertValue() {
        return certValue;
    }

    public void setCertValue(byte[] certValue) {
        this.certValue = certValue;
    }
}
