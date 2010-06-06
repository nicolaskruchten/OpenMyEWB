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

package ca.myewb.controllers.actions.conference;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.CreditCardTransaction;
import ca.myewb.frame.ErrorMessage;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Message;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.ConferenceRegistrationForm;
import ca.myewb.model.ConferenceRegistrationModel;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.UserModel;


public class SaveConferenceRegistration extends Controller
{
	public void handle(Context ctx) throws Exception
	{

		GroupChapterModel chapter = currentUser.getChapter();

		// Create & validate form object
		ConferenceRegistrationForm form = new ConferenceRegistrationForm(path + "/actions/SaveConferenceRegistration",
		                                      requestParams, currentUser.getCanadianinfo() == 'n');

		Message m = form.validate();
		
		//check email update
		
		String email = form.getParameter("Email");
		UserModel userForEmail = UserModel.getUserForEmail(email);
		if (!isOnConfirmLeg() && (m==null) && (userForEmail != null) && (!userForEmail.equals(currentUser)))
		{
			if( userForEmail.getUsername().equals("") )
			{
				currentUser.mergeRolesWithMailAccount(userForEmail);
			}
			else
			{
				m = new ErrorMessage("That email address is already linked to another account.  <br/>" +
					"The system administrator has been notified and will contact you shortly to resolve the situation.");
				form.setError("Email", "Please use a different email");
	
				log.warn(currentUser.getUsername() + " (" + currentUser.getEmail() + ") failed email change: " 
						+ email + " is already in use by " + userForEmail.getUsername());
			}
		}
		

		if (!((m == null) || (isOnConfirmLeg())))
		{
			// Display error and prompt user to fix
			throw getValidationException(form, m,
			                             path + "/events/Conference");
		}

		String confirmText;
		if(!isOnConfirmLeg())
		{
			VelocityContext confirmCtx = new VelocityContext();
			confirmCtx.put("helpers", new Helpers());
			confirmCtx.put("africafund", form.getParameter("AfricaFund").equals("y"));
			confirmCtx.put("name", ConferenceRegistrationModel.getName(form.getSelectedType()));
			confirmCtx.put("cost", ConferenceRegistrationModel.getCost(form.getSelectedType()));
			if(ConferenceRegistrationModel.needsToRenew(currentUser))
			{
				confirmCtx.put("regularfee", form.getParameter("Student").equals("y") ? 20 : 40);
			}
			else
			{
				confirmCtx.put("regularfee", 0);
			}
			
			Template template = Velocity.getTemplate("confirmations/conferenceregistration.vm");
			StringWriter writer = new StringWriter();
			template.merge(confirmCtx, writer);
			confirmText = writer.toString();
		}
		else
		{
			confirmText = "";
		}
		
		requireConfirmation("Confirm: pay for the following?",
		                    confirmText,
		                    path + "/events/Conference",
		                    path + "/actions/SaveConferenceRegistration",
		                    "events", form, false);
		
		form = new ConferenceRegistrationForm(path + "/actions/SaveConferenceRegistration",
                requestParams, currentUser.getCanadianinfo() == 'n');
		
		//update various parameters here
		
		currentUser.setGender(form.getParameter("Gender").charAt(0));
		currentUser.setLanguage(form.getParameter("Language"));
		currentUser.saveUpgradeData(currentUser.getFirstname(), currentUser.getLastname(), 
				email, 
				form.getParameter("Address"), form.getParameter("Phone"), form.getParameter("Student"));
		

		
		
		//set up transaction

		String selectedType = form.getSelectedType();
		
		CreditCardTransaction trn = new CreditCardTransaction();
		trn.setContactInfo(currentUser);
		trn.setCardInfo(currentUser.getFirstname() + " " + currentUser.getLastname(), 
				form.getParameter("Creditcardnumber"), 
				form.getParameter("Creditcardexpirydate").substring(0, 2), 
				form.getParameter("Creditcardexpirydate").substring(2));
		
		trn.addItem(form.getSelectedType(), 1, ConferenceRegistrationModel.getCost(selectedType), 
				ConferenceRegistrationModel.getName(selectedType));
		
		if(ConferenceRegistrationModel.needsToRenew(currentUser))
		{
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
		}
		
		boolean africafund = false;
		
		if(form.getParameter("AfricaFund").equals("y"))
		{
			africafund = true;
			trn.addItem("09-africafund", 1, 20, "$20 to support an African Delegate");
		}
		
		trn.attemptTransaction("confreg");
		
		//wrap up
		
		if(trn.isSucceeded())
		{
			trn.sendReceipt("Conference Registration");
			int prevConfs = new Integer(form.getParameter("PrevConfs"));
			int prevRetreats = new Integer(form.getParameter("PrevRetreats"));
			boolean headset = form.getParameter("Headset").equals("y");
			String foodPrefs = form.getParameter("Food");
			String emergName = form.getParameter("EmergName");
			String emergPhone = form.getParameter("EmergPhone");
			String specialNeeds = form.getParameter("Needs");
			String code = form.getParameter("Code");
			
			ConferenceRegistrationModel.newRegistration(currentUser, selectedType,
					prevConfs, prevRetreats, headset, foodPrefs, emergName, emergPhone, 
					specialNeeds, code, trn.getOrderNumber(), africafund);
			
			if(ConferenceRegistrationModel.needsToRenew(currentUser))
			{
				currentUser.renew(currentUser, true);
			}
			setSessionMessage("Transaction successfully processed.");
		}
		else
		{
			setSessionErrorMessage("Transaction NOT successfully processed: " + trn.getOutput().get("messageText"));
		}

		throw new RedirectionException(path + "/events/Conference");
	}

	public Set<String> invisibleGroups()
	{
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
		return "SaveConferenceRegistration";
	}
}
