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


public class MassDeleteForm extends Form
{
	public MassDeleteForm(String target, PostParamWrapper requestParams)
	{
		super(target);
		addTextArea("Emails", "Email addresses", requestParams.get("Emails"), true)
		.setInstructions("one email address per line, no commas or other delimiters");

	}

	public boolean cleanAndValidate(boolean isClean)
	{
		return (getElement("Emails")).ensureEmailList();
	}
}
