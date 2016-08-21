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
	

	function verifyPassInput() {
        var text = $("#adminpassinput").val();
        var passok = true;
        var linelength = 20;
        $("#passline").html("Password secure");

        if(!/[~`!@#$%\^&*()+=\-\[\]\\';,/{}|\\":<>\?]/g.test(text)) {
            $("#passline").html("Password doesn't contain any special characters");
            passok = false;
        } else {
            linelength += 30;
        }

        if (text.toLowerCase() == text) {
            $("#passline").html("Password doesn't contain any uppercase characters");
            passok = false;
        } else {
            linelength += 30;
        }

        if (text.length < 5) {
            $("#passline").html("Password too short");
            passok = false;
        } else {
            linelength += 30;
        }

        $("#passline").css("width", linelength);

        if (linelength < 40) {
            $("#passline").css("border-bottom", "3px solid #ff3344");
        } else if (linelength >= 40 && linelength < 90) {
            $("#passline").css("border-bottom", "3px solid #f3b100");
        } else {
            $("#passline").css("border-bottom", "3px solid #33ff00");
        }

        if (passok && $("#adminpassinput").val() == $("#adminpassinputrepeat").val()) {
            $("#changepass_save").removeAttr("disabled");
        } else {
            $("#changepass_save").attr("disabled", "disabled");
        }
	}

	$("#changepass_save").click(function() {
		$.post("/api/objectsave", {
		    objecttype: "folder",
		    objectid: pId,
			name: pName,
			description: pDescription
        }, function() {
           	buildFolderList();
        });
	});


    $("#adminpassinputrepeat").on('input', function() {
        verifyPassInput();

        if ($(this).val() != $("#adminpassinput").val()) {
            $("#passline").html("Passwords doesn't match!");
        }
    });

    $("#adminpassinput").on('input', function() {
        verifyPassInput();
    });
	
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
		var linkDestination = $(this).attr("href");
		e.preventDefault();

	    $("#progressbar").fadeIn().css("width", "1920px");

        $("#contentheader").fadeOut(300);
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

	$("#contact_save_button").click(function() {
	    var pId = $("#contact_edit").attr("data-id");
        var pAlias = $("#contact_edit .alias").val().replace("+", "[SOLARCORE_PLUS]");
	    var pFirstname= $("#contact_edit .firstname").val().replace("+", "[SOLARCORE_PLUS]");
	    var pLastname = $("#contact_edit .lastname").val().replace("+", "[SOLARCORE_PLUS]");
	    var pEmail = $("#contact_edit .email").val().replace("+", "[SOLARCORE_PLUS]");
	    var pPhone = $("#contact_edit .phone").val().replace("+", "[SOLARCORE_PLUS]");
        var pLocation = $("#contact_edit .location").val().replace("+", "[SOLARCORE_PLUS]");

		$.post("/api/objectsave", {
		    objecttype: "contact",
		    objectid: pId,
			alias: pAlias,
			firstname: pFirstname,
			lastname: pLastname,
			email: pEmail,
			phone: pPhone,
			location: pLocation
        }, function() {
           	buildFolderList();
        });

	});

	$("#user_save_button").click(function() {
	    var pId = $("#user_edit").attr("data-id");
        var pUsername = $("#user_edit .username").val().replace("+", "[SOLARCORE_PLUS]");
	    var pFirstname= $("#user_edit .firstname").val().replace("+", "[SOLARCORE_PLUS]");
	    var pLastname = $("#user_edit .lastname").val().replace("+", "[SOLARCORE_PLUS]");
	    var pEmail = $("#user_edit .email").val().replace("+", "[SOLARCORE_PLUS]");
	    var pPhone = $("#user_edit .phone").val().replace("+", "[SOLARCORE_PLUS]");
        var pLocation = $("#user_edit .location").val().replace("+", "[SOLARCORE_PLUS]");

		$.post("/api/objectsave", {
		    objecttype: "user",
		    objectid: pId,
			username: pUsername,
			firstname: pFirstname,
			lastname: pLastname,
			email: pEmail,
			phone: pPhone,
			location: pLocation
        }, function() {
           	buildFolderList();
        });

	});

	$("#target_save_button").click(function() {
	    var pId = $("#user_edit").attr("data-id");
        var pName = $("#user_edit .name").val().replace("+", "[SOLARCORE_PLUS]");
	    var pDescription = $("#user_edit .description").val().replace("+", "[SOLARCORE_PLUS]");
	    var pAddress = $("#user_edit .address").val().replace("+", "[SOLARCORE_PLUS]");
	    var pLocation = $("#user_edit .location").val().replace("+", "[SOLARCORE_PLUS]");

        if (pDescription.length == 0) pDescription = "[UNSET]";

		$.post("/api/objectsave", {
		    objecttype: "target",
		    objectid: pId,
			name: pName,
			description: pDescription,
			address: pAddress,
			location: pLocation
        }, function() {
           	buildFolderList();
        });

	});


	$("#folder_save_button").click(function() {
	    var pId = $("#folder_edit").attr("data-id");
        var pName = $("#folder_edit .name").val().replace("+", "[SOLARCORE_PLUS]");
	    var pDescription = $("#folder_edit .description").val().replace("+", "[SOLARCORE_PLUS]");

        if (pDescription.length == 0) pDescription = "[UNSET]";

		$.post("/api/objectsave", {
		    objecttype: "folder",
		    objectid: pId,
			name: pName,
			description: pDescription
        }, function() {
           	buildFolderList();
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
				"data-alias=\"" + $(this).children("alias").text() +
				"\" data-firstname=\"" + $(this).children("firstname").text() +
				"\" data-lastname=\"" + $(this).children("lastname").text() +
				"\" data-email=\"" + $(this).children("email").text() +
				"\" data-phone=\"" + $(this).children("phone").text() +
				"\" data-location=\"" + $(this).children("location").text() +
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
				"\" data-name=\"" + $(this).children("name").text() +
				"\" data-description=\"" + $(this).children("description").text() +
				"\" data-address=\"" + $(this).children("address").text() +
				"\" data-location=\"" + $(this).children("location").text() +


				"\" class=\"folder-object object-target" + 
				"\" href=\"#\">" + $(this).children("name").text() + "<span class='object-description'>Target</span>" + 
				"</a>" + findSubFolders($(this)) + "</li>";
				break;
			case "folder":
				data += "<li><a " + 
				"data-name=\"" + $(this).children("name").text() +
				"\" data-description=\"" + $(this).children("description").text() +
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

			$("#placeholder").hide();
			
			$(".folder-object").each(function() {
				$(this).removeClass("active");
			});
			
			$(this).addClass("active");
			
			$(".object-editor").each(function() {
				$(this).hide();
			});

			
			if (type == "folder") {
				$("#folder_edit .name").val($(this).attr("data-name"));
				$("#folder_edit .description").val($(this).attr("data-description"));
				$("#folder_edit").attr("data-id", $(this).attr("data-id"));
				$("#folder_edit").show();
			}

			if (type == "contact") {
                $("#contact_edit .alias").val($(this).attr("data-alias"));
                $("#contact_edit .firstname").val($(this).attr("data-firstname"));
                $("#contact_edit .lastname").val($(this).attr("data-lastname"));
                $("#contact_edit .email").val($(this).attr("data-email"));
                $("#contact_edit .phone").val($(this).attr("data-phone"));
                $("#contact_edit .location").val($(this).attr("data-location"));

                $("#contact_edit").attr("data-id", $(this).attr("data-id"));
                $("#contact_edit ").show();
      		}
			
			if (type == "target") {
				$("#target_edit .name").val($(this).attr("data-name"));
				$("#target_edit .description").val($(this).attr("data-description"));
				$("#target_edit .address").val($(this).attr("data-address"));
				$("#target_edit .location").val($(this).attr("data-location"));
				$("#target_edit").attr("data-id", $(this).attr("data-id"));
				$("#target_edit").show();
			}
			
			if (type == "user") {
			    $("#user_edit .username").val($(this).attr("data-username"));
				$("#user_edit .firstname").val($(this).attr("data-firstname"));
			    $("#user_edit .lastname").val($(this).attr("data-lastname"));
			    $("#user_edit .email").val($(this).attr("data-email"));
			    $("#user_edit .phone").val($(this).attr("data-phone"));
			    $("#user_edit .location").val($(this).attr("data-location"));

				$("#user_edit").attr("data-id", $(this).attr("data-id"));
				$("#user_edit").show();
			}
			
		}).each(function() {
			$(this).contextmenu(function(e) {
				$("#object_context_menu").css("top", e.pageY).css("left", e.pageX).show();
				e.preventDefault();
			}).click(function() {
				$(this).addClass("active");
			});
		}).draggable();

		
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