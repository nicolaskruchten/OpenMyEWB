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

import ca.myewb.frame.Helpers;
import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.forms.element.Element;
import ca.myewb.frame.forms.element.Radio;
import ca.myewb.frame.forms.element.Text;
import ca.myewb.frame.forms.element.TextArea;


public class ApplicantEmailEditForm extends Form
{
	public ApplicantEmailEditForm(String target, PostParamWrapper requestParams) throws Exception
	{
		super(target, "preview email");

		Element elem = addText("Subject", "Subject",
		                       requestParams.get("Subject"), true);
		elem.setInstructions("this will be prefixed with \"[" + Helpers.getEnShortName() + "-applications]\"");
		((Text)elem).setSize(300);
		
		Radio r = addRadio("Sender", "Send from", requestParams.get("Sender"), true);
		r.addOption("self", "my email address");
		r.addOption("system", Helpers.getSystemEmail());

		elem = addTextArea("Body", "Body", requestParams.get("Body"), true);

		elem.setInstructionTemplate("formatting");
		((TextArea)elem).makeTwoCols();
		
		addCheckbox("Sendtorejects", "Send to users who's applications have already been rejected?", requestParams.get("Sendtorejects"), "(check if yes)");
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		isClean = getElement("Subject").ensureTotalLength(true, 100) && isClean;

		return isClean;
	}
}
