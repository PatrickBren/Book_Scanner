from pyzbar import pyzbar
from imutils.video import VideoStream
from bs4 import BeautifulSoup
from firebase_admin import credentials
from firebase_admin import db
import firebase_admin
import cv2
import urllib2
import datetime
import imutils
import requests
import time
import ConfigParser
import socket
import threading


#Server Client Auth Settings
#Defines Server Values
listensocket = socket.socket()
Port = 8001
maxConnections = 999
IP = "192.168.43.10" #GetsHostname Of Current Macheine
listensocket.bind(('',Port))

#Opens Server
listensocket.listen(maxConnections)
print("Server started at " + IP + " on port " + str(Port))


#Firebase access information and firebase database URL
cred = credentials.Certificate('/home/pi/book-scann-firebase-adminsdk-ym2b5-f96f666660.json')
firebase_admin.initialize_app(cred, {'databaseURL':'https://book-scann.firebaseio.com/'})

print("[INFO] starting video stream...")

vs = VideoStream(src=0).start()
time.sleep(2.0)

found = set()

#Read the Useer Id from the config file
def User_Config():
    parser = ConfigParser.ConfigParser()
    parser.read('/home/pi/Final_Book_Scanner/config/config.ini')
    User_ID = parser.get('AUTHEINTICATION','user_id')
    return User_ID

#listen for any wifi sockets when a user logs in
#grabs the logged in users UserID
#overwrites config file with the new UserID
def getMessage():
    while True:
        (clientsocket, address) = listensocket.accept()
        print("New connection made!")
        message = clientsocket.recv(1024).decode() #Receives Message
        print (message)
        parser = ConfigParser.ConfigParser()
        parser.read('/home/pi/Final_Book_Scanner/config/config.ini')
        parser.set('AUTHEINTICATION','user_id',message) 
        with open('/home/pi/Final_Book_Scanner/config/config.ini', 'w') as configfile:
            parser.write(configfile)
        print ("Config File Updated")
        print ("===================================")
        time.sleep(1)

#Start getMessage thread to constantly listen for any socket data
threading.Thread(target=getMessage).start()
time.sleep(1)

#Starts the book scanning
while True:
    user_ID = str(User_Config())
    frame = vs.read()
    frame = imutils.resize(frame, width=400)
    barcodes = pyzbar.decode(frame)
    #Open CV decrypting barcode image
    for barcode in barcodes:
        (x, y, w, h) = barcode.rect
        cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 0, 255), 2)
        barcodeData = barcode.data.decode("utf-8")
        barcodeType = barcode.type
        
        text = "{} ({})".format(barcodeData, barcodeType)
        cv2.putText(frame, text, (x, y - 10),cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 255), 2)
        
        #EAN13 is exclusive to barcodes and prevents any QR codes from scanning
        if barcodeType == "EAN13":
                
            #print out scanned barcode data and type
            print "Barcode:",barcodeData," Barcode Type:",barcodeType
                    
            #Google Books API
            Url = ("https://www.googleapis.com/books/v1/volumes?q=isbn:"+barcodeData)
            print Url
         
            #Google Books Api and response code
            response = requests.get(Url)
            response_status = response.status_code
                    
            #404 error handling
            if response_status == 404:
                print("404 error, cannot find URL")
            else:
                try:
                    #Pull Data from the retrieved JSON
                    book_json = response.json()
                    book_data = book_json['items'][0]['volumeInfo']     
                    book_title = book_data['title']
                    book_id = book_json['items'][0]['id']
                    book_author = book_data['authors'][0]
                    book_publisher = book_data['publisher']
                    book_publish_date = book_data['publishedDate']
                    book_description = book_data['description']
                    book_pageCount = book_data['pageCount']
                    book_categories = book_data['categories'][0]
                    book_printType = book_data['printType']
                    book_buyLink = book_data['infoLink']
                    
                    #Book object with settings data and book data
                    book_object = {
                        "title":book_title,
                        "book_id":book_id,
                        "author":book_author,
                        "publisher":book_publisher,
                        "release_data":book_publish_date,
                        "description":book_description,
                        "page_count":book_pageCount,
                        "category":book_categories,
                        "print_type":book_printType,
                        "buy_link":book_buyLink,
                        "general_settings":{
                            "is_borrowed":False,
                            "is_bought":False,
                            "is_custom":False
                            },
                        "notification_settings":{
                            "is_new":True,
                            "return_date":{
                                "day": 0,
                                "month": 0,
                                "year": 0
                            }
                        }
                    }
                                        
                    #Firebase database reference for books
                    ref = db.reference('Users/'+user_ID+'/)
                                       
                    #Add the book to the firebase database
                    ref.push(book_object)
                                     
                    if key == ord("q"):
                        break
                        
                except Exception as e:
                    print "an Error has occured",e
                
        else:
            print("Wrong Barcode Type")
            
print("[INFO] cleaning up...")
cv2.destroyAllWindows()
vs.stop()