package main.utils;

import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import main.ExternalCall;

/**
 * Created by yons on 17/2/17.
 */

public class SocketHelper {
    private static final String TAG = "SocketHelper";
    private JSONObject json;
    private String host;
    private int port;
    private int timeout;
    private int index;
    private int cmdid;
    private Socket socket;
    private JSONArray datas;
    private OutputStream out;

    public SocketHelper(String json, int cmdid) {
        try {
            this.cmdid = cmdid;
            this.json = new JSONObject(json);
            parse();
        } catch (JSONException e) {
            Log.e(TAG, "SocketHelper: it's not json array: " + json);
            e.printStackTrace();
        }
    }

    private void parse() {
        try {
            host = json.getString("host");
            port = json.getInt("port");
            timeout = json.getInt("timeout");
            datas = json.getJSONArray("datas");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void newConnect() {

        try {
            new SocketConnect(datas.getString(index)).start();
        } catch (JSONException e) {
            Log.d(TAG, "newConnect: datas is not array: " + datas.toString());
            e.printStackTrace();
        }
    }


    private class SocketConnect extends Thread {
        private String data;

        SocketConnect(String data) {
            this.data = data;
            Log.d(TAG, "SocketConnect: " + host + port + timeout);
            Log.d(TAG, "SocketConnect: data  " + data);
        }

        @Override
        public void run() {
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        receive();
                    }
                }).start();

                socket = new Socket(host, port);
                socket.setSoTimeout(timeout);
                out = socket.getOutputStream();

                for (int i = 0; i < datas.length(); i++) {
                    if (socket.isClosed()) {
                        return;
                    }
                    String data = datas.getString(i);
                    sendData(data);
                }

            } catch (UnknownHostException e) {
                Log.d(TAG, "Unknown host: " + host);
            } catch (IOException e) {
                Log.d(TAG, "No I/O");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        private void sendData(String data) throws IOException {
            byte[] stream = Base64.decode(data, Base64.DEFAULT);
            out.write(stream);
            out.flush();
        }

        private void receive() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
                        1024);
                byte[] buffer = new byte[1024];
                int bytesRead;
                InputStream inputStream = socket.getInputStream();

                //notice: inputStream.read() will block if no data return
                String response = "";
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");
                }
                ExternalCall.sendMessageToGame(cmdid, response);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendFinished() {
            index++;
            try {
                sendData(datas.getString(index));

            } catch (IOException | JSONException e) {
                Log.e(TAG, "sendFinished: can't get next data at index:" + index + ", length is " + datas.length());
                e.printStackTrace();
            }
        }
    }


}
