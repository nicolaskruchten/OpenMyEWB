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

import java.util.List;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.CreditCardTransaction;
import ca.myewb.frame.forms.element.Dropdown;
import ca.myewb.frame.forms.element.Element;
import ca.myewb.frame.forms.element.Radio;
import ca.myewb.frame.forms.element.Text;
import ca.myewb.model.GroupChapterModel;

public class PayDuesForm extends Form {

	private boolean isIntl;

	public PayDuesForm(String target, PostParamWrapper requestParams, boolean isIntl, boolean chapterChoice) 
	{

		super(target, "submit payment");

		this.isIntl = isIntl;

		addText("Firstname", "First name", requestParams.get("Firstname"), true);
		addText("Lastname", "Last name", requestParams.get("Lastname"), true);
		addText("Email", "Email address", requestParams.get("Email"), true);

		if (isIntl) 
		{
			Element e = addIntlAddress("Address", "Mailing Address", requestParams.getArray("Address"), true);
			if(!chapterChoice)
			{
				e.setInstructions("<a href=\"" + Helpers.getAppPrefix() + "/profile/EditProfile\">Click here</a> to change your profile settings if you are in North America");
			}
			addText("Phone", "Phone number", requestParams.get("Phone"), true);
		} 
		else 
		{
			Element e = addAddress("Address", "Mailing Address", requestParams.getArray("Address"), true);
			if(!chapterChoice)
			{	
				e.setInstructions("<a href=\"" + Helpers.getAppPrefix() + "/profile/EditProfile\">Click here</a> to change your profile settings if you are not in North America");
			}
			addPhone("Phone", "Phone number", requestParams.getArray("Phone"), true);
		}

		if(chapterChoice)
		{
			Dropdown chapter = addDropdown("Chapter", "Join a Chapter", requestParams.get("Chapter"), false);
			chapter.setInstructions("this is optional, but recommended if you want to attend meetings or get more involved with members in your area");
			List<GroupChapterModel> chapters = GroupChapterModel.getChapters();
			for(GroupChapterModel c : chapters)
			{
				chapter.addOption(Integer.toString(c.getId()), c.getName());
			}
		}
		
		Radio s = addRadio("Student", "Student status", requestParams.get("Student"), true);
		s.addOption("y", "Student ($20)");
		s.addOption("n", "Non-student ($40)");
		s.setNumAcross(2);

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
		isClean = (getElement("Firstname")).ensureName() && isClean;
		isClean = (getElement("Lastname")).ensureName() && isClean;
		isClean = (getElement("Email")).ensureEmail() && isClean;

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
