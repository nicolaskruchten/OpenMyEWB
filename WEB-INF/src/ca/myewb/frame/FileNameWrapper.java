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
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import org.apache.log4j.Logger;


public class FileNameWrapper
{
	private File theFile;
	private static ArrayList<String> knownFormats;
	static
	{
		knownFormats = new ArrayList<String>();
		knownFormats.addAll(Arrays.asList("doc","pdf","ppt","txt","rtf","xls","zip",
			"tar","gz","rar","html","htm","php","xml","odt","ods","odp","odg","odf",
			"jpeg","jpg","gif","png","psd","mp3","ogg","wav","au","mov","mpg","mpeg",
			"divx","xvid","docx","pptx","xlsx","bmp"));
	}
	private static ArrayList<String> imageExts;
	static
	{
		imageExts = new ArrayList<String>();
		imageExts.addAll(Arrays.asList("jpg", "jpeg", "gif", "png"));
	}

	public FileNameWrapper(File file)
	{
		super();
		theFile = file;
	}

	public String getName()
	{
		return theFile.getName();
	}
	
	public String getFormattedSize()
	{
		int kBytes = (int)(theFile.length() / 1024);
		
		if(kBytes >= 1024)
		{
			return Integer.toString(kBytes / 1024) + "MB";
		}
		else
		{
			return Integer.toString(kBytes) + "KB";
		}
	}

	public String getURLEncodedname()
	{
		try
		{
			return URLEncoder.encode(theFile.getName(), "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			Logger.getLogger(this.getClass()).error("filename encoding error", e);

			return theFile.getName();
		}
	}
	
	public String getRelativePath() throws FileNotFoundException, UnsupportedEncodingException
	{
		File root = new File( Helpers.getUserFilesDir() );
		
		if(root.getAbsolutePath().regionMatches(0, theFile.getAbsolutePath(), 0, root.getAbsolutePath().length()))
		{
			Vector<File> pathList = new Vector<File>(10);
			
			File parent = theFile;		
			
			do
			{
				pathList.add(parent);
				parent = parent.getParentFile();
			}while( !parent.equals(root) );
			
			//Remove the groupfiles directory, and the groupId directory
			for( int i = 0; i < 2; i++)
			{
				Logger.getLogger(this.getClass()).info(pathList.get(pathList.size() - 1));
				pathList.remove(pathList.size() - 1);
			}
			
			String path = "";
			
			if(pathList.isEmpty()){
				path = "/";
			}
			else
			{
				
				for( File f : pathList )
				{
					path = "/" + URLEncoder.encode(f.getName(), "UTF-8") + path;
				}
				
			}
			
			return path;
		}
		
		throw new FileNotFoundException();
	}
	
	public boolean isDirectory()
	{
		return theFile.isDirectory();
	}
	
	public boolean isFile()
	{
		return theFile.isFile();
	}
	
	public String getMD5Hash() throws NoSuchAlgorithmException, FileNotFoundException, UnsupportedEncodingException{
		
		String key = getRelativePath();
		
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(key.getBytes());

		byte[] v = md.digest();

		// Thank you
		// http://forum.java.sun.com/thread.jspa?threadID=429739&messageID=1921162
		String HEX_DIGITS = "0123456789abcdef";
		StringBuffer sb = new StringBuffer(v.length * 2);

		for (int i = 0; i < v.length; i++)
		{
			int b = v[i] & 0xFF;
			sb.append(HEX_DIGITS.charAt(b >>> 4)).append(
					HEX_DIGITS.charAt(b & 0xF));
		}
		
		return sb.toString();
	}
	
	public String getExtension(){
		
		String extension = getName().substring(getName().lastIndexOf('.') + 1).toLowerCase();
		if(theFile.isFile() && knownFormats.contains(extension))
		{
			return extension;
		}
		else
		{
			return "";
		}
	}
	
	public int getSubfolderCount( boolean recursive )
	{
		int i = 0;
		
		if(theFile.listFiles() == null)
		{
			return 0;
		}
		
		for(File f: theFile.listFiles()){
			if( f.isDirectory() )
			{
				i++;
				if( recursive )
				{
					i += new FileNameWrapper(f).getSubfolderCount(true);
				}
			}
		}
		
		return i;
	}
	
	public int getFileCount( boolean recursive )
	{
		int i = 0;
		
		if(theFile.listFiles() == null)
		{
			return 0;
		}
		
		for(File f: theFile.listFiles()){
			if( f.isDirectory() )
			{
				if( recursive )
				{
					i += new FileNameWrapper(f).getFileCount(true);
				}
			}
			else
			{
				i++;
			}
		}
		
		return i;
	}
	
	public boolean needsThickBox()
	{
		return imageExts.contains(getExtension());
	}

	
	public boolean needsNewWindow()
	{
		return getExtension().equals("pdf");
	}
}
