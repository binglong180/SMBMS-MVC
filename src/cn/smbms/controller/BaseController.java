package cn.smbms.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

public class BaseController {
	/**
	 * 
	 * 使用@InitBinder 注解实现日期格式的绑定！
	 * 
	 * @author 牛牛
	 * 
	 * @date 2018-2-3
	 * 
	 * @param dateBinder
	 */
	@InitBinder
	public void initBinder(WebDataBinder dateBinder) {
		System.out.println("initBinder=======================");
		dateBinder.registerCustomEditor(Date.class, new CustomDateEditor(
				new SimpleDateFormat("yyyy-MM-dd"), true));
	}
}
