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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import ca.myewb.frame.HibernateUtil;
import ca.myewb.model.ApplicationModel;
import ca.myewb.model.ConferenceRegistrationModel;
import ca.myewb.model.OVInfoModel;
import ca.myewb.model.PlacementModel;
import ca.myewb.model.PostModel;
import ca.myewb.model.RoleModel;

public abstract class User {

	protected int id;
	protected String username;
	protected String passhash;
	protected String firstname;
	protected String lastname;
	protected String email;
	protected String language;
	protected String phone;
	protected String businessno;
	protected String cellno;
	protected String alternateno;
	protected int birth;
	protected char gender;
	protected char student;
	protected char canadianinfo;
	protected Date expiry;
	protected Date addressUpdated;
	protected Collection<RoleModel> roles;
	protected Collection<PostModel> posts;
	protected Collection<PlacementModel> placements;
	protected Logger log;
	protected Session session;
	protected Date lastLogin;
	protected Date currentLogin;
	protected int logins;
	protected boolean adminToggle;
	protected boolean showreplies;
	protected boolean showemails;
	protected boolean sortByLastReply;
	protected String studentnumber;
	protected String studentinstitution;
	protected String studentfield;
	protected int studentlevel;
	protected int studentgradmonth;
	protected int studentgradyear;
	protected String proemployer;
	protected String prosector;
	protected String proposition;
	protected int procompsize;
	protected int proincomelevel;
	protected String additionalInfo;
	private char upgradeLevel;
	private Collection<ApplicationModel> applications;
	private Set<String> emails;
	private Set<PostModel> flaggedPosts;
	protected boolean repliesAsEmails;
	
	protected String address1;
	protected String suite;
	protected String address2;
	protected String city;
	protected String province;
	protected String postalcode;
	protected String country;	
		
/////////// confreg	
	protected Collection<ConferenceRegistrationModel> registrations; 

	
	public Collection<ConferenceRegistrationModel> getRegistrations() {   	 	 
        return registrations;  	 	 
    }  	 	 
      	 	 
    public ConferenceRegistrationModel getRegistration()  	 	 
    {
    	for(ConferenceRegistrationModel r: registrations)
    	{
    		if(!r.isCancelled()) return r;
    	}
    	return null;
    }  	 	 
	 	 
    public void setRegistrations(  	 	 
                    Collection<ConferenceRegistrationModel> registrations) {  	 	 
            this.registrations = registrations;  	 	 
    } 
////////////////// end-confreg
	
	public Collection<ApplicationModel> getApplications()
	{
		return applications;
	}

	protected void setApplications(Collection<ApplicationModel> applications)
	{
		this.applications = applications;
	}


	public boolean isRepliesAsEmails() {
		return repliesAsEmails;
	}

	public void setRepliesAsEmails(boolean repliesAsEmails) {
		this.repliesAsEmails = repliesAsEmails;
	}
	
	public User() throws Exception
	{
		id = 0;
		username = "";
		passhash = "";
		firstname = "";
		lastname = "";
		adminToggle = true;
		showemails = true;
		showreplies = false;
		sortByLastReply = true;
		repliesAsEmails = false;

		gender = 0;
		student = 0;

		email = "";
		language = "";

		studentnumber = "";
		studentinstitution = "";
		studentfield = "";
		studentlevel = 0;
		studentgradmonth = 0;
		studentgradyear = 0;

		proemployer = "";
		prosector = "";
		proposition = "";
		procompsize = 0;
		proincomelevel = 0;
		
		additionalInfo = "";

		roles = new HashSet<RoleModel>();
		posts = new HashSet<PostModel>();
		placements = new HashSet<PlacementModel>();
		applications = new HashSet<ApplicationModel>();
		emails = new HashSet<String>();
		flaggedPosts = new HashSet<PostModel>();

		log = Logger.getLogger(this.getClass());

		try
		{
			session = HibernateUtil.currentSession();
		}
		catch (Exception e)
		{
			log.fatal("Problem getting session for User object: " + e, e);
			throw e;
		}
	}

	public int getBirth() {
		return birth;
	}

	public void setBirth(int birth) {
		this.birth = birth;
	}

	public String getBusinessno() {
		return businessno;
	}

	public void setBusinessno(String businessno) {
		this.businessno = businessno;
	}

	public Date getExpiry() {
		return expiry;
	}

	public void setExpiry(Date expiry) {
		this.expiry = expiry;
	}

	public char getGender() {
		return gender;
	}

	public void setGender(char gender) {
		this.gender = gender;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public char getStudent() {
		return student;
	}

	public void setStudent(char student) {
		this.student = student;
	}

	public Collection getRoles() {
		return roles;
	}

	private void setRoles(Collection<RoleModel> r) {
		roles = r;
	}

	public Date getCurrentLogin() {
		return currentLogin;
	}

	public void setCurrentLogin(Date currentLogin) {
		this.currentLogin = currentLogin;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	private void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	public int getLogins() {
		return logins;
	}

	private void setLogins(int logins) {
		this.logins = logins;
	}

	public Collection<PostModel> getPosts() {
		return posts;
	}

	private void setPosts(Collection<PostModel> p) {
		posts = p;
	}

	public void setCanadianinfo(char canadianinfo) {
		this.canadianinfo = canadianinfo;
	}

	public char getCanadianinfo() {
		return canadianinfo;
	}

	public int getProcompsize() {
		return procompsize;
	}

	public void setProcompsize(int procompsize) {
		this.procompsize = procompsize;
	}

	public String getProemployer() {
		return proemployer;
	}

	public void setProemployer(String proemployer) {
		this.proemployer = proemployer;
	}

	public int getProincomelevel() {
		return proincomelevel;
	}

	public void setProincomelevel(int proincomelevel) {
		this.proincomelevel = proincomelevel;
	}

	public String getProposition() {
		return proposition;
	}

	public void setProposition(String proposition) {
		this.proposition = proposition;
	}

	public String getProsector() {
		return prosector;
	}

	public void setProsector(String prosector) {
		this.prosector = prosector;
	}

	public String getStudentfield() {
		return studentfield;
	}

	public void setStudentfield(String studentfield) {
		this.studentfield = studentfield;
	}

	public int getStudentgradmonth() {
		return studentgradmonth;
	}

	public void setStudentgradmonth(int studentgradmonth) {
		this.studentgradmonth = studentgradmonth;
	}

	public int getStudentgradyear() {
		return studentgradyear;
	}

	public void setStudentgradyear(int studentgradyear) {
		this.studentgradyear = studentgradyear;
	}

	public String getStudentinstitution() {
		return studentinstitution;
	}

	public void setStudentinstitution(String studentinstitution) {
		this.studentinstitution = studentinstitution;
	}

	public int getStudentlevel() {
		return studentlevel;
	}

	public void setStudentlevel(int studentlevel) {
		this.studentlevel = studentlevel;
	}

	public String getStudentnumber() {
		return studentnumber;
	}

	public void setStudentnumber(String studentnumber) {
		this.studentnumber = studentnumber;
	}

	public int getId() {
		return id;
	}

	private void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String name) {
		username = name;
	}

	private String getPasshash() {
		return passhash;
	}

	public void setPasshash(String p) {
		passhash = p;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String name) {
		firstname = name;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String name) {
		lastname = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public char getUpgradeLevel() {
		return upgradeLevel;
	}

	public void setUpgradeLevel(char upgradeLevel) {
		this.upgradeLevel = upgradeLevel;
	}

	public boolean getAdminToggle() {
		return adminToggle;
	}

	private void setAdminToggle(boolean adminToggle) {
		this.adminToggle = adminToggle;
	}

	public Collection<PlacementModel> getPlacements() {
		return placements;
	}

	public void setPlacements(Collection<PlacementModel> placements) {
		this.placements = placements;
	}



	public OVInfoModel getOVInfo() {
		return OVInfoModel.getForUser(id);
	}

	public void setOVInfo(OVInfoModel input) {
		input.setUserid(id);
	}

	public Set<String> getEmails()
	{
		return emails;
	}

	protected void setEmails(Set<String> emails)
	{
		this.emails = emails;
	}

	public Set<PostModel> getFlaggedPosts() {
		return flaggedPosts;
	}

	public void setFlaggedPosts(Set<PostModel> flaggedPosts) {
		this.flaggedPosts = flaggedPosts;
	}

	public String getAlternateno()
	{
		return alternateno;
	}

	public void setAlternateno(String alternateno)
	{
		this.alternateno = alternateno;
	}

	public String getCellno()
	{
		return cellno;
	}

	public void setCellno(String cellno)
	{
		this.cellno = cellno;
	}

	public boolean getShowemails()
	{
		return showemails;
	}

	public void setShowemails(boolean showemails)
	{
		this.showemails = showemails;
	}

	public boolean getShowreplies()
	{
		return showreplies;
	}

	public void setShowreplies(boolean showreplies)
	{
		this.showreplies = showreplies;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public boolean getSortByLastReply() {
		return sortByLastReply;
	}

	public void setSortByLastReply(boolean sortByLastReply) {
		this.sortByLastReply = sortByLastReply;
	}


	public Date getAddressUpdated()
	{
		return addressUpdated;
	}

	public void setAddressUpdated(Date addressUpdated)
	{
		this.addressUpdated = addressUpdated;
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
	
	
}
