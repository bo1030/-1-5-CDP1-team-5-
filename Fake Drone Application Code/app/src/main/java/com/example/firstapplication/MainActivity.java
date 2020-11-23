package com.example.firstapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import android.view.View;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MainActivity extends AppCompatActivity implements Button.OnClickListener{

    Button startBtn, stopBtn;
    TextView battery, positionPitch, positionRoll, positionYaw , vibrationX, vibrationY, vibrationZ, controlPitch, controlRoll, controlYaw, locationX, locationY, locationZ, mod, ekfStatus, task;

    private static CSEBase csebase = new CSEBase();
    private static AE ae = new AE();
    private static String TAG = "MainActivity";
    private String MQTTPort = "1883";
    private String ServiceAEName = "CDP5";
    private String MQTT_Req_Topic = "";
    private String MQTT_Resp_Topic = "";
    private ParseElementXml par = null;
    private String Mobius_Address ="13.209.165.214";
    public Handler handler;
    RetrieveRequest bat;
    RetrieveRequest con;
    RetrieveRequest loc;
    RetrieveRequest tas;
    RetrieveRequest EKF;
    RetrieveRequest vib;
    RetrieveRequest pos;
    RetrieveRequest mode;
    public MainActivity() {
        handler = new Handler();
        par = new ParseElementXml();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = (Button) findViewById(R.id.startButton);
        stopBtn = (Button) findViewById(R.id.stopButton);
        startBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);

        battery = (TextView) findViewById(R.id.batteryInfo);

        positionPitch = (TextView) findViewById(R.id.pitchPosition);
        positionRoll = (TextView) findViewById(R.id.rollPosition);
        positionYaw = (TextView) findViewById(R.id.yawPosition);

        vibrationX = (TextView) findViewById(R.id.xVibration);
        vibrationY = (TextView) findViewById(R.id.yVibration);
        vibrationZ = (TextView) findViewById(R.id.zVibration);

        controlPitch = (TextView) findViewById(R.id.pitchControl);
        controlRoll = (TextView) findViewById(R.id.rollControl);
        controlYaw = (TextView) findViewById(R.id.yawControl);

        locationX = (TextView) findViewById(R.id.xLocation);
        locationY = (TextView) findViewById(R.id.yLocation);
        locationZ = (TextView) findViewById(R.id.zLocation);

        mod = (TextView) findViewById(R.id.flightMode);

        ekfStatus = (TextView) findViewById(R.id.efkInfo);
        task = (TextView) findViewById(R.id.missionInfo);
    }

    public void GetAEInfo() {
        csebase.setInfo(Mobius_Address,"7579","Mobius","1883");

        //csebase.setInfo("203.253.128.151","7579","Mobius","1883");
        // AE Create for Android AE
        ae.setAppName("ncubeapp");
        aeCreateRequest aeCreate = new aeCreateRequest();
        aeCreate.setReceiver(new IReceived() {
            public void getResponseBody(final String msg) {
                handler.post(new Runnable() {
                    public void run() {
                        Log.d(TAG, "** AE Create ResponseCode[" + msg +"]");
                        if( Integer.parseInt(msg) == 201 ){
                            MQTT_Req_Topic = "/oneM2M/req/Mobius2/"+ae.getAEid()+"_sub"+"/#";
                            MQTT_Resp_Topic = "/oneM2M/resp/Mobius2/"+ae.getAEid()+"_sub"+"/json";
                            Log.d(TAG, "ReqTopic["+ MQTT_Req_Topic+"]");
                            Log.d(TAG, "ResTopic["+ MQTT_Resp_Topic+"]");
                        }
                        else { // If AE is Exist , GET AEID
                            aeRetrieveRequest aeRetrive = new aeRetrieveRequest();
                            aeRetrive.setReceiver(new IReceived() {
                                public void getResponseBody(final String resmsg) {
                                    handler.post(new Runnable() {
                                        public void run() {
                                            Log.d(TAG, "** AE Retrive ResponseCode[" + resmsg +"]");
                                            MQTT_Req_Topic = "/oneM2M/req/Mobius2/"+ae.getAEid()+"_sub"+"/#";
                                            MQTT_Resp_Topic = "/oneM2M/resp/Mobius2/"+ae.getAEid()+"_sub"+"/json";
                                            Log.d(TAG, "ReqTopic["+ MQTT_Req_Topic+"]");
                                            Log.d(TAG, "ResTopic["+ MQTT_Resp_Topic+"]");
                                        }
                                    });
                                }
                            });
                            aeRetrive.start();
                        }
                    }
                });
            }
        });
        aeCreate.start();
    }

    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startButton:{
                bat = new RetrieveRequest("battery");
                con = new RetrieveRequest("control");
                loc = new RetrieveRequest("location");
                tas = new RetrieveRequest("task");
                EKF = new RetrieveRequest("EKF");
                vib = new RetrieveRequest("vibration");
                pos = new RetrieveRequest("position");
                mode = new RetrieveRequest("flightmode");
                bat.setReceiver(new IReceived() {
                    public void getResponseBody(final String msg) {
                        handler.post(new Runnable() {
                            public void run() {
                                battery.setText(par.GetElementJson(par.GetElementXml(msg,"con"),"battery"));
                            }
                        });
                    }
                });
                bat.start();

                con.setReceiver(new IReceived() {
                    public void getResponseBody(final String msg) {
                        handler.post(new Runnable() {
                            public void run() {
                                controlPitch.setText(par.GetElementJson(par.GetElementXml(msg,"con"),"Pitch"));
                                controlRoll.setText(par.GetElementJson(par.GetElementXml(msg,"con"),"Roll"));
                                controlYaw.setText(par.GetElementJson(par.GetElementXml(msg,"con"),"Yaw"));
                            }
                        });
                    }
                });
                con.start();

                loc.setReceiver(new IReceived() {
                    public void getResponseBody(final String msg) {
                        handler.post(new Runnable() {
                            public void run() {
                                locationX.setText(par.GetElementJson(par.GetElementXml(msg,"con"),"x"));
                                locationY.setText(par.GetElementJson(par.GetElementXml(msg,"con"),"y"));
                                locationZ.setText(par.GetElementJson(par.GetElementXml(msg,"con"),"z"));
                            }
                        });
                    }
                });
                loc.start();

                tas.setReceiver(new IReceived() {
                    public void getResponseBody(final String msg) {
                        handler.post(new Runnable() {
                            public void run() {
                                task.setText(par.GetElementJson(par.GetElementXml(msg,"con"),"task"));
                            }
                        });
                    }
                });
                tas.start();

                EKF.setReceiver(new IReceived() {
                    public void getResponseBody(final String msg) {
                        handler.post(new Runnable() {
                            public void run() {
                                ekfStatus.setText(par.GetElementJson(par.GetElementXml(msg,"con"),"information"));
                            }
                        });
                    }
                });
                EKF.start();

                vib.setReceiver(new IReceived() {
                    public void getResponseBody(final String msg) {
                        handler.post(new Runnable() {
                            public void run() {
                                vibrationX.setText(par.GetElementJson(par.GetElementXml(msg,"con"),"x_vibration"));
                                vibrationY.setText(par.GetElementJson(par.GetElementXml(msg,"con"),"y_vibration"));
                                vibrationZ.setText(par.GetElementJson(par.GetElementXml(msg,"con"),"z_vibration"));
                            }
                        });
                    }
                });
                vib.start();

                pos.setReceiver(new IReceived() {
                    public void getResponseBody(final String msg) {
                        handler.post(new Runnable() {
                            public void run() {
                                positionPitch.setText(par.GetElementJson(par.GetElementXml(msg,"con"),"Pitch"));
                                positionRoll.setText(par.GetElementJson(par.GetElementXml(msg,"con"),"Roll"));
                                positionYaw.setText(par.GetElementJson(par.GetElementXml(msg,"con"),"Yaw"));
                            }
                        });
                    }
                });
                pos.start();

                mode.setReceiver(new IReceived() {
                    public void getResponseBody(final String msg) {
                        handler.post(new Runnable() {
                            public void run() {
                                mod.setText(par.GetElementJson(par.GetElementXml(msg,"con"),"mode"));
                            }
                        });
                    }
                });
                mode.start();

                break;  
            }
            case R.id.stopButton:{
                break;
            }
        }
    }

     @Override
    public void onStart() {
        super.onStart();

    }
    @Override
    public void onStop() {
        super.onStop();

    }

    public interface IReceived {
        void getResponseBody(String msg);
    }

    class RetrieveRequest extends Thread {
        private final Logger LOG = Logger.getLogger(RetrieveRequest.class.getName());
        private IReceived receiver;
        private String ContainerName;

        public RetrieveRequest(String containerName) {
            this.ContainerName = containerName;
        }
        public RetrieveRequest() {}
        public void setReceiver(IReceived hanlder) { this.receiver = hanlder; }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl() + "/" + ServiceAEName + "/" + ContainerName + "/" + "latest";

                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", ae.getAEid() );
                conn.setRequestProperty("nmtype", "long");
                conn.connect();

                String strResp = "";
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String strLine= "";
                while ((strLine = in.readLine()) != null) {
                    strResp += strLine;
                }

                if ( strResp != "" ) {
                    receiver.getResponseBody(strResp);
                }
                conn.disconnect();

            } catch (Exception exp) {
                LOG.log(Level.WARNING, exp.getMessage());
            }
        }
    }

     class aeCreateRequest extends Thread {
        private final Logger LOG = Logger.getLogger(aeCreateRequest.class.getName());
        String TAG = aeCreateRequest.class.getName();
        private IReceived receiver;
        int responseCode=0;
        public ApplicationEntityObject applicationEntity;
        public void setReceiver(IReceived hanlder) { this.receiver = hanlder; }
        public aeCreateRequest(){
            applicationEntity = new ApplicationEntityObject();
            applicationEntity.setResourceName(ae.getappName());
        }
        @Override
        public void run() {
            try {

                String sb = csebase.getServiceUrl();
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+xml;ty=2");
                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("locale", "ko");
                conn.setRequestProperty("X-M2M-Origin", "S"+ae.getappName());
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-NM", ae.getappName() );

                String reqXml = applicationEntity.makeXML();
                conn.setRequestProperty("Content-Length", String.valueOf(reqXml.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqXml.getBytes());
                dos.flush();
                dos.close();

                responseCode = conn.getResponseCode();

                BufferedReader in = null;
                String aei = "";
                if (responseCode == 201) {
                    // Get AEID from Response Data
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String resp = "";
                    String strLine;
                    while ((strLine = in.readLine()) != null) {
                        resp += strLine;
                    }

                    ParseElementXml pxml = new ParseElementXml();
                    aei = pxml.GetElementXml(resp, "aei");
                    ae.setAEid( aei );
                    Log.d(TAG, "Create Get AEID[" + aei + "]");
                    in.close();
                }
                if (responseCode != 0) {
                    receiver.getResponseBody( Integer.toString(responseCode) );
                }
                conn.disconnect();
            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }

        }
    }

    class aeRetrieveRequest extends Thread {
        private final Logger LOG = Logger.getLogger(aeCreateRequest.class.getName());
        private IReceived receiver;
        int responseCode=0;

        public aeRetrieveRequest() {
        }
        public void setReceiver(IReceived hanlder) {
            this.receiver = hanlder;
        }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl()+"/"+ ae.getappName();
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", "Sandoroid");
                conn.setRequestProperty("nmtype", "short");
                conn.connect();

                responseCode = conn.getResponseCode();
                BufferedReader in = null;
                String aei = "";
                if (responseCode == 200) {
                    // Get AEID from Response Data
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String resp = "";
                    String strLine;
                    while ((strLine = in.readLine()) != null) {
                        resp += strLine;
                    }

                    ParseElementXml pxml = new ParseElementXml();
                    aei = pxml.GetElementXml(resp, "aei");
                    ae.setAEid( aei );
                    //Log.d(TAG, "Retrieve Get AEID[" + aei + "]");
                    in.close();
                }
                if (responseCode != 0) {
                    receiver.getResponseBody( Integer.toString(responseCode) );
                }
                conn.disconnect();
            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }
}