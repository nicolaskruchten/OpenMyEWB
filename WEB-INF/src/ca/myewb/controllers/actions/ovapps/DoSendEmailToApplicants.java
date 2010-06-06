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
import java.util.List;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Message;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.ApplicantEmailEditForm;
import ca.myewb.model.ApplicationSessionModel;
import ca.myewb.model.EmailModel;


public class DoSendEmailToApplicants extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		ApplicationSessionModel session = (ApplicationSessionModel)getAndCheckFromUrl(ApplicationSessionModel.class);

		// Create & validate form object
		ApplicantEmailEditForm form = new ApplicantEmailEditForm(path + "/actions/DoSendEmailToApplicants/"
		                                       + session.getId(), requestParams);

		Message m = form.validate();

		if (!((m == null) || (isOnConfirmLeg())))
		{
			// Display error and prompt user to fix
			throw getValidationException(form, m,
			                             path + "/mailing/SendEmailToApplicants/"
			                             + session.getId());
		}

		requireConfirmation("Confirm: email this to " + (form.getParameter("Sendtorejects").equals("yes") ? "all applicants including rejected applicants" : "current open applicats") + " of " + session.getName() +
							" application session as-is?",
		                    "<h3>Email Preview</h3><p class=\"postbody\">"
		                    + Helpers.wikiFormat(form.getParameter("Body"))
		                    + "</p>",
		                    path + "/mailing/SendEmailToApplicants/" + session.getId(),
		                    path + "/actions/DoSendEmailToApplicants/" + session.getId(),
		                    "volunteering", form);

		form = new ApplicantEmailEditForm(path + "/actions/DoSendEmailToApplicants/" + session.getId(),
		                         requestParams);
		

		String sender = "\"" + currentUser.getFirstname() + " "
         + currentUser.getLastname() + "\" <"
         + currentUser.getEmail() + ">";

		if(form.getParameter("Sender").equals("system"))
		{	
			sender = Helpers.getSystemEmail();
		}
		
		String body = form.getParameter("Body");
		String subject = form.getParameter("Subject");
		
		List<String> to = session.getApplicantEmails(form.getParameter("Sendtorejects").equals("on"));
		
		EmailModel.sendEmail(sender, to, "[" + Helpers.getEnShortName() + "-applications] " + subject, body, Helpers.getEnShortName() + "-applications");

		// Leave a message in the session
		setSessionMessage(("Email sent!"));
		throw new RedirectionException(path + "/volunteering/ApplicationList/name/asc/" + session.getId());
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Admin");

		return s;
	}
	
	public String oldName()
	{
		return "DoSendEmailToApplicants";
	}
}
