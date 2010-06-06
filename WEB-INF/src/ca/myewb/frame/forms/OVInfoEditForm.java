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
import ca.myewb.frame.forms.element.Radio;


public class OVInfoEditForm extends Form
{
	public OVInfoEditForm(String target, PostParamWrapper requestParams)
	{
		super(target, "save info");
		
		addHeader("h1", "General Info");
		addText("Healthnumber", "Provincial health number", requestParams.get("Healthnumber"), false);
		addText("SIN", "Social Insurance Number", requestParams.get("SIN"), false);
		addDatePicker("DOB", "Date of birth", requestParams.get("DOB"), false)
		.setInstructions("YYYY-MM-DD");

		addHeader("h2", "Passport Info");
		addText("Passnumber", "Passport number", requestParams.get("Passnumber"), false);
		addText("Passname", "Name on passport", requestParams.get("Passname"), false)
			.setInstructions("exactly as written on passport");
		addText("Passplace", "Place of issue", requestParams.get("Passplace"), false)
			.setInstructions("exactly as written on passport");
		addDatePicker("Passstart", "Date of issue", requestParams.get("Passstart"), false)
		.setInstructions("YYYY-MM-DD");
		addDatePicker("Passend", "Expiry date", requestParams.get("Passend"), false)
		.setInstructions("YYYY-MM-DD");


		addHeader("h3", "First Emergency Contact");
		addText("E1name", "Name", requestParams.get("E1name"), false);
		addText("E1relation", "Relation", requestParams.get("E1relation"), false);
		addAddress("E1address", "Address", requestParams.getArray("E1address"), false);
		addPhone("E1business", "Business number", requestParams.getArray("E1business"), false);
		addPhone("E1home", "Home number", requestParams.getArray("E1home"), false);
		addPhone("E1fax", "Fax number", requestParams.getArray("E1fax"), false);
		addText("E1email", "Email address", requestParams.get("E1email"), false);
		
		Radio l = addRadio("E1language", "Preferred language",
                requestParams.get("E1language"), false);
		l.addOption("en", "English");
		l.addOption("fr", "French");
		l.setNumAcross(2);
		
		addCheckbox("E1updates", "Send updates to this person?", requestParams.get("E1updates"), "yes");



		addHeader("h4", "Second Emergency Contact");
		addText("E2name", "Name", requestParams.get("E2name"), false);
		addText("E2relation", "Relation", requestParams.get("E2relation"), false);
		addAddress("E2address", "Address", requestParams.getArray("E2address"), false);
		addPhone("E2business", "Business number", requestParams.getArray("E2business"), false);
		addPhone("E2home", "Home number", requestParams.getArray("E2home"), false);
		addPhone("E2fax", "Fax number", requestParams.getArray("E2fax"), false);
		addText("E2email", "Email address", requestParams.get("E2email"), false);
		
		l = addRadio("E2language", "Preferred language",
                requestParams.get("E2language"), false);
		l.addOption("en", "English");
		l.addOption("fr", "French");
		l.setNumAcross(2);
		
		addCheckbox("E2updates", "Send updates to this person?", requestParams.get("E2updates"), "yes");
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		isClean = getElement("DOB").ensureDate() && isClean;
		isClean = getElement("Passstart").ensureDate() && isClean;
		isClean = getElement("Passend").ensureDate() && isClean;
		isClean = getElement("E1email").ensureEmail() && isClean;
		isClean = getElement("E2email").ensureEmail() && isClean;
		return isClean;
	}
}
