package com.javen.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

public class Member extends Model<Member> {
	public static final Member dao=new Member();
	
	public List<Member> getMemberById(String id){
		return Member.dao.find("select * from t_member where id=?", id);
	}
	
	public List<Member> getMemberByWeixinId(String weixinId){
		return Member.dao.find("select * from t_member where Fweixin_id=?", weixinId);
	}
	
	public List<Member> getAll(){
		return Member.dao.find("select * from t_member");
	}
}
