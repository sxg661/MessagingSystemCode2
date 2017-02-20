Repository Link:

https://git.cs.bham.ac.uk/sxg661/SoftwareWorkshop1QuitFunctionsxg661.git master e33763ee

Solution Explaination:

If typing in quit to quit always quits, it's impossible to have a client called quit. I added some validation to make sure the client can never call themself this.
The ClientSender has a loop that loops while a boolean variable called quit is false. When the constructor is called it's automatically set to false so that the loop will begin. When the client types in the recipient it checks to see if it is equal to "quit", and if it is sends a blank message ("") with recipient quit to the ServerReciever, then sets quit to true so that the loop terminates and the thread shuts down gracefully.
The ServerReciever has a loop similar to ClientSender with the boolean quit variable. When it recieves a message it checks to see if the recipient is "quit", and if it is adds a blank message with sender "quit" to the sender's queue. Then it sets quit to true to terminate the loop, then closes it's BufferReader with the client before the thread ends.
The ServerSender also has a loop with a quit boolean variable. When it gets to the message in the queue with the sender "quit" it reports to the server that the client has disconnected, sets quit to true so the loop terminates and before the thread ends it removes the clients queue from the ClientTable (I added a remove() method to the ClientTable class to do this) and closes the PrintStream with the client.
The ClientReciever class then throws an exception because the ServerSender has shut down, so it prints that it's disconected and then ends the thread.
The Client class waits for the threads to finishes and closes down it's PrintStream and BufferReader itself.

