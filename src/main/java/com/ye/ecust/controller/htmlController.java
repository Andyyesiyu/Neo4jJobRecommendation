package com.ye.ecust.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by yesiyu on 2019/1/7.
 */

@Controller
@RequestMapping("/")
public class htmlController {
    @RequestMapping("2019/01/05/Hello-Hexo/")
    public String FirstPage(){
        return "redirect:/2019/01/05/Hello-Hexo/index.html";
    }
}
