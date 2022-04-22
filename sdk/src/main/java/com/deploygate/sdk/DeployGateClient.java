package com.deploygate.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;

import com.deploygate.sdk.internal.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class DeployGateClient {
    private static final String[] FINGERPRINTS = new String[]{
            // deploygate release
            "2f97f647645cb762bf5fc1445599a954e6ad76e7",
            // mba debug
            "c1f285f69cc02a397135ed182aa79af53d5d20a1"
    };

    public final long versionCode;
    public final boolean isInstalled;
    private final int featuresFlag;

    @SuppressLint("PackageManagerGetSignatures")
    DeployGateClient(
            Context context,
            String packageName
    ) {
        PackageInfo info = null;
        int flag = PackageManager.GET_META_DATA;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            flag |= PackageManager.GET_SIGNING_CERTIFICATES;
        } else {
            flag |= PackageManager.GET_SIGNATURES;
        }

        try {
            info = context.getPackageManager().getPackageInfo(packageName, flag);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.w("deploygate app is not found");
        }

        Signature[] signatures = getSignatures(info);

        if (info != null && checkSignature(signatures)) {
            this.isInstalled = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                this.versionCode = info.getLongVersionCode();
            } else {
                this.versionCode = info.versionCode;
            }

            this.featuresFlag = info.applicationInfo.metaData.getInt("com.deploygate.features", 0);
        } else {
            this.isInstalled = false;
            this.versionCode = 0;
            this.featuresFlag = -1;
        }
    }

    boolean isSupported(Compatibility compatibility) {
        if (featuresFlag < 0) {
            return false;
        }

        if (featuresFlag == 0) {
            switch (compatibility) {
                case UPDATE_MESSAGE_OF_BUILD:
                    return versionCode >= Compatibility.ClientVersion.SUPPORT_UPDATE_MESSAGE_OF_BUILD;
                case SERIALIZED_EXCEPTION:
                    return versionCode >= Compatibility.ClientVersion.SUPPORT_SERIALIZED_EXCEPTION;
                default:
                    return false;
            }
        }

        return (featuresFlag & compatibility.bitMask) > 0;
    }

    /**
     * @param info
     *         com.deploygate's package info
     *
     * @return signature array that is not null but may be zero-length
     */
    private Signature[] getSignatures(PackageInfo info) {
        if (info == null) {
            return new Signature[0];
        }

        final Signature[] signatures;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            SigningInfo signingInfo = info.signingInfo;

            if (signingInfo == null) {
                return new Signature[0];
            }

            if (signingInfo.hasMultipleSigners()) {
                signatures = signingInfo.getApkContentsSigners();
            } else {
                signatures = signingInfo.getSigningCertificateHistory();
            }
        } else {
            signatures = info.signatures;
        }

        return signatures != null ? signatures : new Signature[0];
    }

    /**
     * Check if the whitelist contains at least one of signing information.
     * Returning *true* may contain false-positive API 19 or lower. ref: FakeID
     *
     * @param signatures
     *         an array of signatures
     *
     * @return true if at least one of signature is in the whitelist, otherwise false.
     */
    static boolean checkSignature(Signature[] signatures) {
        final MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            Logger.e("SHA1 is not supported on this platform?", e);
            return false;
        }

        for (Signature signature : signatures) {
            byte[] digest = md.digest(signature.toByteArray());
            StringBuilder result = new StringBuilder(40);
            for (byte b : digest) {
                result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            String hex = result.toString();

            for (final String fingerprint : FINGERPRINTS) {
                if (fingerprint.equals(hex)) {
                    return true;
                }
            }
        }

        return false;
    }
}
