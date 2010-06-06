/*

    This file is part of OpenMyEWB.

    OpenMyEWB is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OpenMyEWB is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OpenMyEWB.  If not, see <http://www.gnu.org/licenses/>.

    OpenMyEWB is Copyright 2005-2009 Nicolas Kruchten (nicolas@kruchten.com), Francis Kung, Engineers Without Borders Canada, Michael Trauttmansdorff, Jon Fishbein, David Kadish

*/

package ca.myewb.model;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import ca.myewb.frame.HibernateUtil;

public class ConferenceRegistrationModel 
{
	private int id;
	private UserModel user;
	private GroupChapterModel chapter;
	private int amountPaid;
	private int roomSize;
	private boolean subsidized;
	private String type;
	private Date date;
	private boolean headset;
	private String foodPrefs;
	private boolean cancelled;
	private String specialNeeds;
	private String emergName;
	private String emergPhone;
	private String code;
	private int prevConfs;
	private int prevRetreats;
	private String receiptNum;
	private boolean africafund;
	
	private static Hashtable<String, Integer> costs = new Hashtable<String, Integer>();
	static
	{
		costs.put("09-stureg0-gtasub", 300);
		costs.put("09-stureg4-gtasub", 350);
		costs.put("09-stureg2-gtasub", 525);
		costs.put("09-stureg1-gtasub", 850);
		
		costs.put("09-stureg0-onqcsub", 250);
		costs.put("09-stureg4-onqcsub", 300);
		costs.put("09-stureg2-onqcsub", 475);
		costs.put("09-stureg1-onqcsub", 700);

		costs.put("09-stureg0-sub", 200);
		costs.put("09-stureg4-sub", 250);
		costs.put("09-stureg2-sub", 425);
		costs.put("09-stureg1-sub", 650);

		costs.put("09-stureg0", 400);
		costs.put("09-stureg4", 450);
		costs.put("09-stureg2", 625);
		costs.put("09-stureg1", 850);

		costs.put("09-proreg0", 450);
		costs.put("09-proreg4", 525);
		costs.put("09-proreg2", 675);
		costs.put("09-proreg1", 900);
	}
	
	private static Hashtable<String, String> names = new Hashtable<String, String>();
	static
	{
		names.put("09-stureg0-gtasub", "Student No-Hotel Registration (GTA discount)");
		names.put("09-stureg4-gtasub", "Student Quad-Room Registration (GTA discount)");
		names.put("09-stureg2-gtasub", "Student Double-Room Registration (GTA discount)");
		names.put("09-stureg1-gtasub", "Student Single-Room Registration (GTA discount)");

		names.put("09-stureg0-onqcsub", "Student No-Hotel Registration (ON/QC discount)");
		names.put("09-stureg4-onqcsub", "Student Quad-Room Registration (ON/QC discount)");
		names.put("09-stureg2-onqcsub", "Student Double-Room Registration (ON/QC discount)");
		names.put("09-stureg1-onqcsub", "Student Single-Room Registration (ON/QC discount)");

		names.put("09-stureg0-sub", "Student No-Hotel Registration (with discount)");
		names.put("09-stureg4-sub", "Student Quad-Room Registration (with discount)");
		names.put("09-stureg2-sub", "Student Double-Room Registration (with discount)");
		names.put("09-stureg1-sub", "Student Single-Room Registration (with discount)");

		names.put("09-stureg0", "Student No-Hotel Registration");
		names.put("09-stureg4", "Student Quad-Room Registration");
		names.put("09-stureg2", "Student Double-Room Registration");
		names.put("09-stureg1", "Student Single-Room Registration");

		names.put("09-proreg0", "Non-Student No-Hotel Registration");
		names.put("09-proreg4", "Non-Student Quad-Room Registration");
		names.put("09-proreg2", "Non-Student Double-Room Registration");
		names.put("09-proreg1", "Non-Student Single-Room Registration");
	}
	
	public boolean isAfricafund() {
		return africafund;
	}

	public void setAfricafund(boolean africafund) {
		this.africafund = africafund;
	}

	public String getReceiptNum() {
		return receiptNum;
	}

	public void setReceiptNum(String receiptNum) {
		this.receiptNum = receiptNum;
	}

	public ConferenceRegistrationModel()
	{
		
	}

	public static ConferenceRegistrationModel newRegistration(UserModel user, String type,
			int prevConfs, int prevRetreats, boolean headset, String foodPrefs, String emergName, String emergPhone, 
			String specialNeeds, String code, String receiptNum, boolean africafund)
	{
		ConferenceRegistrationModel reg = new ConferenceRegistrationModel();
		reg.setUser(user);
		reg.setType(type);
		reg.setDate(new Date());
		reg.setReceiptNum(receiptNum);
		reg.setAmountPaid(costs.get(type));
		reg.setChapter(user.getChapter());
		reg.setSubsidized(type.contains("sub"));

		reg.setCancelled(false);
		
		if(type.contains("reg0"))
			reg.setRoomSize(0);
		if(type.contains("reg1"))
			reg.setRoomSize(1);
		if(type.contains("reg2"))
			reg.setRoomSize(2);
		if(type.contains("reg4"))
			reg.setRoomSize(4);
		
		reg.setEmergName(emergName);
		reg.setEmergPhone(emergPhone);
		reg.setSpecialNeeds(specialNeeds);
		reg.setHeadset(headset);
		reg.setFoodPrefs(foodPrefs);
		reg.setPrevConfs(prevConfs);
		reg.setPrevRetreats(prevRetreats);
		reg.setCode(code);
		
		reg.setAfricafund(africafund);
		
		HibernateUtil.currentSession().save(reg);
		return reg;
	}
	

	public void cancel() 
	{
		setCancelled(true);
		setChapter(null); // so that they no longer show up on the chapter reg page
	}
	
	public String getName()
	{
		return names.get(type);
	}
	
	public int getRefundableAmount()
	{
		if(type.contains("pro"))
			return amountPaid - 50;
		else
			return amountPaid - 20;
	}

	
	public static boolean needsToRenew(UserModel u)
	{
		if(u.isMember("Associate"))
		{
			return true;
		}
		
		GregorianCalendar cal = new GregorianCalendar();
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, 20);
		cal.set(Calendar.YEAR, 2009);
		cal.set(Calendar.HOUR_OF_DAY, 1);
		cal.set(Calendar.MINUTE, 0);
		
		GregorianCalendar exp = new GregorianCalendar();
		exp.setTime(u.getExpiry());
		return exp.before(cal);
	}
	
	public static String getName(String sku)
	{
		return names.get(sku);
	}
	
	public static Integer getCost(String sku)
	{
		return costs.get(sku);
	}
	
	public UserModel getUser() {
		return user;
	}


	public void setUser(UserModel user) {
		this.user = user;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public int getAmountPaid() {
		return amountPaid;
	}

	public void setAmountPaid(int amountPaid) {
		this.amountPaid = amountPaid;
	}

	public GroupChapterModel getChapter() {
		return chapter;
	}

	public void setChapter(GroupChapterModel chapter) {
		this.chapter = chapter;
	}

	public boolean isSubsidized() {
		return subsidized;
	}

	public void setSubsidized(boolean subsidized) {
		this.subsidized = subsidized;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getRoomSize() {
		return roomSize;
	}

	public void setRoomSize(int roomSize) {
		this.roomSize = roomSize;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getEmergName() {
		return emergName;
	}

	public void setEmergName(String emergName) {
		this.emergName = emergName;
	}

	public String getEmergPhone() {
		return emergPhone;
	}

	public void setEmergPhone(String emergPhone) {
		this.emergPhone = emergPhone;
	}

	public boolean isHeadset() {
		return headset;
	}

	public void setHeadset(boolean headset) {
		this.headset = headset;
	}
	
	public String getFoodPrefs() {
		return foodPrefs;
	}
	
	public void setFoodPrefs(String foodPrefs) {
		this.foodPrefs = foodPrefs;
	}	

	public int getPrevConfs() {
		return prevConfs;
	}

	public void setPrevConfs(int prevConfs) {
		this.prevConfs = prevConfs;
	}

	public int getPrevRetreats() {
		return prevRetreats;
	}

	public void setPrevRetreats(int prevRetreats) {
		this.prevRetreats = prevRetreats;
	}

	public String getSpecialNeeds() {
		return specialNeeds;
	}

	public void setSpecialNeeds(String specialNeeds) {
		this.specialNeeds = specialNeeds;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
