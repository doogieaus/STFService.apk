package jp.co.cyberagent.stf.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.net.ConnectivityManager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import jp.co.cyberagent.stf.io.MessageWritable;
import jp.co.cyberagent.stf.proto.Wire;

public class NetworkMonitor extends AbstractMonitor {
    private static final String TAG = "STFNetworkMonitor";

    public NetworkMonitor(Context context, MessageWritable writer) {
        super(context, writer);
    }

    public static String getLocalIpAddresses(){
        String ipAddressList = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements();) {
                NetworkInterface networkInterface = en.nextElement();
                String networkInterfaceName = networkInterface.getName();
                for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        ipAddressList += networkInterfaceName + "=" + inetAddress.getHostAddress() + ";";
                    }
                }
            }
        } catch (Exception ex) {
            // Don't do anything
        }
        return ipAddressList;
    }

    @Override
    public void run() {
        Log.i(TAG, "Monitor starting");

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                report(writer);
            }
        };

        context.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        try {
            synchronized (this) {
                while (!isInterrupted()) {
                    wait();
                }
            }
        }
        catch (InterruptedException e) {
            // Okay
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            Log.i(TAG, "Monitor stopping");

            context.unregisterReceiver(receiver);
        }
    }

    @Override
    public void peek(MessageWritable writer) {
        if (Build.VERSION.SDK_INT >= 17) {
            report(writer);
        }
        else {
            report(writer);
        }
    }

    private void report(MessageWritable writer) {
        Log.i(TAG, String.format("Network configuration has changed"));

        writer.write(Wire.Envelope.newBuilder()
            .setType(Wire.MessageType.EVENT_NETWORK_CONNECTIVITY)
            .setMessage(Wire.NetworkConnectivityEvent.newBuilder()
                .setIpAddressList(NetworkMonitor.getLocalIpAddresses())
                .build()
                .toByteString())
            .build());
    }
}
