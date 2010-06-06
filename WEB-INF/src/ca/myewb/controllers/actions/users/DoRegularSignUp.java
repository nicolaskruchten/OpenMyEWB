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

package ca.myewb.controllers.actions.users;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.CreditCardTransaction;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.Message;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.PayDuesForm;
import ca.myewb.model.EmailModel;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.UserModel;

public class DoRegularSignUp extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		if (!currentUser.getUsername().equals("guest"))
		{
			throw new RedirectionException(path + "/profile/PayDues");
		}
		
		// Create and validate form object
		PayDuesForm	register = new PayDuesForm(path + "/actions/DoRegularSignup", requestParams, false, true);
		Message m = register.validate();

		if (m != null)
		{
			// Display error and prompt user to fix
			throw getValidationException(register, m, path + "/profile/RegularSignUp");
		}

		String email = register.getParameter("Email");
		boolean newUser = false;
		UserModel u = UserModel.getUserForEmail(email);
		if (u != null && !u.getUsername().equals(""))
		{
			if((u.getExpiry() != null) && !u.canRenew())
			{
				setSessionErrorMessage("The account with that email address is already listed a regular member " +
						"and this membership does not expire within the next 30 days.");
				throw new RedirectionException(path + "/profile/RegularSignUp");
			}
		}
		else
		{
			String firstname = register.getParameter("Firstname");
			String lastname = register.getParameter("Lastname");
			String password = UserModel.generateRandomPassword();


			u = UserModel.newAssociateSignUp(u, email, firstname, lastname, password);
			u.setCanadianinfo('y');
			
			// Send them an email with user/pass
			VelocityContext mailCtx = new VelocityContext();
			mailCtx.put("username", u.getUsername());
			mailCtx.put("password", password);
			mailCtx.put("helpers", new Helpers());
			mailCtx.put("name", u.getFirstname() + " " + u.getLastname());

			Template template = Velocity.getTemplate("emails/signup.vm");
			StringWriter writer = new StringWriter();
			template.merge(mailCtx, writer);

			EmailModel.sendEmail(u.getEmail(), writer.toString());

			newUser = true;
		}
		
		String phone = register.getParameter("Phone");
		String student = register.getParameter("Student");
		String chapterSelect = register.getParameter("Chapter");
		u.saveUpgradeData(u.getFirstname(), u.getLastname(), u.getEmail(), register.getParameter("Address"), phone, student);


		
		
		String chapterText;
		String sku = "";
		
		if((chapterSelect == null) || (chapterSelect.equals("")))
		{
			chapterText = "no chapter";
		}
		else
		{			
			int chapID = Integer.parseInt(chapterSelect);
			GroupChapterModel chapter = (GroupChapterModel)HibernateUtil.currentSession().load(GroupChapterModel.class, chapID);
			chapterText = chapter.getShortname();
			sku = chapter.getShortname();
		}

		CreditCardTransaction trn = new CreditCardTransaction();
		trn.setContactInfo(u);
		trn.setCardInfo(u.getFirstname() + " " + u.getLastname(), 
				register.getParameter("Creditcardnumber"), 
				register.getParameter("Creditcardexpirydate").substring(0, 2), 
				register.getParameter("Creditcardexpirydate").substring(2));

		if(student.charAt(0) == 'y')
		{
			trn.addItem("studues" + sku, 1, 20, "Student membership (" + chapterText + ")");
		}
		else
		{
			trn.addItem("produes" + sku, 1, 40, "Non-student membership (" + chapterText + ")");
		}

		trn.attemptTransaction("dues");
		
		if(trn.isSucceeded())
		{
			trn.sendReceipt("Membership Dues");
			u.renew(u, true);
			if(newUser)
			{
				setSessionMessage("Welcome to " + Helpers.getLongName() + ", " + u.getFirstname() + " " + u.getLastname() + "!" +
					"<br />An email containing your password has been sent to you so" +
					" that you will be able to sign in to this system and connect with other users.");
			}
			else
			{
				setSessionMessage("Transaction successfully processed.");
			}
			setInterpageVar("receipt", trn);
			//they get dropped onto PaymentResult after this next bit
		}
		else
		{
			setSessionErrorMessage("Transaction NOT successfully processed: " +  trn.getOutput().get("messageText"));
			throw new RedirectionException(path+ "/profile/RegularSignUp");	
			//ugly: they'll have to retype everything in case of declined :(
		}
		

		//do this stuff here only if the trn succeeds
		
		if(chapterSelect != null && !chapterSelect.equals(""))
		{
			int chapID = Integer.parseInt(chapterSelect);
			GroupChapterModel chapter = (GroupChapterModel)HibernateUtil.currentSession().load(GroupChapterModel.class, chapID);
			
			if(u.getChapter() != chapter)
			{
				u.leaveChapter(u.getChapter());
				if(u.joinChapter(chapter) && chapter.getWelcomeMessage() != null)
				{
					EmailModel.sendEmail(chapter.getEmail(), u.getEmail(), chapter.getFullWelcomeEmail());
				}
			}
		}

		throw new RedirectionException(path + "/profile/PaymentResult");
	}


	public boolean secureAccessRequired()
	{
		return true;
	}
	
	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}
	
	public String oldName()
	{
		return "DoRegularSignUp";
	}
}
