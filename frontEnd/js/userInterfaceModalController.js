 // Get the modal
 var modal = document.getElementById('myModalRespondQuestion');
 
 // Get the button that opens the modal
 var btn = document.getElementById("myBtnRespondQuestion");
 
 // Get the <span> element that closes the modal
 var span = document.getElementsByClassName("close")[0];
 
 // When the user clicks the button, open the modal 
 function openModalNewQuestion(){
     modal.style.display = "block";
 }
 function closeNewQuestion() {
    killStats();
    modal.style.display = "none";
 }
 // When the user clicks on <span> (x), close the modal
 span.onclick = function() {
    killStats();
     modal.style.display = "none";
 }

 // When the user clicks anywhere outside of the modal, close it
 window.onclick = function(event) {
     if (event.target == modal) {
         killStats();
         modal.style.display = "none";
     }
 }