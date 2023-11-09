package net.shuaiwu.hc.utils;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HcVideoUtil {

    public static HCNetSDK hCNetSDK; //动态库
    public static PlayCtrl playControl; //播放库

    public static FExceptionCallBack_Imp fExceptionCallBack; //异常消息回调

    public static int lUserID = -1; //用户句柄

    public static int lPlay = -1;  //预览句柄

    public static int lDChannel;  //预览通道号

    public static FRealDataCallBack fRealDataCallBack;//预览回调函数实现

    /**
     * 动态库加载
     * @return
     */
    public static boolean createSDKInstance() {
        if (hCNetSDK == null) {
            synchronized (HCNetSDK.class) {
                String strDllPath = "";
                try {
                    if (osSelect.isWindows())
                    //win系统加载库路径
                    {
                        strDllPath = System.getProperty("user.dir") + "\\lib\\HCNetSDK.dll";
                    } else if (osSelect.isLinux())
                    //Linux系统加载库路径
                    {
                        strDllPath = System.getProperty("user.dir") + "/lib/libhcnetsdk.so";
                    }
                    hCNetSDK = (HCNetSDK) Native.loadLibrary(strDllPath, HCNetSDK.class);
                } catch (Exception ex) {
                    log.info("loadLibrary: {}, Error: {}", strDllPath, ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 播放库加载
     * @return
     */
    public static boolean createPlayInstance() {
        if (playControl == null) {
            synchronized (PlayCtrl.class) {
                String strPlayPath = "";
                try {
                    if (osSelect.isWindows())
                    //win系统加载库路径
                    {
                        strPlayPath = System.getProperty("user.dir") + "\\lib\\PlayCtrl.dll";
                    } else if (osSelect.isLinux())
                    //Linux系统加载库路径
                    {
                        strPlayPath = System.getProperty("user.dir") + "/lib/libPlayCtrl.so";
                    }
                    playControl = (PlayCtrl) Native.loadLibrary(strPlayPath, PlayCtrl.class);
                } catch (Exception ex) {
                    log.info("loadLibrary: {}, Error: {}", strPlayPath, ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 登录
     * @param ip
     * @param port
     * @param user
     * @param psw
     */
    public static void login_V40(String ip, short port, String user, String psw) {
        //注册
        HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();//设备登录信息
        HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();//设备信息

        String m_sDeviceIP = ip;//设备ip地址
        m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(m_sDeviceIP.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0,
            m_sDeviceIP.length());

        String m_sUsername = user;//设备用户名
        m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(m_sUsername.getBytes(), 0, m_strLoginInfo.sUserName, 0,
            m_sUsername.length());

        String m_sPassword = psw;//设备密码
        m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
        System.arraycopy(m_sPassword.getBytes(), 0, m_strLoginInfo.sPassword, 0,
            m_sPassword.length());

        m_strLoginInfo.wPort = port;
        m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是
        m_strLoginInfo.byLoginMode = 0;  //0- SDK私有协议，1- ISAPI协议
        m_strLoginInfo.write();

        lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
        if (lUserID == -1) {
            log.error("登录失败，错误码为:{}", hCNetSDK.NET_DVR_GetLastError());
        } else {
            log.info("{}:设备登录成功！数字通道号{}, 模拟通道号{}", ip, (int) m_strDeviceInfo.struDeviceV30.byStartDChan, (int) m_strDeviceInfo.struDeviceV30.byStartChan);
            //相机一般只有一个通道号，热成像相机有2个通道号，通道号为1或1,2
            //byStartDChan为IP通道起始通道号, 预览回放NVR的IP通道时需要根据起始通道号进行取值
            if ((int) m_strDeviceInfo.struDeviceV30.byStartDChan == 1
                || (int) m_strDeviceInfo.struDeviceV30.byStartDChan == 33
                || (int) m_strDeviceInfo.struDeviceV30.byStartChan == 1
                || (int) m_strDeviceInfo.struDeviceV30.byStartChan == 33
            ) {
                //byStartDChan为IP通道起始通道号, 预览回放NVR的IP通道时需要根据起始通道号进行取值,NVR起始通道号一般是33或者1开始
                lDChannel = (int) m_strDeviceInfo.struDeviceV30.byStartDChan == 0 ? m_strDeviceInfo.struDeviceV30.byStartChan : m_strDeviceInfo.struDeviceV30.byStartDChan;
                log.info("预览起始通道号为：{}", lDChannel);
            }
        }
    }

    /**
     * 获取IP通道
     * @param userID
     */
    public static void getIPChannelInfo(int userID) {
        IntByReference ibrBytesReturned = new IntByReference(0);//获取IP接入配置参数
        HCNetSDK.NET_DVR_IPPARACFG_V40 m_strIpparaCfg = new HCNetSDK.NET_DVR_IPPARACFG_V40();
        m_strIpparaCfg.write();
        //lpIpParaConfig 接收数据的缓冲指针
        Pointer lpIpParaConfig = m_strIpparaCfg.getPointer();
        boolean bRet = hCNetSDK.NET_DVR_GetDVRConfig(userID, HCNetSDK.NET_DVR_GET_IPPARACFG_V40, 0, lpIpParaConfig, m_strIpparaCfg.size(), ibrBytesReturned);
        m_strIpparaCfg.read();
        log.info("起始数字通道号：" + m_strIpparaCfg.dwStartDChan);

        for (int iChannum = 0; iChannum < m_strIpparaCfg.dwDChanNum; iChannum++) {
            int channum = iChannum + m_strIpparaCfg.dwStartDChan;
            m_strIpparaCfg.struStreamMode[iChannum].read();
            if (m_strIpparaCfg.struStreamMode[iChannum].byGetStreamType == 0) {
                m_strIpparaCfg.struStreamMode[iChannum].uGetStream.setType(HCNetSDK.NET_DVR_IPCHANINFO.class);
                m_strIpparaCfg.struStreamMode[iChannum].uGetStream.struChanInfo.read();
                if (m_strIpparaCfg.struStreamMode[iChannum].uGetStream.struChanInfo.byEnable == 1) {
                    log.info("IP通道" + channum + "在线");
                } else {
                    log.info("IP通道" + channum + "不在线");
                }
            }
        }
    }

    /**
     * 实时取流
     * @param userID
     * @param iChannelNo
     * @return lPlay 预览句柄
     */
    public static void realPlay(int userID, int iChannelNo) {
        if (userID == -1) {
            log.error("请先注册");
            return;
        }
        HCNetSDK.NET_DVR_PREVIEWINFO strClientInfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
        strClientInfo.read();
        //strClientInfo.hPlayWnd = null;  //窗口句柄，从回调取流不显示一般设置为空
        strClientInfo.lChannel = iChannelNo;  //通道号
        strClientInfo.dwStreamType = 0; //0-主码流，1-子码流，2-三码流，3-虚拟码流，以此类推
        strClientInfo.dwLinkMode = 0; //连接方式：0- TCP方式，1- UDP方式，2- 多播方式，3- RTP方式，4- RTP/RTSP，5- RTP/HTTP，6- HRUDP（可靠传输） ，7- RTSP/HTTPS，8- NPQ
        strClientInfo.bBlocked = 0;
        strClientInfo.byProtoType = 1;
        strClientInfo.write();

        //回调函数定义必须是全局的,
        if (fRealDataCallBack == null) {
            fRealDataCallBack = new FRealDataCallBack();
        }

        //开启预览
        lPlay = hCNetSDK.NET_DVR_RealPlay_V40(userID, strClientInfo, fRealDataCallBack, null);
        if (lPlay == -1) {
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            log.error("取流失败, {}", iErr);
            return;
        }
        log.info("取流成功, 预览句柄:{}", lPlay);
    }


    /**
     * 保存码流至文件
     * @param sFileName
     */
    public static void saveRealPlay(String sFileName){
        //设置码流保存
        if(lPlay != -1){
            if (hCNetSDK.NET_DVR_SaveRealData(lPlay, sFileName)){
                log.info("开始录像成功");
            }
        }
    }

    /**
     * 停止实时预览
     */
    public static void stopRealPlay(){
        if (lPlay != -1) {
            if (hCNetSDK.NET_DVR_StopRealPlay(lPlay)) {
                log.info("停止预览成功");
            }
        }
    }

    public static void stopSaveRealPlay(){
        if (lPlay >= 0) {
            if(hCNetSDK.NET_DVR_StopSaveRealData(lPlay)){
                log.info("停止录像成功");
            }
        }
    }

    public static void logout(){
        if (lUserID != -1){
            if (hCNetSDK.NET_DVR_Logout(lUserID)){
                log.info("用户登出成功");
            }
        }
    }

    public static void clean(){
        if (hCNetSDK.NET_DVR_Cleanup()){
            log.info("资源已释放");
        }
    }

    /**
     * 异常消息回调
     */
    public static class FExceptionCallBack_Imp implements HCNetSDK.FExceptionCallBack {
        public void invoke(int dwType, int lUserID, int lHandle, Pointer pUser) {
            log.error("异常事件类型:{}", dwType);
        }
    }

    public static int Count = 0;
    public static IntByReference m_lPort = new IntByReference(-1);

    /**
     * 预览回调函数
     */
    public static class FRealDataCallBack implements HCNetSDK.FRealDataCallBack_V30 {
        //预览回调
        public void invoke(int lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize,
            Pointer pUser) {
            if (Count == 100) {//降低打印频率
                log.info("码流数据回调...dwBufSize={}", dwBufSize);
                Count = 0;
            }
            Count++;
            //播放库解码
            switch (dwDataType) {
                case HCNetSDK.NET_DVR_SYSHEAD: //系统头
                    if (!playControl.PlayM4_GetPort(m_lPort)) {//获取播放库未使用的通道号
                        break;
                    }
                    if (dwBufSize > 0) {
                        if (!playControl.PlayM4_SetStreamOpenMode(m_lPort.getValue(),
                            PlayCtrl.STREAME_REALTIME)) {//设置实时流播放模式
                            break;
                        }
                        if (!playControl.PlayM4_OpenStream(m_lPort.getValue(), pBuffer, dwBufSize,
                            1024 * 1024)) {//打开流接口
                            break;
                        }
                        if (!playControl.PlayM4_Play(m_lPort.getValue(), null)) {//播放开始
                            break;
                        }
                    }
                case HCNetSDK.NET_DVR_STREAMDATA:   //码流数据
                    if ((dwBufSize > 0) && (m_lPort.getValue() != -1)) {
                        if (!playControl.PlayM4_InputData(m_lPort.getValue(), pBuffer,
                            dwBufSize)) {//输入流数据
                            break;
                        }
                    }
            }
        }
    }
}
