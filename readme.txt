To start Application run gradle build
after gradle build is complete, it will create build folder.
go to scripts folder
on windows run backend-java.bat
on linux run backend-java


Following is the script to create tables in database.
use challenge;

CREATE TABLE user_info(user_id VARCHAR(50) NOT NULL, password VARCHAR(100) NOT NULL, first_name VARCHAR(50), last_name VARCHAR(50), PRIMARY KEY(user_id));
INSERT INTO user_info(user_id, password) values('ashish', 'test');

CREATE TABLE messages(
message_id INTEGER AUTO_INCREMENT,
sender VARCHAR(50) NOT NULL,
receiver VARCHAR(50) NOT NULL,
message LONGTEXT,
message_type VARCHAR(50) NOT NULL default "text/plain",
image_height INTEGER,
image_width INTEGER,
video_length INTEGER,
video_source VARCHAR(50),
PRIMARY KEY(message_id),
FOREIGN KEY (sender) REFERENCES user_info(user_id),
FOREIGN KEY (receiver) REFERENCES user_info(user_id));
create index message_idx on messages (message_id);
create index sender_receiver_idx on messages (sender, receiver);


//Test script
// start server by
//go to http://localhost:8000/createUser
//add userName test1
//add password
//go to http://localhost:8000/createUser
//add userName test2
//add password

//go to http://localhost:8000/sendMessage
//add sender as test1
//add receiver as test2
//for regular message add this is test message
//go to http://localhost:8000/sendMessage
//add sender as test1
//add receiver as test2
// add below video url in message
//http://techslides.com/demos/sample-videos/small.webm
//go to http://localhost:8000/sendMessage
//add sender as test1
//add receiver as test2
// add below image url in message
//https://s-media-cache-ak0.pinimg.com/736x/82/b7/27/82b7271c43a81122cd3fc27591219db7.jpg

//go to http://localhost:8000/fetchMessages
//add first user as test1
//add second user as test2
//by default you will get all messages back.
//you can give number of messages you want to see,
//you can see page number you want to see
//then submit.

//go to http://localhost:8000/fetchMessages
//add first user as test2
//add second user as test1
//you should see same results for default page number and num pages


// My plan was to add a cache layer using hazelcast to cache message ids for a user duo key, and another cache for messages. this would reduce the number of hops to db.
