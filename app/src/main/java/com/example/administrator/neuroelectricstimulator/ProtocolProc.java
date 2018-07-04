package com.example.administrator.neuroelectricstimulator;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by Administrator on 2018/5/23.
 */

public class ProtocolProc {


    private static final int MSG_CMD_0 =10;
    private static final int MSG_CMD_1 =11;
    private static final int MSG_CMD_2 =12;
    private static final int MSG_CMD_3 =13;
    private static final int MSG_CMD_4 =14;
    private static final int MSG_CMD_5 =15;
    private static final int MSG_CMD_6 =16;
    private static final int MSG_CMD_7 =17;
    private static final int MSG_CMD_AUTO =51;
    private static final int MSG_BLE_DOWN =52;
    private static final int MSG_NEW_DATA=53;

    private static final int DEV_TYPE_0_SHOUJI =0;
    private static final int DEV_TYPE_1_SHOUBIAO=1;
    private static final int DEV_TYPE_2_ZUSANLI=2;
    private static final int DEV_TYPE_3_ZHENJIU=3;
    private static final int MSG_ADD_OK=61;
    private static final int MSG_REDUCE_OK=62;
    private byte[] logBuffer;
    private int contentLength=32;
    private int electric_max=190;
    private byte[] buffer;
    public Handler mHandler=null;
    private int bufferLen;
    private int  tmpIndex;
    private int   procIndex;
    private int frame_len;
    public Context context;
    private boolean debug_enabled=false;
    private final String TAG="@@@@DEBUG@@@@@@@@@@@@@@@@@@";
    //call this  only once
    public void init_sys(Handler _handler,Context _context)
    {
        if(mHandler==null)
        {
            mHandler=_handler;
        }
        context=_context;
        bufferLen=2000;
        buffer=new byte[bufferLen];
        tmpIndex=0;procIndex=0;
        frame_len=40;
        logBuffer=new byte[contentLength];
    }

    private void my_debug(){
        Log.d(TAG, "医疗产品");
        Log.d(TAG,String.format("our tmpIndex is %d", tmpIndex));
        Log.d(TAG,String.format("our procIndex is %d", procIndex));
        byte[] newd=new byte[tmpIndex];
        for(int i=0;i<tmpIndex;i++){
            newd[i]=buffer[i];
        }
        String str= ConvertData.bytesToHexString(newd, false);
        Log.d(TAG,str);
    }
    private void my_debug2(byte a,String _a){
        Log.d(TAG,String.format("our _a is %d", a));

    }
    private void my_debug_comply(){
        Log.d(TAG,String.format("Something does comply"));

    }
    public void onIncomingData(byte[] incoming,int connNum)
    {
        int len=incoming.length;
        for(int i=0;i<len;i++)
        {
            buffer[tmpIndex%bufferLen]=incoming[i];
            tmpIndex++;
        }
        process_data(connNum);
    }
    private void process_cmd(int cmd,int connNum){
        Message msg=new Message();
        ExtraInfo info=new ExtraInfo();
        switch(cmd)
        {
            case 0:
                info.DevID=new byte[6];
                info.DevID[0]=buffer[(procIndex+3)%bufferLen];
                info.DevID[1]=buffer[(procIndex+4)%bufferLen];
                info.DevID[2]=buffer[(procIndex+5)%bufferLen];
                info.DevID[3]=buffer[(procIndex+6)%bufferLen];
                info.DevID[4]=buffer[(procIndex+7)%bufferLen];
                info.DevID[5]=buffer[(procIndex+8)%bufferLen];

                info.DevType=buffer[(procIndex+4)%bufferLen];
                info.channelType=buffer[(procIndex+5)%bufferLen];
                info.Energy=buffer[(procIndex+9)%bufferLen];
                info.StimuStatus=buffer[(procIndex+10)%bufferLen];
                info.Current=0xFF&(buffer[(procIndex+11)%bufferLen]);
                info.Current2=0xFF&(buffer[(procIndex+12)%bufferLen]);
                info.ConnNum=connNum;
                msg.what=MSG_CMD_0;
                msg.obj=info;
                mHandler.sendMessage(msg);
                break;
            case 1:
                info.channelType=buffer[(procIndex+3)%bufferLen];
                info.Current=0xFF&(buffer[(procIndex+4)%bufferLen]);
                info.ConnNum=connNum;
                msg.what=MSG_CMD_1;
                msg.obj=info;
                mHandler.sendMessage(msg);
                break;
            case 2:
            case 3:
                info.PresID=buffer[(procIndex+3)%bufferLen];
                //info.Freq=buffer[(procIndex+4)%bufferLen];
                info.newFreq[0]=buffer[(procIndex+4)%bufferLen];
                info.newFreq[1]=buffer[(procIndex+5)%bufferLen];
                info.PWM[0]=buffer[(procIndex+6)%bufferLen];
                info.PWM[1]=buffer[(procIndex+7)%bufferLen];
                info.Type=buffer[(procIndex+8)%bufferLen];
                info.OnTime[0]=buffer[(procIndex+9)%bufferLen];
                info.OnTime[1]=buffer[(procIndex+10)%bufferLen];
                info.OffTime[0]=buffer[(procIndex+11)%bufferLen];
                info.OffTime[1]=buffer[(procIndex+12)%bufferLen];
                info.Overal=buffer[(procIndex+13)%bufferLen];
                //info.Current=buffer[(procIndex+13)%bufferLen];
                msg.what=MSG_CMD_3;
                msg.obj=info;
                mHandler.sendMessage(msg);
                break;
            case 4:
                int pages=0;
                pages=(int)(0xFF&(buffer[(procIndex+3)%bufferLen]))+(0xFF&(buffer[(procIndex+4)%bufferLen]))*256;
                //pages=(int)((buffer[(procIndex+4)%bufferLen])<<8)+buffer[(procIndex+3)%bufferLen];
                msg.what=MSG_CMD_4;
                msg.obj=pages;
                msg.arg1=connNum;
                mHandler.sendMessage(msg);
                break;
            case 5:
                //int currentPage=0;
                //currentPage=(int)(0xFF&(buffer[(procIndex+3)%bufferLen]))+(0xFF&(buffer[(procIndex+4)%bufferLen]))*256;
                msg.what=MSG_CMD_5;
                for(int i=0;i<logBuffer.length;i++){
                    logBuffer[i]=buffer[(procIndex+5+i)%bufferLen];
                }
                msg.obj=logBuffer;
                msg.arg1=connNum;
                mHandler.sendMessage(msg);
                break;
            case 7:
                info.StimuStatus=buffer[(procIndex+3)%bufferLen];
                info.ConnNum=connNum;
                msg.what=MSG_CMD_7;
                msg.obj=info;
                mHandler.sendMessage(msg);
                break;
            default:
                break;

        }
    }

    public void send_cmd_0(final HolloBluetooth ble, final  String address)
    {
        new Handler().post(new Runnable() {

                               @Override
                               public void run() {
                                   // TODO Auto-generated method stub
                                   try {
                                       byte[] bytes=getCmd0Bytes();
                                       if(!ble.sendData(bytes,address))
                                       {
                                           //Toast.makeText(context, context.getResources().getString(R.string.cmd0), Toast.LENGTH_SHORT).show();
                                       }
                                       else
                                       {
                                           Toast.makeText(context, context.getResources().getString(R.string.cmd0), Toast.LENGTH_SHORT).show();
                                       }
                                   }catch (Exception e){
                                       String str=e.toString();
                                   }
                               }
                           }

        );
    }
    public void send_cmd_1(final HolloBluetooth ble, final int value, final boolean isAdd, final int cha, final  String address)
    {
        new Handler().post(new Runnable() {

                               @Override
                               public void run() {
                                   // TODO Auto-generated method stub
                                   int new_value=0;
                                   if(isAdd){
                                       new_value=value+1;
                                       if(new_value>electric_max)
                                           new_value=electric_max;
                                   }else{
                                       new_value=value-1;
                                       if(new_value<1)
                                           new_value=1;
                                   }

                                   try{
                                       byte[] bytes=getCurrentBytes(new_value,cha);
                                       if(!ble.sendData(bytes,address))
                                       {
                                           Toast.makeText(context, context.getResources().getString(R.string.currentModFailed), Toast.LENGTH_SHORT).show();
                                       }
                                       else
                                       {
                                           Toast.makeText(context, context.getResources().getString(R.string.currentModOk), Toast.LENGTH_SHORT).show();
                                           //					if(isAdd)
                                           //					{
                                           //						//Toast.makeText(context, "add is ok", Toast.LENGTH_SHORT).show();
                                           //						Message msg=new Message();
                                           //						msg.what=MSG_ADD_OK;
                                           //						mHandler.sendMessage(msg);
                                           //					}else{
                                           //						//Toast.makeText(context, "reduce is ok", Toast.LENGTH_SHORT).show();
                                           //						Message msg=new Message();
                                           //						msg.what=MSG_REDUCE_OK;
                                           //						mHandler.sendMessage(msg);
                                           //					}
                                       }
                                   }catch (Exception e){
                                       String str=e.toString();
                                   }
                               }
                           }

        );
    }
    public void send_cmd_2(final HolloBluetooth ble, final  String address)
    {
        new Handler().post(new Runnable() {

                               @Override
                               public void run() {
                                   // TODO Auto-generated method stub
                                   try{
                                       byte[] bytesSend=getQueryPresBytes();
                                       if(!ble.sendData(bytesSend,address)){
                                           Toast.makeText(context, context.getResources().getString(R.string.queryPresFailed), Toast.LENGTH_SHORT).show();
                                       }else{
                                           Toast.makeText(context, context.getResources().getString(R.string.queryPresOK), Toast.LENGTH_SHORT).show();
                                       }
                                   }catch (Exception e){
                                       String str=e.toString();
                                   }
                               }
                           }
        );
    }
    public void send_cmd_3(final HolloBluetooth ble, final byte[] presData, final  String address)
    {
        new Handler().post(new Runnable() {

                               @Override
                               public void run() {
                                   // TODO Auto-generated method stub
                                   try{
                                       if(!ble.sendData(presData,address)){
                                           Toast.makeText(context, context.getResources().getString(R.string.downloadPresFailed), Toast.LENGTH_SHORT).show();
                                       }else{
                                           Toast.makeText(context, context.getResources().getString(R.string.downloadPresOK), Toast.LENGTH_SHORT).show();
                                       }
                                   }catch (Exception e){
                                       String str=e.toString();
                                   }
                               }
                           }
        );
    }
    public void send_cmd_7(final HolloBluetooth ble, final int cmdWord, final  String address)
    {
        new Handler().post(new Runnable() {

                               @Override
                               public void run() {
                                   // TODO Auto-generated method stub
                                   try{
                                       byte[] toSend=getCmd7Bytes(cmdWord);
                                       if(!ble.sendData(toSend,address)){
                                           if(cmdWord==2){
                                               Toast.makeText(context, context.getResources().getString(R.string.startDeviceFailed), Toast.LENGTH_SHORT).show();
                                           }
                                           if(cmdWord==0){
                                               Toast.makeText(context, context.getResources().getString(R.string.stopDeviceFailed), Toast.LENGTH_SHORT).show();
                                           }
                                       }else{
                                           if(cmdWord==2){
                                               Toast.makeText(context, context.getResources().getString(R.string.startDeviceOK), Toast.LENGTH_SHORT).show();
                                           }
                                           if(cmdWord==0){
                                               Toast.makeText(context, context.getResources().getString(R.string.stopDeviceOK), Toast.LENGTH_SHORT).show();
                                           }
                                       }
                                   }catch (Exception e){
                                       String str=e.toString();
                                   }
                               }
                           }
        );
    }
    public void send_cmd_4(final HolloBluetooth ble, final  String address)
    {
        new Handler().post(new Runnable() {

                               @Override
                               public void run() {
                                   // TODO Auto-generated method stub
                                   try{
                                       byte[] cmd4Bytes=getCmd4Bytes();
                                       if(!ble.sendData(cmd4Bytes,address)){
                                           Toast.makeText(context, context.getResources().getString(R.string.queryPagesFailed), Toast.LENGTH_SHORT).show();
                                       }else{
                                           Toast.makeText(context, context.getResources().getString(R.string.queryPagesOK), Toast.LENGTH_SHORT).show();
                                       }
                                   }catch (Exception e){
                                       String str=e.toString();
                                   }
                               }
                           }
        );
    }
    public void send_cmd_5(final HolloBluetooth ble, final int pageNumber, final  String address)
    {
        new Handler().post(new Runnable() {

                               @Override
                               public void run() {
                                   // TODO Auto-generated method stub
                                   try{
                                       byte[] cmd5Bytes=getCmd5Bytes(pageNumber);
                                       if(!ble.sendData(cmd5Bytes,address)){
                                           Toast.makeText(context, context.getResources().getString(R.string.getPagesFailed), Toast.LENGTH_SHORT).show();
                                       }else{
                                           Toast.makeText(context, context.getResources().getString(R.string.getPagesOK), Toast.LENGTH_SHORT).show();
                                       }
                                   }catch (Exception e){
                                       String str=e.toString();
                                   }
                               }
                           }
        );
    }
    public void send_cmd_6(final HolloBluetooth ble, final  String address)
    {
        new Handler().post(new Runnable() {

                               @Override
                               public void run() {
                                   // TODO Auto-generated method stub
                                   try{
                                       byte[] cmd6Bytes=getCmd6Bytes();
                                       if(!ble.sendData(cmd6Bytes,address)){
                                           Toast.makeText(context, context.getResources().getString(R.string.deletePagesFailed), Toast.LENGTH_SHORT).show();
                                       }else{
                                           Toast.makeText(context, context.getResources().getString(R.string.deletePagesOk), Toast.LENGTH_SHORT).show();
                                       }
                                   }catch (Exception e){
                                       String str=e.toString();
                                   }
                               }
                           }
        );
    }
    public byte[] getCmd5Bytes(final int pageNumber){
        int len=40;
        byte[] result =new byte[len];
        for(int i=0;i<len;i++){
            result[i]=0;
        }
        result[0]=result[1]=0x55;
        result[2]=5;
        result[3]=(byte) (pageNumber%256);
        result[4]=(byte) (pageNumber/256);

        int sum=0;
        for(int i=2;i<=len-4;i++)
        {
            sum+=0xFF&result[i];
        }
        sum%=256;


        result[len-3]=(byte)sum;
        result[len-2]=(byte)0xAA;
        result[len-1]=(byte)0xAA;
        return result;
    }
    public byte[] getCmd0Bytes(){
        int len=40;
        byte[] result =new byte[len];
        for(int i=0;i<len;i++){
            result[i]=0;
        }
        result[0]=result[1]=0x55;
        result[2]=0;
        //result[3]=(byte) (0xFF&cmdWord);
        Calendar cal=Calendar.getInstance();//(byte)(0xFF&())
        Log.d(TAG,String.format("In Calender six parameters are %d %d %d %d %d %d and",
                cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND)

        ));
        byte year=(byte)((cal.get(Calendar.YEAR)-2000)&0xFF);
        byte month=(byte)((cal.get(Calendar.MONTH)+1)&0xFF);
        byte day=(byte)(0xFF&(cal.get(Calendar.DAY_OF_MONTH)));
        byte hour=(byte)(0xFF&(cal.get(Calendar.HOUR_OF_DAY)));
        byte minute=(byte)(0xFF&(cal.get(Calendar.MINUTE)));
        byte second=(byte)(0xFF&(cal.get(Calendar.SECOND)));
        result[3]=year;
        result[4]=month;
        result[5]=day;
        result[6]=hour;
        result[7]=minute;
        result[8]=second;
        int sum=0;
        for(int i=2;i<=len-4;i++)
        {
            sum+=0xFF&result[i];
        }
        sum%=256;


        result[len-3]=(byte)sum;
        result[len-2]=(byte)0xAA;
        result[len-1]=(byte)0xAA;
        return result;
    }
    public byte[] getCmd4Bytes(){
        int len=40;
        byte[] result =new byte[len];
        for(int i=0;i<len;i++){
            result[i]=0;
        }
        result[0]=result[1]=0x55;
        result[2]=4;
        //result[3]=(byte) (0xFF&cmdWord);

        int sum=0;
        for(int i=2;i<=len-4;i++)
        {
            sum+=0xFF&result[i];
        }
        sum%=256;


        result[len-3]=(byte)sum;
        result[len-2]=(byte)0xAA;
        result[len-1]=(byte)0xAA;
        return result;
    }
    public byte[] getCmd6Bytes(){
        int len=40;
        byte[] result =new byte[len];
        for(int i=0;i<len;i++){
            result[i]=0;
        }
        result[0]=result[1]=0x55;
        result[2]=6;

        int sum=0;
        for(int i=2;i<=len-4;i++)
        {
            sum+=0xFF&result[i];
        }
        sum%=256;

        result[len-3]=(byte)sum;
        result[len-2]=(byte)0xAA;
        result[len-1]=(byte)0xAA;
        return result;
    }

    public byte[] getCmd7Bytes(int cmdWord){
        int len=40;
        byte[] result =new byte[len];
        for(int i=0;i<len;i++){
            result[i]=0;
        }
        result[0]=result[1]=0x55;
        result[2]=7;
        result[3]=(byte) (0xFF&cmdWord);

        int sum=0;
        for(int i=2;i<=len-4;i++)
        {
            sum+=0xFF&result[i];
        }
        sum%=256;


        result[len-3]=(byte)sum;
        result[len-2]=(byte)0xAA;
        result[len-1]=(byte)0xAA;
        return result;
    }
    public void process_data(int connNum){
        while((tmpIndex-procIndex)>=frame_len)
        {
            //my_debug();

            byte a= buffer[(int) (procIndex%bufferLen)];
            byte b= buffer[(int) ((procIndex+1)%bufferLen)];
            byte c= buffer[(int) ((procIndex+frame_len-2)%bufferLen)];
            byte d= buffer[(int) ((procIndex+frame_len-1)%bufferLen)];
//			my_debug2(a, "a");
//			my_debug2(b, "b");
//			my_debug2(c, "c");
//			my_debug2(d, "d");

            if(a==0x55 && b==0x55 && (c&0xFF)==0xAA && (d&0xFF)==0xAA)
            {

                int sum=0;
                for(int i=2;i<=frame_len-4;i++){
                    sum+=(buffer[(procIndex+i)%bufferLen])&0xFF;

                }
                sum%=256;
                //byte check=(byte)sum;
                if(sum==((buffer[(procIndex+frame_len-3)%bufferLen])&0xFF))
                {
                    //my_debug_comply();
                    int command= (0xFF&buffer[(procIndex+2)%bufferLen]);
                    process_cmd(command,connNum);
                }
                procIndex+= frame_len;
                continue;
            }
            procIndex++;
        }
    }
    private static ProtocolProc singleton;
    public static ProtocolProc getInstance(){
        if(singleton==null){
            singleton=new ProtocolProc();
        }
        return singleton;
    }
    public byte[] getQueryPresBytes()
    {
        int len=40;
        byte[] result =new byte[len];
        for(int i=0;i<len;i++){
            result[i]=0;
        }
        result[0]=result[1]=0x55;
        result[2]=2;

        int sum=0;
        for(int i=2;i<=len-4;i++)
        {
            sum+=0xFF&result[i];
        }
        sum%=256;


        result[len-3]=(byte)sum;
        result[len-2]=(byte)0xAA;
        result[len-1]=(byte)0xAA;
        return result;
    }
    public byte[] getCurrentBytes(int value,int cha)
    {
        int len=40;
        byte[] result =new byte[len];
        for(int i=0;i<len;i++){
            result[i]=0;
        }
        result[0]=result[1]=0x55;
        result[2]=1;
        result[3]=(byte) cha;
        result[4]=(byte)(0xFF&value);
        int sum=0;
        for(int i=2;i<=len-4;i++)
        {
            sum+=0xFF&result[i];
        }
        sum%=256;


        result[len-3]=(byte)sum;
        result[len-2]=(byte)0xAA;
        result[len-1]=(byte)0xAA;
        return result;
    }
    public class ExtraInfo{
        public byte[] DevID;
        public byte DevType;
        public byte Energy;
        public byte CountDown;
        public byte StimuStatus;
        public int Current;
        public int Current2;
        public byte channelType;
        public byte State;
        public byte PresID;
        public byte Freq;
        public byte[] newFreq;
        public byte[] PWM;
        public byte Type;
        public byte[] OnTime;
        public byte[] OffTime;
        public byte Overal;
        public int ConnNum;

        public ExtraInfo() {
            // TODO Auto-generated constructor stub
            DevID=new byte[4];
            PWM=new byte[2];
            newFreq=new byte[2];
            OnTime=new byte[2];
            OffTime=new byte[2];
        }

    }

}
