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
import ca.myewb.frame.forms.ApplicantEmailEditForm;
import ca.myewb.model.ApplicationSessionModel;


public class SendEmailToApplicants extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		ApplicationSessionModel session = (ApplicationSessionModel)getAndCheckFromUrl(ApplicationSessionModel.class);

		ctx.put("session", session);

		ApplicantEmailEditForm form = (ApplicantEmailEditForm)checkForValidationFailure(ctx);

		if (form == null)
		{
			// First try: create a fresh form
			form = new ApplicantEmailEditForm(path + "/actions/DoSendEmailToApplicants/"
			                         + session.getId(), requestParams);
		}

		ctx.put("form", form);
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Admin");

		return s;
	}

	public String displayName()
	{
		return "Send an Email to Applicants";
	}

	public int weight()
	{
		return -1;
	}
}
