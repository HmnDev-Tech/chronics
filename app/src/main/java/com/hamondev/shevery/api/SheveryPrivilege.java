package com.hamondev.shevery.api;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;

import moe.shizuku.api.BinderContainer;
import moe.shizuku.server.IShizukuApplication;
import moe.shizuku.server.IShizukuService;

/**
 * Native, low-latency Privilege API for Shevery ({@code com.hamondev.shevery.permission.PRIVILEGED_API}).
 */
public final class SheveryPrivilege {

    private static final String TAG = "SheveryPrivilege";
    private static final int TRANSACTION_attachApplication_v13 = 18;

    private static final String ATTACH_APPLICATION_API_VERSION = "shizuku:attach-api-version";
    private static final String ATTACH_APPLICATION_PACKAGE_NAME = "shizuku:attach-package-name";
    private static final String BIND_APPLICATION_SERVER_UID = "shizuku:attach-reply-uid";
    private static final String BIND_APPLICATION_SERVER_VERSION = "shizuku:attach-reply-version";
    private static final String BIND_APPLICATION_SERVER_PATCH_VERSION = "shizuku:attach-reply-patch-version";
    private static final String BIND_APPLICATION_SERVER_SECONTEXT = "shizuku:attach-reply-secontext";
    private static final String BIND_APPLICATION_PERMISSION_GRANTED = "shizuku:attach-reply-permission-granted";
    private static final String BIND_APPLICATION_SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE = "shizuku:attach-reply-should-show-request-permission-rationale";
    private static final String REQUEST_PERMISSION_REPLY_ALLOWED = "shizuku:request-permission-reply-allowed";

    private static volatile IBinder binder;
    private static volatile IShizukuService service;

    private static volatile int serverUid = -1;
    private static volatile int serverApiVersion = -1;
    private static volatile int serverPatchVersion = -1;
    private static volatile String serverContext = null;
    private static volatile boolean permissionGranted = false;
    private static volatile boolean shouldShowRequestPermissionRationale = false;
    private static volatile boolean binderReady = false;

    public interface OnBinderReceivedListener {
        void onBinderReceived();
    }

    public interface OnBinderDeadListener {
        void onBinderDead();
    }

    public interface OnPermissionResultListener {
        void onPermissionResult(int requestCode, int grantResult);
    }

    private static final class ListenerEntry<T> {
        final T listener;
        final Handler handler;

        ListenerEntry(@NonNull T listener, @Nullable Handler handler) {
            this.listener = listener;
            this.handler = handler;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ListenerEntry)) return false;
            ListenerEntry<?> that = (ListenerEntry<?>) o;
            return Objects.equals(listener, that.listener);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listener);
        }
    }

    private static final CopyOnWriteArraySet<ListenerEntry<OnBinderReceivedListener>> RECEIVED_LISTENERS = new CopyOnWriteArraySet<>();
    private static final CopyOnWriteArraySet<ListenerEntry<OnBinderDeadListener>> DEAD_LISTENERS = new CopyOnWriteArraySet<>();
    private static final CopyOnWriteArraySet<ListenerEntry<OnPermissionResultListener>> PERMISSION_LISTENERS = new CopyOnWriteArraySet<>();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private static final IShizukuApplication SHEVERY_APPLICATION = new IShizukuApplication.Stub() {

        @Override
        public void bindApplication(Bundle data) {
            serverUid = data.getInt(BIND_APPLICATION_SERVER_UID, -1);
            serverApiVersion = data.getInt(BIND_APPLICATION_SERVER_VERSION, -1);
            serverPatchVersion = data.getInt(BIND_APPLICATION_SERVER_PATCH_VERSION, -1);
            serverContext = data.getString(BIND_APPLICATION_SERVER_SECONTEXT);
            permissionGranted = data.getBoolean(BIND_APPLICATION_PERMISSION_GRANTED, false);
            shouldShowRequestPermissionRationale = data.getBoolean(BIND_APPLICATION_SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE, false);

            binderReady = true;
            dispatchBinderReceived();
        }

        @Override
        public void dispatchRequestPermissionResult(int requestCode, Bundle data) {
            boolean allowed = data != null && data.getBoolean(REQUEST_PERMISSION_REPLY_ALLOWED, false);
            permissionGranted = allowed;
            if (allowed) {
                shouldShowRequestPermissionRationale = false;
            }
            int result = allowed ? PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED;
            dispatchPermissionResult(requestCode, result);
        }

        @Override
        public void showPermissionConfirmation(int requestUid, int requestPid, String requestPackageName, int requestCode) {
        }
    };

    private static final IBinder.DeathRecipient DEATH_RECIPIENT = () -> {
        binderReady = false;
        onBinderReceived(null, null);
    };

    private SheveryPrivilege() {
    }

    /**
     * Proactive fast pull acquisition: connects directly to Shevery Manager's provider.
     */
    public static boolean connectFast(@NonNull Context context) {
        if (pingBinder()) {
            return true;
        }
        final String pkg = context.getPackageName();
        final String[] authorities = new String[]{
                SheveryApiConstants.MANAGER_PROVIDER_AUTHORITY,
                SheveryApiConstants.MANAGER_LEGACY_PROVIDER_AUTHORITY,
                pkg + SheveryApiConstants.PROVIDER_SUFFIX
        };

        for (String authority : authorities) {
            try {
                Bundle extras = new Bundle();
                extras.putString(ATTACH_APPLICATION_PACKAGE_NAME, pkg);
                extras.putString(SheveryApiConstants.ATTACH_APPLICATION_CLIENT_CHANNEL, SheveryApiConstants.CHANNEL_SHEVERY_NATIVE);
                Bundle reply = context.getContentResolver().call(
                        Uri.parse("content://" + authority),
                        SheveryApiConstants.METHOD_REQUEST_BINDER,
                        pkg,
                        extras
                );
                if (reply != null) {
                    reply.setClassLoader(BinderContainer.class.getClassLoader());
                    BinderContainer container = reply.getParcelable(SheveryApiConstants.EXTRA_BINDER);
                    if (container == null) {
                        container = reply.getParcelable(SheveryApiConstants.LEGACY_EXTRA_BINDER);
                    }
                    if (container != null && container.binder != null && container.binder.pingBinder()) {
                        onBinderReceived(container.binder, pkg);
                        return true;
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return pingBinder();
    }

    private static boolean attachApplicationFast(@NonNull IBinder targetBinder, @NonNull String packageName) throws RemoteException {
        Bundle args = new Bundle();
        args.putInt(ATTACH_APPLICATION_API_VERSION, SheveryApiConstants.SERVER_VERSION);
        args.putString(ATTACH_APPLICATION_PACKAGE_NAME, packageName);
        args.putString(SheveryApiConstants.ATTACH_APPLICATION_CLIENT_CHANNEL, SheveryApiConstants.CHANNEL_SHEVERY_NATIVE);
        args.putString(SheveryApiConstants.EXTRA_REQUESTED_PERMISSION, SheveryApiConstants.PERMISSION_PRIVILEGED_API);

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(SheveryApiConstants.BINDER_DESCRIPTOR);
            data.writeStrongBinder(SHEVERY_APPLICATION.asBinder());
            data.writeInt(1);
            args.writeToParcel(data, 0);
            boolean result = targetBinder.transact(TRANSACTION_attachApplication_v13, data, reply, 0);
            reply.readException();
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public static synchronized void onBinderReceived(@Nullable IBinder newBinder, @Nullable String packageName) {
        if (binder == newBinder && (newBinder == null || newBinder.pingBinder())) {
            return;
        }

        if (newBinder == null) {
            if (binder != null) {
                try {
                    binder.unlinkToDeath(DEATH_RECIPIENT, 0);
                } catch (Throwable ignored) {
                }
            }
            binder = null;
            service = null;
            serverUid = -1;
            serverApiVersion = -1;
            serverPatchVersion = -1;
            serverContext = null;
            permissionGranted = false;
            binderReady = false;
            dispatchBinderDead();
            return;
        }

        try {
            newBinder.linkToDeath(DEATH_RECIPIENT, 0);
        } catch (RemoteException e) {
            return;
        }

        binder = newBinder;
        service = IShizukuService.Stub.asInterface(newBinder);

        if (packageName != null) {
            try {
                attachApplicationFast(newBinder, packageName);
            } catch (Throwable t) {
                Log.w(TAG, "attachApplicationFast failed", t);
            }
        }
    }

    public static boolean pingBinder() {
        return binder != null && binder.pingBinder();
    }

    @NonNull
    public static IShizukuService requireService() {
        IShizukuService s = service;
        if (s == null) {
            throw new IllegalStateException("Shevery service is not attached");
        }
        return s;
    }

    public static boolean isGranted() {
        return checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
    }

    public static int checkSelfPermission() {
        if (permissionGranted) {
            return PackageManager.PERMISSION_GRANTED;
        }
        try {
            permissionGranted = requireService().checkSelfPermission();
            return permissionGranted ? PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void requestPermission(int requestCode) {
        try {
            requireService().requestPermission(requestCode);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getVersion() {
        if (serverApiVersion != -1) return serverApiVersion;
        try {
            serverApiVersion = requireService().getVersion();
            return serverApiVersion;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            return -1;
        }
    }

    public static SheveryRemoteProcess newProcess(@NonNull String[] cmd, @Nullable String[] env, @Nullable String dir) {
        try {
            return new SheveryRemoteProcess(requireService().newProcess(cmd, env, dir));
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addBinderReceivedListener(@NonNull OnBinderReceivedListener listener) {
        addBinderReceivedListener(listener, null);
    }

    public static void addBinderReceivedListener(@NonNull OnBinderReceivedListener listener, @Nullable Handler handler) {
        RECEIVED_LISTENERS.add(new ListenerEntry<>(listener, handler));
        if (pingBinder() && binderReady) {
            postDispatch(handler, listener::onBinderReceived);
        }
    }

    public static void removeBinderReceivedListener(@NonNull OnBinderReceivedListener listener) {
        RECEIVED_LISTENERS.remove(new ListenerEntry<>(listener, null));
    }

    public static void addBinderDeadListener(@NonNull OnBinderDeadListener listener) {
        addBinderDeadListener(listener, null);
    }

    public static void addBinderDeadListener(@NonNull OnBinderDeadListener listener, @Nullable Handler handler) {
        DEAD_LISTENERS.add(new ListenerEntry<>(listener, handler));
    }

    public static void removeBinderDeadListener(@NonNull OnBinderDeadListener listener) {
        DEAD_LISTENERS.remove(new ListenerEntry<>(listener, null));
    }

    public static void addRequestPermissionResultListener(@NonNull OnPermissionResultListener listener) {
        addRequestPermissionResultListener(listener, null);
    }

    public static void addRequestPermissionResultListener(@NonNull OnPermissionResultListener listener, @Nullable Handler handler) {
        PERMISSION_LISTENERS.add(new ListenerEntry<>(listener, handler));
    }

    public static void removeRequestPermissionResultListener(@NonNull OnPermissionResultListener listener) {
        PERMISSION_LISTENERS.remove(new ListenerEntry<>(listener, null));
    }

    private static void dispatchBinderReceived() {
        for (ListenerEntry<OnBinderReceivedListener> entry : RECEIVED_LISTENERS) {
            postDispatch(entry.handler, entry.listener::onBinderReceived);
        }
    }

    private static void dispatchBinderDead() {
        for (ListenerEntry<OnBinderDeadListener> entry : DEAD_LISTENERS) {
            postDispatch(entry.handler, entry.listener::onBinderDead);
        }
    }

    private static void dispatchPermissionResult(int requestCode, int result) {
        for (ListenerEntry<OnPermissionResultListener> entry : PERMISSION_LISTENERS) {
            postDispatch(entry.handler, () -> entry.listener.onPermissionResult(requestCode, result));
        }
    }

    private static void postDispatch(@Nullable Handler handler, @NonNull Runnable action) {
        if (handler != null) {
            handler.post(action);
        } else if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run();
        } else {
            MAIN_HANDLER.post(action);
        }
    }
}
