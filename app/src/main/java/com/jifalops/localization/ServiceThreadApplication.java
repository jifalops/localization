package com.jifalops.localization;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * ServiceThreadApplication provides an Application and related Service that can be bound to. See
 * {@link LocalService} for details.
 */
public class ServiceThreadApplication extends Application {
    public interface LocalServiceConnection {
        void onServiceConnected(LocalService service);
        void onServiceDisconnected(ComponentName className);
    }

    private LocalService service;
    private boolean isBound;
    private LocalServiceConnection conn;
    private boolean isPersistent;
    private Handler handler;

    private final ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            ServiceThreadApplication.this.service = ((LocalService.LocalBinder) service).getService();
            if (conn != null) conn.onServiceConnected(ServiceThreadApplication.this.service);
        }
        public void onServiceDisconnected(ComponentName className) {
            service = null;
            if (conn != null) conn.onServiceDisconnected(className);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    public void bindLocalService(Context ctx, LocalServiceConnection conn) {
        if (!isBound) {
            this.conn = conn;
            bindService(new Intent(ctx, LocalService.class),
                    connection, Context.BIND_AUTO_CREATE);
            isBound = true;
        }
    }

    public void unbindLocalService(LocalServiceConnection conn) {
        if (isBound) {
            this.conn = conn;
            unbindService(connection);
            isBound = false;
        }
    }

    public boolean isPersistent() {
        return isPersistent;
    }

    /**
     * Set whether the service should continue to run in the background.
     * The target activity should have either android:launchMode="singleInstance" or
     * android:launchMode="singleTask" set to avoid multiple instances.
     */
    public void setPersistent(boolean persist, @Nullable Class<? extends Activity> notificationTarget) {
        isPersistent = persist;
        Intent i = new Intent(this, LocalService.class);
        i.putExtra("notificationTarget", new Intent(this, notificationTarget));
        if (persist) {
            startService(i);
        } else {
            stopService(i);
        }
    }

    /**
     * Convenience for sending app-wide broadcasts
     */
    public void broadcast(String intentAction) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(
                new Intent(intentAction));
    }

    @Nullable
    public LocalService getService() {
        return service;
    }

    /** Post to UI thread. {@link Handler#post(Runnable)} */
    public boolean post(Runnable r) {
        return handler.post(r);
    }
    /** Post to UI thread. {@link Handler#postDelayed(Runnable, long)} */
    public boolean postDelayed(Runnable r, long delayMillis) {
        return handler.postDelayed(r, delayMillis);
    }

    /**
     * The service manages its own thread and exposes two methods from the thread's Handler,
     * {@link #post(Runnable)} ()} and {@link #postDelayed(Runnable, long)}.
     */
    public static class LocalService extends Service {
        private HandlerThread mHandlerThread;
        private Handler mServiceHandler;
        private final IBinder mBinder = new LocalBinder();
        // Activities can use when running in the background.
        private Map<String, Object> cache = new HashMap<>();
        private final int NOTIFICATION_ID = R.string.app_persisting;

        private class LocalBinder extends Binder {
            LocalService getService() {
                return LocalService.this;
            }
        }

        @Override
        public void onCreate() {
            mHandlerThread = new HandlerThread(getClass().getName());
            mHandlerThread.start();
            mServiceHandler = new Handler(mHandlerThread.getLooper());
        }

        @Override
        public void onDestroy() {
            mServiceHandler.removeCallbacksAndMessages(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mHandlerThread.quitSafely();
            } else {
                mHandlerThread.quit();
            }
        }

        @Override
        public IBinder onBind(Intent intent) {
            return mBinder;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.i("LocalService", "Received start id " + startId + ": " + intent);
            if (intent != null) {
                Intent target = intent.getParcelableExtra("notificationTarget");
                if (target != null) showNotification(target);
            }
            return START_STICKY;
        }

        @Override
        public boolean stopService(Intent name) {
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
            return super.stopService(name);
        }


        /** Post to service thread. {@link Handler#post(Runnable)} */
        public boolean post(Runnable r) {
            return mServiceHandler.post(r);
        }
        /** Post to service thread. {@link Handler#postDelayed(Runnable, long)} */
        public boolean postDelayed(Runnable r, long delayMillis) {
            return mServiceHandler.postDelayed(r, delayMillis);
        }

        public Thread.State getThreadState() {
            return mHandlerThread.getState();
        }

        public Object getCachedObject(String key) {
            return cache.get(key);
        }
        /** Returns the displaced object if one exists. */
        public Object setCachedObject(String key, Object obj) {
            return cache.put(key, obj);
        }


        private void showNotification(Intent target) {
            CharSequence text = getString(R.string.app_persisting, getText(R.string.app_name));

            if (target == null) {
                // Start the main activity for the app when the notification is clicked
                target = getPackageManager().getLaunchIntentForPackage(getPackageName());
            }

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, target, 0);

            // Set the info for the views that show in the notification panel.
            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)  // the status icon
                    .setTicker(text)  // the status text
                    .setWhen(System.currentTimeMillis())  // the time stamp
                    .setContentText(text)  // the contents of the entry
                    .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                    .build();
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
                .notify(NOTIFICATION_ID, notification);
        }
    }
}
