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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ca.myewb.model.ConferenceRegistrationModel;
import ca.myewb.model.GroupModel;

public abstract class GroupChapter extends GroupModel {


	protected String address1;
	protected String suite;
	protected String address2;
	protected String city;
	protected String province;
	protected String postalcode;
	protected String country;
	protected String email;
	protected String url;
	
	protected String phone;
	protected String fax;
	protected GroupModel exec;
	protected boolean francophone;
	protected boolean professional;
	protected Set<GroupModel> children;
	
	////////////// confreg
	protected Collection<ConferenceRegistrationModel> registrations; 

	public Collection<ConferenceRegistrationModel> getRegistrations() {
		return registrations;
	}

	public void setRegistrations(Collection<ConferenceRegistrationModel> registrations) {
		this.registrations = registrations;
	}
	//////////////// end-confreg

	public GroupChapter() throws Exception {
		super();
	}
	
	public GroupChapter(Group exec) throws Exception
	{
		super();
		shortname = "";
		phone = "";
		fax = "";
		francophone = false;
		professional = false;
		this.exec = (GroupModel)exec;
		children = new HashSet<GroupModel>();
		super.setPublic(true);
	}

	public GroupModel getExec() {
		return exec;
	}

	private void setExec(Group exec) {
		this.exec = (GroupModel)exec;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Set<GroupModel> getChildren() {
		return children;
	}

	public void setChildren(Set<GroupModel> children) {
		this.children = children;
	}

	public boolean isFrancophone()
	{
		return francophone;
	}

	public void setFrancophone(boolean francophone)
	{
		this.francophone = francophone;
	}

	public boolean isProfessional()
	{
		return professional;
	}

	public void setProfessional(boolean professional)
	{
		this.professional = professional;
	}


	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPostalcode() {
		return postalcode;
	}

	public void setPostalcode(String postalcode) {
		this.postalcode = postalcode;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getSuite() {
		return suite;
	}

	public void setSuite(String suite) {
		this.suite = suite;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
