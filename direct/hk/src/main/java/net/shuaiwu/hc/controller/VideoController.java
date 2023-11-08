package net.shuaiwu.hc.controller;

import static net.shuaiwu.hc.utils.HcVideoUtil.*;
import static net.shuaiwu.hc.utils.HcVideoUtil.realPlay;

import cn.hutool.json.JSONObject;
import com.sun.jna.Pointer;
import java.io.File;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.shuaiwu.hc.utils.HcVideoUtil;
import net.shuaiwu.hc.utils.HcVideoUtil.FExceptionCallBack_Imp;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/video")
public class VideoController {

    /**
     * 初始化操作
     * @return
     */
    @PostConstruct
    public void init(){
        if (hCNetSDK == null && HcVideoUtil.playControl==null) {
            if (!HcVideoUtil.createSDKInstance()) {
                System.out.println("Load SDK fail");
            }
            if (!HcVideoUtil.createPlayInstance()) {
                System.out.println("Load PlayCtrl fail");
            }
        }

        //SDK初始化，一个程序只需要调用一次
        hCNetSDK.NET_DVR_Init();

        if(HcVideoUtil.fExceptionCallBack == null) {
            HcVideoUtil.fExceptionCallBack = new FExceptionCallBack_Imp();
        }

        Pointer pUser = null;
        if (!hCNetSDK.NET_DVR_SetExceptionCallBack_V30(0, 0, HcVideoUtil.fExceptionCallBack, pUser)) {
            return;
        }
        log.info("设置异常消息回调成功");

        // 启动日志写入接口
        hCNetSDK.NET_DVR_SetLogToFile(3, "./sdkLog", false);

        log.info("初始化完成！");
    }

    @GetMapping("getPath")
    public Object getCurrentPaht(){
        return System.getProperty("user.dir");
    }

    @RequestMapping("login")
    public void login(@RequestBody JSONObject jsonObject){
        // "192.168.0.251", (short) 8000,"admin","zn123456"
        String ip = jsonObject.getStr("ip");
        short port = jsonObject.getShort("port");
        String user = jsonObject.getStr("user");
        String password = jsonObject.getStr("password");

        login_V40(ip, port, user, password);
//        getIPChannelInfo(lUserID);
    }

    @RequestMapping("readPlay")
    public void realPlayAction(@RequestBody JSONObject jsonObject){
        int sleepTime = jsonObject.getInt("realPlayTime");
        boolean flag = jsonObject.getBool("saveRealPlay");

        realPlay(lUserID, lDChannel);

        if (flag){
            String fP = System.getProperty("user.dir") + File.separator + "Download";
            File f = new File(fP);
            if (!f.exists()){
                f.mkdir();
            }
            saveRealPlay(lPlay, f.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".mp4");
        }
        try {
            TimeUnit.SECONDS.sleep(sleepTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping("stopRealPlay")
    public void stopReal(){
        stopRealPlay();
        stopSaveRealPlay();
    }
}
