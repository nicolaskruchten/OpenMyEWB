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

package ca.myewb.controllers.events;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.forms.ConferenceRegistrationForm;
import ca.myewb.model.ConferenceRegistrationModel;


public class Conference extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		GregorianCalendar cal = new GregorianCalendar();
		cal.set(Calendar.MONTH, 9);
		cal.set(Calendar.DAY_OF_MONTH, 16);
		cal.set(Calendar.YEAR, 2007);
		cal.set(Calendar.HOUR_OF_DAY, 20);
		cal.set(Calendar.MINUTE, 0);
		
		ConferenceRegistrationModel registration = currentUser.getRegistration();
		
		GregorianCalendar now = new GregorianCalendar();
		now.setTime(new Date());
		
		if(cal.after(now))
		{
			ctx.put("mode", "closed");
		}
		else if(registration != null)
		{
			ctx.put("mode", "registered");
		}
		else
		{
			ctx.put("mode", "unregistered");
			
			ConferenceRegistrationForm theForm = (ConferenceRegistrationForm)checkForValidationFailure(ctx);
			
			
			ctx.put("needsToRenew", ConferenceRegistrationModel.needsToRenew(currentUser));

			
			if (theForm == null)
			{
				// Load up a fresh form object
				theForm = new ConferenceRegistrationForm(path + "/actions/SaveConferenceRegistration",
				                          requestParams, currentUser.getCanadianinfo() == 'n');
				theForm.setValue("Address", currentUser.getAddress());
				theForm.setValue("Email", currentUser.getEmail());
				theForm.setValue("Gender", String.valueOf(currentUser.getGender()));
				theForm.setValue("Phone", currentUser.getPhone());
				theForm.setValue("Language", currentUser.getLanguage());
				theForm.setValue("Student", String.valueOf(currentUser.getStudent()));
				theForm.setValue("Food", "none");
			}

			ctx.put("form", theForm);

		}
	}

    public Set<String> defaultGroups()
    {
            Set<String> s = new HashSet<String>();
            s.add("Users");

            return s;
    }

	public String displayName()
	{
		return "Conference Registration";
	}
	
	public int weight()
	{
		return -1000;
	}
	
	public boolean secureAccessRequired()
	{
		return true;
	}
}
