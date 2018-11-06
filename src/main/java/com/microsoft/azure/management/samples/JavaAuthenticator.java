package com.microsoft.azure.management.samples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;

public class JavaAuthenticator {

    /**
     * Read pfx file and get privateKey
     * 
     * @param path     pfx file path
     * @param password the password to the pfx file
     */
    public static KeyCert readPfx(String path, String password) throws NoSuchProviderException, KeyStoreException,
            IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {

        try (FileInputStream stream = new FileInputStream(path)) {
            KeyCert keyCert = new KeyCert(null, null, null);

            boolean isAliasWithPrivateKey = false;

            // Access Java keystore
            final KeyStore store = KeyStore.getInstance("pkcs12", "SunJSSE");

            // Load Java Keystore with password for access
            store.load((InputStream) stream, password.toCharArray());

            // Iterate over all aliases to find the private key
            Enumeration<String> aliases = store.aliases();
            String alias = "";
            while (aliases.hasMoreElements()) {
                alias = aliases.nextElement();
                System.out.println(alias);
                // Break if alias refers to a private key because we want to use that
                // certificate
                if (isAliasWithPrivateKey = store.isKeyEntry(alias)) {
                    break;
                }
            }

            if (isAliasWithPrivateKey) {
                // Retrieves the certificate from the Java keystore
                X509Certificate certificate = (X509Certificate) store.getCertificate(alias);
                System.out.println("the alias is: " + alias);

                byte[] certValue = Files.readAllBytes(Paths.get(path));

                // Retrieves the private key from the Java keystore
                PrivateKey key = (PrivateKey) store.getKey(alias, password.toCharArray());

                keyCert.setCertificate(certificate);
                keyCert.setKey(key);
                keyCert.setCertValue(certValue);

                System.out.println("key in primary encoding format is: " + key.getEncoded());
            }
            return keyCert;
        }
    }

    /**
     * Read pem file
     * 
     * @param path     pem file path
     * @param password the password to the pem file
     */

    public static KeyCert readPem(String path, String password)
            throws IOException, CertificateException, OperatorCreationException, PKCSException {

        Security.addProvider(new BouncyCastleProvider());
        PEMParser pemParser = new PEMParser(new FileReader(new File(path)));
        PrivateKey privateKey = null;
        X509Certificate cert = null;
        Object object = pemParser.readObject();

        while (object != null) {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            if (object instanceof X509CertificateHolder) {
                cert = new JcaX509CertificateConverter().getCertificate((X509CertificateHolder) object);
            }
            if (object instanceof PKCS8EncryptedPrivateKeyInfo) {
                PKCS8EncryptedPrivateKeyInfo pinfo = (PKCS8EncryptedPrivateKeyInfo) object;
                InputDecryptorProvider provider = new JceOpenSSLPKCS8DecryptorProviderBuilder()
                        .build(password.toCharArray());
                PrivateKeyInfo info = pinfo.decryptPrivateKeyInfo(provider);
                privateKey = converter.getPrivateKey(info);
            }
            if (object instanceof PrivateKeyInfo) {
                privateKey = converter.getPrivateKey((PrivateKeyInfo) object);
            }
            object = pemParser.readObject();
        }

        KeyCert keycert = new KeyCert(null, null, null);
        keycert.setCertificate(cert);
        keycert.setKey(privateKey);
        pemParser.close();
        return keycert;
    }
}
