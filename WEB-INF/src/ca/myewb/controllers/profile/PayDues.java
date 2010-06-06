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

package ca.myewb.controllers.profile;

import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.forms.PayDuesForm;


public class PayDues extends Controller
{
	public void handle(Context ctx) throws Exception
	{	
		if(currentUser.isMember(Helpers.getGroup("Regular")))
		{
			if(currentUser.canRenew())
			{
				ctx.put("mode", "canRenew");
			}
			else
			{
				ctx.put("mode", "alreadyPaid");
			}
		}
		else
		{
			ctx.put("mode", "canPay");
		}
		
		PayDuesForm f = (PayDuesForm)checkForValidationFailure(ctx);
		
		if(f == null)
		{
			f = new PayDuesForm(path + "/actions/SubmitDuesPayment",
                    requestParams, currentUser.getCanadianinfo() == 'n', false);
			
			f.setValue("Firstname", currentUser.getFirstname());
			f.setValue("Lastname", currentUser.getLastname());
			f.setValue("Email", currentUser.getEmail());
			f.setValue("Address", currentUser.getAddress());
			f.setValue("Phone", currentUser.getPhone());
			f.setValue("Student", String.valueOf(currentUser.getStudent()));
		}

		ctx.put("form", f);
	}

	public Set<String> defaultGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}
	
	public boolean secureAccessRequired()
	{
		return true;
	}

	public String displayName()
	{
		return "Pay Membership Dues";
	}

	public int weight()
	{
		return -90;
	}
}
