package com.hamondev.shevery.api;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import moe.shizuku.api.BinderContainer;

/**
 * Native ContentProvider endpoint for Shevery Privilege API ({@code ${applicationId}.shevery}).
 */
public class SheveryProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        Context context = getContext();
        if (context != null) {
            SheveryPrivilege.connectFast(context);
        }
        return true;
    }

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        if (extras == null) {
            return null;
        }
        if (SheveryApiConstants.METHOD_SEND_BINDER.equals(method)) {
            extras.setClassLoader(BinderContainer.class.getClassLoader());
            BinderContainer container = extras.getParcelable(SheveryApiConstants.EXTRA_BINDER);
            if (container == null) {
                container = extras.getParcelable(SheveryApiConstants.LEGACY_EXTRA_BINDER);
            }
            if (container != null && container.binder != null) {
                Context context = getContext();
                SheveryPrivilege.onBinderReceived(container.binder, context != null ? context.getPackageName() : null);
            }
            return new Bundle();
        }
        return null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
