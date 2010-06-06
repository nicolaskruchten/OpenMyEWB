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

package ca.myewb.frame.forms;

import ca.myewb.frame.ConferenceCode;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.CreditCardTransaction;
import ca.myewb.frame.forms.element.Dropdown;
import ca.myewb.frame.forms.element.Element;
import ca.myewb.frame.forms.element.Radio;
import ca.myewb.frame.forms.element.Text;
import ca.myewb.frame.forms.element.TextArea;

public class ConferenceRegistrationForm extends Form {

	private boolean isIntl;

	public ConferenceRegistrationForm(String target, PostParamWrapper requestParams, boolean isIntl) 
	{
		super(target, "preview order");

		this.isIntl = isIntl;
		addText("Email", "Primary email address", requestParams.get("Email"), true);

		if (isIntl) 
		{
			Element e = addIntlAddress("Address", "Mailing Address", requestParams.getArray("Address"), true);
			e.setInstructions("<a href=\"" + Helpers.getAppPrefix() + "/profile/EditProfile\">Click here</a> to change your profile settings if you are in North America");
			addText("Phone", "Main Phone number", requestParams.get("Phone"), true);
		} 
		else 
		{
			Element e = addAddress("Address", "Mailing Address", requestParams.getArray("Address"), true);
			e.setInstructions("<a href=\"" + Helpers.getAppPrefix() + "/profile/EditProfile\">Click here</a> to change your profile settings if you are not in North America");
			addPhone("Phone", "Main Phone number", requestParams.getArray("Phone"), true);
		}

		Radio g = addRadio("Gender", "Gender", requestParams.get("Gender"), true);
		g.addOption("m", "Male");
		g.addOption("f", "Female");
		g.setNumAcross(2);
		
		Radio s = addRadio("Student", "Student status", requestParams.get("Student"), true);
		s.addOption("y", "Student");
		s.addOption("n", "Non-student");
		s.setNumAcross(2);
		

		Radio l = addRadio("Language", "Preferred Language", requestParams.get("Language"), true);
		l.addOption("en", "English");
		l.addOption("fr", "French");
		l.setNumAcross(2);
		

		Radio h = addRadio("Headset", "Headset requested?", requestParams.get("Headset"), true);
		h.addOption("y", "yes");
		h.addOption("n", "no");
		h.setNumAcross(2);
		h.setInstructions("Would you use a simultaneous-translation headset for keynote speakers not in your preferred language, if headsets were available?");
		
		Radio food = addRadio("Food", "Food preferences", requestParams.get("Food"), false);
		food.addOption("none", "no special preferences");
		food.addOption("vegetarian", "vegetarian");
		food.addOption("vegan", "vegan");
		food.setNumAcross(3);
		food.setInstructions("Please use the text area below to provide details or any other requirements, if needed");
		
		TextArea ta = addTextArea("Needs", "Special Needs", requestParams.get("Needs"), false);
		ta.setInstructions("Please let us know about any special dietary, accessibility or other needs you may have.");

		
		addText("EmergName", "Emergency Contact Name", requestParams.get("EmergName"), true);
		addPhone("EmergPhone", "Emergency Contact Phone", requestParams.getArray("EmergPhone"), true);

		Dropdown d1 = addDropdown("PrevConfs", "Previous Conferences attended", requestParams.get("PrevConfs"), true);
		d1.setInstructions("How many previous National Conference have you attended?");
		d1.addOption("0", "0");
		d1.addOption("1", "1");
		d1.addOption("2", "2");
		d1.addOption("3", "3");
		d1.addOption("4", "4");
		d1.addOption("5", "5");
		d1.addOption("6", "6");
		d1.addOption("7", "7");

		Dropdown d2 = addDropdown("PrevRetreats", "Previous Retreats attended", requestParams.get("PrevRetreats"), true);
		d2.setInstructions("How many previous Regional Retreats have you attended?");
		d2.addOption("0", "0");
		d2.addOption("1", "1");
		d2.addOption("2", "2");
		d2.addOption("3", "3");
		d2.addOption("4", "4");
		d2.addOption("5", "5");
		d2.addOption("6", "6");
		d2.addOption("7", "7");
		
		Radio rs = addRadio("RoomSize", "Registration Type", requestParams.get("RoomSize"), true);
		
		rs.addOption("0", "No-Hotel Registration (Students: $400, Non-Students: $450)");
		rs.addOption("4", "Quad-Room Registration (Students: $450, Non-Students: $525)");
		rs.addOption("2", "Double-Room Registration (Students: $625, Non-Students: $675)");
		rs.addOption("1", "Single-Room Registration (Students: $850, Non-Students: $900)");
		
		rs.setInstructions("<b>Note:</b> costs listed are for registration without a coupon code. <br />Hotel options cover 4 nights: arrival on Wednesday, Jan 21, departure on Sunday, Jan 25.");

		addText("Code", "Coupon Code", requestParams.get("Code"), false)
		.setInstructions("please enter the coupon code sent to you by your chapter president if you have one");

		Radio h2 = addRadio("AfricaFund", "Support an African Delegate?", requestParams.get("AfricaFund"), true);
		h2.addOption("y", "yes");
		h2.addOption("n", "no");
		h2.setNumAcross(2);
		h2.setInstructions("select 'yes' to contribute an additional $20 (non-refundable) towards a fund to bring young African leaders to the conference as delegates.");

		
		Dropdown d = addDropdown("Creditcardtype", "Credit Card Type", requestParams.get("Creditcardtype"), true);
		d.addOption("visa", "Visa");
		d.addOption("mc", "MasterCard");
		d.addOption("amex", "American Express");
		addText("Creditcardnumber", "Credit Card Number", requestParams.get("Creditcardnumber"), true);
		Text t = addText("Creditcardexpirydate", "Credit Card Expiry Date", requestParams.get("Creditcardexpirydate"), true);
		t.setSize(50);
		t.setInstructions("Please enter the date as MMYY");
		
	}

	public String getSelectedType() throws Exception
	{
		String result = "09-";
		
		if(getParameter("Student").equals("y"))
		{
			result += "stu";
		}
		else
		{
			result += "pro";
		}
		
		
		result += "reg" + getParameter("RoomSize");

		ConferenceCode code = new ConferenceCode(getElement("Code").getValue());
		if(code.isValid() && getParameter("Student").equals("y"))
		{
			//here is where you can manipulate the registration type based on a valid code
			if(code.getType().equals("subsidized"))
			{
				result += "-sub";
			}
			
			if(code.getType().equals("on/qc subsidized"))
			{
				result += "-onqcsub";
			}

			if(code.getType().equals("gta subsidized"))
			{
				result += "-gtasub";
			}
		}
		
		return result.intern();
	}
	
	public boolean cleanAndValidate(boolean isClean) 
	{
		Element codeElement = getElement("Code");
		if(!codeElement.getValue().equals(""))
		{
			if(!(new ConferenceCode(codeElement.getValue())).isValid())
			{
				isClean = false;
				codeElement.highlight();
				codeElement.setError("this code is either invalid or has already been used");
			}

			if (getElement("Student").getValue().equals("n"))
			{
				isClean = false;
				codeElement.highlight();
				codeElement.setError("coupon codes can only be used by students");
			}
		}
			
		isClean = (getElement("Creditcardnumber")).ensureNumeric() && isClean;
		isClean = (getElement("Creditcardexpirydate")).ensureNumeric() && isClean;

		isClean = checkCard(getElement("Creditcardnumber"), getElement("Creditcardtype"), getElement("Creditcardexpirydate")) && isClean;

		return isClean;
	}

	private boolean checkCard(Element cardNumEl, Element cardTypeEl, Element cardExpEl) 
	{
		String cardTypeError = CreditCardTransaction.checkCardType(cardTypeEl.getValue(), cardNumEl.getValue());
		if(cardTypeError != null)
		{
			cardTypeEl.setError(cardTypeError);
			cardTypeEl.highlight();
			cardNumEl.setError(cardTypeError);
			cardNumEl.highlight();
			return false;
		}

		String expiryError = CreditCardTransaction.checkExpiry(cardExpEl.getValue());
		if(expiryError != null)
		{
			cardExpEl.setError(expiryError);
			cardExpEl.highlight();
			return false;
		}
		
		if(!CreditCardTransaction.checkCardNo(cardNumEl.getValue()))
		{
			cardNumEl.setError("This is not a valid credit card number");
			cardNumEl.highlight();
			return false;
		}

		return true;

	}


}
