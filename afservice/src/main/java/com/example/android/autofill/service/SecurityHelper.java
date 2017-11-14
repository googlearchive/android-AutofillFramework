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
package com.example.android.autofill.service;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.util.Log;

import com.google.common.net.InternetDomainName;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static com.example.android.autofill.service.Util.logd;
import static com.example.android.autofill.service.Util.logv;
import static com.example.android.autofill.service.Util.logw;

/**
 * Helper class for security checks.
 */
public final class SecurityHelper {

    private static final String REST_TEMPLATE =
            "https://digitalassetlinks.googleapis.com/v1/assetlinks:check?"
                    + "source.web.site=%s&relation=delegate_permission/%s"
                    + "&target.android_app.package_name=%s"
                    + "&target.android_app.certificate.sha256_fingerprint=%s";

    private static final String PERMISSION_GET_LOGIN_CREDS = "common.get_login_creds";
    private static final String PERMISSION_HANDLE_ALL_URLS = "common.handle_all_urls";

    private SecurityHelper() {
        throw new UnsupportedOperationException("provides static methods only");
    }

    private static boolean isValidSync(String webDomain, String permission, String packageName,
            String fingerprint) {
        logd("validating domain %s for pkg %s and fingerprint %s for permission %s",
                webDomain, packageName, fingerprint, permission);
        if (!webDomain.startsWith("http:") && !webDomain.startsWith("https:")) {
            // Unfortunately AssistStructure.ViewNode does not tell what the domain is, so let's
            // assume it's https
            webDomain = "https://" + webDomain;
        }

        String restUrl =
                String.format(REST_TEMPLATE, webDomain, permission, packageName, fingerprint);
        logd("DAL REST request: %s", restUrl);

        HttpURLConnection urlConnection = null;
        StringBuilder output = new StringBuilder();
        try {
            URL url = new URL(restUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()))) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }
            String response = output.toString();
            logv("DAL REST Response: %s", response);

            JSONObject jsonObject = new JSONObject(response);
            boolean valid = jsonObject.optBoolean("linked", false);
            logd("Valid: %b", valid);

            return valid;
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

    }

    private static boolean isValidSync(String webDomain, String packageName, String fingerprint) {
        boolean isValid =
                isValidSync(webDomain, PERMISSION_GET_LOGIN_CREDS, packageName, fingerprint);
        if (!isValid) {
            // Ideally we should only check for the get_login_creds, but not all domains set
            // it yet, so validating for handle_all_urls gives a higher coverage.
            logd("%s validation failed; trying %s", PERMISSION_GET_LOGIN_CREDS,
                    PERMISSION_HANDLE_ALL_URLS);
            isValid = isValidSync(webDomain, PERMISSION_HANDLE_ALL_URLS, packageName, fingerprint);
        }
        return isValid;
    }

    public static String getCanonicalDomain(String domain) {
        InternetDomainName idn = InternetDomainName.from(domain);
        while (idn != null && !idn.isTopPrivateDomain()) {
            idn = idn.parent();
        }
        return idn == null ? null : idn.toString();
    }

    public static boolean isValid(String webDomain, String packageName, String fingerprint) {
        String canonicalDomain = getCanonicalDomain(webDomain);
        logd("validating domain %s (%s) for pkg %s and fingerprint %s.", canonicalDomain,
                webDomain, packageName, fingerprint);
        final String fullDomain;
        if (!webDomain.startsWith("http:") && !webDomain.startsWith("https:")) {
            // Unfortunately AssistStructure.ViewNode does not tell what the domain is, so let's
            // assume it's https
            fullDomain = "https://" + canonicalDomain;
        } else {
            fullDomain = canonicalDomain;
        }

        // TODO: use the DAL Java API or a better REST alternative like Volley
        // and/or document it should not block until it returns (for example, the server could
        // start parsing the structure while it waits for the result.
        AsyncTask<String, Integer, Boolean> task = new AsyncTask<String, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(String... strings) {
                return isValidSync(fullDomain, packageName, fingerprint);
            }
        };
        try {
            return task.execute((String[]) null).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logw("Thread interrupted");
        } catch (Exception e) {
            logw(e, "Async task failed");
        }
        return false;
    }

    /**
     * Gets the fingerprint of the signed certificate of a package.
     */
    public static String getFingerprint(Context context, String packageName) throws Exception {
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
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
