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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.ApplicationRejectionEmailStorageForm;
import ca.myewb.model.ApplicationModel;
import ca.myewb.model.ApplicationSessionModel;
import ca.myewb.model.EmailModel;


public class SendApplicationRejectionEmails extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		ApplicationSessionModel s = (ApplicationSessionModel)getAndCheckFromUrl(ApplicationSessionModel.class);
		HashSet<ApplicationModel> rejects = new HashSet<ApplicationModel>();
		StringBuffer confirmMessage = new StringBuffer();
		confirmMessage.append("Confirm: send rejection emails this to the following users?<br/>");
		ArrayList<String> emails = new ArrayList<String>();
		ApplicationRejectionEmailStorageForm form = new ApplicationRejectionEmailStorageForm("", requestParams);
		
		for(ApplicationModel app : s.getApplications())
		{
			if(app.getEvaluation() != null && !app.getEvaluation().isRejectionSent())
			{
				if(requestParams.get("app-" + app.getId()) != null)
				{
					rejects.add(app);
					confirmMessage.append(app.getUser().getFirstname() + " " + app.getUser().getLastname() + "<br/>");
					emails.add(app.getUser().getEmail());
					form.addHidden("app-" + app.getId(), requestParams.get("app-" + app.getId()), false);
				}
			}
		}
		
		if(rejects.size() == 0 && !isOnConfirmLeg())
		{
			setSessionErrorMessage(("No Application was selected."));
			throw new RedirectionException(path + "/volunteering/ApplicationList/name/asc/" + s.getId());
		}
		
		requireConfirmation(confirmMessage.toString(), "This operation cannot be undone!",
		                    path + "/volunteering/ApplicationList/name/asc/" + s.getId(),
		                    path + "/actions/SendApplicationRejectionEmails/" + s.getId(),
		                    "volunteering", form);
		
		String subject = "Application Status";
		EmailModel.sendEmail(Helpers.getSystemEmail(), 
					emails, 
					"[" + Helpers.getEnShortName() + "applications] " + subject, 
					s.getRejectionEmailText(), 
					Helpers.getEnShortName() + "-applications");

		for(ApplicationModel app : rejects)
		{
			app.getEvaluation().rejectApplication();
		}
		
		// Leave a message in the session
		setSessionMessage(("Rejection Emails Sent!"));
		throw new RedirectionException(path + "/volunteering/ApplicationList/name/asc/" + s.getId());
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Admin");

		return s;
	}
	
	public String oldName()
	{
		return "SendApplicationRejectionEmails";
	}
}
