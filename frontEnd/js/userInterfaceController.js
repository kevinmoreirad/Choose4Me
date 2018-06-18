function clearContents(element) {
    element.value = '';
  }

function showImg(element, file, idInput) {
    element.style.display = "none";

    var reader = new FileReader();
    reader.readAsDataURL(file);
    var imgElement = document.createElement('img');
    imgElement.setAttribute('class', 'img-square');
    imgElement.setAttribute('id', idInput + 'Img');     
    reader.onload = function () {
      imgElement.src = reader.result;
    };
    element.parentElement.setAttribute('class', 'img-background')
    element.parentElement.appendChild(imgElement);

}

function checkFileExtension(element, form) {
    var idInput = element.id;
    var fileName = element.files[0].name;
    var file = element.files[0];
    if(!fileName)
      return false;

    var extension = fileName.split(".");
    if(extension && extension.length > 1){
        extension = extension[extension.length-1].toUpperCase();
        if ((["JPG"].indexOf(extension) != -1) || (["PNG"].indexOf(extension) != -1) || (["JPEG"].indexOf(extension) != -1)) {
            showImg(form, file, idInput);
            return true;
        }
        else{
            alert("Browse to upload a valid File with png, jpg or jpeg extension");
            form.reset();
            return false;
        }
    }
    else{
        alert("Browse to upload a valid File with png, jpg or jpeg extension");
        form.reset();
        return false;
    }
}

function confirmAnswer(answerNumber) {
    postAnswer(answerNumber);
    showStatsPage();
}

function showStats(surveyId) {
    getSurvey(surveyId);
    getStatList(surveyId);
    showStatsPage();
}

function showStatsPage() {
    openModalNewQuestion();
    document.getElementById("answer1Btn").setAttribute("style", "visibility: hidden;");
    document.getElementById("answer2Btn").setAttribute("style", "visibility: hidden;");
    document.getElementById("statsAfterAnswer").style.display = "block";
}

function killStats() {
    console.log("kill stats!!")
    document.getElementById("answer1Btn").setAttribute("style", "visibility: visible; margin-top: 30px; margin-bottom:40px; text-align:center;");
    document.getElementById("answer2Btn").setAttribute("style", "visibility: visible;margin-top: 30px; margin-bottom:40px; text-align:center;");

    document.getElementById("statsAfterAnswer").style.display = "none";
}

function isAtLeastResponseOrImg () {
    if((document.getElementById("in2Img") == null && document.getElementById("responseText2").value == "") || (document.getElementById("in1Img") == null && document.getElementById("responseText1").value == "") || (document.getElementById("questionText").value == ""))
        return false;
    return true;

}
