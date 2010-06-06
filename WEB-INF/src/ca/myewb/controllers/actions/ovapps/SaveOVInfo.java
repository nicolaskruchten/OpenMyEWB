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

package ca.myewb.controllers.actions.ovapps;

import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Message;
import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.OVInfoEditForm;
import ca.myewb.model.OVInfoModel;
import ca.myewb.model.UserModel;


public class SaveOVInfo extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		OVInfoEditForm form = new OVInfoEditForm(path
		                                           + "/actions/SaveOVInfo/"
		                                           + urlParams.getParam(),
		                                           requestParams);

		Message m = form.validate();

		// No messages: changes are valid
		if (m != null)
		{
				throw getValidationException(form, m,
				                             path + "/volunteering/EditOVInfo/"
				                             + urlParams.getParam());
		}

		UserModel theUser = (UserModel)getAndCheckFromUrl(UserModel.class);
		OVInfoModel theInfo = theUser.retreiveOVInfo();
		
		if(!theUser.equals(currentUser) && !currentUser.isAdmin())
		{
			throw getSecurityException("you can't edit this information!", path + "/volunteering/PlacementList");
		}
		
		if(!theUser.isMember("OVs", false))
		{
			throw getSecurityException("that user is not an OVS!",  path + "/volunteering/PlacementList");
		}

		theInfo.setHealthnumber(form.getParameter("Healthnumber"));
		theInfo.setSin(form.getParameter("SIN"));
		theInfo.setDob(form.getParameterAsDate("DOB"));
		
		theInfo.setPassportnumber(form.getParameter("Passnumber"));
		theInfo.setPassportname(form.getParameter("Passname"));
		theInfo.setPassportplace(form.getParameter("Passplace"));
		theInfo.setPassportstart(form.getParameterAsDate("Passstart"));
		theInfo.setPassportend(form.getParameterAsDate("Passend"));

		theInfo.setE1name(form.getParameter("E1name"));
		theInfo.setE1relation(form.getParameter("E1relation"));
		theInfo.setE1address(form.getParameter("E1address"));
		theInfo.setE1business(form.getParameter("E1business"));
		theInfo.setE1home(form.getParameter("E1home"));
		theInfo.setE1fax(form.getParameter("E1fax"));
		theInfo.setE1email(form.getParameter("E1email"));
		theInfo.setE1language(form.getParameter("E1language"));
		theInfo.setE1updates(form.getParameter("E1updates").equals("on"));

		theInfo.setE2name(form.getParameter("E2name"));
		theInfo.setE2relation(form.getParameter("E2relation"));
		theInfo.setE2address(form.getParameter("E2address"));
		theInfo.setE2business(form.getParameter("E2business"));
		theInfo.setE2home(form.getParameter("E2home"));
		theInfo.setE2fax(form.getParameter("E2fax"));
		theInfo.setE2email(form.getParameter("E2email"));
		theInfo.setE2language(form.getParameter("E2language"));
		theInfo.setE2updates(form.getParameter("E2updates").equals("on"));
		
		// Leave a message in the session
		setSessionMessage(("OV Info Updated"));

		// Redirect to somewhere
		throw new RedirectionException(path + "/volunteering/OVInfo/" + urlParams.getParam());
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("OVs");

		return s;
	}
	
	public boolean secureAccessRequired()
	{
		return true;
	}
	
	public String oldName()
	{
		return "SaveOVInfo";
	}
}
