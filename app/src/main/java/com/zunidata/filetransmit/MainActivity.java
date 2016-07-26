package com.zunidata.filetransmit;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zunidata.ethernethelper.EthManager;
import com.zunidata.wifihelper.StaticIpSet;

import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {
    private TextView tvMsg;
    private EditText txtIP, txtPort, txtEt;
    private Button btnSend;
    private Button btnStop;
    private Handler handler;
    private ServerSocket server;
    private Thread sendThread;
    private Thread receiveThread;
    private long sendCount;
    private long receiveCount;
    private SocketManager socketManager;
    private boolean isRunning;
    private EthManager mEthManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initControler();
    }

    private void initData() {
        socketManager = new SocketManager();
        mEthManager = new EthManager(this);
        tvMsg = (TextView) findViewById(R.id.tvMsg);
        txtIP = (EditText) findViewById(R.id.txtIP);
        txtPort = (EditText) findViewById(R.id.txtPort);
        txtEt = (EditText) findViewById(R.id.et);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnSend = (Button) findViewById(R.id.btnSend);
//        setStaticIP();
    }

    private void initControler() {
        btnSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent intent = new Intent(getApplicationContext(),
                // FilesViewActivity.class);
                // startActivityForResult(intent, 0);
                btnSend.setEnabled(false);
                setRunning(true);
                clearData();
                final String fileName = "android.txt";
                final String filePath = "/mnt/internal_sd/" + fileName;
                final String ipAddress = txtIP.getText().toString();
                final int port = Integer.parseInt(txtPort.getText().toString());
                Message.obtain(handler, 0, fileName + " Sending to " + ipAddress + ":" + port).sendToTarget();
                sendThread = new Thread(new Runnable() {
                    String responce = null;

                    @Override
                    public void run() {
                        while (isRunning) {
                            sendCount++;
                            String response = socketManager.SendFile(fileName, filePath, ipAddress, port);
                            Message.obtain(handler, 0, response).sendToTarget();
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                sendThread.start();
            }
        });
        btnStop.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(isRunning){
                    setRunning(false);
                    try {
                        sendThread.join(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }finally {
                        getSummary();
                        btnSend.setEnabled(true);
                    }
                }
            }
        });
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
                        txtEt.append("\n[" + format.format(new Date()) + "]" + msg.obj.toString());
                        break;
                    case 1:
                        tvMsg.setText(msg.obj.toString());
                    case 2:
                        Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        };
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // �󶨶˿�
                int port = 9999;
                while (port > 9000) {
                    try {
                        server = new ServerSocket(port);
                        break;
                    } catch (Exception e) {
                        port--;
                    }
                }
                if (server != null) {
                    socketManager.setServer(server);
                    Message.obtain(handler, 1, "IP:" + GetIpAddress() + "\tlistening port:" + port).sendToTarget();
                    while (true) {// 循環發送
                        receiveCount++;
                        String response = socketManager.ReceiveFile();
                        Message.obtain(handler, 0, response).sendToTarget();
                    }
                } else {
                    Message.obtain(handler, 1, "can't bind any port").sendToTarget();
                }
            }
        });
        receiveThread.start();
    }

    private void clearData(){
        sendCount = 0;
        receiveCount = 0;
        socketManager.setRxFailCount(0);
        socketManager.setTxFailCount(0);
        txtEt.setText("");
    }
	/*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// ѡ�����ļ�����
		if (resultCode == RESULT_OK) {
			final String fileName = data.getStringExtra("FileName");
			final String path = data.getStringExtra("FilePath");
			final String ipAddress = txtIP.getText().toString();
			final int port = Integer.parseInt(txtPort.getText().toString());
			Message.obtain(handler, 0, fileName + " ���ڷ�����" + ipAddress + ":" + port).sendToTarget();
			Thread sendThread = new Thread(new Runnable() {
				@Override
				public void run() {
					String response = socketManager.SendFile(fileName, path, ipAddress, port);
					Message.obtain(handler, 0, response).sendToTarget();
				}
			});
			sendThread.start();
		}
	}*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }

    public String GetIpAddress() {
       /* ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo.getType() == cm.TYPE_WIFI) {
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
        } else {*/
            return mEthManager.getIP();
//        }

    }

    private void getSummary() {
        String result = "\n------------------------------------------------------\n" + "Tx:" + sendCount + "\tfail:"
                + socketManager.getTxSummary() + "\tRx:" + receiveCount + "\tfail:" + socketManager.getRxSummary()
                + "\n" + "------------------------------------------------------\n";
        Message.obtain(handler, 0, result).sendToTarget();
//        Message msg = new Message();
//        msg.what = 0;
//        msg.obj = result;
//        handler.sendMessageDelayed(msg,1000);
//        txtEt.append(result);
    }

    private void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    private void setStaticIP() {
//        EthManager mEthManager = new EthManager(getApplicationContext());
        mEthManager.disable();
        mEthManager.setMode(EthManager.STATIC_IP_MODE);
        mEthManager.setIP(getResources().getString(R.string.ip));
        mEthManager.setMask(getResources().getString(R.string.netmask));
        mEthManager.setGateway(getResources().getString(R.string.gateway));
        mEthManager.setDNS(getResources().getString(R.string.dns));
        mEthManager.enable();
    }

    private void wifiConneted(){
        //获得当前在已经连接的wifi配置对象
        WifiConfiguration wifiConfig = null;
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        List<WifiConfiguration> configuredNetworks = wifiManager
                .getConfiguredNetworks();
        for (WifiConfiguration conf : configuredNetworks) {
            if (conf.networkId == connectionInfo.getNetworkId()) {
                wifiConfig = conf;
                break;
            }
        }
        //获得ip数据包
        String ipAddress = "192.168.1.155";
        int preLength = 24;
        String getWay = "192.168.1.1";
        String dns1 = "192.168.1.1";

        //接受ip数据包，配置指定的wifi配置对象
        new StaticIpSet(this, ipAddress, preLength, getWay, dns1).confingStaticIp(wifiConfig);

        //更新指定的wifi配置对象并连接
        wifiManager.updateNetwork(wifiConfig);
    }
}
