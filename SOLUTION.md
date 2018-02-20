# Assignment 1 - Solution

## GitLab link

https://git.cs.bham.ac.uk/jxc1089/sww-assignment1.git

## Solution

To explain my approach, I believe it is best to look at all the system extensions for this assignment one by one.

###Login/register
The first task was the register/login functionality. To achieve this, I took advantage of the PrintStream and BufferedReader which was already in place from the quit exercise. I extended it so that registering and logging in were handled directly by the server. Only once the client had logged in were the sender and receiver threads started. I chose to login the user immediately after they register. I felt that from the user’s perspective it is most likely that they would want to login as the user they just registered. This streamlines the register and login process. I decided to allow multiple logins for the same user on different machines. This is because nowadays it is common practice for a user to own multiple devices and as such will want to access the instant messenger from all their devices. Multiple logins are achieved by having both instances of the user, with their own sender and receiver threads, reading from the same user in the client table.

###Logout
Logout is treated the same as quit was in the previous exercise. When the user types logout their threads end and the client program finishes. The server continues to operate normally.

###Keep all messages received by any user
To be able to keep all messages received by a user the BlockingQueue had to be changed. I decided to implement a CopyOnWriteArrayList. To put simply, it is a thread-safe version of an ArrayList. This allowed me to store every message each user receives. To be able to show the latest message to a client when they login I had to make some changes to how the server sender worked. I used a variable called oldSize to hold, at the beginning, 0 and then after the first message is shown, the size of the client’s messages list before a new message is received. By checking the oldSize to the new size of the client’s messages list I make sure to only ever show them one instance of their newest message.

###Previous, next, delete
To detect these commands, I used a switch statement in the client sender and server receiver. To implement previous and next I added a new variable called currentMessage which holds the pointer location of the current message. Therefore, when executing the previous or next command the pointer just moves to the previous or next message and prints it to the user. For the delete command I once again used the currentMessage pointer and simply removed that message from the client table. The current message is then what was previously the ‘next’ message. However, if the message deleted was the last message then the current message will now be the previous message.

 
###Self-chosen features

####Current command
I decided to implement a ‘current’ command for the client. This way they can check what the current message is. This can be especially useful if they have been deleting messages and want to know what the currently selected message is.

####File storage for users and messages
I implemented a file storage system for storing the users and their messages. This way when the server stops and starts up again the users can keep all their messages. I achieved this by storing a users.txt file which contains all the usernames. Then, for the messages, each user has their own specific file which stores all their messages.

####Username check
I implemented a simple check when the user tries to register a new username to avoid what I would assume to be accidental registers. For example, if the user enters just a space as a username it will not be allowed. Also, the user cannot include a semi-colon in their username. This is a limitation of how the server stores user messages. When reading in messages from the file it cuts the string on the semi-colon between the sender and the message contents. Therefore, a semi-colon in the username would disrupt this read.
