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

package ca.myewb.beans;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class OVInfo {

	private int id;
	private int userid;
	protected String healthnumber;
	protected String sin;
	protected Date dob;
	protected String passportnumber;
	protected String passportname;
	protected String passportplace;
	protected Date passportstart;
	protected Date passportend;
	protected String e1name;
	protected String e1relation;
	protected String e1address;
	protected String e1business;
	protected String e1home;
	protected String e1fax;
	protected String e1email;
	protected String e1language;
	protected boolean e1updates;
	protected String e2name;
	protected String e2relation;
	protected String e2address;
	protected String e2business;
	protected String e2home;
	protected String e2fax;
	protected String e2email;
	protected String e2language;
	protected boolean e2updates;
	protected static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	public OVInfo()
	{
		
		healthnumber = "";
		sin = "";
		dob = null;
		
		passportnumber = "";
		passportname = "";
		passportplace = "";
		passportstart = null;
		passportend = null;

		e1name = "";
		e1relation = "";
		e1address = "";
		e1business = "";
		e1home = "";
		e1fax = "";
		e1email = "";
		e1relation = "";
		e1language = "";

		e2name = "";
		e2relation = "";
		e2address = "";
		e2business = "";
		e2home = "";
		e2fax = "";
		e2email = "";
		e2relation = "";
		e2language = "";
	}

	public Date getDob() {
		return dob;
	}

	public void setDob(Date dob) {
		this.dob = dob;
	}

	public String getE1address() {
		return e1address;
	}

	public void setE1address(String e1address) {
		this.e1address = e1address;
	}

	public String getE1business() {
		return e1business;
	}

	public void setE1business(String e1business) {
		this.e1business = e1business;
	}

	public String getE1email() {
		return e1email;
	}

	public void setE1email(String e1email) {
		this.e1email = e1email;
	}

	public String getE1fax() {
		return e1fax;
	}

	public void setE1fax(String e1fax) {
		this.e1fax = e1fax;
	}

	public String getE1home() {
		return e1home;
	}

	public void setE1home(String e1home) {
		this.e1home = e1home;
	}

	public String getE1language() {
		return e1language;
	}

	public void setE1language(String e1language) {
		this.e1language = e1language;
	}

	public String getE1name() {
		return e1name;
	}

	public void setE1name(String e1name) {
		this.e1name = e1name;
	}

	public String getE1relation() {
		return e1relation;
	}

	public void setE1relation(String e1relation) {
		this.e1relation = e1relation;
	}

	public boolean isE1updates() {
		return e1updates;
	}

	public void setE1updates(boolean e1updates) {
		this.e1updates = e1updates;
	}

	public String getE2address() {
		return e2address;
	}

	public void setE2address(String e2address) {
		this.e2address = e2address;
	}

	public String getE2business() {
		return e2business;
	}

	public void setE2business(String e2business) {
		this.e2business = e2business;
	}

	public String getE2email() {
		return e2email;
	}

	public void setE2email(String e2email) {
		this.e2email = e2email;
	}

	public String getE2fax() {
		return e2fax;
	}

	public void setE2fax(String e2fax) {
		this.e2fax = e2fax;
	}

	public String getE2home() {
		return e2home;
	}

	public void setE2home(String e2home) {
		this.e2home = e2home;
	}

	public String getE2language() {
		return e2language;
	}

	public void setE2language(String e2language) {
		this.e2language = e2language;
	}

	public String getE2name() {
		return e2name;
	}

	public void setE2name(String e2name) {
		this.e2name = e2name;
	}

	public String getE2relation() {
		return e2relation;
	}

	public void setE2relation(String e2relation) {
		this.e2relation = e2relation;
	}

	public boolean isE2updates() {
		return e2updates;
	}

	public void setE2updates(boolean e2updates) {
		this.e2updates = e2updates;
	}

	public String getHealthnumber() {
		return healthnumber;
	}

	public void setHealthnumber(String healthnumber) {
		this.healthnumber = healthnumber;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getPassportend() {
		return passportend;
	}

	public void setPassportend(Date passportend) {
		this.passportend = passportend;
	}

	public String getPassportname() {
		return passportname;
	}

	public void setPassportname(String passportname) {
		this.passportname = passportname;
	}

	public String getPassportnumber() {
		return passportnumber;
	}

	public void setPassportnumber(String passportnumber) {
		this.passportnumber = passportnumber;
	}

	public String getPassportplace() {
		return passportplace;
	}

	public void setPassportplace(String passportplace) {
		this.passportplace = passportplace;
	}

	public Date getPassportstart() {
		return passportstart;
	}

	public void setPassportstart(Date passportstart) {
		this.passportstart = passportstart;
	}

	public String getSin() {
		return sin;
	}

	public void setSin(String sin) {
		this.sin = sin;
	}

	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

}
