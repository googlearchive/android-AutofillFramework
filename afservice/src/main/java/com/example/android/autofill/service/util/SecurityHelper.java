/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.autofill.service.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Helper class for security checks.
 */
public final class SecurityHelper {

    private SecurityHelper() {
        throw new UnsupportedOperationException("Provides static methods only.");
    }

    /**
     * Gets the fingerprint of the signed certificate of a package.
     */
    public static String getFingerprint(PackageInfo packageInfo, String packageName) throws
            PackageManager.NameNotFoundException, IOException, NoSuchAlgorithmException,
            CertificateException {
        Signature[] signatures = packageInfo.signatures;
        if (signatures.length != 1) {
            throw new SecurityException(packageName + " has " + signatures.length + " signatures");
        }
        byte[] cert = signatures[0].toByteArray();
        try (InputStream input = new ByteArrayInputStream(cert)) {
            CertificateFactory factory = CertificateFactory.getInstance("X509");
            X509Certificate x509 = (X509Certificate) factory.generateCertificate(input);
            MessageDigest md = MessageDigest.getInstance("SHA256");
            byte[] publicKey = md.digest(x509.getEncoded());
            return toHexFormat(publicKey);
        }
    }

    private static String toHexFormat(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i]);
            int length = hex.length();
            if (length == 1) {
                hex = "0" + hex;
            }
            if (length > 2) {
                hex = hex.substring(length - 2, length);
            }
            builder.append(hex.toUpperCase());
            if (i < (bytes.length - 1)) {
                builder.append(':');
            }
        }
        return builder.toString();
    }
}
