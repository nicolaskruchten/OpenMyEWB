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

package ca.myewb.frame;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.DefaultFileItem;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.log4j.Logger;

import sun.text.Normalizer;


public class PostParamWrapper extends RequestParamWrapper
{
	private Logger log = Logger.getLogger(this.getClass());
	private Hashtable<String, FileItem> fileParams = new Hashtable<String, FileItem>();

	public PostParamWrapper(HttpServletRequest request)
	                 throws Exception
	{
		if (!FileUpload.isMultipartContent(request))
		{
			return;
		}

		DiskFileUpload upload = new DiskFileUpload();
		upload.setRepositoryPath(Helpers.getUserFilesDir() + "/temp");

		try
        {
            List items = upload.parseRequest(request);

            Iterator iter = items.iterator();

            while (iter.hasNext())
            {
                FileItem item = (FileItem)iter.next();

                if (item.isFormField())
                {
                    stringParams.put(item.getFieldName(), item.getString());
                    allParams.put(item.getFieldName(), item);
                }
                else if (item.getSize() > 0)
                {
                    fileParams.put(item.getFieldName(), item);
                    allParams.put(item.getFieldName(), item);
                }
            }
		}
        catch (FileUploadException ex)
        {
            log.debug("The file upload exception returned the message: " + ex.getMessage());        	
        	// Usually means user cancelled upload and connection timed out.
            // Do nothing.
        }
	}

	public String[] getArray(String value)
	{
		ArrayList<String> values = new ArrayList<String>();
		int i = 0;

		while (stringParams.get(value + i) != null)
		{
			values.add(stringParams.get(value + i++));
		}

		return values.toArray(new String[1]);
	}

	public boolean fileReceived(String name)
	{
		return (fileParams.get(name) != null);
	}
	
	public Map<String, FileItem> getFilesReceived(){
		return fileParams;
	}
	
	public void createDirectory(String folderName, String parentDir) throws Exception{
		folderName = makeNormalFileName(folderName);
		
		File folder = new File(Helpers.getUserFilesDir() + "/groupfiles/" + parentDir + "/" + folderName);		
		
		/*if(folder.exists())
		{
			setSessionErrorMessage("The folder you are trying to create already exists");
			throw new RedirectionException(path + "/mailing/ShowGroupFiles/" + g.getId());
		}*/
		
		log.info("Creating new folder at " + folder.getAbsolutePath());
		folder.mkdirs();
	}
	
	public void saveFile(String fieldPrefix, String subpath) throws Exception
	{
		
		for( String fileName : fileParams.keySet() )
		{
			log.info("Attempting to upload file from field: " + fileName);
			//subpath must be of form a/b/c not /a/b/c/
			if (fileParams.get(fileName) != null && fileName.regionMatches(0, fieldPrefix, 0, fieldPrefix.length()))
			{
				String[] dirs = subpath.split("/");
				String current = "";
	
				for (int i = 0; i < dirs.length; i++)
				{
					File currentSubDir = (new File(Helpers.getUserFilesDir()
					                               + current + "/" + dirs[i]));
	
					if (!currentSubDir.exists())
					{
						currentSubDir.mkdir();
						log.info("Directory " + currentSubDir.getPath() + " created.");
					}
	
					current += ("/" + dirs[i]);
				}
	
				DefaultFileItem item = ((DefaultFileItem)fileParams.get(fileName));
				String prepath = Helpers.getUserFilesDir() + "/" + subpath + "/";
				
				safeFileReplace(new File(prepath + makeNormalFileName(item.getName())), item);
				
				log.info("Successfully uploaded " + item.getName());
				Helpers.currentDailyStats().logFilesAdded();
			}
		}
	}

	// This is similar to the saveFile method above, but it does not assume
	// a zipfile and only accepts jpeg files, creating a thumbnail while we're at it
	public boolean saveJpeg(String name, int userid)
	                 throws Exception
	{
		DefaultFileItem item = ((DefaultFileItem)fileParams.get(name));

		if (item.getName().equals(""))
		{
			return false;
		}
		
		String userIdStr = Integer.toString(userid);
		String userPicturesDir = Helpers.getUserPicturesDir();
		String fullPath = userPicturesDir  + "/fullsize/" + userIdStr + ".jpg";
		String thumbPath = userPicturesDir  + "/thumbs/" + userIdStr + ".jpg";
		String minithumbPath = userPicturesDir  + "/minithumbs/" + userIdStr + ".jpg";
		String screenPath = userPicturesDir  + "/screensize/" + userIdStr + ".jpg";
		
		File file = new File(fullPath);
		File thumbnail = new File(thumbPath);
		File minithumbnail = new File(minithumbPath);
		File screensize = new File(screenPath);

         try
        {
     		file.delete();
     		item.write(file);
     		
			String command = "";
			
			thumbnail.delete();
			command = "convert -thumbnail 200x200 -quality 100 " + fullPath + " " + thumbPath;
			Runtime.getRuntime().exec(command).waitFor();
			if(!thumbnail.exists()) 
			{
				throw new Exception("no file: " + thumbPath);
			}

			minithumbnail.delete();
			command = "convert -thumbnail 75x -quality 100 " + fullPath + " " + minithumbPath;
			Runtime.getRuntime().exec(command).waitFor();
			if(!minithumbnail.exists()) 
			{
				throw new Exception("no file: " + minithumbPath);
			}

			screensize.delete();
			command = "convert -thumbnail 800x600 -quality 100 " + fullPath + " " + screenPath;
			Runtime.getRuntime().exec(command).waitFor();
			if(!screensize.exists()) 
			{
				throw new Exception("no file: " + screenPath);
			}
			 
			return true;
        }
        catch (Exception e)
        {
			Logger.getLogger(this.getClass()).error("jpeg error: " + e, e);
                   
    		file.delete();
    		thumbnail.delete();
    		minithumbnail.delete();

    		return false;
        }
	}

	private String makeNormalFileName(String badName) throws Exception
	{
		String result = Normalizer.normalize(badName, Normalizer.DECOMP, 0); //decompose accented into ascii plus unicode accent mark
		result = result.replaceAll("[^\\p{ASCII}]",""); //nuke non-ascii (so unicode accent marks from above)
		result = result.substring(result.lastIndexOf('\\') + 1); //remove all before last backslash
		result = result.substring(result.lastIndexOf('/') + 1); //remove all before last slash
		result = result.replaceAll("[^\\w .-]*", ""); //nukes anything that's not a letter/number/underscore/space/period/dash
		return result;
	}
	
	private File safeFileReplace(File file, DefaultFileItem item) throws Exception{
		
		File temp = new File(file.getPath() + file.getName() + ".tmp");
		
		//If the file exists, rename it .tmp
		if(file.exists())
		{
			file.renameTo(temp);
			log.info("Old file renamed to " + file.getPath());
		}
		
		item.write(file); //write the file

		log.info("New file saved as " + file.getPath());
		
		//delete the temporary file
		if( temp.exists() ){
			temp.delete();
			log.info("Temporary file deleted from " + temp.getPath());
		}
		
		return file;
	}
	
	public String toString()
	{
		String result = "(";

		for (String key : stringParams.keySet())
		{
			result += (key + "=" + stringParams.get(key) + "&");
		}

		return result + ")";
	}
}
