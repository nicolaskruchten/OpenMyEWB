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

import java.util.Date;

import ca.myewb.frame.HibernateUtil;


public class CDayRegistrationModel 
{
	private int id;
	private String name;
	private String phone;
	private String affiliation;
	private String sector;
	private String experience;
	private String email;
	private String address1;
	private String suite;
	private String address2;
	private String city;
	private String province;
	private String postalcode;
	private String country;
	private String specialNeeds;
	private boolean headset;
	private String receiptNum;
	private Date date;
	private String heardabout;
	private String language;
	private int numtickets;
	private String prochapter;
	private boolean chapterdinner;
	private String prochaptername;
	
	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}

	public CDayRegistrationModel()
	{
		
	}
	
	public String getReceiptNum()
	{
		return receiptNum;
	}

	public void setReceiptNum(String receiptNum)
	{
		this.receiptNum = receiptNum;
	}

	public CDayRegistrationModel(String address1, String address2, String affiliation, 
			String city, String country, String email, String experience, 
			boolean headset, String heardabout, String name, String postalcode, 
			String province, String needs, String suite, String receiptNum, String sector,
			String language, int numtickets, String phone, String prochapter, 
			boolean chapterdinner, String prochaptername)
	{
		super();
		this.phone = phone;
		this.address1 = address1;
		this.address2 = address2;
		this.affiliation = affiliation;
		this.city = city;
		this.country = country;
		this.date = new Date();
		this.email = email;
		this.experience = experience;
		this.headset = headset;
		this.heardabout = heardabout;
		this.name = name;
		this.postalcode = postalcode;
		this.province = province;
		specialNeeds = needs;
		this.suite = suite;
		this.receiptNum = receiptNum;
		this.sector = sector;
		this.language = language;
		this.numtickets = numtickets;
		this.prochapter = prochapter;
		this.chapterdinner = chapterdinner;
		this.prochaptername = prochaptername;
	}

	public static CDayRegistrationModel newCDayReg(String address, String affiliation, 
			String email, String experience, boolean headset, String heardabout, 
			String name, String needs, String receiptNum, String sector, String language,
			int numtickets, String phone, String prochapter, boolean chapterdinner, 
			String prochaptername)
	{
		String[] splitAddress = address.split("\n");
		String address1 = (splitAddress.length > 0 && splitAddress[0] != null) ? splitAddress[0] : "";
		String suite = (splitAddress.length > 1 && splitAddress[1] != null) ? splitAddress[1] : "";
		String address2 = (splitAddress.length > 2 && splitAddress[2] != null) ? splitAddress[2] : "";
		String city = (splitAddress.length > 3 && splitAddress[3] != null) ? splitAddress[3] : "";
		String province = (splitAddress.length > 4 && splitAddress[4] != null) ? splitAddress[4] : "";
		String postalcode = (splitAddress.length > 5 && splitAddress[5] != null) ? splitAddress[5] : "";
		String country = (splitAddress.length > 6 && splitAddress[6] != null) ? splitAddress[6] : "";
		
		CDayRegistrationModel reg = new CDayRegistrationModel(address1, address2, affiliation, 
				city, country, email, experience, headset, heardabout, name, postalcode, 
				province, needs, suite, receiptNum, sector, language, numtickets, phone,
				prochapter, chapterdinner, prochaptername);
		HibernateUtil.currentSession().save(reg);
		return reg;
	}
	
	public String getAddress1()
	{
		return address1;
	}
	public void setAddress1(String address1)
	{
		this.address1 = address1;
	}
	public String getAddress2()
	{
		return address2;
	}
	public void setAddress2(String address2)
	{
		this.address2 = address2;
	}
	public String getAffiliation()
	{
		return affiliation;
	}
	public void setAffiliation(String affiliation)
	{
		this.affiliation = affiliation;
	}
	public String getCity()
	{
		return city;
	}
	public void setCity(String city)
	{
		this.city = city;
	}
	public String getCountry()
	{
		return country;
	}
	public void setCountry(String country)
	{
		this.country = country;
	}
	public Date getDate()
	{
		return date;
	}
	public void setDate(Date date)
	{
		this.date = date;
	}
	public String getEmail()
	{
		return email;
	}
	public void setEmail(String email)
	{
		this.email = email;
	}
	public String getExperience()
	{
		return experience;
	}
	public void setExperience(String experience)
	{
		this.experience = experience;
	}
	public boolean isHeadset()
	{
		return headset;
	}
	public void setHeadset(boolean headset)
	{
		this.headset = headset;
	}
	public String getHeardabout()
	{
		return heardabout;
	}
	public void setHeardabout(String heardabout)
	{
		this.heardabout = heardabout;
	}
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getPostalcode()
	{
		return postalcode;
	}
	public void setPostalcode(String postalcode)
	{
		this.postalcode = postalcode;
	}
	public String getProvince()
	{
		return province;
	}
	public void setProvince(String province)
	{
		this.province = province;
	}
	public String getSpecialNeeds()
	{
		return specialNeeds;
	}
	public void setSpecialNeeds(String specialNeeds)
	{
		this.specialNeeds = specialNeeds;
	}
	public String getSuite()
	{
		return suite;
	}
	public void setSuite(String suite)
	{
		this.suite = suite;
	}

	public String getSector()
	{
		return sector;
	}

	public void setSector(String sector)
	{
		this.sector = sector;
	}

	public int getNumtickets()
	{
		return numtickets;
	}

	public void setNumtickets(int numtickets)
	{
		this.numtickets = numtickets;
	}

	public String getPhone()
	{
		return phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}
	
	public String getProchapter()
	{
		return prochapter;
	}

	public void setProchapter(String pc)
	{
		this.prochapter = pc;
	}

	public boolean getChapterdinner()
	{
		return chapterdinner;
	}

	public void setChapterdinner(boolean c)
	{
		this.chapterdinner = c;
	}

	public String getChaptername()
	{
		return prochaptername;
	}

	public void setChaptername(String pcn)
	{
		this.prochaptername = pcn;
	}
}
