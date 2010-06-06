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
import ca.myewb.frame.forms.element.Checkbox;
import ca.myewb.frame.forms.element.Dropdown;
import ca.myewb.frame.forms.element.Element;
import ca.myewb.frame.forms.element.Radio;
import ca.myewb.frame.forms.element.Text;
import ca.myewb.frame.forms.element.TextArea;

public class CDayRegistrationForm extends Form {

	public CDayRegistrationForm(String target, PostParamWrapper requestParams) 
	{
		super(target, "preview order");
		
		addText("Name", "Name", requestParams.get("Name"), true)
		.setInstructions("this will appear as-is on your name-tag");
		
		addText("Affiliation", "Affiliation/Company", requestParams.get("Affiliation"), false)
		.setInstructions("this will appear as-is on your name-tag");
		
		Dropdown d3 = addDropdown("Sector", "Sector?", requestParams.get("Sector"), false);
		d3.addOption("Oil and gas", "Oil and gas");
		d3.addOption("EPC", "EPC");
		d3.addOption("Consulting", "Consulting");
		d3.addOption("Environmental", "Environmental");
		d3.addOption("NGO", "NGO");
		d3.addOption("Government", "Government");
		d3.addOption("Hi Tech", "Hi Tech");
		d3.addOption("Manufacturing", "Manufacturing");
		d3.addOption("Transportation", "Transportation");
		d3.addOption("Construction", "Construction");
		d3.addOption("Other", "Other");
		d3.setInstructions("optional information to help us tailor the day's content to our audience");
		
		Dropdown d4 = addDropdown("Exp", "Years of Experience?", requestParams.get("Exp"), false);
		d4.addOption("0-5", "0-5");
		d4.addOption("6-10", "6-10");
		d4.addOption("11-15", "11-15");
		d4.addOption("16-20", "16-20");
		d4.addOption("20+", "20+");
		d4.setInstructions("optional information to help us tailor the day's content to our audience");
		
		Radio pc = addRadio("ProChapter", "Professional Chapter", requestParams.get("ProChapter"), true);
		pc.addOption("member", "I am currently affiliated with a professional chapter.");
		pc.addOption("info", "I would like to receive more information about a professional chapter in my registration package.");
		pc.addOption("none", "I am not affiliated with a professional chapter, and do not wish to receive information about one.");

		Checkbox cbox = addCheckbox("ChapterDinner", "Chapter Dinner?", requestParams.get("ChapterDinner"), 
		"I would like to join a professional chapter for an informal dinner following Collaboration Day");
		cbox.setInstructions("(dinner will be at a restaurant local to the Delta Meadowvale from approximately 6:30-8pm and will be a great opportunity to learn more about our organization and connect with our Pro Chapter members. Further details will be provided in the registration package)");

		Dropdown d5 = addDropdown("ProChapterName", "Which Professional Chapter?", requestParams.get("ProChapterName"), false);
		d5.addOption("Calgary", "Calgary");
		d5.addOption("Montreal", "Montreal");
		d5.addOption("Ottawa", "Ottawa");
		d5.addOption("Saskatoon", "Saskatoon");
		d5.addOption("Toronto", "Toronto");
		d5.addOption("Vancouver", "Vancouver");
		d5.addOption("Waterloo", "Waterloo");
		d5.setInstructions("If you belong to a professional chapter, would like to receive information about one, and/or would like to join a chapter for dinner, please indicate the chapter here.");
		
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


		Dropdown d2 = addDropdown("NumTickets", "Number of Gala Tickets", requestParams.get("NumTickets"), true);
		d2.setInstructions("tickets to the Gala Banquet on Sat, Jan 24 are $250 each<br/>(not included in a Collaboration Day registration)<br/>we will mail you a charitable tax receipt for $125 per ticket.");
		d2.addOption("0", "0");
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
		
		addTextArea("Heardabout", "How did you hear about Collaboration Day?", requestParams.get("Heardabout"), false);

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
