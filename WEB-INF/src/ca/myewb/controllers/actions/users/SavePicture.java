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

package ca.myewb.controllers.actions.users;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Message;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.PictureForm;


public class SavePicture extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		// Create & validate form object
		PictureForm form = new PictureForm(path + "/actions/SavePicture",
		                                   requestParams);

		Message m = form.validate();

		if (!((m == null) || (isOnConfirmLeg())))
		{
			// Display error and prompt user to fix
			throw getValidationException(form, m,
			                             path + "/profile/ChangePicture");
		}

		urlParams.processParams(new String[]{"action"}, new String[]{"save"});

		if (urlParams.get("action").equals("delete"))
		{
			requireConfirmation("Confirm: delete your picture?", "",
			                    path + "/profile/ShowInfo",
			                    path + "/actions/SavePicture/delete",
			                    "profile", form);

			log.debug("Deleting " + Helpers.getUserFilesDir()
			          + "/userpics/thumbs/"
			          + Integer.toString(currentUser.getId()) + ".jpg");

			File file = new File(Helpers.getUserFilesDir() + "/userpics/thumbs/"
			                     + Integer.toString(currentUser.getId()) + ".jpg");
			if (file.exists())
			{
				file.delete();
			}

			file = new File(Helpers.getUserFilesDir() + "/userpics/fullsize/"
			                + Integer.toString(currentUser.getId()) + ".jpg");
			if (file.exists())
			{
				file.delete();
			}

			file = new File(Helpers.getUserFilesDir() + "/userpics/minithumbs/"
			                + Integer.toString(currentUser.getId()) + ".jpg");
			if (file.exists())
			{
				file.delete();
			}

			file = new File(Helpers.getUserFilesDir() + "/userpics/screensize/"
			                + Integer.toString(currentUser.getId()) + ".jpg");
			if (file.exists())
			{
				file.delete();
			}

			setSessionMessage(("Picture deleted<br />(refresh page if changes not visible)"));
		}
		else if (!requestParams.fileReceived("File0"))
		{
			currentUser.setAdditionalInfo(form.getParameter("additionalInfo"));
			setSessionMessage(("Additional info updated<br />(refresh page if changes not visible)"));
			log.info("Additional info updated for " + currentUser.getId());
		}
		else if (requestParams.saveJpeg("File0", currentUser.getId()) == false)
		{
			setSessionErrorMessage(("Error saving file: are you sure it was a picture in JPEG format?"));
		}
		else
		{
			currentUser.setAdditionalInfo(form.getParameter("additionalInfo"));
			log.info("User picture and/or additional info updated for " + currentUser.getId());

			// Leave a message in the session
			setSessionMessage(("Picture and/or additional info updated<br />(refresh page if changes not visible)"));
		}

		// Redirect to somewhere
		throw new RedirectionException(path + "/profile/ShowInfo");
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}
	
	public String oldName()
	{
		return "SavePicture";
	}
}
