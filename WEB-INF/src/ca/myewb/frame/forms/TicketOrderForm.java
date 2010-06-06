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

public class TicketOrderForm extends Form {

	public TicketOrderForm(String target, PostParamWrapper requestParams) 
	{
		super(target, "preview order");

		addText("Name", "Name", requestParams.get("Name"), true);
		
		addText("Email", "Primary email address", requestParams.get("Email"), true);
		addCheckbox("SignUp", "Sign up to email list?", requestParams.get("SignUp"), 
		"(check this box to receive monthly email updates)");

		addAddress("Address", "Mailing Address", requestParams.getArray("Address"), true);

		addPhone("Phone", "Main Phone number", requestParams.getArray("Phone"), true);

		Radio l = addRadio("Language", "Preferred Language", requestParams.get("Language"), true);
		l.addOption("en", "English");
		l.addOption("fr", "French");
		l.setNumAcross(2);
		

		TextArea ta = addTextArea("Needs", "Special Needs", requestParams.get("Needs"), false);
		ta.setInstructions("Please let us know about any special dietary, accessibility or other needs you may have.");


		Dropdown d2 = addDropdown("NumTickets", "Number of Gala Tickets", requestParams.get("NumTickets"), true);
		d2.setInstructions("tickets to the Gala Banquet on Sat, Jan 24 are $250 each<br />we will mail you a charitable tax receipt for $125 per ticket.");
		d2.addOption("1", "1");
		d2.addOption("2", "2");
		d2.addOption("3", "3");
		d2.addOption("4", "4");
		d2.addOption("5", "5");
		d2.addOption("6", "6");
		
		
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
