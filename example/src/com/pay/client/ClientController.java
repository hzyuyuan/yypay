/**   
 * Copyright (c) 版权所有 2010-2016 驭缘科技有限公司  
 * 产品名：   
 * 包名：com.pay.test   
 * 文件名：PayController.java   
 * 版本信息：   
 * 创建日期：2016年10月25日-上午2:04:29
 */
package com.pay.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.pay.api.PayClient;
import com.pay.api.bean.request.DetailBean;
import com.pay.api.bean.request.PayMentBean;
import com.pay.api.bean.request.ProrateBean;
import com.pay.api.bean.request.RefundBean;
import com.pay.api.bean.response.PayMentResponse;
import com.pay.api.bean.response.ProrateResponse;
import com.pay.api.bean.response.RefundResponse;
import com.pay.api.util.JsonUtil;
import com.pay.api.util.StringUtil;
import com.pay.api.util.UrlConnectUtil;
import com.pay.system.util.ConfigUtil;
import com.pay.system.util.LoggerUtil;

/**
 * 类名：TestController 类描述： 创建人： YYC 修改人： YYC 修改时间：2016年10月16日 下午10:13:28 修改备注：
 * 
 * @version 1.0.0
 */
@Controller
public class ClientController {
	/** 从配置文件读取商户的合作账户id **/
	private static String appid = ConfigUtil.getInstance().getStringValue(
			"merchant.partner");

	/** 从配置文件读取商户的安全校验码 **/
	private static String key = ConfigUtil.getInstance().getStringValue(
			"merchant.key");

	/** 从配置文件读取支付接口地址 **/
	private static String gateurl = ConfigUtil.getInstance().getStringValue(
			"pay_gateway");

	/** 从配置文件读取支付接口地址 **/
	private static String publicKey = ConfigUtil.getInstance().getStringValue(
			"merchant.publicKey");

	/** 支付网关 **/
	private static String gateway = gateurl + "/pay/gateway.do";

	/** 从配置文件读取异步返回地址 **/
	private static String localhost = ConfigUtil.getInstance().getStringValue(
			"localhost");

	private static PayClient client = new PayClient(gateway, appid, key,
			publicKey);

	/**
	 * 支付页面
	 * 
	 * @since 1.0.0
	 */
	@RequestMapping("/index.do")
	public String index(Model model, HttpServletRequest request,
			HttpServletResponse response) {
		boolean validation = true;
		String ua = request.getHeader("user-agent").toLowerCase();
		if (ua.indexOf("micromessenger") < 0
				&& (ua.indexOf("android") > 0 || ua.indexOf("iphone") > 0
						|| ua.indexOf("iPod") > 0 || ua.indexOf("iPad") > 0)) {// 是微信浏览器
			validation = false;
		}

		Map<String, String> payMethod = client.getPayMethod(validation);
		model.addAttribute("payMethod", payMethod);
		return "/index";
	}

	/**
	 * 执行支付请求
	 * 
	 * @param response
	 *            响应参数对象
	 * @param request
	 *            支付请求参数
	 * @throws Exception
	 * @return void
	 * @exception
	 * @since 1.0.0
	 */
	@RequestMapping("/payment.do")
	@ResponseBody
	public void freezeAndPay(PayMentBean request, HttpServletResponse response)
			throws Exception {
		request.setReturn_url(localhost + "/payment/callback.do");
		request.setNotify_url(localhost + "/payment/notify.do");
		String payUrl = client.payment(request);
		response.sendRedirect(payUrl);
	}
	

	
	
	/**
	 * 执行支付请求
	 * 
	 * @param response
	 *            响应参数对象
	 * @param request
	 *            支付请求参数
	 * @throws Exception
	 * @return void
	 * @exception
	 * @since 1.0.0
	 */
	@RequestMapping("/getorder.do")
	@ResponseBody
	public void getorder(PayMentBean request, HttpServletResponse response)
			throws Exception {
		request.setReturn_url(localhost + "/payment/callback.do");
		request.setNotify_url(localhost + "/payment/notify.do");
		request.setService("pay.auth.orders.logs");
		request.setLimit_pay("orders");
		request.setOut_trade_no("");
		String payUrl = client.payment(request);
	    System.out.println(payUrl);
	}

	/**
	 * 支付接口同步回调入口
	 * 
	 * @param request
	 *            请求对象
	 * @param response
	 *            返回对象
	 * @param model
	 *            model
	 * @since 1.0.0
	 */
	@RequestMapping("/payment/callback.do")
	public String callback(HttpServletRequest request,
			HttpServletResponse response, Model model) throws Exception {
		LoggerUtil.info(this.getClass(), JsonUtil.objToJson(request.getParameterMap()));
//		PayMentResponse payMentResponse = new PayMentResponse();
//				(PayMentResponse) JsonUtil
//				.jsonToBean(
//						StringUtil.urlStrToString(request.getParameter("info")),
//						PayMentResponse.class);

//		String message = "";
//		if (payMentResponse == null) {
//			message = "返回信息为空";
//		} else {
//			if (payMentResponse.getCode().equals("200")) {
//				message = "支付成功，返回结果如下:</br></br>"
//						+ JsonUtil.objToJson(payMentResponse);
//			} else {
//				message = "支付失败，返回结果如下:</br></br>"
//						+ JsonUtil.objToJson(payMentResponse);
//			}
//		}

		model.addAttribute("message", JsonUtil.objToJson(request.getParameterMap()));
//		LoggerUtil.info(this.getClass(), "冻结支付同步返回参数:" + message);
		return "/result";
	}

	/**
	 * 支付异步回调入口
	 * 
	 * @param request
	 *            请求对象
	 * @param response
	 *            响应对象
	 * @param model
	 *            model
	 * @since 1.0.0
	 */
	@RequestMapping("/payment/notify.do")
	public void notify(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		LoggerUtil.info(this.getClass(), "支付异步通知参数:" + JsonUtil.objToJson(request.getParameterMap()));
//		PayMentResponse payMentResponse = (PayMentResponse) client.getReturn(
//				request.getParameter("info"), PayMentResponse.class);
//		String message = "";
//		if (payMentResponse == null) {
//			message = "返回信息为空";
//		} else {
//			if (payMentResponse.getCode().equals("200")) {
//				message = "支付成功，返回结果如下:</br></br>"
//						+ JsonUtil.objToJson(payMentResponse);
//			} else {
//				message = "支付失败，返回结果如下:</br></br>"
//						+ JsonUtil.objToJson(payMentResponse);
//			}
//		}
//		LoggerUtil.info(this.getClass(), "支付异步通知参数:" + message);
	}

	/**
	 * 退款页面
	 * 
	 * @since 1.0.0
	 */
	@RequestMapping("/refund.do")
	public String refund(Model model) {
		return "/refund";
	}

	/**
	 * 退款同步接收请求
	 * 
	 * @param response
	 *            响应对象
	 * @param request
	 *            分账支付请求参数封装对象
	 * @param model
	 *            model
	 * @since 1.0.0
	 */
	@RequestMapping("/refundApply.do")
	public String refund(HttpServletResponse response, RefundBean request,
			Model model) throws Exception {
		request.setNotify_url(localhost + "/refund/notify.do");
		LoggerUtil.info(this.getClass(), "请求链接：" + JsonUtil.objToJson(request));
		RefundResponse prorateResponse = client.refund(request);
		String message = "";
		if (prorateResponse.getCode().equals("200")) {
			message = "退款成功，返回结果如下:</br></br>";
		} else {
			message = "退款失败，返回结果如下:</br></br>";
		}
		message = message + JsonUtil.objToJson(prorateResponse);

		model.addAttribute("message", message);
		LoggerUtil.info(this.getClass(), "支付同步通知参数:" + message);
		return "/result";
	}

	/**
	 * 退款异步接收请求
	 * 
	 * @param response
	 *            响应对象
	 * @param request
	 *            分账支付请求参数封装对象
	 * @param model
	 *            model
	 * @since 1.0.0
	 */
	@RequestMapping("/refund/notify.do")
	public void refundNotify(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		RefundResponse prorateResponse = (RefundResponse) client.getReturn(
				request.getParameter("info"), RefundResponse.class);
		String message = "";
		if (prorateResponse.getCode().equals("200")) {
			message = "退款成功，返回结果如下:</br></br>";
		} else {
			message = "退款失败，返回结果如下:</br></br>";
		}
		message = message + JsonUtil.objToJson(prorateResponse);

		LoggerUtil.info(this.getClass(), "退款异步通知参数:" + message);
	}

	/**
	 * 分账支付页面
	 * 
	 * @param model
	 *            model
	 * @since 1.0.0
	 */
	@RequestMapping("/confirm.do")
	public String confirm(Model model) {
		return "/confirm";
	}

	/**
	 * 分账支付请求
	 * 
	 * @param response
	 *            响应对象
	 * @param request
	 *            分账支付请求参数封装对象
	 * @param model
	 *            model
	 * @since 1.0.0
	 */
	@RequestMapping("/prorate.do")
	public String prorate(HttpServletResponse response, ProrateBean request,
			Model model) throws Exception {
		request.setNotify_url(localhost + "/confirm/notify.do");
		ProrateResponse prorateResponse = client.prorate(request);
		String message = "";
		if (prorateResponse.getCode().equals("200")) {
			message = "分账成功，返回结果如下:</br></br>";
		} else {
			message = "分账失败，返回结果如下:</br></br>";
		}
		message = message + JsonUtil.objToJson(prorateResponse);

		model.addAttribute("message", message);
		LoggerUtil.info(this.getClass(), "支付同步通知参数:" + message);
		return "/result";
	}

	/**
	 * 分账支付异步回调入口
	 * 
	 * @param request
	 * @param response
	 * @return void
	 * @exception
	 * @since 1.0.0
	 */
	@RequestMapping("/confirm/notify.do")
	public void confirmNotify(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ProrateResponse prorateResponse = (ProrateResponse) client.getReturn(
				request.getParameter("info"), ProrateResponse.class);
		String message = "";
		if (prorateResponse.getCode().equals("200")) {
			message = "分账成功，返回结果如下:</br></br>";
		} else {
			message = "分账失败，返回结果如下:</br></br>";
		}
		message = message + JsonUtil.objToJson(prorateResponse);
		LoggerUtil.info(this.getClass(), "分账异步通知参数:" + message);
	}

	/**
	 * 返回订单信息列表页面
	 */
	@RequestMapping("/list.do")
	public String list(Model model) {
		// String ordersJson = UrlConnectUtil.SendHttpsPOST(gateurl +
		// "/pay/getOrders.do");
		String ordersJson;
		if (gateurl.substring(0, 5).toLowerCase().equals("https")) {
			ordersJson = UrlConnectUtil.SendHttpsPOST(gateurl
					+ "/pay/getOrders.do");
		} else {
			ordersJson = UrlConnectUtil.doUrl(gateurl + "/pay/getOrders.do");
		}
		ordersJson = StringUtil.base64ToString(ordersJson, "utf-8");
		LoggerUtil.info(this.getClass(), "获取订单信息：" + ordersJson);

		List<PayMentBean> list = new ArrayList<PayMentBean>();
		list = JSONObject.parseArray(ordersJson, PayMentBean.class);
		model.addAttribute("orders", list);

		return "/list";
	}
	
	
	@RequestMapping("/orderlist")
	public String orderlist(){
		return "/orderselect";
		}
	
	/**
	 * 执行流水查询请求
	 * 
	 * @param response
	 *            响应参数对象
	 * @param request
	 *            支付请求参数
	 * @throws Exception
	 * @return void
	 * @exception
	 * @since 1.0.0
	 */
	@RequestMapping("/orderslist.do")
	public String orderslist(DetailBean request, HttpServletResponse response)
			throws Exception {
		request.setService("pay.auth.orders.logs");
		String payUrl = client.getorders(request);
		return payUrl;
	}
	
	/**
	 * 执行流水查询请求
	 * 
	 * @param response
	 *            响应参数对象
	 * @param request
	 *            支付请求参数
	 * @throws Exception
	 * @return void
	 * @exception
	 * @since 1.0.0
	 */
	@RequestMapping("/orderlistlogs.do")
	public String orderlistlogs(DetailBean request, HttpServletResponse response,Model model) throws Exception{
		request.setService("pay.auth.orders.logs");
		LoggerUtil.info(this.getClass(), "请求订单流水参数：" + request.toUrlEncode());
		String result = client.getorders(request);
		LoggerUtil.info(this.getClass(), "获取订单流水信息：" + result);
		List<PayMentResponse> list = JSONObject.parseArray(result,PayMentResponse.class);
		model.addAttribute("list",list);
		return "/orderlist";
	}
}
