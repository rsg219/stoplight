$(function () {
    console.log("page load for file page");
    fillVersionDates();
    $('#backToProject').click(backToProject);
    $('#uploadNewVersion').click(uploadNewVersion);
});

function uploadNewVersion() {
    console.log("clicked upload new version of file");
    $('#files').append(`
        <div class="col-sm-3">
            <div class="well">
                <a href="#" data-toggle="tooltip" title="Yellow File"><img src="Images/fileyellow.png" class="center" alt="Yellow File"></a>
            <div class="name">Expense Report</div>
            <div class="date">March 26 2018</div>
        </div>
    `);
}


function fillVersionDates() {
    console.log("in fillVersionDates");
    let i = 20;
    $(".date").each(
        function(index, element) {
            $(element).html(getDateOfVersion(i));
            i -= 1;
        }
    );
}

function getDateOfVersion(version) {
    console.log("in getDateOfVerion, " + version);
    let time = version - 11;
    let date = "March " + version + " 2018,  " + time + ":00pm";
    return date;
    //return new Date(date);
}

function backToProject() {
    location.href = "projectPage.html";
}

/* When the user clicks on the button, toggle between hiding and showing the dropdown content */
function showTasks() {
    console.log("show tasks called");
    document.getElementById("tasksDropdown").classList.toggle('show');
    console.log(document.getElementById("tasksDropdown"));
}

// Close the dropdown if the user clicks outside of it
window.onclick = function(e) {
  if (!e.target.matches('.dropbtn')) {
    var myDropdown = document.getElementById("tasksDropdown");
      if (myDropdown.classList.contains('show')) {
        myDropdown.classList.remove('show');
      }
  }
}