package com.shuaiwu.hk.utils;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.TooManyListenersException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ComUtil {

    public SerialPort serialPort;

    /**
     * 添加监听器
     *
     * @param port     串口对象
     * @param listener 串口监听器 //     * @throws TooManyListeners 监听类对象过多
     */
    public void addListener(SerialPort port, SerialPortEventListener listener) {
        try {
            //给串口添加监听器
            port.addEventListener(listener);
            //设置当有数据到达时唤醒监听接收线程
            port.notifyOnDataAvailable(true);
            //设置当通信中断时唤醒中断线程
            port.notifyOnBreakInterrupt(true);
        } catch (TooManyListenersException e) {
            log.error("太多监听器");
            e.printStackTrace();
        }
    }

    /**
     * 删除监听器
     *
     * @param port     串口对象
     * @param listener 串口监听器 //     * @throws TooManyListeners 监听类对象过多
     */
    public void removeListener(SerialPort port) {
        //删除串口监听器
        port.removeEventListener();
    }


    /**
     * 打开串口
     *
     * @param portName 端口名称
     * @param baudrate 波特率  9600
     * @param databits 数据位  8
     * @param parity   校验位（奇偶位）  NONE ：0
     * @param stopbits 停止位 1
     * @return 串口对象
     */
    public SerialPort openPort(String portName, int baudrate, int databits, int parity,
        int stopbits) {
        try {
            //通过端口名识别端口
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            //打开端口，并给端口名字和一个timeout（打开操作的超时时间）
            CommPort commPort = portIdentifier.open(portName, 2000);
            //判断是不是串口
            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                //设置一下串口的波特率等参数
                serialPort.setSerialPortParams(baudrate, databits, stopbits, parity);
                log.info("打开 " + portName + " 成功!");
                this.serialPort = serialPort;
                return serialPort;
            } else {
                log.error("不是串口");
            }
        } catch (NoSuchPortException e1) {
            log.error("没有找到端口");
            e1.printStackTrace();
        } catch (PortInUseException e2) {
            log.error("端口被占用");
            e2.printStackTrace();
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 往串口发送数据
     *
     * @param serialPort 串口对象
     * @param order      待发送数据
     */
    public void sendToPort(SerialPort serialPort, byte[] order) {
        OutputStream out = null;
        try {
            out = serialPort.getOutputStream();
            out.write(order);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从串口读取数据
     *
     * @param serialPort 当前已建立连接的SerialPort对象
     * @return 读取到的数据
     */
    public byte[] readFromPort(SerialPort serialPort) {
        InputStream in = null;
        byte[] bytes = null;
        try {
            in = serialPort.getInputStream();
            // 获取buffer里的数据长度
            int bufflenth = in.available();
            while (bufflenth != 0) {
                // 初始化byte数组为buffer中数据的长度
                bytes = new byte[bufflenth];
                in.read(bytes);
                bufflenth = in.available();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }

    /**
     * 字节数组转16进制字符串
     *
     * @param b 字节数组
     * @return 16进制字符串
     */
    public static String printHexString(byte[] b) {
        StringBuilder sbf = new StringBuilder();
        for (byte value : b) {
            String hex = Integer.toHexString(value & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sbf.append(hex.toUpperCase()).append(" ");
        }
        return sbf.toString().trim();
    }

    /**
     * 16进制字符串转字节数组
     *
     * @param hex 16进制字符串
     * @return 字节数组
     */
    public static byte[] hex2byte(String hex) {
        if (!isHexString(hex)) {
            return null;
        }
        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteint = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = new Integer(byteint).byteValue();
        }
        return b;
    }

    /**
     * 校验是否是16进制字符串
     *
     * @param hex
     * @return
     */
    public static boolean isHexString(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            return false;
        }
        for (int i = 0; i < hex.length(); i++) {
            char c = hex.charAt(i);
            if (!isHexChar(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 校验是否是16进制字符
     *
     * @param c
     * @return
     */
    private static boolean isHexChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }

    public class ComLister implements SerialPortEventListener {

        @Override
        public void serialEvent(SerialPortEvent event) {
            switch (event.getEventType()) {
                //串口存在有效数据
                case SerialPortEvent.DATA_AVAILABLE:
                    byte[] bytes = readFromPort(serialPort);
                    String byteStr = new String(bytes, 0, bytes.length).trim();
                    System.out.println("===========start===========");
                    System.out.println(new Date() + "【读到的字符串】：-----" + byteStr);
                    System.out.println(
                        new Date() + "【字节数组转16进制字符串】：-----" + printHexString(bytes));
                    System.out.println("===========end===========");
                    break;
                // 2.输出缓冲区已清空
                case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                    log.error("输出缓冲区已清空");
                    break;
                // 3.清除待发送数据
                case SerialPortEvent.CTS:
                    log.error("清除待发送数据");
                    break;
                // 4.待发送数据准备好了
                case SerialPortEvent.DSR:
                    log.error("待发送数据准备好了");
                    break;
                // 10.通讯中断
                case SerialPortEvent.BI:
                    log.error("与串口设备通讯中断");
                    break;
                default:
                    break;
            }
        }
    }
}
