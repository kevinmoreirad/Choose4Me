# Choose4Me
Scala project. Choose4Me let you ask a question to the community, and have stats
## How to get Started
1. Create a schema "choose4medb" in a local mysql instance
2. Set the path to the instance, and credentials in application.conf
   * slick.dbs.default.db.url = "jdbc:mysql://localhost:3306/choose4medb"
   * slick.dbs.default.db.user = ""
   * slick.dbs.default.db.password = ""
3. Open backend in Intelliji and run it. It will be running on http://localhost:9000
4. Go to frontend folder, and launch cmd. Run http-server 
   * (if you have not http-server installed go there : https://httpd.apache.org/download.cgi)
5. The website should be running on http://localhost:8080
6. If your backend is not running on http://localhost:9000, please go to frontend/js/apiController and change the : "var urlApi = "http://localhost:9000/api" to match your backend instance
