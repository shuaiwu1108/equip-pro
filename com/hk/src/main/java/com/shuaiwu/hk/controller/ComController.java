package com.shuaiwu.hk.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import com.shuaiwu.hk.utils.ComUtil;
import com.shuaiwu.hk.utils.ComUtil.ComLister;
import gnu.io.SerialPort;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("com")
public class ComController {

    public static Map<Integer, SerialPort> comMap = new HashMap<>();

    public static Integer COM_ID = 1;

    @PostConstruct
    public void init(){
        String path = System.getProperty("user.dir");
        System.load(path + File.separator + "lib" + File.separator + "rxtxParallel.dll");
        System.load(path + File.separator + "lib" + File.separator + "rxtxSerial.dll");
    }

    @RequestMapping("conn")
    public Object conn(@RequestBody JSONObject jsonObject){
        String comName = jsonObject.getStr("comName");
        int baudrate = jsonObject.getInt("baudrate");
        int databits = jsonObject.getInt("databits");
        int parity = jsonObject.getInt("parity");
        int stopbits = jsonObject.getInt("stopbits");
        ComUtil comUtil = new ComUtil();
        SerialPort serialPort = comUtil.openPort(comName, baudrate, databits, parity, stopbits);
        ComLister comLister = comUtil.new ComLister();
        comUtil.addListener(serialPort, comLister);
        comMap.put(COM_ID, serialPort);
        Map<Object, Object> build = MapUtil.builder()
            .put("message", "Com口已开启")
            .put("comId", COM_ID)
            .build();
        COM_ID++;
        return build;
    }

    @RequestMapping("send")
    public Object send(@RequestBody JSONObject jsonObject){
        Integer anInt = jsonObject.getInt("mapId");
        String data = jsonObject.getStr("data");
        SerialPort serialPort = comMap.get(anInt);
        ComUtil comUtil = new ComUtil();
        comUtil.sendToPort(serialPort, data.getBytes());
        return MapUtil.builder()
            .put("message", "数据已发送")
            .build();
    }

    @RequestMapping("close")
    public Object close(@RequestBody JSONObject jsonObject){
        Integer anInt = jsonObject.getInt("mapId");
        SerialPort serialPort = comMap.get(anInt);
        ComUtil comUtil = new ComUtil();
        comUtil.removeListener(serialPort);
        serialPort.close();
        return MapUtil.builder()
            .put("message1", "监听已关闭")
            .put("message2", "串口已关闭")
            .build();
    }
}
