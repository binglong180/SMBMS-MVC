package cn.smbms.controller;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.mysql.jdbc.StringUtils;

import cn.smbms.pojo.Role;
import cn.smbms.pojo.User;
import cn.smbms.service.role.RoleService;
import cn.smbms.service.user.UserService;
import cn.smbms.tools.Constants;
import cn.smbms.tools.PageSupport;

@Controller
@RequestMapping("/user")
public class UserController extends BaseController{
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
			// session.setMaxInactiveInterval(30);
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

	@RequestMapping(value = "/useradd.html", method = RequestMethod.GET)
	public String userAdd(@ModelAttribute("user") User user) {
		logger.info("添加用户========================》");
		return "useradd";

	}

	@RequestMapping(value = "/addsave.html", method = RequestMethod.POST)
	public String userAddSave(
			User user,
			HttpSession session,
			@RequestParam(value = "attachs", required = false) MultipartFile[] attachs,
			HttpServletRequest request) {
		String idPicPath = null;
		String workPicPath = null;
		String errorInfo = null;
		String path = session.getServletContext().getRealPath(
				"statics" + File.separator + "uploadfiles");
		logger.info("uploadfiles path===================>" + path);
		for (int i = 0; i < attachs.length; i++) {
			MultipartFile attach = attachs[i];
			if (i == 0) {
				errorInfo = "uplodaFileError";
			} else if (i == 1) {
				errorInfo = "uplodaWkError";
			}
			String oldFileName = attach.getOriginalFilename();
			logger.info("OriginalFilename===================>" + oldFileName);
			String prefix = FilenameUtils.getExtension(oldFileName);
			logger.info("oldFileName prefix===================>" + prefix);
			int filrSize = 500000;
			logger.info("filrSize size======================" + filrSize);
			if (attach.getSize() > filrSize) {
				request.setAttribute(errorInfo, "上传文件不得大于500KB");
				return "useradd";
			} else if (prefix.equalsIgnoreCase("jpg")
					|| prefix.equalsIgnoreCase("jpeg")
					|| prefix.equalsIgnoreCase("png")
					|| prefix.equalsIgnoreCase("pneg")) {
				String fileName = System.currentTimeMillis()
						+ RandomUtils.nextInt(1000000) + "";
				if (i == 0) {
					fileName += "_Personal.jsp";
				} else if (i == 1) {
					fileName += "_Work.jsp";
				}
				logger.info("fileName new======================" + fileName);
				File targetFile = new File(path, fileName);
				// 判断文件夹是否存在
				if (!targetFile.exists()) {
					boolean mkdirs = targetFile.mkdirs();
					if (!mkdirs) {
						request.setAttribute(errorInfo, "上传文件格式不正确");
						return "useradd";
					}
				}
				// 保存
				try {
					attach.transferTo(targetFile);
				} catch (Exception e) {
					request.setAttribute(errorInfo, "上传文件失败");
					e.printStackTrace();
					return "useradd";
				}
				if (i == 0) {
					idPicPath = path + File.separator + fileName;
				} else if (i == 1) {
					workPicPath = path + File.separator + fileName;
				}
				logger.info("idPicPath======================" + idPicPath);
				logger.info("workPicPath======================" + workPicPath);
			} else {
				request.setAttribute(errorInfo, "上传文件格式不正确");
				return "useradd";
			}
		}

		user.setIdPicPath(idPicPath);
		user.setWorkPicPath(workPicPath);
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
	public String getUserId(@RequestParam String uid, Model model) {
		logger.info("进入修改信息页面======================" + uid);
		User user = userService.getUserById(uid);
		model.addAttribute(user);
		return "usermodify";
	}

	// 修改用户信息
	@RequestMapping(value = "/usermodifysave.html", method = RequestMethod.POST)
	public String userModifysave(User user, HttpSession session) {
		logger.info("保存修改信息操作id" + user.getId() + "=========================");
		user.setModifyBy(((User) session.getAttribute(Constants.USER_SESSION))
				.getId());
		user.setModifyDate(new Date());
		if (userService.modify(user)) {
			return "redirect:/user/userList.html";
		}
		return "usermodify";
	}

	// 查看用户信息

	@RequestMapping(value = "/view", method = RequestMethod.GET)
	@ResponseBody
	public User view(@RequestParam String id) {
		logger.info("查看用户信息" + id);
		User user = new User();
		try {
			user = userService.getUserById(id);
			logger.info("user===========>" + user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return user;
	}

	// 查看是否存在用户UserCode 使用@ResponseBody
	@RequestMapping(value = "/ucexist.html")
	@ResponseBody
	public Object userCodeIsExist(@RequestParam String userCode) {
		logger.info("查重 userCode============》" + userCode);
		HashMap<String, String> hashMap = new HashMap<String, String>();

		if (StringUtils.isNullOrEmpty(userCode)) {
			hashMap.put("userCode", "exist");
		} else {
			User user = userService.selectUserCodeExist(userCode);
			if (user == null) {
				hashMap.put("userCode", "noexist");
			} else {
				hashMap.put("userCode", "exist");
			}
		}
		return JSONArray.toJSONString(hashMap);
	}

}
