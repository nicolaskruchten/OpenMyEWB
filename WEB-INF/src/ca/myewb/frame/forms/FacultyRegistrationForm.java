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

import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.CreditCardTransaction;
import ca.myewb.frame.forms.element.Dropdown;
import ca.myewb.frame.forms.element.Element;
import ca.myewb.frame.forms.element.Radio;
import ca.myewb.frame.forms.element.Text;
import ca.myewb.frame.forms.element.TextArea;

public class FacultyRegistrationForm extends Form {

	public FacultyRegistrationForm(String target, PostParamWrapper requestParams) 
	{
		super(target, "preview order");
		
		addText("Name", "Name", requestParams.get("Name"), true)
		.setInstructions("this will appear as-is on your name-tag");
		
		addText("Affiliation", "Affiliation", requestParams.get("Affiliation"), false)
		.setInstructions("this will appear as-is on your name-tag");
		
		addText("Email", "Primary email address", requestParams.get("Email"), true);
		addCheckbox("SignUp", "Sign up to email list?", requestParams.get("SignUp"), 
		"(check this box to receive monthly email updates)");
		addAddress("Address", "Mailing Address", requestParams.getArray("Address"), true);

		addPhone("Phone", "Main Phone number", requestParams.getArray("Phone"), true);

		Radio l = addRadio("Language", "Preferred Language", requestParams.get("Language"), true);
		l.addOption("en", "English");
		l.addOption("fr", "French");
		l.setNumAcross(2);
		

		Radio h = addRadio("Headset", "Headset required?", requestParams.get("Headset"), true);
		h.addOption("y", "yes");
		h.addOption("n", "no");
		h.setNumAcross(2);
		h.setInstructions("Would you like a simultaneous-translation headset to listen to keynotes which are not in your preferred language?");
		
		
		TextArea ta = addTextArea("Needs", "Special Needs", requestParams.get("Needs"), false);
		ta.setInstructions("Please let us know about any special dietary, accessibility or other needs you may have.");



		addCheckbox("HotelWed", "Hotel Wednesday?", requestParams.get("HotelWed"), 
		"(check this box to pay for a hotel room for Wed, Jan 21)")
		.setInstructions("cost is $150");
		
		addCheckbox("HotelThu", "Hotel Thursday?", requestParams.get("HotelThu"), 
		"(check this box to pay for a hotel room for Thu, Jan 22)")
		.setInstructions("cost is $150");
		
		addCheckbox("HotelFri", "Hotel Friday?", requestParams.get("HotelFri"), 
		"(check this box to pay for a hotel room for Fri, Jan 23)")
		.setInstructions("cost is $150");
		
		addCheckbox("HotelSat", "Hotel Saturday?", requestParams.get("HotelSat"), 
		"(check this box to pay for a hotel room for Sat, Jan 24)")
		.setInstructions("cost is $150");
		
		addCheckbox("BanquetTicket", "Gala Ticket?", requestParams.get("BanquetTicket"), 
		"(check this box to pay for a gala banquet ticket for the night of Sat, Jan 24)")
		.setInstructions("you may buy one ticket at the reduced price of $100 and we will mail you a $25 tax receipt");
		
		
		Dropdown d = addDropdown("Creditcardtype", "Credit Card Type", requestParams.get("Creditcardtype"), true);
		d.addOption("visa", "Visa");
		d.addOption("mc", "MasterCard");
		d.addOption("amex", "American Express");
		addText("Creditcardnumber", "Credit Card Number", requestParams.get("Creditcardnumber"), true);
		Text t = addText("Creditcardexpirydate", "Credit Card Expiry Date", requestParams.get("Creditcardexpirydate"), true);
		t.setSize(50);
		t.setInstructions("Please enter the date as MMYY");
		

	}

	public boolean cleanAndValidate(boolean isClean) 
	{

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
