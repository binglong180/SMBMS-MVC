package cn.smbms.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.smbms.pojo.Role;
import cn.smbms.pojo.User;
import cn.smbms.service.role.RoleService;
import cn.smbms.service.user.UserService;
import cn.smbms.tools.Constants;
import cn.smbms.tools.PageSupport;

@Controller
@RequestMapping("/user")
public class UserController {
	private Logger logger = Logger.getLogger(UserController.class);
	@Resource(name = "userService")
	private UserService userService;
	@Resource(name = "roleService")
	private RoleService roleService;

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
		logger.debug("dologin=================");
		User login = userService.login(userCode, userPassword);
		if (login != null) {
			//session.setMaxInactiveInterval(30);
			session.setAttribute(Constants.USER_SESSION, login);
			return "redirect:/user/main.html";
		} else {
			request.setAttribute("error", "密码或用户名不正确！");
			return "login";
		}
	}

	@RequestMapping(value = "/main.html")
	public String main(HttpSession session) {
		logger.info("用户状态处理====================");
		if (session.getAttribute(Constants.USER_SESSION) == null) {
			if (!session.isNew() == true) {
				throw new RuntimeException("登录已超时！");
			}
			return "redirect:/user/login.html";
		}

		return "frame";
	}

	// 局部异常处理
	@RequestMapping(value = "/exLogin.html", method = RequestMethod.GET)
	public String exLogin(@RequestParam String userCode,
			@RequestParam String userPassword) {
		logger.debug("exLogin=============================");
		User login = userService.login(userCode, userPassword);
		if (login == null) {
			throw new RuntimeException("密码或用户名错误！");
		}
		return "redirect:/user/main.html";
	}

	/*
	 * @ExceptionHandler(value = (RuntimeException.class)) public String
	 * handlerException(RuntimeException e, HttpServletRequest request) {
	 * request.setAttribute("e", e); return "error"; }
	 */
	// 注销用户
	@RequestMapping(value = "logOut.html")
	public String logOut(HttpSession session) {
		logger.info("注销用户=====================");
		session.invalidate();
		return "redirect:/user/main.html";
	}

	// 查询用户列表
	@RequestMapping(value = "userList.html")
	public String queryUserList(
			Model model,
			@RequestParam(value = "queryUserName", required = false) String queryUserName,
			@RequestParam(value = "queryUserRole", required = false) String queryUserRole,
			@RequestParam(value = "pageIndex", required = false) String pageIndex

	) {
		logger.info("queryUserName=========>" + queryUserName);
		logger.info("queryUserRole=========>" + queryUserRole);
		logger.info("pageIndex=========>" + pageIndex);
		int _queryUserRole = 0;
		List<User> userList = null;
		// 设置页面容量
		int pageSize = Constants.pageSize;
		// 当前页码
		int currentPageNo = 1;
		if (queryUserName == null) {
			queryUserName = "";
		}
		if (queryUserRole != null && !queryUserRole.equals("")) {
			_queryUserRole = Integer.parseInt(queryUserRole);
		}
		if (pageIndex != null) {
			try {
				currentPageNo = Integer.valueOf(pageIndex);
			} catch (Exception e) {
				return "redirect:/user/syserror.html";
			}
		}
		// 总数量
		int totalCount = userService
				.getUserCount(queryUserName, _queryUserRole);
		// 总页数
		PageSupport pages = new PageSupport();
		pages.setCurrentPageNo(currentPageNo);
		pages.setPageSize(pageSize);
		pages.setTotalCount(totalCount);
		int totalPageCount = pages.getTotalPageCount();
		if (currentPageNo < 1) {
			currentPageNo = 1;
		} else if (currentPageNo > totalPageCount) {
			currentPageNo = totalPageCount;
		}
		userList = userService.getUserList(queryUserName, _queryUserRole,
				currentPageNo, pageSize);
		model.addAttribute("userList", userList);
		List<Role> roleList = null;
		roleList = roleService.getRoleList();
		model.addAttribute("roleList", roleList);
		model.addAttribute("queryUserName", queryUserName);
		model.addAttribute("queryUserRole", queryUserRole);
		model.addAttribute("totalPageCount", totalPageCount);
		model.addAttribute("currentPageNo", currentPageNo);
		return "userlist";
	}

	@RequestMapping(value = "/add.html", method = RequestMethod.GET)
	public String userAdd(@ModelAttribute("user") User user) {
		logger.info("添加用户========================》");
		return "useradd";

	}

	@RequestMapping(value = "/add.html", method = RequestMethod.POST)
	public String userAddSave(@Valid User user, BindingResult bindingResult,
			HttpSession session) {
		if (bindingResult.hasErrors()) {
			logger.debug("添加有错误格式数据");
			return "useradd";
		}
		user.setCreatedBy(((User) session.getAttribute(Constants.USER_SESSION))
				.getId());
		user.setCreationDate(new Date());
		if (userService.add(user)) {
			logger.info(user.getUserName() + "添加成功");
			return "redirect:/user/userList.html";
		}
		logger.info("添加失败");
		return "useradd";
	}

	@RequestMapping(value = "/usermodify.html", method = RequestMethod.GET)
	public String getUserId(@RequestParam String uid,Model model) {
		logger.info("进入修改信息页面======================"+uid);
		User user = userService.getUserById(uid);
		model.addAttribute(user);
		return "usermodify";
	}

	@RequestMapping(value = "/usermodifysave.html", method = RequestMethod.POST)
	public String userModifysave(User user, HttpSession session) {
		logger.info("保存修改信息操作id" + user.getId()+"=========================");
		user.setModifyBy(((User) session.getAttribute(Constants.USER_SESSION))
				.getId());
		user.setModifyDate(new Date());
		if (userService.modify(user)) {
			return "redirect:/user/userList.html";
		}
		return "usermodify";

	}
}
