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

import java.util.Vector;

import ca.myewb.frame.PostParamWrapper;


public class PlacementEditForm extends Form
{
	public PlacementEditForm(String target, PostParamWrapper requestParams)
	{
		super(target, "save placement");
		addText("Name", "Name", requestParams.get("Name"), true);
		addText("Startdate", "Start Date", requestParams.get("Startdate"), false).setInstructions("date format doesn't matter");
		addText("Enddate", "End Date", requestParams.get("Enddate"), false).setInstructions("date format doesn't matter");
		addText("Country", "Country", requestParams.get("Country"), false);
		addText("Town", "Town", requestParams.get("Town"), false);
		addText("Accountingid", "Accounting ID", requestParams.get("Accountingid"), false);
		addTextArea("Description", "Description", requestParams.get("Description"), false)
		.setInstructionTemplate("formatting");
		addCheckbox("Longterm", "Longterm?", requestParams.get("Longterm"), "(check if yes)");
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		Vector<Character> allowed = new Vector<Character>();
		isClean = (getElement("Accountingid")).ensureAlphanumeric(allowed, true)
        && isClean;
		allowed.add(new Character(' '));
		allowed.add(new Character('-'));
		allowed.add(new Character(','));
		isClean = (getElement("Startdate")).ensureAlphanumeric(allowed, true)
        && isClean;
		isClean = (getElement("Enddate")).ensureAlphanumeric(allowed, true)
        && isClean;
		isClean = (getElement("Country")).ensureAlphanumeric(allowed, true)
        && isClean;
		isClean = (getElement("Town")).ensureAlphanumeric(allowed, true)
        && isClean;
		allowed.add(new Character('['));
		allowed.add(new Character(']'));
		isClean = (getElement("Name")).ensureAlphanumeric(allowed, true)
        && isClean;
		//getElement("Description").ensureDisplayableText();

		return isClean;
	}
}
