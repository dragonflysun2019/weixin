package com.javen.common;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;
import com.javen.alipay.AliPayController;
import com.javen.controller.AjaxController;
import com.javen.controller.AjaxFileContorlller;
import com.javen.controller.ConstellationController;
import com.javen.controller.FileController;
import com.javen.controller.IndexController;
import com.javen.controller.JSSDKController;
import com.javen.controller.TUserController;
import com.javen.model.*;
import com.javen.weixin.controller.RedPackApiController;
import com.javen.weixin.controller.WeiXinOauthController;
import com.javen.weixin.controller.WeixinApiController;
import com.javen.weixin.controller.WeixinMsgController;
import com.javen.weixin.controller.WeixinPayController;
import com.javen.weixin.controller.WeixinTransfersController;
import com.javen.weixin.controller.LianLianKanController;
import com.javen.weixin.user.UserController;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.SchedulerPlugin;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.druid.DruidStatViewHandler;
import com.jfinal.plugin.druid.IDruidStatViewAuth;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.render.ViewType;
import com.jfinal.weixin.sdk.api.ApiConfigKit;

/**
 * @author Javen
 */
public class APPConfig extends JFinalConfig {
	static Log log = Log.getLog(WeixinMsgController.class);

	/**
	 * 如果生产环境配置文件存在，则优先加载该配置，否则加载开发环境配置文件
	 * 
	 * @param pro
	 *            生产环境配置文件
	 * @param dev
	 *            开发环境配置文件
	 */
	public void loadProp(String pro, String dev) {
		try {
			PropKit.use(pro);
		} catch (Exception e) {
			PropKit.use(dev);
		}
	}

	/**
	 * 配置常量
	 */
	public void configConstant(Constants me) {
		// 加载少量必要配置，随后可用PropKit.get(...)获取值
		loadProp("javen_config_pro.txt", "javen_config.txt");
		me.setDevMode(PropKit.getBoolean("devMode", false));
		me.setEncoding("utf-8");
		me.setViewType(ViewType.JSP);
		// 设置上传文件保存的路径
		me.setBaseUploadPath(PathKit.getWebRootPath() + File.separator + "myupload");
		// ApiConfigKit 设为开发模式可以在开发阶段输出请求交互的 xml 与 json 数据
		ApiConfigKit.setDevMode(me.getDevMode());

	}

	/**
	 * 配置路由
	 */
	public void configRoute(Routes me) {
		// 微信
		me.add("/msg", WeixinMsgController.class);
		me.add("/api", WeixinApiController.class);
		me.add("/oauth", WeiXinOauthController.class);
		me.add("/jssdk", JSSDKController.class, "/view");
		// 可以去掉 /front
		me.add("/pay", WeixinPayController.class, "/view");
		me.add("/", IndexController.class, "/front");
		me.add("/tuser", TUserController.class, "/back");

		me.add("/ajax", AjaxController.class);
		me.add("/constellation", ConstellationController.class, "/front");
		me.add("/wxuser", UserController.class, "/front");
		me.add("/file", FileController.class, "/front");
		me.add("/ajaxfile", AjaxFileContorlller.class, "/front");

		me.add("/read", RedPackApiController.class);
		me.add("/transfers", WeixinTransfersController.class);
		me.add("/alipay", AliPayController.class);
		
		//连连看游戏
		me.add("/wordlist", LianLianKanController.class);
	}

	/**
	 * 配置插件
	 */
	public void configPlugin(Plugins me) {
		// 配置ActiveRecord插件
		DruidPlugin druidPlugin = createDruidPlugin();
		me.add(druidPlugin);

		// 配置ActiveRecord插件
		ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
		//arp.addMapping("course", Course.class);
		//arp.addMapping("orders", Order.class);
		//arp.addMapping("users", "id", Users.class);
		//arp.addMapping("Tuser", TUser.class);
		//arp.addMapping("stock", Stock.class);
		//arp.addMapping("idea", Idea.class);
		arp.addMapping("t_member", "Fmember_id", Member.class);
		arp.addMapping("t_member_barron800", "Fid", MemberBarron800.class);
		arp.setShowSql(PropKit.getBoolean("devMode", false));
		me.add(arp);

		// ehcahce插件配置
		/*me.add(new EhCachePlugin());

		SchedulerPlugin sp = new SchedulerPlugin("job.properties");
		me.add(sp);*/
	}

	public static DruidPlugin createDruidPlugin() {
		String jdbcUrl = PropKit.get("jdbcUrl");
		String user = PropKit.get("user");
		String password = PropKit.get("password");
		log.error(jdbcUrl + " " + user + " " + password);
		// 配置druid数据连接池插件
		DruidPlugin dp = new DruidPlugin(jdbcUrl, user, password);
		// 配置druid监控
		dp.addFilter(new StatFilter());
		WallFilter wall = new WallFilter();
		wall.setDbType("mysql");
		dp.addFilter(wall);
		return dp;
	}

	/**
	 * 配置全局拦截器
	 */
	public void configInterceptor(Interceptors me) {

	}

	/**
	 * 配置处理器
	 */
	public void configHandler(Handlers me) {
		// Druid监控
		DruidStatViewHandler dvh = new DruidStatViewHandler("/druid", new IDruidStatViewAuth() {

			@Override
			public boolean isPermitted(HttpServletRequest request) {
				return true;
			}
		});
		me.add(dvh);
	}

	/**
	 * 建议使用 JFinal 手册推荐的方式启动项目 运行此 main
	 * 方法可以启动项目，此main方法可以放置在任意的Class类定义中，不一定要放于此
	 */
	public static void main(String[] args) {
		JFinal.start("src/main/webapp", 8080, "/", 5);// 启动配置项
	}

}
