package com.hamondev.shevery.api;

/**
 * Constants for the native Shevery Privilege API.
 */
public final class SheveryApiConstants {

    private SheveryApiConstants() {
    }

    public static final int SERVER_VERSION = 14;
    public static final int SERVER_PATCH_VERSION = 5;

    // Binder interface
    public static final String BINDER_DESCRIPTOR = "moe.shizuku.server.IShizukuService";
    public static final int BINDER_TRANSACTION_transact = 1;

    // Package identifiers
    public static final String MANAGER_APPLICATION_ID = "com.hamondev.shevery";
    public static final String SHIZUKU_STUB_APPLICATION_ID = "moe.shizuku.privileged.api";

    // Native Shevery permissions
    public static final String PERMISSION_GROUP_API = "com.hamondev.shevery.permission-group.API";
    public static final String PERMISSION_PRIVILEGED_API = "com.hamondev.shevery.permission.PRIVILEGED_API";
    public static final String PERMISSION_MANAGER = "com.hamondev.shevery.permission.MANAGER";

    // Client metadata markers
    public static final String META_DATA_CLIENT_SUPPORT = "com.hamondev.shevery.client.PRIVILEGED_API_SUPPORT";

    // ContentProvider authorities & methods
    public static final String PROVIDER_SUFFIX = ".shevery";
    public static final String LEGACY_PROVIDER_SUFFIX = ".shizuku";
    public static final String MANAGER_PROVIDER_AUTHORITY = MANAGER_APPLICATION_ID + PROVIDER_SUFFIX;
    public static final String MANAGER_LEGACY_PROVIDER_AUTHORITY = MANAGER_APPLICATION_ID + LEGACY_PROVIDER_SUFFIX;

    public static final String METHOD_SEND_BINDER = "sendBinder";
    public static final String METHOD_GET_BINDER = "getBinder";
    public static final String METHOD_REQUEST_BINDER = "requestBinder";

    // Extras & broadcast actions
    public static final String EXTRA_BINDER = "com.hamondev.shevery.intent.extra.BINDER";
    public static final String LEGACY_EXTRA_BINDER = "moe.shizuku.privileged.api.intent.extra.BINDER";
    public static final String EXTRA_PERMISSION_CHANNEL = "com.hamondev.shevery.intent.extra.PERMISSION_CHANNEL";
    public static final String EXTRA_REQUESTED_PERMISSION = "com.hamondev.shevery.intent.extra.REQUESTED_PERMISSION";

    public static final String ACTION_BINDER_RECEIVED = "com.hamondev.shevery.api.action.BINDER_RECEIVED";
    public static final String ACTION_REQUEST_PERMISSION = "com.hamondev.shevery.intent.action.REQUEST_PERMISSION";
    public static final String ACTION_REQUEST_BINDER = "com.hamondev.shevery.intent.action.REQUEST_BINDER";

    // Attach application channel negotiation
    public static final String ATTACH_APPLICATION_CLIENT_CHANNEL = "shevery:attach-client-channel";
    public static final String CHANNEL_SHEVERY_NATIVE = "shevery_native";
}
