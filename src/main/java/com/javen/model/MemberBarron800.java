package com.javen.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;
public class MemberBarron800 extends Model<MemberBarron800> {
	public static final MemberBarron800 dao=new MemberBarron800();
	
	public List<MemberBarron800> getMemberById(long id){
		return MemberBarron800.dao.find("select * from t_member_barron800 where Fid=?", id);
	}
	
	public List<MemberBarron800> getMemberByWeixinId(String weixinId){
		return MemberBarron800.dao.find("select * from t_member_barron800 where Fweixin_id=?", weixinId);
	}
	
	public List<MemberBarron800> getAll(){
		return MemberBarron800.dao.find("select * from t_member_barron800 where 1=1");
	}
	
	public List<MemberBarron800> getTotalStatisticData(){
		return MemberBarron800.dao.find("select Fsucc_time from t_member_barron800 "
				+ "where Fstate=2 order by Fsucc_time asc");
	}
	
	public List<MemberBarron800> getTodayDateByWeixinId(String weixinId){
		return MemberBarron800.dao.find("select * from t_member_barron800 where Fend_time >=date(now()) "
				+ "and Fend_time <DATE_ADD(date(now()),INTERVAL 1 DAY) and Fweixin_id=?", weixinId);
	}
}
