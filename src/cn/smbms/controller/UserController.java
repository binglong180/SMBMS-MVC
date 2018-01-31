package cn.smbms.controller;



import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.smbms.pojo.User;
import cn.smbms.service.user.UserService;


@Controller
@RequestMapping("/user")
public class UserController {
	private Logger logger = Logger.getLogger(UserController.class);
	@Resource(name="userService")
	private UserService userService;
	
	public UserService getUserService() {
		return userService;
	}
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	@RequestMapping(value="/login.html")
	public String login(){
		logger.debug("welcome=====================");
		
		return "login";
	
	}
	@RequestMapping(value="/dologin.html",method=RequestMethod.POST)
	public String doLogin(@RequestParam String userCode,@RequestParam String userPassword){
		logger.debug("dologin++++++++++++++++++++");
		User login = userService.login(userCode, userPassword);
		if(login!=null){
			return "redirect:/user/main.html";
		}else{
			return "login";
		}		
	}
	@RequestMapping(value="/main.html")
	public String main(){
		return "frame";
	}
}
