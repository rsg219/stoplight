/*
https://stoplight-280.herokuapp.com/
var HOST = "localhost:3000";
var SERVER = "http://" + HOST + "/";
*/
var HOST = "stoplight-280.herokuapp.com";
var SERVER = "https://" + HOST + "/";

$(function () {
    console.log("on page load");
    attachEventHandlers();
    populateFolders();
    $('[data-toggle="tooltip"]').tooltip();
});

function createNewFolder() {
    console.log("clicked newFolder");
    let foldername = $("#folderName").val();
    let parentfolderid = 0;  // no parent folder yet
    let projectid = 0;
    let url = projectid+"/"+parentfolderid+"/folder";
    console.log("url: "+url);
    doAjaxCall("POST", url, {mFolderName:foldername}, 
    function (result) {
        console.log("received response from createNewFolder");
        let status = result.mStatus;
        console.log("status: "+status);
        if (status == "ok") {
            let folderid = result.mId;
            console.log("folderid: "+folderid);
            if (folderid != -1) {
                injectFolder(folderid, foldername);
            }
        }
    });
}

function uploadNewFile() {
    console.log("clicked upload new file");
    let filename = $("#filename").val();
    console.log("filename before send: "+filename);
    doAjaxCall("GET", "upload/file", {filename: filename},
    function (result) {
        console.log("success of upload file: "+result.success);
        if (result.success) {
            console.log("filename: "+ filename);
            let addfilehtml = '<div class="col-sm-3">' +
                    '<div class="well">' +
                        '<a href="#" data-toggle="tooltip" title="' + filename + '"><img src="Images/filered.png" class="center" alt="Red File"></a>' +
                        '<div class="name">' + filename + '</div>' +
                    '</div> </div>';
            $('#folders').append(addfilehtml);
        }
    });
    
    /* FormData Version 
    let formElem = $("#uploadFileForm");
    let formData = new FormData(formElem);
    doAjaxCall("POST", "upload/file", {formData: formData}, 
    function (result) {
        console.log("Post for file upload recieved");
    });
    */
}

function populateFolders() {
    let projectid = 0;
    let parentfolderid = 0;
    let url = projectid+"/"+parentfolderid+"/folder";
    doAjaxCall("GET", url, {}, 
    function (result) {
        console.log("populateFolders returned");
        let status = result.mStatus;
        console.log("status: "+status);
        if (status == "ok") {
            let mData = result.mData;
            for (let i=0; i<mData.length; i++) {
                let foldername = mData[i].mFolderName;
                let folderid = mData[i].mFolderId;
                let projectid = mData[i].mProjectId;
                //console.log("folder name: "+foldername);
                //console.log("folder id: "+folderid);
                //console.log("project id: "+projectid);
                injectFolder(folderid, foldername);
            }
        }
    });
}

function injectFolder(folderid, foldername) {
    let folderhtml = 
        '<div class="col-sm-3">'
            + '<div class="well">'
                + '<div class="folder" id="'+folderid+'">'
                    + '<a href="#" data-toggle="tooltip" title="'+foldername+'"><img src="Images/folderred.png" class="center" alt="Folder"></a>'
                    + '<div class="name">'+foldername+'</div>'
                + '</div>'
            + '</div>'
        + '</div>';
    $('#folders').append(folderhtml);
}

function backToAllProjects() {
    location.href = "allProjectsPage.html";
}

/* duplicate js code in filePage.js */
/* When the user clicks on the button, toggle between hiding and showing the dropdown content */
function showTasks() {
    console.log("show tasks called");
    $("#tasksDropdown").toggle();
    //document.getElementById("tasksDropdown").classList.toggle('show');
    console.log(document.getElementById("tasksDropdown"));
}

// Utility method for encapsulating the jQuery Ajax Call
function doAjaxCall(method, cmd, params, fcn) {
    $.ajax(
            SERVER + cmd,
            {
                type: method,
                processData: true,
                data: JSON.stringify(params),
                //data: params,
                dataType: "json",
                success: function (result) {
                    fcn(result)
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    console.log("params: "+params);
                    console.log("Error: " + jqXHR.responseText);
                    console.log("Error: " + textStatus);
                    console.log("Error: " + errorThrown);
                }
            }
    );
}

// Close the dropdown if the user clicks outside of it
window.onclick = function(e) {
    if (!e.target.matches('.dropbtn')) {
        if ($("#tasksDropdown").is(":visible")) {
            $("#tasksDropdown").hide();
        }
    }
}

function attachEventHandlers() {
    $('#newFolder').click(createNewFolder);
    $('#uploadNewFile').click(uploadNewFile);
    $('#backToAllProjects').click(backToAllProjects);

    $("#fileuploadbutton").click(function () {
        uploadNewFile();
    });

    $('#submitnewfolder').click(function () {
        createNewFolder();
    });
}