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
import ca.myewb.frame.forms.element.Dropdown;
import ca.myewb.model.UserModel;


public class UserProfileStudentForm extends Form
{
	public UserProfileStudentForm(String target, PostParamWrapper requestParams)
	{
		super(target, "save student info");

		addText("Institution", "Institution", requestParams.get("Institution"),
		        false);

		Dropdown d3 = addDropdown("Level", "Level of study",
		                          requestParams.get("Level"), false);

		for (int i = 1; i < UserModel.studentLevels.length; i++)
		{
			d3.addOption("" + i, UserModel.studentLevels[i]);
		}

		addText("Field", "Field of study", requestParams.get("Field"), false);
		addText("Studentnumber", "Student number",
		        requestParams.get("Studentnumber"), false);

		Dropdown d = addDropdown("Gradmonth", "Month of Graduation",
		                         requestParams.get("Gradmonth"), false);

		for (int i = 0; i < UserModel.gradMonths.length; i++)
		{
			d.addOption("" + (i + 1), UserModel.gradMonths[i]);
		}

		Dropdown d2 = addDropdown("Gradyear", "Year of Graduation",
		                          requestParams.get("Gradyear"), false);

		for (int i = 2006; i < 2015; i++)
		{
			d2.addOption("" + i, "" + i);
		}
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		return isClean;
	}
}
