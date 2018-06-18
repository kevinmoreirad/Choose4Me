 // Get the modal
 var modal = document.getElementById('myModalLogin');
 
 // Get the button that opens the modal
 var btn = document.getElementById("myBtn");
 
 // Get the <span> element that closes the modal
 var span = document.getElementsByClassName("close")[0];
 
 // When the user clicks the button, open the modal 
 btn.onclick = function() {
     modal.style.display = "block";
 }
 
 // When the user clicks on <span> (x), close the modal
 span.onclick = function() {
     modal.style.display = "none";
 }
 
 // Get the modal
 var modalRegister = document.getElementById("myModalRegister");
 
 // Get the button that opens the modal
 var btnRegister = document.getElementById("myBtnRegister");
 
 // Get the <span> element that closes the modal
 var spanRegister = document.getElementsByClassName("closeRegister")[0];
 
 // When the user clicks the button, open the modal 
 btnRegister.onclick = function() {
     modalRegister.style.display = "block";
 }
 
 // When the user clicks on <span> (x), close the modal
 spanRegister.onclick = function() {
     modalRegister.style.display = "none";
 }


 // When the user clicks anywhere outside of the modal, close it
 window.onclick = function(event) {
     if (event.target == modal) {
         modal.style.display = "none";
     }
     else if(event.target == modalRegister) {
         modalRegister.style.display = "none";
     }
 }