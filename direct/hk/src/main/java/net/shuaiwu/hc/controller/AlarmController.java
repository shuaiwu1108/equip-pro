package net.shuaiwu.hc.controller;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("alarm")
public class AlarmController {

    @PostConstruct
    public void init(){

    }

    @RequestMapping("login")
    public Object login(){
        return "Hello Login";
    }
}
