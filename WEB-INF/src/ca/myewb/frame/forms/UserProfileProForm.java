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


public class UserProfileProForm extends Form
{
	public UserProfileProForm(String target, PostParamWrapper requestParams)
	{
		super(target, "save non-student info");
		addText("Employer", "Employer", requestParams.get("Employer"), false);
		addText("Sector", "Sector", requestParams.get("Sector"), false);

		Dropdown d = addDropdown("Compsize", "Company size",
		                         requestParams.get("Compsize"), false);

		for (int i = 1; i < UserModel.companySizes.length; i++)
		{
			d.addOption("" + i, UserModel.companySizes[i]);
		}

		addText("Position", "Position", requestParams.get("Position"), false);

		Dropdown d2 = addDropdown("Income", "Income level",
		                          requestParams.get("Income"), false);

		for (int i = 1; i < UserModel.incomeLevels.length; i++)
		{
			d2.addOption("" + i, UserModel.incomeLevels[i]);
		}
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		return isClean;
	}
}
