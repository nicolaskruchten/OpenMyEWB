/*
 * Copyright 2000-2001,2004, 2008 The Apache Software Foundation, Nicolas Kruchten.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.myewb.frame.servlet;


import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.io.VelocityWriter;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.util.SimplePool;

public abstract class CachingVelocityServlet extends HttpServlet
{
    private static SimplePool writerPool = new SimplePool(40);

    public void doGet( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        doRequest(request, response);
    }

    public void doPost( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        doRequest(request, response);
    }

    protected void doRequest(HttpServletRequest request, HttpServletResponse response )
         throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        
        boolean isCachable = isCachable(request);
        
        if(isCachable)
    	{
        	String cacheIfFresh = getCachedOutputIfFresh(request);
	    	if(cacheIfFresh != null)
	    	{
				PrintWriter printWriter = new PrintWriter(response.getOutputStream());
				printWriter.write(cacheIfFresh);
				printWriter.flush();
				return;
	    	}
    	}
    	
        try
        {
        	Context context = new VelocityContext();
            context.put( "req", request );
            context.put( "res", response );
            
            ////////////////// This is the big call!
            	Template template = handleRequest( request, response, context );     
            
            if ( template == null )
            {
                return;
            }
            mergeTemplate( template, context, response, isCachable);
        }
        catch (Exception e)
        {
            error( request, response, e);
        }
        finally
        {
            requestCleanup( request, response);
        }
    }

    
    protected void mergeTemplate( Template template, Context context, HttpServletResponse response, boolean isCachable )
        throws ResourceNotFoundException, ParseErrorException, 
               MethodInvocationException, IOException, UnsupportedEncodingException, Exception
    {
        VelocityWriter vw = null;
        String encoding = response.getCharacterEncoding();
        
        try
        {
			if(isCachable)
			{
				PrintWriter printWriter = new PrintWriter(response.getOutputStream());
				StringWriter sw = new StringWriter();
				
				template.merge(context, sw);
				
				String toCache = sw.toString();
				saveOutputToCache(toCache);
				
				printWriter.write(toCache);
				printWriter.flush();
			}
			else
			{
				vw = (VelocityWriter) writerPool.get();
	            
	            Writer theWriter = new OutputStreamWriter(response.getOutputStream(), encoding);
	            
				if (vw == null)
	            {
	                vw = new VelocityWriter(theWriter,
	                                        4 * 1024, true);
	            }
	            else
	            {
	                vw.recycle(theWriter);
	            }
	           
				template.merge(context, vw);
			}
        }
        finally
        {
            try
            {
                if (vw != null)
                {
                    vw.flush();
                    vw.recycle(null);
                    writerPool.put(vw);
                }
            }
            catch (Exception e)
            {
                // do nothing
            }
        }
    }


    public Template getTemplate( String name )
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return RuntimeSingleton.getTemplate(name);
    }

    protected abstract Template handleRequest( HttpServletRequest request, HttpServletResponse response, Context ctx ) 
        throws Exception;
    
    protected abstract void error( HttpServletRequest request, HttpServletResponse response, Exception cause )
        throws ServletException, IOException;

    protected abstract void requestCleanup( HttpServletRequest request, HttpServletResponse response);

    //is this particular request cachable? called after handleRequest, so no args needed
    protected abstract boolean isCachable(HttpServletRequest request);
    
    protected abstract void saveOutputToCache(String toCache);
	
    //return null if cache is stale
	public abstract String getCachedOutputIfFresh(HttpServletRequest request);
}

