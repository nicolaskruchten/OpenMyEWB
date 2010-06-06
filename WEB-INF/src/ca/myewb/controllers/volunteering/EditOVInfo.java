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

package ca.myewb.controllers.volunteering;

import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.forms.OVInfoEditForm;
import ca.myewb.model.OVInfoModel;
import ca.myewb.model.UserModel;


public class EditOVInfo extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		OVInfoEditForm f = (OVInfoEditForm)checkForValidationFailure(ctx);
		
		if(f == null)
		{
			f = new OVInfoEditForm(path + "/actions/SaveOVInfo/" + urlParams.getParam(), requestParams);

			UserModel theUser = (UserModel)getAndCheckFromUrl(UserModel.class);		
			
			if(!theUser.equals(currentUser) && !currentUser.isAdmin())
			{
				throw getSecurityException("you can't view this information!", path + "/volunteering/PlacementList");
			}
			
			if(!theUser.isMember("OVs", false))
			{
				throw getSecurityException("that user is not an OVS!",  path + "/volunteering/PlacementList");
			}
			
			OVInfoModel theInfo = theUser.getOVInfo();
			
			if(theInfo == null)
			{
				theInfo = OVInfoModel.newOVInfo();
			}
			
			f.setValue("Healthnumber", theInfo.getHealthnumber());
			f.setValue("SIN", theInfo.getSin());
			f.setValue("DOB", theInfo.getFormattedDob());

			f.setValue("Passname", theInfo.getPassportname());
			f.setValue("Passnumber", theInfo.getPassportnumber());
			f.setValue("Passplace", theInfo.getPassportplace());
			f.setValue("Passstart", theInfo.getFormattedPassportstart());
			f.setValue("Passend", theInfo.getFormattedPassportend());

			f.setValue("E1name", theInfo.getE1name());
			f.setValue("E1relation", theInfo.getE1relation());
			f.setValue("E1address", theInfo.getE1address());
			f.setValue("E1home", theInfo.getE1home());
			f.setValue("E1business", theInfo.getE1business());
			f.setValue("E1fax", theInfo.getE1fax());
			f.setValue("E1email", theInfo.getE1email());
			f.setValue("E1language", theInfo.getE1language());
			if(theInfo.isE1updates())
			{
				f.setValue("E1updates", "on");
			}

			f.setValue("E2name", theInfo.getE2name());
			f.setValue("E2relation", theInfo.getE2relation());
			f.setValue("E2address", theInfo.getE2address());
			f.setValue("E2home", theInfo.getE2home());
			f.setValue("E2business", theInfo.getE2business());
			f.setValue("E2fax", theInfo.getE2fax());
			f.setValue("E2email", theInfo.getE2email());
			f.setValue("E2language", theInfo.getE2language());
			if(theInfo.isE2updates())
			{
				f.setValue("E2updates", "on");
			}
			
		}
		
		ctx.put("form", f);
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();

		s.add("OVs");

		return s;
	}
	
	public int weight()
	{
		return -15;
	}

	public String displayName()
	{
		return "Edit OV Info";
	}
	
	public boolean secureAccessRequired()
	{
		return true;
	}
}
