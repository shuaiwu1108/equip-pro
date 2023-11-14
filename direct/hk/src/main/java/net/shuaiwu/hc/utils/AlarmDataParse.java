package net.shuaiwu.hc.utils;

import static net.shuaiwu.hc.utils.HcVideoUtil.*;
import com.sun.jna.Pointer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlarmDataParse {

    public static void alarmDataHandle(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
        log.info("报警事件类型： lCommand:" + Integer.toHexString(lCommand));
        String sTime;
        String MonitoringSiteID;
        //lCommand是传的报警类型
        switch (lCommand) {
            //异常行为检测信息
            case HCNetSDK.COMM_ALARM_RULE:
                HCNetSDK.NET_VCA_RULE_ALARM strVcaAlarm = new HCNetSDK.NET_VCA_RULE_ALARM();
                strVcaAlarm.write();
                Pointer pVCAInfo = strVcaAlarm.getPointer();
                pVCAInfo.write(0, pAlarmInfo.getByteArray(0, strVcaAlarm.size()), 0, strVcaAlarm.size());
                strVcaAlarm.read();

                switch (strVcaAlarm.struRuleInfo.wEventTypeEx) {
                    case 1: //穿越警戒面 (越界侦测)
                        log.info("越界侦测报警发生");
                        strVcaAlarm.struRuleInfo.uEventParam.setType(HCNetSDK.NET_VCA_TRAVERSE_PLANE.class);
                        log.info("检测目标："+strVcaAlarm.struRuleInfo.uEventParam.struTraversePlane.byDetectionTarget); //检测目标，0表示所有目标（表示不锁定检测目标，所有目标都将进行检测），其他取值按位表示不同的检测目标：0x01-人，0x02-车
                        //开始抓图
                        HCNetSDK.NET_DVR_JPEGPARA netDvrJpegpara = new HCNetSDK.NET_DVR_JPEGPARA();
                        netDvrJpegpara.wPicQuality = 0;
                        netDvrJpegpara.wPicSize = 5;
                        String path = System.getProperty("user.dir")  +  File.separator + "pic";
                        File file = new File(path);
                        if (!file.exists()){
                            file.mkdir();
                        }
                        String tt = file.getAbsolutePath() + File.separator  + System.currentTimeMillis() + ".jpg";
                        byte[] bb = tt.getBytes();
                        hCNetSDK.NET_DVR_CaptureJPEGPicture(lUserID, lDChannel, netDvrJpegpara, bb);
                        break;
                    case 2: //目标进入区域
                        log.info("目标进入区域报警发生");
                        strVcaAlarm.struRuleInfo.uEventParam.setType(HCNetSDK.NET_VCA_AREA.class);
                        log.info("检测目标："+strVcaAlarm.struRuleInfo.uEventParam.struArea.byDetectionTarget);
                        //图片保存
                        if ((strVcaAlarm.dwPicDataLen > 0) && (strVcaAlarm.byPicTransType == 0)) {
                            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                            String newName = sf.format(new Date());
                            FileOutputStream fout;
                            try {
                                String filename = "../pic/" + newName + "_TargetEnter" + ".jpg";
                                fout = new FileOutputStream(filename);
                                //将字节写入文件
                                long offset = 0;
                                ByteBuffer buffers = strVcaAlarm.pImage.getByteBuffer(offset, strVcaAlarm.dwPicDataLen);
                                byte[] bytes = new byte[strVcaAlarm.dwPicDataLen];
                                buffers.rewind();
                                buffers.get(bytes);
                                fout.write(bytes);
                                fout.close();
                            } catch (FileNotFoundException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        break;
                    case 3: //目标离开区域
                        log.info("目标离开区域报警触发");
                        strVcaAlarm.struRuleInfo.uEventParam.setType(HCNetSDK.NET_VCA_AREA.class);
                        log.info("检测目标："+strVcaAlarm.struRuleInfo.uEventParam.struArea.byDetectionTarget);
                        //图片保存
                        if ((strVcaAlarm.dwPicDataLen > 0) && (strVcaAlarm.byPicTransType == 0)) {
                            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                            String newName = sf.format(new Date());
                            FileOutputStream fout;
                            try {
                                String filename = "../pic/" + newName + "_TargetLeave" + ".jpg";
                                fout = new FileOutputStream(filename);
                                //将字节写入文件
                                long offset = 0;
                                ByteBuffer buffers = strVcaAlarm.pImage.getByteBuffer(offset, strVcaAlarm.dwPicDataLen);
                                byte[] bytes = new byte[strVcaAlarm.dwPicDataLen];
                                buffers.rewind();
                                buffers.get(bytes);
                                fout.write(bytes);
                                fout.close();
                            } catch (FileNotFoundException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        break;
                    case 4: //周界入侵
                        log.info("周界入侵报警发生");
                        strVcaAlarm.struRuleInfo.uEventParam.setType(HCNetSDK.NET_VCA_INTRUSION.class);
                        log.info("检测目标："+strVcaAlarm.struRuleInfo.uEventParam.struIntrusion.byDetectionTarget);
                        //图片保存
                        if ((strVcaAlarm.dwPicDataLen > 0) && (strVcaAlarm.byPicTransType == 0)) {
                            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                            String newName = sf.format(new Date());
                            FileOutputStream fout;
                            try {
                                String filename = "../pic/" + newName + "VCA_INTRUSION" + ".jpg";
                                fout = new FileOutputStream(filename);
                                //将字节写入文件
                                long offset = 0;
                                ByteBuffer buffers = strVcaAlarm.pImage.getByteBuffer(offset, strVcaAlarm.dwPicDataLen);
                                byte[] bytes = new byte[strVcaAlarm.dwPicDataLen];
                                buffers.rewind();
                                buffers.get(bytes);
                                fout.write(bytes);
                                fout.close();
                            } catch (FileNotFoundException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        break;
                    case 5: //徘徊
                        log.info("徘徊事件触发");

                        break;
                    case 8: //快速移动(奔跑)，
                        log.info("快速移动(奔跑)事件触发");
                        break;
                    case 15: //离岗
                        log.info("离岗事件触发{}");
                        //strVcaAlarm.struRuleInfo.uEventParam.struLeavePos.byOnPosition
                        strVcaAlarm.struRuleInfo.uEventParam.setType(HCNetSDK.NET_VCA_LEAVE_POSITION.class);
                        //图片保存
                        if ((strVcaAlarm.dwPicDataLen > 0) && (strVcaAlarm.byPicTransType == 0)) {
                            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                            String newName = sf.format(new Date());
                            FileOutputStream fout;
                            try {
                                String filename = "../pic/" + newName + "VCA_LEAVE_POSITION_" + ".jpg";
                                fout = new FileOutputStream(filename);
                                //将字节写入文件
                                long offset = 0;
                                ByteBuffer buffers = strVcaAlarm.pImage.getByteBuffer(offset, strVcaAlarm.dwPicDataLen);
                                byte[] bytes = new byte[strVcaAlarm.dwPicDataLen];
                                buffers.rewind();
                                buffers.get(bytes);
                                fout.write(bytes);
                                fout.close();
                            } catch (FileNotFoundException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    case 20: //倒地检测
                        log.info("倒地事件触发");
                        break;
                    case 44: //玩手机

                        log.info("玩手机报警发生");
                        //图片保存
                        if ((strVcaAlarm.dwPicDataLen > 0) && (strVcaAlarm.byPicTransType == 0)) {
                            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                            String newName = sf.format(new Date());
                            FileOutputStream fout;
                            try {
                                String filename = "../pic/" + newName + "PLAY_CELLPHONE_" + ".jpg";
                                fout = new FileOutputStream(filename);
                                //将字节写入文件
                                long offset = 0;
                                ByteBuffer buffers = strVcaAlarm.pImage.getByteBuffer(offset, strVcaAlarm.dwPicDataLen);
                                byte[] bytes = new byte[strVcaAlarm.dwPicDataLen];
                                buffers.rewind();
                                buffers.get(bytes);
                                fout.write(bytes);
                                fout.close();
                            } catch (FileNotFoundException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        break;
                    case 45: //持续检测
                        log.info("持续检测事件触发");
                    default:
                        log.info("行为事件类型:" + strVcaAlarm.struRuleInfo.wEventTypeEx);
                        break;
                }
                break;
            default:
                log.info("报警类型" + Integer.toHexString(lCommand));
                break;
        }
    }
}
