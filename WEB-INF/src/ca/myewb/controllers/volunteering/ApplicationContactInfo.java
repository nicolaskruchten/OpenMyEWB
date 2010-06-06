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
import ca.myewb.frame.forms.ApplicationContactInfoForm;
import ca.myewb.model.ApplicationModel;
import ca.myewb.model.ApplicationSessionModel;

public class ApplicationContactInfo extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		ApplicationContactInfoForm form = (ApplicationContactInfoForm)checkForValidationFailure(ctx);
		ApplicationSessionModel session = (ApplicationSessionModel)getAndCheckFromUrl(ApplicationSessionModel.class);

		if (form == null)
		{
			// Load up a fresh form object
			form = new ApplicationContactInfoForm(path
					+ "/actions/SaveApplicationContactInfo/" + session.getId(), requestParams);
			form.setValue("Firstname", currentUser.getFirstname());
			form.setValue("Lastname", currentUser.getLastname());
			form.setValue("Email", currentUser.getEmail());
			form.setValue("Phone", currentUser.getPhone());

			ApplicationModel theApp = currentUser.getAppForSession(session);
			if (theApp == null)
			{
				theApp = new ApplicationModel();
			}

			form.setValue("En1", new Integer(theApp.getEnglishWriting()).toString());
			form.setValue("En2", new Integer(theApp.getEnglishReading()).toString());
			form.setValue("En3", new Integer(theApp.getEnglishSpeaking()).toString());

			form.setValue("Fr1", new Integer(theApp.getFrenchWriting()).toString());
			form.setValue("Fr2", new Integer(theApp.getFrenchReading()).toString());
			form.setValue("Fr3", new Integer(theApp.getFrenchSpeaking()).toString());

			form.setValue("Schooling", theApp.getSchooling());
			form.setValue("GPA", Float.toString(theApp.getGPA()));
			form.setValue("References", theApp.getRefs());
			form.setValue("Resume", theApp.getResume());

		}

		ctx.put("form", form);
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();

		s.add("Users");

		return s;
	}

	public String displayName()
	{
		return "Application Details";
	}
}