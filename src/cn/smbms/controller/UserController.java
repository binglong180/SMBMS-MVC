package cn.smbms.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.smbms.pojo.User;
import cn.smbms.service.user.UserService;
import cn.smbms.tools.Constants;

@Controller
@RequestMapping("/user")
public class UserController {
	private Logger logger = Logger.getLogger(UserController.class);
	@Resource(name = "userService")
	private UserService userService;

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@RequestMapping(value = "/login.html")
	public String login() {
		logger.debug("welcome=====================");

		return "login";

	}

	@RequestMapping(value = "/dologin.html", method = RequestMethod.POST)
	public String doLogin(@RequestParam String userCode,
			@RequestParam String userPassword, HttpSession session,
			HttpServletRequest request) {
		logger.debug("dologin++++++++++++++++++++");
		User login = userService.login(userCode, userPassword);
		if (login != null) {
			session.setAttribute(Constants.USER_SESSION, login);
			return "redirect:/user/main.html";
		} else {
			request.setAttribute("error", "密码或用户名不正确！");
			return "login";
		}
	}

	@RequestMapping(value = "/main.html")
	public String main(HttpSession session) {
		if(session.getAttribute(Constants.USER_SESSION)==null){
			return "redirect:/user/login.html";
		}
		return "frame";
	}
}
