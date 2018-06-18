var urlApi = "http://localhost:9000/api"

function getAccount() {
    console.log(sessionStorage.getItem('token'));
    var xhttp = new XMLHttpRequest();
    xhttp.onprogress = waitForApiCallResponse(event);
    xhttp.open("GET", urlApi + "/accounts/" + sessionStorage.getItem('username'), true);
    xhttp.setRequestHeader("Content-type", "application/json");
    xhttp.setRequestHeader("token", sessionStorage.getItem('token'));
    xhttp.send();
    xhttp.onreadystatechange = function() {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if(xhttp.status != 200){
                alert("problem during connection log in again");
            } 
            else {
                var result = JSON.parse(xhttp.responseText);
                sessionStorage.setItem('id', result.id);
                // Get the modal
                var modalLogin = document.getElementById("myModalLogin");
                modalLogin.style.display = "none";

                location.replace("userPage.html");
            }
        }
    }
}

function postAccount() {
    var username = document.getElementById("usernameRegister").value;
    var password = document.getElementById("passwordRegister").value;
    var email = document.getElementById("email").value;
    var age = document.getElementById("age").value;
    var city = document.getElementById("city").value;
    var sex = "male";
    if(document.getElementById("radio2").checked) {
        sex = "female";
    }

    var accountJson = '{ "username":"' + username + '",' +
                         '"password":"' + password + '",'+
                         '"email":"' + email + '",'+
                         '"sex":"' + sex + '",'+
                         '"age":' + age + ','+
                         '"city":"' + city + '"'+
                       '}';
    var xhttp = new XMLHttpRequest();
    xhttp.onprogress = waitForApiCallResponse(event);
    xhttp.open("POST", urlApi + "/accounts", true);
    xhttp.setRequestHeader("Content-type", "application/json");
    xhttp.setRequestHeader("Access-Control-Allow-Origin", "*");
    xhttp.send(accountJson);
    xhttp.onreadystatechange = function() {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if(xhttp.status != 200){
                alert("This account already exists!");
            } 
            else {
                alert("Account created with success!");
                // Get the modal
                var modalRegister = document.getElementById("myModalRegister");
                modalRegister.style.display = "none";
 
            }
        }
    }

    return false;
}


function getSurveyList() {
    var username = sessionStorage.getItem('username');
    var token = sessionStorage.getItem('token');
    var xhttp = new XMLHttpRequest();
    xhttp.onprogress = waitForApiCallResponse(event);
    xhttp.open("GET", urlApi + "/accounts/" + username + "/surveys", true);
    xhttp.setRequestHeader("Content-type", "application/json");
    xhttp.setRequestHeader("token", token);
    xhttp.send();
    xhttp.onreadystatechange = function() {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            console.log(xhttp.status)
            if(xhttp.status != 200){
                alert("Problem with server connection!");
            } 
            else {
                var response = JSON.parse(xhttp.responseText);

                for(var i = 0; i < response.length; i++) {
                    var survey = response[i];
                
                    document.getElementById("surveyTable").innerHTML += '<tr> <td align="center"> <a class="btn btn-default" onclick="showStats('+survey.id+')" id="stats'+survey.id +'"><img style="width: 18px;" src="img/statsLogo.png"></a> <a class="btn btn-danger" onclick="deleteSurvey('+survey.id+')" id="delete'+survey.id +'""><em class="fa fa-trash"></em></a>' +
                    '</td> <td class="hidden-xs">'+ survey.id +'</td> <td>'+ survey.question +'</td></tr>'
                }
            }
        }
    }
}


function getSurvey(id) {
    var xhttp = new XMLHttpRequest();
    xhttp.onprogress = waitForApiCallResponse(event);
    xhttp.open("GET", urlApi + "/surveys/" + id, true);
    xhttp.setRequestHeader("Content-type", "application/json");
    xhttp.send();
    xhttp.onreadystatechange = function() {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if(xhttp.status != 200){
                alert("there is a problem with server connection!");
            } 
            else {
                var response = JSON.parse(xhttp.responseText);
                document.getElementById("responseText").innerHTML = response.question;
                
                sessionStorage.setItem('lastSurveyAnsweredId', response.id);
                //propositions
                var xhttp1 = new XMLHttpRequest();
                xhttp1.onprogress = waitForApiCallResponse(event);
                xhttp1.open("GET", urlApi + "/surveys/" + response.id + "/surveyPropositions", true);
                xhttp1.setRequestHeader("Content-type", "application/json");
                xhttp1.send();
                xhttp1.onreadystatechange = function() {
                    if (xhttp1.readyState == XMLHttpRequest.DONE) {
                        if(xhttp1.status != 200){
                            alert("Problem with server connection");
                            return null;
                        } 
                        else {
                            var responsePropositions = JSON.parse(xhttp1.responseText);
                            for(var i = 0; i < responsePropositions.length; i++) {
                                var surveyProp = responsePropositions[i];
                                if(surveyProp.response != null) {
                                    document.getElementById("propText"+(i+1)).innerHTML = surveyProp.response;
                                }
                                if(surveyProp.image != null) {
                                    document.getElementById("imageNewQuestion"+(i+1)).src = String.fromCharCode.apply(null, surveyProp.image);
                                }
                            }                                                                        
                        }
                    }
                }
            }
        }
    }
}


function postSurvey(question, isCorrect) {
    console.log(isCorrect);
    if(isCorrect) {
        var surveyJson = '{ "question": "' + question + '" ,' +
                            '"accountId":' + sessionStorage.getItem('id') +
                        '}';
        var xhttp = new XMLHttpRequest();
        xhttp.onprogress = waitForApiCallResponse(event);
        xhttp.open("POST", urlApi + "/accounts/" + sessionStorage.getItem('username') +"/surveys", true);
        xhttp.setRequestHeader("token", sessionStorage.getItem('token'));
        xhttp.setRequestHeader("Content-type", "application/json");
        xhttp.send(surveyJson);
        xhttp.onreadystatechange = function() {
            if (xhttp.readyState == XMLHttpRequest.DONE) {
                if(xhttp.status != 200){
                    alert("Problem with server connection");
                } 
                else {
                    alert("New survey added with succes!");
                    postSurveyPropositions(JSON.parse(xhttp.responseText).id);
                    
                    window.location.reload(true);
                }
            }
        }
    }
}


function deleteSurvey(id) {
    var xhttp = new XMLHttpRequest();
    xhttp.onprogress = waitForApiCallResponse(event);
    xhttp.open("DELETE", urlApi + "/accounts/" + sessionStorage.getItem('username') + "/surveys/" + id, true);
    xhttp.setRequestHeader("Content-type", "application/json");
    xhttp.setRequestHeader("token", sessionStorage.getItem('token'));
    xhttp.send();
    xhttp.onreadystatechange = function() {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if(xhttp.status != 200){
                alert("Problem with server connection");
            } 
            else {
                window.location.reload(true);
            }
        }
    }
}


function getSurveyPropositionList(id) {
    var xhttp = new XMLHttpRequest();
    xhttp.onprogress = waitForApiCallResponse(event);
    xhttp.open("GET", urlApi + "/surveys/" + id + "/surveyPropositions", true);
    xhttp.setRequestHeader("Content-type", "application/json");
    xhttp.send();
    xhttp.onreadystatechange = function() {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if(xhttp.status != 200){
                alert("Problem with server connection");
                return null;
            } 
            else {
                console.log(xhttp.responseText);
                return JSON.parse(xhttp.responseText);
            }
        }
    }
}

function postSurveyPropositions(surveyId) {
    var response1 = document.getElementById("responseText1").value;
    var choice1 = 1;
    var image1 = null;
    if(document.getElementById("in1Img") != null)
        image1 = document.getElementById("in1Img").src;
    postSurveyProposition(response1, choice1, surveyId, image1);

    var response2 = document.getElementById("responseText2").value;
    var choice2 = 2;
    var image2 = null;
    if(document.getElementById("in2Img") != null)
        image2 = document.getElementById("in2Img").src;
    postSurveyProposition(response2, choice2, surveyId, image2);
}
function postSurveyProposition(response, choiceNumb, surveyId, image) {
    var surveyPropositionJson = '{';
    if(response != "")
        surveyPropositionJson += '"response": "' + response +'",';
    surveyPropositionJson += '"choiceNumb":' + choiceNumb + ','+ '"surveyId":' + surveyId;
    if(image != null) {
        //convert to array of bytes
        var data = [];
        for (var i = 0; i < image.length; i++){  
            data.push(image.charCodeAt(i));
        }
        surveyPropositionJson += ', "image": [' + data + ']';
    }
    surveyPropositionJson += '}';
    console.log(surveyPropositionJson);
    var xhttp = new XMLHttpRequest();
    xhttp.onprogress = waitForApiCallResponse(event);
    xhttp.open("POST", urlApi + "/surveys/" + surveyId + "/surveyPropositions", true);
    xhttp.setRequestHeader("Content-type", "application/json");
    xhttp.setRequestHeader("token", sessionStorage.getItem('token'));
    xhttp.send(surveyPropositionJson);
}


function postAnswer(choice) {

    var answerJson = '{ "choice":' + choice + ',' +
                       '"accountId":' + sessionStorage.getItem('id') + ','+
                       '"surveyId":' + sessionStorage.getItem('lastSurveyAnsweredId') +
                     '}';
                     console.log(answerJson);
    var xhttp = new XMLHttpRequest();
    xhttp.onprogress = waitForApiCallResponse(event);
    xhttp.open("POST", urlApi + "/accounts/" + sessionStorage.getItem('username') + "/answers", true);
    xhttp.setRequestHeader("Content-type", "application/json");
    xhttp.setRequestHeader("token", sessionStorage.getItem('token'));
    xhttp.send(answerJson);
    xhttp.onreadystatechange = function() {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if(xhttp.status != 200){
                alert("Problem with server connection!");
            } 
            else {
                getStatList(sessionStorage.getItem('lastSurveyAnsweredId'))
            }
        }
    }
}

function getStatList(id) {
    var xhttp = new XMLHttpRequest();
    xhttp.onprogress = waitForApiCallResponse(event);
    xhttp.open("GET", urlApi + "/surveys/" + id + "/stats", true);
    xhttp.setRequestHeader("Content-type", "application/json");
    xhttp.send();
    xhttp.onreadystatechange = function() {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if(xhttp.status != 200){
                alert("Problem with server connection!");
            } 
            else {
                var response = JSON.parse(xhttp.responseText);
                console.log(response);
                document.getElementById("cityMostVotes").innerHTML = response.cityMost;
                document.getElementById("numberTotalVotes").innerHTML = response.nbVotes;
                document.getElementById("womenPercentage").setAttribute('style', "width:"+response.percentageWomenCh1 *100+"%;");
                document.getElementById("menPercentage").setAttribute('style', "width:"+response.percentageMenCh1 * 100+"%;");
                document.getElementById("percentage1To18").setAttribute('style', "width:"+response.percentage1To18Ch1 * 100+"%;");
                document.getElementById("percentage19To35").setAttribute('style', "width:"+response.percentage19To35Ch1 * 100+"%;");
                document.getElementById("percentage36To55").setAttribute('style', "width:"+response.percentage36To55Ch1 * 100+"%;");
                document.getElementById("percentage56To80").setAttribute('style', "width:"+response.percentage56To80Ch1 * 100+"%;");
                document.getElementById("percentage80More").setAttribute('style', "width:"+response.percentage80MoreCh1 * 100+"%;");
                document.getElementById("totalPercentage").setAttribute('style', "width:"+response.percentageTotalCh1 * 100+"%;");

                document.getElementById("progressValueWomen").innerHTML = response.percentageWomenCh1 *100+"%";
                document.getElementById("progressValueMen").innerHTML = response.percentageMenCh1 *100+"%";
                document.getElementById("progressValue1To18").innerHTML = response.percentage1To18Ch1 * 100+"%";
                document.getElementById("progressValue19To35").innerHTML = response.percentage19To35Ch1 * 100+"%";
                document.getElementById("progressValue36To55").innerHTML = response.percentage36To55Ch1 * 100+"%";
                document.getElementById("progressValue56To80").innerHTML = response.percentage56To80Ch1 * 100+"%";
                document.getElementById("progressValue80More").innerHTML = response.percentage80MoreCh1 * 100+"%";
                document.getElementById("progressValueTotal").innerHTML = response.percentageTotalCh1 * 100+"%";
            }
        }
    }
  

}


function getApiToken() {
    var username = document.getElementById("usernameLogin").value;
    var password = document.getElementById("passwordLogin").value;

    var xhttp = new XMLHttpRequest();
    xhttp.onprogress = waitForApiCallResponse(event);
    var params = "?username=" + username +"&password=" + password;
    xhttp.open("GET", urlApi + "/token" + params, true);
    xhttp.setRequestHeader("Content-type", "application/json");
    xhttp.setRequestHeader("Access-Control-Allow-Origin", "*");
    xhttp.send();
    xhttp.onreadystatechange = function() {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if(xhttp.status != 200){
                alert("This account doesn't exist or the communication with the server is down!");
            } 
            else {
                var response = JSON.parse(xhttp.responseText);
                sessionStorage.setItem('token', response);
                sessionStorage.setItem('username', username.toLowerCase());
                alert("Loged in with success!");
                getAccount();
            }
        }
    }
}


function getNewQuestion() {
    console.log("getnewquestion");
    var username = sessionStorage.getItem('username');
    var xhttp = new XMLHttpRequest();
    xhttp.onprogress = waitForApiCallResponse(event);
    xhttp.open("GET", urlApi + "/accounts/"+username+"/newquestion", true);
    xhttp.setRequestHeader("Content-type", "application/json");
    xhttp.setRequestHeader("token", sessionStorage.getItem('token'));
    xhttp.send();
    xhttp.onreadystatechange = function() {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if(xhttp.status != 200){
                alert("Really sorry, there is no questions left to answer!");
            } 
            else {
                openModalNewQuestion();
                var response = JSON.parse(xhttp.responseText);
                document.getElementById("responseText").innerHTML = response.question;
                
                sessionStorage.setItem('lastSurveyAnsweredId', response.id);
                //propositions
                var xhttp1 = new XMLHttpRequest();
                xhttp1.onprogress = waitForApiCallResponse(event);
                xhttp1.open("GET", urlApi + "/surveys/" + response.id + "/surveyPropositions", true);
                xhttp1.setRequestHeader("Content-type", "application/json");
                xhttp1.send();
                xhttp1.onreadystatechange = function() {
                    if (xhttp1.readyState == XMLHttpRequest.DONE) {
                        if(xhttp1.status != 200){
                            alert("Problem with server connection");
                            return null;
                        } 
                        else {
                            var responsePropositions = JSON.parse(xhttp1.responseText);
                            for(var i = 0; i < responsePropositions.length; i++) {
                                var surveyProp = responsePropositions[i];
                                if(surveyProp.response != null) {
                                    document.getElementById("propText"+(i+1)).innerHTML = surveyProp.response;
                                }
                                if(surveyProp.image != null) {
                                    document.getElementById("imageNewQuestion"+(i+1)).src = String.fromCharCode.apply(null, surveyProp.image);
                                }
                            }                                                                        
                        }
                    }
                }
            }
        }
    }
}

function waitForApiCallResponse(xhttp) {

}
    