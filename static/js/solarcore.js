var is_fullscreen = false;
var is_error = false;
var fs_padding = 0;
	
$(function() {
	if (!String.format) {
		String.format = function(format) {
			var args = Array.prototype.slice.call(arguments, 1);
			return format.replace(/{(\d+)}/g, function(match, number) { 
				return typeof args[number] != 'undefined' ? args[number] : match;
			});
		};
	}
	
	$(document).click(function() {
		$(".contextmenu").each(function() {
			$(this).hide();
		});
		$(".item-row").each(function() {
			$(this).removeClass("active");
		});

	});
	
	if ($("#login").length) {
		setTimeout(function() {
				$("#sc_login_field").focus();
				$("#login").removeClass("transparent");
		}, 200);
	}
	
	$("#panel a").click(function() {
		$("#panel a").each(function() {
			$(this).removeClass("active");
		});
		$(this).addClass("active");
	});
	
	$("#sc_login_button").click(function() {
		$("#login").addClass("transparent");
		setTimeout(function() {
			$("#login_form").submit();
		}, 400);
	});
	
	$(".fullscreen_button").click(function() {
		var fade_speed = 400;
		if (!is_fullscreen) {
			$("#panel").fadeOut(fade_speed);
			$("#contentheader").fadeOut(fade_speed);
			$("#content").fadeOut(fade_speed, function() {
				fs_padding = $(this).css("padding");
				$(this).css("padding", "0px").fadeIn(fade_speed);
				$("#fullscreen_controls").show();
			});
			is_fullscreen = true;
		} else {
			$("#content").fadeOut(fade_speed, function() {
				$(this).css("padding", fs_padding).fadeIn(fade_speed);
				$("#fullscreen_controls").hide();
				$("#panel").fadeIn(fade_speed);
				$("#contentheader").fadeIn(fade_speed);
			});
			is_fullscreen = false;
		}
	});
	
	$("#panel a").click(function(e) {
		return;
		var linkDestination = $(this).attr("href");
		e.preventDefault();
		$("#content .content").fadeOut(300, function() {
			window.location = linkDestination;
		});
	});
	
	if ($("#dashboard").length) {
		buildDashboard();
		setInterval(buildDashboard, 5000);
	}
	
	if ($("#folderlist").length) {
		buildFolderList();
	}
	
	$("#add_folder_button").click(function() {
		$("#add_folder_name").val("");
		$("#add_folder_description").val("");
		var s = $("#add_folder_folder");
		s.html("");
		$.get("/api/folderlist", function(xml) {
			var xmlCollection = xml.getElementsByTagName("folder");
			s.append("<option name='0'>Root folder</option>");
			for (i = 0; i < xmlCollection.length; i++) {
				var folder = xmlCollection[i];
				s.append("<option name='" + folder.getAttribute("id") + "'>" + folder.getElementsByTagName("name")[0].childNodes[0].nodeValue + "</option>");
			}
			$("#folder_modal").modal();
		});
	});
	
	$("#add_target_button").click(function() {
		$("#add_folder_name").val("");
		$("#add_folder_description").val("");
		var s = $("#add_target_folder");
		s.html("");
		$.get("/api/folderlist", function(xml) {
			var xmlCollection = xml.getElementsByTagName("folder");
			s.append("<option name='0'>Root folder</option>");
			for (i = 0; i < xmlCollection.length; i++) {
				var folder = xmlCollection[i];
				s.append("<option name='" + folder.getAttribute("id") + "'>" + folder.getElementsByTagName("name")[0].childNodes[0].nodeValue + "</option>");
			}
			$("#target_modal").modal({backdrop: 'static', keyboard: true });
		});
	});
	
	$("#change_admin_password").click(function() {
		$("#add_folder_name").val("");
		$("#add_folder_description").val("");
		var s = $("#add_target_folder");
		s.html("");
		$.get("/api/folderlist", function(xml) {
			var xmlCollection = xml.getElementsByTagName("folder");
			s.append("<option name='0'>Root folder</option>");
			for (i = 0; i < xmlCollection.length; i++) {
				var folder = xmlCollection[i];
				s.append("<option name='" + folder.getAttribute("id") + "'>" + folder.getElementsByTagName("name")[0].childNodes[0].nodeValue + "</option>");
			}
			$("#changepass_modal").modal({backdrop: 'static', keyboard: true });
		});
	});
	
	
	
	$("#save_configuration").click(function() {
		var btn = $(this);
		btn.find(".loading").show();
		btn.attr("disabled", true);
		
		$.post("/api/configsave", { 
			systemname: $("#systemname").val().replace("+", "[SOLARCORE_PLUS]"),
			httpbindaddress: $("#httpbindaddress").val().replace("+", "[SOLARCORE_PLUS]"),
			httpbindport: $("#httpbindport").val().replace("+", "[SOLARCORE_PLUS]"),
			adminfirstname: $("#adminfirstname").val().replace("+", "[SOLARCORE_PLUS]"),
			adminlastname: $("#adminlastname").val().replace("+", "[SOLARCORE_PLUS]"),
			adminemail: $("#adminemail").val().replace("+", "[SOLARCORE_PLUS]"),
			adminphonenumber: $("#adminphonenumber").val().replace("+", "[SOLARCORE_PLUS]"),
			emailserver: $("#emailserver").val().replace("+", "[SOLARCORE_PLUS]"),
			emailport: $("#emailport").val().replace("+", "[SOLARCORE_PLUS]"),
			emailauthrequired: $("#emailauthrequired").val(),
			emailusername: $("#emailusername").val().replace("+", "[SOLARCORE_PLUS]"),
			emailuserpass: $("#emailuserpass").val().replace("+", "[SOLARCORE_PLUS]"),
			emailstarttls: $("#emailstarttls").val(),
			emailsender: $("#emailsender").val().replace("+", "[SOLARCORE_PLUS]"),
			emailsendername: $("#emailsendername").val().replace("+", "[SOLARCORE_PLUS]")
			}, function(data) {
			btn.removeAttr("disabled");
			btn.find(".loading").hide();
		});
	});
	
	setInterval(keepalive, 5000);
});

function keepalive() {
	$.get("/api/keepalive", function(xml) {
		var authorizedStatus = xml.getElementsByTagName("login")[0].getAttribute("authorized");
		
		if (authorizedStatus != "true" && window.location.pathname != "/login") {
			is_error = true;
			$("#error").fadeIn(200);
			$("#error .message").html("Your login has expired.");
			$("#sc_login_back_button").show();
			
			$("#sc_login_back_button").click(function() {
				window.location.href = "/login";
			});
		}
		
		is_error = false;
	}).fail(function(e) {
		if (is_error) return;
		else is_error = true;
				
		$("#error").fadeIn(200);
		$("#error .message").html("Unable to connect to Solarcore daemon. This window will auto-close when the daemon becomes available.");
		$("#sc_login_back_button").hide();
	});
}

function findSubFolders(xml) {
	var count = 0;
	var data = "<ul>";
	xml.children("*").each(function() {
		var id = $(this).attr("id");
		var ttype = $(this).get(0).tagName;
		var cclass = "";
		
		if (ttype != "folder" && ttype != "contact" && ttype != "target" && ttype != "user" && ttype != "probe") return;
		if (typeof(id) == 'undefined') return;
		count += 1;
				
		switch (ttype) {
			case "user":
				data += "<li><a " + 
				"data-username=\"" + $(this).children("username").text() + 
				"\" data-firstname=\"" + $(this).children("firstname").text() + 
				"\" data-lastname=\"" + $(this).children("lastname").text() + 
				"\" data-email=\"" + $(this).children("email").text() + 
				"\" data-phone=\"" + $(this).children("phone").text() + 
				"\" data-location=\"" + $(this).children("location").text() + 
				"\" data-id=\"" + id + 
				"\" data-type=\"" + ttype + 
				
				"\" class=\"folder-object object-user" + 
				"\" href=\"#\">" + $(this).children("username").text() + "<span class='object-description'>User</span>" + 
				"</a>" + findSubFolders($(this)) + "</li>";
				break;
			case "contact":
				data += "<li><a " + 
				"data-foldername=\"" + $(this).children("name").text() + 
				"\" data-id=\"" + id + 
				"\" data-type=\"" + ttype + 
				
				"\" class=\"folder-object object-contact" + 
				"\" href=\"#\">" + $(this).children("firstname").text() + " " + $(this).children("lastname").text() + "<span class='object-description'>Contact</span>" + 
				"</a>" + findSubFolders($(this)) + "</li>";
				break;
			case "target":
				data += "<li><a " + 
				"data-foldername=\"" + $(this).children("name").text() + 
				"\" data-id=\"" + id + 
				"\" data-type=\"" + ttype + 
				
				"\" class=\"folder-object object-target" + 
				"\" href=\"#\">" + $(this).children("name").text() + "<span class='object-description'>Target</span>" + 
				"</a>" + findSubFolders($(this)) + "</li>";
				break;
			case "folder":
				data += "<li><a " + 
				"data-name=\"" + $(this).children("name").text() + 
				"data-description=\"" + $(this).children("description").text() + 
				"\" data-id=\"" + id + 
				"\" data-type=\"" + ttype + 
				
				"\" class=\"folder-object object-folder" + 
				"\" href=\"#\">" + $(this).children("name").text() + "<span class='object-description'>Folder</span>" + 
				"</a>" + findSubFolders($(this)) + "</li>";
				
				break;
			case "probe":
				data += "<li><a " + 
				"data-foldername=\"" + $(this).children("name").text() + 
				"\" data-id=\"" + id + 
				"\" data-type=\"" + ttype + 
				
				"\" class=\"folder-object object-probe" + 
				"\" href=\"#\">" + $(this).children("type").text() + "<span class='object-description'>Probe</span>" + 
				"</a>" + findSubFolders($(this)) + "</li>";
				break;
		}
		
		
	});
	
	data += "</ul>";
	
	
	if (count > 0) return data;
	else return "";
}

function buildFolderList() {
	$.get("/api/folderlist?megatree=1", function(xml) {
		var flist = $("#folderlist");
		var data = "<ul>";
		
		
		$(xml).children('*').each(function() {
			data += "<li>" + findSubFolders($(this)) + "</li>";
		});
	
		flist.html(data);
		
		$(".folder-object").click(function() {
			var type = $(this).attr("data-type");
			var id = $(this).attr("data-id");
			
			$(".folder-object").each(function() {
				$(this).removeClass("active");
			});
			
			$(this).addClass("active");
			
			$(".object-editor").each(function() {
				$(this).hide();
			});

			
			if (type == "folder") {
				$("#folder_edit .name").val($(this).attr("data-name"));
				$("#folder_edit").show();
			}
			
			if (type == "target") {
				$("#target_edit .name").val($(this).attr("data-name"));
				$("#target_edit").show();
			}
			
			if (type == "user") {
				$("#user_edit .name").val($(this).attr("data-name"));
				$("#user_edit").show();
			}
			
		}).each(function() {
			$(this).contextmenu(function(e) {
				$("#object_context_menu").css("top", e.pageY).css("left", e.pageX).show();
				e.preventDefault();
			}).click(function() {
				$(this).addClass("active");
			});
		});

		
	});
}

function buildDashboard() {
	$.get("/api/dashboard", function(xml) {
		var data = '<div class="container-fluid"><div class="row"><div class="noselect column header col-lg-3 col-md-4 col-sm-12">Name</div><div class="noselect column header col-lg-3 col-md-4 col-sm-12">Location</div><div class="noselect column header col-lg-3 col-md-4 col-sm-12">Message</div><div class="noselect column header col-lg-3 col-md-4 col-sm-12">State</div></div>';
		
		
		
		var xmlCollection = xml.getElementsByTagName("target");
		for (i = 0; i < xmlCollection.length; i++) {
			var target = xmlCollection[i];
			var targetID = target.getElementsByTagName("id")[0].childNodes[0].nodeValue;
			
			var targetName = target.getElementsByTagName("name")[0].childNodes[0].nodeValue;
			var targetDescription = target.getElementsByTagName("description")[0].childNodes[0].nodeValue;
			var targetLocation = target.getElementsByTagName("location")[0].childNodes[0].nodeValue;
			var targetMessage = target.getElementsByTagName("message")[0].childNodes[0].nodeValue;
			var targetState = target.getElementsByTagName("status")[0].childNodes[0].nodeValue;
			
			var objectHtml = '<div class="{0} row item-row" id="target-{1}">'

			objectHtml += '<div class="noselect col col-lg-3 col-md-4 col-sm-12">{2}<br/><small class="noselect">{3}</small></div>\r\n';
			objectHtml += '<div class="noselect col col-lg-3 col-md-4 col-sm-12">{4}</div>\r\n';
			objectHtml += '<div class="noselect col col-lg-3 col-md-4 col-sm-12">{5}</div>\r\n';
			objectHtml += '<div class="noselect col col-lg-3 col-md-4 col-sm-12">{6}</div>\r\n';
			
			
			
			if (targetState == "OK") {
				objectHtml = String.format(objectHtml, "state-ok", targetID, targetName, targetDescription, targetLocation, targetMessage, targetState);
			} else if (targetState == "UNKNOWN") {
				objectHtml = String.format(objectHtml, "state-unknown", targetID, targetName, targetDescription, targetLocation, targetMessage, targetState);
			} else {
				objectHtml = String.format(objectHtml, "state-error", targetID, targetName, targetDescription, targetLocation, targetMessage, targetState);
			}
			
			var probeXMLCollection = target.getElementsByTagName("probe");
			
			for (z = 0; z < probeXMLCollection.length; z++) {
				var probe = probeXMLCollection[z];
				
				var probeStateType = probe.getElementsByTagName("state")[0].getAttribute("type");
				var probeState = probe.getElementsByTagName("state")[0].childNodes[0].nodeValue;

			}
			
			objectHtml += '</div>';
			
			data += objectHtml;
		}
		
		data += '</div>';
		
		$("#dashboard").html(data);
		
		$(".item-row").each(function() {
			$(this).contextmenu(function(e) {
				$("#target_context_menu").css("top", e.pageY).css("left", e.pageX).show();
				$(this).addClass("active");
				e.preventDefault();
			}).click(function() {
				$(this).addClass("active");
			});
		});
	});
}

 function getXmlAsString(xmlDom){
	return (typeof XMLSerializer!=="undefined") ? (new window.XMLSerializer()).serializeToString(xmlDom) : xmlDom.xml;
 }          