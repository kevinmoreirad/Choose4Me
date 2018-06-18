var password = document.getElementById("passwordRegister")
, confirm_password = document.getElementById("confirm");

function validatePassword(){
    console.log(password.value +" "+confirm_password.value)
if(password.value != confirm_password.value) {
  confirm_password.setCustomValidity("Passwords Don't Match");
} else {
  confirm_password.setCustomValidity('');
}
}

password.onchange = validatePassword;
confirm_password.onkeyup = validatePassword;