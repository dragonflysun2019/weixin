package com.javen.weixin.controller;

import com.jfinal.core.Controller;
import java.io.File;  
import java.io.FileInputStream;  
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.javen.model.Member;

import org.apache.poi.hssf.usermodel.HSSFCell;  
import org.apache.poi.hssf.usermodel.HSSFRow;  
import org.apache.poi.hssf.usermodel.HSSFSheet;  
import org.apache.poi.hssf.usermodel.HSSFWorkbook;  
import org.apache.poi.xssf.usermodel.XSSFCell;  
import org.apache.poi.xssf.usermodel.XSSFRow;  
import org.apache.poi.xssf.usermodel.XSSFSheet;  
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.javen.model.*;
import java.util.Date;
import java.text.SimpleDateFormat;
/**
 * @author dragonfly
 *
 */

class WordOut{
	public String name;
	public Integer id;
	public String lang;
};

class ReturnJson{
	public Integer code;
	public String msg;
	public Vector<WordOut> value;
};

class ResultJson{
	public Integer code;
	public String msg;
	public Integer percent;
	public Integer totalNumOfGamesToday;
}

public class LianLianKanController extends Controller {
	public void index(){
		try {
			int nStartIndex = 0;
			int nEndIndex = 0;
			if (this.getPara("endindex") != null && this.getPara("startindex") != null)
			{
				nStartIndex = this.getParaToInt("startindex");
				nEndIndex = this.getParaToInt("endindex");
				if (nStartIndex < 1 || nStartIndex > 80 || 
				nEndIndex < 1 || nEndIndex > 80 || 
				nStartIndex > nEndIndex)
				{
					ReturnJson returnJson = new ReturnJson();
					returnJson.code = 1001;
					returnJson.msg = "WordList参数非法";
					returnJson.value = new Vector<WordOut>();
					String jsonStr = JSON.toJSONString(returnJson);
					this.renderJson(jsonStr);
					return;
				}
			}else{
				nStartIndex = 1;
				nEndIndex = 80;
			}
			File file = new File("/data/wwwroot/www.newsater.org/weixin/file/Barron800WordList.xlsx");
		    InputStream is = new FileInputStream(file);
		    XSSFWorkbook wb = new XSSFWorkbook(is);
		    XSSFSheet st = wb.getSheetAt(0);
		    int lastRowNum = st.getLastRowNum();
		    Map<String,String> mapWordandMeaning = new TreeMap<String,String>();
		    Random random = new Random();
		    for (int i = 0; i <= 9; ++i)
		    {
		    	int rowNum = (nStartIndex-1)*10 + random.nextInt((nEndIndex - nStartIndex + 1)*10);
		    	XSSFRow row = st.getRow(rowNum);
	 
				//排重
				while(mapWordandMeaning.containsKey(row.getCell(0).toString()))
				{
				  rowNum = (nStartIndex-1)*10 + random.nextInt((nEndIndex - nStartIndex + 1)*10);
				  row = st.getRow(rowNum);
				}
				mapWordandMeaning.put(row.getCell(0).toString(), row.getCell(1).toString());
		    }
	    
		    System.out.println("mapWordandMeaning size:"+mapWordandMeaning.size());
		    Iterator it = mapWordandMeaning.keySet().iterator();
		    Map<String,Integer> mapOut = new TreeMap<String,Integer>();
		    int nCount = 0;
		    while (it.hasNext()){
			    String key = it.next().toString();
			    mapOut.put(key, nCount);
			    mapOut.put(mapWordandMeaning.get(key).toString(), nCount);
			    ++nCount;
		    }
	    
		    Vector<WordOut> vecOut = new Vector<WordOut>();
		    it = mapOut.keySet().iterator();
		    nCount = 0;
		    while(it.hasNext()){
			    String key = it.next().toString();
			    WordOut wordOut  = new WordOut();
			    wordOut.name    = key;
			    wordOut.id  = mapOut.get(key);
			    if (nCount < 10)
			    {
			    	wordOut.lang = "en";
			    }
			    else
			    {
			    	wordOut.lang = "zh";
			    }
			    ++nCount;
			    vecOut.add(wordOut);
		    }
	    
		    is.close();
			ReturnJson returnJson = new ReturnJson();
			returnJson.code = 0;
			returnJson.msg = "正确";
			returnJson.value = vecOut;
			String jsonStr = JSON.toJSONString(returnJson);
			this.renderJson(jsonStr);
		}catch(Exception e){
			 e.printStackTrace();
			 ReturnJson returnJson = new ReturnJson();
			 returnJson.code = 1002;
			 returnJson.msg = "文件读写错误";
			 returnJson.value = new Vector<WordOut>();
			 String jsonStr = JSON.toJSONString(returnJson);
			 this.renderJson(jsonStr);
		}
	}
	public void GetResult(){
		MemberBarron800 membar = new MemberBarron800();
		if  (null == this.getPara("weixinid") || null ==  this.getPara("succtime") || null ==  this.getPara("state"))
		{
			ResultJson resultJson = new ResultJson();
			resultJson.code = 1001;
			resultJson.msg = "结果请求参数有误";
			resultJson.percent = 0;
			resultJson.totalNumOfGamesToday = 0;
			String jsonStr = JSON.toJSONString(resultJson);
			this.renderJson(jsonStr);
			return;
		}
		//membar.set("Fweixin_id","oA-0BjyNZ8K_GFRqXoSanT5vnopk");
		membar.set("Fweixin_id",this.getPara("weixinid"));
		membar.set("Fmember_school", 1);
		membar.set("Fsucc_time", this.getParaToInt("succtime"));
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		membar.set("Fend_time", df.format(new Date()));
		membar.set("Fstate", this.getParaToInt("state"));
		membar.save();
		
		//计算排名百分比
		List<MemberBarron800> membarl = membar.getTotalStatisticData();
		Long nSuccTime = this.getParaToLong("succtime");   //获取本次成功时间
		int nIndex = 0;
		for (MemberBarron800 mem : membarl){
			++nIndex;
			if (nSuccTime == mem.getLong("Fsucc_time"))
			{
				break;
			}
		}
		int nPercent = 100 - nIndex * 100 / membarl.size();
		
		//计算当日完成局数
		List<MemberBarron800> todayMembarl = membar.getTodayDateByWeixinId(this.getPara("weixinid"));
		ResultJson resultJson = new ResultJson();
		resultJson.code = 0;
		resultJson.msg = "";
		resultJson.percent = nPercent;
		resultJson.totalNumOfGamesToday = todayMembarl.size();
		String jsonStr = JSON.toJSONString(resultJson);
		this.renderJson(jsonStr);
	  }
}