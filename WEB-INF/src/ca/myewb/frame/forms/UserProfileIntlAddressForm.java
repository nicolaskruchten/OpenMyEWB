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
import ca.myewb.frame.forms.element.Text;


public class UserProfileIntlAddressForm extends Form
{
	public UserProfileIntlAddressForm(String target,
	                                  PostParamWrapper requestParams,
	                                  boolean isRegular)
	{
		super(target, "save contact info");

		addIntlAddress("Address", "Mailing Address", requestParams.getArray("Address"), isRegular);

		Text ph = addText("Phone", "Main phone number", requestParams.get("Phone"),
		                  isRegular);
		ph.setInstructions("Please include country code");

		Text alt = addText("Alt", "Alternate phone number", requestParams.get("Alt"), false);
		alt.setInstructions("Please include country code");

		Text cell = addText("Cell", "Cell phone number", requestParams.get("Cell"), false);
		cell.setInstructions("Please include country code");

		Text biz = addText("Business", "Work phone number",
		                   requestParams.get("Business"), false);
		biz.setInstructions("Please include country code");
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		return isClean;
	}
}
