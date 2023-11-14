package net.shuaiwu.hc.controller;

import static net.shuaiwu.hc.utils.HcVideoUtil.*;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import com.sun.jna.Pointer;
import java.io.UnsupportedEncodingException;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.shuaiwu.hc.utils.HcVideoUtil;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("alarm")
public class AlarmController {

    @PostConstruct
    public void init(){
        if (hCNetSDK == null) {
            if (!HcVideoUtil.createSDKInstance()) {
                log.info("加载SDK失败");
                return;
            }
        }
        /**初始化*/
        hCNetSDK.NET_DVR_Init();
        /**加载日志*/
        hCNetSDK.NET_DVR_SetLogToFile(3, "./sdklog", false);

        //设置报警回调函数
        if (fMSFCallBack_V31 == null) {
            fMSFCallBack_V31 = new FMSGCallBack_V31();
            Pointer pUser = null;
            if (!hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(fMSFCallBack_V31, pUser)) {
                log.error("设置回调函数失败");
                return;
            } else {
                log.info("设置回调函数成功");
            }
        }
        log.info("初始化完毕");
    }

    @RequestMapping("login")
    public Object login(@RequestBody JSONObject jsonObject){
        // "192.168.0.251", (short) 8000,"admin","zn123456"
        String ip = jsonObject.getStr("ip");
        short port = jsonObject.getShort("port");
        String user = jsonObject.getStr("user");
        String password = jsonObject.getStr("password");

        login_V40(ip, port, user, password);
        return MapUtil.builder()
            .put("lUserID", lUserID)
            .put("lDChannel", lDChannel)
            .build();
//        getIPChannelInfo(lUserID);
    }

    @RequestMapping("setAlarm")
    public void setAlarm(){
        HcVideoUtil.setAlarm();
    }

    @RequestMapping("startPic")
    public Object startPic() {
        return HcVideoUtil.satartPic();
    }

    @RequestMapping("logout")
    public void logout(){
        HcVideoUtil.logout();
    }
}
