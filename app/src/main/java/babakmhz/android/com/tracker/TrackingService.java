package babakmhz.android.com.tracker;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class TrackingService extends Service {

    private static final String TAG = "TrackingService";
    private static final String SERVER_IP = "192.168.43.92";
    private static final String NOTIFICATION_CHANNEL_ID = "my_channel0";
    private static final int SERVER_PORT = 8885;
    private Intent intent;
    private Binder serviceBinder = new Binder();
    private Socket client;
    private Notification notification;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        this.intent = intent;
        return serviceBinder;
    }

    private void setupConnection() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //ادرس رو آی پی سیستم خودتون بزارید و پورت رو پورتی که روی سرور تعریف کردید
                    while (true) {
                        client = new Socket(SERVER_IP, SERVER_PORT);
                        Scanner in1 = null;
                        try {
                            in1 = new Scanner(client.getInputStream());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String mes;
                        while (true) {
                            if (in1.hasNext()) {
                                mes = in1.next();
                                Log.i(TAG, "MESSAGE: " + mes);

                            } else
                                break;
                        }
                        client.close();
                    }
                } catch (RuntimeException | IOException ignored) {

                }
            }

        }).start();


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupConnection();
        setupNotification();
        return START_STICKY;
    }

    private void setupNotification() {


        notification = new NotificationCompat.Builder(this)
                .setContentTitle(this.getString(R.string.app_name))
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .build();


        startForeground(101, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (client != null) {
            try {
                client.close();
                client = null;
                stopForeground(true);
                stopSelf();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public class Binder extends android.os.Binder {

        public TrackingService getService() {
            return TrackingService.this;
        }
    }
}
