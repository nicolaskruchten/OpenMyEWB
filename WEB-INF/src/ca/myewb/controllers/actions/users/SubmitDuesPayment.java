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

import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.CreditCardTransaction;
import ca.myewb.frame.ErrorMessage;
import ca.myewb.frame.Message;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.PayDuesForm;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.UserModel;

public class SubmitDuesPayment extends Controller 
{
	public void handle(Context ctx) throws Exception 
	{
		PayDuesForm form = new PayDuesForm(path + "/actions/SubmitDuesPayment",
				requestParams, currentUser.getCanadianinfo() == 'n', false);

		log.debug("Validating form");

		Message m = form.validate();

		String email = form.getParameter("Email");
		UserModel userForEmail = UserModel.getUserForEmail(email);
		if ((m==null) && (userForEmail != null) && (!userForEmail.equals(currentUser)))
		{
			if( userForEmail.getUsername().equals("") )
			{
				currentUser.mergeRolesWithMailAccount(userForEmail);
			}
			else
			{
				m = new ErrorMessage("That email address is already linked to another user account.  <br/>" +
					"The system administrator has been notified and will contact you shortly to resolve the situation.");
				form.setError("Email", "Please use a different email");
	
				log.warn(currentUser.getUsername() + " (" + currentUser.getEmail() + ") failed email change: " 
						+ email + " is already in use by " + userForEmail.getUsername());
			}
		}

		if (m != null) 
		{
			throw getValidationException(form, m, path + "/profile/PayDues/");
		}

		String firstname = form.getParameter("Firstname");
		String lastname = form.getParameter("Lastname");
		String phone = form.getParameter("Phone");
		String student = form.getParameter("Student");

		currentUser.saveUpgradeData(firstname, lastname, email,
				form.getParameter("Address"), phone, student);

		GroupChapterModel chapter = currentUser.getChapter();
		
		CreditCardTransaction trn = new CreditCardTransaction();
		trn.setContactInfo(currentUser);
		trn.setCardInfo(currentUser.getFirstname() + " " + currentUser.getLastname(), 
				form.getParameter("Creditcardnumber"), 
				form.getParameter("Creditcardexpirydate").substring(0, 2), 
				form.getParameter("Creditcardexpirydate").substring(2));

		String chaptername = "no chapter";
		String sku = "";
		if(chapter != null)
		{
			chaptername = chapter.getShortname();
			sku = chapter.getShortname();
		}
		
		if(currentUser.getStudent() == 'y')
		{
			trn.addItem("studues" + sku, 1, 20, "Student membership (" + chaptername + ")");
		}
		else
		{
			trn.addItem("produes" + sku, 1, 40, "Non-student membership (" + chaptername + ")");
		}

		trn.attemptTransaction("dues");
		
		if(trn.isSucceeded())
		{			
			trn.sendReceipt("Membership Dues");
			currentUser.renew(currentUser, true);
			setSessionMessage("Transaction successfully processed.");
			setInterpageVar("receipt", trn);
			throw new RedirectionException(path + "/profile/PaymentResult");
		}
		else
		{			
			setSessionErrorMessage("Transaction NOT successfully processed: " + trn.getOutput().get("messageText"));
			log.warn("User " + currentUser.getUsername() +" NOT upgraded! " + trn.getOutput().get("messageText"));
			throw new RedirectionException(path + "/profile/PayDues");
		}

		

	}

	public Set<String> invisibleGroups() {
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}

	public boolean secureAccessRequired()
	{
		return true;
	}
	
	public String oldName()
	{
		return "SubmitDuesPayment";
	}
}
