import requests
import json
import time
from datetime import datetime, timedelta

url_cs = "https://bd-exp.andrew.cmu.edu:81"
url_ds = "https://bd-exp.andrew.cmu.edu:82"

client_id = "<CLIENT_ID>"
client_secret = "<CLIENT_SECRET>"

access_url = url_cs+"/oauth/access_token/client_id="+client_id+"/client_secret="+client_secret;

headers = {'content-type':'application/json',
			'charset' : 'utf-8',
			'Authorization' : 'Bearer ',
			'verify' : 'False'}

access_token = requests.get(access_url, headers = headers).json()
print access_token
headers['Authorization'] = 'Bearer '+ access_token['access_token']

make_url = url_cs + "/api/sensor"
make_data = {
			'data':{
               	"name":"Beacon1",
  				"identifier":"Beacon Deployment Beeks",
  				"building":"Beacon"
            	}
            }

post = requests.post(make_url, headers = headers, data = json.dumps(make_data))
uuid = post.json()['uuid']

make_tags_url = url_cs + "/api/sensor/"+uuid+"/tags"
tags_data = {
			  "data": 
			  		[
			           {
			            "name": "mac",
			            "value": ""
			           },
			           {
			            "name": "url",
			            "value": ""
			           },
			           {
			            "name": "battery",
			            "value": ""
			           },
			           {
			            "name": "EID",
			            "value": ""
			           },
			           {
			            "name": "iBeaconMajorID",
			            "value": ""
			           },
			           {
			            "name": "iBeaconMinorID",
			            "value": ""
			           },
			           {
			            "name": "iBeaconUUID",
			            "value": ""
			           },
			           {
			            "name": "latlong",
			            "value": ""
			           },
			           {
			            "name": "name",
			            "value": ""
			           },
			           {
			            "name": "rssi",
			            "value": ""
			           },
			           {
			            "name": "sBeaconID",
			            "value": ""
			           },
			           {
			            "name": "temperature",
			            "value": ""
			           },
			           {
			            "name": "uidInstance",
			            "value": ""
			           },
			           {
			            "name": "uidNamespace",
			            "value": ""
			           }
			        ]
			}

post = requests.post(make_tags_url, headers = headers, data = json.dumps(tags_data))
print post.text

