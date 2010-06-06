function handleLoad(base, setTimer, area) 
{
    if(setTimer)
    {
        setTimeout("ajaxKeepAlive('" + base + "/ajax/keepalive')",   4 * 60 * 1000);
    }
        
    $("#formelemKeywords").autocomplete(base + "/ajax/autocomplete?area=" + area, {multiple:true, cacheLength: 1, minChars: 2});
    
}


// ajax stuff

function ajaxKeepAlive(url)
{
	jQuery.get(url);
	setTimeout('ajaxKeepAlive(\'' + url + '\')',  4 * 60 * 1000);
}

function togglePostFlag(url, aTag, target, label)
{
	aTag.innerHTML = "(processing...)";
	aTag.href = target;
	aTag.className = "nodeco";
	new jQuery.post(url, 
	   function(data) 
	   {      
	       aTag.innerHTML = "(" + label + ")";
	   }
	   );
	aTag.onclick = null;
	return false;
}

function pushBackNewPosts(url, aTag)
{
	aTag.innerHTML = "(processing...)";
	aTag.removeAttribute("href");
	aTag.className = "nodeco";
	
    new jQuery.post(url, 
       function(data) 
       {      
          aTag.innerHTML = "(done: these posts will still be 'new' next time you sign in)";
       }
       );
	aTag.onclick = null;
	return false;
}

// additional files

var n = 1;
			
function mouseHand(){
	document.body.style.cursor = "hand";
}

function mousePointer(){
	document.body.style.cursor = "pointer";				
}

function addField(formName, addButtonName, elementBaseName){
	fileChooser = document.createElement("input");
	fileChooser.setAttribute("type", "file");
	fileChooser.setAttribute("name", elementBaseName + n);
	
	br = document.createElement("br");
	
	addButton = document.getElementById(addButtonName);
	addButton.parentNode.insertBefore(br, addButton);
	addButton.parentNode.insertBefore(fileChooser, addButton);
	
	n++;
}


// groupfiles

function showHideDirectory( base, groupId, relPath, pathHash ){
	try{
				
		dir = document.getElementById("dir_" + pathHash);
				
		if(dir.style.display != "none")
		{
			dir.style.display = "none";
		}
		else
		{
			dir.style.display = "block";
		}
		
	}catch(err){
		
		message = document.createElement("div");
		file = document.getElementById( "file_" + pathHash );
		file.appendChild(message);
		
		message.innerHTML = '<img src="' + base + '/images/indicator.white.gif" /> Loading Directory...';
		
        new jQuery.post(base + "/ajax/groupfiles", 
	        {"action": "showDirectory", "groupId": groupId, "path" : relPath},
			function(data) 
			{      
				file.removeChild(message);
				file.innerHTML += data;
			}
        );
	}
}

function showHideFileOptions( pathHash ){
	
	dir = document.getElementById("fileoptions_" + pathHash);
	
	if(dir.style.display != "none")
	{
		dir.style.display = "none";
	}
	else
	{
		dir.style.display = "block";
	}
}

function deleteFile( base, groupId, relPath, pathHash ){
	file = document.getElementById( "file_" + pathHash );
	
	//TODO decode relPaths!
	if(file.className.match("file") != null){
		if(!confirm("Are you sure you want to delete the file \"" + relPath + "\"?"))
		{
			return;
		}
	}
	else{
		if(!confirm("Are you sure you want to delete the folder \"" + relPath + "\" and all of its contents?"))
		{
			return;
		}
	}
	
	file = document.getElementById( "file_" + pathHash );

	new jQuery.post(base + "/ajax/groupfiles", 
	    {"action": "delete", "groupId": groupId, "path" : relPath},
	    function(data) 
	    {      
	        file.parentNode.removeChild(file);
	    }
	);
}