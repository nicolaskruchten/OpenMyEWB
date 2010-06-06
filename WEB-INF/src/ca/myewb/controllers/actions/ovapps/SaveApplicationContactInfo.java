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

import ca.myewb.frame.Controller;
import ca.myewb.frame.ErrorMessage;
import ca.myewb.frame.Message;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.ApplicationContactInfoForm;
import ca.myewb.model.ApplicationModel;
import ca.myewb.model.ApplicationQuestionModel;
import ca.myewb.model.ApplicationSessionModel;
import ca.myewb.model.UserModel;


public class SaveApplicationContactInfo extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		ApplicationContactInfoForm form = new ApplicationContactInfoForm(path
		                                           + "/actions/SaveApplicationContactInfo/"
		                                           + urlParams.getParam(),
		                                           requestParams);

		Message m = form.validate();
		
		String email = form.getParameter("Email");
		UserModel userForEmail = UserModel.getUserForEmail(email);
		if((m == null) && (userForEmail != null) && (!userForEmail.equals(currentUser)))
		{
			if( userForEmail.getUsername().equals("") )
			{
				currentUser.mergeRolesWithMailAccount(userForEmail);
			}
			else
			{
				m = new ErrorMessage("That email address is already linked to another account.<br/>" +
					"The system administrator has been notified and will contact you shortly to resolve the situation.");
				form.setError("Email", "Please use a different email");
	
				log.warn(currentUser.getUsername() + " (" + currentUser.getEmail() + ") failed email change: " 
					+ email + " is already in use by " + userForEmail.getUsername());		
			}
		}
		
		

		// No messages: changes are valid
		if (m != null)
		{
				throw getValidationException(form, m,
				                             path + "/volunteering/ApplicationContactInfo/"
				                             + urlParams.getParam());
		}
		
		String firstname = form.getParameter("Firstname");
		String lastname = form.getParameter("Lastname");
		
		String phone = form.getParameter("Phone");
		int englishWriting = Integer.parseInt(form.getParameter("En1"));
		int englishReading = Integer.parseInt(form.getParameter("En2"));
		int englishSpeaking = Integer.parseInt(form.getParameter("En3"));
		int frenchWriting = Integer.parseInt(form.getParameter("Fr1"));
		int frenchReading = Integer.parseInt(form.getParameter("Fr2"));
		int frenchSpeaking = Integer.parseInt(form.getParameter("Fr3"));
		String schooling = form.getParameter("Schooling");
		String resume = form.getParameter("Resume");
		String references = form.getParameter("References");
		float gpa = Float.parseFloat(form.getParameter("GPA"));

		currentUser.saveApplicationData(firstname, lastname, email, phone);
		ApplicationSessionModel session = (ApplicationSessionModel)getAndCheckFromUrl(ApplicationSessionModel.class);
		ApplicationModel app = currentUser.getAppForSession(session);
		if (app == null)
		{
			app = currentUser.applyToSession(session);
		}
		
		app.save(englishWriting, englishReading, englishSpeaking, frenchWriting, frenchReading, frenchSpeaking, schooling, resume, references, gpa);
		
		setSessionMessage(("Contact Information Saved"));

		int sessionID = session.getId();
		
		ApplicationQuestionModel nextQ = session.getNextQuestion(null);
		if(nextQ == null)
		{
			throw new RedirectionException(path + "/volunteering/ApplicationFinished/" + sessionID);
		}
		else 
		{
			int questionID = nextQ.getId();
			throw new RedirectionException(path + "/volunteering/AnswerApplicationQuestion/" + sessionID + "/" + questionID);
		}
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}
	
	public String oldName()
	{
		return "SaveApplicationContactInfo";
	}
}
